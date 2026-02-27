package com.univscheduler.service;

import com.univscheduler.model.Conflit;
import com.univscheduler.model.Cours;
import com.univscheduler.model.enums.TypeConflit;

import java.util.ArrayList;
import java.util.List;

/**
 * Service chargé de détecter les conflits dans la planification.
 * C'est le "cerveau" qui vérifie les chevauchements avant d'ajouter un cours.
 * 
 * Pattern utilisé : Service Layer (couche de logique métier)
 */
public class ConflitService {

    // ── Méthodes principales ────────────────────────────────────

    /**
     * Vérifie si un nouveau cours crée des conflits avec les cours existants.
     * 
     * @param nouveauCours  Le cours qu'on veut ajouter
     * @param coursExistants La liste des cours déjà planifiés
     * @return La liste des conflits détectés (vide = pas de conflit)
     */
    public List<Conflit> detecterConflits(Cours nouveauCours, List<Cours> coursExistants) {
        List<Conflit> conflits = new ArrayList<>();

        for (Cours existant : coursExistants) {
            // Ignorer si c'est le même cours
            if (existant.getId() == nouveauCours.getId()) continue;

            // Vérifier le chevauchement de créneau
            if (!nouveauCours.getCreneau().chevauche(existant.getCreneau())) {
                continue; // Pas de chevauchement horaire → pas de conflit possible
            }

            // Conflit de salle : même salle, même créneau
            if (nouveauCours.getSalle().getId() == existant.getSalle().getId()) {
                conflits.add(new Conflit(TypeConflit.SALLE_OCCUPEE, nouveauCours, existant));
            }

            // Conflit d'enseignant : même enseignant, même créneau
            if (nouveauCours.getEnseignant().getId() == existant.getEnseignant().getId()) {
                conflits.add(new Conflit(TypeConflit.ENSEIGNANT_INDISPONIBLE, nouveauCours, existant));
            }

            // Conflit de capacité : salle trop petite
            if (nouveauCours.getNombreEtudiants() > nouveauCours.getSalle().getCapacite()) {
                conflits.add(new Conflit(TypeConflit.CAPACITE_INSUFFISANTE, nouveauCours, existant));
            }
        }

        return conflits;
    }

    /**
     * Vérifie s'il y a des conflits et retourne true si le cours peut être ajouté.
     * 
     * @param nouveauCours   Le cours à tester
     * @param coursExistants Les cours déjà planifiés
     * @return true si aucun conflit n'est détecté
     */
    public boolean peutAjouterCours(Cours nouveauCours, List<Cours> coursExistants) {
        return detecterConflits(nouveauCours, coursExistants).isEmpty();
    }

    /**
     * Retourne tous les conflits non résolus dans une liste de cours.
     * 
     * @param tousLesCours Tous les cours planifiés
     * @return Liste des conflits détectés
     */
    public List<Conflit> getTousLesConflits(List<Cours> tousLesCours) {
        List<Conflit> tousLesConflits = new ArrayList<>();

        // Compare chaque cours avec tous ceux qui suivent (évite les doublons)
        for (int i = 0; i < tousLesCours.size(); i++) {
            for (int j = i + 1; j < tousLesCours.size(); j++) {
                Cours c1 = tousLesCours.get(i);
                Cours c2 = tousLesCours.get(j);

                if (c1.estEnConflitAvec(c2)) {
                    // Déterminer le type de conflit
                    if (c1.getSalle().getId() == c2.getSalle().getId()) {
                        tousLesConflits.add(new Conflit(TypeConflit.SALLE_OCCUPEE, c1, c2));
                    }
                    if (c1.getEnseignant().getId() == c2.getEnseignant().getId()) {
                        tousLesConflits.add(new Conflit(TypeConflit.ENSEIGNANT_INDISPONIBLE, c1, c2));
                    }
                }
            }
        }

        return tousLesConflits;
    }
}
