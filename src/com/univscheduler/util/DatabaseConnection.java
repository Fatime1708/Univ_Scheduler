package com.univscheduler.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Classe de connexion à la base de données MySQL.
 *
 * Utilise le pattern Singleton : une seule connexion est créée
 * et réutilisée partout dans l'application.
 *
 * ⚠️ Avant d'utiliser cette classe :
 *   1. Ajouter le driver MySQL (mysql-connector-j-8.x.jar) dans
 *      Build Path → Classpath de ton projet Eclipse
 *   2. Modifier URL, USER et PASSWORD selon ta configuration
 */
public class DatabaseConnection {

    // ── Configuration ────────────────────────────────────────────
	private static final String URL      = "jdbc:mysql://localhost:3306/univ_scheduler"
            + "?useSSL=false"
            + "&serverTimezone=UTC"
            + "&allowPublicKeyRetrieval=true"
            + "&user=root"
            + "&password=";
private static final String USER     = "root";
private static final String PASSWORD = "";
    // ── Instance unique (Singleton) ──────────────────────────────
    private static Connection instance = null;

    // Constructeur privé : on ne peut pas faire "new DatabaseConnection()"
    private DatabaseConnection() {}

    /**
     * Retourne la connexion active.
     * Si elle n'existe pas encore, elle est créée.
     *
     * Utilisation :
     *   Connection conn = DatabaseConnection.getConnection();
     */
    public static Connection getConnection() {
        try {
            // Créer la connexion si elle n'existe pas ou si elle est fermée
            if (instance == null || instance.isClosed()) {
                // Charger le driver MySQL
                Class.forName("com.mysql.cj.jdbc.Driver");

                // Établir la connexion
                instance = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✓ Connexion MySQL établie !");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Driver MySQL introuvable !");
            System.err.println("   → Ajoute mysql-connector-j.jar dans le Build Path");
        } catch (SQLException e) {
            System.err.println("❌ Erreur de connexion MySQL : " + e.getMessage());
            System.err.println("   → Vérifie que MySQL est lancé et que le mot de passe est correct");
        }
        return instance;
    }

    /**
     * Ferme la connexion proprement.
     * À appeler quand l'application se ferme.
     */
    public static void fermer() {
        if (instance != null) {
            try {
                instance.close();
                instance = null;
                System.out.println("✓ Connexion MySQL fermée.");
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture : " + e.getMessage());
            }
        }
    }

    /**
     * Teste si la connexion fonctionne.
     * Utile pour afficher un message d'erreur au démarrage.
     */
    public static boolean testerConnexion() {
        return getConnection() != null;
    }
}