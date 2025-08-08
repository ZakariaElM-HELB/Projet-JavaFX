package com.example.models.lottery;

import java.util.Random;

public class BronzeTicket extends LotteryTicket {
    public static final int BRONZE_REDUCTION = 25;
    private static final int NUMBERS_DOORS = 2;
    public static final int CHOICE_DOOR1 = 1;
    public static final int CHOICE_DOOR2 = 2;
    public static final int START_DOOR = 1;


    private final int winningDoor;
    private boolean hasPlayed = false;

    public BronzeTicket() {
        super(BRONZE_REDUCTION);
        this.winningDoor = new Random().nextInt(NUMBERS_DOORS) + START_DOOR; // porte 1 ou 2
    }

    /**
     * Joue le jeu du ticket bronze avec le choix utilisateur
     * choice numéro de la porte choisie (1 ou 2)
     */
    public void play(int choice) {
        if (hasPlayed) return;
        hasPlayed = true;

        if (choice == winningDoor) {
            notifyObservers("Bravo ! Vous avez choisi la bonne porte (" + winningDoor + ") et gagné " + getReduction() + "% de réduction !");
        } else {
            notifyObservers("Dommage ! La bonne porte était la n°" + winningDoor + ". Pas de réduction cette fois.");
        }
    }
} 
