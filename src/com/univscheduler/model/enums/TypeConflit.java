package com.univscheduler.model.enums;

/**
 * Les différents types de conflits pouvant survenir lors de la planification.
 */
public enum TypeConflit {
    SALLE_OCCUPEE("Salle déjà occupée"),
    ENSEIGNANT_INDISPONIBLE("Enseignant indisponible"),
    CAPACITE_INSUFFISANTE("Capacité insuffisante");

    private final String libelle;

    TypeConflit(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() { return libelle; }

    @Override
    public String toString() { return libelle; }
}

