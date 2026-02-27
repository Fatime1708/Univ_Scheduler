package com.univscheduler;

import com.univscheduler.model.*;
import com.univscheduler.model.enums.TypeSalle;
import com.univscheduler.service.CoursService;
import com.univscheduler.service.SalleService;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe principale de test du projet UNIV-SCHEDULER.
 * Permet de vérifier que toutes les classes fonctionnent correctement
 * avant de connecter la base de données et l'interface JavaFX.
 */
public class Main {

    public static void main(String[] args) {

        System.out.println("═══════════════════════════════════════════");
        System.out.println("       UNIV-SCHEDULER - Test du modèle     ");
        System.out.println("═══════════════════════════════════════════\n");

        // ── 1. Créer un bâtiment et des salles ──────────────────
        Batiment batA = new Batiment("Bâtiment A", "Campus Principal", 3);
        batA.setId(1);

        Salle salleA101 = new Salle("A101", 40, TypeSalle.TD, 1);
        salleA101.setId(1);
        Equipement projecteur = new Equipement("Vidéoprojecteur", "Epson EB-S41");
        salleA101.ajouterEquipement(projecteur);
        batA.ajouterSalle(salleA101);

        Salle salleA102 = new Salle("A102", 30, TypeSalle.TD, 1);
        salleA102.setId(2);
        batA.ajouterSalle(salleA102);

        System.out.println("✓ Infrastructure créée : " + batA);
        System.out.println("  → " + salleA101);
        System.out.println("  → " + salleA102 + "\n");

        // ── 2. Créer des utilisateurs ────────────────────────────
        Enseignant prof = new Enseignant("Diallo", "Moussa", "m.diallo@univ.sn",
                "pass123", "Informatique", "UFR Sciences");
        prof.setId(1);

        Etudiant etudiant = new Etudiant("Ndiaye", "Fatou", "f.ndiaye@etu.sn",
                "pass456", "L2 Informatique", "Groupe A", "2024001");
        etudiant.setId(1);

        System.out.println("✓ Utilisateurs créés :");
        System.out.println("  → " + prof.getNomComplet() + " | " + prof.getInfosRole());
        System.out.println("  → " + etudiant.getNomComplet() + " | " + etudiant.getInfosRole() + "\n");

        // ── 3. Créer des créneaux et des cours ───────────────────
        Creneau creneau1 = new Creneau("Lundi", LocalTime.of(8, 0), 120);  // 08h-10h
        creneau1.setId(1);
        Creneau creneau2 = new Creneau("Lundi", LocalTime.of(9, 0), 90);   // 09h-10h30 (chevauchement !)
        creneau2.setId(2);
        Creneau creneau3 = new Creneau("Lundi", LocalTime.of(10, 0), 120); // 10h-12h (pas de conflit)
        creneau3.setId(3);

        Cours cours1 = new Cours("Algorithmique", "L2 Informatique", "Groupe A",
                prof, salleA101, creneau1);
        cours1.setId(1);
        cours1.ajouterEtudiant(etudiant);

        Cours cours2 = new Cours("Base de données", "L2 Informatique", "Groupe A",
                prof, salleA101, creneau2); // Même salle + même prof + chevauchement = CONFLIT
        cours2.setId(2);

        Cours cours3 = new Cours("Réseaux", "L2 Informatique", "Groupe A",
                prof, salleA102, creneau3); // Salle différente, pas de conflit
        cours3.setId(3);

        // ── 4. Tester la détection de conflits ───────────────────
        System.out.println("── Test des créneaux ──────────────────────");
        System.out.println("Créneau 1 : " + creneau1);
        System.out.println("Créneau 2 : " + creneau2);
        System.out.println("Chevauchement 1↔2 : " + creneau1.chevauche(creneau2)); // Attendu : true
        System.out.println("Chevauchement 1↔3 : " + creneau1.chevauche(creneau3) + "\n"); // Attendu : false

        CoursService coursService = new CoursService();
        List<Cours> emploiDuTemps = new ArrayList<>();

        System.out.println("── Ajout des cours ────────────────────────");
        coursService.ajouterCours(cours1, emploiDuTemps); // Doit réussir
        coursService.ajouterCours(cours2, emploiDuTemps); // Doit échouer (conflit)
        coursService.ajouterCours(cours3, emploiDuTemps); // Doit réussir

        // ── 5. Recherche de salles disponibles ───────────────────
        System.out.println("\n── Recherche de salles disponibles ────────");
        SalleService salleService = new SalleService();
        List<Salle> toutesLesSalles = batA.getSalles();

        List<Salle> disponibles = salleService.getSallesDisponibles(
                toutesLesSalles, emploiDuTemps, creneau1);
        System.out.println("Salles libres le " + creneau1 + " :");
        for (Salle s : disponibles) {
            System.out.println("  → " + s);
        }

        System.out.println("\n═══════════════════════════════════════════");
        System.out.println("         Tests terminés avec succès !");
        System.out.println("═══════════════════════════════════════════");
    }
}
