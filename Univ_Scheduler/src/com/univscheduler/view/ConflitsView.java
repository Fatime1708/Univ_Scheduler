package com.univscheduler.view;

import com.univscheduler.dao.ConflitDAO;
import com.univscheduler.dao.NotificationDAO;
import com.univscheduler.dao.CoursDAO;
import com.univscheduler.dao.SalleDAO;
import com.univscheduler.model.Cours;
import com.univscheduler.model.Salle;

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
import java.util.List;

public class ConflitsView {

    private final ConflitDAO conflitDAO = new ConflitDAO();
    private VBox listeConflits;

    public VBox getVue() {
        VBox vue = new VBox(16);
        vue.setPadding(new Insets(10, 0, 0, 0));

        Label titre = new Label("⚠️  Gestion des Conflits");
        titre.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        titre.setTextFill(Color.web("#b71c1c"));

        // Bouton détecter
        Button btnDetecter = new Button("🔍  Détecter les conflits");
        btnDetecter.setPrefHeight(38);
        btnDetecter.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        btnDetecter.setStyle("-fx-background-color:#b71c1c;-fx-text-fill:white;"
                           + "-fx-background-radius:8;-fx-cursor:hand;");
        btnDetecter.setOnAction(e -> {
            conflitDAO.detecterEtSauvegarder();
            // Envoyer notification au gestionnaire
            NotificationDAO notifDAO = new NotificationDAO();
            int nb = conflitDAO.compterNonResolus();
            if (nb > 0) {
                notifDAO.ajouter(
                    "⚠️ " + nb + " conflit(s) détecté(s)",
                    "Des conflits ont été détectés dans l'emploi du temps.",
                    "CONFLIT", "GESTIONNAIRE", null);
            }
            chargerConflits();
        });

        HBox barre = new HBox(12, btnDetecter);
        barre.setAlignment(Pos.CENTER_LEFT);

        listeConflits = new VBox(10);
        chargerConflits();

        ScrollPane scroll = new ScrollPane(listeConflits);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:transparent;-fx-background:transparent;");

        vue.getChildren().addAll(titre, barre, scroll);
        return vue;
    }

    private void chargerConflits() {
        listeConflits.getChildren().clear();
        List<String[]> conflits = conflitDAO.getNonResolus();

        if (conflits.isEmpty()) {
            Label aucun = new Label("✅  Aucun conflit détecté !");
            aucun.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            aucun.setTextFill(Color.web("#388e3c"));
            aucun.setPadding(new Insets(20));
            aucun.setStyle("-fx-background-color:#e8f5e9;-fx-background-radius:8;");
            listeConflits.getChildren().add(aucun);
            return;
        }

        for (String[] conflit : conflits) {
            listeConflits.getChildren().add(creerCarteConflit(conflit));
        }
    }

    private VBox creerCarteConflit(String[] conflit) {
        // conflit = [id, type, description, created_at, cours1_id, cours2_id]
        VBox carte = new VBox(10);
        carte.setPadding(new Insets(16));
        carte.setStyle("-fx-background-color:#fff8f8;"
                     + "-fx-background-radius:10;"
                     + "-fx-border-color:#ef9a9a;"
                     + "-fx-border-radius:10;"
                     + "-fx-border-width:0 0 0 5;");

        // Type badge
        String typeLabel = conflit[1].equals("SALLE_OCCUPEE")
                ? "🏫 Conflit de salle"
                : conflit[1].equals("ENSEIGNANT_INDISPONIBLE")
                ? "👤 Conflit d'enseignant"
                : "⚠️ Conflit";

        Label lblType = new Label(typeLabel);
        lblType.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        lblType.setTextFill(Color.web("#b71c1c"));

        Label lblDesc = new Label(conflit[2]);
        lblDesc.setFont(Font.font("Arial", 12));
        lblDesc.setTextFill(Color.web("#424242"));
        lblDesc.setWrapText(true);

        Label lblDate = new Label("🕐 " + conflit[3]);
        lblDate.setFont(Font.font("Arial", 11));
        lblDate.setTextFill(Color.GRAY);

        // Boutons actions
        Button btnResoudre = new Button("✅  Marquer résolu");
        btnResoudre.setStyle("-fx-background-color:#e8f5e9;-fx-text-fill:#2e7d32;"
                           + "-fx-background-radius:6;-fx-cursor:hand;"
                           + "-fx-font-weight:bold;");
        btnResoudre.setOnAction(e -> {
            conflitDAO.marquerResolu(Integer.parseInt(conflit[0]));
            chargerConflits();
        });

        Button btnReassigner = new Button("🔄  Réassigner la salle");
        btnReassigner.setStyle("-fx-background-color:#e3f2fd;-fx-text-fill:#1565c0;"
                             + "-fx-background-radius:6;-fx-cursor:hand;"
                             + "-fx-font-weight:bold;");
        btnReassigner.setOnAction(e -> ouvrirReassignation(conflit));

        HBox btnBox = new HBox(10, btnResoudre, btnReassigner);
        carte.getChildren().addAll(lblType, lblDesc, lblDate, btnBox);
        return carte;
    }

    private void ouvrirReassignation(String[] conflit) {
        Stage popup = new Stage();
        popup.setTitle("Réassigner la salle");
        popup.initModality(Modality.APPLICATION_MODAL);

        VBox form = new VBox(14);
        form.setPadding(new Insets(24));
        form.setStyle("-fx-background-color:white;");

        Label titre = new Label("🔄  Réassigner une salle");
        titre.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        titre.setTextFill(Color.web("#1a237e"));

        // Choisir quel cours réassigner
        Label lblCours = new Label("Cours à déplacer :");
        lblCours.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        CoursDAO coursDAO = new CoursDAO();
        ComboBox<Cours> comboCours = new ComboBox<>();
        if (conflit[4] != null) {
            Cours c1 = coursDAO.getById(Integer.parseInt(conflit[4]));
            if (c1 != null) comboCours.getItems().add(c1);
        }
        if (conflit[5] != null) {
            Cours c2 = coursDAO.getById(Integer.parseInt(conflit[5]));
            if (c2 != null) comboCours.getItems().add(c2);
        }
        comboCours.setMaxWidth(Double.MAX_VALUE);
        comboCours.setConverter(new javafx.util.StringConverter<Cours>() {
            @Override public String toString(Cours c) {
                return c == null ? "" : c.getMatiere() + " — " + c.getClasse();
            }
            @Override public Cours fromString(String s) { return null; }
        });

        // Choisir nouvelle salle
        Label lblSalle = new Label("Nouvelle salle :");
        lblSalle.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        SalleDAO salleDAO = new SalleDAO();
        ComboBox<Salle> comboSalle = new ComboBox<>();
        comboSalle.getItems().addAll(salleDAO.getTous());
        comboSalle.setMaxWidth(Double.MAX_VALUE);
        comboSalle.setConverter(new javafx.util.StringConverter<Salle>() {
            @Override public String toString(Salle s) {
                return s == null ? "" : s.getNumero()
                        + " — " + s.getType()
                        + " — " + s.getCapacite() + " places"
                        + (s.isDisponible() ? " ✅" : " ❌");
            }
            @Override public Salle fromString(String s) { return null; }
        });

        Label erreur = new Label("");
        erreur.setTextFill(Color.web("#c62828"));

        Button btnOk = new Button("💾  Réassigner");
        btnOk.setStyle("-fx-background-color:#1a237e;-fx-text-fill:white;"
                     + "-fx-background-radius:8;-fx-cursor:hand;-fx-font-weight:bold;");
        btnOk.setOnAction(e -> {
            Cours cours = comboCours.getValue();
            Salle salle = comboSalle.getValue();
            if (cours == null || salle == null) {
                erreur.setText("⚠  Choisissez un cours et une salle.");
                return;
            }
            boolean ok = coursDAO.modifierSalle(cours.getId(), salle.getId());
            if (ok) {
                conflitDAO.marquerResolu(Integer.parseInt(conflit[0]));
                chargerConflits();
                popup.close();
            } else {
                erreur.setText("❌ Erreur lors de la réassignation.");
            }
        });

        Button btnAnnuler = new Button("Annuler");
        btnAnnuler.setStyle("-fx-background-color:#f5f5f5;"
                          + "-fx-background-radius:8;-fx-cursor:hand;");
        btnAnnuler.setOnAction(e -> popup.close());

        HBox boutons = new HBox(10, btnAnnuler, btnOk);
        boutons.setAlignment(Pos.CENTER_RIGHT);

        form.getChildren().addAll(titre, lblCours, comboCours,
                lblSalle, comboSalle, erreur, boutons);
        popup.setScene(new Scene(form, 440, 320));
        popup.show();
    }
}