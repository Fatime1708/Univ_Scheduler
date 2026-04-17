package com.univscheduler.view;

import com.univscheduler.model.*;
import com.univscheduler.dao.NotificationDAO;
import com.univscheduler.dao.EquipementDAO;
import java.util.List;
import com.univscheduler.model.enums.TypeSalle;
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

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;


public class DashboardView extends Application {

    // ── Données ──────────────────────────────────────────────────
	public static Utilisateur utilisateurConnecte;
    private List<Salle> salles       = new ArrayList<>();
    private List<Cours> cours        = new ArrayList<>();
    private List<Conflit> conflits   = new ArrayList<>();

    // ── Composants principaux ────────────────────────────────────
    private VBox contenuCentral;  // Zone qui change quand on clique le menu


    // ── Constructeur (appelé depuis LoginView) ───────────────────
    public DashboardView(Utilisateur utilisateur) {
    	DashboardView.utilisateurConnecte = utilisateur;
        chargerDonneesTest();
    }

    // Constructeur sans argument (pour lancer directement)
    public DashboardView() {
    	DashboardView.utilisateurConnecte  = new Administrateur(
                "Admin", "Super", "admin@univ.sn", "admin123");
        chargerDonneesTest();
    }

    // ── Lancement JavaFX ─────────────────────────────────────────
    @Override
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

        Label logo = new Label("🎓  UNIV-SCHEDULER");
        logo.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        logo.setTextFill(Color.WHITE);

        Region espace = new Region();
        HBox.setHgrow(espace, Priority.ALWAYS);

        VBox infoUser = new VBox(2);
        infoUser.setAlignment(Pos.CENTER_RIGHT);

        Label nomUser = new Label(utilisateurConnecte.getNomComplet());
        nomUser.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        nomUser.setTextFill(Color.WHITE);

        Label roleUser = new Label(utilisateurConnecte.getRole());
        roleUser.setFont(Font.font("Arial", 11));
        roleUser.setTextFill(Color.web("#b0bec5"));

        infoUser.getChildren().addAll(nomUser, roleUser);

        // ── Bouton cloche 🔔 ─────────────────────────────────────────
        NotificationDAO notifDAO = new NotificationDAO();
        int nbNonLues = notifDAO.compterNonLues(
                utilisateurConnecte.getId(),
                utilisateurConnecte.getRole());

        StackPane cloche = new StackPane();

        Button btnCloche = new Button("🔔");
        btnCloche.setStyle("-fx-background-color: transparent;"
                         + "-fx-text-fill: white;"
                         + "-fx-font-size: 18;"
                         + "-fx-cursor: hand;");

        // Badge rouge avec le nombre
     // Badge rouge avec le nombre
        Label badge = new Label(nbNonLues > 0 ? String.valueOf(nbNonLues) : "");
        badge.setFont(Font.font("Arial", FontWeight.BOLD, 9));
        badge.setTextFill(Color.WHITE);
        badge.setStyle("-fx-background-color: #e53935;"
                     + "-fx-background-radius: 10;"
                     + "-fx-padding: 1 4 1 4;");
        badge.setVisible(nbNonLues > 0);
        StackPane.setAlignment(badge, Pos.TOP_RIGHT);

        // ✅ Point rouge si notifications non lues
        if (nbNonLues > 0) {
            Label point = new Label("●");
            point.setTextFill(Color.web("#ff1744")); // ✅ rouge vif
            point.setFont(Font.font(12));
            point.setStyle("-fx-effect: dropshadow(gaussian, #ff1744, 6, 0.8, 0, 0);");
            StackPane.setAlignment(point, Pos.TOP_RIGHT);
            cloche.getChildren().addAll(btnCloche, point, badge);
        } else {
            cloche.getChildren().addAll(btnCloche, badge);
        }

        // Popup notifications au clic
        btnCloche.setOnAction(e -> afficherPopupNotifications(btnCloche, notifDAO, badge));

        // Bouton déconnexion
        Button btnDeco = new Button("⏻  Déconnexion");
        btnDeco.setStyle("-fx-background-color: rgba(255,255,255,0.15);"
                       + "-fx-text-fill: white;"
                       + "-fx-background-radius: 6;"
                       + "-fx-cursor: hand;"
                       + "-fx-font-size: 12;");
        btnDeco.setOnMouseEntered(e -> btnDeco.setStyle(
            "-fx-background-color: rgba(255,255,255,0.25);"
          + "-fx-text-fill: white;-fx-background-radius: 6;-fx-cursor: hand;-fx-font-size: 12;"));
        btnDeco.setOnMouseExited(e -> btnDeco.setStyle(
            "-fx-background-color: rgba(255,255,255,0.15);"
          + "-fx-text-fill: white;-fx-background-radius: 6;-fx-cursor: hand;-fx-font-size: 12;"));
        btnDeco.setOnAction(e -> deconnecter(btnDeco));

        HBox.setMargin(cloche, new Insets(0, 10, 0, 20));
        HBox.setMargin(btnDeco, new Insets(0, 0, 0, 10));

        topBar.getChildren().addAll(logo, espace, infoUser, cloche, btnDeco);
        return topBar;
    }

    // ── Popup des notifications ──────────────────────────────────────
    private void afficherPopupNotifications(Button btnCloche,
            NotificationDAO notifDAO, Label badge) {

        javafx.stage.Popup popup = new javafx.stage.Popup();
        popup.setAutoHide(true);

        VBox contenu = new VBox(0);
        contenu.setStyle("-fx-background-color: white;"
                       + "-fx-border-color: #e0e0e0;"
                       + "-fx-border-radius: 10;"
                       + "-fx-background-radius: 10;"
                       + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 4);");
        contenu.setPrefWidth(340);

        // En-tête
        HBox entete = new HBox();
        entete.setPadding(new Insets(14, 16, 14, 16));
        entete.setAlignment(Pos.CENTER_LEFT);
        entete.setStyle("-fx-background-color: #1a237e;"
                      + "-fx-background-radius: 10 10 0 0;");

        Label lblTitre = new Label("🔔  Notifications");
        lblTitre.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        lblTitre.setTextFill(Color.WHITE);

        Region esp = new Region();
        HBox.setHgrow(esp, Priority.ALWAYS);

        Button btnToutLire = new Button("Tout lire");
        btnToutLire.setStyle("-fx-background-color: rgba(255,255,255,0.2);"
                           + "-fx-text-fill: white;"
                           + "-fx-background-radius: 6;"
                           + "-fx-cursor: hand;"
                           + "-fx-font-size: 10;");

        entete.getChildren().addAll(lblTitre, esp, btnToutLire);

        // Liste des notifications
        VBox listeNotifs = new VBox(0);
        ScrollPane scroll = new ScrollPane(listeNotifs);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(300);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        List<String[]> notifs = notifDAO.getParUtilisateur(
                utilisateurConnecte.getId(),
                utilisateurConnecte.getRole());

        if (notifs.isEmpty()) {
            Label aucune = new Label("✅  Aucune notification");
            aucune.setFont(Font.font("Arial", 13));
            aucune.setTextFill(Color.GRAY);
            aucune.setPadding(new Insets(20));
            listeNotifs.getChildren().add(aucune);
        } else {
            for (String[] n : notifs) {
                listeNotifs.getChildren().add(
                        creerLigneNotification(n, notifDAO, badge, listeNotifs));
            }
        }

        // Tout marquer comme lu
        btnToutLire.setOnAction(e -> {
            notifDAO.marquerToutesLues(
                    utilisateurConnecte.getId(),
                    utilisateurConnecte.getRole());
            badge.setVisible(false);
            badge.setText("");
            // Rafraîchir la liste
            listeNotifs.getChildren().clear();
            List<String[]> notifsMAJ = notifDAO.getParUtilisateur(
                    utilisateurConnecte.getId(),
                    utilisateurConnecte.getRole());
            for (String[] n : notifsMAJ) {
                listeNotifs.getChildren().add(
                        creerLigneNotification(n, notifDAO, badge, listeNotifs));
            }
        });

        contenu.getChildren().addAll(entete, scroll);
        popup.getContent().add(contenu);

        // Positionner sous la cloche
        javafx.geometry.Bounds bounds = btnCloche.localToScreen(
                btnCloche.getBoundsInLocal());
        popup.show(btnCloche.getScene().getWindow(),
                bounds.getMaxX() - 340,
                bounds.getMaxY() + 8);
    }

    private VBox creerLigneNotification(String[] n, NotificationDAO notifDAO,
            Label badge, VBox listeNotifs) {
        boolean lue = "1".equals(n[4]);

        VBox ligne = new VBox(4);
        ligne.setPadding(new Insets(12, 16, 12, 16));
        
        // ✅ Fond blanc pur / blanc cassé — plus de bleu clair
        ligne.setStyle("-fx-background-color: " + (lue ? "#1a237e" : "#283593") + ";"
                + "-fx-border-color: #3949ab;"
                + "-fx-border-width: 0 0 1 0;"
                + "-fx-cursor: hand;");

        String icone = switch (n[3]) {
            case "CONFLIT"     -> "⚠️";
            case "RESERVATION" -> "📅";
            case "SIGNALEMENT" -> "🔧";
            case "SALLE"       -> "🏫";
            case "COURS"       -> "📚";
            default            -> "🔔";
        };

        HBox haut = new HBox(8);
        haut.setAlignment(Pos.CENTER_LEFT);

        Label lblIcone = new Label(icone);
        lblIcone.setFont(Font.font(14));

        // ✅ Titre en noir gras
        Label lblTitre = new Label(n[1]);
        lblTitre.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        lblTitre.setTextFill(Color.WHITE);
        if (!lue) {
            Label dot = new Label("●");
            dot.setTextFill(Color.web("#e53935"));
            dot.setFont(Font.font(8));
            haut.getChildren().addAll(lblIcone, lblTitre, dot);
        } else {
            haut.getChildren().addAll(lblIcone, lblTitre);
        }

        // ✅ Message en noir foncé, bien lisible
        Label lblMsg = new Label(n[2]);
        lblMsg.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        lblMsg.setTextFill(Color.WHITE);
        lblMsg.setWrapText(true);
        // ✅ Date en gris foncé

        Label lblDate = new Label("🕐 " + n[5]);
        lblDate.setFont(Font.font("Arial", 10));
        lblDate.setTextFill(Color.web("#b0bec5"));

        ligne.getChildren().addAll(haut, lblMsg, lblDate);

        // Clic → marquer comme lue
        ligne.setOnMouseClicked(e -> {
            if (!lue) {
                notifDAO.marquerLue(Integer.parseInt(n[0]));
                ligne.setStyle("-fx-background-color: #f9f9f9;"
                             + "-fx-border-color: #dddddd;"
                             + "-fx-border-width: 0 0 1 0;");
                lblTitre.setFont(Font.font("Arial", FontWeight.NORMAL, 13));
                int nb = notifDAO.compterNonLues(
                        utilisateurConnecte.getId(),
                        utilisateurConnecte.getRole());
                badge.setText(nb > 0 ? String.valueOf(nb) : "");
                badge.setVisible(nb > 0);
            }
        });

        ligne.setOnMouseEntered(e -> ligne.setStyle(
        	    "-fx-background-color: #3949ab;"
        	  + "-fx-border-color: #3949ab;"
        	  + "-fx-border-width: 0 0 1 0;"
        	  + "-fx-cursor: hand;"));
        	ligne.setOnMouseExited(e -> ligne.setStyle(
        	    "-fx-background-color: " + (lue ? "#1a237e" : "#283593") + ";"
        	  + "-fx-border-color: #3949ab;"
        	  + "-fx-border-width: 0 0 1 0;"
        	  + "-fx-cursor: hand;"));

        return ligne;
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

        Button[] boutons = {
        	    creerBoutonMenu("🏠", "Accueil",         true),
        	    creerBoutonMenu("🏫", "Salles",          false),
        	    creerBoutonMenu("📅", "Emploi du temps", false),
        	    creerBoutonMenu("👥", "Utilisateurs",    false),
        	    creerBoutonMenu("📊", "Rapports",        false),
        	    creerBoutonMenu("⚠️",  "Conflits",        false),
        	    creerBoutonMenu("🔧", "Signalements",    false),
        	    creerBoutonMenu("⭐", "Bloc des Plus",   false),
        	};

        boutons[0].setOnAction(e -> { activerBouton(boutons, 0); afficherAccueil(); });
        boutons[1].setOnAction(e -> { activerBouton(boutons, 1); afficherSalles(); });
        boutons[2].setOnAction(e -> { activerBouton(boutons, 2); afficherEmploiDuTemps(); });
        boutons[3].setOnAction(e -> { activerBouton(boutons, 3); afficherUtilisateurs(); });
        boutons[4].setOnAction(e -> { activerBouton(boutons, 4); afficherRapports(); });
        boutons[5].setOnAction(e -> { activerBouton(boutons, 5); afficherConflits(); });
        boutons[6].setOnAction(e -> { activerBouton(boutons, 6); afficherSignalements(); });
        boutons[7].setOnAction(e -> {
            activerBouton(boutons, 7);
            contenuCentral.getChildren().clear();
            new BlocPlusView((Enseignant) utilisateurConnecte).afficher();
        });
        // Tout masquer par défaut selon le rôle
        String role = utilisateurConnecte.getRole();
        if (role.equals("ENSEIGNANT")) {
            boutons[3].setVisible(false); boutons[3].setManaged(false);
            boutons[4].setVisible(false); boutons[4].setManaged(false);
            boutons[5].setVisible(false); boutons[5].setManaged(false);
            // ✅ déjà présent au dessus — ajouter :
            boutons[7].setVisible(true);  boutons[7].setManaged(true); // Bloc des Plus visible
        }

        // Masquer pour tous les autres rôles
        if (role.equals("ETUDIANT") || role.equals("ADMIN") || role.equals("GESTIONNAIRE")) {
            boutons[7].setVisible(false); boutons[7].setManaged(false);
        }
        if (role.equals("ETUDIANT")) {
            boutons[3].setVisible(false); boutons[3].setManaged(false);
            boutons[4].setVisible(false); boutons[4].setManaged(false);
            boutons[5].setVisible(false); boutons[5].setManaged(false);
            boutons[6].setVisible(false); boutons[6].setManaged(false);
        }
        if (role.equals("ENSEIGNANT")) {
            boutons[3].setVisible(false); boutons[3].setManaged(false); // Utilisateurs
            boutons[4].setVisible(false); boutons[4].setManaged(false); // Rapports
            boutons[5].setVisible(false); boutons[5].setManaged(false); // Conflits
            
        }
        if (role.equals("GESTIONNAIRE")) {
            boutons[3].setVisible(false); boutons[3].setManaged(false);
            boutons[6].setVisible(false); boutons[6].setManaged(false);
        }
        if (role.equals("ADMIN")) {
            boutons[5].setVisible(false); boutons[5].setManaged(false);
            boutons[6].setVisible(false); boutons[6].setManaged(false);
        }

        menu.getChildren().add(labelMenu);
        for (Button b : boutons) menu.getChildren().add(b);

        Region espace = new Region();
        VBox.setVgrow(espace, Priority.ALWAYS);
        menu.getChildren().add(espace);

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

        Label titre = new Label("Bonjour, " + utilisateurConnecte.getPrenom() + " 👋");
        titre.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titre.setTextFill(Color.web("#1a237e"));

        Label sousTitre = new Label("Voici un résumé de la situation aujourd'hui");
        sousTitre.setFont(Font.font("Arial", 14));
        sousTitre.setTextFill(Color.web("#757575"));

        // ── Données ──
        com.univscheduler.dao.SalleDAO salleDAO = new com.univscheduler.dao.SalleDAO();
        com.univscheduler.dao.CoursDAO coursDAO = new com.univscheduler.dao.CoursDAO();
        List<Salle> sallesReelles = salleDAO.getTous();
        List<Cours> coursReels    = coursDAO.getTous();
        long sallesLibres   = sallesReelles.stream().filter(Salle::isDisponible).count();
        long sallesOccupees = sallesReelles.size() - sallesLibres;

        // ── Cartes selon le rôle ──
        HBox cartes = new HBox(16);
        String role = utilisateurConnecte.getRole();

        if (role.equals("ENSEIGNANT")) {
            List<Cours> mesCours = coursDAO.getParEnseignant(utilisateurConnecte.getId());
            long nbSallesUtilisees = mesCours.stream()
                .map(c -> c.getSalle().getId())
                .distinct().count();

            cartes.getChildren().addAll(
                creerCarteStat("📚", "Mes cours",          String.valueOf(mesCours.size()),   "#1a237e", "#e8eaf6"),
                creerCarteStat("🏫", "Salles utilisées",   String.valueOf(nbSallesUtilisees), "#1b5e20", "#e8f5e9"),
                creerCarteStat("✅", "Salles disponibles", String.valueOf(sallesLibres),       "#e65100", "#fff3e0"),
                creerCarteStat("🔴", "Salles occupées",    String.valueOf(sallesOccupees),     "#b71c1c", "#ffebee"),
                creerCarteStat("⚠️",  "Conflits détectés", String.valueOf(conflits.size()),   "#4a148c", "#f3e5f5")
            );
        } else {
            cartes.getChildren().addAll(
                creerCarteStat("🏫", "Salles totales",     String.valueOf(sallesReelles.size()), "#1a237e", "#e8eaf6"),
                creerCarteStat("✅", "Salles disponibles", String.valueOf(sallesLibres),          "#1b5e20", "#e8f5e9"),
                creerCarteStat("🔴", "Salles occupées",    String.valueOf(sallesOccupees),         "#b71c1c", "#ffebee"),
                creerCarteStat("📚", "Cours planifiés",    String.valueOf(coursReels.size()),      "#e65100", "#fff3e0"),
                creerCarteStat("⚠️",  "Conflits détectés", String.valueOf(conflits.size()),       "#4a148c", "#f3e5f5")
            );
        }

        // ── Section conflits ──
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

        // ── Section cours ──
     // ── Section cours d'aujourd'hui ──
        VBox sectionCours = new VBox(10);
        Label titreCours = new Label("📅  Cours d'aujourd'hui");
        titreCours.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        titreCours.setTextFill(Color.web("#1a237e"));
        sectionCours.getChildren().add(titreCours);

        // ✅ Charger depuis la BDD selon le rôle
        com.univscheduler.dao.CoursDAO coursDAO2 = new com.univscheduler.dao.CoursDAO();
        String jourBrut = java.time.LocalDate.now()
        	    .getDayOfWeek()
        	    .getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.FRENCH);

        	final String jourAujourdhui = jourBrut.substring(0, 1).toUpperCase() 
        	                            + jourBrut.substring(1);
        List<Cours> coursAujourdhui;
        if (utilisateurConnecte.getRole().equals("ENSEIGNANT")) {
            coursAujourdhui = coursDAO2.getParEnseignant(utilisateurConnecte.getId())
                .stream()
                .filter(c -> c.getCreneau().getJour().equalsIgnoreCase(jourAujourdhui))
                .collect(java.util.stream.Collectors.toList());
        } else {
            coursAujourdhui = coursDAO2.getTous()
                .stream()
                .filter(c -> c.getCreneau().getJour().equalsIgnoreCase(jourAujourdhui))
                .collect(java.util.stream.Collectors.toList());
        }

        String[] couleurs = {"#e8eaf6", "#e8f5e9", "#fff3e0", "#fce4ec", "#e0f7fa"};
        String[] bordures = {"#3949ab", "#43a047", "#fb8c00", "#e91e63", "#00acc1"};

        if (coursAujourdhui.isEmpty()) {
            Label aucun = new Label("✅  Aucun cours prévu aujourd'hui (" + jourAujourdhui + ")");
            aucun.setFont(Font.font("Arial", 13));
            aucun.setTextFill(Color.web("#388e3c"));
            aucun.setPadding(new Insets(16));
            aucun.setStyle("-fx-background-color: #e8f5e9; -fx-background-radius: 8;");
            sectionCours.getChildren().add(aucun);
        } else {
            int i = 0;
            for (Cours c : coursAujourdhui) {
                HBox ligneC = new HBox(16);
                ligneC.setPadding(new Insets(12, 16, 12, 16));
                ligneC.setAlignment(Pos.CENTER_LEFT);
                ligneC.setStyle("-fx-background-color: " + couleurs[i % couleurs.length] + ";"
                              + "-fx-background-radius: 8;"
                              + "-fx-border-color: " + bordures[i % bordures.length] + ";"
                              + "-fx-border-radius: 8;"
                              + "-fx-border-width: 0 0 0 4;");

                Label mat = new Label(c.getMatiere());
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
    public static void main(String[] args) {
        launch(args);
    }
    private void afficherConflits() {
        contenuCentral.getChildren().clear();
        ConflitsView conflitsView = new ConflitsView();
        contenuCentral.getChildren().add(conflitsView.getVue());
    }

    private void afficherSignalements() {
        contenuCentral.getChildren().clear();
        SignalementView signalementView = new SignalementView();
        contenuCentral.getChildren().add(signalementView.getVue());
    }
}
