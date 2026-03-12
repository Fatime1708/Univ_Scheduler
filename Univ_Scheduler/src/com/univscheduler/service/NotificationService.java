
package com.univscheduler.service;

import com.univscheduler.model.*;
import com.univscheduler.model.enums.TypeConflit;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Service de gestion des notifications et alertes de conflits.
 * Détecte automatiquement les conflits entre cours, réservations et salles.
 */
public class NotificationService {

    // Liste de toutes les notifications générées
    private static final List<Notification> notifications = new ArrayList<>();

    // ─────────────────────────────────────────────────────────────
    // CLASSE INTERNE : Notification
    // ─────────────────────────────────────────────────────────────
    public static class Notification {

        public enum Type { CONFLIT, AVERTISSEMENT, INFO }
        public enum Statut { NON_LUE, LUE }

        private final String id;
        private final String titre;
        private final String message;
        private final Type type;
        private Statut statut;
        private final LocalDateTime dateCreation;
        private final String entiteType;  // "Cours", "Reservation", etc.
        private final int entiteId;

        public Notification(String titre, String message, Type type,
                            String entiteType, int entiteId) {
            this.id           = UUID.randomUUID().toString();
            this.titre        = titre;
            this.message      = message;
            this.type         = type;
            this.statut       = Statut.NON_LUE;
            this.dateCreation = LocalDateTime.now();
            this.entiteType   = entiteType;
            this.entiteId     = entiteId;
        }

        // Getters
        public String getId()                { return id; }
        public String getTitre()             { return titre; }
        public String getMessage()           { return message; }
        public Type getType()                { return type; }
        public Statut getStatut()            { return statut; }
        public LocalDateTime getDateCreation() { return dateCreation; }
        public String getEntiteType()        { return entiteType; }
        public int getEntiteId()             { return entiteId; }
        public boolean isNonLue()            { return statut == Statut.NON_LUE; }

        public void marquerCommeLue() {
            this.statut = Statut.LUE;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // DÉTECTION DE CONFLITS ENTRE COURS
    // ─────────────────────────────────────────────────────────────

    /**
     * Vérifie si un nouveau cours entre en conflit avec la liste existante.
     * Crée automatiquement une notification si conflit détecté.
     */
    public static List<Conflit> detecterConflits(Cours nouveauCours, List<Cours> tousLesCours) {
        List<Conflit> conflitsTrouves = new ArrayList<>();

        for (Cours existant : tousLesCours) {
            // Ignorer le cours lui-même
            if (existant.getId() == nouveauCours.getId()) continue;

            Creneau c1 = nouveauCours.getCreneau();
            Creneau c2 = existant.getCreneau();

            if (c1 == null || c2 == null) continue;

            // Même créneau horaire ?
            if (!c1.chevauche(c2)) continue;

            // Conflit de salle ?
            if (nouveauCours.getSalle() != null && existant.getSalle() != null
                    && nouveauCours.getSalle().getId() == existant.getSalle().getId()) {

            	Conflit conflit = new Conflit(
            		    TypeConflit.SALLE_OCCUPEE,
            		    nouveauCours, existant
            		);
                conflitsTrouves.add(conflit);

                // Créer la notification
                ajouterNotification(new Notification(
                    "⚠️ Conflit de salle",
                    "La salle " + nouveauCours.getSalle().getNumero()
                    + " est déjà réservée pour \"" + existant.getMatiere()
                    + "\" au même créneau.",
                    Notification.Type.CONFLIT,
                    "Cours", nouveauCours.getId()
                ));
            }

            // Conflit d'enseignant ?
            if (nouveauCours.getEnseignant() != null && existant.getEnseignant() != null
                    && nouveauCours.getEnseignant().getId() == existant.getEnseignant().getId()) {

            	Conflit conflit = new Conflit(
            		    TypeConflit.ENSEIGNANT_INDISPONIBLE,
            		    nouveauCours, existant
            		);
                conflitsTrouves.add(conflit);

                ajouterNotification(new Notification(
                    "⚠️ Conflit enseignant",
                    "L'enseignant " + nouveauCours.getEnseignant().getPrenom()
                    + " " + nouveauCours.getEnseignant().getNom()
                    + " a déjà un cours \"" + existant.getMatiere()
                    + "\" au même créneau.",
                    Notification.Type.CONFLIT,
                    "Cours", nouveauCours.getId()
                ));
            }
        }
     // Conflit de capacité insuffisante ?
        if (nouveauCours.getSalle() != null 
                && nouveauCours.getNombreEtudiants() > nouveauCours.getSalle().getCapacite()) {

            ajouterNotification(new Notification(
                "⚠️ Capacité insuffisante",
                "La salle " + nouveauCours.getSalle().getNumero()
                + " a une capacité de " + nouveauCours.getSalle().getCapacite()
                + " places mais le cours \"" + nouveauCours.getMatiere()
                + "\" a " + nouveauCours.getNombreEtudiants() + " étudiants.",
                Notification.Type.CONFLIT,
                "Cours", nouveauCours.getId()
            ));
        }

       

        return conflitsTrouves;
    }

    // ─────────────────────────────────────────────────────────────
    // DÉTECTION DE CONFLITS POUR UNE RÉSERVATION
    // ─────────────────────────────────────────────────────────────

    /**
     * Vérifie si une réservation entre en conflit avec les cours existants.
     */
    public static boolean detecterConflitReservation(Reservation reservation,
                                                      List<Cours> tousLesCours) {
        for (Cours cours : tousLesCours) {
            if (cours.getSalle() == null || reservation.getSalle() == null) continue;
            if (cours.getSalle().getId() != reservation.getSalle().getId()) continue;

            Creneau cCours = cours.getCreneau();
            if (cCours == null) continue;

            // Vérification simple de chevauchement par jour et heure

            if (cCours.getJour().equalsIgnoreCase(
                    reservation.getCreneau().getJour())) {
                ajouterNotification(new Notification(
                    "⚠️ Conflit de réservation",
                    "La salle " + reservation.getSalle().getNumero()
                    + " est déjà occupée par le cours \""
                    + cours.getMatiere() + "\" ce jour-là.",
                    Notification.Type.CONFLIT,
                    "Reservation", reservation.getId()
                ));
                return true;
            }
        }
        return false;
    }

    // ─────────────────────────────────────────────────────────────
    // GESTION DES NOTIFICATIONS
    // ─────────────────────────────────────────────────────────────

    /** Ajoute une notification à la liste globale */
    public static void ajouterNotification(Notification n) {
        notifications.add(0, n); // Ajouter en tête de liste (plus récent en premier)
    }

    /** Ajoute une notification d'information simple */
    public static void ajouterInfo(String titre, String message) {
        ajouterNotification(new Notification(
            titre, message,
            Notification.Type.INFO,
            "Système", 0
        ));
    }

    /** Ajoute un avertissement */
    public static void ajouterAvertissement(String titre, String message) {
        ajouterNotification(new Notification(
            titre, message,
            Notification.Type.AVERTISSEMENT,
            "Système", 0
        ));
    }

    /** Retourne toutes les notifications */
    public static List<Notification> getToutesLesNotifications() {
        return Collections.unmodifiableList(notifications);
    }

    /** Retourne uniquement les notifications non lues */
    public static List<Notification> getNonLues() {
        List<Notification> result = new ArrayList<>();
        for (Notification n : notifications) {
            if (n.isNonLue()) result.add(n);
        }
        return result;
    }

    /** Nombre de notifications non lues (pour le badge) */
    public static int getNombreNonLues() {
        return (int) notifications.stream().filter(Notification::isNonLue).count();
    }

    /** Marquer une notification comme lue */
    public static void marquerCommeLue(String id) {
        for (Notification n : notifications) {
            if (n.getId().equals(id)) {
                n.marquerCommeLue();
                break;
            }
        }
    }

    /** Marquer toutes les notifications comme lues */
    public static void marquerToutesCommeLues() {
        notifications.forEach(Notification::marquerCommeLue);
    }

    /** Supprimer une notification */
    public static void supprimer(String id) {
        notifications.removeIf(n -> n.getId().equals(id));
    }

    /** Vider toutes les notifications */
    public static void viderTout() {
        notifications.clear();
    }

    // ─────────────────────────────────────────────────────────────
    // INITIALISATION : notifications de test
    // ─────────────────────────────────────────────────────────────

    /** Charge des notifications de démonstration au démarrage */
    public static void initialiserNotificationsDeTest() {
        ajouterNotification(new Notification(
            "⚠️ Conflit de salle détecté",
            "La salle B201 est doublement réservée le Mardi de 10h à 12h : cours 'Algo' et 'Réseaux'.",
            Notification.Type.CONFLIT, "Cours", 1
        ));
        ajouterNotification(new Notification(
            "⚠️ Enseignant en double",
            "M. Diallo est assigné à 2 cours simultanés le Lundi de 8h à 10h.",
            Notification.Type.CONFLIT, "Cours", 2
        ));
        ajouterNotification(new Notification(
            "ℹ️ Nouveau cours ajouté",
            "Le cours 'Base de données' a été ajouté à l'emploi du temps de L3 Informatique.",
            Notification.Type.INFO, "Cours", 3
        ));
        ajouterNotification(new Notification(
            "⚠️ Salle en maintenance",
            "La salle A101 sera indisponible du 10 au 14 mars pour travaux.",
            Notification.Type.AVERTISSEMENT, "Salle", 1
        ));
    }
    
}
