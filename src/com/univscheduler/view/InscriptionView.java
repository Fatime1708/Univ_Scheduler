package com.univscheduler.view;

import com.univscheduler.model.*;
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

/**
 * Page d'inscription avec code d'activation.
 * L'étudiant/enseignant doit avoir un code fourni par l'Admin.
 */
public class InscriptionView extends Application {

    // ── Composants ───────────────────────────────────────────────
    private TextField     champNom;
    private TextField     champPrenom;
    private TextField     champEmail;
    private PasswordField champMdp;
    private PasswordField champMdpConfirm;
    private TextField     champCode;
    private TextField     champClasse;
    private TextField     champGroupe;
    private Label         messageErreur;
    private Label         messageSucces;
    private VBox          champsSupp;

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

    // ════════════════════════════════════════════════════════════
    //  PANNEAU GAUCHE
    // ════════════════════════════════════════════════════════════

    private VBox construirePanneauGauche() {
        VBox panneau = new VBox(20);
        panneau.setPrefWidth(280);
        panneau.setAlignment(Pos.CENTER);
        panneau.setPadding(new Insets(40));
        panneau.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #1b5e20, #2e7d32);");

        Label icone = new Label("📝");
        icone.setFont(Font.font(50));

        Label titre = new Label("INSCRIPTION");
        titre.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 24));
        titre.setTextFill(Color.WHITE);

        Separator sep = new Separator();
        sep.setPrefWidth(120);
        sep.setStyle("-fx-background-color: rgba(255,255,255,0.3);");

        Label info = new Label("Pour créer un compte,\nvous avez besoin d'un\ncode d'activation\nfourni par l'Admin.");
        info.setFont(Font.font("Arial", 12));
        info.setTextFill(Color.web("#c8e6c9"));
        info.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        info.setAlignment(Pos.CENTER);

        // Étapes
        VBox etapes = new VBox(10);
        etapes.setAlignment(Pos.CENTER_LEFT);
        etapes.setPadding(new Insets(16));
        etapes.setStyle("-fx-background-color: rgba(255,255,255,0.1);"
                      + "-fx-background-radius: 10;");

        etapes.getChildren().addAll(
            etape("1", "Obtenez un code\nde l'Administrateur"),
            etape("2", "Remplissez vos\ninformations"),
            etape("3", "Entrez le code\nd'activation"),
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
        lblNum.setStyle("-fx-background-color: white;"
                      + "-fx-background-radius: 20;");

        Label lblTexte = new Label(texte);
        lblTexte.setFont(Font.font("Arial", 11));
        lblTexte.setTextFill(Color.web("#c8e6c9"));

        h.getChildren().addAll(lblNum, lblTexte);
        return h;
    }

    // ════════════════════════════════════════════════════════════
    //  FORMULAIRE
    // ════════════════════════════════════════════════════════════

    private ScrollPane construireFormulaire(Stage stage) {
        VBox formulaire = new VBox(14);
        formulaire.setAlignment(Pos.TOP_CENTER);
        formulaire.setPadding(new Insets(30, 50, 30, 50));
        formulaire.setStyle("-fx-background-color: #f5f5f5;");

        Label titreForm = new Label("Créer un compte");
        titreForm.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        titreForm.setTextFill(Color.web("#1b5e20"));

        Label sousTitre = new Label("Remplissez vos informations et entrez votre code d'activation");
        sousTitre.setFont(Font.font("Arial", 12));
        sousTitre.setTextFill(Color.web("#757575"));

        // Champs principaux
        champNom    = champ("Nom");
        champPrenom = champ("Prénom");
        champEmail  = champ("Email (ex: j.dupont@etu.sn)");
        champMdp    = new PasswordField();
        champMdp.setPromptText("Mot de passe");
        champMdp.setStyle(styleChamp());

        champMdpConfirm = new PasswordField();
        champMdpConfirm.setPromptText("Confirmer le mot de passe");
        champMdpConfirm.setStyle(styleChamp());

        // Champ code d'activation
        champCode = champ("Code d'activation (ex: ETU-2024-XK9P)");
        champCode.setStyle(styleChamp()
            + "-fx-border-color: #2e7d32; -fx-border-width: 2;");

        // Champs supplémentaires (cachés au début, visibles après vérif du code)
        champsSupp = new VBox(10);
        champsSupp.setVisible(false);
        champsSupp.setManaged(false);

        champClasse = champ("Classe (ex: L2 Informatique)");
        champGroupe = champ("Groupe (ex: Groupe A)");

        Label lblSupp = new Label("Informations complémentaires");
        lblSupp.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        lblSupp.setTextFill(Color.web("#1b5e20"));

        champsSupp.getChildren().addAll(lblSupp, champClasse, champGroupe);

        // Messages
        messageErreur = new Label("");
        messageErreur.setTextFill(Color.web("#c62828"));
        messageErreur.setFont(Font.font("Arial", 12));
        messageErreur.setVisible(false);
        messageErreur.setWrapText(true);

        messageSucces = new Label("");
        messageSucces.setTextFill(Color.web("#2e7d32"));
        messageSucces.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        messageSucces.setVisible(false);

        // Bouton vérifier code
        Button btnVerifCode = new Button("🔑  Vérifier le code");
        btnVerifCode.setMaxWidth(Double.MAX_VALUE);
        btnVerifCode.setPrefHeight(38);
        btnVerifCode.setStyle("-fx-background-color: #388e3c; -fx-text-fill: white;"
                            + "-fx-background-radius: 8; -fx-cursor: hand;"
                            + "-fx-font-weight: bold;");
        btnVerifCode.setOnAction(e -> verifierCode());

        // Bouton inscription
        Button btnInscrire = new Button("✅  Créer mon compte");
        btnInscrire.setMaxWidth(Double.MAX_VALUE);
        btnInscrire.setPrefHeight(44);
        btnInscrire.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        btnInscrire.setStyle("-fx-background-color: #1b5e20; -fx-text-fill: white;"
                           + "-fx-background-radius: 8; -fx-cursor: hand;");
        btnInscrire.setOnAction(e -> inscrire(stage));
        btnInscrire.setOnMouseEntered(e -> btnInscrire.setStyle(
            "-fx-background-color: #2e7d32; -fx-text-fill: white;"
          + "-fx-background-radius: 8; -fx-cursor: hand;"));
        btnInscrire.setOnMouseExited(e -> btnInscrire.setStyle(
            "-fx-background-color: #1b5e20; -fx-text-fill: white;"
          + "-fx-background-radius: 8; -fx-cursor: hand;"));

        // Lien retour connexion
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
            new Label("Nom :"),              champNom,
            new Label("Prénom :"),           champPrenom,
            new Label("Email :"),            champEmail,
            new Label("Mot de passe :"),     champMdp,
            new Label("Confirmer :"),        champMdpConfirm,
            new Label("Code d'activation :"),champCode,
            btnVerifCode,
            champsSupp,
            messageErreur, messageSucces,
            btnInscrire, btnRetour
        );

        ScrollPane scroll = new ScrollPane(formulaire);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #f5f5f5; -fx-background: #f5f5f5;");
        return scroll;
    }

    // ════════════════════════════════════════════════════════════
    //  LOGIQUE
    // ════════════════════════════════════════════════════════════

    /**
     * Vérifie si le code est valide dans la base de données.
     */
    private void verifierCode() {
        String code = champCode.getText().trim();
        if (code.isEmpty()) {
            afficherErreur("⚠  Entrez un code d'activation.");
            return;
        }

        String sql = "SELECT * FROM codes_activation WHERE code = ? AND utilise = FALSE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, code);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                // Code valide !
                messageErreur.setVisible(false);
                messageSucces.setText("✅  Code valide ! Rôle : " + role);
                messageSucces.setVisible(true);
                champCode.setStyle(styleChamp()
                    + "-fx-border-color: #2e7d32; -fx-border-width: 2;");

                // Afficher champs supplémentaires si étudiant
                if (role.equals("ETUDIANT")) {
                    champsSupp.setVisible(true);
                    champsSupp.setManaged(true);
                } else {
                    champsSupp.setVisible(false);
                    champsSupp.setManaged(false);
                }
            } else {
                afficherErreur("⚠  Code invalide ou déjà utilisé.");
                champCode.setStyle(styleChamp()
                    + "-fx-border-color: #c62828; -fx-border-width: 2;");
                champsSupp.setVisible(false);
                champsSupp.setManaged(false);
                messageSucces.setVisible(false);
            }

        } catch (SQLException e) {
            afficherErreur("❌  Erreur de connexion à la base.");
        }
    }

    /**
     * Crée le compte dans la base de données.
     */
    private void inscrire(Stage stage) {
        // Validation des champs
        if (champNom.getText().isEmpty() || champPrenom.getText().isEmpty()
                || champEmail.getText().isEmpty() || champMdp.getText().isEmpty()
                || champCode.getText().isEmpty()) {
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

        String code = champCode.getText().trim();

        // Vérifier le code une dernière fois
        String sqlCode = "SELECT * FROM codes_activation WHERE code = ? AND utilise = FALSE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement psCode = conn.prepareStatement(sqlCode)) {

            psCode.setString(1, code);
            ResultSet rs = psCode.executeQuery();

            if (!rs.next()) {
                afficherErreur("⚠  Code invalide ou déjà utilisé.");
                return;
            }

            String role = rs.getString("role");
            int codeId  = rs.getInt("id");

            conn.setAutoCommit(false);
            try {
                // 1. Insérer l'utilisateur
                String sqlUser = "INSERT INTO utilisateurs "
                        + "(nom, prenom, email, mot_de_passe, role) VALUES (?,?,?,?,?)";
                PreparedStatement psUser = conn.prepareStatement(
                        sqlUser, Statement.RETURN_GENERATED_KEYS);
                psUser.setString(1, champNom.getText());
                psUser.setString(2, champPrenom.getText());
                psUser.setString(3, champEmail.getText());
                psUser.setString(4, champMdp.getText());
                psUser.setString(5, role);
                psUser.executeUpdate();

                int userId = 0;
                ResultSet cleUser = psUser.getGeneratedKeys();
                if (cleUser.next()) userId = cleUser.getInt(1);

                // 2. Insérer les données selon le rôle
                if (role.equals("ETUDIANT")) {
                    String sqlEtu = "INSERT INTO etudiants "
                            + "(id, classe, groupe, numero_etudiant) VALUES (?,?,?,?)";
                    PreparedStatement psEtu = conn.prepareStatement(sqlEtu);
                    psEtu.setInt(1,    userId);
                    psEtu.setString(2, champClasse.getText());
                    psEtu.setString(3, champGroupe.getText());
                    psEtu.setString(4, "ETU" + System.currentTimeMillis());
                    psEtu.executeUpdate();

                } else if (role.equals("ENSEIGNANT")) {
                    String sqlEns = "INSERT INTO enseignants "
                            + "(id, specialite, departement) VALUES (?,?,?)";
                    PreparedStatement psEns = conn.prepareStatement(sqlEns);
                    psEns.setInt(1,    userId);
                    psEns.setString(2, "À définir");
                    psEns.setString(3, "UFR Sciences");
                    psEns.executeUpdate();
                }

                // 3. Marquer le code comme utilisé
                String sqlMaj = "UPDATE codes_activation SET utilise = TRUE WHERE id = ?";
                PreparedStatement psMaj = conn.prepareStatement(sqlMaj);
                psMaj.setInt(1, codeId);
                psMaj.executeUpdate();

                conn.commit();

                // Succès !
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Inscription réussie !");
                alert.setHeaderText("✅  Compte créé avec succès !");
                alert.setContentText("Bienvenue " + champPrenom.getText()
                        + " " + champNom.getText() + " !\n\n"
                        + "Vous pouvez maintenant vous connecter\n"
                        + "avec votre email et mot de passe.");
                alert.showAndWait();

                // Retour à la page de connexion
                new LoginView().start(stage);

            } catch (SQLException ex) {
                conn.rollback();
                if (ex.getMessage().contains("Duplicate entry")) {
                    afficherErreur("⚠  Cet email est déjà utilisé.");
                } else {
                    afficherErreur("❌  Erreur lors de l'inscription : " + ex.getMessage());
                }
            }

        } catch (Exception e) {
            afficherErreur("❌  Erreur de connexion à la base.");
        }
    }

    // ════════════════════════════════════════════════════════════
    //  UTILITAIRES
    // ════════════════════════════════════════════════════════════

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

    public static void main(String[] args) { launch(args); }
}
