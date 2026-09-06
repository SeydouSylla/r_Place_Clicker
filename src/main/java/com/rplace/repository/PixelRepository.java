package com.rplace.repository;

import com.rplace.modele.Pixel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

//Repository JPA pour les pixels de la grille.
//Verrou pessimiste sur trouverAvecVerrou : evite les race conditions
//quand deux joueurs posent sur la meme case simultanement.
@Repository
public interface PixelRepository extends JpaRepository<Pixel, Long> {

    // Lecture avec verrou exclusif pour l'ecriture (evite les conflits)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Pixel p WHERE p.positionX = :x AND p.positionY = :y")
    Optional<Pixel> trouverAvecVerrou(int x, int y);

    // Lecture seule pour l'affichage (sans verrou)
    Optional<Pixel> findByPositionXAndPositionY(int positionX, int positionY);

    // Tous les pixels d'un joueur encore en place
    List<Pixel> findByJoueurIdentifiant(Long joueurId);

    // Grille complete triee pour l'affichage initial
    @Query("SELECT p FROM Pixel p ORDER BY p.positionX, p.positionY")
    List<Pixel> findAllOrdered();
}
