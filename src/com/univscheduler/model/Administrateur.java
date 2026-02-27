package com.univscheduler.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente un administrateur du système.
 * Hérite de Utilisateur et possède les droits les plus élevés.
 */
public class Administrateur extends Utilisateur {

    // ── Constructeur ────────────────────────────────────────────
    public Administrateur(String nom, String prenom, String email, String motDePasse) {
        // On appelle le constructeur de la classe mère avec le rôle "ADMIN"
        super(nom, prenom, email, motDePasse, "ADMIN");
    }

    // ── Méthodes spécifiques ────────────────────────────────────

    /**
     * Ajoute un nouvel utilisateur dans la liste.
     */
    public void ajouterUtilisateur(List<Utilisateur> listeUtilisateurs, Utilisateur utilisateur) {
        listeUtilisateurs.add(utilisateur);
        System.out.println("Utilisateur ajouté : " + utilisateur.getNomComplet());
    }

    /**
     * Supprime un utilisateur par son ID.
     */
    public boolean supprimerUtilisateur(List<Utilisateur> listeUtilisateurs, int id) {
        return listeUtilisateurs.removeIf(u -> u.getId() == id);
    }

    @Override
    public String getInfosRole() {
        return "Administrateur - Accès complet au système";
    }
}
