package com.example.models;

/**
 * Représente une chambre vide ou invalide (Null Object).
 * Permet d’éviter l’utilisation de null pour signaler l’absence de chambre réelle.
 * https://www.geeksforgeeks.org/null-object-design-pattern/
 */
public final class EmptyRoom extends Room {

    private static final EmptyRoom instance = new EmptyRoom();

    private EmptyRoom() {
        super("EMPTY", "Z", -1, -1,-1);
    }

    public static EmptyRoom getInstance() {
        return instance;
    }

    @Override
    public boolean isOccupied() {
        return true; // Empêche toute assignation accidentelle
    }

    @Override
    public void assignTo(Reservation reservation) {
        // Ne rien faire
    }

    @Override
    public void release() {
        // Ne rien faire
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj; // Singleton : comparaison stricte
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(instance);
    }
}
