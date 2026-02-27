# 🎓 UNIV-SCHEDULER

> Système de gestion des salles et emplois du temps universitaires

![Java](https://img.shields.io/badge/Java-17%2B-orange?style=flat-square&logo=java)
![JavaFX](https://img.shields.io/badge/JavaFX-21-blue?style=flat-square)
![MySQL](https://img.shields.io/badge/MySQL-MariaDB-4479A1?style=flat-square&logo=mysql)
![License](https://img.shields.io/badge/License-Académique-green?style=flat-square)

---

## 📋 Description

**UNIV-SCHEDULER** est une application de bureau développée en Java/JavaFX permettant la gestion intelligente des salles et des emplois du temps au sein d'un établissement universitaire.

L'application permet à différents types d'utilisateurs (Administrateurs, Gestionnaires, Enseignants, Étudiants) d'interagir avec le système selon leurs permissions respectives.

---

## 👥 Auteurs

| Nom | Rôle |
|-----|------|
| **Fatime Tambedou** | Développeuse |
| **Ndeye Nogaye Faye** | Développeuse |

- 🏫 **Université** : Iba Der Thiam de Thiès
- 👨‍🏫 **Professeur** : Mr Diallo
- 📚 **Niveau** : L2 Informatique
- 📅 **Année académique** : 2025-2026

---

## ✨ Fonctionnalités

### 🔐 Authentification & Sécurité
- Connexion sécurisée avec email et mot de passe
- Système de rôles (Admin, Gestionnaire, Enseignant, Étudiant)
- Inscription avec **code d'activation** fourni par l'Administrateur
- Accès aux pages restreint selon le rôle

### 🏫 Gestion des Salles
- Affichage de toutes les salles avec statut en temps réel
- Ajout, modification et suppression de salles
- Filtres par type (TD, TP, Amphi, Réunion) et disponibilité
- Statistiques rapides (total, libres, occupées, amphithéâtres)

### 📅 Emploi du Temps
- Grille calendrier interactive (Lundi → Vendredi, 8h → 17h)
- Affichage coloré des cours par matière
- Filtres par classe et enseignant
- Ajout de nouveaux cours directement dans la grille
- Popup de détails au clic sur un cours

### 👥 Gestion des Utilisateurs *(Admin uniquement)*
- Liste complète des utilisateurs avec badges de rôle
- Ajout, modification et suppression d'utilisateurs
- Recherche et filtres par rôle
- Formulaire dynamique (champs adaptés selon le rôle)

### 🔑 Codes d'Activation *(Admin uniquement)*
- Génération de codes pour Étudiants, Enseignants, Gestionnaires
- Génération en lot (1, 5, 10, 20 ou 50 codes)
- Suivi des codes utilisés / disponibles
- Suppression des codes inutilisés

### 📊 Rapports
- Rapport hebdomadaire (cours par jour)
- Rapport mensuel (statistiques globales)
- Rapport d'occupation des salles (barres de progression)
- Export PDF et Excel

---

## 🛠️ Technologies utilisées

| Technologie | Version | Utilisation |
|-------------|---------|-------------|
| **Java** | 17+ | Langage principal |
| **JavaFX** | 21 | Interface graphique |
| **MySQL / MariaDB** | 10.4+ | Base de données |
| **XAMPP** | - | Serveur local |
| **JDBC** | - | Connexion Java ↔ MySQL |
| **Eclipse IDE** | - | Environnement de développement |

---

## 🏗️ Architecture

Le projet suit le pattern **MVC (Modèle - Vue - Contrôleur)** :

```
univ-scheduler/
│
├── src/
│   └── com/univscheduler/
│       ├── model/              # Classes métier
│       │   ├── Utilisateur.java
│       │   ├── Administrateur.java
│       │   ├── Enseignant.java
│       │   ├── Etudiant.java
│       │   ├── GestionnaireEmploiDuTemps.java
│       │   ├── Salle.java
│       │   ├── Cours.java
│       │   ├── Creneau.java
│       │   ├── Rapport.java
│       │   └── enums/
│       │       ├── TypeSalle.java
│       │       └── TypeRapport.java
│       │
│       ├── dao/                # Accès base de données
│       │   ├── SalleDAO.java
│       │   ├── CoursDAO.java
│       │   └── UtilisateurDAO.java
│       │
│       ├── service/            # Logique métier
│       │   └── AuthService.java
│       │
│       ├── util/               # Utilitaires
│       │   └── DatabaseConnection.java
│       │
│       └── view/               # Interface JavaFX
│           ├── LoginView.java
│           ├── InscriptionView.java
│           ├── DashboardView.java
│           ├── SallesView.java
│           ├── EmploiDuTempsView.java
│           ├── UtilisateursView.java
│           ├── CodesActivationView.java
│           └── RapportsView.java
│
└── univ_scheduler.sql          # Script de création BDD
```

---

## ⚙️ Installation et Configuration

### Prérequis

- ☕ **Java JDK 17** ou supérieur
- 🎨 **JavaFX SDK 21**
- 🗄️ **XAMPP** (avec MySQL/MariaDB)
- 🔧 **Eclipse IDE**
- 📦 **mysql-connector-j-8.x.x.jar**

---

### Étape 1 — Cloner le projet

```bash
git clone https://github.com/votre-username/univ-scheduler.git
```

Ou télécharger le ZIP depuis GitHub → **Code** → **Download ZIP**

---

### Étape 2 — Créer la base de données

**1.** Démarrez XAMPP → Start **Apache** et **MySQL**

**2.** Ouvrez `http://localhost/phpmyadmin`

**3.** Cliquez sur **"SQL"** → copiez-collez le contenu de `univ_scheduler.sql` → **Exécuter**

**4.** Vérifiez que la base `univ_scheduler` apparaît avec toutes ses tables

---

### Étape 3 — Configurer Eclipse

**1.** Importez le projet : `File` → `Import` → `Existing Projects into Workspace`

**2.** Ajoutez JavaFX :
   - `Window` → `Preferences` → `Java` → `Build Path` → `User Libraries`
   - Créez une librairie **JavaFX21** → ajoutez tous les `.jar` de `C:\javafx\javafx-sdk-21.0.10\lib`

**3.** Ajoutez le connecteur MySQL :
   - Clic droit projet → `Build Path` → `Configure Build Path`
   - `Classpath` → `Add External JARs` → sélectionnez `mysql-connector-j-8.x.x.jar`

---

### Étape 4 — Configurer la connexion MySQL

Ouvrez `src/com/univscheduler/util/DatabaseConnection.java` et vérifiez :

```java
private static final String URL  = "jdbc:mysql://localhost:3306/univ_scheduler"
                                  + "?useSSL=false&serverTimezone=UTC"
                                  + "&allowPublicKeyRetrieval=true";
private static final String USER     = "root";
private static final String PASSWORD = "";  // Vide par défaut avec XAMPP
```

---

### Étape 5 — Lancer l'application

**1.** Clic droit sur `LoginView.java` → `Run As` → `Run Configurations`

**2.** Onglet **"Arguments"** → **"VM arguments"** → ajoutez :

```
--module-path "C:\javafx\javafx-sdk-21.0.10\lib" --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base
```

**3.** Cliquez **Apply** → **Run** 🚀

---

## 🔑 Comptes de test

| Email | Mot de passe | Rôle |
|-------|-------------|------|
| admin@univ.sn | admin123 | Administrateur |
| gest@univ.sn | gest123 | Gestionnaire |
| m.diallo@univ.sn | pass123 | Enseignant |
| a.seck@univ.sn | pass456 | Enseignant |
| o.ba@univ.sn | pass789 | Enseignant |
| f.ndiaye@etu.sn | pass456 | Étudiant |
| c.fall@etu.sn | pass123 | Étudiant |

---

## 🔐 Permissions par rôle

| Fonctionnalité | Admin | Gestionnaire | Enseignant | Étudiant |
|----------------|-------|-------------|------------|---------|
| Accueil | ✅ | ✅ | ✅ | ✅ |
| Salles | ✅ | ✅ | ✅ | ✅ |
| Emploi du temps | ✅ | ✅ | ✅ | ✅ |
| Utilisateurs | ✅ | ❌ | ❌ | ❌ |
| Rapports | ✅ | ✅ | ✅ | ❌ |
| Codes activation | ✅ | ❌ | ❌ | ❌ |

---

## 🗄️ Base de données

### Tables principales

| Table | Description |
|-------|-------------|
| `utilisateurs` | Tous les comptes utilisateurs |
| `enseignants` | Données spécifiques aux enseignants |
| `etudiants` | Données spécifiques aux étudiants |
| `salles` | Salles de cours |
| `cours` | Cours planifiés |
| `creneaux` | Créneaux horaires |
| `reservations` | Réservations de salles |
| `conflits` | Conflits détectés |
| `codes_activation` | Codes d'inscription |
| `rapports` | Rapports générés |

---

## 📸 Aperçu de l'application

```
Page de Connexion  →  Dashboard  →  Salles
                              ↓
                       Emploi du Temps
                              ↓
                        Utilisateurs (Admin)
                              ↓
                    Codes Activation (Admin)
                              ↓
                           Rapports
```

---

## 📝 Diagramme UML

Le projet implémente les relations suivantes :
- `Utilisateur` ← (héritage) — `Administrateur`, `Enseignant`, `Etudiant`, `GestionnaireEmploiDuTemps`
- `Cours` → `Salle`, `Enseignant`, `Creneau`
- `Rapport` → `Utilisateur`

---

*Développé avec ❤️ par **Fatime Tambedou** et **Ndeye Nogaye Faye***
