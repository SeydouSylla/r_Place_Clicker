package com.rplace.securite;

import com.rplace.modele.Joueur;
import com.rplace.repository.JoueurRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

 // Charge le joueur pour Spring Security lors de la connexion.
 // On a utilise le Pattern Adapter pour adapte notre Joueur a l'interface UserDetails.
 // L'identifiant est utilise comme "username" pour @AuthenticationPrincipal.

@Service
public class ServiceDetailsJoueur implements UserDetailsService {

    private final JoueurRepository joueurRepository;

    public ServiceDetailsJoueur(JoueurRepository joueurRepository) {
        this.joueurRepository = joueurRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String pseudo)
            throws UsernameNotFoundException {
        Joueur joueur = joueurRepository.findByPseudo(pseudo)
                .orElseThrow(() -> new UsernameNotFoundException(
                    "Joueur introuvable : " + pseudo));

        // L'identifiant sert de "username" interne pour recuperer le joueur
        // dans les controleurs via @AuthenticationPrincipal
        return User.builder()
                .username(String.valueOf(joueur.getIdentifiant()))
                .password(joueur.getMotDePasseHache())
                .roles("JOUEUR")
                .build();
    }
}
