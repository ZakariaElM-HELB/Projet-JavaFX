package com.example.utils;

import java.util.Set;

import com.example.models.Reservation;


    
/**
 * Utilitaire pour valider les données d'une réservation.
 */
public class ReservationValidator {

    private static final int MAX_NUMBERS_PERSONS = 4;
    private static final int MIN_NUMBERS_PERSONS = 1;
    private static final Set<String> VALID_PURPOSES = Set.of("Tourisme", "Affaire", "Autre");

    // Vérifie que la réservation est complète et respecte toutes les règles métiers :
    // - nom et prénom non vides
    // - nombre de personnes entre 1 et 4
    // - motif du séjour parmi ceux autorisés (Tourisme, Affaire, Autre)
    // - au moins un adulte (donc enfants < personnes)
    // - pas plus d’enfants que de personnes
    public static boolean isValid(Reservation reservation) {
        if (isEmpty(reservation.getLastName()) || isEmpty(reservation.getFirstName())) return false;

        if (reservation.getNumberOfPeople() < MIN_NUMBERS_PERSONS || reservation.getNumberOfPeople() > MAX_NUMBERS_PERSONS) return false;

        if (isEmpty(reservation.getStayPurpose())) return false;

        if (!VALID_PURPOSES.contains(reservation.getStayPurpose())) return false;

        if (reservation.getNumberOfChildren() > reservation.getNumberOfPeople()) return false;

        if (reservation.getNumberOfChildren() >= reservation.getNumberOfPeople()) return false;

        return true;
    }

    // Vérifie si une chaînea est vide
    private static boolean isEmpty(String text) {
        return text.trim().isEmpty(); // text ne peut jamais être null
    }
    
}
