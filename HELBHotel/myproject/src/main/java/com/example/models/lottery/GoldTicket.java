package com.example.models.lottery;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.example.observer.LotteryObserver;
//import com.example.view.LotteryView;

public class GoldTicket extends LotteryTicket {

    // === Constantes spécifiques au ticket Gold ===
    
    /** Pourcentage de réduction en cas de gain */
    public static final int GOLD_REDUCTION = 100;

    /** Nombre de portes supplémentaires par rapport à l'étage */
    private static final int EXTRA_DOORS = 2;

    /** Nombre minimal de portes pour jouer */
    private static final int MIN_DOORS = 3;

    /** Valeur utilisée quand aucun choix n’a encore été fait */
    private static final int NO_PLAYER_CHOICE = -1;

    /** Seuil à partir duquel la phase finale est activée (2 portes restantes) */
    private static final int FINAL_PHASE_THRESHOLD = 2;

    // === Attributs d'état du jeu ===

    private final int totalDoors;
    private final int winningDoor;
    private final List<Integer> remainingDoors;
    private int playerChoice = NO_PLAYER_CHOICE;
    private boolean hasPlayed = false;
    private boolean finalPhaseStarted = false;

    /**
     * Construit un ticket Gold avec un nombre de portes basé sur l'étage.
     * floor l’étage auquel se trouve la chambre
     */
    public GoldTicket(int floor) {
        super(GOLD_REDUCTION);
        this.totalDoors = Math.max(MIN_DOORS, floor + EXTRA_DOORS);
        this.winningDoor = new Random().nextInt(totalDoors) + 1;

        this.remainingDoors = new ArrayList<>();
        for (int i = 1; i <= totalDoors; i++) {
            remainingDoors.add(i);
        }
    }

    /** Retourne la liste actuelle des portes encore en jeu */
    public List<Integer> getRemainingDoors() {
        return new ArrayList<>(remainingDoors); // copie défensive
    }

    /**
     * Étape 1 : le joueur choisit une porte.
     * Cette porte est mémorisée et les portes éliminables commencent à être ouvertes.
     */
    public void makeInitialChoice(int chosenDoor) {
        if (playerChoice != NO_PLAYER_CHOICE || !remainingDoors.contains(chosenDoor)) return;

        this.playerChoice = chosenDoor;
        notifyObservers("Vous avez choisi la porte n°" + chosenDoor + ". Ouverture des autres portes en cours...");
        //eliminateNextBadDoor();
    }

    /**
     * Étape 2 : on élimine une porte qui n’est ni celle du joueur, ni la gagnante.
     * Cette élimination est visible dans la vue via l’observer.
     */
    public void eliminateNextBadDoor() {
        if (playerChoice == NO_PLAYER_CHOICE || hasPlayed || finalPhaseStarted) return;

        List<Integer> eliminables = new ArrayList<>(remainingDoors);
        eliminables.remove(Integer.valueOf(playerChoice));
        eliminables.remove(Integer.valueOf(winningDoor));

        if (!eliminables.isEmpty()) {
            int eliminated = eliminables.get(new Random().nextInt(eliminables.size()));
            remainingDoors.remove(Integer.valueOf(eliminated));

            notifyObservers("La porte n°" + eliminated + " a été ouverte. Elle était vide.");
            notifyDoorEliminated(eliminated);

            if (remainingDoors.size() == FINAL_PHASE_THRESHOLD) {
                finalPhaseStarted = true;
                notifyObservers("Il ne reste que deux portes : la vôtre (n°" + playerChoice + ") et une autre.");
                notifyObservers("Souhaitez-vous garder votre porte ou changer ?");
            }
        }
    }

    /**
     * Étape finale : le joueur confirme son choix (ou change).
     * Le jeu se termine et affiche le résultat.
     */
    public void makeFinalChoice(int finalDoor) {
        if (!finalPhaseStarted || hasPlayed || !remainingDoors.contains(finalDoor)) return;

        hasPlayed = true;

        if (finalDoor == winningDoor) {
            notifyObservers("Félicitations ! Vous avez gagné " + getReduction() + "% de réduction !");
        } else {
            notifyObservers("Perdu... La bonne porte était la n°" + winningDoor + ".");
        }
    }

    /** Notifie spécifiquement la vue qu'une porte a été éliminée */
    private void notifyDoorEliminated(int door) {
        for (LotteryObserver object : observers) {
            object.onDoorEliminated(door);
        }
    }


    public void progress() {
    // Ne fait que déclencher les changements d’état internes
    if (playerChoice == NO_PLAYER_CHOICE) return;

    if (remainingDoors.size() > FINAL_PHASE_THRESHOLD) {
        eliminateNextBadDoor(); // si autorisé à ce moment
    } else {
        finalPhaseStarted = true;
    }
    notifyObservers("État mis à jour.");
}

    // === Getters d’état ===

    public int getPlayerChoice() {
        return playerChoice;
    }

    public boolean isFinalPhaseStarted() {
        return finalPhaseStarted;
    }

    public boolean hasPlayed() {
        return hasPlayed;
    }

    /**
     * Permet de changer de porte en phase finale.
     * Ne déclenche pas l’élimination automatique.
     */
    public void setPlayerChoice(int newDoor) {
        if (!remainingDoors.contains(newDoor)) return;
        this.playerChoice = newDoor;
        notifyObservers("Vous avez maintenant choisi la porte n°" + newDoor + ".");
    }

    /** Retourne la porte gagnante (utile pour test ou debug) */
    public int getWinningDoor() {
        return winningDoor;
    }
}
