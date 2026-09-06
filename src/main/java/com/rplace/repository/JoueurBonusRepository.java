package com.rplace.repository;

import com.rplace.modele.JoueurBonus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

//Repository pour les bonus possedes par les joueurs.
@Repository
public interface JoueurBonusRepository extends JpaRepository<JoueurBonus, Long> {

    List<JoueurBonus> findByJoueurIdentifiant(Long joueurId);

    Optional<JoueurBonus> findByJoueurIdentifiantAndTypeBonusIdentifiant(
            Long joueurId, Long typeBonusId);
}
