package com.example.view;

import com.example.HELBHotelController;
import com.example.models.ColorPalette;
import com.example.models.EmptyRoom;
import com.example.models.Reservation;
import com.example.models.Room;
import com.example.observer.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class ReservationView implements ReservationObserver{

    // === CONSTANTES ===
    private static final int PANEL_SPACING = 10;
    private static final int PANEL_PADDING = 10;
    private static final int GRID_HGAP = 20;
    private static final int GRID_VGAP = 10;
    private static final int GRID_PADDING = 20;
    private static final int RIGHT_BOX_SPACING = 15;
    private static final int ROOM_STATUS_FONT_SIZE = 11;
    private static final String ROOM_STATUS_COLOR = "-fx-text-fill: " + ColorPalette.COLOR_ERROR + ";";
    private static final String ROOM_BUTTON_STYLE = "-fx-background-color: " + ColorPalette.COLOR_BACKGROUND_LIGHT + ";";
    private static final String DEFAULT_ROOM_STYLE = "-fx-background-color: " + ColorPalette.COLOR_DEFAULT + "; -fx-text-fill: black;";

    private static final int ROW_NOM = 0;
    private static final int ROW_PRENOM = 1;
    private static final int ROW_FUMEUR = 2;
    private static final int ROW_ENFANTS = 3;
    private static final int ROW_PERSONNES = 4;
    private static final int ROW_MOTIF = 5;
    private static final int RIGHT_BOX_COLUMN = 3;
    private static final int RIGHT_BOX_ROWSPAN = 6;


    public static final String LABEL_LAST_NAME = "Nom : ";
    public static final String LABEL_FIRST_NAME = "Prénom : ";
    public static final String LABEL_SMOKER = "Fumeur : ";
    public static final String LABEL_CHILDREN = "Enfants : ";
    public static final String LABEL_PEOPLE = "Personnes : ";
    public static final String LABEL_PURPOSE = "Motif : ";
    public static final String LABEL_ROOM = "Chambre : ";

    private HELBHotelController controller;

    public final VBox panel;
    private final ListView<Reservation> listView;

    
    public final List<Button> reservationButtons = new ArrayList<>();
    public final List<Button> reservationProposeButtons = new ArrayList<>();
    public final List<Reservation> reservationRefs = new ArrayList<>();

    /** Constructeur : initialise la liste et le panneau principal. */
    public ReservationView() {
        panel = new VBox(PANEL_SPACING);
        panel.setPadding(new Insets(PANEL_PADDING));
        listView = new ListView<>();
        setupListView();
        panel.getChildren().add(listView);
    }

    /** Configure la cellule de chaque élément dans la liste de réservations. */
    private void setupListView() {
        listView.setCellFactory(list -> new ListCell<>() {
            private final HBox container = new HBox(PANEL_SPACING);
            private final Button nameButton = new Button();
            private final Button proposeBtn = new Button("\u21BB");

            {
                container.getChildren().addAll(nameButton, proposeBtn);
            }

            @Override
            protected void updateItem(Reservation reservation, boolean empty) {
                super.updateItem(reservation, empty);
                if (empty || reservation == null) {
                    setGraphic(null);
                    return;
                }

                reservationButtons.add(nameButton);
                reservationProposeButtons.add(proposeBtn);
                reservationRefs.add(reservation);

                String proposedLabel = controller.reservationManager.getProposedRoom(reservation);
                String displayName = reservation.getFirstName().charAt(0) + ". " + reservation.getLastName();
                if (!proposedLabel.isBlank()) displayName += "   " + proposedLabel;
                nameButton.setText(displayName);

                Room room = controller.hotel.getRoomByLabel(proposedLabel);
                if (!(room instanceof EmptyRoom)) {
                    String color = ColorPalette.getColor(room.getType());
                    nameButton.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white;");
                } else {
                    nameButton.setStyle(DEFAULT_ROOM_STYLE);
                }

                proposeBtn.setStyle(ROOM_BUTTON_STYLE);

                setGraphic(container); // c’est celui créé dans le constructeur de cellule
            }
        });
    }

    /** Crée un popup de confirmation pour une réservation avec champ de validation. */
    public Dialog<Void> createReservationDialog(Reservation reservation, Button confirmBtn, TextField roomField){

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Confirmer la réservation");

        GridPane grid = new GridPane();
        grid.setHgap(GRID_HGAP);
        grid.setVgap(GRID_VGAP);
        grid.setPadding(new Insets(GRID_PADDING));

        grid.add(new Label(LABEL_LAST_NAME), 0, ROW_NOM);
        grid.add(new Label(reservation.getLastName()), 1, ROW_NOM);

        grid.add(new Label(LABEL_FIRST_NAME), 0, ROW_PRENOM);
        grid.add(new Label(reservation.getFirstName()), 1, ROW_PRENOM);

        grid.add(new Label(LABEL_SMOKER), 0, ROW_FUMEUR);
        grid.add(new Label(reservation.hasChildren() ? "Oui" : "Non"), 1, ROW_FUMEUR);

        grid.add(new Label(LABEL_CHILDREN), 0, ROW_ENFANTS);
        grid.add(new Label(String.valueOf(reservation.getNumberOfChildren())), 1, ROW_ENFANTS);

        grid.add(new Label(LABEL_PEOPLE), 0, ROW_PERSONNES);
        grid.add(new Label(String.valueOf(reservation.getNumberOfPeople())), 1, ROW_PERSONNES);

        grid.add(new Label(LABEL_PURPOSE), 0, ROW_MOTIF);
        grid.add(new Label(reservation.getStayPurpose()), 1, ROW_MOTIF);

        VBox rightBox = new VBox(RIGHT_BOX_SPACING);
        rightBox.setAlignment(Pos.CENTER_LEFT);

        String proposition = controller.reservationManager.getProposedRoom(reservation);
        roomField.setText(proposition);
        roomField.setPromptText("Chambre proposée");

        Label roomStatus = new Label();
        roomStatus.setStyle("-fx-font-size: " + ROOM_STATUS_FONT_SIZE + "; " + ROOM_STATUS_COLOR);

        confirmBtn.setDisable(true);

        // Validation de la chambre entrée
        roomField.textProperty().addListener((obs, oldVal, newVal) -> {
            validateRoomField(newVal, confirmBtn, roomStatus);
        });
        
        // Appel initial de validation avec le contenu déjà présent
        validateRoomField(roomField.getText(), confirmBtn, roomStatus);
        

        rightBox.getChildren().addAll(new Label("Proposition"), roomField, roomStatus, confirmBtn);
        grid.add(rightBox, RIGHT_BOX_COLUMN, 0, 1, RIGHT_BOX_ROWSPAN);

        // Ajout pour forcer la validation initiale
        roomField.textProperty().set(roomField.getText());

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        return dialog;
    }

    private void validateRoomField(String value, Button confirmBtn, Label roomStatus) {
        String label = value.trim().toUpperCase();
        Room room = controller.hotel.getRoomByLabel(label);
        if (room instanceof EmptyRoom) {
            confirmBtn.setDisable(true);
            roomStatus.setText(" Chambre inexistante");
        } else if (room.isOccupied()) {
            confirmBtn.setDisable(true);
            roomStatus.setText(" Chambre occupée");
        } else {
            confirmBtn.setDisable(false);
            roomStatus.setText(" Chambre disponible");
        }
    }
    

    /** Trie la liste des réservations par nom ou par chambre. */
    public void sortReservations(String sortMode) {
        List<Reservation> sorted = controller.reservationManager.getSortedReservations(sortMode);
        listView.getItems().setAll(sorted);
    }
    

    

    public void setController(HELBHotelController controller) {
        this.controller = controller;
    }


    /** Met à jour les réservations visibles et réinitialise les listes de boutons. */
    public void setReservations(List<Reservation> reservations) {
        reservationButtons.clear();
        reservationProposeButtons.clear();
        reservationRefs.clear();
    
        listView.getItems().setAll(reservations);
    }

    /** Supprime une réservation de la vue. */
    public void removeReservation(Reservation reservation) {
        listView.getItems().remove(reservation);
    }

    @Override
    public void update() {
        if (controller != null) {
            List<Reservation> updated = controller.reservationManager.getAllReservations();
            setReservations(updated);
            sortReservations(controller.view.getSelectedSortMode());
            //controller.connectReservationButtons();
        }
    }
    

}
