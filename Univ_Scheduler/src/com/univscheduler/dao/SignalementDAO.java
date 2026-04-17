package com.univscheduler.dao;

import com.univscheduler.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SignalementDAO {

	public boolean ajouter(String type, String description,
            int salleId, int enseignantId, Integer equipementId) {
String sql = "INSERT INTO signalements "
   + "(type, description, statut, salle_id, enseignant_id, equipement_id) "
   + "VALUES (?, ?, 'OUVERT', ?, ?, ?)";
try (Connection conn = DatabaseConnection.getConnection();
PreparedStatement ps = conn.prepareStatement(sql)) {
ps.setString(1, type);
ps.setString(2, description);
ps.setInt(3, salleId);
ps.setInt(4, enseignantId);
if (equipementId != null) ps.setInt(5, equipementId);
else ps.setNull(5, Types.INTEGER);
return ps.executeUpdate() > 0;
} catch (SQLException e) {
System.err.println("Erreur ajouter() signalement : " + e.getMessage());
}
return false;
}
    public List<String[]> getTous() {
        List<String[]> liste = new ArrayList<>();
        String sql = "SELECT s.id, s.type, s.description, s.statut, "
                   + "s.date_signalement, sa.numero, u.nom, u.prenom "
                   + "FROM signalements s "
                   + "LEFT JOIN salles sa ON s.salle_id = sa.id "
                   + "LEFT JOIN utilisateurs u ON s.enseignant_id = u.id "
                   + "ORDER BY s.date_signalement DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                liste.add(new String[]{
                    rs.getString("id"),
                    rs.getString("type"),
                    rs.getString("description"),
                    rs.getString("statut"),
                    rs.getString("date_signalement"),
                    rs.getString("numero"),
                    rs.getString("nom") + " " + rs.getString("prenom")
                });
            }
        } catch (SQLException e) {
            System.err.println("Erreur getTous() signalements : " + e.getMessage());
        }
        return liste;
    }

    public boolean changerStatut(int id, String statut) {
        String sql = "UPDATE signalements SET statut = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, statut);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur changerStatut() signalement : " + e.getMessage());
        }
        return false;
    }
}
