package com.example;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.util.Duration;
import javafx.stage.Stage;

import com.example.models.*;
import com.example.models.lottery.*;
import com.example.view.DiscountView;
import com.example.view.HELBHotelView;
import com.example.view.LotteryView;
import com.example.view.RatingView;
import com.example.observer.RoomObserver;
import com.example.parser.*;

import java.io.IOException;
import java.util.*;

public class HELBHotelController {

    private static final String HOTEL_CONFIG_PATH = "src/main/resources/hotel.hconf";
    private static final String RESERVATION_FILE_PATH = "src/main/resources/reservation.csv";
    private static final int REFRESH_INTERVAL_SECONDS = 10;

    public Hotel hotel;
    public final HELBHotelView view;
    private final RoomObserver roomObserver;
    private final DiscountView discountView;
    private final Strategy strategyModel;
    public final ReservationManager reservationManager = new ReservationManager();
    ReservationParser parser = new ReservationParser(RESERVATION_FILE_PATH);
    

    private Room pendingRoomForRelease = EmptyRoom.getInstance();
    // ============================
    // Initialisation & Configuration
    // ============================

    /** Constructeur principal qui initialise l'hôtel, la vue, les observateurs et lance le rafraîchissement. */
    public HELBHotelController(Stage stage) throws IOException {
        // === Étape 1 : Charger l’hôtel ou échouer immédiatement ===
        HotelParser hotelParser = new HotelParser(HOTEL_CONFIG_PATH);
        this.hotel = hotelParser.loadHotel(); // Peut lancer IOException ou IllegalArgumentException

        // === Étape 2 : Charger les réservations ===
        ReservationParser parser = new ReservationParser(RESERVATION_FILE_PATH);
        List<Reservation> loadedReservations = parser.loadReservations();
        for (Reservation res : loadedReservations) {
            reservationManager.add(res);
        }
        parser.clearFile();

        // === Étape 3 : Initialiser la vue et le reste ===
        this.view = new HELBHotelView(this, stage);
        reservationManager.addObserver(view.reservationView);
        this.roomObserver = view;
        this.discountView = new DiscountView(this);
        this.strategyModel = new Strategy(hotel, reservationManager);
        connectReservationButtons();
        
        initializeViewEvents();
        view.show();
        refreshData();

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(REFRESH_INTERVAL_SECONDS), e -> refreshData()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    

    /** Connecte les événements de la vue principale (sélection étage, redimensionnement, bouton vérif code). */
    public void initializeViewEvents() {
        view.floorSelector.valueProperty().addListener((obs, oldVal, newVal) -> updateRoomGridAndReconnect());
        view.scene.widthProperty().addListener((obs, oldVal, newVal) -> updateRoomGridAndReconnect());
        view.scene.heightProperty().addListener((obs, oldVal, newVal) -> updateRoomGridAndReconnect());
        view.verifyCodeBtn.setOnAction(e -> handleDiscountPopup());
    }
    


    // ============================
    // Gestion des Chambres (Rooms)
    // ============================

    /** Rafraîchit l'état de l'hôtel : rechargement CSV, attribution, rendu. */
    public void refreshData() {
        try {
            // Étape 1 : Recharger les nouvelles réservations du fichier
            List<Reservation> newlyLoaded = parser.loadReservations();

            // Étape 2 : Ajouter uniquement les réservations non déjà présentes
            for (Reservation r : newlyLoaded) {
                if (!reservationManager.contains(r)) {
                    reservationManager.add(r);
                }
            }

            // Étape 3 : Nettoyer le fichier pour enlever les lignes valides
            parser.clearFile();

            // Étape 4 : Libérer les chambres devenues obsolètes
            hotel.releaseObsoleteRooms(roomObserver);

            // Étape 5 : Proposer une chambre à chaque réservation sans proposition
            for (Reservation reservation : reservationManager.getAllReservations()) {
                if (!reservationManager.hasProposal(reservation)) {
                    String label = proposeRoomForReservation(reservation);
                    if (!label.isEmpty()) {
                        reservationManager.proposeRoom(reservation, label);
                    }
                }
            }

            // Étape 6 : Rafraîchir les vues
            view.reservationView.sortReservations(view.getSelectedSortMode());
            connectReservationButtons();
            view.roomView.render();     

        } catch (Exception e) {
            System.err.println("Erreur lors du rafraîchissement : " + e.getMessage());
        }
    }

    /** Met à jour la grille de chambres et reconnecte les boutons. */
    public void updateRoomGridAndReconnect() {
        view.updateRoomGrid();
        connectRoomButtons();
    }

    /** Connecte les boutons des chambres pour afficher un popup lorsqu'elles sont occupées. */
    public void connectRoomButtons() {
        for (Room room : hotel.getAllRooms()) {
            String label = room.getLabel();
            if (view.roomView.hasButton(label)) {
                Button button = view.roomView.getButton(label); // jamais null ici
    
                button.setOnAction(e -> {
                    if (room.isOccupied()) {
                        Dialog<Void> dialog = view.roomView.createOccupiedRoomDialog(room);
                        Button releaseBtn = view.roomView.getCurrentReleaseButton(); // garanti par construction
                        releaseBtn.setOnAction(ev -> promptForRatingAndRelease(room, dialog));
                        dialog.showAndWait();
                    }
                });
            }
        }
    }
    
    

    /** Finalise la libération d'une chambre après l'étape de ticket. */
    public void finalizeRoomRelease() {
        if (pendingRoomForRelease != EmptyRoom.getInstance()) {
            String label = pendingRoomForRelease.getLabel();
            hotel.releaseRoom(label);
            roomObserver.onRoomReleased(label);
            pendingRoomForRelease = EmptyRoom.getInstance();
            refreshData();
        }
    }
    


    // ============================
    // Évaluation (Rating) & Libération
    // ============================

    /** Affiche une fenêtre d’évaluation avec des CheckBox, puis déclenche le ticket. */
    public void promptForRatingAndRelease(Room room, Dialog<Void> parentDialog) {
        RatingView ratingView = new RatingView();
        ratingView.build();
    
        for (CheckBox cb : ratingView.checkBoxes) {
            cb.setOnAction(ev -> {
                for (CheckBox other : ratingView.checkBoxes) {
                    if (other != cb) other.setSelected(false);
                }
                ratingView.selectedRating = (int) cb.getUserData();
            });
        }
    
        //Reservation pendingReservationToRemove = room.getAssignedReservation();
    
        ratingView.confirmButton.setOnAction(ev -> {
            int rating = ratingView.selectedRating;
            promptForTicket(room, rating, parentDialog); // passe la fenêtre originale à fermer plus tard
            ratingView.dialog.close();             // ferme la fenêtre d'évaluation
        });
    
        ratingView.show();
    }
    

    // ============================
    // Loterie (Tickets & Jeux)
    // ============================

    /** Affiche la popup de ticket et prépare le jeu de loterie. */
    public void promptForTicket(Room room, int rating, Dialog<Void> dialogToClose) {
        pendingRoomForRelease = room;

        LotteryTicket ticket = LotteryTicketFactory.generateTicket(room, rating);
        startTicketGame(ticket, dialogToClose); // contrôleur gère la vue
    }

    /** Connecte les bons boutons en fonction du type de ticket. */
    public void connectLotteryButtons(LotteryView view) {
        LotteryTicket ticket = view.getTicket();

        if (ticket instanceof BronzeTicket) {
            setupBronzeTicket((BronzeTicket) ticket, view);
        } else if (ticket instanceof SilverTicket) {
            setupSilverTicket((SilverTicket) ticket, view);
        } else if (ticket instanceof GoldTicket) {
            setupGoldTicket((GoldTicket) ticket, view);
        }
    }

    /** Gère le jeu Bronze : 2 portes au choix. */
    private void setupBronzeTicket(BronzeTicket bronze, LotteryView view) {
        if (view.buttons.containsKey(LotteryView.BTN_BRONZE_DOOR1)) {
            Button door1 = view.buttons.get(LotteryView.BTN_BRONZE_DOOR1);
            door1.setOnAction(e -> bronze.play(BronzeTicket.CHOICE_DOOR1));
        }
    
        if (view.buttons.containsKey(LotteryView.BTN_BRONZE_DOOR2)) {
            Button door2 = view.buttons.get(LotteryView.BTN_BRONZE_DOOR2);
            door2.setOnAction(e -> bronze.play(BronzeTicket.CHOICE_DOOR2));
        }
    }

    /** Gère le jeu Silver : devinette à saisir. */
    private void setupSilverTicket(SilverTicket silver, LotteryView view) {
        if (view.buttons.containsKey(LotteryView.BTN_SILVER_SUBMIT)) {
            Button submit = view.buttons.get(LotteryView.BTN_SILVER_SUBMIT);
            submit.setOnAction(e -> {
                String guess = view.getSilverInput().getText();
                silver.submitGuess(guess);
            });
        }
    }
    

    /** Gère le jeu Gold : choix initial puis final parmi des portes. */
    private void setupGoldTicket(GoldTicket gold, LotteryView view) {
        for (Integer door : gold.getRemainingDoors()) {
            String key = LotteryView.BTN_GOLD_INITIAL_PREFIX + door;
            if (view.buttons.containsKey(key)) {
                Button initialBtn = view.buttons.get(key);
                final int chosen = door;

                initialBtn.setOnAction(e -> {
                    // Étape 1 : Mémoriser le choix dans le modèle
                    gold.makeInitialChoice(chosen);

                    // Étape 2 : Désactiver tous les boutons de départ
                    disableGoldDoorButtons(view); // méthode centralisée dans le contrôleur

                    // Étape 3 : Éliminer une mauvaise porte (modèle pur)
                    gold.eliminateNextBadDoor();

                    // Étape 4 : Gérer la suite du jeu via le contrôleur
                    handleGoldProgress(gold, view);
                });
            }
        }
    }

    // Cette méthode démarre le jeu de loterie correspondant au ticket reçu.
    // Je crée une nouvelle vue `LotteryView` en lui passant le ticket, le contrôleur (this), et
    // éventuellement une fenêtre à fermer une fois le jeu terminé.
    // Ensuite, j'affiche la popup du jeu avec showPopup(...)
    // et je connecte les boutons du jeu via connectLotteryButtons.
    public void startTicketGame(LotteryTicket ticket, Dialog<Void> dialogToClose) {
        // Création de la vue du jeu avec le ticket, le contrôleur et le dialog à fermer
        LotteryView lotteryView = new LotteryView(ticket, this, dialogToClose);

        // Affichage de la popup du jeu (affiche automatiquement le bon type de jeu)
        lotteryView.showPopup(view.stage);

        // Connexion des boutons du jeu (chaque type de ticket a des boutons différents à gérer)
        connectLotteryButtons(lotteryView);
    }




    // Cette méthode connecte les boutons affichés après l’élimination d’une première porte dans le jeu Gold.
    // À ce moment-là, le joueur peut soit garder sa porte actuelle, soit en changer (phase intermédiaire).
    // Je gère trois cas possibles : garder, changer, ou continuer (s'il ne reste plus qu'une décision simple à prendre).
    public void connectIntermediateGoldButtons(LotteryView view, GoldTicket gold) {
        // Je vérifie que les deux boutons "Garder" et "Changer" sont bien présents dans la vue
        if (view.buttons.containsKey(LotteryView.BTN_GOLD_KEEP_INTER) &&
            view.buttons.containsKey(LotteryView.BTN_GOLD_SWITCH_INTER)) {

            Button keepBtn = view.buttons.get(LotteryView.BTN_GOLD_KEEP_INTER);
            Button switchBtn = view.buttons.get(LotteryView.BTN_GOLD_SWITCH_INTER);

            // Si le joueur choisit de garder sa porte
            keepBtn.setOnAction(e -> {
                view.removeContinueButton(); // je nettoie le bouton "Continuer" si présent
                view.layout.getChildren().remove(((Button) e.getSource()).getParent()); // je retire les boutons de l’affichage

                gold.eliminateNextBadDoor(); // la logique interne du ticket élimine une nouvelle porte
                handleGoldProgress(gold, view); // je continue le jeu (affichage + logique)
            });

            // Si le joueur choisit de changer de porte
            switchBtn.setOnAction(e -> {
                view.removeContinueButton();
                view.layout.getChildren().remove(((Button) e.getSource()).getParent());

                // Je cherche la nouvelle porte disponible (celle qu'il n’a pas choisie à la base)
                for (Integer d : gold.getRemainingDoors()) {
                    if (!d.equals(gold.getPlayerChoice())) {
                        String key = LotteryView.BTN_GOLD_INITIAL_PREFIX + d;

                        if (view.buttons.containsKey(key)) {
                            Button b = view.buttons.get(key);
                            b.setDisable(false); // je réactive le bouton de cette porte

                            final int door = d;
                            b.setOnAction(ev -> {
                                // Le joueur choisit cette nouvelle porte
                                gold.setPlayerChoice(door);
                                view.highlightSelectedDoor(door);
                                disableGoldDoorButtons(view); // je désactive tous les autres
                                gold.eliminateNextBadDoor(); // j’élimine encore une mauvaise porte
                                handleGoldProgress(gold, view); // je poursuis le jeu
                            });
                        }
                    }
                }
            });
        }

        // Si un bouton "Continuer" est présent (cas rare : pas de choix à faire), je le connecte ici
        if (view.buttons.containsKey(LotteryView.BTN_GOLD_CONTINUE)) {
            Button continueBtn = view.buttons.get(LotteryView.BTN_GOLD_CONTINUE);
            continueBtn.setOnAction(e -> {
                view.removeContinueButton();
                gold.eliminateNextBadDoor(); // même logique : on avance
                handleGoldProgress(gold, view);
            });
        }
    }


    
   // Cette méthode gère l’avancement du jeu Gold après chaque action du joueur.
    // Elle s’occupe d’afficher les bons boutons et de continuer la logique selon l’étape du jeu.
    // Je commence toujours par retirer le bouton "Continuer" s’il était encore affiché.
    public void handleGoldProgress(GoldTicket gold, LotteryView view) {
        view.removeContinueButton(); // on nettoie toujours d’abord l’interface

        // Si on est déjà dans la phase finale (choix définitif entre deux portes)
        if (gold.isFinalPhaseStarted()) {
            // J’affiche l’écran final (garder ou changer définitivement)
            view.showGoldFinalChoice(gold);
            connectGoldFinalButtons(view, gold); // je connecte les deux boutons
        }

        // Sinon, s’il reste plus de 2 portes : on est encore dans une phase intermédiaire
        else if (gold.getRemainingDoors().size() > 2) {
            // Le joueur doit décider de garder sa porte ou en choisir une autre
            view.showIntermediateKeepOrSwitch(gold);
            connectIntermediateGoldButtons(view, gold); // je connecte les boutons "garder" et "changer"
        }

        // Dernier cas : il ne reste que 2 portes → le joueur doit cliquer pour changer
        else {
            view.showSwitchableDoors(gold); // j’affiche les boutons "changer pour porte X"

            for (Integer door : gold.getRemainingDoors()) {
                // Je ne montre que les portes différentes de celle choisie
                if (!door.equals(gold.getPlayerChoice())) {
                    String key = LotteryView.BTN_GOLD_SWITCH_TO_PREFIX + door;

                    if (view.buttons.containsKey(key)) {
                        Button b = view.buttons.get(key);
                        b.setOnAction(e -> {
                            gold.setPlayerChoice(door); // je change la porte choisie
                            view.highlightSelectedDoor(door); // je mets à jour visuellement
                            disableGoldDoorButtons(view); // je désactive toutes les autres
                            gold.eliminateNextBadDoor(); // j’élimine encore une porte (si possible)
                            handleGoldProgress(gold, view); // je continue le jeu normalement
                        });
                    }
                }
            }
        }
    }


    // Désactive tous les boutons de portes initiales dans le jeu Gold une fois qu’un choix a été fait
    public void disableGoldDoorButtons(LotteryView view) {
        for (Map.Entry<String, Button> entry : view.buttons.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith(LotteryView.BTN_GOLD_INITIAL_PREFIX)) {
                entry.getValue().setDisable(true);
            }
        }
    }



    // Connecte les deux derniers boutons du jeu Gold : "Garder" ou "Changer" pour la décision finale
    public void connectGoldFinalButtons(LotteryView view, GoldTicket gold) {
        Button keepBtn = view.buttons.get(LotteryView.BTN_GOLD_KEEP);
        Button switchBtn = view.buttons.get(LotteryView.BTN_GOLD_SWITCH);

        keepBtn.setOnAction(e -> {
            keepBtn.setDisable(true);
            switchBtn.setDisable(true); // désactive l’autre bouton aussi
            gold.makeFinalChoice(gold.getPlayerChoice()); // garde sa porte initiale
        });

        switchBtn.setOnAction(e -> {
            switchBtn.setDisable(true);
            keepBtn.setDisable(true); // désactive l’autre bouton aussi

            for (Integer d : gold.getRemainingDoors()) {
                if (!d.equals(gold.getPlayerChoice())) {
                    gold.makeFinalChoice(d); // change pour l’autre porte
                    break;
                }
            }
        });
    }

    
    

   // ============================
    //  Réservations & Assignation
    // ============================

    /** Met à jour la proposition de chambre pour une réservation. */
    public void updateRoomProposal(Reservation reservation) {
        String label = proposeRoomForReservation(reservation);
        if (!label.isEmpty()) {
            reservationManager.proposeRoom(reservation, label);
            refreshData();
        }
    }
    
    
    // Connecte les boutons associés aux réservations affichées :
    // - Le bouton "proposer" génère une nouvelle chambre avec la stratégie actuelle.
    // - Le bouton "nom" ouvre une fenêtre pour permettre une réservation manuelle avec saisie du label.
    public void connectReservationButtons() {
        List<Button> nameButtons = view.reservationView.reservationButtons;
        List<Button> proposeButtons = view.reservationView.reservationProposeButtons;
        List<Reservation> reservations = view.reservationView.reservationRefs;

        for (int i = 0; i < reservations.size(); i++) {
            Reservation res = reservations.get(i);
            Button nameBtn = nameButtons.get(i);
            Button proposeBtn = proposeButtons.get(i);

            proposeBtn.setOnAction(e -> updateRoomProposal(res));

            nameBtn.setOnAction(e -> {
                Button confirmBtn = new Button("Confirmer");
                TextField roomField = new TextField();
                Dialog<Void> dialog = view.reservationView.createReservationDialog(res, confirmBtn, roomField);

                confirmBtn.setOnAction(ev -> {
                    String label = roomField.getText().trim().toUpperCase();
                    confirmReservation(res, label);
                    view.reservationView.removeReservation(res);
                    dialog.close();
                });

                dialog.showAndWait();
            });
        }
    }



    /** Confirme une réservation et assigne une chambre. */
    public void confirmReservation(Reservation reservation, String roomLabel) {
        // Vérifie si la chambre est valide et disponible
        if (!hotel.confirmReservation(roomLabel, reservation)) {
            System.err.println("Chambre non valide ou déjà occupée : " + roomLabel);
            return;
        }
    
        // Marque la chambre comme occupée dans le modèle
        roomObserver.onRoomAssigned(roomLabel);
    
        // Marque la réservation comme assignée (trace dans assignedClients)
        reservationManager.assignReservation(reservation, roomLabel);
    
        // Supprime définitivement la réservation de la mémoire
        reservationManager.remove(reservation);
    
        System.out.println("Réservation confirmée pour : " + reservation.getKey());
    }
    
    
    
    
    

    /** Utilise la stratégie sélectionnée pour proposer une chambre. */
    public String proposeRoomForReservation(Reservation reservation) {
        String selected = view.getSelectedAssignmentMode();
    
        if (!strategyModel.contains(selected)) {
            throw new IllegalStateException("Stratégie invalide sélectionnée : " + selected);
        }
    
        return strategyModel.assign(reservation, selected);
    }

// ============================
// Outils & Utilitaires
// ============================

    /** Retourne les étiquettes d'étages (A, B, C...). */
    public List<String> getFloorLabels() {
        return hotel.getFloorLabels();
    } 


// ============================
// Réduction (Discount)
// ============================

    /** Affiche la fenêtre de réduction et traite le code saisi. */
    private void handleDiscountPopup() {
        discountView.buildDiscountDialog();
    
        discountView.getVerifyButton().setOnAction(ev -> {
            String rawCode = discountView.getInputField().getText();
            if (rawCode == null) return; // Défensif (évite un NullPointerException)
    
            String code = rawCode.trim();
    
            if (code.length() != DiscountView.DISCOUNT_CODE_LENGTH) {
                setDiscountResult(discountView.getResultLabel(),
                    "✘ Code invalide (10 caractères requis)",
                    ColorPalette.COLOR_ERROR);
                return;
            }
    
            int reduction = DiscountCode.decodeReduction(code);
    
            if (reduction > 0) {
                setDiscountResult(discountView.getResultLabel(),
                    "✔ " + reduction + "% de réduction",
                    ColorPalette.COLOR_SUCCESS);
            } else {
                setDiscountResult(discountView.getResultLabel(),
                    "✘ Aucun code valide détecté",
                    ColorPalette.COLOR_ERROR);
            }
        });
    
        discountView.showDialog();
    }
    

    /** Applique le message et la couleur en fonction du résultat du code. */
    private void setDiscountResult(Label label, String msg, String color) {
        label.setText(msg);
        label.setStyle("-fx-text-fill: " + color + ";");
    }
}