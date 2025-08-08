package com.example.observer;

public interface LotteryObserver {
    void update(String resultMessage); // Méthode pour notifier la vue du résultat
    void onDoorEliminated(int door); // notifier les portes éliminer
}
