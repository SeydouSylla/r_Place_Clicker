package com.rplace.service;

import com.rplace.dto.JoueurDto;
import com.rplace.modele.HistoriquePixel;
import com.rplace.repository.HistoriquePixelRepository;
import com.rplace.repository.JoueurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// Ce service s'occupe uniquement des statistiques et des classements des joueurs
// On a separe cette partie pour respecter le principe SRP (Single Responsibility)
// Toutes les methodes ne font que de la lecture donc on met readOnly = true
// au niveau de la classe pour optimiser les transactions
// Source @Transactional(readOnly) :
// https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/annotations.html
@Service
@Transactional(readOnly = true)
public class StatService {

    private final JoueurRepository joueurRepository;
    private final HistoriquePixelRepository historiqueRepository;

    // Injection par constructeur (recommandee par Spring plutot que @Autowired sur les champs)
    public StatService(JoueurRepository joueurRepository,
                       HistoriquePixelRepository historiqueRepository) {
        this.joueurRepository = joueurRepository;
        this.historiqueRepository = historiqueRepository;
    }

    // Retourne le classement des joueurs tries par nombre de pixels encore en place
    // On convertit chaque Joueur en JoueurDto pour ne pas exposer l'entite directement
    // au front (separation des couches)
    public List<JoueurDto> getClassementParCouverture() {
        return joueurRepository.trierParNbPixelsEnPlace()
                .stream().map(JoueurDto::depuisJoueur).collect(Collectors.toList());
    }

    // Nettoyage : on a supprime la methode getClassementParAnciennete()
    // et findAllByOrderByDateInscriptionAsc()
    // dans le repository.

    // Classement par plus vieux pixel encore en place du joueur
    // "liste ordonnee des joueurs dans l'ordre du plus vieux pixel par joueur"
    // Les joueurs sont tries par anciennete de leur pixel le plus vieux ENCORE en place
    // Ca recompense ceux qui arrivent a tenir leur territoire dans la duree
    public List<JoueurDto> getClassementParPlusVieuxPixel() {
        return joueurRepository.trierParPlusVieuxPixelEnPlace()
                .stream().map(JoueurDto::depuisJoueur).collect(Collectors.toList());
    }

    // Compte le nombre total de pixels qu'un joueur a place dans toute son historique
    // (meme ceux qui ont ete recouverts par d'autres joueurs apres)
    public long getNbPixelsTotaux(Long joueurId) {
        return historiqueRepository.countByJoueurIdentifiant(joueurId);
    }

    // Trouve le pixel le plus ancien d'un joueur qui est encore visible sur la grille
    // Si le joueur n'a aucun pixel encore en place on retourne null grace a orElse(null)
    public HistoriquePixel getPixelLePlusAncienEnPlace(Long joueurId) {
        return historiqueRepository.trouverPixelLePlusAncien(joueurId).orElse(null);
    }

    // Calcule le record d'age d'un pixel pour un joueur
    // "le record d'age pour un pixel (il peut ne plus exister)"
    // C'est donc la duree de vie maximale qu'un pixel pose par le joueur a tenu avant
    // d'etre recouvert. Si le pixel est encore en place on prend la duree depuis sa pose
    //
    // Algorithme :
    //   - On parcourt tous les placements faits par le joueur
    //   - Pour chaque placement on cherche le placement suivant a la meme position
    //   - Si on en trouve un -> duree = date_recouvrement - date_pose
    //      Sinon -> duree = maintenant - date_pose (le pixel est encore en place)
    //   - On garde le maximum
    public Duration getRecordAgePixel(Long joueurId) {
        List<HistoriquePixel> historiques =
                historiqueRepository.findByJoueurIdentifiantOrderByDatePoseDesc(joueurId);

        if (historiques.isEmpty()) {
            return Duration.ZERO;
        }

        Duration record = Duration.ZERO;
        LocalDateTime maintenant = LocalDateTime.now();

        for (HistoriquePixel h : historiques) {
            // On cherche si quelqu'un a recouvert ce pixel apres
            Optional<HistoriquePixel> suivant = historiqueRepository
                    .findFirstByPositionXAndPositionYAndDatePoseAfterOrderByDatePoseAsc(
                            h.getPositionX(), h.getPositionY(), h.getDatePose());

            // Si recouvert -> on prend la date du recouvrement comme fin
            // Sinon -> le pixel est encore en place donc fin = maintenant
            LocalDateTime fin = suivant.map(HistoriquePixel::getDatePose).orElse(maintenant);
            Duration duree = Duration.between(h.getDatePose(), fin);

            if (duree.compareTo(record) > 0) {
                record = duree;
            }
        }

        return record;
    }

    // Methode utilitaire pour afficher une duree dans un format lisible
    // Exemple : "2j 3h 15min" au lieu de "PT51H15M" qui est illisible
    // Source Duration.toDaysPart() / toHoursPart() / toMinutesPart() :
    // https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/time/Duration.html
    public static String formaterDuree(Duration d) {
        if (d == null || d.isZero()) {
            return "—";
        }
        long jours = d.toDays();
        long heures = d.toHoursPart();
        long minutes = d.toMinutesPart();
        long secondes = d.toSecondsPart();

        StringBuilder sb = new StringBuilder();
        if (jours > 0) sb.append(jours).append("j ");
        if (heures > 0) sb.append(heures).append("h ");
        if (minutes > 0) sb.append(minutes).append("min ");
        if (sb.length() == 0) sb.append(secondes).append("s");
        return sb.toString().trim();
    }

    // Retourne la liste complete des joueurs sous forme de DTO
    // Pattern classique : stream -> map -> collect pour transformer une liste
    // Source Stream et Collectors :
    // https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/stream/Collectors.html
    public List<JoueurDto> getTousLesJoueurs() {
        return joueurRepository.findAll()
                .stream().map(JoueurDto::depuisJoueur).collect(Collectors.toList());
    }
}