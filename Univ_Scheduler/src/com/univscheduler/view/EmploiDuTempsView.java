package com.univscheduler.view;
import com.univscheduler.dao.SalleDAO;
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
          + "-fx-cursor: hand;");

        // Matière
        Label matiere = new Label(cours.getMatiere());
        matiere.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        matiere.setTextFill(Color.web(COULEURS_BORD[index % COULEURS_BORD.length]));

        // Classe (ex: LGI1, L2 Informatique...)
     // Classe
        Label classe = new Label("🎓 " + cours.getClasse());
        classe.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        classe.setTextFill(Color.web("#1a237e"));

        // Salle avec ID
        Label salle = new Label("📍 Salle : " + cours.getSalle().getId()
                + " — " + cours.getSalle().getNumero());
        salle.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        salle.setTextFill(Color.web("#c62828"));

        // Enseignant
        Label enseignant = new Label("👤 " + cours.getEnseignant().getNom());
        enseignant.setFont(Font.font("Arial", 9));
        enseignant.setTextFill(Color.web("#616161"));

        cellule.getChildren().addAll(matiere, classe, salle, enseignant);

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
     // Bouton annulation — visible uniquement pour l'enseignant propriétaire
        boolean estProprietaire = DashboardView.utilisateurConnecte instanceof Enseignant
            && DashboardView.utilisateurConnecte.getId() == cours.getEnseignant().getId();

        if (estProprietaire) {
            Button btnAnnuler = new Button("❌  Annuler ce cours");
            btnAnnuler.setMaxWidth(Double.MAX_VALUE);
            btnAnnuler.setStyle("-fx-background-color:#c62828;-fx-text-fill:white;"
                              + "-fx-background-radius:8;-fx-cursor:hand;"
                              + "-fx-font-weight:bold;");
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
                            // ── Notifier tous les étudiants ──
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
                            }

                            tousLesCours.remove(cours);
                            rafraichirGrille();
                            popup.close();
                        }
                    }
                });
            });
            contenu.getChildren().add(btnAnnuler);
        } 

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
        TextField champClasse  = creerChampTexte("Classe (ex: L2 Informatique)");
        TextField champGroupe  = creerChampTexte("Groupe (ex: Groupe A)");

        ComboBox<String> selectJour = new ComboBox<>();
        selectJour.getItems().addAll(JOURS);
        selectJour.setPromptText("Jour");
        selectJour.setMaxWidth(Double.MAX_VALUE);

        TextField champHeure = creerChampTexte("Heure de début (ex: 08:00)");
        TextField champDuree = creerChampTexte("Durée en minutes (ex: 120)");

        // ── Salle : ComboBox des salles disponibles ──
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

        // ── Zone équipements ──
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

        // Charger équipements quand salle change
        comboSalle.setOnAction(e -> {
            boxEquipements.getChildren().clear();
            Salle salleChoisie = comboSalle.getValue();
            if (salleChoisie == null) {
                boxEquipements.getChildren().add(lblAucun);
                return;
            }
            // Charger équipements depuis la BDD
            String sql = "SELECT nom, description, fonctionnel "
                       + "FROM equipements WHERE salle_id = ?";
            try (java.sql.Connection conn = com.univscheduler.util.DatabaseConnection.getConnection();
                 java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, salleChoisie.getId());
                java.sql.ResultSet rs = ps.executeQuery();
                boolean aucunEquip = true;
                while (rs.next()) {
                    aucunEquip = false;
                    String nom         = rs.getString("nom");
                    String description = rs.getString("description");
                    boolean fonctionnel = rs.getBoolean("fonctionnel");
                    String icone = fonctionnel ? "✅" : "❌";
                    String texte = icone + " " + nom;
                    if (description != null && !description.isEmpty())
                        texte += " — " + description;
                    Label lEquip = new Label(texte);
                    lEquip.setFont(Font.font("Arial", 11));
                    lEquip.setTextFill(fonctionnel
                            ? Color.web("#2e7d32") : Color.web("#c62828"));
                    boxEquipements.getChildren().add(lEquip);
                }
                if (aucunEquip) {
                    Label l = new Label("Aucun équipement enregistré pour cette salle");
                    l.setTextFill(Color.GRAY);
                    l.setFont(Font.font("Arial", 11));
                    boxEquipements.getChildren().add(l);
                }
            } catch (Exception ex) {
                System.err.println("Erreur équipements : " + ex.getMessage());
            }
         // Vérifier si la salle est occupée par un cours existant
            boolean occupee = tousLesCours.stream()
                .anyMatch(c -> c.getSalle() != null
                        && c.getSalle().getId() == salleChoisie.getId());
            if (occupee) {
                Label lblAlerte = new Label("⚠️ Cette salle est déjà occupée par un cours !");
                lblAlerte.setTextFill(Color.web("#c62828"));
                lblAlerte.setFont(Font.font("Arial", FontWeight.BOLD, 11));
                lblAlerte.setStyle("-fx-background-color: #ffebee;"
                                 + "-fx-padding: 6 10;"
                                 + "-fx-background-radius: 6;");
                boxEquipements.getChildren().add(lblAlerte);
            }
        });

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
                    || champDuree.getText().isEmpty()) {
                msgErreur.setText("⚠  Veuillez remplir tous les champs.");
                return;
            }
            try {
                String heureTexte = champHeure.getText().trim();
                String dureeTexte = champDuree.getText().trim();

                String[] heureParts = heureTexte.split(":");
                if (heureParts.length != 2) {
                    msgErreur.setText("⚠  Heure invalide. Format attendu : 08:00");
                    return;
                }
                LocalTime heureDebut = LocalTime.of(
                    Integer.parseInt(heureParts[0].trim()),
                    Integer.parseInt(heureParts[1].trim()));
                int duree = Integer.parseInt(dureeTexte);

                Salle salle     = comboSalle.getValue();
                Enseignant ens  = (Enseignant) DashboardView.utilisateurConnecte;
                Creneau creneau = new Creneau(selectJour.getValue(), heureDebut, duree);

                Cours nouveau = new Cours(
                    champMatiere.getText().trim(),
                    champClasse.getText().trim(),
                    champGroupe.getText().trim(),
                    ens, salle, creneau);

                CoursDAO coursDAO = new CoursDAO();
                boolean ok = coursDAO.ajouter(nouveau);
                if (ok) {
                    tousLesCours.add(nouveau);
                    rafraichirGrille();

                    // ── Notifier tous les étudiants ──
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
                    }

                    popup.close();
                } else {
                    msgErreur.setText("⚠  Erreur lors de l'ajout en base de données.");
                }
            } catch (NumberFormatException ex) {
                msgErreur.setText("⚠  Durée invalide — entrez un nombre entier. Ex: 120");
            } catch (Exception ex) {
                msgErreur.setText("⚠  Erreur : " + ex.getMessage());
                System.err.println("Erreur ajout cours : " + ex.getMessage());
            }
        });
        form.getChildren().addAll(
            titre,
            new Label("Matière :"),  champMatiere,
            new Label("Classe :"),   champClasse,
            new Label("Groupe :"),   champGroupe,
            new Label("Jour :"),     selectJour,
            new Label("Heure :"),    champHeure,
            new Label("Durée :"),    champDuree,
            lblSalle,                comboSalle,
            lblEquip,                boxEquipements,
            msgErreur,               btnSauver
        );

        ScrollPane scroll = new ScrollPane(form);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: white;");

        popup.setScene(new javafx.scene.Scene(scroll, 450, 600));
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
