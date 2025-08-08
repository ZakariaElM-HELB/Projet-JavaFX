package com.example.strategy;

import com.example.models.Hotel;
import com.example.models.Reservation;
import com.example.models.ReservationManager;
import com.example.models.Room;
import com.example.models.EmptyRoom;

/**
 * Stratégie d'assignation pour les zones calmes.
 * Applique des règles spécifiques :
 * - Fumeurs → colonnes avec fenêtre (colonne 0)
 * - Familles avec enfants → pas de voisins adultes seuls
 */
public class QuietZone implements AssignmentStrategy {

    private static QuietZone instance; // Singleton
    private ReservationManager reservationManager;
    private Hotel hotel;

    // === Règles constantes ===
    private static final int QUIET_COLUMN_WITH_WINDOW = 0;
    private static final int ADJACENT_COLUMN_DISTANCE = 1;

    // Constructeur privé (Singleton)
    private QuietZone(Hotel hotel) {
        this.hotel = hotel;
    }

    // Fournit l’instance unique de la stratégie, met à jour l’hôtel si nécessaire
    public static QuietZone getInstance(Hotel hotel, ReservationManager rm) {
        if (instance == null) {
            instance = new QuietZone(hotel);
        } else {
            instance.hotel = hotel;
        }
        instance.reservationManager = rm;
        return instance;
    }


    /**
     * Tente d’assigner une chambre selon les contraintes de tranquillité :
     * - fumeurs en colonne 0,
     * - familles avec enfants séparées des adultes sans enfants.
     */
    @Override
    public String assignRoom(Reservation reservation) {
        boolean smoker = reservation.isSmoker();
        boolean hasChildren = reservation.hasChildren();

        for (Room room : hotel.getAllRooms()) {
            if (room.isOccupied()) continue;
            if (reservationManager.isRoomAlreadyProposed(room.getLabel())) continue;
        
            if (smoker && room.getColumn() != QUIET_COLUMN_WITH_WINDOW) continue;
            if (hasChildren && !isSurroundedByCompatibleFamilies(room)) continue;
        
            return room.getLabel();
        }
        

        return EmptyRoom.getInstance().getLabel(); // Aucun résultat compatible
    }

    /**
     * Vérifie que les voisins directs (colonne -1 et +1 sur le même étage) sont compatibles :
     * - soit vides
     * - soit occupés par des familles avec enfants.
     */
    private boolean isSurroundedByCompatibleFamilies(Room room) {
        int floor = room.getFloor();
        int col = room.getColumn();

        for (Room other : hotel.getAllRooms()) {
            if (!other.isOccupied()) continue;
            if (other.getFloor() != floor) continue;

            int otherCol = other.getColumn();
            if (Math.abs(otherCol - col) == ADJACENT_COLUMN_DISTANCE) {
                Reservation res = other.getAssignedReservation(); // jamais null
                if (!res.hasChildren()) {
                    return false;
                }
            }
        }
        return true;
    }
}
