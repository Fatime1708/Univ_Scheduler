
package com.univscheduler.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.univscheduler.model.Cours;
import com.univscheduler.model.Creneau;

import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service d'export PDF de l'emploi du temps.
 * Utilise la bibliothèque iText 5.
 */
public class ExportPDFService {

    // ── Couleurs ────────────────────────────────────────────────
    private static final BaseColor COULEUR_ENTETE     = new BaseColor(44, 62, 80);   // #2c3e50
    private static final BaseColor COULEUR_JOURS      = new BaseColor(52, 73, 94);   // #34495e
    private static final BaseColor COULEUR_HEURES     = new BaseColor(236, 240, 241);// #ecf0f1
    private static final BaseColor COULEUR_COURS_1    = new BaseColor(52, 152, 219); // #3498db
    private static final BaseColor COULEUR_COURS_2    = new BaseColor(46, 204, 113); // #2ecc71
    private static final BaseColor COULEUR_COURS_3    = new BaseColor(155, 89, 182); // #9b59b6
    private static final BaseColor COULEUR_COURS_4    = new BaseColor(230, 126, 34); // #e67e22
    private static final BaseColor COULEUR_COURS_5    = new BaseColor(231, 76, 60);  // #e74c3c
    private static final BaseColor COULEUR_VIDE       = new BaseColor(255, 255, 255);// blanc
    private static final BaseColor COULEUR_TITRE_TEXT = BaseColor.WHITE;

    // ── Jours et heures ────────────────────────────────────────
    private static final String[] JOURS  = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi"};
    private static final String[] HEURES = {"08:00", "09:00", "10:00", "11:00", "12:00",
                                             "13:00", "14:00", "15:00", "16:00", "17:00"};

    // ── Fonts ───────────────────────────────────────────────────
    private static Font fontTitre;
    private static Font fontSousTitre;
    private static Font fontEntete;
    private static Font fontCours;
    private static Font fontHeure;
    private static Font fontNormal;

    static {
        try {
            fontTitre     = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD,   BaseColor.WHITE);
            fontSousTitre = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, BaseColor.WHITE);
            fontEntete    = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD,   BaseColor.WHITE);
            fontCours     = new Font(Font.FontFamily.HELVETICA,  8, Font.BOLD,   BaseColor.WHITE);
            fontHeure     = new Font(Font.FontFamily.HELVETICA,  9, Font.BOLD,   new BaseColor(80, 80, 80));
            fontNormal    = new Font(Font.FontFamily.HELVETICA,  8, Font.NORMAL, new BaseColor(60, 60, 60));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ════════════════════════════════════════════════════════════
    // MÉTHODE PRINCIPALE
    // ════════════════════════════════════════════════════════════

    /**
     * Exporte l'emploi du temps en PDF.
     *
     * @param cours      Liste des cours à afficher
     * @param filePath   Chemin du fichier PDF à créer (ex: "C:/emploi_du_temps.pdf")
     * @param classe     Nom de la classe/promotion (ex: "L3 Informatique")
     * @return true si l'export a réussi, false sinon
     */
    public static boolean exporterEmploiDuTemps(List<Cours> cours, String filePath, String classe) {
        try {
            // Créer le document en format A4 paysage
            Document document = new Document(PageSize.A4.rotate(), 20, 20, 20, 20);
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            // ── En-tête ──────────────────────────────────────
            ajouterEnTete(document, classe);

            // ── Espace ───────────────────────────────────────
            document.add(new Paragraph(" "));

            // ── Grille emploi du temps ────────────────────────
            ajouterGrilleEmploiDuTemps(document, cours);

            // ── Légende ──────────────────────────────────────
            document.add(new Paragraph(" "));
            ajouterLegende(document, cours);

            // ── Pied de page ──────────────────────────────────
            ajouterPiedDePage(document);

            document.close();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ════════════════════════════════════════════════════════════
    // EN-TÊTE
    // ════════════════════════════════════════════════════════════
    private static void ajouterEnTete(Document document, String classe) throws Exception {
        PdfPTable tableEntete = new PdfPTable(2);
        tableEntete.setWidthPercentage(100);
        tableEntete.setWidths(new float[]{3f, 1f});

        // Cellule gauche : titre
        PdfPCell cellTitre = new PdfPCell();
        cellTitre.setBackgroundColor(COULEUR_ENTETE);
        cellTitre.setPadding(15);
        cellTitre.setBorder(Rectangle.NO_BORDER);

        Paragraph titre = new Paragraph("🎓 UNIV-SCHEDULER", fontTitre);
        titre.setSpacingAfter(5);
        Paragraph sousTitre = new Paragraph(
            "Emploi du temps — " + classe + "\n"
            + "Semaine du " + getDateSemaine(),
            fontSousTitre
        );
        cellTitre.addElement(titre);
        cellTitre.addElement(sousTitre);
        tableEntete.addCell(cellTitre);

        // Cellule droite : infos
        PdfPCell cellInfos = new PdfPCell();
        cellInfos.setBackgroundColor(COULEUR_JOURS);
        cellInfos.setPadding(15);
        cellInfos.setBorder(Rectangle.NO_BORDER);
        cellInfos.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellInfos.setVerticalAlignment(Element.ALIGN_MIDDLE);

        Paragraph infos = new Paragraph(
            "Généré le\n" + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
            fontSousTitre
        );
        infos.setAlignment(Element.ALIGN_CENTER);
        cellInfos.addElement(infos);
        tableEntete.addCell(cellInfos);

        document.add(tableEntete);
    }

    // ════════════════════════════════════════════════════════════
    // GRILLE EMPLOI DU TEMPS
    // ════════════════════════════════════════════════════════════
    private static void ajouterGrilleEmploiDuTemps(Document document, List<Cours> cours)
            throws Exception {

        // 6 colonnes : Heure + 5 jours
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1f, 2f, 2f, 2f, 2f, 2f});

        // ── Ligne d'en-tête des jours ──
        // Cellule vide en haut à gauche
        PdfPCell cellVide = new PdfPCell(new Phrase("Heure", fontEntete));
        cellVide.setBackgroundColor(COULEUR_ENTETE);
        cellVide.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellVide.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cellVide.setPadding(8);
        cellVide.setBorderColor(BaseColor.WHITE);
        table.addCell(cellVide);

        for (String jour : JOURS) {
            PdfPCell cellJour = new PdfPCell(new Phrase(jour, fontEntete));
            cellJour.setBackgroundColor(COULEUR_JOURS);
            cellJour.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellJour.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cellJour.setPadding(8);
            cellJour.setBorderColor(BaseColor.WHITE);
            table.addCell(cellJour);
        }

        // ── Lignes des créneaux horaires ──
        for (int i = 0; i < HEURES.length - 1; i++) {
            String heureDebut = HEURES[i];
            String heureFin   = HEURES[i + 1];

            // Colonne heure
            PdfPCell cellHeure = new PdfPCell(
                new Phrase(heureDebut + "\n" + heureFin, fontHeure)
            );
            cellHeure.setBackgroundColor(COULEUR_HEURES);
            cellHeure.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellHeure.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cellHeure.setPadding(6);
            cellHeure.setBorderColor(new BaseColor(200, 200, 200));
            table.addCell(cellHeure);

            // Colonnes des jours
            for (String jour : JOURS) {
                Cours coursCase = trouverCours(cours, jour, heureDebut);

                if (coursCase != null) {
                    // Cellule avec un cours
                    BaseColor couleur = getCouleurCours(coursCase.getMatiere(), cours);
                    PdfPCell cellCours = creerCelluleCours(coursCase, couleur);
                    table.addCell(cellCours);
                } else {
                    // Cellule vide
                    PdfPCell celluleVide = new PdfPCell(new Phrase(""));
                    celluleVide.setBackgroundColor(COULEUR_VIDE);
                    celluleVide.setMinimumHeight(40f);
                    celluleVide.setBorderColor(new BaseColor(220, 220, 220));
                    table.addCell(celluleVide);
                }
            }
        }

        document.add(table);
    }

    // ════════════════════════════════════════════════════════════
    // CELLULE COURS
    // ════════════════════════════════════════════════════════════
    private static PdfPCell creerCelluleCours(Cours cours, BaseColor couleur) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(couleur);
        cell.setPadding(5);
        cell.setBorderColor(BaseColor.WHITE);
        cell.setBorderWidth(2f);
        cell.setMinimumHeight(40f);

        // Matière
        Paragraph matiere = new Paragraph(cours.getMatiere(), fontCours);
        matiere.setSpacingAfter(2);
        cell.addElement(matiere);

        // Salle
        if (cours.getSalle() != null) {
            Font fontDetail = new Font(Font.FontFamily.HELVETICA, 7, Font.NORMAL, BaseColor.WHITE);
            Paragraph salle = new Paragraph("📍 " + cours.getSalle().getNumero(), fontDetail);
            cell.addElement(salle);
        }

        // Enseignant
        if (cours.getEnseignant() != null) {
            Font fontDetail = new Font(Font.FontFamily.HELVETICA, 7, Font.NORMAL, BaseColor.WHITE);
            Paragraph ens = new Paragraph(
                "👤 " + cours.getEnseignant().getPrenom()
                + " " + cours.getEnseignant().getNom(),
                fontDetail
            );
            cell.addElement(ens);
        }

        return cell;
    }

    // ════════════════════════════════════════════════════════════
    // LÉGENDE
    // ════════════════════════════════════════════════════════════
    private static void ajouterLegende(Document document, List<Cours> cours) throws Exception {
        Paragraph titreLegend = new Paragraph("Légende des cours :",
            new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, new BaseColor(44, 62, 80))
        );
        titreLegend.setSpacingAfter(5);
        document.add(titreLegend);

        PdfPTable tableLegend = new PdfPTable(5);
        tableLegend.setWidthPercentage(100);

        // Afficher les matières uniques avec leur couleur
        List<String> matieresDeja = new java.util.ArrayList<>();
        for (Cours c : cours) {
            if (!matieresDeja.contains(c.getMatiere())) {
                matieresDeja.add(c.getMatiere());

                BaseColor couleur = getCouleurCours(c.getMatiere(), cours);
                PdfPCell cell = new PdfPCell();
                cell.setBackgroundColor(couleur);
                cell.setPadding(6);
                cell.setBorderColor(BaseColor.WHITE);

                String info = c.getMatiere();
                if (c.getSalle() != null) info += " — " + c.getSalle().getNumero();
                cell.addElement(new Paragraph(info,
                    new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD, BaseColor.WHITE)
                ));
                tableLegend.addCell(cell);
            }
        }

        // Remplir les cellules vides si nécessaire
        int reste = matieresDeja.size() % 5;
        if (reste != 0) {
            for (int i = 0; i < 5 - reste; i++) {
                PdfPCell cellVide = new PdfPCell(new Phrase(""));
                cellVide.setBorder(Rectangle.NO_BORDER);
                tableLegend.addCell(cellVide);
            }
        }

        document.add(tableLegend);
    }

    // ════════════════════════════════════════════════════════════
    // PIED DE PAGE
    // ════════════════════════════════════════════════════════════
    private static void ajouterPiedDePage(Document document) throws Exception {
        document.add(new Paragraph(" "));
        Paragraph pied = new Paragraph(
            "Document généré automatiquement par UNIV-SCHEDULER — "
            + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
            new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC, BaseColor.GRAY)
        );
        pied.setAlignment(Element.ALIGN_CENTER);
        document.add(pied);
    }

    // ════════════════════════════════════════════════════════════
    // MÉTHODES UTILITAIRES
    // ════════════════════════════════════════════════════════════

    /** Cherche un cours pour un jour et une heure donnés */
    private static Cours trouverCours(List<Cours> cours, String jour, String heure) {
        for (Cours c : cours) {
            Creneau creneau = c.getCreneau();
            if (creneau == null) continue;
            if (creneau.getJour().equalsIgnoreCase(jour)
                    && creneau.getHeureDebut() != null
                    && creneau.getHeureDebut().toString().startsWith(heure.substring(0, 2))) {
                return c;
            }
        }
        return null;
    }

    /** Attribue une couleur unique à chaque matière */
    private static BaseColor getCouleurCours(String matiere, List<Cours> tousLesCours) {
        BaseColor[] couleurs = {
            COULEUR_COURS_1, COULEUR_COURS_2, COULEUR_COURS_3,
            COULEUR_COURS_4, COULEUR_COURS_5
        };
        List<String> matieres = new java.util.ArrayList<>();
        for (Cours c : tousLesCours) {
            if (!matieres.contains(c.getMatiere())) {
                matieres.add(c.getMatiere());
            }
        }
        int index = matieres.indexOf(matiere);
        return couleurs[Math.abs(index) % couleurs.length];
    }

    /** Retourne la date du lundi de la semaine courante */
    private static String getDateSemaine() {
        LocalDate aujourd_hui = LocalDate.now();
        LocalDate lundi = aujourd_hui.minusDays(aujourd_hui.getDayOfWeek().getValue() - 1);
        LocalDate vendredi = lundi.plusDays(4);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return lundi.format(fmt) + " au " + vendredi.format(fmt);
    }
}

