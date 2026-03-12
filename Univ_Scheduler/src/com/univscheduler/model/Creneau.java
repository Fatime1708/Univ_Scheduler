package com.univscheduler.model;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Représente un créneau horaire (jour + heure début + durée).
 * C'est le "quand" d'un cours.
 * 
 * Exemple : Lundi de 08h00 à 10h00 (durée = 120 minutes)
 */
public class Creneau {

    // ── Attributs ──────────────────────────────────────────────
    private int id;
    private String jour;          // Ex: "Lundi", "Mardi"
    private LocalTime heureDebut; // Ex: 08:00
    private int dureMinutes;      // Ex: 120 (pour 2h de cours)

    // ── Constructeur ────────────────────────────────────────────
    /**
     * @param jour        Le jour de la semaine (ex: "Lundi")
     * @param heureDebut  L'heure de début (ex: LocalTime.of(8, 0))
     * @param dureMinutes La durée en minutes (ex: 90 pour 1h30)
     */
    public Creneau(String jour, LocalTime heureDebut, int dureMinutes) {
        this.jour = jour;
        this.heureDebut = heureDebut;
        this.dureMinutes = dureMinutes;
    }

    // ── Méthodes ────────────────────────────────────────────────

    /**
     * Calcule et retourne l'heure de fin du créneau.
     * Ex: si début = 08:00 et durée = 120 min → fin = 10:00
     */
    public LocalTime getHeureFin() {
        return heureDebut.plusMinutes(dureMinutes);
    }

    /**
     * Vérifie si ce créneau chevauche (se superpose) avec un autre.
     * Utilisé pour détecter les conflits d'emploi du temps.
     * 
     * Logique : deux créneaux se chevauchent si :
     *   - Ils sont le même jour
     *   - L'un commence avant que l'autre se termine
     * 
     * @param autre L'autre créneau à comparer
     * @return true si les deux créneaux se chevauchent
     */
    public boolean chevauche(Creneau autre) {
        // D'abord, vérifier que c'est le même jour
        if (!this.jour.equalsIgnoreCase(autre.jour)) {
            return false; // Jours différents → pas de conflit possible
        }

        // Ensuite, vérifier le chevauchement des heures
        // Chevauchement si : début1 < fin2 ET début2 < fin1
        return this.heureDebut.isBefore(autre.getHeureFin())
                && autre.heureDebut.isBefore(this.getHeureFin());
    }

    // ── Getters & Setters ───────────────────────────────────────
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getJour() { return jour; }
    public void setJour(String jour) { this.jour = jour; }

    public LocalTime getHeureDebut() { return heureDebut; }
    public void setHeureDebut(LocalTime heureDebut) { this.heureDebut = heureDebut; }

    public int getDureMinutes() { return dureMinutes; }
    public void setDureMinutes(int dureMinutes) { this.dureMinutes = dureMinutes; }

    @Override
    public String toString() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH'h'mm");
        return jour + " " + heureDebut.format(fmt) + " → " + getHeureFin().format(fmt);
    }
}
