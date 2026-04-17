package com.univscheduler.view;



import java.sql.Connection;
import java.sql.DriverManager;

public class Test {
    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/univ_scheduler",
                "root",
                "" // mot de passe vide par défaut sur XAMPP
            );

            if (con != null) {
                System.out.println("✅ Connexion réussie !");
                con.close();
            }

        } catch (Exception e) {
            System.out.println("❌ Erreur : " + e.getMessage());
        }
    }
}