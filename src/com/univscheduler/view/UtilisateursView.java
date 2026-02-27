package com.univscheduler.view;

import com.univscheduler.dao.UtilisateurDAO;
import com.univscheduler.model.*;
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
 * Écran de gestion des utilisateurs — données venant de MySQL via UtilisateurDAO.
 */
public class UtilisateursView {

    // ── DAO ──────────────────────────────────────────────────────
    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();

    // ── Données ──────────────────────────────────────────────────
    private ObservableList<Utilisateur> listeUtilisateurs;
    private List<Utilisateur> tousLesUtilisateurs;

    // ── Composants ───────────────────────────────────────────────
    private TableView<Utilisateur> tableau;

    // ── Constructeur ─────────────────────────────────────────────
    public UtilisateursView() {
        // ← Données depuis MySQL
        tousLesUtilisateurs = utilisateurDAO.getTous();
        listeUtilisateurs   = FXCollections.observableArrayList(tousLesUtilisateurs);
    }

    // ════════════════════════════════════════════════════════════
    //  INTERFACE
    // ════════════════════════════════════════════════════════════

    public VBox getVue() {
        VBox vue = new VBox(20);
        vue.setPadding(new Insets(10, 0, 0, 0));

        Label titre = new Label("👥  Gestion des Utilisateurs");
        titre.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        titre.setTextFill(Color.web("#1a237e"));

        vue.getChildren().addAll(titre, construireStats(),
                construireBarreOutils(), construireTableau());
        return vue;
    }

    private HBox construireStats() {
        HBox stats = new HBox(12);
        long admins  = tousLesUtilisateurs.stream().filter(u -> u.getRole().equals("ADMIN")).count();
        long gests   = tousLesUtilisateurs.stream().filter(u -> u.getRole().equals("GESTIONNAIRE")).count();
        long ens     = tousLesUtilisateurs.stream().filter(u -> u.getRole().equals("ENSEIGNANT")).count();
        long etu     = tousLesUtilisateurs.stream().filter(u -> u.getRole().equals("ETUDIANT")).count();
        stats.getChildren().addAll(
            miniStat("Total",         String.valueOf(tousLesUtilisateurs.size()), "#1a237e", "#e8eaf6"),
            miniStat("Admins",        String.valueOf(admins), "#b71c1c", "#ffebee"),
            miniStat("Gestionnaires", String.valueOf(gests),  "#e65100", "#fff3e0"),
            miniStat("Enseignants",   String.valueOf(ens),    "#1b5e20", "#e8f5e9"),
            miniStat("Étudiants",     String.valueOf(etu),    "#4a148c", "#f3e5f5")
        );
        return stats;
    }

    private HBox construireBarreOutils() {
        HBox barre = new HBox(12);
        barre.setAlignment(Pos.CENTER_LEFT);

        TextField recherche = new TextField();
        recherche.setPromptText("🔍  Rechercher par nom, email...");
        recherche.setPrefWidth(260); recherche.setPrefHeight(38);
        recherche.setStyle("-fx-background-radius:8;-fx-border-radius:8;"
                         + "-fx-border-color:#bdbdbd;-fx-padding:0 10 0 10;");
        recherche.textProperty().addListener((o, a, n) -> filtrer(n));

        ComboBox<String> filtreRole = new ComboBox<>();
        filtreRole.getItems().addAll("Tous les rôles","ADMIN","GESTIONNAIRE","ENSEIGNANT","ETUDIANT");
        filtreRole.setValue("Tous les rôles");
        filtreRole.setPrefHeight(38);
        filtreRole.setOnAction(e -> filtrerRole(filtreRole.getValue()));

        Region espace = new Region();
        HBox.setHgrow(espace, Priority.ALWAYS);

        Button btnAjouter = bouton("➕  Ajouter un utilisateur", "#1a237e", "#283593");
        btnAjouter.setOnAction(e -> ouvrirFormulaire(null));

        barre.getChildren().addAll(recherche, filtreRole, espace, btnAjouter);
        return barre;
    }

    @SuppressWarnings("unchecked")
    private TableView<Utilisateur> construireTableau() {
        tableau = new TableView<>();
        tableau.setItems(listeUtilisateurs);
        tableau.setPrefHeight(420);
        tableau.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableau.setPlaceholder(new Label("Aucun utilisateur trouvé"));

        // Nom
        TableColumn<Utilisateur, String> colNom = new TableColumn<>("Nom");
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colNom.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean e) {
                super.updateItem(v, e);
                if (e || v == null) setText(null);
                else {
                    Utilisateur u = getTableView().getItems().get(getIndex());
                    setText(u.getNomComplet());
                    setFont(Font.font("Arial", FontWeight.BOLD, 12));
                }
            }
        });

        // Email
        TableColumn<Utilisateur, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colEmail.setMinWidth(180);

        // Rôle (badge)
        TableColumn<Utilisateur, String> colRole = new TableColumn<>("Rôle");
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colRole.setMinWidth(130);
        colRole.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean e) {
                super.updateItem(v, e);
                if (e || v == null) { setGraphic(null); return; }
                String[] cfg = switch (v) {
                    case "ADMIN"        -> new String[]{"Administrateur","#b71c1c","#ffebee"};
                    case "GESTIONNAIRE" -> new String[]{"Gestionnaire",  "#e65100","#fff3e0"};
                    case "ENSEIGNANT"   -> new String[]{"Enseignant",    "#1b5e20","#e8f5e9"};
                    case "ETUDIANT"     -> new String[]{"Étudiant",      "#4a148c","#f3e5f5"};
                    default             -> new String[]{v,               "#424242","#f5f5f5"};
                };
                Label b = new Label(cfg[0]);
                b.setFont(Font.font("Arial", FontWeight.BOLD, 11));
                b.setTextFill(Color.web(cfg[1]));
                b.setPadding(new Insets(3, 10, 3, 10));
                b.setStyle("-fx-background-color:" + cfg[2] + ";-fx-background-radius:20;");
                setGraphic(b); setText(null);
            }
        });

        // Actions
        TableColumn<Utilisateur, Void> colAct = new TableColumn<>("Actions");
        colAct.setMinWidth(180);
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
                btnS.setOnAction(e -> supprimerUtilisateur(
                        getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void v, boolean e) {
                super.updateItem(v, e);
                setGraphic(e ? null : box);
            }
        });

        tableau.getColumns().addAll(colNom, colEmail, colRole, colAct);
        return tableau;
    }

    // ════════════════════════════════════════════════════════════
    //  FORMULAIRE
    // ════════════════════════════════════════════════════════════

    private void ouvrirFormulaire(Utilisateur existant) {
        boolean modif = existant != null;
        Stage popup = new Stage();
        popup.setTitle(modif ? "Modifier " + existant.getNomComplet() : "Nouvel utilisateur");
        popup.initModality(Modality.APPLICATION_MODAL);

        VBox form = new VBox(14);
        form.setPadding(new Insets(24));
        form.setStyle("-fx-background-color:white;");

        Label titre = new Label(modif ? "✏️  Modifier" : "➕  Nouvel utilisateur");
        titre.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        titre.setTextFill(Color.web("#1a237e"));

        TextField fNom    = champ("Nom",    modif ? existant.getNom()    : "");
        TextField fPrenom = champ("Prénom", modif ? existant.getPrenom() : "");
        TextField fEmail  = champ("Email",  modif ? existant.getEmail()  : "");
        PasswordField fMdp = new PasswordField();
        fMdp.setPromptText("Mot de passe");
        fMdp.setStyle("-fx-background-color:white;-fx-border-color:#bdbdbd;"
                    + "-fx-border-radius:8;-fx-background-radius:8;-fx-padding:8 12 8 12;");

        ComboBox<String> fRole = new ComboBox<>();
        fRole.getItems().addAll("ADMIN","GESTIONNAIRE","ENSEIGNANT","ETUDIANT");
        fRole.setValue(modif ? existant.getRole() : "ETUDIANT");
        fRole.setMaxWidth(Double.MAX_VALUE);

        TextField fSpecialite = champ("Spécialité (Enseignant)", "");
        TextField fClasse     = champ("Classe (Étudiant)", "");
        TextField fGroupe     = champ("Groupe (Étudiant)", "");

        majChamps(fRole.getValue(), fSpecialite, fClasse, fGroupe);
        fRole.setOnAction(e -> majChamps(fRole.getValue(), fSpecialite, fClasse, fGroupe));

        Label erreur = new Label("");
        erreur.setTextFill(Color.web("#c62828"));

        Button btnOk = bouton(modif ? "💾  Enregistrer" : "➕  Ajouter", "#1a237e", "#283593");
        btnOk.setOnAction(e -> {
            if (fNom.getText().isEmpty() || fEmail.getText().isEmpty()) {
                erreur.setText("⚠  Nom et email obligatoires."); return;
            }
            if (modif) {
                existant.setNom(fNom.getText());
                existant.setPrenom(fPrenom.getText());
                existant.setEmail(fEmail.getText());
                if (!fMdp.getText().isEmpty()) existant.setMotDePasse(fMdp.getText());
                utilisateurDAO.modifier(existant);  // ← MySQL UPDATE
                tableau.refresh();
            } else {
                Utilisateur nouveau = switch (fRole.getValue()) {
                    case "ADMIN"        -> new Administrateur(
                            fNom.getText(), fPrenom.getText(), fEmail.getText(), fMdp.getText());
                    case "GESTIONNAIRE" -> new GestionnaireEmploiDuTemps(
                            fNom.getText(), fPrenom.getText(), fEmail.getText(), fMdp.getText(), "");
                    case "ENSEIGNANT"   -> new Enseignant(
                            fNom.getText(), fPrenom.getText(), fEmail.getText(), fMdp.getText(),
                            fSpecialite.getText(), "UFR");
                    default             -> new Etudiant(
                            fNom.getText(), fPrenom.getText(), fEmail.getText(), fMdp.getText(),
                            fClasse.getText(), fGroupe.getText(),
                            "ETU" + (tousLesUtilisateurs.size() + 1));
                };
                utilisateurDAO.ajouter(nouveau);    // ← MySQL INSERT
                tousLesUtilisateurs.add(nouveau);
                listeUtilisateurs.add(nouveau);
            }
            popup.close();
        });

        Button btnAnnuler = new Button("Annuler");
        btnAnnuler.setStyle("-fx-background-color:#f5f5f5;-fx-background-radius:8;-fx-cursor:hand;");
        btnAnnuler.setOnAction(e -> popup.close());

        HBox boutons = new HBox(10, btnAnnuler, btnOk);
        boutons.setAlignment(Pos.CENTER_RIGHT);

        form.getChildren().addAll(titre,
            new Label("Nom :"),      fNom,
            new Label("Prénom :"),   fPrenom,
            new Label("Email :"),    fEmail,
            new Label("Mot de passe :"), fMdp,
            new Label("Rôle :"),     fRole,
            fSpecialite, fClasse, fGroupe,
            erreur, boutons);

        popup.setScene(new Scene(form, 420, 500));
        popup.show();
    }

    private void supprimerUtilisateur(Utilisateur u) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Supprimer"); a.setHeaderText("Supprimer " + u.getNomComplet() + " ?");
        a.setContentText("Action irréversible.");
        a.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                utilisateurDAO.supprimer(u.getId()); // ← MySQL DELETE
                tousLesUtilisateurs.remove(u);
                listeUtilisateurs.remove(u);
            }
        });
    }

    // ════════════════════════════════════════════════════════════
    //  FILTRES
    // ════════════════════════════════════════════════════════════

    private void filtrer(String t) {
        if (t == null || t.isEmpty()) { listeUtilisateurs.setAll(tousLesUtilisateurs); return; }
        String r = t.toLowerCase();
        listeUtilisateurs.setAll(tousLesUtilisateurs.stream().filter(u ->
            u.getNomComplet().toLowerCase().contains(r)
         || u.getEmail().toLowerCase().contains(r)).toList());
    }

    private void filtrerRole(String role) {
        if (role.equals("Tous les rôles")) { listeUtilisateurs.setAll(tousLesUtilisateurs); return; }
        listeUtilisateurs.setAll(tousLesUtilisateurs.stream()
            .filter(u -> u.getRole().equals(role)).toList());
    }

    // ════════════════════════════════════════════════════════════
    //  UTILITAIRES
    // ════════════════════════════════════════════════════════════

    private void majChamps(String role, TextField spec, TextField classe, TextField groupe) {
        spec.setVisible(role.equals("ENSEIGNANT")); spec.setManaged(role.equals("ENSEIGNANT"));
        classe.setVisible(role.equals("ETUDIANT")); classe.setManaged(role.equals("ETUDIANT"));
        groupe.setVisible(role.equals("ETUDIANT")); groupe.setManaged(role.equals("ETUDIANT"));
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

    private TextField champ(String placeholder, String valeur) {
        TextField t = new TextField(valeur); t.setPromptText(placeholder);
        t.setStyle("-fx-background-color:white;-fx-border-color:#bdbdbd;"
                 + "-fx-border-radius:8;-fx-background-radius:8;-fx-padding:8 12 8 12;");
        return t;
    }

    private Button bouton(String texte, String bg, String hover) {
        Button b = new Button(texte); b.setPrefHeight(38);
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
