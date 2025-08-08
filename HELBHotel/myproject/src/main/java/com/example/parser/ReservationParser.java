package com.example.parser;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import com.example.models.Reservation;
import com.example.utils.ReservationValidator;

/**
 * Parseur de fichier CSV de réservations : chargement, sauvegarde, suppression.
 */
public class ReservationParser {

    private static final String SMOKER_YES = "Fumeur";
    private static final String SMOKER_NO = "Non-fumeur";

    private static final String CSV_SEPARATOR = ",";
    private static final int EXPECTED_FIELD_COUNT = 6;

    private final String reservationFilePath;

    public ReservationParser(String reservationFilePath) {
        this.reservationFilePath = reservationFilePath;
    }

    /** Charge les réservations à partir du fichier CSV */
    public List<Reservation> loadReservations() throws IOException {
        List<Reservation> reservations = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(reservationFilePath))) {
            String line;
            int lineNumber = 1;
            while ((line = br.readLine()) != null) {
                if (line.contains("#INVALID")) continue; // Ignore les lignes déjà marquées invalides

                String[] parts = line.split(CSV_SEPARATOR);
                if (parts.length < EXPECTED_FIELD_COUNT) {
                    System.out.println("[Ligne " + lineNumber + "] Incomplète : " + line);
                    lineNumber++;
                    continue;
                }

                try {
                    String lastName = parts[0].trim();
                    String firstName = parts[1].trim();
                    String numberStr = parts[2].trim();
                    String smokerStr = parts[3].trim();
                    String purpose = parts[4].trim();
                    String childrenStr = parts[5].trim();

                    int numberOfPeople = Integer.parseInt(numberStr);
                    if (!(smokerStr.equalsIgnoreCase(SMOKER_YES) || smokerStr.equalsIgnoreCase(SMOKER_NO))) {
                        System.out.println("[Ligne " + lineNumber + "] Valeur fumeur invalide : " + smokerStr);
                        lineNumber++;
                        continue;
                    }

                    boolean smoker = smokerStr.equalsIgnoreCase(SMOKER_YES);
                    int numberOfChildren = Integer.parseInt(childrenStr);

                    Reservation r = new Reservation(lastName, firstName, numberOfPeople, smoker, purpose, numberOfChildren);

                    if (ReservationValidator.isValid(r)) {
                        reservations.add(r);
                    } else {
                        System.out.println("[Ligne " + lineNumber + "] Réservation invalide selon les règles métier : " + lastName + " " + firstName);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("[Ligne " + lineNumber + "] Erreur de format numérique : " + line);
                }

                lineNumber++;
            }
        }
        return reservations;
    }

    

    /** Sauvegarde toutes les réservations dans le fichier (en écrasant le précédent contenu) */
    public void saveReservations(List<Reservation> reservations) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(reservationFilePath, false))) { // false = overwrite
            for (Reservation reservation : reservations) {
                String line = reservation.getLastName() + CSV_SEPARATOR +
                              reservation.getFirstName() + CSV_SEPARATOR +
                              reservation.getNumberOfPeople() + CSV_SEPARATOR +
                              (reservation.isSmoker() ? SMOKER_YES : SMOKER_NO) + CSV_SEPARATOR +
                              reservation.getStayPurpose() + CSV_SEPARATOR +
                              reservation.getNumberOfChildren();
                writer.write(line);
                writer.newLine();
            }
        }
    }
    
  /** Supprime uniquement les lignes valides du fichier CSV, conserve les lignes invalides */
    public void clearFile() throws IOException {
        List<String> invalidLines = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(reservationFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(CSV_SEPARATOR);
                if (parts.length < EXPECTED_FIELD_COUNT) {
                    invalidLines.add(line); // ligne incomplète
                    continue;
                }
                try {
                    String lastName = parts[0].trim();
                    String firstName = parts[1].trim();
                    int numberOfPeople = Integer.parseInt(parts[2].trim());
                    boolean smoker = parts[3].trim().equalsIgnoreCase(SMOKER_YES);
                    String stayPurpose = parts[4].trim();
                    int numberOfChildren = Integer.parseInt(parts[5].trim());

                    Reservation r = new Reservation(lastName, firstName, numberOfPeople, smoker, stayPurpose, numberOfChildren);
                    if (!ReservationValidator.isValid(r)) {
                        invalidLines.add(line); // non valide
                    }
                    // sinon → ligne valide, on ne la garde pas
                } catch (NumberFormatException e) {
                    invalidLines.add(line); // ligne corrompue
                }
            }
        }

        // Réécriture du fichier avec marquage des lignes invalides
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(reservationFilePath, false))) {
            for (String invalidLine : invalidLines) {
                if (!invalidLine.contains("#INVALID")) {
                    invalidLine += " #INVALID"; // Marque la ligne comme traitée
                }
                writer.write(invalidLine);
                writer.newLine();
            }
        }
    }
}
