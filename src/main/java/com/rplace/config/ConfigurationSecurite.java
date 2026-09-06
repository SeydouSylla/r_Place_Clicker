package com.rplace.config;

import com.rplace.securite.ServiceDetailsJoueur;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

// Configuration Spring Security.
// Definit les regles d'acces par URL et le hachage bcrypt des mots de passe.

@Configuration
@EnableWebSecurity
public class ConfigurationSecurite {

    private final ServiceDetailsJoueur serviceDetailsJoueur;

    public ConfigurationSecurite(ServiceDetailsJoueur serviceDetailsJoueur) {
        this.serviceDetailsJoueur = serviceDetailsJoueur;
    }


    // Ici aussi on a utilise l'encodeur bcrypt force 12
    // pour ne jamais stocker les mots de passes en claires

    @Bean
    public PasswordEncoder encodeurMotDePasse() {
        return new BCryptPasswordEncoder(12);
    }

    // Regles d'acces : qui peut acceder a quoi.
    @Bean
    public SecurityFilterChain filtreSecurite(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Pages accessibles a tous (visiteurs inclus)
                        // On a ajoute /jeu et /stats/** parce que le cahier des charges dit :
                        // "Un utilisateur non connecte verra la grille etre modifiee en temps reel
                        //  et pourra consulter qui a pose chaque pixel ainsi que le classement
                        //  des utilisateurs en temps reel mais ne pourra pas interagir."
                        // Donc la consultation doit etre publique, seule l'interaction est protegee
                        .requestMatchers("/", "/accueil", "/connexion",
                                "/inscription", "/regles",
                                "/jeu", "/stats/**").permitAll()
                        // Ressources statiques et WebSocket accessibles a tous
                        .requestMatchers("/css/**", "/js/**", "/ws/**",
                                "/webjars/**", "/images/**").permitAll()
                        // API de la grille accessible en lecture a tous
                        .requestMatchers("/api/pixels").permitAll()
                        // Tout le reste necessite d'etre connecte
                        // (l'API credits, bonus et le profil restent proteges
                        //  car ce sont des actions de joueur authentifie)
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/connexion")
                        .loginProcessingUrl("/connexion")
                        .usernameParameter("pseudo")
                        .passwordParameter("motDePasse")
                        .defaultSuccessUrl("/jeu", true)
                        .failureUrl("/connexion?erreur")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/deconnexion")
                        .logoutSuccessUrl("/connexion?deconnecte")
                        .invalidateHttpSession(true)   // detruire la session
                        .deleteCookies("JSESSIONID")   // supprimer le cookie
                        .permitAll()
                )
                // CSRF desactive pour les appels REST et WebSocket
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**", "/ws/**"));

        return http.build();
    }

    @Bean
    public AuthenticationManager gestionnaireAuth(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder builder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.userDetailsService(serviceDetailsJoueur)
                .passwordEncoder(encodeurMotDePasse());
        return builder.build();
    }
}