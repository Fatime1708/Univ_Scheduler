
package com.univscheduler.dao;

import com.univscheduler.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    // ── Ajouter une notification ─────────────────────────────────
    public boolean ajouter(String titre, String message, String type,
                            String destinataireRole, Integer destinataireId) {
        String sql = "INSERT INTO notifications "
                   + "(titre, message, type, destinataire_role, destinataire_id) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, titre);
            ps.setString(2, message);
            ps.setString(3, type);
            ps.setString(4, destinataireRole);
            if (destinataireId != null) ps.setInt(5, destinataireId);
            else ps.setNull(5, Types.INTEGER);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur ajouter() notification : " + e.getMessage());
        }
        return false;
    }

    // ── Récupérer les notifications d'un utilisateur ─────────────
    public List<String[]> getParUtilisateur(int userId, String role) {
        List<String[]> liste = new ArrayList<>();
        String sql = "SELECT id, titre, message, type, lu, date_creation "
                   + "FROM notifications "
                   + "WHERE (destinataire_id = ? OR destinataire_role = ? "
                   + "OR destinataire_role = 'TOUS') "
                   + "ORDER BY date_creation DESC LIMIT 20";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, role);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                liste.add(new String[]{
                    rs.getString("id"),
                    rs.getString("titre"),
                    rs.getString("message"),
                    rs.getString("type"),
                    rs.getString("lu"),
                    rs.getString("date_creation")
                });
            }
        } catch (SQLException e) {
            System.err.println("Erreur getParUtilisateur() : " + e.getMessage());
        }
        return liste;
    }

    // ── Compter les notifications non lues ───────────────────────
    public int compterNonLues(int userId, String role) {
        String sql = "SELECT COUNT(*) FROM notifications "
                   + "WHERE (destinataire_id = ? OR destinataire_role = ? "
                   + "OR destinataire_role = 'TOUS') AND lu = 0";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, role);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Erreur compterNonLues() : " + e.getMessage());
        }
        return 0;
    }

    // ── Marquer une notification comme lue ───────────────────────
    public void marquerLue(int id) {
        String sql = "UPDATE notifications SET lu = 1 WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur marquerLue() : " + e.getMessage());
        }
    }

    // ── Marquer toutes comme lues ────────────────────────────────
    public void marquerToutesLues(int userId, String role) {
        String sql = "UPDATE notifications SET lu = 1 "
                   + "WHERE destinataire_id = ? OR destinataire_role = ? "
                   + "OR destinataire_role = 'TOUS'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, role);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur marquerToutesLues() : " + e.getMessage());
        }
    }
}

