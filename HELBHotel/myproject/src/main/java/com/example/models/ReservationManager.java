package com.example.models;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import com.example.observer.*;

public class ReservationManager {

    // Ensemble des clés des clients déjà assignés à une chambre
    private final Set<String> assignedClients = new HashSet<>();

    // Map entre chaque réservation et la chambre proposée
    private final Map<String, String> proposedRooms = new HashMap<>();

    // Map entre chaque réservation et la chambre effectivement assignée
    private final Map<String, String> assignedRoomMap = new HashMap<>();

    // Observateurs à notifier quand les réservations changent
    private final Set<ReservationObserver> observers = new HashSet<>();

    // Map contenant toutes les réservations actives (clé = "Nom:Prénom")
    private final Map<String, Reservation> reservationMap = new HashMap<>();

    private static final int MIN_ROOM_LABEL_LENGTH = 3; // Exemple : A1B ou A10B




    // Vérifie si une réservation est déjà officiellement assignée à une chambre
    public boolean isAlreadyAssigned(Reservation reservation) {
        return assignedClients.contains(reservation.getKey());
    }

    // Marque une réservation comme assignée à une chambre donnée
    // et notifie les observateurs de la modification
    public void assignReservation(Reservation reservation, String roomLabel) {
        String key = reservation.getKey();
        assignedClients.add(key);
        assignedRoomMap.put(key, roomLabel);
        notifyObservers();
    }

    

    // Retourne le label de la chambre assignée à une réservation, sinon une chaîne vide
    public String getAssignedRoomLabel(String key) {
        return assignedRoomMap.getOrDefault(key, "");
    }

    // Retourne la chambre proposée à une réservation (pas encore confirmée)
    public String getProposedRoom(Reservation reservation) {
        return proposedRooms.getOrDefault(reservation.getKey(), "");
    }


    // Propose une chambre à une réservation, puis notifie les observateurs
    public void proposeRoom(Reservation reservation, String label) {
        proposedRooms.put(reservation.getKey(), label);
        notifyObservers();
    }

    // Vérifie si une réservation a déjà une chambre proposée
    public boolean hasProposal(Reservation reservation) {
        return proposedRooms.containsKey(reservation.getKey());
    }

    // Met à jour la chambre proposée (utilisé pour modifier manuellement)
    public void setProposal(Reservation reservation, String roomLabel) {
        proposedRooms.put(reservation.getKey(), roomLabel);
    }

    // Ajoute une nouvelle réservation dans la map principale
    public void add(Reservation reservation) {
        reservationMap.put(reservation.getKey(), reservation);
        notifyObservers();
    }

    // Supprime une réservation de toutes les structures
    public void remove(Reservation reservation) {
        String key = reservation.getKey();
        reservationMap.remove(key);
        proposedRooms.remove(key);
        notifyObservers();
    }


    // Retourne toutes les réservations actuellement en mémoire
    public List<Reservation> getAllReservations() {
        return new ArrayList<>(reservationMap.values());
    }

    // Retourne la chambre proposée à une réservation si elle existe
    public String getProposalIfExists(String key) {
        return proposedRooms.getOrDefault(key, "");
    }

    // Supprime tout : propositions, assignations et réservations (ex: pour un reset)
    public void clear() {
        assignedClients.clear();
        assignedRoomMap.clear();
        proposedRooms.clear();
        reservationMap.clear();
        notifyObservers();
    }


    public void addObserver(ReservationObserver observer) {
        observers.add(observer);
    }
    
    public void removeObserver(ReservationObserver observer) {
        observers.remove(observer);
    }
    
    // Préviens tous les observateurs qu'une mise à jour a eu lieu
    private void notifyObservers() {
        for (ReservationObserver observer : observers) {
            observer.update();
        }
    }

    // Retourne une copie de la map des propositions (utile pour affichage ou debug)
    public Map<String, String> getProposedRoomMap() {
        return new HashMap<>(proposedRooms);
    }

    // Vérifie si une réservation est déjà connue (clé dans la map)
    public boolean contains(Reservation reservation) {
        return reservationMap.containsKey(reservation.getKey());
    }


    // Retourne une liste triée selon le mode demandé (par nom ou par chambre)
    public List<Reservation> getSortedReservations(String sortMode) {
        List<Reservation> base = getAllReservations();
        List<Reservation> sorted = new ArrayList<>(base);
    
        if ("Trier par : Nom".equals(sortMode)) {
            // tri par nom
            for (int i = 0; i < sorted.size() - 1; i++) {
                for (int j = i + 1; j < sorted.size(); j++) {
                    if (sorted.get(i).getLastName().compareToIgnoreCase(sorted.get(j).getLastName()) > 0) {
                        Reservation tmp = sorted.get(i);
                        sorted.set(i, sorted.get(j));
                        sorted.set(j, tmp);
                    }
                }
            }
        } else if ("Trier par : Chambre".equals(sortMode)) {
            for (int i = 0; i < sorted.size() - 1; i++) {
                for (int j = i + 1; j < sorted.size(); j++) {
                    String room1 = getProposedRoom(sorted.get(i));
                    String room2 = getProposedRoom(sorted.get(j));
        
                    // Trier d'abord par étage (lettre)
                    char floor1 = room1.length() > 0 ? room1.charAt(0) : '?';
                    char floor2 = room2.length() > 0 ? room2.charAt(0) : '?';
                    int cmp = Character.compare(floor1, floor2);
        
                    // Si même étage, trier par numéro
                    if (cmp == 0) {
                        int num1 = extractRoomNumber(room1);
                        int num2 = extractRoomNumber(room2);
                        cmp = Integer.compare(num1, num2);
                    }
        
                    if (cmp > 0) {
                        Reservation tmp = sorted.get(i);
                        sorted.set(i, sorted.get(j));
                        sorted.set(j, tmp);
                    }
                }
            }
        }
        
        return sorted;
    }
    


    // Cette méthode extrait le numéro de chambre depuis un label du type "A10B"
    // Elle suppose que la lettre d’étage est en première position,
    // la lettre de type en dernière position, et le numéro au milieu.
    // Exemple : A10B → 10
    private int extractRoomNumber(String label) {
        // Si le label est trop court pour contenir une lettre + numéro + type, je renvoie -1
        if (label.length() < MIN_ROOM_LABEL_LENGTH) return -1;

        try {
            // Je prends la sous-chaîne entre la 2e lettre (exclue) et l’avant-dernière (exclue)
            String numPart = label.substring(1, label.length() - 1);
            return Integer.parseInt(numPart);
        } catch (Exception e) {
            // En cas d'erreur (ex: non numérique), je renvoie -1 pour signaler l’échec
            return -1;
        }
    }


    
    //verifie si la room est déjà assigner
    public boolean isRoomAlreadyProposed(String roomLabel) {
        for (String proposedLabel : proposedRooms.values()) {
            if (roomLabel.equalsIgnoreCase(proposedLabel)) {
                return true;
            }
        }
        return false;
    }
    
    
    
} 
