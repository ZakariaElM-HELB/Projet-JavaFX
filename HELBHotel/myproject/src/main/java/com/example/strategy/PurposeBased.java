package com.example.strategy;

import com.example.models.EmptyRoom;
import com.example.models.Hotel;
import com.example.models.Reservation;
import com.example.models.ReservationManager;
import com.example.models.Room;

/**
 * Stratégie d'assignation basée sur le motif du séjour.
 * Utilise les préférences issues de la réservation pour choisir le bon type de chambre.
 */
public class PurposeBased extends Assignment {

    private static PurposeBased instance; // Singleton
    private ReservationManager reservationManager;
    private Hotel hotel;

    // Constructeur privé (Singleton)
    private PurposeBased(Hotel hotel) {
        this.hotel = hotel;
    }

    // Fournit l'instance unique de la stratégie, avec mise à jour de l'hôtel si nécessaire
    public static PurposeBased getInstance(Hotel hotel, ReservationManager rm) {
        if (instance == null) {
            instance = new PurposeBased(hotel);
        } else {
            instance.hotel = hotel;
        }
        instance.reservationManager = rm;
        return instance;
    }
    

    // Cherche la première chambre libre qui correspond au motif du séjour
    @Override
    public String assignRoom(Reservation reservation) {
        for (Room room : hotel.getAllRooms()) {
            if (!room.isOccupied()
                && !reservationManager.isRoomAlreadyProposed(room.getLabel())
                && matchesStayPurpose(room, reservation)) {
                return room.getLabel();
            }
        }
        return EmptyRoom.getInstance().getLabel();
    }
    

}
