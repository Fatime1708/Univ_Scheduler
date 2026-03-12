package com.univscheduler.model;

/**
 * Représente le gestionnaire des emplois du temps.
 * C'est lui qui planifie les cours, assigne les salles et résout les conflits.
 */
public class GestionnaireEmploiDuTemps extends Utilisateur {

    // ── Attributs spécifiques ───────────────────────────────────
    private String service;  // Ex: "Scolarité", "Direction des études"

    // ── Constructeur ────────────────────────────────────────────
    public GestionnaireEmploiDuTemps(String nom, String prenom,
                                      String email, String motDePasse, String service) {
        super(nom, prenom, email, motDePasse, "GESTIONNAIRE");
        this.service = service;
    }

    @Override
    public String getInfosRole() {
        return "Gestionnaire EDT - Service : " + service;
    }

    // ── Getters & Setters ───────────────────────────────────────
    public String getService() { return service; }
    public void setService(String service) { this.service = service; }
}

