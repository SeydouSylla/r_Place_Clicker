package com.rplace.repository;

import com.rplace.modele.HistoriquePixel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

//Repository pour l'historique des placements.
//Fournit toutes les donnees pour les statistiques du sujet.
@Repository
public interface HistoriquePixelRepository extends JpaRepository<HistoriquePixel, Long> {

    // Total des pixels poses par un joueur depuis le debut
    long countByJoueurIdentifiant(Long joueurId);

    // Historique d'un joueur du plus recent au plus ancien
    List<HistoriquePixel> findByJoueurIdentifiantOrderByDatePoseDesc(Long joueurId);

    // Ce qu'on a corrige : on cherche le pixel le plus ancien ENCORE EN PLACE du joueur
    // Avant on prenait juste le plus vieux historique mais ca incluait les pixels
    // deja recouverts par d'autres joueurs ce qui n'etait pas correct
    // On ajoute deux conditions :
    //   - h.pixel.joueur.identifiant = :joueurId   (le pixel appartient encore au joueur)
    //   - h.datePose = h.pixel.datePose            (l'historique correspond au placement actuel)
    // Source JPQL :
    // https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html
    @Query("SELECT h FROM HistoriquePixel h " +
            "WHERE h.joueur.identifiant = :joueurId " +
            "AND h.pixel.joueur.identifiant = :joueurId " +
            "AND h.datePose = h.pixel.datePose " +
            "ORDER BY h.datePose ASC LIMIT 1")
    Optional<HistoriquePixel> trouverPixelLePlusAncien(Long joueurId);

    // Ce qu'onn ajoute de nouveau : trouve le placement suivant a la meme position apres une date donnee
    // Sert au calcul du record d'age : on cherche quand un pixel a ete recouvert
    // pour calculer combien de temps il a tenu avant d'etre remplace
    // Si pas de resultat le pixel est encore en place (record en cours)
    // Source Spring Data derived queries :
    // https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html#repositories.query-methods.query-creation
    Optional<HistoriquePixel> findFirstByPositionXAndPositionYAndDatePoseAfterOrderByDatePoseAsc(
            int positionX, int positionY, LocalDateTime apres);
}