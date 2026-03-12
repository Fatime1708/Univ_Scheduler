
package com.univscheduler.dao;

import com.univscheduler.model.Equipement;
import com.univscheduler.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EquipementDAO {

    // ── Récupérer les équipements d'une salle ────────────────────
    public List<Equipement> getBySalle(int salleId) {
        List<Equipement> liste = new ArrayList<>();
        String sql = "SELECT * FROM equipements WHERE salle_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, salleId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Equipement e = new Equipement(
                    rs.getString("nom"),
                    rs.getString("description"));
                e.setId(rs.getInt("id"));
                e.setFonctionnel(rs.getBoolean("fonctionnel"));
                liste.add(e);
            }
        } catch (SQLException e) {
            System.err.println("Erreur getBySalle() équipements : " + e.getMessage());
        }
        return liste;
    }

    // ── Ajouter un équipement ────────────────────────────────────
    public boolean ajouter(Equipement eq, int salleId) {
        String sql = "INSERT INTO equipements (nom, description, fonctionnel, salle_id) "
                   + "VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, eq.getNom());
            ps.setString(2, eq.getDescription());
            ps.setBoolean(3, eq.isFonctionnel());
            ps.setInt(4, salleId);
            int lignes = ps.executeUpdate();
            if (lignes > 0) {
                ResultSet cle = ps.getGeneratedKeys();
                if (cle.next()) eq.setId(cle.getInt(1));
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erreur ajouter() équipement : " + e.getMessage());
        }
        return false;
    }

    // ── Supprimer tous les équipements d'une salle ───────────────
    public void supprimerBySalle(int salleId) {
        String sql = "DELETE FROM equipements WHERE salle_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, salleId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur supprimerBySalle() : " + e.getMessage());
        }
    }
}