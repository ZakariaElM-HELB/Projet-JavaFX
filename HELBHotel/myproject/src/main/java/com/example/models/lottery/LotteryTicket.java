package com.example.models.lottery;

import java.util.ArrayList;
import java.util.List;

import com.example.observer.LotteryObserver;


/**
 * Classe abstraite représentant un ticket de loterie.
 * Chaque ticket contient un pourcentage de réduction et une liste d'observateurs à notifier.
 */
public abstract class LotteryTicket {

    /** Pourcentage de réduction associé à ce ticket (défini dans la sous-classe) */
    private final int reduction;

    /** Observateurs qui seront notifiés (souvent la vue associée) */
    protected final List<LotteryObserver> observers = new ArrayList<>();

    /**
     * Constructeur commun à tous les tickets de loterie.
     * reduction le pourcentage de réduction associé à ce ticket
     */
    public LotteryTicket(int reduction) {
        this.reduction = reduction;
    }

    /** Retourne le pourcentage de réduction offert par ce ticket */
    public int getReduction() {
        return reduction;
    }

    /** Ajoute un observateur à la liste */
    public void addObserver(LotteryObserver observer) {
        observers.add(observer);
    }


    /**
     * Notifie tous les observateurs avec un message.
     * Typiquement utilisé pour signaler le résultat d’un jeu.
     */
    public void notifyObservers(String resultMessage) {
        for (LotteryObserver observer : observers) {
            observer.update(resultMessage);
        }
    }
}
