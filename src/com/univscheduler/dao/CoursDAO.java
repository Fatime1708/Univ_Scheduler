package com.univscheduler.dao;

import com.univscheduler.model.*;
import com.univscheduler.model.enums.TypeSalle;
import com.univscheduler.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour les cours.
 * Récupère les cours avec toutes leurs relations
 * (enseignant, salle, créneau).
 */
public class CoursDAO {

    // ── READ : Tous les cours ────────────────────────────────────
    public List<Cours> getTous() {
        List<Cours> liste = new ArrayList<>();

        // Jointure entre cours, utilisateurs (enseignant), salles, créneaux
        String sql = "SELECT c.*, "
                   + "u.nom AS ens_nom, u.prenom AS ens_prenom, "
                   + "u.email AS ens_email, u.mot_de_passe AS ens_mdp, "
                   + "e.specialite, e.departement, "
                   + "s.numero AS salle_num, s.capacite, s.type AS salle_type, "
                   + "s.disponible, s.batiment_id, "
                   + "cr.jour, cr.heure_debut, cr.duree_minutes "
                   + "FROM cours c "
                   + "JOIN utilisateurs u ON c.enseignant_id = u.id "
                   + "LEFT JOIN enseignants e ON u.id = e.id "
                   + "JOIN salles s    ON c.salle_id    = s.id "
                   + "JOIN creneaux cr ON c.creneau_id  = cr.id "
                   + "ORDER BY cr.jour, cr.heure_debut";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {

            while (rs.next()) liste.add(construireCours(rs));

        } catch (SQLException e) {
            System.err.println("Erreur getTous() cours : " + e.getMessage());
        }
        return liste;
    }

    // ── READ : Cours par classe ──────────────────────────────────
    public List<Cours> getParClasse(String classe) {
        List<Cours> liste = new ArrayList<>();
        String sql = "SELECT c.*, "
                   + "u.nom AS ens_nom, u.prenom AS ens_prenom, "
                   + "u.email AS ens_email, u.mot_de_passe AS ens_mdp, "
                   + "e.specialite, e.departement, "
                   + "s.numero AS salle_num, s.capacite, s.type AS salle_type, "
                   + "s.disponible, s.batiment_id, "
                   + "cr.jour, cr.heure_debut, cr.duree_minutes "
                   + "FROM cours c "
                   + "JOIN utilisateurs u ON c.enseignant_id = u.id "
                   + "LEFT JOIN enseignants e ON u.id = e.id "
                   + "JOIN salles s    ON c.salle_id    = s.id "
                   + "JOIN creneaux cr ON c.creneau_id  = cr.id "
                   + "WHERE c.classe = ? "
                   + "ORDER BY cr.jour, cr.heure_debut";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, classe);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(construireCours(rs));

        } catch (SQLException e) {
            System.err.println("Erreur getParClasse() : " + e.getMessage());
        }
        return liste;
    }

    // ── READ : Cours d'un enseignant ─────────────────────────────
    public List<Cours> getParEnseignant(int enseignantId) {
        List<Cours> liste = new ArrayList<>();
        String sql = "SELECT c.*, "
                   + "u.nom AS ens_nom, u.prenom AS ens_prenom, "
                   + "u.email AS ens_email, u.mot_de_passe AS ens_mdp, "
                   + "e.specialite, e.departement, "
                   + "s.numero AS salle_num, s.capacite, s.type AS salle_type, "
                   + "s.disponible, s.batiment_id, "
                   + "cr.jour, cr.heure_debut, cr.duree_minutes "
                   + "FROM cours c "
                   + "JOIN utilisateurs u ON c.enseignant_id = u.id "
                   + "LEFT JOIN enseignants e ON u.id = e.id "
                   + "JOIN salles s    ON c.salle_id    = s.id "
                   + "JOIN creneaux cr ON c.creneau_id  = cr.id "
                   + "WHERE c.enseignant_id = ? "
                   + "ORDER BY cr.jour, cr.heure_debut";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, enseignantId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(construireCours(rs));

        } catch (SQLException e) {
            System.err.println("Erreur getParEnseignant() : " + e.getMessage());
        }
        return liste;
    }

    // ── CREATE : Ajouter un cours ────────────────────────────────
    public boolean ajouter(Cours cours) {
        // D'abord insérer le créneau
        String sqlCreneau = "INSERT INTO creneaux (jour, heure_debut, duree_minutes) "
                          + "VALUES (?, ?, ?)";
        String sqlCours   = "INSERT INTO cours (matiere, description, classe, groupe, "
                          + "enseignant_id, salle_id, creneau_id) VALUES (?,?,?,?,?,?,?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Transaction : tout ou rien

            try {
                // 1. Insérer le créneau
                PreparedStatement psCreneau = conn.prepareStatement(
                        sqlCreneau, Statement.RETURN_GENERATED_KEYS);
                psCreneau.setString(1, cours.getCreneau().getJour());
                psCreneau.setString(2, cours.getCreneau().getHeureDebut().toString());
                psCreneau.setInt(3,    cours.getCreneau().getDureMinutes());
                psCreneau.executeUpdate();

                int creneauId = 0;
                ResultSet cleCreneau = psCreneau.getGeneratedKeys();
                if (cleCreneau.next()) creneauId = cleCreneau.getInt(1);

                // 2. Insérer le cours
                PreparedStatement psCours = conn.prepareStatement(
                        sqlCours, Statement.RETURN_GENERATED_KEYS);
                psCours.setString(1, cours.getMatiere());
                psCours.setString(2, cours.getDescription());
                psCours.setString(3, cours.getClasse());
                psCours.setString(4, cours.getGroupe());
                psCours.setInt(5,    cours.getEnseignant().getId());
                psCours.setInt(6,    cours.getSalle().getId());
                psCours.setInt(7,    creneauId);
                psCours.executeUpdate();

                ResultSet cleCours = psCours.getGeneratedKeys();
                if (cleCours.next()) cours.setId(cleCours.getInt(1));

                conn.commit(); // Valider la transaction
                System.out.println("✓ Cours ajouté : " + cours.getMatiere());
                return true;

            } catch (SQLException e) {
                conn.rollback(); // Annuler si erreur
                System.err.println("Erreur ajouter() cours (rollback) : " + e.getMessage());
            }

        } catch (SQLException e) {
            System.err.println("Erreur connexion ajouter() cours : " + e.getMessage());
        }
        return false;
    }

    // ── DELETE ───────────────────────────────────────────────────
    public boolean supprimer(int id) {
        String sql = "DELETE FROM cours WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur supprimer() cours : " + e.getMessage());
        }
        return false;
    }

    // ── Méthode privée : construire un objet Cours ───────────────
    private Cours construireCours(ResultSet rs) throws SQLException {
        // Construire l'enseignant
        Enseignant ens = new Enseignant(
            rs.getString("ens_nom"),
            rs.getString("ens_prenom"),
            rs.getString("ens_email"),
            rs.getString("ens_mdp"),
            rs.getString("specialite") != null ? rs.getString("specialite") : "",
            rs.getString("departement") != null ? rs.getString("departement") : ""
        );
        ens.setId(rs.getInt("enseignant_id"));

        // Construire la salle
        Salle salle = new Salle(
            rs.getString("salle_num"),
            rs.getInt("capacite"),
            TypeSalle.valueOf(rs.getString("salle_type")),
            rs.getInt("batiment_id")
        );
        salle.setId(rs.getInt("salle_id"));
        salle.setDisponible(rs.getBoolean("disponible"));

        // Construire le créneau
        // heure_debut vient de SQL comme "08:00:00"
        String heureStr = rs.getString("heure_debut");
        String[] parts  = heureStr.split(":");
        LocalTime heureDebut = LocalTime.of(
            Integer.parseInt(parts[0]),
            Integer.parseInt(parts[1])
        );
        Creneau creneau = new Creneau(
            rs.getString("jour"), heureDebut, rs.getInt("duree_minutes"));
        creneau.setId(rs.getInt("creneau_id"));

        // Construire le cours
        Cours cours = new Cours(
            rs.getString("matiere"),
            rs.getString("classe"),
            rs.getString("groupe"),
            ens, salle, creneau
        );
        cours.setId(rs.getInt("id"));
        cours.setDescription(rs.getString("description"));
        return cours;
    }
}