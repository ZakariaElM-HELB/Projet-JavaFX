package com.example.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;

import com.example.HELBHotelController;
import com.example.models.ColorPalette;
import com.example.models.Reservation;
import com.example.models.Room;
import com.example.observer.RoomObserver;

public class HELBHotelView implements RoomObserver {
    // === CONSTANTES UI GÉNÉRALES ===
    private static final int PADDING = 10;
    private static final double SCENE_WIDTH = 800;
    private static final double SCENE_HEIGHT = 600;

    private static final int ROOM_LABEL_MIN_LENGTH = 7; // "Floor A"
    private static final String DEFAULT_FLOOR_LABEL = "Etage A";

    // === CONSTANTES LÉGENDE ===
    private static final int LEGEND_COLOR_SIZE = 16;
    private static final int LEGEND_ITEM_SPACING = 8;
    private static final int LEGEND_TEXT_SIZE = 13;
    private static final int LEGEND_SECTION_PADDING = 10;
    private static final int LEGEND_PADDING_MULTIPLIER = 2;

    // === MODES ===
    private static final String[] ASSIGNMENT_MODES = {
        "Aléatoire", "Zone calme", "Séquentiel", "Selon le motif"
    };

    private static final String[] SORT_MODES = {
        "Trier par : Nom", "Trier par : Chambre"
    };

    // === UI & CONTROLEURS ===
    public final Stage stage;
    public final Scene scene;
    private final HBox mainLayout;
    private final VBox rightPanel;

    public final ComboBox<String> floorSelector;
    public final ComboBox<String> assignementMode;
    public final ComboBox<String> sortMode;
    public final Button verifyCodeBtn;

    private HELBHotelController controller;
    public RoomView roomView;
    public ReservationView reservationView;

    public HELBHotelView(HELBHotelController controller, Stage stage) {
        this.controller = controller;
        this.stage = stage;
        this.stage.setTitle("Système de réservation HELB Hôtel");
        

        mainLayout = new HBox(PADDING);
        rightPanel = new VBox(PADDING);

        floorSelector = new ComboBox<>();
        verifyCodeBtn = new Button("Vérifier le code");
        assignementMode = new ComboBox<>();
        sortMode = new ComboBox<>();

        initializeViews();
        scene = new Scene(new VBox(), SCENE_WIDTH, SCENE_HEIGHT); // placeholder vide
        setupUI();
        this.stage.setScene(scene);

        populateFloorSelector();
        updateRoomGrid();
    }

    private void initializeViews() {
        roomView = new RoomView();
        roomView.setHotel(controller.hotel);
        roomView.setHotelController(controller);

        reservationView = new ReservationView();
        reservationView.setController(controller);
    }
    
    private void setupUI() {
        HBox topMenu = new HBox(PADDING);
        topMenu.setPadding(new Insets(PADDING));
        topMenu.getChildren().addAll(floorSelector, verifyCodeBtn);
    
        VBox topSection = new VBox(PADDING);
        topSection.getChildren().addAll(createLegend(), topMenu);
    
        rightPanel.setPadding(new Insets(PADDING));
        assignementMode.getItems().addAll(ASSIGNMENT_MODES);
        assignementMode.setValue(ASSIGNMENT_MODES[0]);
        sortMode.getItems().addAll(SORT_MODES);
        sortMode.setValue(SORT_MODES[0]);
    
        rightPanel.getChildren().addAll(
            verifyCodeBtn,
            assignementMode,
            sortMode,
            reservationView.panel
        );
    
        Pane roomPane = roomView.getView();
        roomPane.setMaxWidth(Double.MAX_VALUE);
        roomPane.setPrefWidth(Region.USE_COMPUTED_SIZE);
        HBox.setHgrow(roomPane, Priority.ALWAYS);     // s'étend horizontalement
        VBox.setVgrow(roomPane, Priority.ALWAYS);     // verticalement

    
        mainLayout.getChildren().clear();
        mainLayout.getChildren().addAll(roomPane, rightPanel); // ajoute les deux vues côte à côte
    
        VBox root = new VBox(topSection, mainLayout); // combine top + contenu
        scene.setRoot(root);
    
        sortMode.valueProperty().addListener((obs, oldVal, newVal) -> {
            reservationView.sortReservations(newVal);
        });
    }
    
    
    private void populateFloorSelector() {
        floorSelector.getItems().clear();
    
        List<String> rawLabels = controller.getFloorLabels();
    
        for (int i = 0; i < rawLabels.size(); i++) {
            String label = rawLabels.get(i);           // ex: "Floor A"
            if (label.length() >= 7) {
                char letter = label.charAt(6);         // extrait 'A'
                String customLabel = "Étage " + letter + (i + 1); // ex: "Étage A1"
                floorSelector.getItems().add(customLabel);
            }
        }
    
        if (!floorSelector.getItems().isEmpty()) {
            floorSelector.setValue(floorSelector.getItems().get(0));
        }
    }
    

    public void updateRoomGrid() {
        List<Room> allRooms = controller.hotel.getAllRooms();
        if (allRooms.isEmpty()) {
            System.err.println("Erreur : aucune chambre trouvée.");
            return;
        }

        String selectedFloorValue = floorSelector.getValue();
        String label = (selectedFloorValue != null) ? selectedFloorValue : DEFAULT_FLOOR_LABEL;

        char selectedFloorLetter = 'A';
        if (label.length() >= ROOM_LABEL_MIN_LENGTH) {
            selectedFloorLetter = label.charAt(6);
        }

        roomView.setRooms(allRooms);
        roomView.setFloor(selectedFloorLetter);
        roomView.render();
    }

    public void updateReservationList(List<Reservation> reservations) {
        reservationView.setReservations(reservations);
    }

    public void show() {
        this.stage.show();
    }

    private HBox createLegend() {
        HBox legend = new HBox(PADDING * LEGEND_PADDING_MULTIPLIER);
        legend.setAlignment(Pos.CENTER_LEFT);
        legend.setPadding(new Insets(LEGEND_SECTION_PADDING));
        legend.setStyle(
            "-fx-background-color: " + ColorPalette.COLOR_BACKGROUND_LIGHT + ";" +
            "-fx-border-color: " + ColorPalette.COLOR_BORDER_LIGHT + ";" +
            "-fx-border-radius: 10px; -fx-background-radius: 10px;"
        );

        legend.getChildren().addAll(
            createLegendItem("Luxe", ColorPalette.COLOR_LUXE),
            createLegendItem("Business", ColorPalette.COLOR_BUSINESS),
            createLegendItem("Éco", ColorPalette.COLOR_ECO),
            createLegendItem("Occupée", ColorPalette.COLOR_ERROR)
        );

        return legend;
    }

    private HBox createLegendItem(String label, String color) {
        Label colorCircle = new Label(" ");
        colorCircle.setMinSize(LEGEND_COLOR_SIZE, LEGEND_COLOR_SIZE);
        colorCircle.setMaxSize(LEGEND_COLOR_SIZE, LEGEND_COLOR_SIZE);
        colorCircle.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-background-radius: 8px;" +
            "-fx-border-color: black;" +
            "-fx-border-radius: 8px;"
        );

        Label text = new Label(" " + label);
        text.setStyle("-fx-font-size: " + LEGEND_TEXT_SIZE + "px; -fx-font-weight: bold;");

        HBox item = new HBox(LEGEND_ITEM_SPACING, colorCircle, text);
        item.setAlignment(Pos.CENTER_LEFT);
        return item;
    }

    @Override
    public void onRoomAssigned(String roomLabel) {
        System.out.println("Chambre assignée : " + roomLabel);
        updateRoomGrid();
    }

    @Override
    public void onRoomReleased(String roomLabel) {
        System.out.println("Chambre libérée : " + roomLabel);
        updateRoomGrid();
    }

    public String getSelectedAssignmentMode() {
        return assignementMode.getValue();
    }
    public String getSelectedSortMode() {
        return sortMode.getValue();
    }
    
}
