package com.univscheduler.dao;

import com.univscheduler.model.Salle;
import com.univscheduler.model.enums.TypeSalle;
import com.univscheduler.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) pour les salles.
 * Contient toutes les requêtes SQL liées aux salles.
 *
 * Chaque méthode = une opération en base de données :
 *   - getTous()        → SELECT toutes les salles
 *   - getById()        → SELECT une salle par ID
 *   - ajouter()        → INSERT une nouvelle salle
 *   - modifier()       → UPDATE une salle existante
 *   - supprimer()      → DELETE une salle
 *   - getDisponibles() → SELECT les salles libres
 */
public class SalleDAO {

    // ── READ : Toutes les salles ─────────────────────────────────
    /**
     * Récupère toutes les salles de la base de données.
     */
    public List<Salle> getTous() {
        List<Salle> salles = new ArrayList<>();
        String sql = "SELECT * FROM salles ORDER BY numero";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)) {

            while (rs.next()) {
                salles.add(construireSalle(rs));
            }

        } catch (SQLException e) {
            System.err.println("Erreur getTous() salles : " + e.getMessage());
        }
        return salles;
    }

    // ── READ : Une salle par ID ──────────────────────────────────
    public Salle getById(int id) {
        String sql = "SELECT * FROM salles WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return construireSalle(rs);

        } catch (SQLException e) {
            System.err.println("Erreur getById() salle : " + e.getMessage());
        }
        return null;
    }

    // ── READ : Salles disponibles ────────────────────────────────
    public List<Salle> getDisponibles() {
        List<Salle> salles = new ArrayList<>();
        String sql = "SELECT * FROM salles WHERE disponible = TRUE ORDER BY numero";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)) {

            while (rs.next()) salles.add(construireSalle(rs));

        } catch (SQLException e) {
            System.err.println("Erreur getDisponibles() : " + e.getMessage());
        }
        return salles;
    }

    // ── READ : Recherche avancée ─────────────────────────────────
    /**
     * Recherche des salles selon des critères.
     * @param capaciteMin  Capacité minimale (0 = pas de filtre)
     * @param type         Type de salle (null = pas de filtre)
     * @param disponible   true = seulement les libres, false = toutes
     */
    public List<Salle> rechercher(int capaciteMin, TypeSalle type, boolean disponibleSeulement) {
        List<Salle> salles = new ArrayList<>();

        // Construction dynamique de la requête
        StringBuilder sql = new StringBuilder("SELECT * FROM salles WHERE 1=1");
        if (capaciteMin > 0)          sql.append(" AND capacite >= ?");
        if (type != null)             sql.append(" AND type = ?");
        if (disponibleSeulement)      sql.append(" AND disponible = TRUE");
        sql.append(" ORDER BY numero");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int index = 1;
            if (capaciteMin > 0) ps.setInt(index++, capaciteMin);
            if (type != null)    ps.setString(index++, type.name());

            ResultSet rs = ps.executeQuery();
            while (rs.next()) salles.add(construireSalle(rs));

        } catch (SQLException e) {
            System.err.println("Erreur rechercher() salles : " + e.getMessage());
        }
        return salles;
    }

    // ── CREATE : Ajouter une salle ───────────────────────────────
    /**
     * Insère une nouvelle salle dans la base.
     * @return true si l'insertion a réussi
     */
    public boolean ajouter(Salle salle) {
        String sql = "INSERT INTO salles (numero, capacite, type, disponible, batiment_id) "
                   + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, salle.getNumero());
            ps.setInt(2,    salle.getCapacite());
            ps.setString(3, salle.getType().name());
            ps.setBoolean(4,salle.isDisponible());
            ps.setInt(5,    salle.getBatimentId());

            int lignes = ps.executeUpdate();

            // Récupérer l'ID généré automatiquement
            if (lignes > 0) {
                ResultSet cleGeneree = ps.getGeneratedKeys();
                if (cleGeneree.next()) {
                    salle.setId(cleGeneree.getInt(1));
                }
                System.out.println("✓ Salle ajoutée : " + salle.getNumero());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Erreur ajouter() salle : " + e.getMessage());
        }
        return false;
    }

    // ── UPDATE : Modifier une salle ──────────────────────────────
    public boolean modifier(Salle salle) {
        String sql = "UPDATE salles SET numero=?, capacite=?, type=?, "
                   + "disponible=?, batiment_id=? WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1,  salle.getNumero());
            ps.setInt(2,     salle.getCapacite());
            ps.setString(3,  salle.getType().name());
            ps.setBoolean(4, salle.isDisponible());
            ps.setInt(5,     salle.getBatimentId());
            ps.setInt(6,     salle.getId());

            boolean ok = ps.executeUpdate() > 0;
            if (ok) System.out.println("✓ Salle modifiée : " + salle.getNumero());
            return ok;

        } catch (SQLException e) {
            System.err.println("Erreur modifier() salle : " + e.getMessage());
        }
        return false;
    }

    // ── DELETE : Supprimer une salle ─────────────────────────────
    public boolean supprimer(int id) {
        String sql = "DELETE FROM salles WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            boolean ok = ps.executeUpdate() > 0;
            if (ok) System.out.println("✓ Salle supprimée (id=" + id + ")");
            return ok;

        } catch (SQLException e) {
            System.err.println("Erreur supprimer() salle : " + e.getMessage());
        }
        return false;
    }

    // ── Compter les salles ───────────────────────────────────────
    public int compter() {
        String sql = "SELECT COUNT(*) FROM salles";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Erreur compter() : " + e.getMessage());
        }
        return 0;
    }

    // ── Méthode privée : construire un objet Salle depuis un ResultSet ──
    /**
     * Transforme une ligne SQL en objet Java Salle.
     * Appelée après chaque SELECT.
     */
    private Salle construireSalle(ResultSet rs) throws SQLException {
        Salle salle = new Salle(
            rs.getString("numero"),
            rs.getInt("capacite"),
            TypeSalle.valueOf(rs.getString("type")),
            rs.getInt("batiment_id")
        );
        salle.setId(rs.getInt("id"));
        salle.setDisponible(rs.getBoolean("disponible"));
        return salle;
    }
}
