-- Creation de la base de donnees rplace_db;

CREATE TABLE IF NOT EXISTS joueur (
    identifiant        BIGSERIAL PRIMARY KEY,
    pseudo             VARCHAR(20)  UNIQUE NOT NULL,
    mot_de_passe_hache VARCHAR(255) NOT NULL,
    age                INTEGER      CHECK (age > 0 AND age < 121),
    pays               VARCHAR(60),
    credits            BIGINT       NOT NULL DEFAULT 50,
    date_inscription   TIMESTAMP    NOT NULL DEFAULT NOW(),
    derniere_activite  TIMESTAMP    DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS pixel (
    identifiant      BIGSERIAL PRIMARY KEY,
    position_x       INTEGER NOT NULL CHECK (position_x >= 0 AND position_x < 50),
    position_y       INTEGER NOT NULL CHECK (position_y >= 0 AND position_y < 50),
    couleur          VARCHAR(7),
    nb_recouvrements INTEGER NOT NULL DEFAULT 0,
    joueur_id        BIGINT  REFERENCES joueur(identifiant),
    date_pose        TIMESTAMP,
    UNIQUE (position_x, position_y)
);

CREATE TABLE IF NOT EXISTS historique_pixel (
    identifiant BIGSERIAL PRIMARY KEY,
    position_x  INTEGER   NOT NULL,
    position_y  INTEGER   NOT NULL,
    couleur     VARCHAR(7) NOT NULL,
    joueur_id   BIGINT    NOT NULL REFERENCES joueur(identifiant),
    pixel_id    BIGINT    NOT NULL REFERENCES pixel(identifiant),
    cout_paye   INTEGER   NOT NULL,
    date_pose   TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS type_bonus (
    identifiant    BIGSERIAL PRIMARY KEY,
    nom            VARCHAR(50)    UNIQUE NOT NULL,
    categorie      VARCHAR(20)    NOT NULL,
    prix_base      INTEGER        NOT NULL,
    multiplicateur NUMERIC(10,4)  NOT NULL DEFAULT 0,
    description    VARCHAR(200)
);

CREATE TABLE IF NOT EXISTS joueur_bonus (
    identifiant   BIGSERIAL PRIMARY KEY,
    joueur_id     BIGINT NOT NULL REFERENCES joueur(identifiant),
    type_bonus_id BIGINT NOT NULL REFERENCES type_bonus(identifiant),
    quantite      INTEGER NOT NULL DEFAULT 1,
    date_achat    TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (joueur_id, type_bonus_id)
);

-- Index pour les requetes frequentes
CREATE INDEX IF NOT EXISTS idx_pixel_joueur
    ON pixel(joueur_id);
CREATE INDEX IF NOT EXISTS idx_historique_joueur
    ON historique_pixel(joueur_id);
CREATE INDEX IF NOT EXISTS idx_historique_date
    ON historique_pixel(date_pose);