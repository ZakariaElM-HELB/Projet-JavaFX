package com.example.parser;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import com.example.models.Hotel;

public class HotelParser {
    private final String configFilePath;

    public HotelParser(String configFilePath) {
        this.configFilePath = configFilePath;
    }

    public Hotel loadHotel() throws IOException {
        File configFile = new File(configFilePath);
        if (!configFile.exists()) {
            throw new FileNotFoundException("Le fichier de configuration est introuvable : " + configFilePath);
        }

        List<String[]> matrixList = loadConfiguration();

        if (matrixList.isEmpty()) {
            throw new IllegalArgumentException("Le fichier de configuration est vide.");
        }

        // === Étape 1 : lire et valider le nombre d'étages ===
        String[] firstLine = matrixList.get(0);
        if (firstLine.length != 1) {
            throw new IllegalArgumentException("La première ligne doit contenir uniquement le nombre d'étages (ex: 3).");
        }

        int numberOfFloors;
        try {
            numberOfFloors = Integer.parseInt(firstLine[0].trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Le nombre d'étages est invalide : '" + firstLine[0] + "'. Il doit s'agir d'un entier.");
        }

        if (numberOfFloors <= 0 || numberOfFloors > 26) {
            throw new IllegalArgumentException("Le nombre d'étages doit être compris entre 1 et 26.");
        }

        matrixList.remove(0); // retire la ligne d'en-tête

        if (matrixList.isEmpty()) {
            throw new IllegalArgumentException("La matrice de chambres est absente après la ligne d'étages.");
        }

        // === Étape 2 : valider les lignes de la matrice ===
        int expectedLength = matrixList.get(0).length;
        for (int i = 0; i < matrixList.size(); i++) {
            String[] row = matrixList.get(i);
            int lineNumber = i + 2; // +2 car on a retiré la 1re ligne

            if (row.length != expectedLength) {
                throw new IllegalArgumentException("Ligne " + lineNumber + " invalide : elle contient " +
                        row.length + " colonnes au lieu de " + expectedLength + ".");
            }

            for (int col = 0; col < row.length; col++) {
                String cell = row[col].trim();
                if (!cell.matches("[EBLZ]")) {
                    throw new IllegalArgumentException("Caractère invalide ligne " + lineNumber + ", colonne " + (col + 1) +
                            " : '" + cell + "'. Seuls E, B, L et Z sont autorisés.");
                }
            }
        }

        // === Étape 3 : créer l'hôtel ===
        String[][] roomMatrix = matrixList.toArray(new String[0][]);
        return new Hotel(numberOfFloors, roomMatrix);
    }

    // Cette méthode lit le fichier de configuration de l'hôtel (.hconf)
    // et transforme chaque ligne non vide en tableau de chaînes (String[]),
    // que je stocke dans une liste temporaire pour ensuite l'utiliser ailleurs.
    private List<String[]> loadConfiguration() throws IOException {
        List<String[]> tempMatrix = new ArrayList<>(); // Liste qui va contenir toutes les lignes du fichier sous forme de tableau

        try (BufferedReader br = new BufferedReader(new FileReader(configFilePath))) {
            String line;

            // Je parcours le fichier ligne par ligne
            while ((line = br.readLine()) != null) {
                // Si la ligne n'est pas vide (je la nettoie avec trim), je la traite
                if (!line.trim().isEmpty()) {
                    // Je découpe la ligne en morceaux avec le séparateur ","
                    // et j'ajoute ce tableau à ma liste
                    tempMatrix.add(line.split(","));
                }
            }
        }

        // Je retourne la liste de toutes les lignes du fichier, prêtes à être analysées ailleurs
        return tempMatrix;
    }

}
