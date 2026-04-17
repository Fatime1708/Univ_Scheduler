package com.univscheduler.dao;

import com.univscheduler.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConflitDAO {

    // ── Détecter et sauvegarder tous les conflits ────────────────
    public void detecterEtSauvegarder() {
        String sql =
            // Conflit salle : deux cours dans la même salle au même moment
            "INSERT IGNORE INTO conflits (description, type, cours1_id, cours2_id) " +
            "SELECT CONCAT('Conflit salle : ', s.numero, ' — ', " +
            "c1.matiere, ' et ', c2.matiere, ' se chevauchent'), " +
            "'SALLE_OCCUPEE', c1.id, c2.id " +
            "FROM cours c1 " +
            "JOIN cours c2 ON c1.salle_id = c2.salle_id AND c1.id < c2.id " +
            "JOIN creneaux cr1 ON c1.creneau_id = cr1.id " +
            "JOIN creneaux cr2 ON c2.creneau_id = cr2.id " +
            "JOIN salles s ON c1.salle_id = s.id " +
            "WHERE cr1.jour = cr2.jour " +
            "AND cr1.heure_debut < ADDTIME(cr2.heure_debut, SEC_TO_TIME(cr2.duree_minutes*60)) " +
            "AND cr2.heure_debut < ADDTIME(cr1.heure_debut, SEC_TO_TIME(cr1.duree_minutes*60)) " +
            "AND c1.resolu = 0 " +
            "UNION ALL " +
            // Conflit enseignant : même enseignant deux cours en même temps
            "SELECT CONCAT('Conflit enseignant : ', u.nom, ' ', u.prenom, " +
            "' — ', c1.matiere, ' et ', c2.matiere, ' se chevauchent'), " +
            "'ENSEIGNANT_INDISPONIBLE', c1.id, c2.id " +
            "FROM cours c1 " +
            "JOIN cours c2 ON c1.enseignant_id = c2.enseignant_id AND c1.id < c2.id " +
            "JOIN creneaux cr1 ON c1.creneau_id = cr1.id " +
            "JOIN creneaux cr2 ON c2.creneau_id = cr2.id " +
            "JOIN utilisateurs u ON c1.enseignant_id = u.id " +
            "WHERE cr1.jour = cr2.jour " +
            "AND cr1.heure_debut < ADDTIME(cr2.heure_debut, SEC_TO_TIME(cr2.duree_minutes*60)) " +
            "AND cr2.heure_debut < ADDTIME(cr1.heure_debut, SEC_TO_TIME(cr1.duree_minutes*60))";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            System.err.println("Erreur detecterEtSauvegarder() : " + e.getMessage());
        }
    }

    // ── Récupérer tous les conflits non résolus ──────────────────
    public List<String[]> getNonResolus() {
        List<String[]> liste = new ArrayList<>();
        String sql = "SELECT c.id, c.type, c.description, c.created_at, " +
                     "co1.matiere as mat1, co2.matiere as mat2 " +
                     "FROM conflits c " +
                     "LEFT JOIN cours co1 ON c.cours1_id = co1.id " +
                     "LEFT JOIN cours co2 ON c.cours2_id = co2.id " +
                     "WHERE c.resolu = 0 ORDER BY c.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                liste.add(new String[]{
                    rs.getString("id"),
                    rs.getString("type"),
                    rs.getString("description"),
                    rs.getString("created_at"),
                    rs.getString("cours1_id"),
                    rs.getString("cours2_id")
                });
            }
        } catch (SQLException e) {
            System.err.println("Erreur getNonResolus() : " + e.getMessage());
        }
        return liste;
    }

    // ── Marquer un conflit comme résolu ──────────────────────────
    public boolean marquerResolu(int id) {
        String sql = "UPDATE conflits SET resolu = 1 WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur marquerResolu() : " + e.getMessage());
        }
        return false;
    }

    public int compterNonResolus() {
        String sql = "SELECT COUNT(*) FROM conflits WHERE resolu = 0";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Erreur compterNonResolus() : " + e.getMessage());
        }
        return 0;
    }
 // ── Ajouter un conflit depuis un signalement ──
    public boolean ajouterDepuisSignalement(String type, String description, Integer coursId) {
        String sql = "INSERT INTO conflits (type, description, cours1_id, resolu, created_at) "
                   + "VALUES (?, ?, ?, 0, NOW())";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, type);
            ps.setString(2, description);
            if (coursId != null) ps.setInt(3, coursId);
            else ps.setNull(3, Types.INTEGER);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur ajouterDepuisSignalement() : " + e.getMessage());
        }
        return false;
    }
}