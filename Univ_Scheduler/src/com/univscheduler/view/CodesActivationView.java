package com.univscheduler.view;

import com.univscheduler.model.Utilisateur;
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
import java.util.Random;

/**
 * Page de gestion des codes d'activation.
 * Accessible uniquement par l'Administrateur.
 */
public class CodesActivationView {

    private Utilisateur admin;
    private ObservableList<String[]> listeCodes = FXCollections.observableArrayList();
    private TableView<String[]> tableau;

    public CodesActivationView(Utilisateur admin) {
        this.admin = admin;
    }

    public VBox getVue() {
        VBox vue = new VBox(20);
        vue.setPadding(new Insets(10, 0, 0, 0));

        Label titre = new Label("🔑  Codes d'Activation");
        titre.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        titre.setTextFill(Color.web("#1a237e"));

        Label sousTitre = new Label(
            "Générez des codes pour permettre aux étudiants et enseignants de s'inscrire");
        sousTitre.setFont(Font.font("Arial", 13));
        sousTitre.setTextFill(Color.web("#757575"));

        // Stats
        HBox stats = construireStats();

        // Zone génération + tableau
        HBox zoneprincipale = new HBox(20);

        VBox gauche = construirePanneauGeneration();
        gauche.setPrefWidth(280);

        VBox droite = construireTableau();
        HBox.setHgrow(droite, Priority.ALWAYS);

        zoneprincipale.getChildren().addAll(gauche, droite);

        vue.getChildren().addAll(titre, sousTitre, stats, zoneprincipale);

        // Charger les codes
        chargerCodes();

        return vue;
    }

    // ── Stats ────────────────────────────────────────────────────
    private HBox construireStats() {
        HBox stats = new HBox(12);

        int[] compteurs = compterCodes();
        stats.getChildren().addAll(
            miniStat("Total",    String.valueOf(compteurs[0]), "#1a237e", "#e8eaf6"),
            miniStat("Disponibles", String.valueOf(compteurs[1]), "#1b5e20", "#e8f5e9"),
            miniStat("Utilisés", String.valueOf(compteurs[2]), "#b71c1c", "#ffebee")
        );
        return stats;
    }

    // ── Panneau génération ───────────────────────────────────────
    private VBox construirePanneauGeneration() {
        VBox panneau = new VBox(14);
        panneau.setPadding(new Insets(20));
        panneau.setStyle("-fx-background-color: white;"
                       + "-fx-background-radius: 12;"
                       + "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.08),6,0,0,2);");

        Label titre = new Label("Générer un code");
        titre.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        titre.setTextFill(Color.web("#1a237e"));

        // Sélecteur de rôle
        Label lblRole = new Label("Rôle :");
        lblRole.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        ComboBox<String> selectRole = new ComboBox<>();
        selectRole.getItems().addAll("ETUDIANT", "ENSEIGNANT", "GESTIONNAIRE");
        selectRole.setValue("ETUDIANT");
        selectRole.setMaxWidth(Double.MAX_VALUE);

        // Nombre de codes
        Label lblNb = new Label("Nombre de codes :");
        lblNb.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        ComboBox<Integer> selectNb = new ComboBox<>();
        selectNb.getItems().addAll(1, 5, 10, 20, 50);
        selectNb.setValue(1);
        selectNb.setMaxWidth(Double.MAX_VALUE);

        // Aperçu du code
        Label lblApercu = new Label("Aperçu :");
        lblApercu.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        Label apercu = new Label(genererCodeApercu("ETUDIANT"));
        apercu.setFont(Font.font("Monospace", FontWeight.BOLD, 14));
        apercu.setTextFill(Color.web("#1a237e"));
        apercu.setPadding(new Insets(8, 12, 8, 12));
        apercu.setStyle("-fx-background-color: #e8eaf6; -fx-background-radius: 6;");

        selectRole.setOnAction(e ->
            apercu.setText(genererCodeApercu(selectRole.getValue())));

        // Bouton générer
        Button btnGenerer = new Button("🔑  Générer les codes");
        btnGenerer.setMaxWidth(Double.MAX_VALUE);
        btnGenerer.setPrefHeight(40);
        btnGenerer.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        btnGenerer.setStyle("-fx-background-color: #1a237e; -fx-text-fill: white;"
                          + "-fx-background-radius: 8; -fx-cursor: hand;");
        btnGenerer.setOnAction(e -> {
            genererCodes(selectRole.getValue(), selectNb.getValue());
            apercu.setText(genererCodeApercu(selectRole.getValue()));
        });
        btnGenerer.setOnMouseEntered(e -> btnGenerer.setStyle(
            "-fx-background-color: #283593; -fx-text-fill: white;"
          + "-fx-background-radius: 8; -fx-cursor: hand;"));
        btnGenerer.setOnMouseExited(e -> btnGenerer.setStyle(
            "-fx-background-color: #1a237e; -fx-text-fill: white;"
          + "-fx-background-radius: 8; -fx-cursor: hand;"));

        // Info
        Label info = new Label("💡  Donnez le code généré\nà l'étudiant/enseignant\npour qu'il s'inscrive.");
        info.setFont(Font.font("Arial", 11));
        info.setTextFill(Color.web("#757575"));
        info.setPadding(new Insets(10));
        info.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 8;");

        panneau.getChildren().addAll(
            titre, new Separator(),
            lblRole, selectRole,
            lblNb, selectNb,
            lblApercu, apercu,
            btnGenerer, info
        );
        return panneau;
    }

    // ── Tableau des codes ────────────────────────────────────────
    @SuppressWarnings("unchecked")
    private VBox construireTableau() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: white;"
                   + "-fx-background-radius: 12;"
                   + "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.08),6,0,0,2);");

        Label titre = new Label("📋  Tous les codes");
        titre.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        titre.setTextFill(Color.web("#1a237e"));

        tableau = new TableView<>(listeCodes);
        tableau.setPrefHeight(380);
        tableau.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableau.setPlaceholder(new Label("Aucun code généré"));

        // Colonne Code
        TableColumn<String[], String> colCode = new TableColumn<>("Code");
        colCode.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue()[0]));
        colCode.setMinWidth(150);
        colCode.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean e) {
                super.updateItem(v, e);
                if (e || v == null) setText(null);
                else {
                    setText(v);
                    setFont(Font.font("Monospace", FontWeight.BOLD, 12));
                    setTextFill(Color.web("#1a237e"));
                }
            }
        });

        // Colonne Rôle
        TableColumn<String[], String> colRole = new TableColumn<>("Rôle");
        colRole.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue()[1]));
        colRole.setMinWidth(120);
        colRole.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean e) {
                super.updateItem(v, e);
                if (e || v == null) { setGraphic(null); return; }
                String[] cfg = switch (v) {
                    case "ETUDIANT"     -> new String[]{"Étudiant",    "#4a148c","#f3e5f5"};
                    case "ENSEIGNANT"   -> new String[]{"Enseignant",  "#1b5e20","#e8f5e9"};
                    case "GESTIONNAIRE" -> new String[]{"Gestionnaire","#e65100","#fff3e0"};
                    default             -> new String[]{v,             "#424242","#f5f5f5"};
                };
                Label b = new Label(cfg[0]);
                b.setFont(Font.font("Arial", FontWeight.BOLD, 11));
                b.setTextFill(Color.web(cfg[1]));
                b.setPadding(new Insets(3, 10, 3, 10));
                b.setStyle("-fx-background-color:" + cfg[2] + ";-fx-background-radius:20;");
                setGraphic(b); setText(null);
            }
        });

        // Colonne Statut
        TableColumn<String[], String> colStatut = new TableColumn<>("Statut");
        colStatut.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue()[2]));
        colStatut.setMinWidth(100);
        colStatut.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean e) {
                super.updateItem(v, e);
                if (e || v == null) { setGraphic(null); return; }
                boolean utilise = v.equals("1") || v.equals("true");
                Label l = new Label(utilise ? "● Utilisé" : "● Disponible");
                l.setFont(Font.font("Arial", FontWeight.BOLD, 11));
                l.setTextFill(utilise ? Color.web("#c62828") : Color.web("#2e7d32"));
                setGraphic(l); setText(null);
            }
        });

        // Colonne Date
        TableColumn<String[], String> colDate = new TableColumn<>("Créé le");
        colDate.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue()[3]));
        colDate.setMinWidth(130);

        // Colonne Actions
        TableColumn<String[], Void> colAct = new TableColumn<>("Actions");
        colAct.setMinWidth(100);
        colAct.setCellFactory(c -> new TableCell<>() {
            Button btnSupp = new Button("🗑️ Supprimer");
            {
                btnSupp.setStyle("-fx-background-color:#ffebee;-fx-text-fill:#c62828;"
                               + "-fx-background-radius:6;-fx-cursor:hand;-fx-font-size:11;");
                btnSupp.setOnAction(e -> {
                    String[] row = getTableView().getItems().get(getIndex());
                    // Seulement si pas encore utilisé
                    if (row[2].equals("1")) {
                        Alert a = new Alert(Alert.AlertType.WARNING);
                        a.setHeaderText("Ce code a déjà été utilisé !");
                        a.setContentText("Impossible de supprimer un code déjà utilisé.");
                        a.showAndWait();
                        return;
                    }
                    supprimerCode(row[0]);
                });
            }
            @Override protected void updateItem(Void v, boolean e) {
                super.updateItem(v, e);
                setGraphic(e ? null : btnSupp);
            }
        });

        tableau.getColumns().addAll(colCode, colRole, colStatut, colDate, colAct);

        box.getChildren().addAll(titre, new Separator(), tableau);
        return box;
    }

    // ════════════════════════════════════════════════════════════
    //  LOGIQUE
    // ════════════════════════════════════════════════════════════

    private void genererCodes(String role, int nombre) {
        String sql = "INSERT INTO codes_activation (code, role, created_by) VALUES (?,?,?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int success = 0;
            for (int i = 0; i < nombre; i++) {
                String code = genererCodeAleatoire(role);
                ps.setString(1, code);
                ps.setString(2, role);
                ps.setInt(3,    admin.getId());
                try {
                    ps.executeUpdate();
                    success++;
                } catch (SQLException e) {
                    // Code dupliqué, on réessaie
                    i--;
                }
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Codes générés");
            alert.setHeaderText("✅  " + success + " code(s) généré(s) avec succès !");
            alert.setContentText("Les codes sont maintenant disponibles dans le tableau.");
            alert.showAndWait();

            chargerCodes(); // Rafraîchir

        } catch (SQLException e) {
            System.err.println("Erreur génération codes : " + e.getMessage());
        }
    }

    private void chargerCodes() {
        listeCodes.clear();
        String sql = "SELECT code, role, utilise, created_at FROM codes_activation ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {

            while (rs.next()) {
                listeCodes.add(new String[]{
                    rs.getString("code"),
                    rs.getString("role"),
                    rs.getString("utilise"),
                    rs.getString("created_at").substring(0, 10)
                });
            }
        } catch (SQLException e) {
            System.err.println("Erreur chargement codes : " + e.getMessage());
        }
    }

    private void supprimerCode(String code) {
        String sql = "DELETE FROM codes_activation WHERE code = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.executeUpdate();
            chargerCodes();
        } catch (SQLException e) {
            System.err.println("Erreur suppression code : " + e.getMessage());
        }
    }

    private int[] compterCodes() {
        int[] c = {0, 0, 0};
        String sql = "SELECT COUNT(*) as total, "
                   + "SUM(utilise = 0) as dispo, "
                   + "SUM(utilise = 1) as utilises "
                   + "FROM codes_activation";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            if (rs.next()) {
                c[0] = rs.getInt("total");
                c[1] = rs.getInt("dispo");
                c[2] = rs.getInt("utilises");
            }
        } catch (SQLException e) {
            System.err.println("Erreur comptage codes : " + e.getMessage());
        }
        return c;
    }

    // ════════════════════════════════════════════════════════════
    //  UTILITAIRES
    // ════════════════════════════════════════════════════════════

    private String genererCodeAleatoire(String role) {
        String prefix = switch (role) {
            case "ETUDIANT"     -> "ETU";
            case "ENSEIGNANT"   -> "ENS";
            case "GESTIONNAIRE" -> "GES";
            default             -> "USR";
        };
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        Random rand  = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) sb.append(chars.charAt(rand.nextInt(chars.length())));
        return prefix + "-2024-" + sb;
    }

    private String genererCodeApercu(String role) {
        return genererCodeAleatoire(role) + "  (exemple)";
    }

    private VBox miniStat(String label, String valeur, String ct, String cf) {
        VBox c = new VBox(4);
        c.setPadding(new Insets(12, 20, 12, 20)); c.setAlignment(Pos.CENTER);
        c.setStyle("-fx-background-color:" + cf + ";-fx-background-radius:10;");
        Label v = new Label(valeur);
        v.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 22));
        v.setTextFill(Color.web(ct));
        Label l = new Label(label);
        l.setFont(Font.font("Arial", 11)); l.setTextFill(Color.web("#616161"));
        c.getChildren().addAll(v, l);
        return c;
    }
}
