package com.univscheduler.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente un cours planifié dans l'emploi du temps.
 * Un cours relie : une matière + un enseignant + une salle + un créneau + des étudiants.
 */
public class Cours {

    // ── Attributs ──────────────────────────────────────────────
    private int id;
    private String matiere;       // Ex: "Algorithmique", "Bases de données"
    private String description;   // Description optionnelle
    private String classe;        // Ex: "L2 Informatique"
    private String groupe;        // Ex: "Groupe A" (peut être null si cours pour toute la classe)

    // Relations avec d'autres classes
    private Enseignant enseignant;  // Qui dispense le cours
    private Salle salle;            // Où se déroule le cours
    private Creneau creneau;        // Quand se déroule le cours
    private List<Etudiant> etudiants; // Qui suit le cours

    // ── Constructeur ────────────────────────────────────────────
    public Cours(String matiere, String classe, String groupe,
                 Enseignant enseignant, Salle salle, Creneau creneau) {
        this.matiere = matiere;
        this.classe = classe;
        this.groupe = groupe;
        this.enseignant = enseignant;
        this.salle = salle;
        this.creneau = creneau;
        this.etudiants = new ArrayList<>();
    }

    // ── Méthodes ────────────────────────────────────────────────

    /**
     * Ajoute un étudiant à ce cours.
     */
    public void ajouterEtudiant(Etudiant etudiant) {
        if (!etudiants.contains(etudiant)) {
            etudiants.add(etudiant);
        }
    }

    /**
     * Retourne le nombre d'étudiants inscrits à ce cours.
     */
    public int getNombreEtudiants() {
        return etudiants.size();
    }

    /**
     * Vérifie si ce cours entre en conflit avec un autre cours.
     * Conflit = même salle OU même enseignant, au même créneau.
     * 
     * @param autre L'autre cours à comparer
     * @return true s'il y a un conflit
     */
    public boolean estEnConflitAvec(Cours autre) {
        // Vérifier si les créneaux se chevauchent
        if (!this.creneau.chevauche(autre.creneau)) {
            return false; // Créneaux différents → pas de conflit
        }

        // Même salle au même créneau = conflit !
        boolean memeSalle = this.salle.getId() == autre.salle.getId();

        // Même enseignant au même créneau = conflit !
        boolean memeEnseignant = this.enseignant.getId() == autre.enseignant.getId();

        return memeSalle || memeEnseignant;
    }

    // ── Getters & Setters ───────────────────────────────────────
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getMatiere() { return matiere; }
    public void setMatiere(String matiere) { this.matiere = matiere; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getClasse() { return classe; }
    public void setClasse(String classe) { this.classe = classe; }

    public String getGroupe() { return groupe; }
    public void setGroupe(String groupe) { this.groupe = groupe; }

    public Enseignant getEnseignant() { return enseignant; }
    public void setEnseignant(Enseignant enseignant) { this.enseignant = enseignant; }

    public Salle getSalle() { return salle; }
    public void setSalle(Salle salle) { this.salle = salle; }

    public Creneau getCreneau() { return creneau; }
    public void setCreneau(Creneau creneau) { this.creneau = creneau; }

    public List<Etudiant> getEtudiants() { return etudiants; }

    @Override
    public String toString() {
        return matiere + " | " + classe + " (" + groupe + ") | "
                + enseignant.getNomComplet() + " | " + salle.getNumero()
                + " | " + creneau;
    }
}
