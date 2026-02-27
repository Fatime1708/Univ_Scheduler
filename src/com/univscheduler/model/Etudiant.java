package com.univscheduler.model;

/**
 * Représente un étudiant de l'université.
 * Peut consulter les emplois du temps et chercher des salles libres.
 */
public class Etudiant extends Utilisateur {

    // ── Attributs spécifiques ───────────────────────────────────
    private String classe;    // Ex: "L2 Informatique"
    private String groupe;    // Ex: "Groupe A", "Groupe B"
    private String numeroEtudiant; // Ex: "2024001"

    // ── Constructeur ────────────────────────────────────────────
    public Etudiant(String nom, String prenom, String email, String motDePasse,
                    String classe, String groupe, String numeroEtudiant) {
        super(nom, prenom, email, motDePasse, "ETUDIANT");
        this.classe = classe;
        this.groupe = groupe;
        this.numeroEtudiant = numeroEtudiant;
    }

    @Override
    public String getInfosRole() {
        return "Étudiant - " + classe + " | " + groupe + " | N°" + numeroEtudiant;
    }

    // ── Getters & Setters ───────────────────────────────────────
    public String getClasse() { return classe; }
    public void setClasse(String classe) { this.classe = classe; }

    public String getGroupe() { return groupe; }
    public void setGroupe(String groupe) { this.groupe = groupe; }

    public String getNumeroEtudiant() { return numeroEtudiant; }
    public void setNumeroEtudiant(String numeroEtudiant) { this.numeroEtudiant = numeroEtudiant; }
}
