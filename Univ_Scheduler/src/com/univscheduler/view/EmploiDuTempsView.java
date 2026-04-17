package com.univscheduler.view;
import com.univscheduler.dao.SalleDAO;
import com.univscheduler.dao.UfrDAO;
import com.univscheduler.dao.DepartementDAO;
import com.univscheduler.dao.NotificationDAO;
import com.univscheduler.dao.UtilisateurDAO;
import com.univscheduler.model.Etudiant;
import java.util.List;
import com.univscheduler.dao.CoursDAO;
import com.univscheduler.service.ExportPDFService;
//import javafx.stage.FileChooser;
//import java.io.File;
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

import javafx.stage.FileChooser;
import java.io.File;

import java.time.LocalTime;

import java.util.ArrayList;
import java.util.List;
import com.univscheduler.service.EmailService;



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
    	    "#c5cae9", "#c8e6c9", "#ffe0b2", "#f8bbd0",
    	    "#b2ebf2", "#e1bee7", "#fff9c4"
    	};
    	private static final String[] COULEURS_BORD = {
    	    "#1a237e", "#1b5e20", "#e65100", "#880e4f",
    	    "#006064", "#4a148c", "#f57f17"
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

        

        // ── Bouton Export PDF ──
        Button btnExportPDF = new Button("📄 Exporter PDF");
        btnExportPDF.setPrefHeight(36);
        btnExportPDF.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        btnExportPDF.setStyle(
            "-fx-background-color: #e74c3c; -fx-text-fill: white;"
          + "-fx-background-radius: 8; -fx-cursor: hand;");
        btnExportPDF.setOnAction(e -> exporterPDF());

        barre.getChildren().addAll(
            lblFiltre, filtreClasses, lblEns, filtreEns, espace,  btnExportPDF);

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
        cellule.setPrefSize(160, 70);
        cellule.setPadding(new Insets(6, 8, 6, 8));
        cellule.setStyle(
            "-fx-background-color: " + COULEURS_FOND[index % COULEURS_FOND.length] + ";"
          + "-fx-background-radius: 8;"
          + "-fx-border-color: "      + COULEURS_BORD[index % COULEURS_BORD.length] + ";"
          + "-fx-border-radius: 8;"
          + "-fx-border-width: 0 0 0 4;"
          + "-fx-opacity: 1.0;"
          + "-fx-cursor: hand;");

       

        Label matiere = new Label(cours.getMatiere());
        matiere.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        matiere.setTextFill(Color.web("#000000")); // ← noir pur

        Label classe = new Label("🎓 " + cours.getClasse());
        classe.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        classe.setTextFill(Color.web("#000000")); // ← noir pur

        Label salle = new Label("📍 " + cours.getSalle().getNumero());
        salle.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        salle.setTextFill(Color.web("#000000")); // ← noir pur

        Label enseignant = new Label("👤 " + cours.getEnseignant().getNom());
        enseignant.setFont(Font.font("Arial", FontWeight.BOLD, 9));
        enseignant.setTextFill(Color.web("#000000")); // ← noir pur
        cellule.getChildren().addAll(matiere, classe, salle, enseignant);

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
            creerLigneDetail("🎓 Classe",    cours.getClasse() + " — " + cours.getGroupe()),
            creerLigneDetail("👤 Enseignant", cours.getEnseignant().getNomComplet()),
            creerLigneDetail("📍 Salle",      cours.getSalle().getNumero()
                    + " (" + cours.getSalle().getType() + " — "
                    + cours.getSalle().getCapacite() + " places)"),
            creerLigneDetail("🕐 Créneau",    cours.getCreneau().toString()),
            creerLigneDetail("⏱  Durée",      cours.getCreneau().getDureMinutes() + " minutes")
        );

        boolean estEnseignant = DashboardView.utilisateurConnecte instanceof Enseignant;

        if (estEnseignant) {
            Button btnAnnuler = new Button("❌  Annuler ce cours");
            btnAnnuler.setMaxWidth(Double.MAX_VALUE);
            btnAnnuler.setStyle(
                "-fx-background-color: #c62828; -fx-text-fill: white;"
              + "-fx-background-radius: 8; -fx-cursor: hand;"
              + "-fx-font-weight: bold;");

            btnAnnuler.setOnAction(ev -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Annuler le cours");
                confirm.setHeaderText("Annuler " + cours.getMatiere() + " ?");
                confirm.setContentText("Tous les étudiants seront notifiés.");
                confirm.showAndWait().ifPresent(r -> {
                    if (r == ButtonType.OK) {
                        CoursDAO coursDAO = new CoursDAO();
                        boolean ok = coursDAO.supprimer(cours.getId());
                        if (ok) {
                            UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
                            List<Etudiant> etudiants = utilisateurDAO.getTousEtudiants();
                            NotificationDAO notifDAO = new NotificationDAO();

                            for (Etudiant etu : etudiants) {
                                notifDAO.ajouter(
                                    "❌ Cours annulé",
                                    "Le cours " + cours.getMatiere()
                                        + " (" + cours.getClasse() + " — " + cours.getGroupe() + ")"
                                        + " prévu le " + cours.getCreneau().getJour()
                                        + " à " + cours.getCreneau().getHeureDebut()
                                        + " a été annulé.",
                                    "COURS", "ETUDIANT", etu.getId()
                                );
                                EmailService.envoyerEmail(
                                    etu.getEmail(),
                                    "❌ Cours annulé — " + cours.getMatiere(),
                                    "Bonjour " + etu.getPrenom() + ",\n\n"
                                        + "Le cours suivant a été annulé :\n"
                                        + "• Matière : " + cours.getMatiere() + "\n"
                                        + "• Classe  : " + cours.getClasse() + "\n"
                                        + "• Groupe  : " + cours.getGroupe() + "\n"
                                        + "• Jour    : " + cours.getCreneau().getJour() + "\n"
                                        + "• Heure   : " + cours.getCreneau().getHeureDebut() + "\n\n"
                                        + "Cordialement,\nUNIV-SCHEDULER"
                                );
                            }

                            tousLesCours.remove(cours);
                            popup.close();
                            chargerDepuisBDD();
                            rafraichirGrille();

                            Alert succes = new Alert(Alert.AlertType.INFORMATION);
                            succes.setTitle("Cours annulé");
                            succes.setHeaderText(null);
                            succes.setContentText("✅ Le cours \"" + cours.getMatiere() + "\" a été annulé !");
                            succes.showAndWait();

                        } else {
                            Alert erreur = new Alert(Alert.AlertType.ERROR);
                            erreur.setTitle("Erreur");
                            erreur.setHeaderText(null);
                            erreur.setContentText("❌ Erreur lors de l'annulation du cours.");
                            erreur.showAndWait();
                        }
                    }
                }); // ← fin ifPresent
            }); // ← fin setOnAction

            contenu.getChildren().add(btnAnnuler);
        } // ← fin if estEnseignant

        Button btnFermer = new Button("Fermer");
        btnFermer.setMaxWidth(Double.MAX_VALUE);
        btnFermer.setStyle(
            "-fx-background-color: #1a237e; -fx-text-fill: white;"
          + "-fx-background-radius: 8; -fx-cursor: hand;");
        btnFermer.setOnAction(e -> popup.close());
        contenu.getChildren().add(btnFermer);

        popup.setScene(new Scene(contenu));
        popup.show();
    } // ← fin afficherDetailsCours
        

    /** Formulaire d'ajout d'un nouveau cours */
   
    public void ouvrirFormulaireAjoutCours() {
        Stage popup = new Stage();
        popup.setTitle("Ajouter un cours");
        popup.initModality(Modality.APPLICATION_MODAL);

        VBox form = new VBox(12);
        form.setPadding(new Insets(24));
        form.setStyle("-fx-background-color: white;");
        form.setPrefWidth(450);

        Label titre = new Label("➕  Nouveau cours");
        titre.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        titre.setTextFill(Color.web("#1a237e"));

        TextField champMatiere = creerChampTexte("Matière (ex: Algorithmique)");
     // ── UFR ──
        Label lblUfr = new Label("UFR :");
        lblUfr.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        ComboBox<String> comboUfr = new ComboBox<>();
        comboUfr.setPromptText("Choisir l'UFR");
        comboUfr.setMaxWidth(Double.MAX_VALUE);
        UfrDAO ufrDAO = new UfrDAO();
        for (String[] ufr : ufrDAO.getTous()) {
            comboUfr.getItems().add(ufr[0] + "|" + ufr[1] + " — " + ufr[2]);
        }

        // ── Département ──
        Label lblDep = new Label("Département :");
        lblDep.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        ComboBox<String> comboDep = new ComboBox<>();
        comboDep.setPromptText("Choisissez d'abord une UFR");
        comboDep.setMaxWidth(Double.MAX_VALUE);
        comboDep.setDisable(true);

        // ── Niveau ──
        Label lblNiveau = new Label("Niveau :");
        lblNiveau.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        ComboBox<String> comboNiveau = new ComboBox<>();
        comboNiveau.getItems().addAll("L1", "L2", "L3", "M1", "M2");
        comboNiveau.setPromptText("Choisir le niveau");
        comboNiveau.setMaxWidth(Double.MAX_VALUE);

        // ── Groupe ──
        Label lblGroupe = new Label("Groupe :");
        lblGroupe.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        ComboBox<String> comboGroupe = new ComboBox<>();
        comboGroupe.getItems().addAll("Groupe A", "Groupe B", "Groupe C");
        comboGroupe.setPromptText("Choisir le groupe");
        comboGroupe.setMaxWidth(Double.MAX_VALUE);

        // ── Cascade UFR → Département ──
        DepartementDAO depDAO = new DepartementDAO();
        comboUfr.setOnAction(e -> {
            comboDep.getItems().clear();
            comboDep.setDisable(true);
            String val = comboUfr.getValue();
            if (val == null) return;
            int ufrId = Integer.parseInt(val.split("\\|")[0]);
            for (String[] dep : depDAO.getParUfr(ufrId)) {
                comboDep.getItems().add(dep[0] + "|" + dep[1]);
            }
            comboDep.setDisable(false);
            comboDep.setPromptText("Choisir le département");
        });

        ComboBox<String> selectJour = new ComboBox<>();
        selectJour.getItems().addAll(JOURS);
        selectJour.setPromptText("Jour");
        selectJour.setMaxWidth(Double.MAX_VALUE);

        TextField champHeure = creerChampTexte("Heure de début (ex: 08:00)");
        TextField champDuree = creerChampTexte("Durée en minutes (ex: 120)");

        // ── Salle ──
        Label lblSalle = new Label("Salle :");
        lblSalle.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        SalleDAO salleDAO = new SalleDAO();
        List<Salle> sallesDisponibles = salleDAO.getDisponibles();

        ComboBox<Salle> comboSalle = new ComboBox<>();
        comboSalle.getItems().addAll(sallesDisponibles);
        comboSalle.setPromptText("Choisir une salle disponible");
        comboSalle.setMaxWidth(Double.MAX_VALUE);
        comboSalle.setConverter(new javafx.util.StringConverter<Salle>() {
            @Override public String toString(Salle s) {
                return s == null ? "" : s.getNumero() + " — capacité : " + s.getCapacite();
            }
            @Override public Salle fromString(String s) { return null; }
        });

        // ── Équipements ──
        Label lblEquip = new Label("🔧 Équipements de la salle :");
        lblEquip.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        VBox boxEquipements = new VBox(4);
        boxEquipements.setPadding(new Insets(8));
        boxEquipements.setStyle("-fx-background-color: #f5f5f5;"
                              + "-fx-background-radius: 8;"
                              + "-fx-border-color: #e0e0e0;"
                              + "-fx-border-radius: 8;");
        boxEquipements.setMinHeight(50);

        Label lblAucun = new Label("Sélectionnez une salle pour voir ses équipements");
        lblAucun.setTextFill(Color.GRAY);
        lblAucun.setFont(Font.font("Arial", 11));
        boxEquipements.getChildren().add(lblAucun);

        // ── Quand salle change ──
        comboSalle.setOnAction(e -> {
            boxEquipements.getChildren().clear();
            Salle salleChoisie = comboSalle.getValue();
            if (salleChoisie == null) {
                boxEquipements.getChildren().add(lblAucun);
                return;
            }

            com.univscheduler.dao.EquipementDAO equipDAO = new com.univscheduler.dao.EquipementDAO();
            List<com.univscheduler.model.Equipement> equips = equipDAO.getBySalle(salleChoisie.getId());

            if (equips.isEmpty()) {
                Label l = new Label("Aucun équipement enregistré pour cette salle");
                l.setTextFill(Color.GRAY);
                l.setFont(Font.font("Arial", 11));
                boxEquipements.getChildren().add(l);
            } else {
                for (com.univscheduler.model.Equipement eq : equips) {
                    String icone = eq.isFonctionnel() ? "✅" : "❌";
                    String texte = icone + " " + eq.getNom();
                    if (eq.getDescription() != null && !eq.getDescription().isEmpty())
                        texte += " — " + eq.getDescription();
                    Label lEquip = new Label(texte);
                    lEquip.setFont(Font.font("Arial", 11));
                    lEquip.setTextFill(eq.isFonctionnel()
                            ? Color.web("#2e7d32") : Color.web("#c62828"));
                    boxEquipements.getChildren().add(lEquip);
                }
            }

            // ── Vérifier conflit horaire ──
            String heureTexte      = champHeure.getText().trim();
            String dureeTexte      = champDuree.getText().trim();
            String jourSelectionne = selectJour.getValue();

            if (!heureTexte.isEmpty() && !dureeTexte.isEmpty() && jourSelectionne != null) {
                try {
                    String[] parts = heureTexte.split(":");
                    LocalTime debutNouv = LocalTime.of(
                        Integer.parseInt(parts[0].trim()),
                        Integer.parseInt(parts[1].trim()));
                    int dureeNouv = Integer.parseInt(dureeTexte);
                    LocalTime finNouv = debutNouv.plusMinutes(dureeNouv);

                    boolean conflit = tousLesCours.stream().anyMatch(c ->
                        c.getSalle() != null
                        && c.getSalle().getId() == salleChoisie.getId()
                        && c.getCreneau().getJour().equalsIgnoreCase(jourSelectionne)
                        && debutNouv.isBefore(c.getCreneau().getHeureFin())
                        && finNouv.isAfter(c.getCreneau().getHeureDebut())
                    );

                    if (conflit) {
                        Label lblAlerte = new Label("⚠️ Cette salle est déjà occupée sur ce créneau !");
                        lblAlerte.setTextFill(Color.web("#c62828"));
                        lblAlerte.setFont(Font.font("Arial", FontWeight.BOLD, 11));
                        lblAlerte.setStyle("-fx-background-color: #ffebee;"
                                         + "-fx-padding: 6 10;"
                                         + "-fx-background-radius: 6;");
                        boxEquipements.getChildren().add(lblAlerte);
                    } else {
                        Label lblLibre = new Label("✅ Salle disponible sur ce créneau !");
                        lblLibre.setTextFill(Color.web("#2e7d32"));
                        lblLibre.setFont(Font.font("Arial", FontWeight.BOLD, 11));
                        lblLibre.setStyle("-fx-background-color: #e8f5e9;"
                                        + "-fx-padding: 6 10;"
                                        + "-fx-background-radius: 6;");
                        boxEquipements.getChildren().add(lblLibre);
                    }
                } catch (Exception ex) {
                    // heure pas encore bien remplie
                }
            } else {
                boolean occupee = tousLesCours.stream()
                    .anyMatch(c -> c.getSalle() != null
                            && c.getSalle().getId() == salleChoisie.getId());
                if (occupee) {
                    Label lblAlerte = new Label("⚠️ Cette salle a des cours — vérifiez l'horaire !");
                    lblAlerte.setTextFill(Color.web("#e65100"));
                    lblAlerte.setFont(Font.font("Arial", FontWeight.BOLD, 11));
                    lblAlerte.setStyle("-fx-background-color: #fff3e0;"
                                     + "-fx-padding: 6 10;"
                                     + "-fx-background-radius: 6;");
                    boxEquipements.getChildren().add(lblAlerte);
                }
            }
        }); // ← fin comboSalle.setOnAction

        Label msgErreur = new Label("");
        msgErreur.setTextFill(Color.web("#c62828"));
        msgErreur.setFont(Font.font("Arial", 11));

        Button btnSauver = new Button("➕  Ajouter le cours");
        btnSauver.setMaxWidth(Double.MAX_VALUE);
        btnSauver.setStyle("-fx-background-color: #1a237e; -fx-text-fill: white;"
                         + "-fx-background-radius: 8; -fx-cursor: hand;"
                         + "-fx-font-weight: bold;");

        btnSauver.setOnAction(e -> {
            if (champMatiere.getText().isEmpty()
                    || selectJour.getValue() == null
                    || comboSalle.getValue() == null
                    || champHeure.getText().isEmpty()
                    || champDuree.getText().isEmpty()
                    || comboUfr.getValue() == null
                    || comboDep.getValue() == null
                    || comboNiveau.getValue() == null
                    || comboGroupe.getValue() == null) {
                msgErreur.setText("⚠  Veuillez remplir tous les champs.");
                return;
            }

            try {
                String[] heureParts = champHeure.getText().trim().split(":");
                if (heureParts.length != 2) {
                    msgErreur.setText("⚠  Heure invalide. Format attendu : 08:00");
                    return;
                }
                LocalTime heureDebut = LocalTime.of(
                    Integer.parseInt(heureParts[0].trim()),
                    Integer.parseInt(heureParts[1].trim()));
                int duree = Integer.parseInt(champDuree.getText().trim());

                int departementId = Integer.parseInt(comboDep.getValue().split("\\|")[0]);
                String nomDep     = comboDep.getValue().split("\\|")[1];
                int ufrId         = Integer.parseInt(comboUfr.getValue().split("\\|")[0]);
                String classe     = comboNiveau.getValue() + " " + nomDep;
                String groupe     = comboGroupe.getValue();

                Salle salle         = comboSalle.getValue();
                Enseignant ens      = (Enseignant) DashboardView.utilisateurConnecte;
                Creneau creneau     = new Creneau(selectJour.getValue(), heureDebut, duree);

                Cours nouveau = new Cours(
                    champMatiere.getText().trim(),
                    classe,
                    groupe,
                    ens, salle, creneau);
                nouveau.setDepartementId(departementId);
                nouveau.setUfrId(ufrId);

                CoursDAO coursDAO = new CoursDAO();
                boolean ok = coursDAO.ajouter(nouveau);

                if (ok) {
                    popup.close();
                    chargerDepuisBDD();
                    rafraichirGrille();

                    Alert succes = new Alert(Alert.AlertType.INFORMATION);
                    succes.setTitle("Succès");
                    succes.setHeaderText(null);
                    succes.setContentText("✅ Le cours \"" + nouveau.getMatiere()
                            + "\" a été ajouté avec succès !");
                    succes.showAndWait();

                    new Thread(() -> {
                        UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
                        List<Etudiant> etudiants = utilisateurDAO.getTousEtudiants();
                        NotificationDAO notifDAO = new NotificationDAO();
                        for (Etudiant etu : etudiants) {
                            notifDAO.ajouter(
                                "📅 Nouveau cours ajouté",
                                "Le cours " + nouveau.getMatiere()
                                    + " (" + nouveau.getClasse() + " — " + nouveau.getGroupe() + ")"
                                    + " a été ajouté le " + nouveau.getCreneau().getJour()
                                    + " à " + nouveau.getCreneau().getHeureDebut() + ".",
                                "COURS", "ETUDIANT", etu.getId()
                            );
                            EmailService.envoyerEmail(
                                etu.getEmail(),
                                "📅 Nouveau cours — " + nouveau.getMatiere(),
                                "Bonjour " + etu.getPrenom() + ",\n\n"
                                    + "Un nouveau cours a été ajouté :\n"
                                    + "• Matière : " + nouveau.getMatiere() + "\n"
                                    + "• Classe  : " + nouveau.getClasse() + "\n"
                                    + "• Groupe  : " + nouveau.getGroupe() + "\n"
                                    + "• Jour    : " + nouveau.getCreneau().getJour() + "\n"
                                    + "• Heure   : " + nouveau.getCreneau().getHeureDebut() + "\n\n"
                                    + "Cordialement,\nUNIV-SCHEDULER"
                            );
                        }
                    }).start();
                
                

                } else {
                    msgErreur.setText("⚠  Erreur lors de l'ajout en base de données.");
                }

            } catch (NumberFormatException ex) {
                msgErreur.setText("⚠  Durée invalide — entrez un nombre entier. Ex: 120");
            } catch (Exception ex) {
                msgErreur.setText("⚠  Erreur : " + ex.getMessage());
                System.err.println("Erreur ajout cours : " + ex.getMessage());
            }
         // ── Assemblage du formulaire ──
        }); // ← fin btnSauver.setOnAction

        // ── Assemblage du formulaire ──
        form.getChildren().addAll(
            titre,
            new Label("Matière :"), champMatiere,
            lblUfr, comboUfr,
            lblDep, comboDep,
            lblNiveau, comboNiveau,
            lblGroupe, comboGroupe,
            new Label("Jour :"), selectJour,
            new Label("Heure de début :"), champHeure,
            new Label("Durée (minutes) :"), champDuree,
            lblSalle, comboSalle,
            lblEquip, boxEquipements,
            msgErreur,
            btnSauver
        );

        ScrollPane scroll = new ScrollPane(form);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: white;");

        Scene scene = new Scene(scroll, 480, 650);
        popup.setScene(scene);
        popup.show();

    } // ← fin ouvrirFormulaireAjoutCours()
     
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

            if (!heureLocale.isBefore(debut) && heureLocale.isBefore(fin)) {

            // N'afficher le cours qu'à son heure de début
            	boolean estDebut = debut.getHour() == heure;

            	if (memeJour && estDebut)
            	    return c;
            	}
            	
        } 
        return null;
    }

    /** Rafraîchit la grille après ajout/modification */
    private void rafraichirGrille() {
        chargerDepuisBDD(); // recharger les cours depuis la BDD
        if (conteneur == null) return; // ← sécurité si appelé sans getVue()
        grille = construireGrille(filtreClasse);
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
        tousLesCours.clear(); // ← vider avant de recharger
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

    private void exporterPDF() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer l'emploi du temps en PDF");
        fileChooser.setInitialFileName("emploi_du_temps_" + filtreClasse + ".pdf");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf")
        );

        File fichier = fileChooser.showSaveDialog(null);

        if (fichier != null) {
            // ── Filtrer les cours selon la classe sélectionnée ──
            List<Cours> coursAExporter = new ArrayList<>();
            for (Cours c : tousLesCours) {
                if (filtreClasse.equals("Tous") || c.getClasse().equals(filtreClasse)) {
                    coursAExporter.add(c);
                }
            }

            // ── Lancer l'export avec les cours filtrés ──
            boolean succes = ExportPDFService.exporterEmploiDuTemps(
                coursAExporter,
                fichier.getAbsolutePath(),
                filtreClasse.equals("Tous") ? "Toutes les classes" : filtreClasse
            );

            if (succes) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export réussi");
                alert.setHeaderText(null);
                alert.setContentText("✅ PDF généré avec succès !\n" + fichier.getAbsolutePath());
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur export");
                alert.setHeaderText(null);
                alert.setContentText("❌ Erreur lors de la génération du PDF.");
                alert.showAndWait();
            }
        }
    }

}
