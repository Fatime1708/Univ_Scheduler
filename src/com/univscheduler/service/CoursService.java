package com.univscheduler.service;

import com.univscheduler.model.Conflit;
import com.univscheduler.model.Cours;
import com.univscheduler.model.Enseignant;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service de gestion des cours et de l'emploi du temps.
 * Utilise le ConflitService pour valider chaque ajout.
 */
public class CoursService {

    // ── Dépendance ──────────────────────────────────────────────
    private final ConflitService conflitService;

    // ── Constructeur ────────────────────────────────────────────
    public CoursService() {
        this.conflitService = new ConflitService();
    }

    // ── Méthodes CRUD ────────────────────────────────────────────

    /**
     * Ajoute un cours si aucun conflit n'est détecté.
     * 
     * @param cours          Le cours à ajouter
     * @param coursExistants La liste courante des cours
     * @return true si ajouté, false si un conflit bloque l'ajout
     */
    public boolean ajouterCours(Cours cours, List<Cours> coursExistants) {
        List<Conflit> conflits = conflitService.detecterConflits(cours, coursExistants);

        if (!conflits.isEmpty()) {
            System.out.println("❌ Impossible d'ajouter le cours : " + conflits.size() + " conflit(s) détecté(s).");
            for (Conflit c : conflits) {
                System.out.println("  → " + c);
            }
            return false;
        }

        coursExistants.add(cours);
        System.out.println("✓ Cours ajouté : " + cours);
        return true;
    }

    /**
     * Supprime un cours par son ID.
     */
    public boolean supprimerCours(int coursId, List<Cours> cours) {
        return cours.removeIf(c -> c.getId() == coursId);
    }

    /**
     * Retourne tous les cours d'un enseignant.
     */
    public List<Cours> getCoursParEnseignant(Enseignant enseignant, List<Cours> tousLesCours) {
        List<Cours> result = new ArrayList<>();
        for (Cours c : tousLesCours) {
            if (c.getEnseignant().getId() == enseignant.getId()) {
                result.add(c);
            }
        }
        return result;
    }

    /**
     * Retourne tous les cours d'une classe.
     */
    public List<Cours> getCoursParClasse(String classe, List<Cours> tousLesCours) {
        List<Cours> result = new ArrayList<>();
        for (Cours c : tousLesCours) {
            if (c.getClasse().equalsIgnoreCase(classe)) {
                result.add(c);
            }
        }
        return result;
    }

    /**
     * Retourne tous les cours d'un jour donné.
     */
    public List<Cours> getCoursParJour(String jour, List<Cours> tousLesCours) {
        List<Cours> result = new ArrayList<>();
        for (Cours c : tousLesCours) {
            if (c.getCreneau().getJour().equalsIgnoreCase(jour)) {
                result.add(c);
            }
        }
        return result;
    }
}
