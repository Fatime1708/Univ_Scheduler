package com.univscheduler.view;

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
import javafx.stage.Stage;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Dashboard principal de UNIV-SCHEDULER.
 * Affiché après la connexion. Contient :
 *  - Barre de navigation à gauche
 *  - Statistiques en haut
 *  - Contenu central (change selon le menu cliqué)
 */
public class DashboardView  {

    // ── Données ──────────────────────────────────────────────────
    private Utilisateur utilisateurConnecte;
    private List<Salle> salles       = new ArrayList<>();
    private List<Cours> cours        = new ArrayList<>();
    private List<Conflit> conflits   = new ArrayList<>();

    // ── Composants principaux ────────────────────────────────────
    private VBox contenuCentral;  // Zone qui change quand on clique le menu
    private Label labelPageActive;

    // ── Constructeur (appelé depuis LoginView) ───────────────────
    public DashboardView(Utilisateur utilisateur) {
        this.utilisateurConnecte = utilisateur;
        chargerDonneesTest();
    }

    // Constructeur sans argument (pour lancer directement)
    public DashboardView() {
        this.utilisateurConnecte = new Administrateur(
                "Admin", "Super", "admin@univ.sn", "admin123");
        chargerDonneesTest();
    }

    // ── Lancement JavaFX ─────────────────────────────────────────
    
    public void start(Stage stage) {
        BorderPane root = new BorderPane();

        // Barre du haut
        root.setTop(construireTopBar());

        // Menu gauche
        root.setLeft(construireMenuGauche(stage));

        // Contenu central (démarre sur le Dashboard)
        contenuCentral = new VBox(20);
        contenuCentral.setPadding(new Insets(30));
        contenuCentral.setStyle("-fx-background-color: #f0f2f5;");

        ScrollPane scroll = new ScrollPane(contenuCentral);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #f0f2f5; -fx-background: #f0f2f5;");
        root.setCenter(scroll);

        // Afficher la page d'accueil par défaut
        afficherAccueil();

        Scene scene = new Scene(root, 1100, 680);
        stage.setTitle("UNIV-SCHEDULER — Dashboard");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.show();
    }

    // ════════════════════════════════════════════════════════════
    //  BARRE DU HAUT
    // ════════════════════════════════════════════════════════════

    private HBox construireTopBar() {
        HBox topBar = new HBox();
        topBar.setPadding(new Insets(14, 24, 14, 24));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: #1a237e;"
                      + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 6, 0, 0, 2);");

        // Logo + nom app
        Label logo = new Label("🎓  UNIV-SCHEDULER");
        logo.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        logo.setTextFill(Color.WHITE);

        // Espace flexible entre logo et infos utilisateur
        Region espace = new Region();
        HBox.setHgrow(espace, Priority.ALWAYS);

        // Infos utilisateur connecté
        VBox infoUser = new VBox(2);
        infoUser.setAlignment(Pos.CENTER_RIGHT);

        Label nomUser = new Label(utilisateurConnecte.getNomComplet());
        nomUser.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        nomUser.setTextFill(Color.WHITE);

        Label roleUser = new Label(utilisateurConnecte.getRole());
        roleUser.setFont(Font.font("Arial", 11));
        roleUser.setTextFill(Color.web("#b0bec5"));

        infoUser.getChildren().addAll(nomUser, roleUser);

        // Bouton déconnexion
        Button btnDeco = new Button("⏻  Déconnexion");
        btnDeco.setStyle("-fx-background-color: rgba(255,255,255,0.15);"
                       + "-fx-text-fill: white;"
                       + "-fx-background-radius: 6;"
                       + "-fx-cursor: hand;"
                       + "-fx-font-size: 12;");
        btnDeco.setOnMouseEntered(e ->
            btnDeco.setStyle("-fx-background-color: rgba(255,255,255,0.25);"
                           + "-fx-text-fill: white;"
                           + "-fx-background-radius: 6;"
                           + "-fx-cursor: hand;"
                           + "-fx-font-size: 12;"));
        btnDeco.setOnMouseExited(e ->
            btnDeco.setStyle("-fx-background-color: rgba(255,255,255,0.15);"
                           + "-fx-text-fill: white;"
                           + "-fx-background-radius: 6;"
                           + "-fx-cursor: hand;"
                           + "-fx-font-size: 12;"));
        btnDeco.setOnAction(e -> deconnecter(btnDeco));

        HBox.setMargin(btnDeco, new Insets(0, 0, 0, 20));

        topBar.getChildren().addAll(logo, espace, infoUser, btnDeco);
        return topBar;
    }

    // ════════════════════════════════════════════════════════════
    //  MENU GAUCHE
    // ════════════════════════════════════════════════════════════

    private VBox construireMenuGauche(Stage stage) {
        VBox menu = new VBox(4);
        menu.setPrefWidth(220);
        menu.setPadding(new Insets(20, 12, 20, 12));
        menu.setStyle("-fx-background-color: #1e2a5e;");

        Label labelMenu = new Label("NAVIGATION");
        labelMenu.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        labelMenu.setTextFill(Color.web("#546e9e"));
        labelMenu.setPadding(new Insets(0, 0, 10, 8));

        // Items du menu
        Button[] boutons = {
            creerBoutonMenu("🏠", "Accueil",          true),
            creerBoutonMenu("🏫", "Salles",           false),
            creerBoutonMenu("📅", "Emploi du temps",  false),
            creerBoutonMenu("👥", "Utilisateurs",     false),
            creerBoutonMenu("📊", "Rapports",         false),
            creerBoutonMenu("🔑", "Codes activation", false),
        };

        // Actions des boutons
        boutons[0].setOnAction(e -> { activerBouton(boutons, 0); afficherAccueil(); });
        boutons[1].setOnAction(e -> { activerBouton(boutons, 1); afficherSalles(); });
        boutons[2].setOnAction(e -> { activerBouton(boutons, 2); afficherEmploiDuTemps(); });
        boutons[3].setOnAction(e -> { activerBouton(boutons, 3); afficherUtilisateurs(); });
        boutons[4].setOnAction(e -> { activerBouton(boutons, 4); afficherRapports(); });
        boutons[5].setOnAction(e -> { activerBouton(boutons, 5); afficherCodesActivation(); });

        // Masquer certains menus selon le rôle
        if (utilisateurConnecte.getRole().equals("ETUDIANT")) {
            boutons[3].setVisible(false); boutons[3].setManaged(false);
            boutons[4].setVisible(false); boutons[4].setManaged(false);
            boutons[5].setVisible(false); boutons[5].setManaged(false);
        }
        if (utilisateurConnecte.getRole().equals("ENSEIGNANT")) {
            boutons[3].setVisible(false); boutons[3].setManaged(false);
            boutons[5].setVisible(false); boutons[5].setManaged(false);
        }
        if (utilisateurConnecte.getRole().equals("GESTIONNAIRE")) {
            boutons[5].setVisible(false); boutons[5].setManaged(false);
        }

        menu.getChildren().add(labelMenu);
        for (Button b : boutons) menu.getChildren().add(b);

        // Espace flexible
        Region espace = new Region();
        VBox.setVgrow(espace, Priority.ALWAYS);
        menu.getChildren().add(espace);

        // Version en bas
        Label version = new Label("v1.0.0 — L2 Info 2026");
        version.setFont(Font.font("Arial", 10));
        version.setTextFill(Color.web("#546e9e"));
        version.setPadding(new Insets(0, 0, 0, 8));
        menu.getChildren().add(version);

        return menu;
    }

    // ════════════════════════════════════════════════════════════
    //  PAGES DU CONTENU CENTRAL
    // ════════════════════════════════════════════════════════════

    /** Page d'accueil avec les statistiques */
    private void afficherAccueil() {
        contenuCentral.getChildren().clear();

        // Titre de bienvenue
        Label titre = new Label("Bonjour, " + utilisateurConnecte.getPrenom() + " 👋");
        titre.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titre.setTextFill(Color.web("#1a237e"));

        Label sousTitre = new Label("Voici un résumé de la situation aujourd'hui");
        sousTitre.setFont(Font.font("Arial", 14));
        sousTitre.setTextFill(Color.web("#757575"));

        // ── Cartes de statistiques ───────────────────────────────
        long sallesLibres   = salles.stream().filter(Salle::isDisponible).count();
        long sallesOccupees = salles.size() - sallesLibres;

        HBox cartes = new HBox(16);
        cartes.getChildren().addAll(
            creerCarteStat("🏫", "Salles totales",    String.valueOf(salles.size()),  "#1a237e", "#e8eaf6"),
            creerCarteStat("✅", "Salles disponibles", String.valueOf(sallesLibres),   "#1b5e20", "#e8f5e9"),
            creerCarteStat("🔴", "Salles occupées",   String.valueOf(sallesOccupees), "#b71c1c", "#ffebee"),
            creerCarteStat("📚", "Cours planifiés",   String.valueOf(cours.size()),   "#e65100", "#fff3e0"),
            creerCarteStat("⚠️",  "Conflits détectés", String.valueOf(conflits.size()),"#4a148c", "#f3e5f5")
        );

        // ── Tableau des conflits récents ─────────────────────────
        VBox sectionConflits = new VBox(10);
        Label titreConflits = new Label("⚠️  Conflits détectés");
        titreConflits.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        titreConflits.setTextFill(Color.web("#b71c1c"));

        if (conflits.isEmpty()) {
            Label aucun = new Label("✅  Aucun conflit détecté — tout est en ordre !");
            aucun.setFont(Font.font("Arial", 13));
            aucun.setTextFill(Color.web("#388e3c"));
            aucun.setPadding(new Insets(16));
            aucun.setStyle("-fx-background-color: #e8f5e9; -fx-background-radius: 8;");
            sectionConflits.getChildren().addAll(titreConflits, aucun);
        } else {
            sectionConflits.getChildren().add(titreConflits);
            for (Conflit c : conflits) {
                Label ligne = new Label("•  " + c.getDescription());
                ligne.setFont(Font.font("Arial", 12));
                ligne.setTextFill(Color.web("#424242"));
                ligne.setPadding(new Insets(10, 16, 10, 16));
                ligne.setMaxWidth(Double.MAX_VALUE);
                ligne.setStyle("-fx-background-color: #fff3e0;"
                             + "-fx-background-radius: 8;"
                             + "-fx-border-color: #ffcc02;"
                             + "-fx-border-radius: 8;"
                             + "-fx-border-width: 0 0 0 4;");
                sectionConflits.getChildren().add(ligne);
            }
        }

        // ── Liste des cours du jour ──────────────────────────────
        VBox sectionCours = new VBox(10);
        Label titreCours = new Label("📅  Cours d'aujourd'hui");
        titreCours.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        titreCours.setTextFill(Color.web("#1a237e"));

        sectionCours.getChildren().add(titreCours);

        String[] couleurs = {"#e8eaf6", "#e8f5e9", "#fff3e0", "#fce4ec", "#e0f7fa"};
        String[] bordures = {"#3949ab", "#43a047", "#fb8c00", "#e91e63", "#00acc1"};
        int i = 0;
        for (Cours c : cours) {
            HBox ligneC = new HBox(16);
            ligneC.setPadding(new Insets(12, 16, 12, 16));
            ligneC.setAlignment(Pos.CENTER_LEFT);
            ligneC.setStyle("-fx-background-color: " + couleurs[i % couleurs.length] + ";"
                          + "-fx-background-radius: 8;"
                          + "-fx-border-color: " + bordures[i % bordures.length] + ";"
                          + "-fx-border-radius: 8;"
                          + "-fx-border-width: 0 0 0 4;");

            Label mat   = new Label(c.getMatiere());
            mat.setFont(Font.font("Arial", FontWeight.BOLD, 13));
            mat.setMinWidth(150);

            Label salle = new Label("📍 " + c.getSalle().getNumero());
            salle.setFont(Font.font("Arial", 12));
            salle.setMinWidth(80);

            Label heure = new Label("🕐 " + c.getCreneau());
            heure.setFont(Font.font("Arial", 12));
            heure.setMinWidth(160);

            Label ens = new Label("👤 " + c.getEnseignant().getNomComplet());
            ens.setFont(Font.font("Arial", 12));

            ligneC.getChildren().addAll(mat, salle, heure, ens);
            sectionCours.getChildren().add(ligneC);
            i++;
        }

        contenuCentral.getChildren().addAll(
                titre, sousTitre, cartes, sectionConflits, sectionCours);
    }

    /** Page Salles — intègre SallesView */
    private void afficherSalles() {
        contenuCentral.getChildren().clear();
        SallesView sallesView = new SallesView();
        contenuCentral.getChildren().add(sallesView.getVue());
    }

    /** Page Emploi du temps — intègre EmploiDuTempsView */
    private void afficherEmploiDuTemps() {
        contenuCentral.getChildren().clear();
        EmploiDuTempsView edtView = new EmploiDuTempsView();
        contenuCentral.getChildren().add(edtView.getVue());
    }

    /** Page Utilisateurs */
    private void afficherUtilisateurs() {
        contenuCentral.getChildren().clear();
        // Vérifier que c'est bien un Admin
        if (!utilisateurConnecte.getRole().equals("ADMIN")) {
            Label interdit = new Label("⛔  Accès refusé — Administrateur uniquement.");
            interdit.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            interdit.setTextFill(Color.web("#c62828"));
            contenuCentral.getChildren().add(interdit);
            return;
        }
        UtilisateursView utilisateursView = new UtilisateursView();
        contenuCentral.getChildren().add(utilisateursView.getVue());
    }

    /** Page Rapports — intègre RapportsView */
    private void afficherRapports() {
        contenuCentral.getChildren().clear();
        if (utilisateurConnecte.getRole().equals("ETUDIANT")) {
            contenuCentral.getChildren().add(pageAccesRefuse()); return;
        }
        RapportsView rapportsView = new RapportsView(utilisateurConnecte);
        contenuCentral.getChildren().add(rapportsView.getVue());
    }

    /** Page Codes d'activation — Admin seulement */
    private void afficherCodesActivation() {
        contenuCentral.getChildren().clear();
        if (!utilisateurConnecte.getRole().equals("ADMIN")) {
            contenuCentral.getChildren().add(pageAccesRefuse()); return;
        }
        CodesActivationView codesView = new CodesActivationView(utilisateurConnecte);
        contenuCentral.getChildren().add(codesView.getVue());
    }

    /** Page d'accès refusé */
    private VBox pageAccesRefuse() {
        VBox page = new VBox(20);
        page.setAlignment(Pos.CENTER);
        page.setPadding(new Insets(60));

        Label icone = new Label("⛔");
        icone.setFont(Font.font(60));

        Label titre = new Label("Accès refusé");
        titre.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        titre.setTextFill(Color.web("#c62828"));

        Label msg = new Label("Vous n'avez pas les permissions\nnécessaires pour accéder à cette page.");
        msg.setFont(Font.font("Arial", 15));
        msg.setTextFill(Color.web("#757575"));
        msg.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        Label role = new Label("Votre rôle : " + utilisateurConnecte.getRole());
        role.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        role.setPadding(new Insets(8, 20, 8, 20));
        role.setStyle("-fx-background-color: #ffebee;"
                    + "-fx-background-radius: 20;"
                    + "-fx-text-fill: #c62828;");

        page.getChildren().addAll(icone, titre, msg, role);
        return page;
    }

    // ════════════════════════════════════════════════════════════
    //  DÉCONNEXION
    // ════════════════════════════════════════════════════════════

    private void deconnecter(Button btn) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Déconnexion");
        confirm.setHeaderText("Voulez-vous vous déconnecter ?");
        confirm.setContentText("Vous serez redirigé vers la page de connexion.");
        confirm.showAndWait().ifPresent(reponse -> {
            if (reponse == ButtonType.OK) {
                Stage stage = (Stage) btn.getScene().getWindow();
                try {
                    new LoginView().start(stage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // ════════════════════════════════════════════════════════════
    //  COMPOSANTS RÉUTILISABLES
    // ════════════════════════════════════════════════════════════

    /** Crée une carte de statistique colorée */
    private VBox creerCarteStat(String icone, String label, String valeur,
                                 String couleurTexte, String couleurFond) {
        VBox carte = new VBox(6);
        carte.setPadding(new Insets(18, 24, 18, 24));
        carte.setAlignment(Pos.CENTER_LEFT);
        carte.setPrefWidth(180);
        carte.setStyle("-fx-background-color: " + couleurFond + ";"
                     + "-fx-background-radius: 12;"
                     + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 6, 0, 0, 2);");

        Label ico = new Label(icone);
        ico.setFont(Font.font(22));

        Label val = new Label(valeur);
        val.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 28));
        val.setTextFill(Color.web(couleurTexte));

        Label lbl = new Label(label);
        lbl.setFont(Font.font("Arial", 12));
        lbl.setTextFill(Color.web("#616161"));

        carte.getChildren().addAll(ico, val, lbl);
        return carte;
    }

    /** Crée un bouton du menu gauche */
    private Button creerBoutonMenu(String icone, String texte, boolean actif) {
        Button btn = new Button(icone + "   " + texte);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(11, 16, 11, 16));
        btn.setFont(Font.font("Arial", actif ? FontWeight.BOLD : FontWeight.NORMAL, 13));
        btn.setStyle(actif ? styleBoutonActif() : styleBoutonInactif());

        btn.setOnMouseEntered(e -> {
            if (!btn.getStyle().contains("1565c0"))
                btn.setStyle(styleBoutonHover());
        });
        btn.setOnMouseExited(e -> {
            if (!btn.getStyle().contains("1565c0"))
                btn.setStyle(styleBoutonInactif());
        });

        return btn;
    }

    /** Met en surbrillance le bouton actif du menu */
    private void activerBouton(Button[] boutons, int indexActif) {
        for (int i = 0; i < boutons.length; i++) {
            if (i == indexActif) {
                boutons[i].setStyle(styleBoutonActif());
                boutons[i].setFont(Font.font("Arial", FontWeight.BOLD, 13));
            } else {
                boutons[i].setStyle(styleBoutonInactif());
                boutons[i].setFont(Font.font("Arial", FontWeight.NORMAL, 13));
            }
        }
    }

    // ════════════════════════════════════════════════════════════
    //  STYLES
    // ════════════════════════════════════════════════════════════

    private String styleBoutonActif() {
        return "-fx-background-color: #1565c0;"
             + "-fx-text-fill: white;"
             + "-fx-background-radius: 8;"
             + "-fx-cursor: hand;";
    }

    private String styleBoutonInactif() {
        return "-fx-background-color: transparent;"
             + "-fx-text-fill: #90caf9;"
             + "-fx-background-radius: 8;"
             + "-fx-cursor: hand;";
    }

    private String styleBoutonHover() {
        return "-fx-background-color: rgba(255,255,255,0.1);"
             + "-fx-text-fill: white;"
             + "-fx-background-radius: 8;"
             + "-fx-cursor: hand;";
    }

    // ════════════════════════════════════════════════════════════
    //  DONNÉES DE TEST
    // ════════════════════════════════════════════════════════════

    private void chargerDonneesTest() {
        // Salles
        Salle s1 = new Salle("A101", 40, TypeSalle.TD, 1); s1.setId(1);
        Salle s2 = new Salle("A102", 30, TypeSalle.TP, 1); s2.setId(2); s2.setDisponible(false);
        Salle s3 = new Salle("B201", 150, TypeSalle.AMPHI, 2); s3.setId(3);
        Salle s4 = new Salle("B202", 35, TypeSalle.TD, 2); s4.setId(4); s4.setDisponible(false);
        salles.addAll(List.of(s1, s2, s3, s4));

        // Enseignants
        Enseignant prof1 = new Enseignant("Diallo", "Moussa", "m.diallo@univ.sn",
                "pass123", "Informatique", "UFR Sciences");
        prof1.setId(1);

        Enseignant prof2 = new Enseignant("Seck", "Aminata", "a.seck@univ.sn",
                "pass789", "Mathématiques", "UFR Sciences");
        prof2.setId(2);

        // Créneaux
        Creneau c1 = new Creneau("Lundi",  LocalTime.of(8,  0), 120); c1.setId(1);
        Creneau c2 = new Creneau("Lundi",  LocalTime.of(10, 0), 90);  c2.setId(2);
        Creneau c3 = new Creneau("Mardi",  LocalTime.of(14, 0), 120); c3.setId(3);

        // Cours
        Cours cours1 = new Cours("Algorithmique", "L2 Info", "Groupe A", prof1, s1, c1); cours1.setId(1);
        Cours cours2 = new Cours("Mathématiques", "L2 Info", "Groupe A", prof2, s3, c2); cours2.setId(2);
        Cours cours3 = new Cours("Réseaux",        "L2 Info", "Groupe B", prof1, s1, c3); cours3.setId(3);
        cours.addAll(List.of(cours1, cours2, cours3));
    }

    // ── Lancement direct ─────────────────────────────────────────
   
}
