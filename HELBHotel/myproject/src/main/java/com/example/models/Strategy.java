package com.example.models;

import com.example.strategy.*;

import java.util.HashMap;
import java.util.Map;

// Cette classe gère toutes les stratégies d'assignation de chambres.
// Elle permet de centraliser leur création, leur accès et leur exécution.
public class Strategy {

    // Je garde un dictionnaire avec le nom de chaque stratégie comme clé
    // et son implémentation comme valeur (toutes implémentent AssignmentStrategy)
    private final Map<String, AssignmentStrategy> strategies = new HashMap<>();

    // Lors de l'initialisation, je construis toutes les stratégies disponibles
    // en leur passant l'hôtel et le gestionnaire de réservations
    public Strategy(Hotel hotel, ReservationManager manager) {
        strategies.put("Aléatoire", RandomAssignment.getInstance(hotel, manager));
        strategies.put("Zone calme", QuietZone.getInstance(hotel, manager));
        strategies.put("Séquentiel", Sequential.getInstance(hotel, manager));
        strategies.put("Selon le motif", PurposeBased.getInstance(hotel, manager));
    }

    // Méthode utilitaire pour savoir si une stratégie est disponible
    public boolean contains(String name) {
        return strategies.containsKey(name);
    }

    // Cette méthode permet d’assigner une chambre à une réservation
    // en choisissant la stratégie selon son nom (ex: "Aléatoire", "Zone calme", etc.)
    public String assign(Reservation reservation, String strategyName) {
        if (!strategies.containsKey(strategyName)) {
            throw new IllegalArgumentException("Stratégie inconnue : " + strategyName);
        }
        return strategies.get(strategyName).assignRoom(reservation);
    }
}
