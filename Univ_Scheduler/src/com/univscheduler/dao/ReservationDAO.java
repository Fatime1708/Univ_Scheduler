package com.univscheduler.dao;

import com.univscheduler.model.*;
import com.univscheduler.model.enums.StatutReservation;
import com.univscheduler.model.enums.TypeSalle;
import com.univscheduler.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour les réservations de salles.
 */
public class ReservationDAO {
	

    // ── CREATE : Ajouter une réservation ─────────────────────────
    public boolean ajouter(Reservation reservation) {
        String sql = "INSERT INTO reservations (date_reserv, motif, statut, utilisateur_id, salle_id, creneau_id) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setDate(1,   Date.valueOf(reservation.getDate()));
            ps.setString(2, reservation.getMotif());
            ps.setString(3, reservation.getStatut().name());
            ps.setInt(4,    reservation.getUtilisateur().getId());
            ps.setInt(5,    reservation.getSalle().getId());
            ps.setInt(6,    reservation.getCreneau().getId());

            int lignes = ps.executeUpdate();
            if (lignes > 0) {
                ResultSet cle = ps.getGeneratedKeys();
                if (cle.next()) reservation.setId(cle.getInt(1));
                System.out.println("✓ Réservation ajoutée : " + reservation);
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Erreur ajouter() réservation : " + e.getMessage());
        }
        return false;
    }

    // ── READ : Toutes les réservations ───────────────────────────
    public List<Reservation> getTous() {
        List<Reservation> liste = new ArrayList<>();
        String sql = buildSelectSQL("ORDER BY r.date_reserv DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {

            while (rs.next()) liste.add(construireReservation(rs));

        } catch (SQLException e) {
            System.err.println("Erreur getTous() réservations : " + e.getMessage());
        }
        return liste;
    }

    // ── READ : Réservations d'un utilisateur ─────────────────────
    public List<Reservation> getParUtilisateur(int utilisateurId) {
        List<Reservation> liste = new ArrayList<>();
        String sql = buildSelectSQL("WHERE r.utilisateur_id = ? ORDER BY r.date_reserv DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, utilisateurId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(construireReservation(rs));

        } catch (SQLException e) {
            System.err.println("Erreur getParUtilisateur() : " + e.getMessage());
        }
        return liste;
    }

    // ── READ : Réservations d'une salle ──────────────────────────
    public List<Reservation> getParSalle(int salleId) {
        List<Reservation> liste = new ArrayList<>();
        String sql = buildSelectSQL("WHERE r.salle_id = ? ORDER BY r.date_reserv DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, salleId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(construireReservation(rs));

        } catch (SQLException e) {
            System.err.println("Erreur getParSalle() : " + e.getMessage());
        }
        return liste;
    }

    // ── UPDATE : Changer le statut ────────────────────────────────
    public boolean changerStatut(int id, StatutReservation statut) {
        String sql = "UPDATE reservations SET statut = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, statut.name());
            ps.setInt(2, id);
            boolean ok = ps.executeUpdate() > 0;
            if (ok) System.out.println("✓ Statut mis à jour : " + statut);
            return ok;

        } catch (SQLException e) {
            System.err.println("Erreur changerStatut() : " + e.getMessage());
        }
        return false;
    }

    // ── DELETE : Annuler une réservation ──────────────────────────
    public boolean supprimer(int id) {
        String sql = "DELETE FROM reservations WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            boolean ok = ps.executeUpdate() > 0;
            if (ok) System.out.println("✓ Réservation supprimée (id=" + id + ")");
            return ok;

        } catch (SQLException e) {
            System.err.println("Erreur supprimer() réservation : " + e.getMessage());
        }
        return false;
    }

    // ── Vérifier disponibilité ────────────────────────────────────
    /**
     * Vérifie si une salle est déjà réservée à une date et un créneau donnés.
     * @return true si la salle est LIBRE (pas de conflit)
     */
    public boolean estDisponible(int salleId, LocalDate date, int creneauId) {
        String sql = "SELECT COUNT(*) FROM reservations "
                   + "WHERE salle_id = ? AND date_reserv = ? AND creneau_id = ? "
                   + "AND statut != 'ANNULEE'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1,  salleId);
            ps.setDate(2, Date.valueOf(date));
            ps.setInt(3,  creneauId);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) == 0;

        } catch (SQLException e) {
            System.err.println("Erreur estDisponible() : " + e.getMessage());
        }
        return false;
    }

    // ── SQL de base (SELECT avec jointures) ───────────────────────
    private String buildSelectSQL(String whereOrderClause) {
        return "SELECT r.id, r.date_reserv, r.motif, r.statut, r.created_at, "
             + "u.id AS util_id, u.nom AS util_nom, u.prenom AS util_prenom, "
             + "u.email AS util_email, u.mot_de_passe AS util_mdp, u.role AS util_role, "
             + "s.id AS salle_id, s.numero AS salle_num, s.capacite, "
             + "s.type AS salle_type, s.disponible, s.batiment_id, "
             + "cr.id AS creneau_id, cr.jour, cr.heure_debut, cr.duree_minutes "
             + "FROM reservations r "
             + "JOIN utilisateurs u  ON r.utilisateur_id = u.id "
             + "JOIN salles s        ON r.salle_id        = s.id "
             + "JOIN creneaux cr     ON r.creneau_id      = cr.id "
             + whereOrderClause;
    }

    // ── Construire un objet Reservation depuis ResultSet ──────────
    private Reservation construireReservation(ResultSet rs) throws SQLException {
        // Utilisateur (classe anonyme pour instancier la classe abstraite)
        Utilisateur util = new Utilisateur(
            rs.getString("util_nom"),
            rs.getString("util_prenom"),
            rs.getString("util_email"),
            rs.getString("util_mdp"),
            rs.getString("util_role")
        ) {
            @Override
            public String getInfosRole() { return getRole(); }
        };
        util.setId(rs.getInt("util_id"));

        // Salle
        Salle salle = new Salle(
            rs.getString("salle_num"),
            rs.getInt("capacite"),
            TypeSalle.valueOf(rs.getString("salle_type")),
            rs.getInt("batiment_id")
        );
        salle.setId(rs.getInt("salle_id"));
        salle.setDisponible(rs.getBoolean("disponible"));

        // Créneau
        String heureStr = rs.getString("heure_debut");
        String[] parts  = heureStr.split(":");
        LocalTime heureDebut = LocalTime.of(
            Integer.parseInt(parts[0]),
            Integer.parseInt(parts[1])
        );
        Creneau creneau = new Creneau(
            rs.getString("jour"), heureDebut, rs.getInt("duree_minutes"));
        creneau.setId(rs.getInt("creneau_id"));

        // Réservation
        Reservation r = new Reservation(
            rs.getDate("date_reserv").toLocalDate(),
            rs.getString("motif"),
            util, salle, creneau
        );
        r.setId(rs.getInt("id"));
        r.setStatut(StatutReservation.valueOf(rs.getString("statut")));
        return r;
    }
}
