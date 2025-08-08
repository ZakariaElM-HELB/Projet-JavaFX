package com.example.models.lottery;

import com.example.models.Room;

/**
 * Fabrique de tickets de loterie : génère un ticket (Bronze, Silver ou Gold)
 * en fonction des caractéristiques de la chambre, de l'évaluation client et de l'étage.
 */
public class LotteryTicketFactory {

    // Pondération selon le type de chambre
    public static final int LUXURY_WEIGHT = 2;     // Bonus de score pour une chambre Luxury
    public static final int BUSINESS_WEIGHT = 1;   // Bonus de score pour une chambre Business

    // Seuils de score pour déterminer le type de ticket
    public static final int GOLD_THRESHOLD = 7;    // Score minimal pour un ticket Gold
    public static final int SILVER_THRESHOLD = 5;  // Score minimal pour un ticket Silver

    /**
     * Crée un ticket en fonction de la chambre, de la note du client, et de l'étage.
     * room la chambre concernée
     * rating la note donnée par le client (de 1 à 5 généralement)
     * floor l'étage sur lequel se situe la chambre (utilisé pour GoldTicket)
     * retourne un objet LotteryTicket (Gold, Silver ou Bronze)
     */
    public static LotteryTicket createTicket(Room room, int rating, int floor) {
        int score = calculateScore(room, rating);

        if (score >= GOLD_THRESHOLD) {
            return new GoldTicket(floor);
        } else if (score >= SILVER_THRESHOLD) {
            return new SilverTicket();
        } else {
            return new BronzeTicket();
        }
    }

    /**
     * Calcule le score d’un client basé sur la chambre et la note donnée.
     * room la chambre concernée
     * rating la note de satisfaction donnée par le client
     * retourne un score entier utilisé pour déterminer le type de ticket
     */
    private static int calculateScore(Room room, int rating) {
        int score = 0;
        if (room.isLuxury()) score += LUXURY_WEIGHT;
        if (room.isBusiness()) score += BUSINESS_WEIGHT;
        score += rating;
        return score;
    }

    /**
     * Méthode utilitaire pour obtenir la réduction associée à un ticket.
     * ticket un objet LotteryTicket
     * retourne le pourcentage de réduction correspondant
     */
    public static LotteryTicket generateTicket(Room room, int rating) {
        char floorLetter = room.getLabel().charAt(0); // ex: 'A', 'B', ...
        int floorIndex = floorLetter - 'A';
        return LotteryTicketFactory.createTicket(room, rating, floorIndex);
    }
}
