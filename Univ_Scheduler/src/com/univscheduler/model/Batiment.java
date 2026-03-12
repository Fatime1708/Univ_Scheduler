package com.univscheduler.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente un bâtiment de l'université.
 * Un bâtiment contient plusieurs salles.
 */
public class Batiment {

    // ── Attributs ──────────────────────────────────────────────
    private int id;
    private String nom;          // Ex: "Bâtiment A", "Amphi Central"
    private String localisation; // Ex: "Campus Nord"
    private int nombreEtages;
    private List<Salle> salles;  // Toutes les salles de ce bâtiment

    // ── Constructeur ────────────────────────────────────────────
    public Batiment(String nom, String localisation, int nombreEtages) {
        this.nom = nom;
        this.localisation = localisation;
        this.nombreEtages = nombreEtages;
        this.salles = new ArrayList<>();
    }

    // ── Méthodes ────────────────────────────────────────────────

    /**
     * Ajoute une salle au bâtiment.
     */
    public void ajouterSalle(Salle salle) {
        salle.setBatimentId(this.id);
        salles.add(salle);
    }

    /**
     * Retourne uniquement les salles disponibles de ce bâtiment.
     */
    public List<Salle> getSallesDisponibles() {
        List<Salle> disponibles = new ArrayList<>();
        for (Salle s : salles) {
            if (s.isDisponible()) {
                disponibles.add(s);
            }
        }
        return disponibles;
    }

    /**
     * Retourne le nombre total de salles.
     */
    public int getNombreSalles() {
        return salles.size();
    }

    // ── Getters & Setters ───────────────────────────────────────
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getLocalisation() { return localisation; }
    public void setLocalisation(String localisation) { this.localisation = localisation; }

    public int getNombreEtages() { return nombreEtages; }
    public void setNombreEtages(int nombreEtages) { this.nombreEtages = nombreEtages; }

    public List<Salle> getSalles() { return salles; }
    public void setSalles(List<Salle> salles) { this.salles = salles; }

    @Override
    public String toString() {
        return nom + " | " + localisation + " | " + nombreEtages + " étage(s) | "
                + salles.size() + " salle(s)";
    }
}

