package com.example.models.lottery;

import java.util.Random;

/**
 * Ticket de loterie de type Silver.
 * Le joueur doit deviner un mot mélangé pour gagner une réduction.
 */
public class SilverTicket extends LotteryTicket {

    public static final int SILVER_REDUCTION = 50; // Réduction offerte en cas de succès

    // Liste de mots possibles à deviner
    private static final String[] WORD_POOL = {
        "hotel", "client", "chambre", "loterie", "reduction"
    };

    private final String originalWord;   // Le mot original à deviner
    private final String scrambledWord;  // La version mélangée du mot
    private boolean hasPlayed = false;   // Pour éviter de rejouer plusieurs fois

    /**
     * Constructeur : sélectionne un mot aléatoire et le mélange.
     */
    public SilverTicket() {
        super(SILVER_REDUCTION);
        originalWord = pickRandomWord();         // Sélection d’un mot aléatoire
        scrambledWord = scramble(originalWord);  // Mélange du mot
    }

    /**
     * Sélectionne un mot aléatoire dans la liste.
     * retourne le mot choisi
     */
    private String pickRandomWord() {
        Random random = new Random();
        return WORD_POOL[random.nextInt(WORD_POOL.length)];
    }

    /**
     * Mélange aléatoirement les lettres d’un mot (algorithme de Fisher-Yates).
     * word le mot à mélanger
     * retourne le mot mélangé
     */
    private String scramble(String word) {
        char[] chars = word.toCharArray();
        Random random = new Random();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        return new String(chars);
    }

    /**
     * Permet à la vue d’afficher le mot mélangé au joueur.
     * retourne le mot mélangé
     */
    public String getScrambledWord() {
        return scrambledWord;
    }

    /**
     * Permet au joueur de soumettre une proposition de mot.
     * Compare la réponse avec le mot original (sans tenir compte de la casse).
     * guess la proposition du joueur
     */
    public void submitGuess(String guess) {
        if (hasPlayed) return;
        hasPlayed = true;
    
        
        if (guess.equalsIgnoreCase(originalWord)) {
            notifyObservers("Bravo ! Vous avez retrouvé le mot '" + originalWord + "' et gagné " + getReduction() + "% de réduction !");
        } else {
            notifyObservers("Dommage ! Le mot était '" + originalWord + "'. Pas de réduction cette fois.");
        }
    }
    
}
