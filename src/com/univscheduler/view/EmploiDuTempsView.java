package com.univscheduler.view;

import com.univscheduler.dao.CoursDAO;
import com.univscheduler.model.*;
import com.univscheduler.model.enums.TypeSalle;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Écran de l'emploi du temps sous forme de grille calendrier.
 * Affiche les cours par jour et par créneau horaire.
 * Cet écran est intégré dans le Dashboard (contenuCentral).
 */
public class EmploiDuTempsView {

    // ── Données ──────────────────────────────────────────────────
    private List<Cours> tousLesCours = new ArrayList<>();
    private String filtreClasse = "Tous";

    // Jours et créneaux affichés
    private static final String[] JOURS = {
        "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi"
    };
    private static final int[] HEURES = { 8, 9, 10, 11, 12, 13, 14, 15, 16, 17 };

    // Couleurs des cours (une par matière)
    private static final String[] COULEURS_FOND = {
        "#e8eaf6", "#e8f5e9", "#fff3e0", "#fce4ec",
        "#e0f7fa", "#f3e5f5", "#fff8e1"
    };
    private static final String[] COULEURS_BORD = {
        "#3949ab", "#43a047", "#fb8c00", "#e91e63",
        "#00acc1", "#8e24aa", "#f9a825"
    };

    // ── Composants ───────────────────────────────────────────────
    private GridPane grille;
    private VBox conteneur;

    // ── Constructeur ─────────────────────────────────────────────
    public EmploiDuTempsView() {
        chargerDepuisBDD();
    }

    // ════════════════════════════════════════════════════════════
    //  CONSTRUCTION DE L'INTERFACE
    // ════════════════════════════════════════════════════════════

    /**
     * Retourne le panneau complet de l'emploi du temps.
     * À appeler depuis DashboardView.
     */
    public VBox getVue() {
        conteneur = new VBox(16);
        conteneur.setPadding(new Insets(10, 0, 0, 0));

        // Titre
        Label titre = new Label("📅  Emploi du Temps");
        titre.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        titre.setTextFill(Color.web("#1a237e"));

        // Barre d'outils
        HBox barreOutils = construireBarreOutils();

        // Légende des cours
        HBox legende = construireLegende();

        // La grille calendrier
        grille = construireGrille(filtreClasse);

        ScrollPane scrollGrille = new ScrollPane(grille);
        scrollGrille.setFitToWidth(true);
        scrollGrille.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        conteneur.getChildren().addAll(titre, barreOutils, legende, scrollGrille);
        return conteneur;
    }

    // ── Barre d'outils ───────────────────────────────────────────
    private HBox construireBarreOutils() {
        HBox barre = new HBox(12);
        barre.setAlignment(Pos.CENTER_LEFT);

        // Filtre par classe
        Label lblFiltre = new Label("Classe :");
        lblFiltre.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        ComboBox<String> filtreClasses = new ComboBox<>();
        filtreClasses.getItems().addAll(
            "Tous", "L1 Informatique", "L2 Informatique", "L3 Informatique");
        filtreClasses.setValue("Tous");
        filtreClasses.setPrefHeight(36);
        filtreClasses.setOnAction(e -> {
            filtreClasse = filtreClasses.getValue();
            rafraichirGrille();
        });

        // Filtre par enseignant
        Label lblEns = new Label("Enseignant :");
        lblEns.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        ComboBox<String> filtreEns = new ComboBox<>();
        filtreEns.getItems().addAll("Tous", "Diallo Moussa", "Seck Aminata", "Ba Oumar");
        filtreEns.setValue("Tous");
        filtreEns.setPrefHeight(36);

        // Espace flexible
        Region espace = new Region();
        HBox.setHgrow(espace, Priority.ALWAYS);

        // Bouton ajouter cours
        Button btnAjouter = new Button("➕  Ajouter un cours");
        btnAjouter.setPrefHeight(36);
        btnAjouter.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        btnAjouter.setStyle(
            "-fx-background-color: #1a237e; -fx-text-fill: white;"
          + "-fx-background-radius: 8; -fx-cursor: hand;");
        btnAjouter.setOnAction(e -> ouvrirFormulaireAjoutCours());
        btnAjouter.setOnMouseEntered(e -> btnAjouter.setStyle(
            "-fx-background-color: #283593; -fx-text-fill: white;"
          + "-fx-background-radius: 8; -fx-cursor: hand;"));
        btnAjouter.setOnMouseExited(e -> btnAjouter.setStyle(
            "-fx-background-color: #1a237e; -fx-text-fill: white;"
          + "-fx-background-radius: 8; -fx-cursor: hand;"));

        barre.getChildren().addAll(
            lblFiltre, filtreClasses, lblEns, filtreEns, espace, btnAjouter);
        return barre;
    }

    // ── Légende ──────────────────────────────────────────────────
    private HBox construireLegende() {
        HBox legende = new HBox(16);
        legende.setAlignment(Pos.CENTER_LEFT);
        legende.setPadding(new Insets(8, 12, 8, 12));
        legende.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 8;");

        Label lblLegende = new Label("Légende : ");
        lblLegende.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        lblLegende.setTextFill(Color.web("#757575"));

        legende.getChildren().add(lblLegende);

        // Afficher une case de légende par matière unique
        List<String> matieresDeja = new ArrayList<>();
        int i = 0;
        for (Cours c : tousLesCours) {
            if (!matieresDeja.contains(c.getMatiere())) {
                matieresDeja.add(c.getMatiere());
                Label item = new Label("  " + c.getMatiere() + "  ");
                item.setFont(Font.font("Arial", FontWeight.BOLD, 10));
                item.setTextFill(Color.web(COULEURS_BORD[i % COULEURS_BORD.length]));
                item.setStyle(
                    "-fx-background-color: " + COULEURS_FOND[i % COULEURS_FOND.length] + ";"
                  + "-fx-background-radius: 6;"
                  + "-fx-border-color: " + COULEURS_BORD[i % COULEURS_BORD.length] + ";"
                  + "-fx-border-radius: 6;"
                  + "-fx-border-width: 0 0 0 3;");
                legende.getChildren().add(item);
                i++;
            }
        }
        return legende;
    }

    // ── Grille calendrier ─────────────────────────────────────────
    private GridPane construireGrille(String classeFiltre) {
        GridPane grille = new GridPane();
        grille.setHgap(4);
        grille.setVgap(4);
        grille.setPadding(new Insets(4));

        // ── En-têtes des jours (ligne 0) ────────────────────────
        // Cellule vide en haut à gauche
        Label coinVide = new Label("");
        coinVide.setPrefSize(60, 40);
        grille.add(coinVide, 0, 0);

        for (int j = 0; j < JOURS.length; j++) {
            Label labelJour = new Label(JOURS[j]);
            labelJour.setFont(Font.font("Arial", FontWeight.BOLD, 13));
            labelJour.setTextFill(Color.WHITE);
            labelJour.setPrefWidth(160);
            labelJour.setPrefHeight(40);
            labelJour.setAlignment(Pos.CENTER);
            labelJour.setStyle(
                "-fx-background-color: #1a237e;"
              + "-fx-background-radius: 8;");
            grille.add(labelJour, j + 1, 0);
        }

        // ── Lignes horaires ──────────────────────────────────────
        for (int h = 0; h < HEURES.length; h++) {
            int heure = HEURES[h];

            // Étiquette de l'heure (colonne 0)
            Label labelHeure = new Label(heure + "h00");
            labelHeure.setFont(Font.font("Arial", FontWeight.BOLD, 11));
            labelHeure.setTextFill(Color.web("#757575"));
            labelHeure.setPrefWidth(60);
            labelHeure.setPrefHeight(60);
            labelHeure.setAlignment(Pos.TOP_RIGHT);
            labelHeure.setPadding(new Insets(4, 8, 0, 0));
            grille.add(labelHeure, 0, h + 1);

            // Cellules pour chaque jour
            for (int j = 0; j < JOURS.length; j++) {
                String jour = JOURS[j];

                // Chercher un cours sur ce créneau
                Cours coursIci = trouverCours(jour, heure, classeFiltre);

                if (coursIci != null) {
                    VBox celluleCours = creerCelluleCours(coursIci,
                            tousLesCours.indexOf(coursIci));
                    grille.add(celluleCours, j + 1, h + 1);
                } else {
                    // Cellule vide cliquable
                    VBox celluleVide = creerCelluleVide(jour, heure);
                    grille.add(celluleVide, j + 1, h + 1);
                }
            }
        }
        return grille;
    }

    /** Crée une cellule affichant un cours */
    private VBox creerCelluleCours(Cours cours, int index) {
        VBox cellule = new VBox(3);
        cellule.setPrefSize(160, 60);
        cellule.setPadding(new Insets(6, 8, 6, 8));
        cellule.setStyle(
            "-fx-background-color: " + COULEURS_FOND[index % COULEURS_FOND.length] + ";"
          + "-fx-background-radius: 8;"
          + "-fx-border-color: "      + COULEURS_BORD[index % COULEURS_BORD.length] + ";"
          + "-fx-border-radius: 8;"
          + "-fx-border-width: 0 0 0 4;"
          + "-fx-cursor: hand;");

        Label matiere = new Label(cours.getMatiere());
        matiere.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        matiere.setTextFill(Color.web(COULEURS_BORD[index % COULEURS_BORD.length]));

        Label info = new Label("📍 " + cours.getSalle().getNumero()
                + "  👤 " + cours.getEnseignant().getNom());
        info.setFont(Font.font("Arial", 9));
        info.setTextFill(Color.web("#616161"));

        Label classe = new Label("🎓 " + cours.getClasse());
        classe.setFont(Font.font("Arial", 9));
        classe.setTextFill(Color.web("#9e9e9e"));

        cellule.getChildren().addAll(matiere, info, classe);

        // Clic → afficher les détails du cours
        cellule.setOnMouseClicked(e -> afficherDetailsCours(cours));
        cellule.setOnMouseEntered(e -> cellule.setStyle(
            "-fx-background-color: " + COULEURS_BORD[index % COULEURS_BORD.length] + "22;"
          + "-fx-background-radius: 8;"
          + "-fx-border-color: "      + COULEURS_BORD[index % COULEURS_BORD.length] + ";"
          + "-fx-border-radius: 8;"
          + "-fx-border-width: 0 0 0 4;"
          + "-fx-cursor: hand;"));
        cellule.setOnMouseExited(e -> cellule.setStyle(
            "-fx-background-color: " + COULEURS_FOND[index % COULEURS_FOND.length] + ";"
          + "-fx-background-radius: 8;"
          + "-fx-border-color: "      + COULEURS_BORD[index % COULEURS_BORD.length] + ";"
          + "-fx-border-radius: 8;"
          + "-fx-border-width: 0 0 0 4;"
          + "-fx-cursor: hand;"));

        return cellule;
    }

    /** Crée une cellule vide (créneau libre) */
    private VBox creerCelluleVide(String jour, int heure) {
        VBox cellule = new VBox();
        cellule.setPrefSize(160, 60);
        cellule.setAlignment(Pos.CENTER);
        cellule.setStyle(
            "-fx-background-color: #fafafa;"
          + "-fx-background-radius: 8;"
          + "-fx-border-color: #e0e0e0;"
          + "-fx-border-radius: 8;"
          + "-fx-border-width: 1;"
          + "-fx-cursor: hand;");

        cellule.setOnMouseEntered(e -> cellule.setStyle(
            "-fx-background-color: #e8eaf6;"
          + "-fx-background-radius: 8;"
          + "-fx-border-color: #9fa8da;"
          + "-fx-border-radius: 8;"
          + "-fx-border-width: 1;"
          + "-fx-cursor: hand;"));
        cellule.setOnMouseExited(e -> cellule.setStyle(
            "-fx-background-color: #fafafa;"
          + "-fx-background-radius: 8;"
          + "-fx-border-color: #e0e0e0;"
          + "-fx-border-radius: 8;"
          + "-fx-border-width: 1;"
          + "-fx-cursor: hand;"));

        return cellule;
    }

    // ── Détails d'un cours (popup) ────────────────────────────────
    private void afficherDetailsCours(Cours cours) {
        Stage popup = new Stage();
        popup.setTitle("Détails du cours");
        popup.initModality(Modality.APPLICATION_MODAL);

        VBox contenu = new VBox(14);
        contenu.setPadding(new Insets(24));
        contenu.setStyle("-fx-background-color: white;");
        contenu.setPrefWidth(360);

        Label titre = new Label("📚  " + cours.getMatiere());
        titre.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        titre.setTextFill(Color.web("#1a237e"));

        contenu.getChildren().addAll(
            titre,
            creerLigneDetail("🎓 Classe",       cours.getClasse() + " — " + cours.getGroupe()),
            creerLigneDetail("👤 Enseignant",    cours.getEnseignant().getNomComplet()),
            creerLigneDetail("📍 Salle",         cours.getSalle().getNumero()
                    + " (" + cours.getSalle().getType() + " — "
                    + cours.getSalle().getCapacite() + " places)"),
            creerLigneDetail("🕐 Créneau",       cours.getCreneau().toString()),
            creerLigneDetail("⏱  Durée",         cours.getCreneau().getDureMinutes() + " minutes")
        );

        // Bouton fermer
        Button btnFermer = new Button("Fermer");
        btnFermer.setMaxWidth(Double.MAX_VALUE);
        btnFermer.setStyle("-fx-background-color: #1a237e; -fx-text-fill: white;"
                         + "-fx-background-radius: 8; -fx-cursor: hand;");
        btnFermer.setOnAction(e -> popup.close());
        contenu.getChildren().add(btnFermer);

        popup.setScene(new Scene(contenu));
        popup.show();
    }

    /** Formulaire d'ajout d'un nouveau cours */
    private void ouvrirFormulaireAjoutCours() {
        Stage popup = new Stage();
        popup.setTitle("Ajouter un cours");
        popup.initModality(Modality.APPLICATION_MODAL);

        VBox form = new VBox(12);
        form.setPadding(new Insets(24));
        form.setStyle("-fx-background-color: white;");
        form.setPrefWidth(400);

        Label titre = new Label("➕  Nouveau cours");
        titre.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        titre.setTextFill(Color.web("#1a237e"));

        TextField champMatiere = creerChampTexte("Matière (ex: Algorithmique)");
        TextField champClasse  = creerChampTexte("Classe (ex: L2 Informatique)");
        TextField champGroupe  = creerChampTexte("Groupe (ex: Groupe A)");

        ComboBox<String> selectJour = new ComboBox<>();
        selectJour.getItems().addAll(JOURS);
        selectJour.setPromptText("Jour");
        selectJour.setMaxWidth(Double.MAX_VALUE);

        TextField champHeure   = creerChampTexte("Heure de début (ex: 08:00)");
        TextField champDuree   = creerChampTexte("Durée en minutes (ex: 120)");

        Label msgErreur = new Label("");
        msgErreur.setTextFill(Color.web("#c62828"));
        msgErreur.setFont(Font.font("Arial", 11));

        Button btnSauver = new Button("➕  Ajouter le cours");
        btnSauver.setMaxWidth(Double.MAX_VALUE);
        btnSauver.setStyle("-fx-background-color: #1a237e; -fx-text-fill: white;"
                         + "-fx-background-radius: 8; -fx-cursor: hand;"
                         + "-fx-font-weight: bold;");
        btnSauver.setOnAction(e -> {
            if (champMatiere.getText().isEmpty() || selectJour.getValue() == null) {
                msgErreur.setText("⚠  Veuillez remplir tous les champs.");
                return;
            }
            try {
                String[] heureParts = champHeure.getText().split(":");
                LocalTime heureDebut = LocalTime.of(
                    Integer.parseInt(heureParts[0]),
                    Integer.parseInt(heureParts[1]));
                int duree = Integer.parseInt(champDuree.getText());

                // Créer le cours avec des données simplifiées
                Salle salle = new Salle("A101", 40, TypeSalle.TD, 1); salle.setId(1);
                Enseignant ens = new Enseignant("Prof", "Nouveau", "p@univ.sn",
                        "pass", "Info", "UFR"); ens.setId(99);
                Creneau creneau = new Creneau(selectJour.getValue(), heureDebut, duree);

                Cours nouveau = new Cours(
                    champMatiere.getText(),
                    champClasse.getText(),
                    champGroupe.getText(),
                    ens, salle, creneau);
                nouveau.setId(tousLesCours.size() + 1);
                CoursDAO coursDAO = new CoursDAO();
                boolean ok = coursDAO.ajouter(nouveau);
                if (ok) {
                    tousLesCours.add(nouveau);
                    rafraichirGrille();
                    popup.close();
                } else {
                    msgErreur.setText("⚠  Erreur lors de l'ajout en base de données.");
                }

            } catch (Exception ex) {
                msgErreur.setText("⚠  Format invalide. Heure: HH:MM, Durée: nombre.");
            }
        });

        form.getChildren().addAll(
            titre,
            new Label("Matière :"), champMatiere,
            new Label("Classe :"),  champClasse,
            new Label("Groupe :"),  champGroupe,
            new Label("Jour :"),    selectJour,
            new Label("Heure :"),   champHeure,
            new Label("Durée :"),   champDuree,
            msgErreur, btnSauver
        );

        popup.setScene(new Scene(form));
        popup.show();
    }

    // ════════════════════════════════════════════════════════════
    //  LOGIQUE
    // ════════════════════════════════════════════════════════════

    /**
     * Cherche un cours sur un créneau donné (jour + heure).
     */
    private Cours trouverCours(String jour, int heure, String classeFiltre) {
        for (Cours c : tousLesCours) {
            // Filtre par classe
            if (!classeFiltre.equals("Tous")
                    && !c.getClasse().equals(classeFiltre)) continue;

            // Vérifier si le cours est sur ce jour et cette heure
            boolean memeJour = c.getCreneau().getJour().equalsIgnoreCase(jour);
            LocalTime debut  = c.getCreneau().getHeureDebut();
            LocalTime fin    = c.getCreneau().getHeureFin();
            LocalTime heureLocale = LocalTime.of(heure, 0);

            boolean dansLeCreneau = !heureLocale.isBefore(debut)
                                 && heureLocale.isBefore(fin);

            // N'afficher le cours qu'à son heure de début
            boolean estDebut = debut.getHour() == heure;

            if (memeJour && estDebut) return c;
        }
        return null;
    }

    /** Rafraîchit la grille après ajout/modification */
    private void rafraichirGrille() {
        grille = construireGrille(filtreClasse);
        // Remplacer l'ancien ScrollPane dans le conteneur
        if (conteneur.getChildren().size() >= 4) {
            ScrollPane nouveauScroll = new ScrollPane(grille);
            nouveauScroll.setFitToWidth(true);
            nouveauScroll.setStyle(
                "-fx-background-color: transparent; -fx-background: transparent;");
            conteneur.getChildren().set(3, nouveauScroll);
        }
    }

    // ════════════════════════════════════════════════════════════
    //  UTILITAIRES
    // ════════════════════════════════════════════════════════════

    private HBox creerLigneDetail(String label, String valeur) {
        HBox ligne = new HBox(12);
        ligne.setAlignment(Pos.CENTER_LEFT);
        ligne.setPadding(new Insets(8, 12, 8, 12));
        ligne.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 6;");

        Label lbl = new Label(label);
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        lbl.setTextFill(Color.web("#424242"));
        lbl.setMinWidth(120);

        Label val = new Label(valeur);
        val.setFont(Font.font("Arial", 12));
        val.setTextFill(Color.web("#616161"));

        ligne.getChildren().addAll(lbl, val);
        return ligne;
    }

    private TextField creerChampTexte(String placeholder) {
        TextField champ = new TextField();
        champ.setPromptText(placeholder);
        champ.setStyle(
            "-fx-background-color: white; -fx-border-color: #bdbdbd;"
          + "-fx-border-radius: 8; -fx-background-radius: 8;"
          + "-fx-padding: 8 12 8 12;");
        return champ;
    }

    private void chargerDepuisBDD() {
    	CoursDAO coursDAO = new CoursDAO();
    	List<Cours> depuisBDD = coursDAO.getTous();
        if (depuisBDD != null && !depuisBDD.isEmpty()) {
            tousLesCours.addAll(depuisBDD);
        } else {
            chargerDonneesTest();
        }
    }

    private void chargerDonneesTest() {
        Salle s1 = new Salle("A101", 40,  TypeSalle.TD,    1); s1.setId(1);
        Salle s2 = new Salle("A102", 25,  TypeSalle.TP,    1); s2.setId(2);
        Salle s3 = new Salle("B201", 150, TypeSalle.AMPHI, 2); s3.setId(3);

        Enseignant prof1 = new Enseignant("Diallo", "Moussa", "m.diallo@univ.sn",
                "p", "Info", "UFR"); prof1.setId(1);
        Enseignant prof2 = new Enseignant("Seck", "Aminata", "a.seck@univ.sn",
                "p", "Maths", "UFR"); prof2.setId(2);
        Enseignant prof3 = new Enseignant("Ba", "Oumar", "o.ba@univ.sn",
                "p", "Réseau", "UFR"); prof3.setId(3);

        // Créneaux
        Creneau c1 = new Creneau("Lundi",    LocalTime.of(8,  0), 120); c1.setId(1);
        Creneau c2 = new Creneau("Lundi",    LocalTime.of(10, 0), 90);  c2.setId(2);
        Creneau c3 = new Creneau("Mardi",    LocalTime.of(8,  0), 120); c3.setId(3);
        Creneau c4 = new Creneau("Mardi",    LocalTime.of(14, 0), 90);  c4.setId(4);
        Creneau c5 = new Creneau("Mercredi", LocalTime.of(10, 0), 120); c5.setId(5);
        Creneau c6 = new Creneau("Jeudi",    LocalTime.of(8,  0), 90);  c6.setId(6);
        Creneau c7 = new Creneau("Vendredi", LocalTime.of(14, 0), 120); c7.setId(7);

        // Cours
        Cours co1 = new Cours("Algorithmique", "L2 Informatique", "Groupe A", prof1, s1, c1); co1.setId(1);
        Cours co2 = new Cours("Mathématiques", "L2 Informatique", "Groupe A", prof2, s3, c2); co2.setId(2);
        Cours co3 = new Cours("Réseaux",        "L2 Informatique", "Groupe B", prof3, s1, c3); co3.setId(3);
        Cours co4 = new Cours("Base de données","L2 Informatique", "Groupe A", prof1, s2, c4); co4.setId(4);
        Cours co5 = new Cours("POO Java",        "L2 Informatique", "Groupe B", prof1, s1, c5); co5.setId(5);
        Cours co6 = new Cours("Mathématiques",   "L2 Informatique", "Groupe B", prof2, s3, c6); co6.setId(6);
        Cours co7 = new Cours("Algorithmique",   "L2 Informatique", "Groupe B", prof1, s2, c7); co7.setId(7);

        tousLesCours.addAll(List.of(co1, co2, co3, co4, co5, co6, co7));
    }
}
