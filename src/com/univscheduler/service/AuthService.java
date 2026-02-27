package com.univscheduler.service;

import com.univscheduler.model.Utilisateur;
import java.util.List;

/**
 * Service d'authentification des utilisateurs.
 * Gère la connexion et la session courante.
 */
public class AuthService {

    // L'utilisateur actuellement connecté (null si personne n'est connecté)
    private Utilisateur utilisateurConnecte;

    // ── Méthodes ────────────────────────────────────────────────

    /**
     * Tente de connecter un utilisateur avec son email et mot de passe.
     * 
     * @param email       L'email saisi
     * @param motDePasse  Le mot de passe saisi
     * @param utilisateurs La liste de tous les utilisateurs (vient de la BDD)
     * @return L'utilisateur connecté, ou null si identifiants incorrects
     */
    public Utilisateur connecter(String email, String motDePasse, List<Utilisateur> utilisateurs) {
        for (Utilisateur u : utilisateurs) {
        	if (u.getEmail().equalsIgnoreCase(email) && u.getMotDePasse().equals(motDePasse)) {
                this.utilisateurConnecte = u;
                System.out.println("Connexion réussie : " + u.getNomComplet() + " (" + u.getRole() + ")");
                return u;
            }
        }
        System.out.println("Échec de connexion : identifiants incorrects.");
        return null;
    }

    /**
     * Déconnecte l'utilisateur courant.
     */
    public void deconnecter() {
        if (utilisateurConnecte != null) {
            System.out.println("Déconnexion : " + utilisateurConnecte.getNomComplet());
        }
        this.utilisateurConnecte = null;
    }

    /**
     * Vérifie si un utilisateur est actuellement connecté.
     */
    public boolean estConnecte() {
        return utilisateurConnecte != null;
    }

    /**
     * Vérifie si l'utilisateur connecté a un rôle donné.
     * @param role Ex: "ADMIN", "GESTIONNAIRE", "ENSEIGNANT", "ETUDIANT"
     */
    public boolean aLeRole(String role) {
        if (!estConnecte()) return false;
        return utilisateurConnecte.getRole().equalsIgnoreCase(role);
    }

    // ── Getter ──────────────────────────────────────────────────
    public Utilisateur getUtilisateurConnecte() {
        return utilisateurConnecte;
    }
}
