package com.univscheduler.view;

import com.univscheduler.dao.UtilisateurDAO;
import com.univscheduler.model.*;
import com.univscheduler.service.AuthService;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.List;

/**
 * Page de connexion — authentification via MySQL (UtilisateurDAO).
 */
public class LoginView extends Application {

    // ── DAO & Service ────────────────────────────────────────────
    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
    private final AuthService    authService    = new AuthService();

    // ── Composants ───────────────────────────────────────────────
    private TextField     emailField;
    private PasswordField passwordField;
    private Label         messageErreur;

    // ── Liste des utilisateurs chargée depuis MySQL ──────────────
    private List<Utilisateur> utilisateurs;

    @Override
    public void start(Stage primaryStage) {
        // Charger les utilisateurs depuis MySQL
        utilisateurs = utilisateurDAO.getTous();

        BorderPane root = construireInterface(primaryStage);
        Scene scene = new Scene(root, 800, 600);

        primaryStage.setTitle("UNIV-SCHEDULER — Connexion");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    // ════════════════════════════════════════════════════════════
    //  INTERFACE
    // ════════════════════════════════════════════════════════════

    private BorderPane construireInterface(Stage stage) {
        BorderPane root = new BorderPane();
        root.setLeft(construirePanneauGauche());
        root.setCenter(construireFormulaire(stage));
        return root;
    }

    private VBox construirePanneauGauche() {
        VBox panneau = new VBox(20);
        panneau.setPrefWidth(320);
        panneau.setAlignment(Pos.CENTER);
        panneau.setPadding(new Insets(40));
        panneau.setStyle("-fx-background-color: linear-gradient(to bottom, #1a237e, #4a148c);");

        Circle cercle = new Circle(45);
        cercle.setFill(Color.WHITE);
        cercle.setOpacity(0.15);

        Label icone     = new Label("🎓");
        icone.setFont(Font.font(50));

        Label titre     = new Label("UNIV");
        titre.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 32));
        titre.setTextFill(Color.WHITE);

        Label sousTitre = new Label("SCHEDULER");
        sousTitre.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        sousTitre.setTextFill(Color.web("#b0bec5"));

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: rgba(255,255,255,0.3);");
        sep.setPrefWidth(120);

        Label slogan = new Label("Gestion intelligente\ndes salles et emplois\ndu temps");
        slogan.setFont(Font.font("Arial", 13));
        slogan.setTextFill(Color.web("#b0bec5"));
        slogan.setAlignment(Pos.CENTER);
        slogan.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        HBox badges = new HBox(8);
        badges.setAlignment(Pos.CENTER);
        badges.getChildren().addAll(
            badge("Admin",        "#ef5350"),
            badge("Gestionnaire", "#ff9800"),
            badge("Enseignant",   "#66bb6a")
        );
        HBox badges2 = new HBox(8);
        badges2.setAlignment(Pos.CENTER);
        badges2.getChildren().add(badge("Étudiant", "#42a5f5"));

        panneau.getChildren().addAll(icone, titre, sousTitre, sep, slogan, badges, badges2);
        return panneau;
    }

    private VBox construireFormulaire(Stage stage) {
        VBox formulaire = new VBox(18);
        formulaire.setAlignment(Pos.CENTER);
        formulaire.setPadding(new Insets(50, 60, 50, 60));
        formulaire.setStyle("-fx-background-color: #f5f5f5;");

        Label titreForm = new Label("Bienvenue !");
        titreForm.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        titreForm.setTextFill(Color.web("#1a237e"));

        Label sousTitreForm = new Label("Connectez-vous à votre espace");
        sousTitreForm.setFont(Font.font("Arial", 14));
        sousTitreForm.setTextFill(Color.web("#757575"));

        Label lblEmail = new Label("Adresse email");
        lblEmail.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        emailField = new TextField();
        emailField.setPromptText("exemple@univ.sn");
        emailField.setPrefHeight(42);
        emailField.setStyle(styleChamp());

        Label lblMdp = new Label("Mot de passe");
        lblMdp.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        passwordField = new PasswordField();
        passwordField.setPromptText("Votre mot de passe");
        passwordField.setPrefHeight(42);
        passwordField.setStyle(styleChamp());
        passwordField.setOnAction(e -> tenterConnexion(stage));

        messageErreur = new Label("");
        messageErreur.setTextFill(Color.web("#e53935"));
        messageErreur.setVisible(false);

        Button btnConnexion = new Button("Se connecter");
        btnConnexion.setPrefWidth(Double.MAX_VALUE);
        btnConnexion.setPrefHeight(44);
        btnConnexion.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        btnConnexion.setStyle(styleBouton());
        btnConnexion.setOnAction(e -> tenterConnexion(stage));
        btnConnexion.setOnMouseEntered(e -> btnConnexion.setStyle(styleBoutonHover()));
        btnConnexion.setOnMouseExited(e  -> btnConnexion.setStyle(styleBouton()));

        TitledPane comptesTest = construireComptesTest();

        // Bouton inscription
        Separator sepBas = new Separator();

        Label lblOu = new Label("Pas encore de compte ?");
        lblOu.setFont(Font.font("Arial", 12));
        lblOu.setTextFill(Color.web("#757575"));

        Button btnInscrire = new Button("📝  Créer un compte avec un code d'activation");
        btnInscrire.setMaxWidth(Double.MAX_VALUE);
        btnInscrire.setStyle(
            "-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32;"
          + "-fx-background-radius: 8; -fx-cursor: hand;"
          + "-fx-font-weight: bold; -fx-font-size: 12;");
        btnInscrire.setOnAction(e -> {
            try { new InscriptionView().start(stage); }
            catch (Exception ex) { ex.printStackTrace(); }
        });
        btnInscrire.setOnMouseEntered(e -> btnInscrire.setStyle(
            "-fx-background-color: #c8e6c9; -fx-text-fill: #1b5e20;"
          + "-fx-background-radius: 8; -fx-cursor: hand;"
          + "-fx-font-weight: bold; -fx-font-size: 12;"));
        btnInscrire.setOnMouseExited(e -> btnInscrire.setStyle(
            "-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32;"
          + "-fx-background-radius: 8; -fx-cursor: hand;"
          + "-fx-font-weight: bold; -fx-font-size: 12;"));

        formulaire.getChildren().addAll(
            titreForm, sousTitreForm, new Separator(),
            lblEmail, emailField,
            lblMdp, passwordField,
            messageErreur, btnConnexion,
            sepBas, lblOu, btnInscrire
        );
        return formulaire;
    }

    private TitledPane construireComptesTest() {
        VBox contenu = new VBox(6);
        contenu.setPadding(new Insets(10));

        String[][] comptes = {
            {"Admin",        "admin@univ.sn",   "admin123"},
            {"Gestionnaire", "gest@univ.sn",    "gest123"},
            {"Enseignant",   "m.diallo@univ.sn","pass123"},
            {"Étudiant",     "f.ndiaye@etu.sn", "pass456"},
        };

        for (String[] c : comptes) {
            HBox ligne = new HBox(10);
            ligne.setAlignment(Pos.CENTER_LEFT);
            ligne.setStyle("-fx-cursor:hand;-fx-padding:4 6 4 6;-fx-background-radius:4;");

            Label role  = new Label(c[0]);
            role.setMinWidth(90);
            role.setFont(Font.font("Arial", FontWeight.BOLD, 11));
            role.setTextFill(Color.web("#1a237e"));

            Label email = new Label(c[1]);
            email.setFont(Font.font("Monospace", 11));

            Label mdp = new Label("/ " + c[2]);
            mdp.setFont(Font.font("Monospace", 11));
            mdp.setTextFill(Color.web("#757575"));

            ligne.setOnMouseClicked(e -> {
                emailField.setText(c[1]);
                passwordField.setText(c[2]);
            });
            ligne.setOnMouseEntered(e -> ligne.setStyle(
                "-fx-cursor:hand;-fx-background-color:#e8eaf6;"
              + "-fx-padding:4 6 4 6;-fx-background-radius:4;"));
            ligne.setOnMouseExited(e  -> ligne.setStyle(
                "-fx-cursor:hand;-fx-padding:4 6 4 6;-fx-background-radius:4;"));

            ligne.getChildren().addAll(role, email, mdp);
            contenu.getChildren().add(ligne);
        }

        TitledPane pane = new TitledPane("💡 Comptes de test (cliquer pour remplir)", contenu);
        pane.setExpanded(false);
        return pane;
    }

    // ════════════════════════════════════════════════════════════
    //  LOGIQUE CONNEXION
    // ════════════════════════════════════════════════════════════

    private void tenterConnexion(Stage stage) {
        String email = emailField.getText().trim();
        String mdp   = passwordField.getText();

        if (email.isEmpty() || mdp.isEmpty()) {
            afficherErreur("Veuillez remplir tous les champs.");
            return;
        }

        // Authentification via MySQL
        Utilisateur utilisateur = authService.connecter(email, mdp, utilisateurs);

        if (utilisateur != null) {
            messageErreur.setVisible(false);
            emailField.setStyle(styleChamp());
            passwordField.setStyle(styleChamp());

            // Ouvrir le Dashboard
            try {
                new DashboardView(utilisateur).start(stage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            afficherErreur("Email ou mot de passe incorrect.");
            passwordField.clear();
            animer(passwordField);
        }
    }

    private void afficherErreur(String msg) {
        messageErreur.setText("⚠  " + msg);
        messageErreur.setVisible(true);
        emailField.setStyle(styleChampErreur());
        passwordField.setStyle(styleChampErreur());
    }

    private void animer(javafx.scene.Node node) {
        javafx.animation.TranslateTransition tt =
            new javafx.animation.TranslateTransition(
                javafx.util.Duration.millis(60), node);
        tt.setByX(8); tt.setCycleCount(6); tt.setAutoReverse(true);
        tt.play();
    }

    // ════════════════════════════════════════════════════════════
    //  STYLES
    // ════════════════════════════════════════════════════════════

    private String styleChamp() {
        return "-fx-background-color:white;-fx-border-color:#bdbdbd;"
             + "-fx-border-radius:8;-fx-background-radius:8;-fx-padding:8 12 8 12;";
    }
    private String styleChampErreur() {
        return "-fx-background-color:#fff8f8;-fx-border-color:#e53935;"
             + "-fx-border-radius:8;-fx-background-radius:8;-fx-padding:8 12 8 12;";
    }
    private String styleBouton() {
        return "-fx-background-color:#1a237e;-fx-text-fill:white;"
             + "-fx-background-radius:8;-fx-cursor:hand;";
    }
    private String styleBoutonHover() {
        return "-fx-background-color:#283593;-fx-text-fill:white;"
             + "-fx-background-radius:8;-fx-cursor:hand;";
    }

    private Label badge(String texte, String couleur) {
        Label b = new Label(texte);
        b.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        b.setTextFill(Color.WHITE);
        b.setPadding(new Insets(3, 8, 3, 8));
        b.setStyle("-fx-background-color:" + couleur + ";-fx-background-radius:20;");
        return b;
    }

    public static void main(String[] args) { launch(args); }
}
