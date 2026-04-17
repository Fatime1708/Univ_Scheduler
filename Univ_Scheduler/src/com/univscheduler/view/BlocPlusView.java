package com.univscheduler.view;

import com.univscheduler.dao.BlocPlusDAO;
import com.univscheduler.dao.UtilisateurDAO;
import com.univscheduler.model.Enseignant;
import com.univscheduler.model.Etudiant;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.util.List;

public class BlocPlusView {

    private final Enseignant enseignant;
    private final BlocPlusDAO blocPlusDAO = new BlocPlusDAO();
    private VBox listeBox;

    public BlocPlusView(Enseignant enseignant) {
        this.enseignant = enseignant;
    }

    public void afficher() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("⭐ Bloc des Plus");

        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: #f9f9f9;");

        // Titre
        Label titre = new Label("⭐ Bloc des Plus");
        titre.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Bouton ajouter
        Button btnAjouter = new Button("+ Ajouter un étudiant");
        btnAjouter.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; "
                + "-fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 6;");
        btnAjouter.setOnAction(e -> afficherFormulaireAjout(stage));

        // Liste
        listeBox = new VBox(10);
        chargerListe();

        ScrollPane scroll = new ScrollPane(listeBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        root.getChildren().addAll(titre, btnAjouter, new Separator(), scroll);

        Scene scene = new Scene(root, 600, 500);
        stage.setScene(scene);
        stage.show();
    }

    private void chargerListe() {
        listeBox.getChildren().clear();
        List<String[]> liste = blocPlusDAO.getParEnseignant(enseignant.getId());

        if (liste.isEmpty()) {
            Label vide = new Label("Aucun étudiant dans le bloc des plus.");
            vide.setStyle("-fx-text-fill: #999; -fx-font-style: italic;");
            listeBox.getChildren().add(vide);
            return;
        }

        for (String[] entry : liste) {
            // entry : [id, nom, email, motif, date_ajout]
            HBox carte = new HBox(12);
            carte.setPadding(new Insets(12));
            carte.setStyle("-fx-background-color: white; -fx-background-radius: 8; "
                    + "-fx-border-color: #e0e0e0; -fx-border-radius: 8;");

            VBox infos = new VBox(4);
            Label nom = new Label("⭐ " + entry[1]);
            nom.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            Label motif = new Label("📝 " + (entry[3] != null ? entry[3] : "Aucun motif"));
            motif.setStyle("-fx-text-fill: #555;");
            Label date = new Label("📅 " + entry[4].substring(0, 10));
            date.setStyle("-fx-text-fill: #999; -fx-font-size: 11px;");
            infos.getChildren().addAll(nom, motif, date);

            Button btnSupprimer = new Button("🗑");
            btnSupprimer.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; "
                    + "-fx-background-radius: 6; -fx-cursor: hand;");
            btnSupprimer.setOnAction(e -> {
                blocPlusDAO.supprimer(Integer.parseInt(entry[0]));
                chargerListe();
            });

            HBox.setHgrow(infos, Priority.ALWAYS);
            carte.getChildren().addAll(infos, btnSupprimer);
            listeBox.getChildren().add(carte);
        }
    }

    private void afficherFormulaireAjout(Stage parent) {
        Stage popup = new Stage();
        popup.initModality(Modality.WINDOW_MODAL);
        popup.initOwner(parent);
        popup.setTitle("Ajouter un étudiant");

        VBox form = new VBox(14);
        form.setPadding(new Insets(24));
        form.setStyle("-fx-background-color: white;");

        Label titre = new Label("Choisir un étudiant");
        titre.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Liste étudiants
        UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
        List<Etudiant> etudiants = utilisateurDAO.getTousEtudiants();
        ComboBox<Etudiant> comboEtudiant = new ComboBox<>(
                FXCollections.observableArrayList(etudiants));
        comboEtudiant.setPromptText("Sélectionner un étudiant");
        comboEtudiant.setMaxWidth(Double.MAX_VALUE);
        comboEtudiant.setCellFactory(lv -> new ListCell<>() {
            protected void updateItem(Etudiant e, boolean empty) {
                super.updateItem(e, empty);
                setText(empty || e == null ? null : e.getNom() + " " + e.getPrenom());
            }
        });
        comboEtudiant.setButtonCell(new ListCell<>() {
            protected void updateItem(Etudiant e, boolean empty) {
                super.updateItem(e, empty);
                setText(empty || e == null ? null : e.getNom() + " " + e.getPrenom());
            }
        });

        // Motif
        Label labelMotif = new Label("Motif (optionnel) :");
        TextField champMotif = new TextField();
        champMotif.setPromptText("Ex: Excellent travail en TD, participation active...");

        // Bouton sauvegarder
        Button btnSauver = new Button("⭐ Ajouter au bloc");
        btnSauver.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; "
                + "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 6;");
        btnSauver.setMaxWidth(Double.MAX_VALUE);
        btnSauver.setOnAction(e -> {
            if (comboEtudiant.getValue() == null) {
                new Alert(Alert.AlertType.WARNING, "Veuillez sélectionner un étudiant.").showAndWait();
                return;
            }
            boolean ok = blocPlusDAO.ajouter(
                    enseignant.getId(),
                    comboEtudiant.getValue().getId(),
                    champMotif.getText().trim()
            );
            if (ok) {
                popup.close();
                chargerListe();
            } else {
                new Alert(Alert.AlertType.ERROR, "Erreur lors de l'ajout.").showAndWait();
            }
        });

        form.getChildren().addAll(titre, comboEtudiant, labelMotif, champMotif, btnSauver);
        popup.setScene(new Scene(form, 420, 280));
        popup.show();
    }
}
