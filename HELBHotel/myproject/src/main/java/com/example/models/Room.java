package com.example.models;

import com.example.observer.RoomObserver;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente une chambre d'hôtel avec un type, un label, et une position dans la grille.
 */
public class Room {

    // === Types de chambre reconnus ===
    private static final String TYPE_LUXURY = "L";
    private static final String TYPE_BUSINESS = "B";
    private static final String TYPE_ECONOMY = "E";

    // === Textes pour info-bulles (tooltips) ===
    private static final String TOOLTIP_OCCUPIED = "Occupée par : ";
    private static final String TOOLTIP_FREE = "Libre";

    private final List<RoomObserver> observers = new ArrayList<>();

    private final String label;
    private final String type;
    private final int floor;
    private final int column;
    private final int floorIndex;

    private Reservation assignedReservation = EmptyReservation.getInstance();

    public Room(String label, String type, int floor, int column, int floorIndex) {
        this.label = label;
        this.type = type;
        this.floor = floor;
        this.column = column;
        this.floorIndex = floorIndex;
    }

    public String getLabel() {
        return label;
    }

    public String getType() {
        return type;
    }

    public int getFloor() {
        return floor;
    }

    public int getColumn() {
        return column;
    }

    public int getFloorIndex() {
        return this.floorIndex;
    }

    /** Indique si la chambre est occupée (réservation présente) */
    public boolean isOccupied() {
        return !(assignedReservation instanceof EmptyReservation);
    }

    /** Assigne une réservation à cette chambre et notifie les observateurs */
    public void assignTo(Reservation reservation) {
        this.assignedReservation = reservation;
        for (RoomObserver obs : new ArrayList<>(observers)) {
            obs.onRoomAssigned(this.label);
        }
    }

    /** Libère la chambre et notifie les observateurs */
    public void release() {
        this.assignedReservation = EmptyReservation.getInstance();
        for (RoomObserver obs : new ArrayList<>(observers)) {
            obs.onRoomReleased(this.label);
        }
    }

    public Reservation getAssignedReservation() {
        return assignedReservation;
    }

    /** Retourne vrai si le type de chambre est "L" (Luxury) */
    public boolean isLuxury() {
        return TYPE_LUXURY.equalsIgnoreCase(type);
    }

    /** Retourne vrai si le type est "E" (Economy) */
    public boolean isEconomy() {
        return TYPE_ECONOMY.equalsIgnoreCase(type);
    }

    /** Retourne vrai si le type est "B" (Business) */
    public boolean isBusiness() {
        return TYPE_BUSINESS.equalsIgnoreCase(type);
    }

    /** Donne un texte résumé sur la chambre, utilisé comme tooltip */
    public String getTooltipText() {
        return isOccupied()
                ? TOOLTIP_OCCUPIED + assignedReservation.getLastName()
                : TOOLTIP_FREE;
    }

    public void addObserver(RoomObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(RoomObserver observer) {
        observers.remove(observer);
    }
}
