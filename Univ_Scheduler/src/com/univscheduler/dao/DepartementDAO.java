
package com.univscheduler.dao;

import com.univscheduler.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DepartementDAO {

    public List<String[]> getParUfr(int ufrId) {
        List<String[]> liste = new ArrayList<>();
        String sql = "SELECT id, nom FROM departements WHERE ufr_id = ? ORDER BY nom";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ufrId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                liste.add(new String[]{
                    rs.getString("id"),
                    rs.getString("nom")
                });
            }
        } catch (SQLException e) {
            System.err.println("Erreur DepartementDAO.getParUfr() : " + e.getMessage());
        }
        return liste;
    }
}