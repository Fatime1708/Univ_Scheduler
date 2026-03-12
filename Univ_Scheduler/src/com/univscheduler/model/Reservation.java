package com.univscheduler.model;

import com.univscheduler.model.enums.StatutReservation;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Représente une réservation ponctuelle d'une salle.
 * Ex: réservation pour une soutenance, une réunion, une étude en groupe.
 */
public class Reservation {

    // ── Attributs ──────────────────────────────────────────────
    private int id;
    private LocalDate date;          // La date précise de la réservation
    private String motif;            // Ex: "Soutenance de stage", "Réunion département"
    private StatutReservation statut; // EN_ATTENTE, CONFIRMEE, ANNULEE

    // Relations avec d'autres classes
    private Utilisateur utilisateur; // Qui a fait la réservation
    private Salle salle;             // Quelle salle est réservée
    private Creneau creneau;         // Sur quel créneau

    // ── Constructeur ────────────────────────────────────────────
    public Reservation(LocalDate date, String motif,
                       Utilisateur utilisateur, Salle salle, Creneau creneau) {
        this.date = date;
        this.motif = motif;
        this.utilisateur = utilisateur;
        this.salle = salle;
        this.creneau = creneau;
        this.statut = StatutReservation.EN_ATTENTE; // Toujours en attente au départ
    }

    // ── Méthodes ────────────────────────────────────────────────

    /**
     * Confirme la réservation.
     */
    public void confirmer() {
        this.statut = StatutReservation.CONFIRMEE;
        System.out.println("✓ Réservation confirmée : " + salle.getNumero()
                + " le " + date + " par " + utilisateur.getNomComplet());
    }

    /**
     * Annule la réservation et libère la salle.
     */
    public void annuler() {
        this.statut = StatutReservation.ANNULEE;
        salle.setDisponible(true); // La salle redevient disponible
        System.out.println("✗ Réservation annulée : " + salle.getNumero()
                + " le " + date);
    }

    /**
     * Vérifie si la réservation est encore active (pas annulée).
     */
    public boolean estActive() {
        return statut != StatutReservation.ANNULEE;
    }

    // ── Getters & Setters ───────────────────────────────────────
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }

    public StatutReservation getStatut() { return statut; }
    public void setStatut(StatutReservation statut) { this.statut = statut; }

    public Utilisateur getUtilisateur() { return utilisateur; }
    public void setUtilisateur(Utilisateur utilisateur) { this.utilisateur = utilisateur; }

    public Salle getSalle() { return salle; }
    public void setSalle(Salle salle) { this.salle = salle; }

    public Creneau getCreneau() { return creneau; }
    public void setCreneau(Creneau creneau) { this.creneau = creneau; }

    @Override
    public String toString() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return "Réservation " + salle.getNumero() + " | " + date.format(fmt)
                + " | " + creneau + " | " + motif + " | " + statut;
    }
}

