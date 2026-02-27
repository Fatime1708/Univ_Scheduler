package com.univscheduler.view;

import com.univscheduler.dao.SalleDAO;
import com.univscheduler.model.Salle;
import com.univscheduler.model.enums.TypeSalle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

/**
 * Écran de gestion des salles — données venant de MySQL via SalleDAO.
 */
public class SallesView {

    // ── DAO ──────────────────────────────────────────────────────
    private final SalleDAO salleDAO = new SalleDAO();

    // ── Données ──────────────────────────────────────────────────
    private ObservableList<Salle> listeSalles;
    private List<Salle> toutesLesSalles;

    // ── Composants ───────────────────────────────────────────────
    private TableView<Salle> tableau;

    // ── Constructeur ─────────────────────────────────────────────
    public SallesView() {
        // ← Données venant de MySQL
        toutesLesSalles = salleDAO.getTous();
        listeSalles     = FXCollections.observableArrayList(toutesLesSalles);
    }

    // ════════════════════════════════════════════════════════════
    //  INTERFACE
    // ════════════════════════════════════════════════════════════

    public VBox getVue() {
        VBox vue = new VBox(20);
        vue.setPadding(new Insets(10, 0, 0, 0));

        Label titre = new Label("🏫  Gestion des Salles");
        titre.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        titre.setTextFill(Color.web("#1a237e"));

        vue.getChildren().addAll(titre, construireStats(),
                construireBarreOutils(), construireTableau());
        return vue;
    }

    private HBox construireStats() {
        HBox stats = new HBox(12);
        long total    = toutesLesSalles.size();
        long libres   = toutesLesSalles.stream().filter(Salle::isDisponible).count();
        long occupees = total - libres;
        long amphi    = toutesLesSalles.stream()
                .filter(s -> s.getType() == TypeSalle.AMPHI).count();
        stats.getChildren().addAll(
            miniStat("Total",    String.valueOf(total),    "#1a237e", "#e8eaf6"),
            miniStat("Libres",   String.valueOf(libres),   "#1b5e20", "#e8f5e9"),
            miniStat("Occupées", String.valueOf(occupees), "#b71c1c", "#ffebee"),
            miniStat("Amphi",    String.valueOf(amphi),    "#e65100", "#fff3e0")
        );
        return stats;
    }

    private HBox construireBarreOutils() {
        HBox barre = new HBox(12);
        barre.setAlignment(Pos.CENTER_LEFT);

        TextField recherche = new TextField();
        recherche.setPromptText("🔍  Rechercher...");
        recherche.setPrefWidth(260); recherche.setPrefHeight(38);
        recherche.setStyle("-fx-background-radius:8;-fx-border-radius:8;"
                         + "-fx-border-color:#bdbdbd;-fx-padding:0 10 0 10;");
        recherche.textProperty().addListener((o, a, n) -> filtrer(n));

        ComboBox<String> filtreType = new ComboBox<>();
        filtreType.getItems().addAll("Tous les types","TD","TP","Amphi","Réunion");
        filtreType.setValue("Tous les types");
        filtreType.setPrefHeight(38);
        filtreType.setOnAction(e -> filtrerType(filtreType.getValue()));

        ComboBox<String> filtreDispo = new ComboBox<>();
        filtreDispo.getItems().addAll("Toutes","Disponibles","Occupées");
        filtreDispo.setValue("Toutes");
        filtreDispo.setPrefHeight(38);
        filtreDispo.setOnAction(e -> filtrerDispo(filtreDispo.getValue()));

        Region espace = new Region();
        HBox.setHgrow(espace, Priority.ALWAYS);

        Button btnAjouter = bouton("➕  Ajouter une salle", "#1a237e", "#283593");
        btnAjouter.setOnAction(e -> ouvrirFormulaire(null));

        barre.getChildren().addAll(recherche, filtreType, filtreDispo, espace, btnAjouter);
        return barre;
    }

    @SuppressWarnings("unchecked")
    private TableView<Salle> construireTableau() {
        tableau = new TableView<>();
        tableau.setItems(listeSalles);
        tableau.setPrefHeight(400);
        tableau.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableau.setPlaceholder(new Label("Aucune salle trouvée"));

        // Numéro
        TableColumn<Salle, String> colNum = new TableColumn<>("Numéro");
        colNum.setCellValueFactory(new PropertyValueFactory<>("numero"));
        colNum.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean e) {
                super.updateItem(v, e);
                if (e || v == null) setText(null);
                else { setText(v);
                    setFont(Font.font("Arial", FontWeight.BOLD, 13));
                    setTextFill(Color.web("#1a237e")); }
            }
        });

        // Type (badge)
        TableColumn<Salle, TypeSalle> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colType.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(TypeSalle v, boolean e) {
                super.updateItem(v, e);
                if (e || v == null) { setGraphic(null); return; }
                Label b = new Label(v.getLibelle());
                b.setFont(Font.font("Arial", FontWeight.BOLD, 11));
                b.setTextFill(Color.WHITE);
                b.setPadding(new Insets(3, 10, 3, 10));
                b.setStyle("-fx-background-color:" + switch(v) {
                    case TD -> "#1565c0"; case TP -> "#2e7d32";
                    case AMPHI -> "#6a1b9a"; case REUNION -> "#e65100";
                } + ";-fx-background-radius:20;");
                setGraphic(b); setText(null);
            }
        });

        // Capacité
        TableColumn<Salle, Integer> colCap = new TableColumn<>("Capacité");
        colCap.setCellValueFactory(new PropertyValueFactory<>("capacite"));
        colCap.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Integer v, boolean e) {
                super.updateItem(v, e);
                setText(e || v == null ? null : v + " places");
            }
        });

        // Statut
        TableColumn<Salle, Boolean> colDispo = new TableColumn<>("Statut");
        colDispo.setCellValueFactory(new PropertyValueFactory<>("disponible"));
        colDispo.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Boolean v, boolean e) {
                super.updateItem(v, e);
                if (e || v == null) { setGraphic(null); return; }
                Label l = new Label(v ? "● Disponible" : "● Occupée");
                l.setFont(Font.font("Arial", FontWeight.BOLD, 11));
                l.setTextFill(v ? Color.web("#2e7d32") : Color.web("#c62828"));
                setGraphic(l); setText(null);
            }
        });

        // Actions
        TableColumn<Salle, Void> colAct = new TableColumn<>("Actions");
        colAct.setCellFactory(c -> new TableCell<>() {
            Button btnM = new Button("✏️ Modifier");
            Button btnS = new Button("🗑️ Supprimer");
            HBox box = new HBox(6, btnM, btnS);
            {
                btnM.setStyle("-fx-background-color:#e3f2fd;-fx-text-fill:#1565c0;"
                            + "-fx-background-radius:6;-fx-cursor:hand;-fx-font-size:11;");
                btnS.setStyle("-fx-background-color:#ffebee;-fx-text-fill:#c62828;"
                            + "-fx-background-radius:6;-fx-cursor:hand;-fx-font-size:11;");
                box.setAlignment(Pos.CENTER);
                btnM.setOnAction(e -> ouvrirFormulaire(
                        getTableView().getItems().get(getIndex())));
                btnS.setOnAction(e -> supprimerSalle(
                        getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void v, boolean e) {
                super.updateItem(v, e);
                setGraphic(e ? null : box);
            }
        });

        tableau.getColumns().addAll(colNum, colType, colCap, colDispo, colAct);
        return tableau;
    }

    // ════════════════════════════════════════════════════════════
    //  FORMULAIRE AJOUT / MODIFICATION
    // ════════════════════════════════════════════════════════════

    private void ouvrirFormulaire(Salle s) {
        boolean modif = s != null;
        Stage popup = new Stage();
        popup.setTitle(modif ? "Modifier " + s.getNumero() : "Nouvelle salle");
        popup.initModality(Modality.APPLICATION_MODAL);

        VBox form = new VBox(14);
        form.setPadding(new Insets(24));
        form.setStyle("-fx-background-color:white;");

        Label titre = new Label(modif ? "✏️  Modifier la salle" : "➕  Nouvelle salle");
        titre.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        titre.setTextFill(Color.web("#1a237e"));

        TextField fNum = champ("Numéro (ex: A101)", modif ? s.getNumero() : "");
        TextField fCap = champ("Capacité (ex: 40)", modif ? String.valueOf(s.getCapacite()) : "");

        ComboBox<TypeSalle> fType = new ComboBox<>();
        fType.getItems().addAll(TypeSalle.values());
        fType.setValue(modif ? s.getType() : TypeSalle.TD);
        fType.setMaxWidth(Double.MAX_VALUE);

        CheckBox fDispo = new CheckBox("Salle disponible");
        fDispo.setSelected(modif ? s.isDisponible() : true);

        Label erreur = new Label("");
        erreur.setTextFill(Color.web("#c62828"));

        Button btnOk = bouton(modif ? "💾  Enregistrer" : "➕  Ajouter", "#1a237e", "#283593");
        btnOk.setOnAction(e -> {
            if (fNum.getText().isEmpty()) {
                erreur.setText("⚠  Numéro obligatoire."); return;
            }
            try {
                int cap = Integer.parseInt(fCap.getText());
                if (modif) {
                    s.setNumero(fNum.getText()); s.setCapacite(cap);
                    s.setType(fType.getValue()); s.setDisponible(fDispo.isSelected());
                    salleDAO.modifier(s);     // ← MySQL UPDATE
                    tableau.refresh();
                } else {
                    Salle n = new Salle(fNum.getText(), cap, fType.getValue(), 1);
                    n.setDisponible(fDispo.isSelected());
                    salleDAO.ajouter(n);      // ← MySQL INSERT
                    toutesLesSalles.add(n);
                    listeSalles.add(n);
                }
                popup.close();
            } catch (NumberFormatException ex) {
                erreur.setText("⚠  Capacité invalide.");
            }
        });

        Button btnAnnuler = new Button("Annuler");
        btnAnnuler.setStyle("-fx-background-color:#f5f5f5;-fx-background-radius:8;"
                          + "-fx-cursor:hand;");
        btnAnnuler.setOnAction(e -> popup.close());

        HBox boutons = new HBox(10, btnAnnuler, btnOk);
        boutons.setAlignment(Pos.CENTER_RIGHT);

        form.getChildren().addAll(titre,
            new Label("Numéro :"), fNum,
            new Label("Capacité :"), fCap,
            new Label("Type :"), fType,
            fDispo, erreur, boutons);

        popup.setScene(new Scene(form, 400, 360));
        popup.show();
    }

    private void supprimerSalle(Salle s) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Supprimer"); a.setHeaderText("Supprimer " + s.getNumero() + " ?");
        a.setContentText("Action irréversible.");
        a.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                salleDAO.supprimer(s.getId());  // ← MySQL DELETE
                toutesLesSalles.remove(s);
                listeSalles.remove(s);
            }
        });
    }

    // ════════════════════════════════════════════════════════════
    //  FILTRES
    // ════════════════════════════════════════════════════════════

    private void filtrer(String t) {
        if (t == null || t.isEmpty()) { listeSalles.setAll(toutesLesSalles); return; }
        String r = t.toLowerCase();
        listeSalles.setAll(toutesLesSalles.stream().filter(s ->
            s.getNumero().toLowerCase().contains(r)
         || s.getType().getLibelle().toLowerCase().contains(r)).toList());
    }

    private void filtrerType(String type) {
        if (type.equals("Tous les types")) { listeSalles.setAll(toutesLesSalles); return; }
        listeSalles.setAll(toutesLesSalles.stream().filter(s ->
            s.getType().getLibelle().equals(type)
         || s.getType().name().equals(type)).toList());
    }

    private void filtrerDispo(String v) {
        switch (v) {
            case "Disponibles" ->
                listeSalles.setAll(toutesLesSalles.stream()
                    .filter(Salle::isDisponible).toList());
            case "Occupées" ->
                listeSalles.setAll(toutesLesSalles.stream()
                    .filter(s -> !s.isDisponible()).toList());
            default -> listeSalles.setAll(toutesLesSalles);
        }
    }

    // ════════════════════════════════════════════════════════════
    //  UTILITAIRES
    // ════════════════════════════════════════════════════════════

    private VBox miniStat(String label, String valeur, String ct, String cf) {
        VBox c = new VBox(4);
        c.setPadding(new Insets(12, 20, 12, 20));
        c.setAlignment(Pos.CENTER);
        c.setStyle("-fx-background-color:" + cf + ";-fx-background-radius:10;");
        Label v = new Label(valeur);
        v.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 22));
        v.setTextFill(Color.web(ct));
        Label l = new Label(label);
        l.setFont(Font.font("Arial", 11)); l.setTextFill(Color.web("#616161"));
        c.getChildren().addAll(v, l);
        return c;
    }

    private TextField champ(String placeholder, String valeur) {
        TextField t = new TextField(valeur);
        t.setPromptText(placeholder);
        t.setStyle("-fx-background-color:white;-fx-border-color:#bdbdbd;"
                 + "-fx-border-radius:8;-fx-background-radius:8;-fx-padding:8 12 8 12;");
        return t;
    }

    private Button bouton(String texte, String bg, String hover) {
        Button b = new Button(texte);
        b.setPrefHeight(38);
        b.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        b.setStyle("-fx-background-color:" + bg + ";-fx-text-fill:white;"
                 + "-fx-background-radius:8;-fx-cursor:hand;");
        b.setOnMouseEntered(e -> b.setStyle("-fx-background-color:" + hover
                + ";-fx-text-fill:white;-fx-background-radius:8;-fx-cursor:hand;"));
        b.setOnMouseExited(e -> b.setStyle("-fx-background-color:" + bg
                + ";-fx-text-fill:white;-fx-background-radius:8;-fx-cursor:hand;"));
        return b;
    }
}
