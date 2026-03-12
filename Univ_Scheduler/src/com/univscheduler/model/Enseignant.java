package com.univscheduler.model;

/**
 * Représente un enseignant de l'université.
 * Peut consulter son emploi du temps et réserver des salles ponctuellement.
 */
public class Enseignant extends Utilisateur {

    // ── Attributs spécifiques ───────────────────────────────────
    private String specialite;    // Ex: "Mathématiques", "Informatique"
    private String departement;   // Ex: "UFR Sciences"

    // ── Constructeur ────────────────────────────────────────────
    public Enseignant(String nom, String prenom, String email,
                      String motDePasse, String specialite, String departement) {
        super(nom, prenom, email, motDePasse, "ENSEIGNANT");
        this.specialite = specialite;
        this.departement = departement;
    }

    // ── Méthodes spécifiques ────────────────────────────────────

    /**
     * Signale un problème technique dans une salle.
     * @param salle       La salle concernée
     * @param description Description du problème
     */
    public void signalerProbleme(Salle salle, String description) {
        System.out.println("Problème signalé dans la salle " + salle.getNumero()
                + " par " + getNomComplet() + " : " + description);
        // En vrai : on créerait un ticket ou une notification en base de données
    }

    @Override
    public String getInfosRole() {
        return "Enseignant - " + specialite + " | " + departement;
    }

    // ── Getters & Setters ───────────────────────────────────────
    public String getSpecialite() { return specialite; }
    public void setSpecialite(String specialite) { this.specialite = specialite; }

    public String getDepartement() { return departement; }
    public void setDepartement(String departement) { this.departement = departement; }
}
