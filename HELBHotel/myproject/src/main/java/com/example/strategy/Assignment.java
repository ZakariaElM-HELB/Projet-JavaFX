package com.example.strategy;

import com.example.models.Reservation;
import com.example.models.Room;

/**
 * Classe abstraite partagée entre les différentes stratégies d'assignation.
 * Fournit une méthode utilitaire pour vérifier la compatibilité entre une chambre et le motif de séjour.
 */
public abstract class Assignment implements AssignmentStrategy {

    // === Constantes de motifs (reconnaissance simplifiée par mots-clés) ===
    private static final String STAY_PURPOSE_AFFAIRE = "affaire";
    private static final String STAY_PURPOSE_TOURISME = "tourisme";
    private static final String STAY_PURPOSE_AUTRE = "autre";

    /**
     * Vérifie si une chambre correspond au motif de séjour et aux préférences du client.
     * room la chambre potentielle
     * reservation les souhaits du client
     * return true si la chambre est considérée comme appropriée
     */
    protected boolean matchesStayPurpose(Room room, Reservation reservation) {
        String stayPurpose = reservation.getStayPurpose().toLowerCase();
        boolean smoker = reservation.isSmoker();
        boolean hasChildren = reservation.hasChildren();

        // Si le séjour est professionnel, on oriente vers Business
        if (stayPurpose.contains(STAY_PURPOSE_AFFAIRE)) {
            return room.isBusiness();
        }

        // Si le séjour est touristique ou autre
        if (stayPurpose.contains(STAY_PURPOSE_TOURISME) || stayPurpose.contains(STAY_PURPOSE_AUTRE)) {
            // Clients calmes (non-fumeurs et sans enfants) → Luxury
            if (!smoker && !hasChildren) {
                return room.isLuxury();
            }
            // Sinon → Economy
            return room.isEconomy();
        }

        // Cas non reconnu → fallback sur Economy
        return room.isEconomy();
    }
}
