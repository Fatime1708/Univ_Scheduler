package com.univscheduler.model;

import com.univscheduler.model.enums.TypeRapport;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Représente un rapport d'utilisation des salles.
 * Peut être hebdomadaire, mensuel, ou sur l'occupation.
 */
public class Rapport {

    // ── Attributs ──────────────────────────────────────────────
    private int id;
    private String titre;
    private LocalDate dateGeneration; // Date à laquelle le rapport a été généré
    private TypeRapport type;         // HEBDOMADAIRE, MENSUEL, OCCUPATION
    private String contenu;           // Le contenu texte du rapport
    private Administrateur generePar; // Qui a généré ce rapport

    // ── Constructeur ────────────────────────────────────────────
    public Rapport(String titre, TypeRapport type, Administrateur generePar) {
        this.titre = titre;
        this.type = type;
        this.generePar = generePar;
        this.dateGeneration = LocalDate.now(); // Date du jour automatiquement
        this.contenu = "";
    }

    // ── Méthodes ────────────────────────────────────────────────

    /**
     * Génère le contenu du rapport.
     * Dans un projet complet, cette méthode irait chercher les données en BDD.
     */
    public void generer() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ").append(titre).append(" ===\n");
        sb.append("Type : ").append(type.getLibelle()).append("\n");
        sb.append("Généré le : ").append(dateGeneration
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n");
        sb.append("Par : ").append(generePar.getNomComplet()).append("\n");
        sb.append("─────────────────────────────\n");
        sb.append("(Données à compléter depuis la base de données)\n");
        this.contenu = sb.toString();
        System.out.println("Rapport généré : " + titre);
    }

    /**
     * Exporte le rapport dans un format donné.
     * @param format "PDF" ou "EXCEL"
     */
    public void exporter(String format) {
        System.out.println("Export en " + format + " : " + titre);
        // Dans un projet complet : appel à une librairie PDF (iText) ou Excel (Apache POI)
    }

    // ── Getters & Setters ───────────────────────────────────────
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public LocalDate getDateGeneration() { return dateGeneration; }
    public void setDateGeneration(LocalDate dateGeneration) { this.dateGeneration = dateGeneration; }

    public TypeRapport getType() { return type; }
    public void setType(TypeRapport type) { this.type = type; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public Administrateur getGenerePar() { return generePar; }
    public void setGenerePar(Administrateur generePar) { this.generePar = generePar; }

    @Override
    public String toString() {
        return titre + " | " + type + " | " + dateGeneration;
    }
}
