package com.univscheduler.dao;

import com.univscheduler.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BlocPlusDAO {

    public boolean ajouter(int enseignantId, int etudiantId, String motif) {
        String sql = "INSERT INTO bloc_plus (enseignant_id, etudiant_id, motif) VALUES (?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, enseignantId);
            ps.setInt(2, etudiantId);
            ps.setString(3, motif);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur ajouter bloc_plus : " + e.getMessage());
            return false;
        }
    }

    public boolean supprimer(int id) {
        String sql = "DELETE FROM bloc_plus WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur supprimer bloc_plus : " + e.getMessage());
            return false;
        }
    }

    public List<String[]> getParEnseignant(int enseignantId) {
        List<String[]> liste = new ArrayList<>();
        String sql = "SELECT bp.id, u.nom, u.prenom, u.email, bp.motif, bp.date_ajout "
                   + "FROM bloc_plus bp "
                   + "JOIN utilisateurs u ON u.id = bp.etudiant_id "
                   + "WHERE bp.enseignant_id = ? "
                   + "ORDER BY bp.date_ajout DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, enseignantId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                liste.add(new String[]{
                    String.valueOf(rs.getInt("id")),
                    rs.getString("nom") + " " + rs.getString("prenom"),
                    rs.getString("email"),
                    rs.getString("motif"),
                    rs.getString("date_ajout")
                });
            }
        } catch (SQLException e) {
            System.err.println("Erreur getParEnseignant bloc_plus : " + e.getMessage());
        }
        return liste;
    }
}
