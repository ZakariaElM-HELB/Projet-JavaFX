package com.example.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.example.strategy.*;
import com.example.observer.*;

public class Hotel {
    private String[][][] roomMatrices; // [floor][row][col]
    private int numberOfFloors;
    private List<Room> rooms = new ArrayList<>();

    // === Constantes internes ===
    private static final int MIN_LABEL_LENGTH = 3; // ex: A1B
    private static final String ROOM_TYPE_UNUSED = "Z"; // valeur pour cellule vide
    private static final String FLOOR_LABEL_PREFIX = "Etage "; // préfixe pour l’affichage



    // Constructeur privé, crée toutes les chambres à partir des matrices de types
    public Hotel(int numberOfFloors, String[][][] roomMatrices) {
        this.numberOfFloors = numberOfFloors;
        this.roomMatrices = roomMatrices;

        for (int floorIndex = 0; floorIndex < numberOfFloors; floorIndex++) {
            char floorLetter = (char) ('A' + floorIndex);
            String[][] matrix = roomMatrices[floorIndex];

            for (int row = 0; row < matrix.length; row++) {
                for (int col = 0; col < matrix[row].length; col++) {
                    String type = matrix[row][col];
                    if (!type.equals(ROOM_TYPE_UNUSED)) {
                        int roomNumber = getNextRoomNumberForFloor(floorLetter);
                        String label = floorLetter + String.valueOf(roomNumber) + type;
                        rooms.add(new Room(label, type, row, col, floorIndex));
                    }
                }
            }
        }
    }

    public Hotel(int numberOfFloors, String[][] roomMatrix) {
        this(numberOfFloors, generateMatrixArray(numberOfFloors, roomMatrix));
    }

    private static String[][][] generateMatrixArray(int numberOfFloors, String[][] roomMatrix) {
        String[][][] matrices = new String[numberOfFloors][][];
        for (int i = 0; i < numberOfFloors; i++) {
            matrices[i] = deepCopyMatrix(roomMatrix);
        }
        return matrices;
    }


    // Copie profonde d'une matrice 2D
    private static String[][] deepCopyMatrix(String[][] original) {
        String[][] copy = new String[original.length][];
        for (int i = 0; i < original.length; i++) {
            copy[i] = original[i].clone();
        }
        return copy;
    }

    // Cette méthode sert à générer un numéro de chambre unique pour un étage donné.
    // Je compte combien de chambres ont déjà la lettre d’étage correspondante,
    // et j’incrémente pour créer une nouvelle étiquette (ex: A1B, A2L...).
    private int getNextRoomNumberForFloor(char floorLetter) {
        int count = 1;
        for (Room room : rooms) {
            if (room.getLabel().charAt(0) == floorLetter) {
                count++;
            }
        }
        return count;
    }


    // Cherche une chambre libre d'un type donné et retourne son label
    public Room findAvailableRoomOfType(String type) {
        for (Room room : rooms) {
            if (room.getType().equals(type) && !room.isOccupied()) {
                return room;
            }
        }
        return EmptyRoom.getInstance();
    }
    

    // Retourne la matrice d’un étage spécifique
    public String[][] getRoomMatrixForFloor(int floorIndex) {
        return roomMatrices[floorIndex];
    }

    // Retourne la matrice du rez-de-chaussée
    public String[][] getRoomMatrix() {
        return roomMatrices.length > 0 ? roomMatrices[0] : new String[0][0];
    }

    // Met à jour toutes les matrices de chambres
    public void setRoomMatrices(String[][][] roomMatrices) {
        this.roomMatrices = roomMatrices;
    }

    // Retourne le nombre total d’étages
    public int getNumberOfFloors() {
        return numberOfFloors;
    }

    // Retourne le nombre de colonnes sur un étage donné
    public int getNumberOfColumns(int floorIndex) {
        return roomMatrices[floorIndex].length > 0 ? roomMatrices[floorIndex][0].length : 0;
    }

    // Donne la lettre associée à un index d’étage (0 -> A, 1 -> B, ...)
    public char getFloorLetter(int index) {
        return (char) ('A' + index);
    }

    // Retourne les noms des étages (ex: "Floor A", "Floor B", ...)
    public List<String> getFloorLabels() {
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < getNumberOfFloors(); i++) {
            labels.add(FLOOR_LABEL_PREFIX + getFloorLetter(i));
        }
        return labels;
    }

    // Vérifie si une chambre est occupée à une position donnée
    public boolean isRoomOccupied(int floorIndex, int row, int col) {
        return !roomMatrices[floorIndex][row][col].equals(ROOM_TYPE_UNUSED);
    }

    // Affiche la structure complète de l’hôtel dans la console
    public void printHotelStructure() {
        System.out.println("Structure de l'hôtel :");
        for (int floor = 0; floor < numberOfFloors; floor++) {
            System.out.println(FLOOR_LABEL_PREFIX + getFloorLetter(floor) + ":");
            for (String[] row : roomMatrices[floor]) {
                for (String cell : row) {
                    System.out.print(cell + "\t");
                }
                System.out.println();
            }
        }
    }

    
    public boolean isReservationAssigned(String lastName, String firstName, Map<String, String> assignedRoomMap) {
        String key = lastName + ":" + firstName;
        return assignedRoomMap.containsKey(key);
    }


    // Cette méthode calcule le nombre de colonnes non vides sur un étage donné.
    // Elle est utile pour l’affichage graphique afin d’ignorer les colonnes "Z".
    public int getRealColumnCount(int floorIndex) {
        int count = 0;
        int rows = roomMatrices[floorIndex].length;
        int cols = roomMatrices[floorIndex][0].length;

        for (int col = 0; col < cols; col++) {
            boolean hasRoom = false;
            for (int row = 0; row < rows; row++) {
                if (!roomMatrices[floorIndex][row][col].equals(ROOM_TYPE_UNUSED)) {
                    hasRoom = true;
                    break;
                }
            }
            if (hasRoom) count++;
        }

        return count;
    }


    // Cette méthode cherche une chambre à partir de son étiquette (ex: "A1B").
    // Si l’étiquette est vide ou trop courte, ou si elle ne correspond à aucune chambre,
    // je retourne l’objet EmptyRoom pour éviter les nulls.
    public Room getRoomByLabel(String label) {
        if (label.isBlank() || label.length() < MIN_LABEL_LENGTH) return EmptyRoom.getInstance();

        for (Room room : rooms) {
            if (room.getLabel().equals(label)) {
                return room;
            }
        }

        return EmptyRoom.getInstance();
    }

    
    

    // Associe une réservation à une chambre si elle est libre
    public boolean assignRoom(String roomLabel, Reservation reservation) {
        Room room = getRoomByLabel(roomLabel);
        if (room instanceof EmptyRoom || room.isOccupied()) return false;
    
        room.assignTo(reservation);
        return true;
    }

    // Cette méthode libère les chambres qui contiennent une "EmptyReservation".
    // Cela permet de nettoyer les chambres qui ont été libérées "virtuellement"
    // mais qui restent techniquement marquées comme occupées dans le modèle.
    public boolean releaseRoom(String roomLabel) {
        Room room = getRoomByLabel(roomLabel);
        if (room instanceof EmptyRoom || !room.isOccupied()) return false;
    
        room.release();
        return true;
    }

    // Retourne toutes les chambres de l’hôtel
    public List<Room> getAllRooms() {
        return rooms;
    }

    public String proposeRoomFor(Reservation reservation, AssignmentStrategy strategy) {
        return strategy.assignRoom(reservation);
    }

    // Cette méthode confirme une réservation manuelle sur une chambre spécifique.
    // Je récupère la chambre par son label. Si elle est vide ou déjà occupée, je refuse la réservation.
    // Sinon, je l’associe à la réservation passée en paramètre.
    // La méthode retourne true si la réservation est acceptée, false sinon.
    public boolean confirmReservation(String roomLabel, Reservation reservation) {
        Room room = getRoomByLabel(roomLabel);
        if (room instanceof EmptyRoom || room.isOccupied()) return false;
    
        assignRoom(roomLabel, reservation);
        return true;
    }
    

    // Cette méthode libère automatiquement toutes les chambres contenant une réservation vide.
    // C’est utile pour le nettoyage automatique lors d’un rafraîchissement,
    // afin de libérer les chambres qui avaient été marquées occupées avec une réservation non valide.
    // Une fois libérée, je notifie l’observateur (ex: la vue) que la chambre est à jour.
    public void releaseObsoleteRooms(RoomObserver observer) {
        for (Room room : getAllRooms()) {
            if (!room.isOccupied()) continue;
    
            // Si la réservation assignée est une EmptyReservation, ce n’est pas une vraie occupation
            if (room.getAssignedReservation() instanceof EmptyReservation) {
                room.release();
                observer.onRoomReleased(room.getLabel());
            }
        }
    }
    
    
}
