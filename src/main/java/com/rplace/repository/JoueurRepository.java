package com.rplace.repository;

import com.rplace.modele.Joueur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

//Repository JPA pour les joueurs.
//Pattern DAO : decouplage total entre service et base de donnees.
//C'est Spring Data qui genere automatiquement le SQL a partir des noms de methodes.
@Repository
public interface JoueurRepository extends JpaRepository<Joueur, Long> {

    // Recherche par pseudo (connexion + inscription)
    Optional<Joueur> findByPseudo(String pseudo);

    // Verification unicite du pseudo a l'inscription
    boolean existsByPseudo(String pseudo);

    // Classement par nombre de pixels en place (couverture de la grille)
    @Query("SELECT j FROM Joueur j LEFT JOIN j.pixelsActifs p " +
            "GROUP BY j ORDER BY COUNT(p) DESC")
    List<Joueur> trierParNbPixelsEnPlace();

    // Nettoyage : on a supprime findAllByOrderByDateInscriptionAsc
    // Si on a besoin de tous les joueurs dans l'ordre par defaut on utilise findAll()

    // Classement par plus vieux pixel encore en place du joueur
    // "liste ordonnee des joueurs dans l'ordre du plus vieux pixel par joueur"
    // On fait un INNER JOIN avec pixelsActifs pour ne garder que les joueurs
    // qui ont au moins un pixel encore en place puis on prend MIN(datePose)
    // pour trouver le pixel le plus ancien de chacun
    // Source JPQL aggregations :
    // https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html
    @Query("SELECT j FROM Joueur j JOIN j.pixelsActifs p " +
            "GROUP BY j ORDER BY MIN(p.datePose) ASC")
    List<Joueur> trierParPlusVieuxPixelEnPlace();
}