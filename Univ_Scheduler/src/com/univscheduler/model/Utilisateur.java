package com.univscheduler.model;

/**
 * Classe mère représentant un utilisateur du système.
 * Toutes les classes (Admin, Enseignant, etc.) héritent de cette classe.
 */
public abstract class Utilisateur {

    // ── Attributs ──────────────────────────────────────────────
    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;  // Stocké hashé en base de données
    private String role;

    // ── Constructeur ────────────────────────────────────────────
    /**
     * Crée un nouvel utilisateur.
     * @param nom       Nom de famille
     * @param prenom    Prénom
     * @param email     Adresse email (identifiant unique)
     * @param motDePasse Mot de passe (sera hashé)
     * @param role      Rôle dans le système
     */
    public Utilisateur(String nom, String prenom, String email,
                       String motDePasse, String role) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.role = role;
    }

    // ── Méthodes ────────────────────────────────────────────────

    /**
     * Vérifie si le mot de passe saisi correspond à celui stocké.
     * @param motDePasseSaisi Le mot de passe à vérifier
     * @return true si correct, false sinon
     */
    public boolean verifierMotDePasse(String motDePasseSaisi) {
        return this.motDePasse.equals(motDePasseSaisi);
    }

    /**
     * Retourne le nom complet (Prénom + Nom).
     */
    public String getNomComplet() {
        return prenom + " " + nom;
    }

    /**
     * Méthode abstraite : chaque sous-classe a ses propres fonctionnalités.
     * Elle doit être implémentée dans chaque classe fille.
     */
    public abstract String getInfosRole();

    // ── Getters & Setters ───────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    @Override
    public String toString() {
        return getNomComplet() + " (" + role + ")";
    }
}
