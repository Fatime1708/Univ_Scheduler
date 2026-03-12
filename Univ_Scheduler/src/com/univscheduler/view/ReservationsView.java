package com.univscheduler.view;

import com.univscheduler.dao.ReservationDAO;
import com.univscheduler.dao.NotificationDAO;
import com.univscheduler.dao.SalleDAO;
import com.univscheduler.model.Creneau;
import com.univscheduler.model.Enseignant;
import com.univscheduler.model.Reservation;
import com.univscheduler.model.Salle;
import com.univscheduler.model.Utilisateur;
import com.univscheduler.model.enums.StatutReservation;
import com.univscheduler.util.DatabaseConnection;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Vue de gestion des réservations de salles.
 * Accessible uniquement aux enseignants pour créer une réservation.
 */
public class ReservationsView extends VBox {

    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final SalleDAO       salleDAO       = new SalleDAO();

    private TableView<Reservation> tableau;
    private ObservableList<Reservation> listeReservations;

    public ReservationsView() {
        setSpacing(0);
        setStyle("-fx-background-color: #f5f6fa;");
        getChildren().addAll(construireEntete(), construireContenu());
        chargerReservations();
    }

    // ── En-tête ───────────────────────────────────────────────────
    private HBox construireEntete() {
        HBox entete = new HBox();
        entete.setPadding(new Insets(24, 32, 16, 32));
        entete.setAlignment(Pos.CENTER_LEFT);
        entete.setStyle("-fx-background-color: white;"
                      + "-fx-border-color: #e0e0e0;"
                      + "-fx-border-width: 0 0 1 0;");

        Label titre = new Label("📅 Réservations de salles");
        titre.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        titre.setTextFill(Color.web("#1a237e"));

        Region espace = new Region();
        HBox.setHgrow(espace, Priority.ALWAYS);

        // Bouton visible UNIQUEMENT pour l'enseignant
        boolean estEnseignant = DashboardView.utilisateurConnecte instanceof Enseignant;
        if (estEnseignant) {
            Button btnNouvelle = new Button("➕  Nouvelle réservation");
            btnNouvelle.setPrefHeight(38);
            btnNouvelle.setFont(Font.font("Arial", FontWeight.BOLD, 13));
            btnNouvelle.setStyle(
                "-fx-background-color: #1a237e; -fx-text-fill: white;"
              + "-fx-background-radius: 8; -fx-cursor: hand;");
            btnNouvelle.setOnAction(e -> ouvrirFormulaireCreation(null));
            btnNouvelle.setOnMouseEntered(e -> btnNouvelle.setStyle(
                "-fx-background-color: #283593; -fx-text-fill: white;"
              + "-fx-background-radius: 8; -fx-cursor: hand;"));
            btnNouvelle.setOnMouseExited(e -> btnNouvelle.setStyle(
                "-fx-background-color: #1a237e; -fx-text-fill: white;"
              + "-fx-background-radius: 8; -fx-cursor: hand;"));
            entete.getChildren().addAll(titre, espace, btnNouvelle);
        } else {
            entete.getChildren().addAll(titre, espace);
        }

        return entete;
    }

    // ── Contenu principal ─────────────────────────────────────────
    private VBox construireContenu() {
        VBox contenu = new VBox(16);
        contenu.setPadding(new Insets(24, 32, 24, 32));

        tableau = new TableView<>();
        tableau.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tableau.setStyle("-fx-background-color: white; -fx-background-radius: 12;");
        tableau.setPrefHeight(500);

        TableColumn<Reservation, String> colDate = new TableColumn<>("📅 Date");
        colDate.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getDate().toString()));
        colDate.setPrefWidth(120);

        TableColumn<Reservation, String> colSalle = new TableColumn<>("🏫 Salle");
        colSalle.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getSalle().getNumero()));
        colSalle.setPrefWidth(100);

        TableColumn<Reservation, String> colCreneau = new TableColumn<>("🕐 Créneau");
        colCreneau.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getCreneau().toString()));
        colCreneau.setPrefWidth(180);

        TableColumn<Reservation, String> colMotif = new TableColumn<>("📝 Motif");
        colMotif.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getMotif()));
        colMotif.setPrefWidth(200);

        TableColumn<Reservation, String> colStatut = new TableColumn<>("Statut");
        colStatut.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getStatut().name()));
        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);
                if (empty || statut == null) { setGraphic(null); return; }
                Label lbl = new Label();
                switch (statut) {
                    case "EN_ATTENTE":
                        lbl.setText("🟠 EN ATTENTE");
                        lbl.setStyle("-fx-background-color:#fff3e0;-fx-text-fill:#e65100;"
                                   + "-fx-padding:4 10;-fx-background-radius:12;"
                                   + "-fx-font-weight:bold;-fx-font-size:11;");
                        break;
                    case "CONFIRMEE":
                        lbl.setText("🟢 CONFIRMÉE");
                        lbl.setStyle("-fx-background-color:#e8f5e9;-fx-text-fill:#2e7d32;"
                                   + "-fx-padding:4 10;-fx-background-radius:12;"
                                   + "-fx-font-weight:bold;-fx-font-size:11;");
                        break;
                    case "ANNULEE":
                        lbl.setText("🔴 ANNULÉE");
                        lbl.setStyle("-fx-background-color:#ffebee;-fx-text-fill:#c62828;"
                                   + "-fx-padding:4 10;-fx-background-radius:12;"
                                   + "-fx-font-weight:bold;-fx-font-size:11;");
                        break;
                    default:
                        lbl.setText(statut);
                }
                setGraphic(lbl);
            }
        });
        colStatut.setPrefWidth(140);

        TableColumn<Reservation, Void> colAct = new TableColumn<>("Actions");
        colAct.setCellFactory(col -> new TableCell<>() {
            Button btnAnnuler = new Button("❌ Annuler");
            {
                btnAnnuler.setStyle(
                    "-fx-background-color:#ffebee;-fx-text-fill:#c62828;"
                  + "-fx-background-radius:6;-fx-cursor:hand;-fx-font-size:11;");
                btnAnnuler.setOnAction(e -> {
                    Reservation r = getTableView().getItems().get(getIndex());
                    annulerReservation(r);
                });
            }
            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                Reservation r = getTableView().getItems().get(getIndex());
                // Bouton annuler visible seulement pour l'enseignant
                boolean estEnseignant = DashboardView.utilisateurConnecte instanceof Enseignant;
                setGraphic((estEnseignant && r.getStatut() != StatutReservation.ANNULEE)
                        ? btnAnnuler : null);
            }
        });
        colAct.setPrefWidth(110);

        tableau.getColumns().add(colDate);
        tableau.getColumns().add(colSalle);
        tableau.getColumns().add(colCreneau);
        tableau.getColumns().add(colMotif);
        tableau.getColumns().add(colStatut);
        tableau.getColumns().add(colAct);

        listeReservations = FXCollections.observableArrayList();
        tableau.setItems(listeReservations);

        contenu.getChildren().add(tableau);
        VBox.setVgrow(tableau, Priority.ALWAYS);
        return contenu;
    }

    // ── Charger les réservations ──────────────────────────────────
    private void chargerReservations() {
        listeReservations.clear();
        Utilisateur connecte = DashboardView.utilisateurConnecte;
        if (connecte == null) return;
        List<Reservation> liste = reservationDAO.getParUtilisateur(connecte.getId());
        listeReservations.addAll(liste);
    }
    // ── Charger les créneaux depuis la BDD ────────────────────────
    private List<Creneau> chargerCreneaux() {
        List<Creneau> liste = new ArrayList<>();
        String sql = "SELECT * FROM creneaux ORDER BY heure_debut";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String heureStr  = rs.getString("heure_debut");
                String[] parts   = heureStr.split(":");
                LocalTime hDebut = LocalTime.of(
                    Integer.parseInt(parts[0]),
                    Integer.parseInt(parts[1])
                );
                Creneau c = new Creneau(
                    rs.getString("jour"), hDebut, rs.getInt("duree_minutes"));
                c.setId(rs.getInt("id"));
                liste.add(c);
            }
        } catch (SQLException e) {
            System.err.println("Erreur chargerCreneaux() : " + e.getMessage());
        }
        return liste;
    }

    // ── Charger les équipements d'une salle ───────────────────────
    private List<String> chargerEquipements(int salleId) {
        List<String> equipements = new ArrayList<>();
        String sql = "SELECT nom, description, fonctionnel "
                   + "FROM equipements WHERE salle_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, salleId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String nom         = rs.getString("nom");
                String description = rs.getString("description");
                boolean fonctionnel = rs.getBoolean("fonctionnel");
                String icone = fonctionnel ? "✅" : "❌";
                String ligne = icone + " " + nom;
                if (description != null && !description.isEmpty()) {
                    ligne += " — " + description;
                }
                equipements.add(ligne);
            }
        } catch (SQLException e) {
            System.err.println("Erreur chargerEquipements() : " + e.getMessage());
        }
        return equipements;
    }

    // ── Formulaire de création ────────────────────────────────────
    public void ouvrirFormulaireCreation(Salle sallePrecochoisie) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Nouvelle réservation");
        dialog.setHeaderText(null);
        dialog.getDialogPane().setPrefWidth(500);

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(14);
        grid.setPadding(new Insets(20));

        // Date
        Label lblDate = new Label("📅 Date :");
        lblDate.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        DatePicker datePicker = new DatePicker(LocalDate.now().plusDays(1));
        datePicker.setPrefWidth(220);

        // Salle
        Label lblSalle = new Label("🏫 Salle :");
        lblSalle.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        ComboBox<Salle> comboSalle = new ComboBox<>();
        comboSalle.getItems().addAll(salleDAO.getTous());
        comboSalle.setPrefWidth(220);
        comboSalle.setConverter(new javafx.util.StringConverter<Salle>() {
            @Override public String toString(Salle s) {
                return s == null ? "" : s.getNumero() + " (cap. " + s.getCapacite() + ")";
            }
            @Override public Salle fromString(String s) { return null; }
        });
        if (sallePrecochoisie != null) comboSalle.setValue(sallePrecochoisie);

        // Zone équipements — mise à jour quand on change de salle
        Label lblEquip = new Label("🔧 Équipements :");
        lblEquip.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        VBox boxEquipements = new VBox(4);
        boxEquipements.setPadding(new Insets(8));
        boxEquipements.setStyle("-fx-background-color: #f5f5f5;"
                              + "-fx-background-radius: 8;"
                              + "-fx-border-color: #e0e0e0;"
                              + "-fx-border-radius: 8;");
        boxEquipements.setPrefWidth(220);
        boxEquipements.setMinHeight(60);

        Label lblAucunEquip = new Label("Sélectionnez une salle");
        lblAucunEquip.setTextFill(Color.GRAY);
        lblAucunEquip.setFont(Font.font("Arial", 11));
        boxEquipements.getChildren().add(lblAucunEquip);

        // Charger équipements quand salle change
        comboSalle.setOnAction(e -> {
            boxEquipements.getChildren().clear();
            Salle salleChoisie = comboSalle.getValue();
            if (salleChoisie == null) {
                boxEquipements.getChildren().add(lblAucunEquip);
                return;
            }
            List<String> equips = chargerEquipements(salleChoisie.getId());
            if (equips.isEmpty()) {
                Label aucun = new Label("Aucun équipement enregistré");
                aucun.setTextFill(Color.GRAY);
                aucun.setFont(Font.font("Arial", 11));
                boxEquipements.getChildren().add(aucun);
            } else {
                for (String eq : equips) {
                    Label lEquip = new Label(eq);
                    lEquip.setFont(Font.font("Arial", 11));
                    // Rouge si non fonctionnel, vert si fonctionnel
                    lEquip.setTextFill(eq.startsWith("✅")
                            ? Color.web("#2e7d32") : Color.web("#c62828"));
                    boxEquipements.getChildren().add(lEquip);
                }
            }
        });

        // Si salle pré-choisie, charger ses équipements tout de suite
        if (sallePrecochoisie != null) {
            boxEquipements.getChildren().clear();
            List<String> equips = chargerEquipements(sallePrecochoisie.getId());
            if (equips.isEmpty()) {
                Label aucun = new Label("Aucun équipement enregistré");
                aucun.setTextFill(Color.GRAY);
                aucun.setFont(Font.font("Arial", 11));
                boxEquipements.getChildren().add(aucun);
            } else {
                for (String eq : equips) {
                    Label lEquip = new Label(eq);
                    lEquip.setFont(Font.font("Arial", 11));
                    lEquip.setTextFill(eq.startsWith("✅")
                            ? Color.web("#2e7d32") : Color.web("#c62828"));
                    boxEquipements.getChildren().add(lEquip);
                }
            }
        }

        // Créneau
        Label lblCreneau = new Label("🕐 Créneau :");
        lblCreneau.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        ComboBox<Creneau> comboCreneau = new ComboBox<>();
        comboCreneau.getItems().addAll(chargerCreneaux());
        comboCreneau.setPrefWidth(220);
        comboCreneau.setConverter(new javafx.util.StringConverter<Creneau>() {
            @Override public String toString(Creneau c) {
                return c == null ? "" : c.toString();
            }
            @Override public Creneau fromString(String s) { return null; }
        });

        // Motif
        Label lblMotif = new Label("📝 Motif :");
        lblMotif.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        TextField tfMotif = new TextField();
        tfMotif.setPromptText("Ex: Soutenance de stage, Réunion...");
        tfMotif.setPrefWidth(220);

        // Message d'erreur
        Label msgErreur = new Label();
        msgErreur.setTextFill(Color.RED);
        msgErreur.setFont(Font.font("Arial", 12));

        grid.add(lblDate,    0, 0); grid.add(datePicker,    1, 0);
        grid.add(lblSalle,   0, 1); grid.add(comboSalle,    1, 1);
        grid.add(lblEquip,   0, 2); grid.add(boxEquipements,1, 2);
        grid.add(lblCreneau, 0, 3); grid.add(comboCreneau,  1, 3);
        grid.add(lblMotif,   0, 4); grid.add(tfMotif,       1, 4);
        grid.add(msgErreur,  0, 5, 2, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button btnOK = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        btnOK.setText("✅ Réserver");
        btnOK.setStyle("-fx-background-color:#1a237e;-fx-text-fill:white;"
                     + "-fx-font-weight:bold;-fx-background-radius:6;");

        btnOK.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            LocalDate date  = datePicker.getValue();
            Salle salle     = comboSalle.getValue();
            Creneau creneau = comboCreneau.getValue();
            String motif    = tfMotif.getText().trim();

            if (date == null || salle == null || creneau == null || motif.isEmpty()) {
                msgErreur.setText("⚠ Tous les champs sont obligatoires.");
                event.consume();
                return;
            }
            if (date.isBefore(LocalDate.now())) {
                msgErreur.setText("⚠ La date doit être aujourd'hui ou dans le futur.");
                event.consume();
                return;
            }
            if (!reservationDAO.estDisponible(salle.getId(), date, creneau.getId())) {
                msgErreur.setText("❌ Cette salle est déjà réservée à ce créneau !");
                event.consume();
                return;
            }

            Reservation reservation = new Reservation(
                date, motif,
                DashboardView.utilisateurConnecte,
                salle, creneau
            );

            if (reservationDAO.ajouter(reservation)) {
                // ── Notifier l'enseignant ──
                NotificationDAO notifDAO = new NotificationDAO();
                notifDAO.ajouter(
                    "📅 Réservation confirmée",
                    "Votre réservation pour la salle "
                        + salle.getNumero() + " le " + date + " est confirmée.",
                    "RESERVATION", "ENSEIGNANT",
                    DashboardView.utilisateurConnecte.getId());
                chargerReservations();
            } else {
                msgErreur.setText("❌ Erreur lors de l'enregistrement.");
                event.consume();
            }
        });

        dialog.showAndWait();
    }

    // ── Annuler une réservation ───────────────────────────────────
    private void annulerReservation(Reservation reservation) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer l'annulation");
        confirm.setHeaderText(null);
        confirm.setContentText("Voulez-vous vraiment annuler cette réservation ?\n"
            + reservation.getSalle().getNumero() + " — " + reservation.getDate());

        confirm.showAndWait().ifPresent(reponse -> {
            if (reponse == ButtonType.OK) {
                reservationDAO.changerStatut(reservation.getId(), StatutReservation.ANNULEE);
                chargerReservations();
            }
        });
    }
}