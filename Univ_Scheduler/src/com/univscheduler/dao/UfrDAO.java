package com.univscheduler.dao;

import com.univscheduler.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UfrDAO {

    public List<String[]> getTous() {
        List<String[]> liste = new ArrayList<>();
        String sql = "SELECT id, code, nom FROM ufr ORDER BY code";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                liste.add(new String[]{
                    rs.getString("id"),
                    rs.getString("code"),
                    rs.getString("nom")
                });
            }
        } catch (SQLException e) {
            System.err.println("Erreur UfrDAO.getTous() : " + e.getMessage());
        }
        return liste;
    }
}
