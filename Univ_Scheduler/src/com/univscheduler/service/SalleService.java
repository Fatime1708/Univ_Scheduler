package com.univscheduler.service;

import com.univscheduler.model.Cours;
import com.univscheduler.model.Creneau;
import com.univscheduler.model.Salle;
import com.univscheduler.model.enums.TypeSalle;

import java.util.ArrayList;
import java.util.List;

/**
 * Service de recherche de salles disponibles.
 * Permet de trouver des salles selon différents critères.
 */
public class SalleService {

    // ── Méthodes de recherche ───────────────────────────────────

    /**
     * Trouve toutes les salles disponibles pour un créneau donné.
     * 
     * @param toutes       Toutes les salles de l'université
     * @param coursActifs  Les cours actuellement planifiés
     * @param creneau      Le créneau pour lequel on cherche une salle
     * @return Liste des salles libres sur ce créneau
     */
    public List<Salle> getSallesDisponibles(List<Salle> toutes,
                                             List<Cours> coursActifs,
                                             Creneau creneau) {
        // On récupère d'abord les IDs des salles déjà occupées sur ce créneau
        List<Integer> sallesOccupees = new ArrayList<>();
        for (Cours cours : coursActifs) {
            if (cours.getCreneau().chevauche(creneau)) {
                sallesOccupees.add(cours.getSalle().getId());
            }
        }

        // Puis on retourne les salles NON occupées
        List<Salle> disponibles = new ArrayList<>();
        for (Salle salle : toutes) {
            if (!sallesOccupees.contains(salle.getId())) {
                disponibles.add(salle);
            }
        }
        return disponibles;
    }

    /**
     * Recherche avancée avec filtres.
     * 
     * @param toutes          Toutes les salles
     * @param coursActifs     Cours planifiés
     * @param creneau         Créneau souhaité
     * @param capaciteMin     Capacité minimale requise (0 = pas de filtre)
     * @param type            Type de salle souhaité (null = pas de filtre)
     * @param equipementRequis Nom d'équipement requis (null = pas de filtre)
     * @return Liste filtrée des salles disponibles
     */
    public List<Salle> rechercherSalles(List<Salle> toutes,
                                         List<Cours> coursActifs,
                                         Creneau creneau,
                                         int capaciteMin,
                                         TypeSalle type,
                                         String equipementRequis) {

        // On part des salles disponibles sur le créneau
        List<Salle> resultats = getSallesDisponibles(toutes, coursActifs, creneau);

        // Filtre par capacité minimale
        if (capaciteMin > 0) {
            resultats.removeIf(s -> s.getCapacite() < capaciteMin);
        }

        // Filtre par type de salle
        if (type != null) {
            resultats.removeIf(s -> s.getType() != type);
        }

        // Filtre par équipement requis
        if (equipementRequis != null && !equipementRequis.isEmpty()) {
            resultats.removeIf(s -> !s.possededEquipement(equipementRequis));
        }

        return resultats;
    }

    /**
     * Calcule le taux d'occupation d'une salle (en pourcentage).
     * 
     * @param salle       La salle à analyser
     * @param tousLesCours Tous les cours planifiés
     * @param nbCreneauxTotal Nombre total de créneaux dans la semaine (ex: 30)
     * @return Le taux d'occupation entre 0.0 et 100.0
     */
    public double calculerTauxOccupation(Salle salle, List<Cours> tousLesCours, int nbCreneauxTotal) {
        long nbCreneauxOccupes = tousLesCours.stream()
                .filter(c -> c.getSalle().getId() == salle.getId())
                .count();

        if (nbCreneauxTotal == 0) return 0.0;
        return (double) nbCreneauxOccupes / nbCreneauxTotal * 100.0;
    }
}
