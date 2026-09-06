package com.rplace.service;

import com.rplace.exception.JoueurIntrouvableException;
import com.rplace.modele.Joueur;
import com.rplace.repository.JoueurRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

// Service gerant les comptes joueurs.
// On a applique ici le principe SRP : uniquement la gestion des joueurs.
// n a applique ici principe DIP : injection par constructeur vers les interfaces.

@Service
@Transactional
public class JoueurService {

    private final JoueurRepository joueurRepository;
    private final PasswordEncoder encodeurMotDePasse;

    public JoueurService(JoueurRepository joueurRepository,
                         PasswordEncoder encodeurMotDePasse) {
        this.joueurRepository = joueurRepository;
        this.encodeurMotDePasse = encodeurMotDePasse;
    }


    //Inscrit un nouveau joueur.
    //on hache le mot de passe avec bcrypt avant stockage.

    public Joueur inscrire(String pseudo, String motDePasse,
                           Integer age, String pays) {
        if (joueurRepository.existsByPseudo(pseudo)) {
            throw new IllegalArgumentException(
                    "Le pseudo '" + pseudo + "' est deja utilise");
        }
        // Hachage bcrypt force 12 - (on a utilise ca pour une bonne pratique de securite)
        String motDePasseHache = encodeurMotDePasse.encode(motDePasse);
        return joueurRepository.save(
                new Joueur(pseudo, motDePasseHache, age, pays));
    }


    // Modifie le profil (age et pays seulement).
    // Le pseudo est immuable selon les regles du jeu.

    public Joueur modifierProfil(Long joueurId, Integer age, String pays) {
        Joueur joueur = trouverParId(joueurId);
        joueur.setAge(age);
        joueur.setPays(pays);
        return joueurRepository.save(joueur);
    }

    // Changement de mot de passe
    // Tous les champs sauf le pseudo sont modifiables.
    // On hache le nouveau mot de passe avec bcrypt
    // avant de l'enregistrer pour ne jamais stocker un mot de passe en clair
    public Joueur changerMotDePasse(Long joueurId, String nouveauMotDePasse) {
        Joueur joueur = trouverParId(joueurId);
        String motDePasseHache = encodeurMotDePasse.encode(nouveauMotDePasse);
        joueur.setMotDePasseHache(motDePasseHache);
        return joueurRepository.save(joueur);
    }

    @Transactional(readOnly = true)
    public Joueur trouverParId(Long id) {
        return joueurRepository.findById(id)
                .orElseThrow(() -> new JoueurIntrouvableException(
                        "Joueur introuvable : " + id));
    }

    @Transactional(readOnly = true)
    public Joueur trouverParPseudo(String pseudo) {
        return joueurRepository.findByPseudo(pseudo)
                .orElseThrow(() -> new JoueurIntrouvableException(
                        "Joueur introuvable : " + pseudo));
    }

    @Transactional(readOnly = true)
    public List<Joueur> listerTous() {
        return joueurRepository.findAll();
    }
}