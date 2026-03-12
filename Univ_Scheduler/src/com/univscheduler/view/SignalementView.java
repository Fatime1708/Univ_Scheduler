package com.univscheduler.view;


import com.univscheduler.dao.EquipementDAO;
import com.univscheduler.dao.NotificationDAO;
import com.univscheduler.dao.SalleDAO;
import com.univscheduler.dao.SignalementDAO;
import com.univscheduler.model.Enseignant;
import com.univscheduler.model.Equipement;
import com.univscheduler.model.Salle;
import com.univscheduler.dao.ConflitDAO;
import com.univscheduler.dao.CoursDAO;
import com.univscheduler.model.Cours;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.List;

public class SignalementView {

    private final SalleDAO salleDAO = new SalleDAO();
    private final EquipementDAO equipementDAO = new EquipementDAO();
    private final SignalementDAO signalementDAO = new SignalementDAO();

    public VBox getVue() {
        VBox vue = new VBox(20);
        vue.setPadding(new Insets(10, 0, 0, 0));

        Label titre = new Label("🔧  Signaler un problème technique");
        titre.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        titre.setTextFill(Color.web("#e65100"));

        vue.getChildren().addAll(titre, construireFormulaire());
        return vue;
    }


    private VBox construireFormulaire() {
        VBox form = new VBox(14);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color:white;"
                    + "-fx-background-radius:12;"
                    + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),8,0,0,2);");

        // Type de problème
        Label lblType = new Label("Type de problème :");
        lblType.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        ComboBox<String> comboType = new ComboBox<>();
        comboType.getItems().addAll(
            "🔌 Équipement en panne",
            "🚫 Salle indisponible",
            "⚡ Problème électrique",
            "🌊 Dégât des eaux / Travaux",
            "🔒 Autre problème"
        );
        comboType.setPromptText("Choisir le type de problème");
        comboType.setMaxWidth(Double.MAX_VALUE);

        // Salle concernée
        Label lblSalle = new Label("Salle concernée :");
        lblSalle.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        ComboBox<Salle> comboSalle = new ComboBox<>();
        comboSalle.getItems().addAll(salleDAO.getTous());
        comboSalle.setPromptText("Choisir la salle");
        comboSalle.setMaxWidth(Double.MAX_VALUE);
        comboSalle.setConverter(new javafx.util.StringConverter<Salle>() {
            @Override public String toString(Salle s) {
                return s == null ? "" : s.getNumero() + " — " + s.getType();
            }
            @Override public Salle fromString(String s) { return null; }
        });

        // Équipement concerné (optionnel)
        Label lblEquip = new Label("Équipement concerné (optionnel) :");
        lblEquip.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        ComboBox<Equipement> comboEquip = new ComboBox<>();
        comboEquip.setPromptText("Choisir un équipement (si applicable)");
        comboEquip.setMaxWidth(Double.MAX_VALUE);
        comboEquip.setConverter(new javafx.util.StringConverter<Equipement>() {
            @Override public String toString(Equipement e) {
                return e == null ? "" : e.getNom() + " — " + e.getDescription();
            }
            @Override public Equipement fromString(String s) { return null; }
        });

        // Charger équipements quand salle change
        comboSalle.setOnAction(e -> {
            comboEquip.getItems().clear();
            Salle salle = comboSalle.getValue();
            if (salle == null) return;
            List<Equipement> equips = equipementDAO.getBySalle(salle.getId());
            comboEquip.getItems().addAll(equips);
        });

        // Description
        Label lblDesc = new Label("Description du problème :");
        lblDesc.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        TextArea taDesc = new TextArea();
        taDesc.setPromptText("Décrivez le problème en détail...");
        taDesc.setPrefRowCount(4);
        taDesc.setWrapText(true);
        taDesc.setStyle("-fx-background-color:white;-fx-border-color:#bdbdbd;"
                      + "-fx-border-radius:8;-fx-background-radius:8;");

        Label msgErreur = new Label("");
        msgErreur.setTextFill(Color.web("#c62828"));

        Label msgSucces = new Label("");
        msgSucces.setTextFill(Color.web("#2e7d32"));
        msgSucces.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        Button btnEnvoyer = new Button("📤  Envoyer le signalement");
        btnEnvoyer.setMaxWidth(Double.MAX_VALUE);
        btnEnvoyer.setPrefHeight(42);
        btnEnvoyer.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        btnEnvoyer.setStyle("-fx-background-color:#e65100;-fx-text-fill:white;"
                          + "-fx-background-radius:8;-fx-cursor:hand;");

        btnEnvoyer.setOnAction(e -> {
            if (comboType.getValue() == null
                    || comboSalle.getValue() == null
                    || taDesc.getText().trim().isEmpty()) {
                msgErreur.setText("⚠  Veuillez remplir tous les champs obligatoires.");
                msgSucces.setText("");
                return;
            }

            Salle salle = comboSalle.getValue();
            Equipement equip = comboEquip.getValue();
            Enseignant ens = (Enseignant) DashboardView.utilisateurConnecte;

            boolean ok = signalementDAO.ajouter(
                comboType.getValue(),
                taDesc.getText().trim(),
                salle.getId(),
                ens.getId(),
                equip != null ? equip.getId() : null
            );


            if (ok) {
                msgSucces.setText("✅  Signalement envoyé avec succès !");
                msgErreur.setText("");

                // ── Notifier admin et gestionnaire ──
                NotificationDAO notifDAO = new NotificationDAO();
                notifDAO.ajouter(
                    "🔧 Nouveau signalement",
                    "L'enseignant " + ens.getNomComplet()
                        + " a signalé un problème dans la salle "
                        + salle.getNumero(),
                    "SIGNALEMENT", "ADMIN", null);
                notifDAO.ajouter(
                    "🔧 Nouveau signalement",
                    "Un problème a été signalé dans la salle " + salle.getNumero(),
                    "SIGNALEMENT", "GESTIONNAIRE", null);

                // ── Marquer équipement en panne si applicable ──
                if (equip != null) {
                    equip.setFonctionnel(false);
                }

                // ── Remettre à zéro le formulaire ──
                comboType.setValue(null);
                comboSalle.setValue(null);
                comboEquip.getItems().clear();
                taDesc.clear();
            }
        });

        form.getChildren().addAll(
            lblType, comboType,
            lblSalle, comboSalle,
            lblEquip, comboEquip,
            lblDesc, taDesc,
            msgErreur, msgSucces,
            btnEnvoyer
        );
        return form;
    }
}
