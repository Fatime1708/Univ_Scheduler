package com.univscheduler.model;

import com.univscheduler.model.enums.TypeSalle;
import java.util.ArrayList;
import java.util.List;

/**
 * Représente une salle dans un bâtiment de l'université.
 * Une salle a une capacité, un type, et des équipements.
 */
public class Salle {

    // ── Attributs ──────────────────────────────────────────────
    private int id;
    private String numero;       // Ex: "A101", "B203"
    private int capacite;        // Nombre de places
    private TypeSalle type;      // TD, TP, AMPHI, REUNION
    private boolean disponible;  // true = disponible maintenant
    private int batimentId;      // Référence vers le bâtiment parent
    private List<Equipement> equipements; // Liste des équipements de la salle

    // ── Constructeur ────────────────────────────────────────────
    public Salle(String numero, int capacite, TypeSalle type, int batimentId) {
        this.numero = numero;
        this.capacite = capacite;
        this.type = type;
        this.batimentId = batimentId;
        this.disponible = true;
        this.equipements = new ArrayList<>(); // Liste vide au départ
    }

    // ── Méthodes ────────────────────────────────────────────────

    /**
     * Ajoute un équipement à cette salle.
     */
    public void ajouterEquipement(Equipement equipement) {
        equipements.add(equipement);
    }

    /**
     * Supprime un équipement de cette salle.
     */
    public void retirerEquipement(Equipement equipement) {
        equipements.remove(equipement);
    }

    /**
     * Vérifie si la salle possède un équipement par son nom.
     * @param nomEquipement  Ex: "Vidéoprojecteur"
     * @return true si la salle possède cet équipement et qu'il fonctionne
     */
    public boolean possededEquipement(String nomEquipement) {
        return equipements.stream()
                .anyMatch(e -> e.getNom().equalsIgnoreCase(nomEquipement) && e.isFonctionnel());
    }

    /**
     * Vérifie si la salle a une capacité suffisante pour un groupe.
     * @param nombreEtudiants Le nombre d'étudiants attendus
     */
    public boolean capaciteSuffisante(int nombreEtudiants) {
        return this.capacite >= nombreEtudiants;
    }

    // ── Getters & Setters ───────────────────────────────────────
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public int getCapacite() { return capacite; }
    public void setCapacite(int capacite) { this.capacite = capacite; }

    public TypeSalle getType() { return type; }
    public void setType(TypeSalle type) { this.type = type; }

    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }

    public int getBatimentId() { return batimentId; }
    public void setBatimentId(int batimentId) { this.batimentId = batimentId; }

    public List<Equipement> getEquipements() { return equipements; }
    public void setEquipements(List<Equipement> equipements) { this.equipements = equipements; }

    @Override
    public String toString() {
        return "Salle " + numero + " | " + type + " | " + capacite + " places"
                + (disponible ? " | Disponible" : " | Occupée");
    }
}
