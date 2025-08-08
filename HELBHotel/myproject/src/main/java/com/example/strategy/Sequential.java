package com.example.strategy;

import com.example.models.Hotel;
import com.example.models.Reservation;
import com.example.models.ReservationManager;
import com.example.models.Room;
import com.example.models.EmptyRoom;

/**
 * Stratégie d'assignation séquentielle.
 * Parcourt toutes les chambres disponibles dans l’ordre et assigne la première compatible avec le séjour.
 */
public class Sequential extends Assignment {

    private static Sequential instance; // Singleton
    private ReservationManager reservationManager;
    private Hotel hotel;

    // Constructeur privé (Singleton)
    private Sequential(Hotel hotel) {
        this.hotel = hotel;
    }

    // Fournit l’instance unique de la stratégie, ou met à jour l’hôtel
    public static Sequential getInstance(Hotel hotel, ReservationManager rm) {
        if (instance == null) {
            instance = new Sequential(hotel);
        } else {
            instance.hotel = hotel;
        }
        instance.reservationManager = rm;
        return instance;
    }
    

    // Parcourt toutes les chambres dans l’ordre et assigne la première qui correspond au profil du client
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
