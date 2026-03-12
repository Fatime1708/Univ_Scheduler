
package com.univscheduler.view;

import com.univscheduler.service.NotificationService;
import com.univscheduler.service.NotificationService.Notification;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Vue JavaFX pour afficher et gérer les notifications/alertes de conflits.
 */
public class NotificationsView {

    private Stage stage;
    private VBox listeContainer;
    private Label labelCompteur;
    private ComboBox<String> filtreCombo;

    private static final String COULEUR_CONFLIT     = "#e74c3c";
    private static final String COULEUR_AVERTISSEMENT = "#f39c12";
    private static final String COULEUR_INFO         = "#3498db";
    private static final String FOND_PRINCIPAL       = "#f0f2f5";

    public NotificationsView() {
        // Initialiser des notifications de test si aucune n'existe
        if (NotificationService.getToutesLesNotifications().isEmpty()) {
            NotificationService.initialiserNotificationsDeTest();
        }
    }

    public Scene creerScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + FOND_PRINCIPAL + ";");

        root.setTop(creerEnTete());
        root.setCenter(creerContenuPrincipal());

        return new Scene(root, 900, 650);
    }

    // ─────────────────────────────────────────────────────────────
    // EN-TÊTE
    // ─────────────────────────────────────────────────────────────
    private HBox creerEnTete() {
        HBox header = new HBox();
        header.setStyle("-fx-background-color: #2c3e50; -fx-padding: 18 25;");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(15);

        Label titre = new Label("🔔 Notifications & Alertes");
        titre.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        titre.setTextFill(Color.WHITE);

        // Badge nombre non lues
        int nonLues = NotificationService.getNombreNonLues();
        labelCompteur = new Label(nonLues > 0 ? nonLues + " non lue(s)" : "Tout lu");
        labelCompteur.setStyle(
            "-fx-background-color: " + (nonLues > 0 ? "#e74c3c" : "#27ae60") + ";"
            + "-fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 12;"
            + "-fx-font-size: 12px; -fx-font-weight: bold;"
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Bouton tout marquer comme lu
        Button btnToutLu = new Button("✓ Tout marquer comme lu");
        btnToutLu.setStyle(
            "-fx-background-color: #27ae60; -fx-text-fill: white;"
            + "-fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 8;"
            + "-fx-cursor: hand;"
        );
        btnToutLu.setOnAction(e -> {
            NotificationService.marquerToutesCommeLues();
            rafraichirListe();
        });

        // Bouton vider tout
        Button btnVider = new Button("🗑 Vider tout");
        btnVider.setStyle(
            "-fx-background-color: #e74c3c; -fx-text-fill: white;"
            + "-fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 8;"
            + "-fx-cursor: hand;"
        );
        btnVider.setOnAction(e -> {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer toutes les notifications ?", ButtonType.YES, ButtonType.NO);
            confirmation.setTitle("Confirmation");
            confirmation.showAndWait().ifPresent(r -> {
                if (r == ButtonType.YES) {
                    NotificationService.viderTout();
                    rafraichirListe();
                }
            });
        });

        header.getChildren().addAll(titre, labelCompteur, spacer, btnToutLu, btnVider);
        return header;
    }

    // ─────────────────────────────────────────────────────────────
    // CONTENU PRINCIPAL
    // ─────────────────────────────────────────────────────────────
    private BorderPane creerContenuPrincipal() {
        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(20));

        // Barre de filtre
        HBox barreFiltre = new HBox(10);
        barreFiltre.setAlignment(Pos.CENTER_LEFT);
        barreFiltre.setPadding(new Insets(0, 0, 15, 0));

        Label lblFiltre = new Label("Filtrer :");
        lblFiltre.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));

        filtreCombo = new ComboBox<>();
        filtreCombo.getItems().addAll("Toutes", "Conflits", "Avertissements", "Informations", "Non lues");
        filtreCombo.setValue("Toutes");
        filtreCombo.setStyle("-fx-font-size: 13px; -fx-padding: 5;");
        filtreCombo.setOnAction(e -> rafraichirListe());

        barreFiltre.getChildren().addAll(lblFiltre, filtreCombo);
        pane.setTop(barreFiltre);

        // Liste des notifications dans un ScrollPane
        listeContainer = new VBox(10);
        listeContainer.setPadding(new Insets(5));

        ScrollPane scroll = new ScrollPane(listeContainer);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        pane.setCenter(scroll);

        rafraichirListe();
        return pane;
    }

    // ─────────────────────────────────────────────────────────────
    // RAFRAÎCHIR LA LISTE
    // ─────────────────────────────────────────────────────────────
    private void rafraichirListe() {
        listeContainer.getChildren().clear();

        List<Notification> toutesNotifs = NotificationService.getToutesLesNotifications();
        String filtre = filtreCombo != null ? filtreCombo.getValue() : "Toutes";

        long affichees = 0;
        for (Notification notif : toutesNotifs) {
            if (filtrerNotification(notif, filtre)) {
                listeContainer.getChildren().add(creerCarteNotification(notif));
                affichees++;
            }
        }

        // Message si aucune notification
        if (affichees == 0) {
            VBox vide = new VBox(10);
            vide.setAlignment(Pos.CENTER);
            vide.setPadding(new Insets(60));
            Label icone = new Label("🎉");
            icone.setFont(Font.font(48));
            Label msg = new Label("Aucune notification");
            msg.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
            msg.setTextFill(Color.GRAY);
            Label sousMesg = new Label("Tout est en ordre, aucun conflit détecté !");
            sousMesg.setTextFill(Color.GRAY);
            vide.getChildren().addAll(icone, msg, sousMesg);
            listeContainer.getChildren().add(vide);
        }

        // Mettre à jour le badge compteur
        if (labelCompteur != null) {
            int nonLues = NotificationService.getNombreNonLues();
            labelCompteur.setText(nonLues > 0 ? nonLues + " non lue(s)" : "Tout lu");
            labelCompteur.setStyle(
                "-fx-background-color: " + (nonLues > 0 ? "#e74c3c" : "#27ae60") + ";"
                + "-fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 12;"
                + "-fx-font-size: 12px; -fx-font-weight: bold;"
            );
        }
    }

    private boolean filtrerNotification(Notification notif, String filtre) {
        switch (filtre) {
            case "Conflits":       return notif.getType() == Notification.Type.CONFLIT;
            case "Avertissements": return notif.getType() == Notification.Type.AVERTISSEMENT;
            case "Informations":   return notif.getType() == Notification.Type.INFO;
            case "Non lues":       return notif.isNonLue();
            default:               return true;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // CARTE NOTIFICATION
    // ─────────────────────────────────────────────────────────────
    private HBox creerCarteNotification(Notification notif) {
        HBox carte = new HBox(15);
        carte.setPadding(new Insets(15, 20, 15, 0));
        carte.setAlignment(Pos.CENTER_LEFT);

        String couleur = getCouleur(notif.getType());
        String bgCouleur = notif.isNonLue() ? "white" : "#f8f9fa";

        carte.setStyle(
            "-fx-background-color: " + bgCouleur + ";"
            + "-fx-background-radius: 10;"
            + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 6, 0, 0, 2);"
        );

        // Barre colorée à gauche
        VBox barre = new VBox();
        barre.setMinWidth(5);
        barre.setStyle("-fx-background-color: " + couleur + "; -fx-background-radius: 5 0 0 5;");

        // Icône
        Label icone = new Label(getIcone(notif.getType()));
        icone.setFont(Font.font(22));
        icone.setPadding(new Insets(0, 5, 0, 15));

        // Contenu texte
        VBox contenu = new VBox(4);
        HBox.setHgrow(contenu, Priority.ALWAYS);

        HBox ligneHaut = new HBox(10);
        ligneHaut.setAlignment(Pos.CENTER_LEFT);

        Label titre = new Label(notif.getTitre());
        titre.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        titre.setTextFill(Color.web("#000000"));

        if (notif.isNonLue()) {
            Label badgeNew = new Label("NOUVEAU");
            badgeNew.setStyle(
                "-fx-background-color: " + couleur + "; -fx-text-fill: white;"
                + "-fx-font-size: 9px; -fx-font-weight: bold;"
                + "-fx-padding: 2 6; -fx-background-radius: 4;"
            );
            ligneHaut.getChildren().addAll(titre, badgeNew);
        } else {
            ligneHaut.getChildren().add(titre);
        }

        Label message = new Label(notif.getMessage());
        message.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        message.setTextFill(Color.web("#000000"));  // noir foncé
        message.setWrapText(true);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        Label date = new Label("🕐 " + notif.getDateCreation().format(fmt)
                + "  •  " + notif.getEntiteType());
        date.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        date.setTextFill(Color.web("#000000"));  // gris foncé lisible

        

        contenu.getChildren().addAll(ligneHaut, message, date);

        // Boutons actions
        VBox actions = new VBox(8);
        actions.setAlignment(Pos.CENTER);
        actions.setPadding(new Insets(0, 10, 0, 0));

        if (notif.isNonLue()) {
            Button btnLu = new Button("✓ Lu");
            btnLu.setStyle(
                "-fx-background-color: #27ae60; -fx-text-fill: white;"
                + "-fx-font-size: 11px; -fx-padding: 5 10; -fx-background-radius: 6;"
                + "-fx-cursor: hand;"
            );
            btnLu.setOnAction(e -> {
                NotificationService.marquerCommeLue(notif.getId());
                rafraichirListe();
            });
            actions.getChildren().add(btnLu);
        }

        Button btnSuppr = new Button("🗑");
        btnSuppr.setStyle(
            "-fx-background-color: #ecf0f1; -fx-text-fill: #e74c3c;"
            + "-fx-font-size: 13px; -fx-padding: 5 8; -fx-background-radius: 6;"
            + "-fx-cursor: hand;"
        );
        btnSuppr.setOnAction(e -> {
            NotificationService.supprimer(notif.getId());
            rafraichirListe();
        });
        actions.getChildren().add(btnSuppr);

        carte.getChildren().addAll(barre, icone, contenu, actions);
        return carte;
    }

    private String getCouleur(Notification.Type type) {
        switch (type) {
            case CONFLIT:       return COULEUR_CONFLIT;
            case AVERTISSEMENT: return COULEUR_AVERTISSEMENT;
            default:            return COULEUR_INFO;
        }
    }

    private String getIcone(Notification.Type type) {
        switch (type) {
            case CONFLIT:       return "🚨";
            case AVERTISSEMENT: return "⚠️";
            default:            return "ℹ️";
        }
    }

    // ─────────────────────────────────────────────────────────────
    // AFFICHER DANS UN STAGE
    // ─────────────────────────────────────────────────────────────
    public void afficher(Stage parentStage) {
        stage = new Stage();
        stage.setTitle("Notifications & Alertes — UNIV-SCHEDULER");
        stage.setScene(creerScene());
        stage.setMinWidth(750);
        stage.setMinHeight(500);
        stage.show();
    }

    /**
     * Crée un bouton cloche avec badge à intégrer dans le Dashboard.
     * Quand on clique, ouvre la fenêtre des notifications.
     */
    public static Button creerBoutonCloche(Stage parentStage) {
        int nonLues = NotificationService.getNombreNonLues();

        
        Button btn = new Button("🔔" + (nonLues > 0 ? " " + nonLues : ""));
        btn.setStyle(
            "-fx-background-color: " + (nonLues > 0 ? "#e74c3c" : "#3d566e") + ";"
            + "-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;"
            + "-fx-padding: 8 14; -fx-background-radius: 20; -fx-cursor: hand;"
        );
        btn.setOnAction(e -> {
            NotificationsView vue = new NotificationsView();
            vue.afficher(parentStage);
            // Rafraîchir le label après fermeture
            btn.setText("🔔");
            btn.setStyle(
                "-fx-background-color: #3d566e; -fx-text-fill: white;"
                + "-fx-font-size: 13px; -fx-padding: 8 14; -fx-background-radius: 20;"
                + "-fx-cursor: hand;"
            );
        });

        return btn;
    }
}
