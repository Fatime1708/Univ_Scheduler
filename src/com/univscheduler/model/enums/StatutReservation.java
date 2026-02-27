package com.univscheduler.model.enums;

/**
 * Les différents statuts possibles pour une réservation.
 */
public enum StatutReservation {
    EN_ATTENTE("En attente"),
    CONFIRMEE("Confirmée"),
    ANNULEE("Annulée");

    private final String libelle;

    StatutReservation(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() { return libelle; }

    @Override
    public String toString() { return libelle; }
}
