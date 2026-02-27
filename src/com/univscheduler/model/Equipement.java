package com.univscheduler.model;

/**
 * Représente un équipement présent dans une salle.
 * Ex: vidéoprojecteur, tableau interactif, climatisation.
 */
public class Equipement {

    // ── Attributs ──────────────────────────────────────────────
    private int id;
    private String nom;          // Ex: "Vidéoprojecteur"
    private String description;  // Ex: "Epson Full HD"
    private boolean fonctionnel; // true = fonctionne, false = en panne

    // ── Constructeur ────────────────────────────────────────────
    public Equipement(String nom, String description) {
        this.nom = nom;
        this.description = description;
        this.fonctionnel = true; // Par défaut, l'équipement fonctionne
    }

    // ── Méthodes ────────────────────────────────────────────────

    /**
     * Signale que cet équipement est en panne.
     */
    public void signalerPanne() {
        this.fonctionnel = false;
        System.out.println("⚠ Panne signalée pour : " + nom);
    }

    /**
     * Marque l'équipement comme réparé.
     */
    public void marquerRepare() {
        this.fonctionnel = true;
        System.out.println("✓ Équipement réparé : " + nom);
    }

    // ── Getters & Setters ───────────────────────────────────────
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isFonctionnel() { return fonctionnel; }
    public void setFonctionnel(boolean fonctionnel) { this.fonctionnel = fonctionnel; }

    @Override
    public String toString() {
        return nom + (fonctionnel ? " ✓" : " ✗ (en panne)");
    }
}
