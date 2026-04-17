package com.univscheduler.view;

import com.univscheduler.dao.SalleDAO;
import com.univscheduler.dao.EquipementDAO;
import com.univscheduler.model.Administrateur;
import com.univscheduler.model.GestionnaireEmploiDuTemps;
import com.univscheduler.dao.CoursDAO;
import com.univscheduler.model.Cours;
import com.univscheduler.model.Salle;
import com.univscheduler.model.enums.TypeSalle;
import com.univscheduler.model.Enseignant;
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
import com.univscheduler.model.Etudiant;
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
        toutesLesSalles = salleDAO.getTous();
        
        // Mettre à jour le statut des salles selon les cours
        CoursDAO coursDAO = new CoursDAO();
        List<Cours> tousLesCours = coursDAO.getTous();
        
        for (Salle salle : toutesLesSalles) {
            boolean occupee = tousLesCours.stream()
                .anyMatch(c -> c.getSalle() != null 
                    && c.getSalle().getId() == salle.getId());
            salle.setDisponible(!occupee);
        }
        
        listeSalles = FXCollections.observableArrayList(toutesLesSalles);
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

     // Bouton Admin — Ajouter une salle
        boolean estAdmin = DashboardView.utilisateurConnecte instanceof Administrateur;
        boolean estGestionnaire = DashboardView.utilisateurConnecte instanceof GestionnaireEmploiDuTemps;

        if (estAdmin) {
            Button btnAjouter = bouton("➕  Ajouter une salle", "#1a237e", "#283593");
            btnAjouter.setOnAction(e -> ouvrirFormulaire(null));
            barre.getChildren().addAll(recherche, filtreType, filtreDispo, espace, btnAjouter);
        } else if (estGestionnaire) {
            Button btnAssigner = bouton("📋  Assigner une salle", "#6a1b9a", "#7b1fa2");
            btnAssigner.setOnAction(e -> ouvrirFormulaireAssignation());
            barre.getChildren().addAll(recherche, filtreType, filtreDispo, espace, btnAssigner);
        } else {
            barre.getChildren().addAll(recherche, filtreType, filtreDispo, espace);
        }
    
        
        
     // Bouton Créer cours — visible uniquement pour l'enseignant
        boolean estEnseignant = DashboardView.utilisateurConnecte instanceof Enseignant;
        if (estEnseignant) {
            Button btnCreerCours = bouton("➕ Créer un cours", "#2e7d32", "#1b5e20");
            btnCreerCours.setOnAction(e -> {
                // Ouvrir le formulaire de création de cours
                EmploiDuTempsView emploiView = new EmploiDuTempsView();
                emploiView.ouvrirFormulaireAjoutCours();
            });
            barre.getChildren().add(btnCreerCours);
        }
        
        return barre;
    }

    @SuppressWarnings("unchecked")
    private TableView<Salle> construireTableau() {
        tableau = new TableView<>();
        tableau.setItems(listeSalles);
        tableau.setPrefHeight(400);
        tableau.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
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
                String couleur;
                if (v == TypeSalle.TD)          couleur = "#1565c0";
                else if (v == TypeSalle.TP)     couleur = "#2e7d32";
                else if (v == TypeSalle.AMPHI)  couleur = "#6a1b9a";
                else                             couleur = "#e65100";
                b.setStyle("-fx-background-color:" + couleur + ";-fx-background-radius:20;");
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
                btnS.setOnAction(e -> {
                    Salle salle = getTableView().getItems().get(getIndex());
                    Alert a = new Alert(Alert.AlertType.CONFIRMATION);
                    a.setTitle("Supprimer");
                    a.setHeaderText("Supprimer " + salle.getNumero() + " ?");
                    a.setContentText("Action irréversible.");
                    a.showAndWait().ifPresent(r -> {
                        if (r == ButtonType.OK) {
                            salleDAO.supprimer(salle.getId());
                            toutesLesSalles.remove(salle);
                            listeSalles.remove(salle);
                            rafraichirStatsDashboard();
                        }
                    });
                });
            }

            @Override protected void updateItem(Void v, boolean e) {
                super.updateItem(v, e);
                if (e) { setGraphic(null); return; }
                boolean peutGerer = DashboardView.utilisateurConnecte instanceof Administrateur
                        || DashboardView.utilisateurConnecte instanceof GestionnaireEmploiDuTemps;
                setGraphic(peutGerer ? box : null);
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

        // ── Section équipements ──────────────────────────────────
        Label lblEquip = new Label("🔧  Équipements de la salle :");
        lblEquip.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        lblEquip.setTextFill(Color.web("#1a237e"));

        // Liste des champs équipements
        List<TextField> champsNom  = new java.util.ArrayList<>();
        List<TextField> champsDesc = new java.util.ArrayList<>();

        // VBox qui contient les lignes + le bouton ajouter
        VBox boxEquipements = new VBox(6);
        boxEquipements.setPadding(new Insets(8));
        boxEquipements.setStyle("-fx-background-color:#f9f9f9;"
                              + "-fx-border-color:#e0e0e0;"
                              + "-fx-border-radius:8;"
                              + "-fx-background-radius:8;");

        // Charger équipements existants si modification
        EquipementDAO equipementDAO = new EquipementDAO();
        List<com.univscheduler.model.Equipement> equipementsExistants =
            modif ? equipementDAO.getBySalle(s.getId()) : new java.util.ArrayList<>();

        for (com.univscheduler.model.Equipement eq : equipementsExistants) {
            HBox ligne = creerLigneEquipement(
                    eq.getNom(), eq.getDescription(),
                    champsNom, champsDesc, boxEquipements);
            boxEquipements.getChildren().add(ligne);
        }

        // Bouton ➕ à l'intérieur de la boxEquipements
        Button btnAjouterEquip = new Button("➕  Ajouter un équipement");
        btnAjouterEquip.setStyle(
                "-fx-background-color:#e8eaf6;-fx-text-fill:#1a237e;"
              + "-fx-background-radius:6;-fx-cursor:hand;-fx-font-size:11;");
        btnAjouterEquip.setOnAction(e -> {
            HBox ligne = creerLigneEquipement(
                    "", "", champsNom, champsDesc, boxEquipements);
            boxEquipements.getChildren().add(ligne);
            popup.sizeToScene();
       
        });

        boxEquipements.getChildren().add(btnAjouterEquip);

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
                    salleDAO.modifier(s);
                    equipementDAO.supprimerBySalle(s.getId());
                    sauvegarderEquipements(champsNom, champsDesc, s.getId(), equipementDAO);
                    tableau.refresh();
                } else {
                    Salle n = new Salle(fNum.getText(), cap, fType.getValue(), 1);
                    n.setDisponible(fDispo.isSelected());
                    boolean ok = salleDAO.ajouter(n); // ← récupérer le résultat

                    if (ok) {
                        com.univscheduler.dao.NotificationDAO notifDAO =
                                new com.univscheduler.dao.NotificationDAO();
                        notifDAO.ajouter(
                            "🏫 Nouvelle salle ajoutée",
                            "La salle " + n.getNumero() + " (" + n.getType() + ") a été ajoutée.",
                            "SALLE", "TOUS", null);
                        sauvegarderEquipements(champsNom, champsDesc, n.getId(), equipementDAO);

                        // ✅ Recharger toute la liste depuis la BDD
                        toutesLesSalles.clear();
                        toutesLesSalles.addAll(salleDAO.getTous());
                        listeSalles.setAll(toutesLesSalles);
                        tableau.refresh();

                        popup.close();

                        Alert succes = new Alert(Alert.AlertType.INFORMATION);
                        succes.setTitle("Succès");
                        succes.setHeaderText(null);
                        succes.setContentText("✅ Salle \"" + n.getNumero() + "\" ajoutée avec succès !");
                        succes.showAndWait();
                    } else {
                        erreur.setText("❌ Erreur lors de l'ajout en base de données.");
                    }
                }
                popup.close();
            } catch (NumberFormatException ex) {
                erreur.setText("⚠  Capacité invalide.");
            }
        });

       

        Button btnAnnuler = new Button("Annuler");
        btnAnnuler.setStyle("-fx-background-color:#f5f5f5;"
                + "-fx-background-radius:8;"
                + "-fx-cursor:hand;");
        btnAnnuler.setOnAction(e -> popup.close());

        HBox boutons = new HBox(10, btnAnnuler, btnOk);
        boutons.setAlignment(Pos.CENTER_RIGHT);

        ScrollPane scrollEquip = new ScrollPane(boxEquipements);
        scrollEquip.setFitToWidth(true);
        scrollEquip.setPrefHeight(150);
        scrollEquip.setStyle("-fx-background-color:transparent;-fx-background:transparent;");

        form.getChildren().addAll(titre,
        	    new Label("Numéro :"), fNum,
        	    new Label("Capacité :"), fCap,
        	    new Label("Type :"), fType,
        	    fDispo,
        	    lblEquip, scrollEquip, btnAjouterEquip,
        	    erreur, boutons);

        popup.setScene(new Scene(form, 440, 520));
        popup.show();
    }

    // ── Crée une ligne nom + description pour un équipement ─────────
    private HBox creerLigneEquipement(String nom, String desc,
            List<TextField> champsNom, List<TextField> champsDesc,
            VBox boxParent) {
        TextField fNom  = champ("Nom (ex: Vidéoprojecteur)", nom);
        TextField fDesc = champ("Description (ex: Epson HD)", desc);
        fNom.setPrefWidth(160);
        fDesc.setPrefWidth(160);

        Button btnSuppr = new Button("✕");
        btnSuppr.setStyle("-fx-background-color:#ffebee;-fx-text-fill:#c62828;"
                        + "-fx-background-radius:6;-fx-cursor:hand;");

        HBox ligne = new HBox(6, fNom, fDesc, btnSuppr);
        ligne.setAlignment(Pos.CENTER_LEFT);

        champsNom.add(fNom);
        champsDesc.add(fDesc);

        btnSuppr.setOnAction(e -> {
            int idx = champsNom.indexOf(fNom);
            if (idx >= 0) { champsNom.remove(idx); champsDesc.remove(idx); }
            boxParent.getChildren().remove(ligne);
        });
        return ligne;
    }

    // ── Sauvegarde les équipements en BDD ───────────────────────────
    private void sauvegarderEquipements(List<TextField> champsNom,
            List<TextField> champsDesc, int salleId, EquipementDAO dao) {
        for (int i = 0; i < champsNom.size(); i++) {
            String nom  = champsNom.get(i).getText().trim();
            String desc = champsDesc.get(i).getText().trim();
            if (!nom.isEmpty()) {
                com.univscheduler.model.Equipement eq =
                    new com.univscheduler.model.Equipement(nom, desc);
                dao.ajouter(eq, salleId);
            }
        }
    }

    // ── Rafraîchit les stats dans le Dashboard ──────────────────────
    private void rafraichirStatsDashboard() {
        toutesLesSalles.clear();
        toutesLesSalles.addAll(salleDAO.getTous());
        listeSalles.setAll(toutesLesSalles);
        tableau.refresh();
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
        b.setOnMouseEntered((javafx.scene.input.MouseEvent e) -> b.setStyle(
                "-fx-background-color:" + hover
              + ";-fx-text-fill:white;-fx-background-radius:8;-fx-cursor:hand;"));
        b.setOnMouseExited((javafx.scene.input.MouseEvent e) -> b.setStyle(
                "-fx-background-color:" + bg
              + ";-fx-text-fill:white;-fx-background-radius:8;-fx-cursor:hand;"));
        return b;
    }
    private void ouvrirFormulaireAssignation() {
        Stage popup = new Stage();
        popup.setTitle("Assigner une salle à un cours");
        popup.initModality(Modality.APPLICATION_MODAL);

        VBox form = new VBox(14);
        form.setPadding(new Insets(24));
        form.setStyle("-fx-background-color:white;");

        Label titre = new Label("📋  Assigner une salle");
        titre.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        titre.setTextFill(Color.web("#6a1b9a"));

        // Choisir le cours
        Label lblCours = new Label("Cours :");
        lblCours.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        CoursDAO coursDAO = new CoursDAO();
        List<Cours> tousLesCours = coursDAO.getTous();
        ComboBox<Cours> comboCours = new ComboBox<>();
        comboCours.getItems().addAll(tousLesCours);
        comboCours.setMaxWidth(Double.MAX_VALUE);
        comboCours.setConverter(new javafx.util.StringConverter<Cours>() {
            @Override public String toString(Cours c) {
                return c == null ? "" : c.getMatiere() + " — " + c.getClasse()
                        + " (" + c.getCreneau().getJour() + " "
                        + c.getCreneau().getHeureDebut() + ")";
            }
            @Override public Cours fromString(String s) { return null; }
        });

        // Choisir la salle
        Label lblSalle = new Label("Salle à assigner :");
        lblSalle.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        ComboBox<Salle> comboSalle = new ComboBox<>();
        comboSalle.getItems().addAll(salleDAO.getTous());
        comboSalle.setMaxWidth(Double.MAX_VALUE);
        comboSalle.setConverter(new javafx.util.StringConverter<Salle>() {
            @Override public String toString(Salle s) {
                return s == null ? "" : s.getNumero()
                        + " — " + s.getType()
                        + " — " + s.getCapacite() + " places"
                        + (s.isDisponible() ? " ✅" : " ❌ Occupée");
            }
            @Override public Salle fromString(String s) { return null; }
        });

        // Zone équipements
        Label lblEquip = new Label("🔧 Équipements :");
        lblEquip.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        VBox boxEquip = new VBox(4);
        boxEquip.setPadding(new Insets(8));
        boxEquip.setStyle("-fx-background-color:#f5f5f5;"
                        + "-fx-background-radius:8;"
                        + "-fx-border-color:#e0e0e0;"
                        + "-fx-border-radius:8;");
        Label lblAucun = new Label("Sélectionnez une salle");
        lblAucun.setTextFill(Color.GRAY);
        boxEquip.getChildren().add(lblAucun);

        // Charger équipements quand salle change
        comboSalle.setOnAction(e -> {
            boxEquip.getChildren().clear();
            Salle salle = comboSalle.getValue();
            if (salle == null) { boxEquip.getChildren().add(lblAucun); return; }
            EquipementDAO equipDAO = new EquipementDAO();
            List<com.univscheduler.model.Equipement> equips = equipDAO.getBySalle(salle.getId());
            if (equips.isEmpty()) {
                Label l = new Label("Aucun équipement enregistré");
                l.setTextFill(Color.GRAY);
                boxEquip.getChildren().add(l);
            } else {
                for (com.univscheduler.model.Equipement eq : equips) {
                    Label l = new Label((eq.isFonctionnel() ? "✅ " : "❌ ")
                            + eq.getNom() + " — " + eq.getDescription());
                    l.setFont(Font.font("Arial", 11));
                    l.setTextFill(eq.isFonctionnel()
                            ? Color.web("#2e7d32") : Color.web("#c62828"));
                    boxEquip.getChildren().add(l);
                }
            }
        });

        Label erreur = new Label("");
        erreur.setTextFill(Color.web("#c62828"));

        Button btnAssigner = bouton("💾  Assigner", "#6a1b9a", "#7b1fa2");
        btnAssigner.setMaxWidth(Double.MAX_VALUE);
        btnAssigner.setOnAction(e -> {
            Cours cours = comboCours.getValue();
            Salle salle = comboSalle.getValue();
            if (cours == null || salle == null) {
                erreur.setText("⚠  Veuillez choisir un cours et une salle.");
                return;
            }
            if (!salle.isDisponible()) {
                erreur.setText("⚠  Cette salle est occupée, choisissez une autre.");
                return;
            }
            // Mettre à jour la salle du cours en BDD
            cours.setSalle(salle);
            boolean ok = coursDAO.modifierSalle(cours.getId(), salle.getId());
            if (ok) {
                popup.close();
                rafraichirStatsDashboard();
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText(null);
                alert.setContentText("✅ Salle " + salle.getNumero()
                        + " assignée au cours " + cours.getMatiere() + " !");
                alert.showAndWait();
            } else {
                erreur.setText("❌ Erreur lors de l'assignation.");
            }
        });

        Button btnAnnuler = new Button("Annuler");
        btnAnnuler.setStyle("-fx-background-color:#f5f5f5;"
                          + "-fx-background-radius:8;-fx-cursor:hand;");
        btnAnnuler.setOnAction(e -> popup.close());

        HBox boutons = new HBox(10, btnAnnuler, btnAssigner);
        boutons.setAlignment(Pos.CENTER_RIGHT);

        form.getChildren().addAll(
            titre,
            lblCours,  comboCours,
            lblSalle,  comboSalle,
            lblEquip,  boxEquip,
            erreur,    boutons);

        popup.setScene(new Scene(form, 480, 480));
        popup.show();
    }
}
