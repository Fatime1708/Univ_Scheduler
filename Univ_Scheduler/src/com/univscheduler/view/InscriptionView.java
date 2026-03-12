package com.univscheduler.view;

import com.univscheduler.util.DatabaseConnection;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.sql.*;

public class InscriptionView extends Application {

    private TextField     champNom;
    private TextField     champPrenom;
    private TextField     champEmail;
    private PasswordField champMdp;
    private PasswordField champMdpConfirm;
    private TextField     champNumEtudiant;
    private ComboBox<String> comboClasse;
    private Label         messageErreur;
    private Label         messageSucces;

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.setLeft(construirePanneauGauche());
        root.setCenter(construireFormulaire(stage));

        Scene scene = new Scene(root, 800, 650);
        stage.setTitle("UNIV-SCHEDULER — Inscription");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    // ── Panneau gauche ───────────────────────────────────────────
    private VBox construirePanneauGauche() {
        VBox panneau = new VBox(20);
        panneau.setPrefWidth(280);
        panneau.setAlignment(Pos.CENTER);
        panneau.setPadding(new Insets(40));
        panneau.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #1b5e20, #2e7d32);");

        Label icone = new Label("🎓");
        icone.setFont(Font.font(50));

        Label titre = new Label("INSCRIPTION");
        titre.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 24));
        titre.setTextFill(Color.WHITE);

        Separator sep = new Separator();
        sep.setPrefWidth(120);
        sep.setStyle("-fx-background-color: rgba(255,255,255,0.3);");

        Label info = new Label("Créez votre compte\nétudiants en quelques\nsecondes et accédez\nà votre emploi du temps.");
        info.setFont(Font.font("Arial", 12));
        info.setTextFill(Color.web("#c8e6c9"));
        info.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        info.setAlignment(Pos.CENTER);

        VBox etapes = new VBox(10);
        etapes.setAlignment(Pos.CENTER_LEFT);
        etapes.setPadding(new Insets(16));
        etapes.setStyle("-fx-background-color: rgba(255,255,255,0.1);"
                      + "-fx-background-radius: 10;");
        etapes.getChildren().addAll(
            etape("1", "Remplissez vos\ninformations"),
            etape("2", "Choisissez votre\nclasse"),
            etape("3", "Créez votre compte"),
            etape("4", "Connectez-vous !")
        );

        panneau.getChildren().addAll(icone, titre, sep, info, etapes);
        return panneau;
    }

    private HBox etape(String num, String texte) {
        HBox h = new HBox(10);
        h.setAlignment(Pos.CENTER_LEFT);
        Label lblNum = new Label(num);
        lblNum.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 14));
        lblNum.setTextFill(Color.web("#1b5e20"));
        lblNum.setMinSize(28, 28);
        lblNum.setMaxSize(28, 28);
        lblNum.setAlignment(Pos.CENTER);
        lblNum.setStyle("-fx-background-color: white; -fx-background-radius: 20;");
        Label lblTexte = new Label(texte);
        lblTexte.setFont(Font.font("Arial", 11));
        lblTexte.setTextFill(Color.web("#c8e6c9"));
        h.getChildren().addAll(lblNum, lblTexte);
        return h;
    }

    // ── Formulaire ───────────────────────────────────────────────
    private ScrollPane construireFormulaire(Stage stage) {
        VBox formulaire = new VBox(12);
        formulaire.setAlignment(Pos.TOP_CENTER);
        formulaire.setPadding(new Insets(30, 50, 30, 50));
        formulaire.setStyle("-fx-background-color: #f5f5f5;");

        Label titreForm = new Label("Créer un compte étudiant");
        titreForm.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        titreForm.setTextFill(Color.web("#1b5e20"));

        Label sousTitre = new Label("Remplissez vos informations pour vous inscrire");
        sousTitre.setFont(Font.font("Arial", 12));
        sousTitre.setTextFill(Color.web("#757575"));

        // Champs
        champNom        = champ("Nom (ex: Diallo)");
        champPrenom     = champ("Prénom (ex: Moussa)");
        champEmail      = champ("Email (ex: m.diallo@etu.sn)");
        champNumEtudiant = champ("Numéro étudiant (ex: 20240001)");

        champMdp = new PasswordField();
        champMdp.setPromptText("Mot de passe (min. 6 caractères)");
        champMdp.setStyle(styleChamp());

        champMdpConfirm = new PasswordField();
        champMdpConfirm.setPromptText("Confirmer le mot de passe");
        champMdpConfirm.setStyle(styleChamp());

        // Classe
        comboClasse = new ComboBox<>();
        comboClasse.getItems().addAll(
            "L1 Informatique", "L2 Informatique", "L3 Informatique",
            "M1 Informatique", "M2 Informatique"
        );
        comboClasse.setPromptText("Choisir votre classe");
        comboClasse.setMaxWidth(Double.MAX_VALUE);
        comboClasse.setStyle(styleChamp());

        // Messages
        messageErreur = new Label("");
        messageErreur.setTextFill(Color.web("#c62828"));
        messageErreur.setFont(Font.font("Arial", 12));
        messageErreur.setWrapText(true);
        messageErreur.setVisible(false);

        messageSucces = new Label("");
        messageSucces.setTextFill(Color.web("#2e7d32"));
        messageSucces.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        messageSucces.setVisible(false);

        // Bouton inscription
        Button btnInscrire = new Button("✅  Créer mon compte");
        btnInscrire.setMaxWidth(Double.MAX_VALUE);
        btnInscrire.setPrefHeight(44);
        btnInscrire.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        btnInscrire.setStyle("-fx-background-color: #1b5e20; -fx-text-fill: white;"
                           + "-fx-background-radius: 8; -fx-cursor: hand;");
        btnInscrire.setOnMouseEntered(e -> btnInscrire.setStyle(
            "-fx-background-color: #2e7d32; -fx-text-fill: white;"
          + "-fx-background-radius: 8; -fx-cursor: hand;"));
        btnInscrire.setOnMouseExited(e -> btnInscrire.setStyle(
            "-fx-background-color: #1b5e20; -fx-text-fill: white;"
          + "-fx-background-radius: 8; -fx-cursor: hand;"));
        btnInscrire.setOnAction(e -> inscrire(stage));

        // Retour connexion
        Button btnRetour = new Button("← Retour à la connexion");
        btnRetour.setStyle("-fx-background-color: transparent;"
                         + "-fx-text-fill: #1b5e20;"
                         + "-fx-cursor: hand;"
                         + "-fx-font-weight: bold;");
        btnRetour.setOnAction(e -> {
            try { new LoginView().start(stage); }
            catch (Exception ex) { ex.printStackTrace(); }
        });

        formulaire.getChildren().addAll(
            titreForm, sousTitre, new Separator(),
            new Label("Nom :"),               champNom,
            new Label("Prénom :"),            champPrenom,
            new Label("Email :"),             champEmail,
            new Label("Numéro étudiant :"),   champNumEtudiant,
            new Label("Classe :"),            comboClasse,
            new Label("Mot de passe :"),      champMdp,
            new Label("Confirmer :"),         champMdpConfirm,
            messageErreur, messageSucces,
            btnInscrire, btnRetour
        );

        ScrollPane scroll = new ScrollPane(formulaire);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #f5f5f5; -fx-background: #f5f5f5;");
        return scroll;
    }

    // ── Logique inscription ──────────────────────────────────────
    private void inscrire(Stage stage) {
        // Validation
        if (champNom.getText().isEmpty() || champPrenom.getText().isEmpty()
                || champEmail.getText().isEmpty() || champMdp.getText().isEmpty()
                || champNumEtudiant.getText().isEmpty() || comboClasse.getValue() == null) {
            afficherErreur("⚠  Tous les champs sont obligatoires.");
            return;
        }
        if (!champMdp.getText().equals(champMdpConfirm.getText())) {
            afficherErreur("⚠  Les mots de passe ne correspondent pas.");
            return;
        }
        if (champMdp.getText().length() < 6) {
            afficherErreur("⚠  Le mot de passe doit contenir au moins 6 caractères.");
            return;
        }
        if (!champEmail.getText().contains("@")) {
            afficherErreur("⚠  Email invalide.");
            return;
        }

        String sqlUser = "INSERT INTO utilisateurs "
                       + "(nom, prenom, email, mot_de_passe, role) VALUES (?,?,?,?,?)";
        String sqlEtu  = "INSERT INTO etudiants "
                       + "(id, classe, groupe, numero_etudiant) VALUES (?,?,?,?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Insérer dans utilisateurs
                PreparedStatement psUser = conn.prepareStatement(
                        sqlUser, Statement.RETURN_GENERATED_KEYS);
                psUser.setString(1, champNom.getText().trim());
                psUser.setString(2, champPrenom.getText().trim());
                psUser.setString(3, champEmail.getText().trim());
                psUser.setString(4, champMdp.getText());
                psUser.setString(5, "ETUDIANT");
                psUser.executeUpdate();

                int userId = 0;
                ResultSet cle = psUser.getGeneratedKeys();
                if (cle.next()) userId = cle.getInt(1);

                // 2. Insérer dans etudiants
                PreparedStatement psEtu = conn.prepareStatement(sqlEtu);
                psEtu.setInt(1,    userId);
                psEtu.setString(2, comboClasse.getValue());
                psEtu.setString(3, "Groupe A");
                psEtu.setString(4, champNumEtudiant.getText().trim());
                psEtu.executeUpdate();

                conn.commit();

                // Succès
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Inscription réussie !");
                alert.setHeaderText("✅  Compte créé avec succès !");
                alert.setContentText("Bienvenue " + champPrenom.getText()
                        + " " + champNom.getText() + " !\n\n"
                        + "Vous pouvez maintenant vous connecter\n"
                        + "avec votre email et mot de passe.");
                alert.showAndWait();

                new LoginView().start(stage);

            } catch (SQLException ex) {
                conn.rollback();
                if (ex.getMessage().contains("Duplicate entry")) {
                    afficherErreur("⚠  Cet email ou numéro étudiant est déjà utilisé.");
                } else {
                    afficherErreur("❌  Erreur : " + ex.getMessage());
                }
            }
        } catch (Exception e) {
            afficherErreur("❌  Erreur de connexion à la base.");
        }
    }

    private void afficherErreur(String msg) {
        messageErreur.setText(msg);
        messageErreur.setVisible(true);
        messageSucces.setVisible(false);
    }

    private TextField champ(String placeholder) {
        TextField t = new TextField();
        t.setPromptText(placeholder);
        t.setStyle(styleChamp());
        return t;
    }

    private String styleChamp() {
        return "-fx-background-color: white; -fx-border-color: #bdbdbd;"
             + "-fx-border-radius: 8; -fx-background-radius: 8;"
             + "-fx-padding: 8 12 8 12;";
    }
}