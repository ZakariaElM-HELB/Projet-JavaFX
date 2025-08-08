package com.example;

import com.example.models.Reservation;
import com.example.parser.ReservationParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class ReservationParserTest {

    // === TEST PARAMÉTRIQUE : lignes valides ===
    @ParameterizedTest(name = "[Valide #{index}] {0}")
    @MethodSource("validReservationLines")
    public void shouldParseValidReservation(String line, String expectedFullName) throws Exception {
        File tempFile = writeLinesToTempFile(line);
        ReservationParser parser = new ReservationParser(tempFile.getAbsolutePath());

        List<Reservation> reservations = parser.loadReservations();

        assertEquals(1, reservations.size(), "Une seule réservation valide doit être chargée");
        Reservation res = reservations.get(0);
        String fullName = res.getFirstName() + " " + res.getLastName();
        assertEquals(expectedFullName, fullName);
    }

    private static Stream<Object[]> validReservationLines() {
        return Stream.of(
            new Object[]{"Dupont,Jean,2,Fumeur,Tourisme,1", "Jean Dupont"},
            new Object[]{"Martin,Alice,1,Non-fumeur,Affaire,0", "Alice Martin"},
            new Object[]{"Durand,Claire,3,Fumeur,Autre,2", "Claire Durand"}
        );
    }


    // === TEST PARAMÉTRIQUE : lignes invalides + vérification des messages console ===
    @ParameterizedTest(name = "[Invalide #{index}] {1}")
    @MethodSource("invalidReservationLines")
    public void shouldReportInvalidReservations(String line, String description) throws Exception {
        File tempFile = writeLinesToTempFile(line);

        // Capture la sortie console
        ByteArrayOutputStream outputCapture = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputCapture));

        ReservationParser parser = new ReservationParser(tempFile.getAbsolutePath());
        List<Reservation> reservations = parser.loadReservations();

        // Restaure la console
        System.setOut(originalOut);

        assertTrue(reservations.isEmpty(), "Aucune réservation ne doit être chargée pour : " + description);

        String consoleOutput = outputCapture.toString();
        assertTrue(consoleOutput.toLowerCase().contains("ligne") || consoleOutput.toLowerCase().contains("format"),
            "Un message d’erreur devait apparaître pour : " + description + "\nSortie : " + consoleOutput);
    }

    private static Stream<Object[]> invalidReservationLines() {
        return Stream.of(
            new Object[]{"BadLine", "ligne incomplète"},
            new Object[]{"Dupont,Jean,abc,Fumeur,Loisir,1", "nombre de personnes invalide"},
            new Object[]{"Martin,Alice,2,peut-être,Affaire,0", "valeur booléenne invalide"},
            new Object[]{"Lemoine,Sarah,2,Fumeur,Pizza,1", "motif invalide"},
            new Object[]{"Durand,Claire,2,Fumeur", "champ manquant"}
            
        );
    }


    // === clearFile() ne conserve que les lignes invalides
    @Test
    public void shouldPreserveOnlyInvalidLines_afterClearFile() throws Exception {
        File tempFile = File.createTempFile("reservations", ".csv");
        tempFile.deleteOnExit();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            writer.write("Dupont,Jean,2,Fumeur,Tourisme,1\n"); // valide
            writer.write("BadLine\n"); // invalide
            writer.write("Martin,Alice,1,Non-fumeur,Affaire,0\n"); // valide
            writer.write("Durand,Claire,abc,oui,Loisir,0\n"); // invalide
        }

        ReservationParser parser = new ReservationParser(tempFile.getAbsolutePath());
        parser.clearFile();

        List<String> remainingLines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(tempFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                remainingLines.add(line);
            }
        }

        assertEquals(2, remainingLines.size(), "Seules les 2 lignes invalides doivent rester");
        assertTrue(remainingLines.get(0).contains("BadLine"));
        assertTrue(remainingLines.get(1).contains("abc"));
    }

    // === Vérifie que saveReservations() écrit correctement les données
    @Test
    public void shouldSaveAllReservationsCorrectly() throws Exception {
        File tempFile = File.createTempFile("saved_reservations", ".csv");
        tempFile.deleteOnExit();

        List<Reservation> reservations = new ArrayList<>();
        reservations.add(new Reservation("Dupont", "Jean", 2, true, "Loisir", 1));
        reservations.add(new Reservation("Martin", "Alice", 1, false, "Affaire", 0));

        ReservationParser parser = new ReservationParser(tempFile.getAbsolutePath());
        parser.saveReservations(reservations);

        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(tempFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }

        assertEquals(2, lines.size(), "Deux lignes doivent être écrites");
        assertTrue(lines.get(0).startsWith("Dupont,Jean,2,Fumeur"));
        assertTrue(lines.get(1).startsWith("Martin,Alice,1,Non-fumeur"));
    }

    // === Outil pour écrire des lignes dans un fichier temporaire
    private static File writeLinesToTempFile(String... lines) throws IOException {
        File tempFile = File.createTempFile("reservations_test", ".csv");
        tempFile.deleteOnExit();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        }

        return tempFile;
    }
}
