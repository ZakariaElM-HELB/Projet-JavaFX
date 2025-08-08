package com.example.strategy;

import com.example.models.Hotel;
import com.example.models.Reservation;
import com.example.models.ReservationManager;
import com.example.models.Room;
import com.example.models.EmptyRoom;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Stratégie d'assignation aléatoire.
 * Sélectionne une chambre libre au hasard parmi toutes les disponibles.
 */
public class RandomAssignment implements AssignmentStrategy {

    private static RandomAssignment instance; // Singleton
    private ReservationManager reservationManager;
    private Hotel hotel;

    // Constructeur privé (Singleton)
    private RandomAssignment(Hotel hotel) {
        this.hotel = hotel;
    }

    // Fournit l’instance unique, ou met à jour l’hôtel si l’instance existe déjà
    public static RandomAssignment getInstance(Hotel hotel, ReservationManager rm) {
        if (instance == null) {
            instance = new RandomAssignment(hotel);
        } else {
            instance.hotel = hotel;
        }
        instance.reservationManager = rm;
        return instance;
    }
    

    // Assigne une chambre aléatoirement parmi les chambres libres
    @Override
    public String assignRoom(Reservation reservation) {
        List<Room> availableRooms = new ArrayList<>();

        for (Room room : hotel.getAllRooms()) {
            if (!room.isOccupied()
                && !reservationManager.isRoomAlreadyProposed(room.getLabel())) {
                availableRooms.add(room);
            }
        }
        

        if (availableRooms.isEmpty()) {
            return EmptyRoom.getInstance().getLabel();
        }

        return availableRooms.get(new Random().nextInt(availableRooms.size())).getLabel();
    }
}
