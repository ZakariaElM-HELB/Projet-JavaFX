package com.example.view;

import com.example.HELBHotelController;
import com.example.models.ColorPalette;
import com.example.models.EmptyRoom;
import com.example.models.Hotel;
import com.example.models.Reservation;
import com.example.models.Room;
import com.example.observer.RoomObserver;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.*;

public class RoomView implements RoomObserver {


        
    // === Dimensions des boutons de chambre ===
    public static final int ROOM_BUTTON_WIDTH = 80;
    public static final int ROOM_BUTTON_HEIGHT = 40;

    // === Espacement de la grille ===
    public static final int GRID_HGAP = 10;
    public static final int GRID_VGAP = 10;

    // === Marges du contenu popup ===
    public static final int POPUP_PADDING = 20;
    public static final int POPUP_SPACING = 15;


    HELBHotelController controller;
    private Hotel hotel;
    private int currentFloorIndex;
    private List<Room> allRooms = new ArrayList<>();
    private char currentFloor = 'A';

    public final GridPane grid;
    private final Map<String, Button> roomButtonMap = new HashMap<>();

    private Button currentReleaseButton;

    /** Retourne le bouton de libération actuellement affiché dans le popup. */
    public Button getCurrentReleaseButton() {
        return currentReleaseButton;
    }

    /** Constructeur de la vue : initialise la grille avec des marges. */
    public RoomView() {
        grid = new GridPane();
        grid.setHgap(GRID_HGAP);
        grid.setVgap(GRID_VGAP);
    }

    /** Met à jour la grille graphique en affichant les chambres de l'étage courant. */
    public void render() {
        grid.getChildren().clear();
        roomButtonMap.clear();
        grid.getColumnConstraints().clear();

        for (Room room : new ArrayList<>(allRooms)) {
            room.removeObserver(this);
            room.addObserver(this);
        }

        int totalCols = hotel.getNumberOfColumns(currentFloorIndex);

        // Contraintes de largeur : chaque colonne prend un pourcentage équitable
        for (int i = 0; i < totalCols; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(100.0 / totalCols);
            grid.getColumnConstraints().add(col);
        }

        for (int row = 0; row < hotel.getRoomMatrix().length; row++) {
            for (int col = 0; col < hotel.getRoomMatrix()[0].length; col++) {

                Room room = findRoomAt(row, col, currentFloorIndex);
                if (room instanceof EmptyRoom) continue;

                Button button = new Button(room.getLabel());
                button.setWrapText(true);
                button.setMaxWidth(Double.MAX_VALUE);      // adapte à la largeur de la colonne
                button.setPrefHeight(50);                  // hauteur fixe confortable

                if (room.isOccupied()) {
                    button.setStyle("-fx-background-color: " + ColorPalette.COLOR_ERROR + "; -fx-text-fill: white;");
                } else {
                    String color = ColorPalette.getColor(room.getType());
                    button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-border-radius: 10px; -fx-background-radius: 10px;");
                }

                Tooltip.install(button, new Tooltip(room.getTooltipText()));

                roomButtonMap.put(room.getLabel(), button);
                grid.add(button, col, row);
            }
        }
    }


    /** Crée le contenu d'un popup pour une chambre occupée, en affichant les infos du client. */
    public VBox createRoomPopupContent(Room room) {
        VBox layout = new VBox(POPUP_SPACING);
        layout.setPadding(new Insets(POPUP_PADDING));
        layout.setAlignment(Pos.CENTER_LEFT);
        
        Reservation reservation = room.getAssignedReservation();

        layout.getChildren().addAll(
            new Label(ReservationView.LABEL_LAST_NAME + reservation.getLastName()),
            new Label(ReservationView.LABEL_FIRST_NAME + reservation.getFirstName()),
            new Label(ReservationView.LABEL_SMOKER + (reservation.isSmoker() ? "Oui" : "Non")),
            new Label(ReservationView.LABEL_PEOPLE + reservation.getNumberOfPeople()),
            new Label(ReservationView.LABEL_CHILDREN + reservation.getNumberOfChildren()),
            new Label(ReservationView.LABEL_PURPOSE + reservation.getStayPurpose()),
            new Label(ReservationView.LABEL_ROOM+ room.getLabel())
        );

        return layout;
    }

    /** Crée une boîte de dialogue pour une chambre occupée, avec bouton de libération. */
    public Dialog<Void> createOccupiedRoomDialog(Room room) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Chambre occupée : " + room.getLabel());

        VBox layout = createRoomPopupContent(room);
        currentReleaseButton = new Button("Libérer la chambre");

        layout.getChildren().add(currentReleaseButton);
        dialog.getDialogPane().setContent(layout);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        return dialog;
    }

    /** Retourne le bouton correspondant à une chambre donnée (label). */
    public Button getButton(String roomLabel) {
        return roomButtonMap.get(roomLabel);
    }

    /** Recherche une chambre dans la liste en fonction de sa position et étage. */
    private Room findRoomAt(int row, int col, int floorIndex) {
        for (Room r : allRooms) {
            if (r.getFloor() == row && r.getColumn() == col && r.getFloorIndex() == floorIndex) {
                return r;
            }
        }
        return EmptyRoom.getInstance();
    }

    /** Retourne la grille graphique à afficher dans la scène principale. */
    public Pane getView() {
        return grid;
    }

    /** Définit l’hôtel courant pour pouvoir afficher ses chambres. */
    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }

    /** Lie le contrôleur principal à cette vue (si besoin d'y accéder). */
    public void setHotelController(HELBHotelController controller) {
        this.controller = controller;
    }

    /** Définit la liste des chambres à afficher et ajoute l'observateur sur chacune. */
    public void setRooms(List<Room> rooms) {
        this.allRooms = rooms;
        for (Room r : rooms) {
            r.addObserver(this);
        }
    }

    /** Change l’étage actif à afficher, à partir de la lettre (A, B, C...). */
    public void setFloor(char floorLetter) {
        this.currentFloor = floorLetter;
        this.currentFloorIndex = floorLetter - 'A';
    }

    /** Méthode appelée quand une chambre est assignée → on réaffiche l'étage courant. */
    @Override
    public void onRoomAssigned(String roomLabel) {
        if (roomLabel.charAt(0) == currentFloor) {
            render();
        }
    }

    /** Méthode appelée quand une chambre est libérée → on réaffiche l'étage courant. */
    @Override
    public void onRoomReleased(String roomLabel) {
        if (roomLabel.charAt(0) == currentFloor) {
            render();
        }
    }

    public boolean hasButton(String label) {
        return roomButtonMap.containsKey(label);
    }
    
}
