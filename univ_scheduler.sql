-- ================================================================
--  UNIV-SCHEDULER — Script de création de la base de données
--  Base : MySQL
--  Projet L2 Informatique 2026
-- ================================================================

-- Créer et sélectionner la base de données
DROP DATABASE IF EXISTS univ_scheduler;
CREATE DATABASE univ_scheduler
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE univ_scheduler;

-- ================================================================
--  TABLE : utilisateurs
-- ================================================================
CREATE TABLE utilisateurs (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    nom          VARCHAR(50)  NOT NULL,
    prenom       VARCHAR(50)  NOT NULL,
    email        VARCHAR(100) NOT NULL UNIQUE,
    mot_de_passe VARCHAR(255) NOT NULL,
    role         ENUM('ADMIN','GESTIONNAIRE','ENSEIGNANT','ETUDIANT') NOT NULL,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ================================================================
--  TABLE : enseignants (extension de utilisateurs)
-- ================================================================
CREATE TABLE enseignants (
    id           INT PRIMARY KEY,
    specialite   VARCHAR(100),
    departement  VARCHAR(100),
    FOREIGN KEY (id) REFERENCES utilisateurs(id) ON DELETE CASCADE
);

-- ================================================================
--  TABLE : etudiants (extension de utilisateurs)
-- ================================================================
CREATE TABLE etudiants (
    id               INT PRIMARY KEY,
    classe           VARCHAR(50),
    groupe           VARCHAR(20),
    numero_etudiant  VARCHAR(20) UNIQUE,
    FOREIGN KEY (id) REFERENCES utilisateurs(id) ON DELETE CASCADE
);

-- ================================================================
--  TABLE : gestionnaires (extension de utilisateurs)
-- ================================================================
CREATE TABLE gestionnaires (
    id       INT PRIMARY KEY,
    service  VARCHAR(100),
    FOREIGN KEY (id) REFERENCES utilisateurs(id) ON DELETE CASCADE
);

-- ================================================================
--  TABLE : batiments
-- ================================================================
CREATE TABLE batiments (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    nom            VARCHAR(100) NOT NULL,
    localisation   VARCHAR(200),
    nombre_etages  INT DEFAULT 1
);

-- ================================================================
--  TABLE : salles
-- ================================================================
CREATE TABLE salles (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    numero       VARCHAR(20)  NOT NULL UNIQUE,
    capacite     INT          NOT NULL,
    type         ENUM('TD','TP','AMPHI','REUNION') NOT NULL,
    disponible   BOOLEAN DEFAULT TRUE,
    batiment_id  INT NOT NULL,
    FOREIGN KEY (batiment_id) REFERENCES batiments(id) ON DELETE CASCADE
);

-- ================================================================
--  TABLE : equipements
-- ================================================================
CREATE TABLE equipements (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    nom          VARCHAR(100) NOT NULL,
    description  VARCHAR(255),
    fonctionnel  BOOLEAN DEFAULT TRUE,
    salle_id     INT NOT NULL,
    FOREIGN KEY (salle_id) REFERENCES salles(id) ON DELETE CASCADE
);

-- ================================================================
--  TABLE : creneaux
-- ================================================================
CREATE TABLE creneaux (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    jour         ENUM('Lundi','Mardi','Mercredi','Jeudi','Vendredi') NOT NULL,
    heure_debut  TIME NOT NULL,
    duree_minutes INT NOT NULL
);

-- ================================================================
--  TABLE : cours
-- ================================================================
CREATE TABLE cours (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    matiere        VARCHAR(100) NOT NULL,
    description    TEXT,
    classe         VARCHAR(50)  NOT NULL,
    groupe         VARCHAR(20),
    enseignant_id  INT NOT NULL,
    salle_id       INT NOT NULL,
    creneau_id     INT NOT NULL,
    FOREIGN KEY (enseignant_id) REFERENCES utilisateurs(id),
    FOREIGN KEY (salle_id)      REFERENCES salles(id),
    FOREIGN KEY (creneau_id)    REFERENCES creneaux(id)
);

-- ================================================================
--  TABLE : cours_etudiants (relation plusieurs-à-plusieurs)
-- ================================================================
CREATE TABLE cours_etudiants (
    cours_id    INT NOT NULL,
    etudiant_id INT NOT NULL,
    PRIMARY KEY (cours_id, etudiant_id),
    FOREIGN KEY (cours_id)    REFERENCES cours(id)       ON DELETE CASCADE,
    FOREIGN KEY (etudiant_id) REFERENCES utilisateurs(id) ON DELETE CASCADE
);

-- ================================================================
--  TABLE : reservations
-- ================================================================
CREATE TABLE reservations (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    date_reserv    DATE         NOT NULL,
    motif          VARCHAR(255),
    statut         ENUM('EN_ATTENTE','CONFIRMEE','ANNULEE') DEFAULT 'EN_ATTENTE',
    utilisateur_id INT NOT NULL,
    salle_id       INT NOT NULL,
    creneau_id     INT NOT NULL,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id),
    FOREIGN KEY (salle_id)       REFERENCES salles(id),
    FOREIGN KEY (creneau_id)     REFERENCES creneaux(id)
);

-- ================================================================
--  TABLE : conflits
-- ================================================================
CREATE TABLE conflits (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    description  TEXT,
    type         ENUM('SALLE_OCCUPEE','ENSEIGNANT_INDISPONIBLE','CAPACITE_INSUFFISANTE'),
    resolu       BOOLEAN DEFAULT FALSE,
    cours1_id    INT,
    cours2_id    INT,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (cours1_id) REFERENCES cours(id) ON DELETE SET NULL,
    FOREIGN KEY (cours2_id) REFERENCES cours(id) ON DELETE SET NULL
);

-- ================================================================
--  TABLE : rapports
-- ================================================================
CREATE TABLE rapports (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    titre            VARCHAR(200) NOT NULL,
    type             ENUM('HEBDOMADAIRE','MENSUEL','OCCUPATION') NOT NULL,
    contenu          TEXT,
    date_generation  DATE DEFAULT (CURRENT_DATE),
    genere_par       INT NOT NULL,
    FOREIGN KEY (genere_par) REFERENCES utilisateurs(id)
);

-- ================================================================
--  DONNÉES DE TEST
-- ================================================================

-- Bâtiments
INSERT INTO batiments (nom, localisation, nombre_etages) VALUES
('Bâtiment A', 'Campus Principal', 3),
('Bâtiment B', 'Campus Principal', 2),
('Bâtiment C', 'Campus Annexe',    2);

-- Salles
INSERT INTO salles (numero, capacite, type, disponible, batiment_id) VALUES
('A101', 40,  'TD',     TRUE,  1),
('A102', 25,  'TP',     FALSE, 1),
('A103', 35,  'TD',     TRUE,  1),
('B201', 150, 'AMPHI',  TRUE,  2),
('B202', 35,  'TD',     FALSE, 2),
('C101', 20,  'REUNION',TRUE,  3),
('C102', 30,  'TP',     TRUE,  3);

-- Équipements
INSERT INTO equipements (nom, description, fonctionnel, salle_id) VALUES
('Vidéoprojecteur', 'Epson EB-S41',        TRUE,  1),
('Tableau interactif', 'Smart Board',       TRUE,  1),
('Climatisation',    'Unité murale',        TRUE,  2),
('Vidéoprojecteur', 'BenQ MX550',          TRUE,  4),
('Micro sans fil',   'Sennheiser XSW',      TRUE,  4),
('Ordinateurs',     '25 postes Dell',       TRUE,  2);

-- Utilisateurs
INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, role) VALUES
('Admin',   'Super',    'admin@univ.sn',      'admin123',  'ADMIN'),
('Sow',     'Ibrahima', 'gest@univ.sn',        'gest123',   'GESTIONNAIRE'),
('Diallo',  'Moussa',   'm.diallo@univ.sn',    'pass123',   'ENSEIGNANT'),
('Seck',    'Aminata',  'a.seck@univ.sn',      'pass456',   'ENSEIGNANT'),
('Ba',      'Oumar',    'o.ba@univ.sn',        'pass789',   'ENSEIGNANT'),
('Ndiaye',  'Fatou',    'f.ndiaye@etu.sn',     'pass456',   'ETUDIANT'),
('Fall',    'Cheikh',   'c.fall@etu.sn',       'pass123',   'ETUDIANT');

-- Enseignants (données supplémentaires)
INSERT INTO enseignants (id, specialite, departement) VALUES
(3, 'Informatique',  'UFR Sciences'),
(4, 'Mathématiques', 'UFR Sciences'),
(5, 'Réseaux',       'UFR Sciences');

-- Étudiants
INSERT INTO etudiants (id, classe, groupe, numero_etudiant) VALUES
(6, 'L2 Informatique', 'Groupe A', '2024001'),
(7, 'L2 Informatique', 'Groupe B', '2024002');

-- Gestionnaire
INSERT INTO gestionnaires (id, service) VALUES
(2, 'Scolarité');

-- Créneaux
INSERT INTO creneaux (jour, heure_debut, duree_minutes) VALUES
('Lundi',    '08:00:00', 120),
('Lundi',    '10:00:00', 90),
('Mardi',    '08:00:00', 120),
('Mardi',    '14:00:00', 90),
('Mercredi', '10:00:00', 120),
('Jeudi',    '08:00:00', 90),
('Vendredi', '14:00:00', 120);

-- Cours
INSERT INTO cours (matiere, classe, groupe, enseignant_id, salle_id, creneau_id) VALUES
('Algorithmique',  'L2 Informatique', 'Groupe A', 3, 1, 1),
('Mathématiques',  'L2 Informatique', 'Groupe A', 4, 4, 2),
('Réseaux',        'L2 Informatique', 'Groupe B', 5, 1, 3),
('Base de données','L2 Informatique', 'Groupe A', 3, 2, 4),
('POO Java',       'L2 Informatique', 'Groupe B', 3, 1, 5),
('Mathématiques',  'L2 Informatique', 'Groupe B', 4, 4, 6),
('Algorithmique',  'L2 Informatique', 'Groupe B', 3, 2, 7);

-- Inscriptions étudiants aux cours
INSERT INTO cours_etudiants (cours_id, etudiant_id) VALUES
(1, 6), (2, 6), (4, 6),
(3, 7), (5, 7), (6, 7), (7, 7);

-- ================================================================
--  VUES UTILES (pour faciliter les requêtes)
-- ================================================================

-- Vue : emploi du temps complet
CREATE VIEW vue_emploi_du_temps AS
SELECT
    c.id,
    c.matiere,
    c.classe,
    c.groupe,
    CONCAT(u.prenom, ' ', u.nom) AS enseignant,
    s.numero                      AS salle,
    s.type                        AS type_salle,
    s.capacite,
    cr.jour,
    cr.heure_debut,
    cr.duree_minutes,
    ADDTIME(cr.heure_debut,
        SEC_TO_TIME(cr.duree_minutes * 60)) AS heure_fin
FROM cours c
JOIN utilisateurs u  ON c.enseignant_id = u.id
JOIN salles s        ON c.salle_id      = s.id
JOIN creneaux cr     ON c.creneau_id    = cr.id
ORDER BY cr.jour, cr.heure_debut;

-- Vue : salles disponibles avec équipements
CREATE VIEW vue_salles_disponibles AS
SELECT
    s.id,
    s.numero,
    s.capacite,
    s.type,
    b.nom AS batiment,
    GROUP_CONCAT(e.nom SEPARATOR ', ') AS equipements
FROM salles s
JOIN batiments b      ON s.batiment_id = b.id
LEFT JOIN equipements e ON s.id = e.salle_id AND e.fonctionnel = TRUE
WHERE s.disponible = TRUE
GROUP BY s.id;

-- ================================================================
--  REQUÊTES UTILES (pour tester)
-- ================================================================

-- Voir tout l'emploi du temps
-- SELECT * FROM vue_emploi_du_temps;

-- Voir les salles disponibles
-- SELECT * FROM vue_salles_disponibles;

-- Compter les cours par enseignant
-- SELECT CONCAT(u.prenom,' ',u.nom) AS enseignant, COUNT(*) AS nb_cours
-- FROM cours c JOIN utilisateurs u ON c.enseignant_id = u.id
-- GROUP BY c.enseignant_id;

-- Taux d'occupation des salles
-- SELECT s.numero, COUNT(c.id) AS nb_cours,
--        ROUND(COUNT(c.id) / 35 * 100, 1) AS taux_occupation
-- FROM salles s LEFT JOIN cours c ON s.id = c.salle_id
-- GROUP BY s.id ORDER BY taux_occupation DESC;
