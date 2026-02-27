package com.univscheduler.model;

import com.univscheduler.model.enums.TypeConflit;

/**
 * Représente un conflit détecté dans la planification.
 * Ex: deux cours dans la même salle au même créneau.
 */
public class Conflit {

    // ── Attributs ──────────────────────────────────────────────
    private int id;
    private String description; // Message explicatif du conflit
    private TypeConflit type;   // Type de conflit (salle occupée, enseignant indispo, etc.)
    private boolean resolu;     // true si le conflit a été résolu

    // Les deux cours en conflit
    private Cours cours1;
    private Cours cours2;

    // ── Constructeur ────────────────────────────────────────────
    public Conflit(TypeConflit type, Cours cours1, Cours cours2) {
        this.type = type;
        this.cours1 = cours1;
        this.cours2 = cours2;
        this.resolu = false;
        this.description = genererDescription(); // Génère automatiquement la description
    }

    // ── Méthodes ────────────────────────────────────────────────

    /**
     * Génère automatiquement un message de description selon le type de conflit.
     */
    private String genererDescription() {
        switch (type) {
            case SALLE_OCCUPEE:
                return "La salle " + cours1.getSalle().getNumero()
                        + " est déjà réservée le " + cours1.getCreneau()
                        + " pour le cours de " + cours2.getMatiere();
            case ENSEIGNANT_INDISPONIBLE:
                return "L'enseignant " + cours1.getEnseignant().getNomComplet()
                        + " a déjà un cours le " + cours1.getCreneau();
            case CAPACITE_INSUFFISANTE:
                return "La salle " + cours1.getSalle().getNumero()
                        + " n'est pas assez grande pour " + cours1.getNombreEtudiants()
                        + " étudiants (capacité : " + cours1.getSalle().getCapacite() + ")";
            default:
                return "Conflit détecté entre " + cours1.getMatiere()
                        + " et " + cours2.getMatiere();
        }
    }

    /**
     * Marque le conflit comme résolu.
     */
    public void marquerResolu() {
        this.resolu = true;
        System.out.println("✓ Conflit résolu : " + description);
    }

    // ── Getters & Setters ───────────────────────────────────────
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TypeConflit getType() { return type; }
    public void setType(TypeConflit type) { this.type = type; }

    public boolean isResolu() { return resolu; }
    public void setResolu(boolean resolu) { this.resolu = resolu; }

    public Cours getCours1() { return cours1; }
    public void setCours1(Cours cours1) { this.cours1 = cours1; }

    public Cours getCours2() { return cours2; }
    public void setCours2(Cours cours2) { this.cours2 = cours2; }

    @Override
    public String toString() {
        return "⚠ [" + type + "] " + description + (resolu ? " (RÉSOLU)" : " (EN ATTENTE)");
    }
}
