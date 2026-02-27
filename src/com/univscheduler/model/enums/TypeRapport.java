package com.univscheduler.model.enums;

/**
 * Les différents types de rapports générables.
 */
public enum TypeRapport {
    HEBDOMADAIRE("Rapport hebdomadaire"),
    MENSUEL("Rapport mensuel"),
    OCCUPATION("Rapport d'occupation");

    private final String libelle;

    TypeRapport(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() { return libelle; }

    @Override
    public String toString() { return libelle; }
}
