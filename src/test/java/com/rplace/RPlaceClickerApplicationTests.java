package com.rplace;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/*Test d'integration : verifie que l'application demarre correctement
 avec une vraie base PostgreSQL.
 Testcontainers demarre une base PostgreSQL jetable dans un conteneur Docker
 le temps du test. Flyway y applique les migrations, puis Hibernate valide
 le schema. Cela garantit que l'application demarre dans des conditions
 proches de la production, sans dependre d'une base preexistante.
 */
@SpringBootTest
@Testcontainers
class RPlaceClickerApplicationTests {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"));

    // Injecte dynamiquement l'URL, l'utilisateur et le mot de passe
    // du conteneur dans les proprietes Spring, avant le demarrage du contexte.
    @DynamicPropertySource
    static void proprietesBase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void contextLoads() {
        // Le test reussit si le contexte Spring demarre :
        // base connectee, migrations Flyway appliquees, schema valide.
    }
}