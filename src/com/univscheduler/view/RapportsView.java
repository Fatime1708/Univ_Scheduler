package com.univscheduler.view;

import com.univscheduler.model.*;
import com.univscheduler.model.enums.TypeRapport;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Écran de gestion des rapports.
 * Permet de générer, consulter et exporter des rapports d'utilisation.
 */
public class RapportsView {

    // ── Données ──────────────────────────────────────────────────
    private ObservableList<Rapport> listeRapports;
    private List<Rapport> tousLesRapports = new ArrayList<>();
    private Administrateur admin;

    // ── Composants ───────────────────────────────────────────────
    private VBox zoneApercu;

    // ── Constructeur ─────────────────────────────────────────────
    public RapportsView(Utilisateur utilisateur) {
        // Créer un admin par défaut si l'utilisateur n'est pas admin
        if (utilisateur instanceof Administrateur) {
            this.admin = (Administrateur) utilisateur;
        } else {
            this.admin = new Administrateur(
                utilisateur.getNom(), utilisateur.getPrenom(),
                utilisateur.getEmail(), "");
            this.admin.setId(utilisateur.getId());
        }
        chargerDonneesTest();
        listeRapports = FXCollections.observableArrayList(tousLesRapports);
    }

    // ════════════════════════════════════════════════════════════
    //  CONSTRUCTION DE L'INTERFACE
    // ════════════════════════════════════════════════════════════

    public VBox getVue() {
        VBox vue = new VBox(20);
        vue.setPadding(new Insets(10, 0, 0, 0));

        // Titre
        Label titre = new Label("📊  Rapports d'utilisation");
        titre.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        titre.setTextFill(Color.web("#1a237e"));

        Label sousTitre = new Label(
            "Générez et exportez des rapports sur l'utilisation des salles et cours");
        sousTitre.setFont(Font.font("Arial", 13));
        sousTitre.setTextFill(Color.web("#757575"));

        // Zone principale : cartes de génération + liste des rapports
        HBox zoneprincipale = new HBox(20);

        // Gauche : cartes pour générer un rapport
        VBox gauche = construirePanneauGeneration();
        gauche.setPrefWidth(320);

        // Droite : liste des rapports générés + aperçu
        VBox droite = construirePanneauRapports();
        HBox.setHgrow(droite, Priority.ALWAYS);

        zoneprincipale.getChildren().addAll(gauche, droite);

        vue.getChildren().addAll(titre, sousTitre, zoneprincipale);
        return vue;
    }

    // ── Panneau gauche : génération ──────────────────────────────
    private VBox construirePanneauGeneration() {
        VBox panneau = new VBox(14);
        panneau.setPadding(new Insets(20));
        panneau.setStyle(
            "-fx-background-color: white;"
          + "-fx-background-radius: 12;"
          + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 6, 0, 0, 2);");

        Label titre = new Label("Générer un rapport");
        titre.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        titre.setTextFill(Color.web("#1a237e"));

        Separator sep = new Separator();

        // Cartes de types de rapports
        panneau.getChildren().addAll(titre, sep);
        panneau.getChildren().addAll(
            creerCarteRapport(
                "📅", "Rapport Hebdomadaire",
                "Résumé des cours et occupation\nde la semaine courante",
                "#e8eaf6", "#3949ab", TypeRapport.HEBDOMADAIRE),
            creerCarteRapport(
                "📆", "Rapport Mensuel",
                "Bilan complet du mois :\ncours, salles, statistiques",
                "#e8f5e9", "#2e7d32", TypeRapport.MENSUEL),
            creerCarteRapport(
                "🏫", "Rapport d'Occupation",
                "Taux d'utilisation de chaque\nsalle par créneau",
                "#fff3e0", "#e65100", TypeRapport.OCCUPATION)
        );

        return panneau;
    }

    /** Crée une carte cliquable pour générer un type de rapport */
    private VBox creerCarteRapport(String icone, String titre, String description,
                                    String couleurFond, String couleurBord,
                                    TypeRapport type) {
        VBox carte = new VBox(8);
        carte.setPadding(new Insets(14));
        carte.setStyle(
            "-fx-background-color: " + couleurFond + ";"
          + "-fx-background-radius: 10;"
          + "-fx-border-color: " + couleurBord + ";"
          + "-fx-border-radius: 10;"
          + "-fx-border-width: 0 0 0 4;"
          + "-fx-cursor: hand;");

        HBox entete = new HBox(10);
        entete.setAlignment(Pos.CENTER_LEFT);

        Label ico = new Label(icone);
        ico.setFont(Font.font(20));

        Label lblTitre = new Label(titre);
        lblTitre.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        lblTitre.setTextFill(Color.web(couleurBord));

        entete.getChildren().addAll(ico, lblTitre);

        Label desc = new Label(description);
        desc.setFont(Font.font("Arial", 11));
        desc.setTextFill(Color.web("#616161"));

        Button btnGenerer = new Button("▶  Générer maintenant");
        btnGenerer.setMaxWidth(Double.MAX_VALUE);
        btnGenerer.setStyle(
            "-fx-background-color: " + couleurBord + ";"
          + "-fx-text-fill: white;"
          + "-fx-background-radius: 6;"
          + "-fx-cursor: hand;"
          + "-fx-font-size: 11;"
          + "-fx-font-weight: bold;");
        btnGenerer.setOnAction(e -> genererRapport(type));

        carte.getChildren().addAll(entete, desc, btnGenerer);

        // Hover
        carte.setOnMouseEntered(e -> carte.setStyle(
            "-fx-background-color: " + couleurBord + "22;"
          + "-fx-background-radius: 10;"
          + "-fx-border-color: " + couleurBord + ";"
          + "-fx-border-radius: 10;"
          + "-fx-border-width: 0 0 0 4;"
          + "-fx-cursor: hand;"));
        carte.setOnMouseExited(e -> carte.setStyle(
            "-fx-background-color: " + couleurFond + ";"
          + "-fx-background-radius: 10;"
          + "-fx-border-color: " + couleurBord + ";"
          + "-fx-border-radius: 10;"
          + "-fx-border-width: 0 0 0 4;"
          + "-fx-cursor: hand;"));

        return carte;
    }

    // ── Panneau droite : liste des rapports ──────────────────────
    private VBox construirePanneauRapports() {
        VBox panneau = new VBox(14);

        // Liste des rapports générés
        VBox listeBox = new VBox(10);
        listeBox.setPadding(new Insets(20));
        listeBox.setStyle(
            "-fx-background-color: white;"
          + "-fx-background-radius: 12;"
          + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 6, 0, 0, 2);");

        Label titreListe = new Label("📋  Rapports générés");
        titreListe.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        titreListe.setTextFill(Color.web("#1a237e"));

        // Liste scrollable
        ListView<Rapport> listView = new ListView<>(listeRapports);
        listView.setPrefHeight(200);
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Rapport rapport, boolean empty) {
                super.updateItem(rapport, empty);
                if (empty || rapport == null) {
                    setGraphic(null);
                    return;
                }
                HBox ligne = new HBox(12);
                ligne.setAlignment(Pos.CENTER_LEFT);
                ligne.setPadding(new Insets(6));

                // Icône selon le type
                String icone = switch (rapport.getType()) {
                    case HEBDOMADAIRE -> "📅";
                    case MENSUEL      -> "📆";
                    case OCCUPATION   -> "🏫";
                };

                Label ico = new Label(icone);
                ico.setFont(Font.font(16));

                VBox infos = new VBox(2);
                Label nom = new Label(rapport.getTitre());
                nom.setFont(Font.font("Arial", FontWeight.BOLD, 12));

                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                Label date = new Label("Généré le " +
                        rapport.getDateGeneration().format(fmt));
                date.setFont(Font.font("Arial", 10));
                date.setTextFill(Color.web("#9e9e9e"));

                infos.getChildren().addAll(nom, date);

                // Boutons
                Button btnVoir     = new Button("👁 Voir");
                Button btnExporter = new Button("⬇ Exporter");

                btnVoir.setStyle(
                    "-fx-background-color: #e3f2fd; -fx-text-fill: #1565c0;"
                  + "-fx-background-radius: 5; -fx-cursor: hand; -fx-font-size: 10;");
                btnExporter.setStyle(
                    "-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32;"
                  + "-fx-background-radius: 5; -fx-cursor: hand; -fx-font-size: 10;");

                btnVoir.setOnAction(e -> afficherApercu(rapport));
                btnExporter.setOnAction(e -> exporterRapport(rapport));

                Region espace = new Region();
                HBox.setHgrow(espace, Priority.ALWAYS);

                ligne.getChildren().addAll(ico, infos, espace, btnVoir, btnExporter);
                setGraphic(ligne);
            }
        });

        listeBox.getChildren().addAll(titreListe, new Separator(), listView);

        // Zone d'aperçu du rapport
        zoneApercu = new VBox(10);
        zoneApercu.setPadding(new Insets(20));
        zoneApercu.setStyle(
            "-fx-background-color: white;"
          + "-fx-background-radius: 12;"
          + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 6, 0, 0, 2);");

        Label titreApercu = new Label("🔍  Aperçu du rapport");
        titreApercu.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        titreApercu.setTextFill(Color.web("#1a237e"));

        Label msgVide = new Label("Cliquez sur un rapport pour voir son contenu.");
        msgVide.setFont(Font.font("Arial", 12));
        msgVide.setTextFill(Color.web("#9e9e9e"));

        zoneApercu.getChildren().addAll(titreApercu, new Separator(), msgVide);

        panneau.getChildren().addAll(listeBox, zoneApercu);
        return panneau;
    }

    // ════════════════════════════════════════════════════════════
    //  LOGIQUE
    // ════════════════════════════════════════════════════════════

    /** Génère un nouveau rapport et l'ajoute à la liste */
    private void genererRapport(TypeRapport type) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String titre = type.getLibelle() + " — " + LocalDate.now().format(fmt);

        Rapport rapport = new Rapport(titre, type, admin);
        rapport.generer();
        rapport.setId(tousLesRapports.size() + 1);

        tousLesRapports.add(0, rapport); // Ajouter en tête de liste
        listeRapports.add(0, rapport);

        // Afficher l'aperçu automatiquement
        afficherApercu(rapport);

        // Message de confirmation
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Rapport généré");
        alert.setHeaderText("✅  " + rapport.getTitre());
        alert.setContentText("Le rapport a été généré avec succès !\n"
                + "Vous pouvez maintenant le consulter ou l'exporter.");
        alert.showAndWait();
    }

    /** Affiche le contenu du rapport dans la zone d'aperçu */
    private void afficherApercu(Rapport rapport) {
        zoneApercu.getChildren().clear();

        Label titreApercu = new Label("🔍  Aperçu du rapport");
        titreApercu.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        titreApercu.setTextFill(Color.web("#1a237e"));

        // En-tête du rapport
        VBox entete = new VBox(6);
        entete.setPadding(new Insets(12));
        entete.setStyle(
            "-fx-background-color: #e8eaf6;"
          + "-fx-background-radius: 8;");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        Label nomRapport = new Label(rapport.getTitre());
        nomRapport.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        nomRapport.setTextFill(Color.web("#1a237e"));

        Label dateRapport = new Label(
            "Généré le : " + rapport.getDateGeneration().format(fmt)
            + "   |   Par : " + rapport.getGenerePar().getNomComplet());
        dateRapport.setFont(Font.font("Arial", 11));
        dateRapport.setTextFill(Color.web("#616161"));

        entete.getChildren().addAll(nomRapport, dateRapport);

        // Contenu simulé selon le type
        VBox contenu = genererContenuVisuel(rapport);

        // Bouton exporter
        HBox boutons = new HBox(10);
        boutons.setAlignment(Pos.CENTER_RIGHT);

        Button btnPDF   = new Button("📄  Exporter PDF");
        Button btnExcel = new Button("📊  Exporter Excel");

        btnPDF.setStyle(
            "-fx-background-color: #c62828; -fx-text-fill: white;"
          + "-fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: bold;");
        btnExcel.setStyle(
            "-fx-background-color: #2e7d32; -fx-text-fill: white;"
          + "-fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: bold;");

        btnPDF.setOnAction(e   -> exporterRapportFormat(rapport, "PDF"));
        btnExcel.setOnAction(e -> exporterRapportFormat(rapport, "Excel"));

        boutons.getChildren().addAll(btnPDF, btnExcel);

        zoneApercu.getChildren().addAll(
            titreApercu, new Separator(), entete, contenu, boutons);
    }

    /** Génère un contenu visuel selon le type de rapport */
    private VBox genererContenuVisuel(Rapport rapport) {
        VBox contenu = new VBox(10);

        switch (rapport.getType()) {

            case HEBDOMADAIRE -> {
                contenu.getChildren().add(
                    creerLigneRapport("Lundi",    "5 cours", "18 étudiants", "#e8eaf6"));
                contenu.getChildren().add(
                    creerLigneRapport("Mardi",    "4 cours", "22 étudiants", "#f3e5f5"));
                contenu.getChildren().add(
                    creerLigneRapport("Mercredi", "3 cours", "15 étudiants", "#e8f5e9"));
                contenu.getChildren().add(
                    creerLigneRapport("Jeudi",    "6 cours", "28 étudiants", "#fff3e0"));
                contenu.getChildren().add(
                    creerLigneRapport("Vendredi", "2 cours", "10 étudiants", "#fce4ec"));
            }

            case MENSUEL -> {
                contenu.getChildren().add(
                    creerLigneRapport("Total cours planifiés",  "42",  "ce mois", "#e8eaf6"));
                contenu.getChildren().add(
                    creerLigneRapport("Salles utilisées",       "7",   "sur 7",   "#e8f5e9"));
                contenu.getChildren().add(
                    creerLigneRapport("Conflits détectés",      "2",   "résolus", "#fff3e0"));
                contenu.getChildren().add(
                    creerLigneRapport("Enseignants actifs",     "5",   "profs",   "#f3e5f5"));
                contenu.getChildren().add(
                    creerLigneRapport("Étudiants concernés",    "87",  "élèves",  "#fce4ec"));
            }

            case OCCUPATION -> {
                contenu.getChildren().add(
                    creerLigneTaux("Salle A101 (TD)",    85, "#3949ab"));
                contenu.getChildren().add(
                    creerLigneTaux("Salle B201 (Amphi)", 60, "#2e7d32"));
                contenu.getChildren().add(
                    creerLigneTaux("Salle A102 (TP)",    45, "#e65100"));
                contenu.getChildren().add(
                    creerLigneTaux("Salle B202 (TD)",    30, "#8e24aa"));
                contenu.getChildren().add(
                    creerLigneTaux("Salle C101 (Réun.)", 15, "#00838f"));
            }
        }
        return contenu;
    }

    /** Ligne de données pour rapports hebdo/mensuel */
    private HBox creerLigneRapport(String label, String valeur,
                                    String detail, String couleur) {
        HBox ligne = new HBox(12);
        ligne.setPadding(new Insets(10, 14, 10, 14));
        ligne.setAlignment(Pos.CENTER_LEFT);
        ligne.setStyle(
            "-fx-background-color: " + couleur + ";"
          + "-fx-background-radius: 8;");

        Label lbl = new Label(label);
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        lbl.setMinWidth(160);

        Region espace = new Region();
        HBox.setHgrow(espace, Priority.ALWAYS);

        Label val = new Label(valeur);
        val.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 18));
        val.setTextFill(Color.web("#1a237e"));

        Label det = new Label(detail);
        det.setFont(Font.font("Arial", 11));
        det.setTextFill(Color.web("#9e9e9e"));
        det.setMinWidth(80);

        ligne.getChildren().addAll(lbl, espace, val, det);
        return ligne;
    }

    /** Ligne avec barre de progression pour taux d'occupation */
    private VBox creerLigneTaux(String salle, int taux, String couleur) {
        VBox ligne = new VBox(4);
        ligne.setPadding(new Insets(8, 0, 4, 0));

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label lblSalle = new Label(salle);
        lblSalle.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        Region espace = new Region();
        HBox.setHgrow(espace, Priority.ALWAYS);

        Label lblTaux = new Label(taux + "%");
        lblTaux.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        lblTaux.setTextFill(Color.web(couleur));

        header.getChildren().addAll(lblSalle, espace, lblTaux);

        // Barre de progression
        ProgressBar barre = new ProgressBar(taux / 100.0);
        barre.setMaxWidth(Double.MAX_VALUE);
        barre.setPrefHeight(12);
        barre.setStyle(
            "-fx-accent: " + couleur + ";"
          + "-fx-background-radius: 6;"
          + "-fx-control-inner-background: #e0e0e0;");

        ligne.getChildren().addAll(header, barre);
        return ligne;
    }

    /** Simule l'export du rapport */
    private void exporterRapport(Rapport rapport) {
        exporterRapportFormat(rapport, "PDF");
    }

    private void exporterRapportFormat(Rapport rapport, String format) {
        rapport.exporter(format);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Export réussi");
        alert.setHeaderText("📄  Export " + format + " réussi !");
        alert.setContentText("Le rapport \"" + rapport.getTitre() + "\"\n"
                + "a été exporté en " + format + " avec succès.\n\n"
                + "(Dans le projet final, le fichier sera sauvegardé\n"
                + "sur le disque via une librairie PDF/Excel.)");
        alert.showAndWait();
    }

    // ════════════════════════════════════════════════════════════
    //  DONNÉES DE TEST
    // ════════════════════════════════════════════════════════════

    private void chargerDonneesTest() {
        // Quelques rapports déjà générés
        Rapport r1 = new Rapport(
            "Rapport Hebdomadaire — 17/02/2026", TypeRapport.HEBDOMADAIRE, admin);
        r1.setId(1);
        r1.generer();

        Rapport r2 = new Rapport(
            "Rapport Mensuel — Janvier 2026", TypeRapport.MENSUEL, admin);
        r2.setId(2);
        r2.generer();

        Rapport r3 = new Rapport(
            "Rapport d'Occupation — Semaine 7", TypeRapport.OCCUPATION, admin);
        r3.setId(3);
        r3.generer();

        tousLesRapports.addAll(List.of(r1, r2, r3));
    }
}
