package com.rplace.service;

import com.rplace.dto.PixelDto;
import com.rplace.exception.CreditInsuffisantException;
import com.rplace.exception.JoueurIntrouvableException;
import com.rplace.exception.PixelIntrouvableException;
import com.rplace.modele.*;
import com.rplace.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

// Ce service gere tout ce qui concerne les pixels de la grille
// Nous avons mis @Transactional pour que toutes les operations se fassent
// ensemble - si une etape echoue, tout est annule automatiquement
// Nous avons consulte comme source : https://docs.spring.io/spring-framework/reference/data-access/transaction.html
@Service
@Transactional
public class PixelService {

    private final PixelRepository pixelRepository;
    private final HistoriquePixelRepository historiqueRepository;
    private final JoueurRepository joueurRepository;

    // La grille fait 50x50 cases = 2500 pixels au total
    private static final int TAILLE_GRILLE = 50;

    // On utilise l'injection par constructeur comme recommande par Spring
    // Nous avons utilise comme source : https://docs.spring.io/spring-framework/reference/core/beans/dependencies/factory-collaborators.html
    public PixelService(PixelRepository pixelRepository,
                        HistoriquePixelRepository historiqueRepository,
                        JoueurRepository joueurRepository) {
        this.pixelRepository = pixelRepository;
        this.historiqueRepository = historiqueRepository;
        this.joueurRepository = joueurRepository;
    }

    // Cette methode cree les 2500 pixels au premier demarrage
    // On verifie d'abord si les pixels existent deja pour ne pas
    // les recreer a chaque redemarrage de l'application
    public void initialiserGrille() {
        if (pixelRepository.count() == 0) {
            for (int x = 0; x < TAILLE_GRILLE; x++) {
                for (int y = 0; y < TAILLE_GRILLE; y++) {
                    pixelRepository.save(new Pixel(x, y));
                }
            }
        }
    }

    // Methode principale pour poser un pixel sur la grille
    // On fait plusieurs verifications avant de sauvegarder :
    // 1. Le joueur existe
    // 2. Le pixel existe
    // 3. Le joueur a assez de credits
    public Pixel placerPixel(Long joueurId, int x, int y, String couleur) {

        // On recupere le joueur ou on lance une exception s'il n'existe pas
        Joueur joueur = joueurRepository.findById(joueurId)
                .orElseThrow(() -> new JoueurIntrouvableException(
                        "Joueur introuvable : " + joueurId));

        // On utilise un verrou pessimiste pour eviter que deux joueurs
        // posent sur le meme pixel exactement en meme temps
        // Nous avons consulte comme source : cours vu en classe et https://docs.spring.io/spring-data/jpa/reference/jpa/locking.html
        Pixel pixel = pixelRepository.trouverAvecVerrou(x, y)
                .orElseThrow(() -> new PixelIntrouvableException(
                        "Pixel introuvable en (" + x + "," + y + ")"));

        int prixActuel = pixel.getPrix();

        // On verifie que le joueur a assez de credits pour payer le pixel
        if (joueur.getCredits() < prixActuel) {
            throw new CreditInsuffisantException(
                    "Credits insuffisants : " + joueur.getCredits() +
                            " disponibles, " + prixActuel + " requis");
        }

        // Refactoring : on n'a plus besoin de manipuler manuellement
        // la collection pixelsActifs de l'ancien et du nouveau proprietaire.
        // Hibernate met automatiquement a jour la cle etrangere joueur_id
        // du pixel quand on fait pixel.setJoueur(joueur). La relation
        // OneToMany cote Joueur est derivee de cette FK et sera donc
        // rechargee correctement a la prochaine lecture de l'entite Joueur.
        // Avant on avait :
        //   if (pixel.getJoueur() != null) {
        //       pixel.getJoueur().getPixelsActifs().remove(pixel);
        //   }
        //   joueur.getPixelsActifs().add(pixel);
        // Ces lignes accedaient a une collection LAZY de l'autre joueur
        // ce qui pouvait causer une LazyInitializationException dans certains
        // contextes. On les a supprimees pour rendre le code plus robuste.

        // On debite les credits du joueur
        joueur.setCredits(joueur.getCredits() - prixActuel);

        // On met a jour le pixel avec la nouvelle couleur et le nouveau proprietaire
        // Hibernate va automatiquement faire l'UPDATE de la FK joueur_id en base
        pixel.setCouleur(couleur);
        pixel.setJoueur(joueur);
        pixel.setNbRecouvrements(pixel.getNbRecouvrements() + 1);
        pixel.setDatePose(LocalDateTime.now());

        joueur.setDerniereActivite(LocalDateTime.now());

        // On sauvegarde dans l'historique pour pouvoir faire les statistiques
        historiqueRepository.save(
                new HistoriquePixel(joueur, pixel, couleur, prixActuel));

        pixelRepository.save(pixel);
        joueurRepository.save(joueur);

        return pixel;
    }

    // Calcule le prix d'un pixel a une position donnee
    // Si le pixel n'existe pas on retourne 10 par defaut (prix de base)
    @Transactional(readOnly = true)
    public int calculerPrix(int x, int y) {
        return pixelRepository.findByPositionXAndPositionY(x, y)
                .map(Pixel::getPrix).orElse(10);
    }

    // Retourne tous les pixels de la grille sous forme de DTO
    // On utilise stream() pour parcourir la liste, map() pour convertir
    // chaque Pixel en PixelDto, et collect() pour recreer une liste
    // Nous avons consulte comme source Java Stream API :
    // https://docs.oracle.com/en/java/docs/api/java.base/java/util/stream/Stream.html
    // Nous avons consulte comme source Collectors :
    // https://docs.oracle.com/en/java/docs/api/java.base/java/util/stream/Collectors.html
    @Transactional(readOnly = true)
    public List<PixelDto> getGrilleComplete() {
        return pixelRepository.findAllOrdered().stream()
                .map(PixelDto::depuisPixel)
                .collect(Collectors.toList());
    }
}