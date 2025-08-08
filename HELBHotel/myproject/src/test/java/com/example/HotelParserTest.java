package com.example;

import com.example.models.Hotel;
import com.example.parser.HotelParser;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class HotelParserTest {

    // === TESTS VALIDES ===
    @ParameterizedTest(name = "[Valide #{index}]")
    @MethodSource("validConfigs")
    public void shouldLoadHotel_whenFileIsValid(String content) throws IOException {
        File tempFile = writeTempConfig(content);
        HotelParser parser = new HotelParser(tempFile.getAbsolutePath());

        Hotel hotel = parser.loadHotel();

        assertNotNull(hotel, "L'hôtel ne doit pas être null");
        assertTrue(hotel.getNumberOfFloors() > 0, "Nombre d'étages doit être > 0");
        assertFalse(hotel.getAllRooms().isEmpty(), "L’hôtel doit contenir des chambres");
    }

    // === TESTS INVALIDES : exception attendue + message clair
    @ParameterizedTest(name = "[Invalide #{index}] {2}")
    @MethodSource("invalidConfigs")
    public void shouldThrowIllegalArgumentException_whenConfigIsInvalid(String content, String expectedMessageFragment, String description) throws IOException {
        File tempFile = writeTempConfig(content);
        HotelParser parser = new HotelParser(tempFile.getAbsolutePath());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, parser::loadHotel);
        assertTrue(ex.getMessage().toLowerCase().contains(expectedMessageFragment.toLowerCase()),
            "Le message d'erreur devait contenir : '" + expectedMessageFragment + "'\nMessage reçu : " + ex.getMessage());
    }

    // === Cas de fichiers valides
    private static Stream<String> validConfigs() {
        return Stream.of(
            "2\nE,B,L\nL,B,E",
            "1\nL,E,B",
            "3\nE,E,E\nB,B,B\nL,L,L"
        );
    }

    // === Cas de fichiers invalides + message attendu + description
    private static Stream<Object[]> invalidConfigs() {
        return Stream.of(
            new Object[]{"", "vide", "Fichier vide"},
            new Object[]{"not_a_number\nE,B,L", "invalide", "Nombre d'étages non numérique"},
            new Object[]{"2\nE,B\nL,B,E", "colonnes", "Lignes de tailles différentes"},
            new Object[]{"1\nE,X,L", "caractère", "Caractère non autorisé"},
            new Object[]{"2\n", "absente", "Pas de matrice"},
            new Object[]{"30\nE,E,E\nE,E,E", "entre 1 et 26", "Trop d'étages"}
        );
    }

    // === Génère un fichier temporaire avec le contenu donné
    private static File writeTempConfig(String content) throws IOException {
        File temp = File.createTempFile("hotel_test_", ".hconf");
        temp.deleteOnExit();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(temp))) {
            for (String line : content.split("\n")) {
                writer.write(line);
                writer.newLine(); // force une vraie ligne
            }
        }

        return temp;
    }
}
