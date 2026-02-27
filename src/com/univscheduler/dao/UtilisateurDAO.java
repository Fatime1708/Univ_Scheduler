package com.univscheduler.dao;

import com.univscheduler.model.*;
import com.univscheduler.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour les utilisateurs.
 * Gère la connexion, la récupération et la modification des utilisateurs.
 */
public class UtilisateurDAO {

    // ── READ : Tous les utilisateurs ────────────────────────────
    public List<Utilisateur> getTous() {
        List<Utilisateur> liste = new ArrayList<>();
        String sql = "SELECT * FROM utilisateurs ORDER BY nom";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Utilisateur u = construireUtilisateur(rs);
                if (u != null) liste.add(u);
            }

        } catch (SQLException e) {
            System.err.println("Erreur getTous() utilisateurs : " + e.getMessage());
        }
        return liste;
    }

    // ── READ : Trouver par email (pour la connexion) ─────────────
    /**
     * Cherche un utilisateur par son email.
     * Utilisé par AuthService pour la connexion.
     */
    public Utilisateur getParEmail(String email) {
        String sql = "SELECT * FROM utilisateurs WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return construireUtilisateur(rs);

        } catch (SQLException e) {
            System.err.println("Erreur getParEmail() : " + e.getMessage());
        }
        return null;
    }

    // ── READ : Tous les enseignants ──────────────────────────────
    public List<Enseignant> getTousEnseignants() {
        List<Enseignant> liste = new ArrayList<>();
        String sql = "SELECT u.*, e.specialite, e.departement "
                   + "FROM utilisateurs u "
                   + "JOIN enseignants e ON u.id = e.id "
                   + "WHERE u.role = 'ENSEIGNANT' "
                   + "ORDER BY u.nom";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Enseignant ens = new Enseignant(
                    rs.getString("nom"),
                    rs.getString("prenom"),
                    rs.getString("email"),
                    rs.getString("mot_de_passe"),
                    rs.getString("specialite"),
                    rs.getString("departement")
                );
                ens.setId(rs.getInt("id"));
                liste.add(ens);
            }

        } catch (SQLException e) {
            System.err.println("Erreur getTousEnseignants() : " + e.getMessage());
        }
        return liste;
    }

    // ── CREATE : Ajouter un utilisateur ─────────────────────────
    public boolean ajouter(Utilisateur utilisateur) {
        String sql = "INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, role) "
                   + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, utilisateur.getNom());
            ps.setString(2, utilisateur.getPrenom());
            ps.setString(3, utilisateur.getEmail());
            ps.setString(4, utilisateur.getMotDePasse());
            ps.setString(5, utilisateur.getRole());

            int lignes = ps.executeUpdate();
            if (lignes > 0) {
                ResultSet cleGeneree = ps.getGeneratedKeys();
                if (cleGeneree.next()) {
                    utilisateur.setId(cleGeneree.getInt(1));
                }
                // Insérer les données spécifiques selon le rôle
                insererDonneesRole(conn, utilisateur);
                System.out.println("✓ Utilisateur ajouté : " + utilisateur.getNomComplet());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Erreur ajouter() utilisateur : " + e.getMessage());
        }
        return false;
    }

    // ── UPDATE : Modifier un utilisateur ────────────────────────
    public boolean modifier(Utilisateur utilisateur) {
        String sql = "UPDATE utilisateurs SET nom=?, prenom=?, email=?, role=? WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, utilisateur.getNom());
            ps.setString(2, utilisateur.getPrenom());
            ps.setString(3, utilisateur.getEmail());
            ps.setString(4, utilisateur.getRole());
            ps.setInt(5,    utilisateur.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erreur modifier() utilisateur : " + e.getMessage());
        }
        return false;
    }

    // ── DELETE : Supprimer ───────────────────────────────────────
    public boolean supprimer(int id) {
        String sql = "DELETE FROM utilisateurs WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erreur supprimer() utilisateur : " + e.getMessage());
        }
        return false;
    }

    // ── Méthodes privées ─────────────────────────────────────────

    /**
     * Construit le bon type d'objet selon le rôle en base.
     */
    private Utilisateur construireUtilisateur(ResultSet rs) throws SQLException {
        String role = rs.getString("role");
        int id = rs.getInt("id");

        Utilisateur u = switch (role) {
            case "ADMIN" -> new Administrateur(
                rs.getString("nom"), rs.getString("prenom"),
                rs.getString("email"), rs.getString("mot_de_passe"));
            case "GESTIONNAIRE" -> new GestionnaireEmploiDuTemps(
                rs.getString("nom"), rs.getString("prenom"),
                rs.getString("email"), rs.getString("mot_de_passe"), "");
            case "ENSEIGNANT" -> new Enseignant(
                rs.getString("nom"), rs.getString("prenom"),
                rs.getString("email"), rs.getString("mot_de_passe"), "", "");
            case "ETUDIANT" -> new Etudiant(
                rs.getString("nom"), rs.getString("prenom"),
                rs.getString("email"), rs.getString("mot_de_passe"), "", "", "");
            default -> null;
        };

        if (u != null) u.setId(id);
        return u;
    }

    /**
     * Insère les données spécifiques au rôle après l'insertion principale.
     */
    private void insererDonneesRole(Connection conn,
                                     Utilisateur utilisateur) throws SQLException {
        switch (utilisateur.getRole()) {
            case "ENSEIGNANT" -> {
                Enseignant ens = (Enseignant) utilisateur;
                PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO enseignants (id, specialite, departement) VALUES (?,?,?)");
                ps.setInt(1,    ens.getId());
                ps.setString(2, ens.getSpecialite());
                ps.setString(3, ens.getDepartement());
                ps.executeUpdate();
            }
            case "ETUDIANT" -> {
                Etudiant etu = (Etudiant) utilisateur;
                PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO etudiants (id, classe, groupe, numero_etudiant) VALUES (?,?,?,?)");
                ps.setInt(1,    etu.getId());
                ps.setString(2, etu.getClasse());
                ps.setString(3, etu.getGroupe());
                ps.setString(4, etu.getNumeroEtudiant());
                ps.executeUpdate();
            }
            case "GESTIONNAIRE" -> {
                GestionnaireEmploiDuTemps g = (GestionnaireEmploiDuTemps) utilisateur;
                PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO gestionnaires (id, service) VALUES (?,?)");
                ps.setInt(1,    g.getId());
                ps.setString(2, g.getService());
                ps.executeUpdate();
            }
        }
    }
}
