package com.rplace.repository;

import com.rplace.modele.TypeBonus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

//Repository pour le catalogue des types de bonus.
@Repository
public interface TypeBonusRepository extends JpaRepository<TypeBonus, Long> {
    boolean existsByNom(String nom);
}
