package com.example.models;

/**
 * Représente une réservation vide ou invalide (Null Object).
 * Implémente le Singleton pour garantir une seule instance globale.
 * https://www.geeksforgeeks.org/null-object-design-pattern/
 */
public final class EmptyReservation extends Reservation {

    private static final EmptyReservation instance = new EmptyReservation();

    private EmptyReservation() {
        super("EMPTY", "EMPTY", 0, false, "EMPTY", 0);
    }

    public static EmptyReservation getInstance() {
        return instance;
    }

    @Override
    public String toString() {
        return "[Aucune réservation]";
    }

    @Override
    public String getKey() {
        return "EMPTY";
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj; // singleton : égalité stricte par instance
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(instance);
    }
}
