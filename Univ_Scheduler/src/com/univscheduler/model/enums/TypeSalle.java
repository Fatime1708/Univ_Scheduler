package com.univscheduler.model.enums;

/**
 * Les différents types de salles disponibles dans l'université.
 */
public enum TypeSalle {
    TD("Travaux Dirigés"),
    TP("Travaux Pratiques"),
    AMPHI("Amphithéâtre"),
    REUNION("Salle de Réunion");

    private final String libelle;

    TypeSalle(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() { return libelle; }

    @Override
    public String toString() { return libelle; }
}
