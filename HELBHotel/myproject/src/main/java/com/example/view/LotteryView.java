package com.example.view;

import com.example.HELBHotelController;
import com.example.models.ColorPalette;
import com.example.models.DiscountCode;
import com.example.models.lottery.*;
import com.example.observer.LotteryObserver;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.scene.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LotteryView implements LotteryObserver {

    public Dialog<Void> dialog;
    
    // === Styles CSS avec couleurs centralisées via ColorPalette ===
    private static final String GOLD_BUTTON_STYLE =
    "-fx-background-color: " + ColorPalette.COLOR_GOLD + "; -fx-text-fill: black; -fx-font-weight: bold; -fx-background-radius: 10px; -fx-padding: 10 15;";

    private static final String GOLD_BUTTON_SELECTED_STYLE =
    "-fx-background-color: " + ColorPalette.COLOR_GOLD_SELECTED + "; -fx-font-weight: bold;";

    private static final String STYLE_KEEP_BUTTON =
    "-fx-background-color: " + ColorPalette.COLOR_SUCCESS + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10px; -fx-padding: 10 15;";

    private static final String STYLE_SWITCH_BUTTON =
    "-fx-background-color: " + ColorPalette.COLOR_ECO + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10px; -fx-padding: 10 15;";

    private static final String STYLE_CONTINUE_BUTTON =
    "-fx-background-color: " + ColorPalette.COLOR_CONTINUE + "; -fx-text-fill: white; -fx-font-weight: bold;";

    private static final String STYLE_INFO_LABEL =
    "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + ColorPalette.COLOR_INFO_TEXT + ";";

    private static final String STYLE_CODE_LABEL =
    "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + ColorPalette.COLOR_CODE_TEXT + ";";


    // === Identifiants publics des boutons pour liaison externe ===
    public static final String BTN_BRONZE_DOOR1 = "bronze_door1";
    public static final String BTN_BRONZE_DOOR2 = "bronze_door2";
    public static final String BTN_SILVER_SUBMIT = "silver_submit";
    public static final String BTN_GOLD_KEEP = "gold_keep";
    public static final String BTN_GOLD_SWITCH = "gold_switch";
    public static final String BTN_GOLD_KEEP_INTER = "gold_keep_inter";
    public static final String BTN_GOLD_SWITCH_INTER = "gold_switch_inter";
    public static final String BTN_GOLD_INITIAL_PREFIX = "gold_initial_"; // suivi du numéro
    public static final String BTN_GOLD_CONTINUE = "gold_continue";
    public static final String BTN_GOLD_SWITCH_TO_PREFIX = "gold_switch_to_"; // suivi du numéro


    // === Dimensions publiques réutilisables (ex: dans controller) ===
    public static final double DIALOG_WIDTH = 500;
    public static final double DIALOG_HEIGHT = 350;
    public static final int PADDING = 20;
    public static final int SPACING_VERTICAL = 15;
    public static final int FLOW_HGAP = 15;
    public static final int FLOW_VGAP = 10;
    public static final int BUTTON_WIDTH_STANDARD = 120;
    public static final int BUTTON_WIDTH_KEEP = 160;
    public static final int BUTTON_WIDTH_SWITCH = 200;
    public static final int BUTTON_WIDTH_CONTINUE = 250;
    public static final int BUTTON_MIN_WIDTH_BRONZE = 120;
    public static final int SPACING_HORIZONTAL_BRONZE = 20;
    public static final int SPACING_HORIZONTAL_INTERMEDIATE = 15;
    public static final int SPACING_HORIZONTAL_FINAL_CHOICE = 20;
    


    // === Textes réutilisables (ex: logs ou UI) ===
    public static final String TEXT_INTERMEDIATE_CHOICE = "Voulez-vous garder votre porte ou en changer ?";
    public static final String TEXT_FINAL_CHOICE = "Souhaitez-vous garder votre porte ou changer ?";
    public static final String TEXT_CONTINUE = "Continuer l’ouverture";




    private final LotteryTicket ticket;                      // Ticket de loterie à afficher
    private Label resultLabel;                               // Label pour afficher le résultat du jeu
    public VBox layout;                                     // Layout principal du popup
    private TextField silverInput;                           // Champ de saisie pour le jeu Silver
    private Button continueButton = new Button(); 
    private Label questionLabel = new Label(); 
    private final Dialog<Void> dialogToClose;

    public final Map<String, Button> buttons = new HashMap<>(); // Map des boutons cliquables par identifiants
    private HELBHotelController controller;                  // Référence vers le contrôleur pour libérer la chambre

    /** Constructeur : initialise la vue avec le ticket à observer. */
    public LotteryView(LotteryTicket ticket, HELBHotelController controller, Dialog<Void> dialogToClose) {
        this.ticket = ticket;
        this.controller = controller;
        this.dialogToClose = dialogToClose;
        this.ticket.addObserver(this);
    }
    

    // Cette méthode affiche la popup du jeu de loterie.
    // Elle adapte l'interface en fonction du type de ticket (Bronze, Silver ou Gold).
    // Le layout est construit dynamiquement, puis je connecte tous les boutons via le contrôleur.
    // Une fois tout affiché, j’ajoute le label de résultat et j’ouvre la fenêtre.
    public void showPopup(Stage owner) {
        dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.setTitle("Jeu de Loterie : " + ticket.getClass().getSimpleName());
        dialog.setResizable(true);

        dialog.getDialogPane().setPrefWidth(DIALOG_WIDTH);
        dialog.getDialogPane().setPrefHeight(DIALOG_HEIGHT);
        layout = new VBox(SPACING_VERTICAL);
        layout.setPadding(new Insets(PADDING));
        layout.setAlignment(Pos.CENTER_LEFT);

        resultLabel = new Label();

        if (ticket instanceof BronzeTicket) {
            buildBronzeGame();
        } else if (ticket instanceof SilverTicket) {
            buildSilverGame();
        } else if (ticket instanceof GoldTicket) {
            buildGoldGame();
        }

        // Les boutons sont ajoutés dans une map publique,
        // donc je les connecte ensuite depuis le contrôleur
        controller.connectLotteryButtons(this);

        layout.getChildren().add(resultLabel);
        dialog.getDialogPane().setContent(layout);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    

    // Ici je construis l’interface pour le ticket Bronze.
    // C’est un jeu simple avec deux boutons représentant deux portes.
    // Les boutons sont stylisés puis ajoutés horizontalement dans le layout principal.
    // Je les enregistre aussi dans la map `buttons` pour qu’ils puissent être récupérés ailleurs.
    private void buildBronzeGame() {
        Label instruction = new Label("Choisissez une porte :");
        instruction.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        instruction.setAlignment(Pos.CENTER);

        Button door1 = new Button("Porte 1");
        Button door2 = new Button("Porte 2");

        String styleBtn = "-fx-background-color: #3498db;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 10px;" +
                        "-fx-padding: 10 20;";

        door1.setStyle(styleBtn);
        door2.setStyle(styleBtn);

        door1.setMinWidth(BUTTON_MIN_WIDTH_BRONZE);
        door2.setMinWidth(BUTTON_MIN_WIDTH_BRONZE);

        HBox buttonRow = new HBox(SPACING_HORIZONTAL_BRONZE, door1, door2);
        buttonRow.setAlignment(Pos.CENTER);

        buttons.put(BTN_BRONZE_DOOR1, door1);
        buttons.put(BTN_BRONZE_DOOR2, door2);

        layout.getChildren().addAll(instruction, buttonRow);
    }

    

    // Pour le ticket Silver, le joueur doit deviner un mot mélangé.
    // Je récupère le mot via getScrambledWord() et je l'affiche.
    // Je crée un champ de texte pour que le joueur saisisse sa réponse,
    // et un bouton "Valider" qui sera connecté depuis le contrôleur.
    // Comme pour les autres jeux, je place tout dans le layout.
    private void buildSilverGame() {
        SilverTicket silver = (SilverTicket) ticket;

        Label scrambled = new Label("Mot mélangé : " + silver.getScrambledWord());

        silverInput = new TextField();
        silverInput.setPromptText("Votre proposition...");

        Button submit = new Button("Valider");
        buttons.put(BTN_SILVER_SUBMIT, submit);

        layout.getChildren().addAll(scrambled, silverInput, submit);
    }
    
    // Cette méthode construit l'interface pour le ticket Gold.
    // Je crée un FlowPane contenant un bouton par porte disponible.
    // Chaque bouton représente une "porte" cliquable par le joueur.
    // Je les stylise et les ajoute au layout. Je garde aussi les boutons
    // dans la map `buttons` avec un identifiant unique (ex: "gold_initial_3").
    private void buildGoldGame() {
        GoldTicket gold = (GoldTicket) ticket;

        Label instruction = new Label("Choisissez une porte :");

        FlowPane doorsFlow = new FlowPane();
        doorsFlow.setHgap(15);
        doorsFlow.setVgap(10);
        doorsFlow.setAlignment(Pos.CENTER);

        for (Integer door : gold.getRemainingDoors()) {
            Button doorBtn = new Button("Porte " + door);
            doorBtn.setPrefWidth(BUTTON_WIDTH_STANDARD);
            doorBtn.setStyle(GOLD_BUTTON_STYLE);

            buttons.put(BTN_GOLD_INITIAL_PREFIX + door, doorBtn);
            doorsFlow.getChildren().add(doorBtn);
        }

        layout.getChildren().addAll(instruction, doorsFlow);
    }


    public void showSwitchableDoors(GoldTicket gold) {
        for (Integer door : gold.getRemainingDoors()) {
            if (!door.equals(gold.getPlayerChoice())) {
                Button btn = new Button("Changer pour porte " + door);
                btn.setStyle(STYLE_SWITCH_BUTTON);
                btn.setMinWidth(BUTTON_WIDTH_SWITCH);
    
                // Identifiant unique pour ce bouton
                String key = LotteryView.BTN_GOLD_SWITCH_TO_PREFIX + door;
                buttons.put(key, btn);
    
                layout.getChildren().add(btn);
            }
        }
    }
    
    // Cette méthode s'affiche après qu'une mauvaise porte a été éliminée dans le jeu Gold.
    // Le joueur a le choix : garder sa porte actuelle ou la changer pour une autre encore disponible.
    // Je crée deux boutons "Garder" et "Changer", et je les ajoute avec un label de question.
    // Les boutons sont aussi ajoutés dans `buttons` pour pouvoir être connectés ensuite.
    public void showIntermediateKeepOrSwitch(GoldTicket gold) {
        removeContinueButton();
        removeQuestionLabel(); // au cas où un autre texte serait encore affiché

        Integer myDoor = gold.getPlayerChoice();
        List<Integer> others = new ArrayList<>();
        for (Integer d : gold.getRemainingDoors()) {
            if (!d.equals(myDoor)) {
                others.add(d);
            }
        }

        if (others.isEmpty()) return;
        Integer switchTarget = others.get(0); // je prends une porte disponible à proposer

        questionLabel = new Label(TEXT_INTERMEDIATE_CHOICE);
        questionLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        questionLabel.setAlignment(Pos.CENTER);
        layout.getChildren().add(questionLabel);

        Button keepBtn = new Button("Garder (n°" + myDoor + ")");
        Button switchBtn = new Button("Changer pour la porte n°" + switchTarget + ")");

        keepBtn.setStyle(STYLE_KEEP_BUTTON);
        switchBtn.setStyle(STYLE_SWITCH_BUTTON);

        keepBtn.setMinWidth(BUTTON_WIDTH_KEEP);
        switchBtn.setMinWidth(BUTTON_WIDTH_SWITCH);

        HBox choiceRow = new HBox(SPACING_HORIZONTAL_INTERMEDIATE, keepBtn, switchBtn);
        choiceRow.setAlignment(Pos.CENTER);
        layout.getChildren().add(choiceRow);

        buttons.put(BTN_GOLD_KEEP_INTER, keepBtn);
        buttons.put(BTN_GOLD_SWITCH_INTER, switchBtn);
    }


    // Cette méthode supprime le label de question affiché dans showIntermediateKeepOrSwitch().
    // Je la lance avant d’afficher une nouvelle question pour éviter d’empiler plusieurs labels.
    public void removeQuestionLabel() {
        layout.getChildren().remove(questionLabel);
    }

    // Cette méthode affiche la dernière étape du jeu Gold.
    // Le joueur doit confirmer s’il garde sa porte ou change pour l’autre restante.
    // J’affiche un label de question et deux boutons : “Garder” ou “Changer”.
    // Les boutons sont ajoutés dans la map `buttons` avec des identifiants bien précis.
    public void showGoldFinalChoice(GoldTicket gold) {
        removeContinueButton(); // je nettoie les boutons intermédiaires

        int myDoor = gold.getPlayerChoice();
        int otherDoor = -1;

        // Je cherche l'autre porte disponible (différente de celle du joueur)
        for (Integer door : gold.getRemainingDoors()) {
            if (door != myDoor) {
                otherDoor = door;
                break;
            }
        }

        // Par sécurité : s’il n’y a pas d’autre porte, je ne fais rien
        if (otherDoor == -1) return;

        Label question = new Label(TEXT_FINAL_CHOICE);
        question.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        question.setAlignment(Pos.CENTER);
        question.setMaxWidth(Double.MAX_VALUE);

        Button keepBtn = new Button("Garder ma porte (n°" + myDoor + ")");
        Button switchBtn = new Button("Changer pour la porte n°" + otherDoor + ")");

        keepBtn.setStyle(STYLE_KEEP_BUTTON);
        switchBtn.setStyle(STYLE_SWITCH_BUTTON);

        HBox buttonRow = new HBox(SPACING_HORIZONTAL_FINAL_CHOICE, keepBtn, switchBtn);
        buttonRow.setAlignment(Pos.CENTER);

        buttons.put(BTN_GOLD_KEEP, keepBtn);
        buttons.put(BTN_GOLD_SWITCH, switchBtn);

        layout.getChildren().addAll(question, buttonRow);
    }


    
    
    
    public void removeDoorButton(int doorNumber) {
        String key = BTN_GOLD_INITIAL_PREFIX + doorNumber;
    
        if (!buttons.containsKey(key)) return;
    
        Button btn = buttons.remove(key); // garanti non-null après containsKey
    
        for (Node node : layout.getChildren()) {
            if (node instanceof FlowPane) {
                FlowPane pane = (FlowPane) node;
                if (pane.getChildren().contains(btn)) {
                    pane.getChildren().remove(btn);
                }
            }
        }
    }
    
    
    

    // Cette méthode met en évidence la porte sélectionnée par le joueur.
    // Elle change la couleur du bouton sélectionné et remet les autres à leur style de base.
    public void highlightSelectedDoor(int selected) {
        for (Map.Entry<String, Button> entry : buttons.entrySet()) {
            String key = entry.getKey();

            if (!key.startsWith(BTN_GOLD_INITIAL_PREFIX)) continue;

            String suffix = key.substring(BTN_GOLD_INITIAL_PREFIX.length());
            if (!suffix.matches("\\d+")) continue;

            int doorNumber = Integer.parseInt(suffix);
            Button button = entry.getValue();

            // Si c’est la porte choisie, je mets le style “sélectionné”, sinon le style par défaut
            String style = (doorNumber == selected) ? GOLD_BUTTON_SELECTED_STYLE : GOLD_BUTTON_STYLE;
            button.setStyle(style);
        }
    }

    

    /** Retourne le champ de texte utilisé pour saisir une réponse dans le ticket Silver. */
    public TextField getSilverInput() {
        return silverInput;
    }

    // Cette méthode est appelée automatiquement quand le ticket notifie un résultat (via Observer).
    // J'affiche le message dans le label de résultat (gagné ou perdu).
    // Si le joueur a gagné, je génère un code de réduction et je l'affiche.
    // Ensuite, je libère la chambre via le contrôleur, puis je ferme la fenêtre précédente.
    @Override
    public void update(String resultMessage) {
        if (resultMessage == null) return;

        resultLabel.setText(resultMessage);

        // Je vérifie si le joueur a gagné (selon le message ou le taux de réduction)
        boolean isWin =
            resultMessage.toLowerCase().contains("gagné") ||
            resultMessage.contains(BronzeTicket.BRONZE_REDUCTION + "%") ||
            resultMessage.contains(SilverTicket.SILVER_REDUCTION + "%") ||
            resultMessage.contains(GoldTicket.GOLD_REDUCTION + "%");

        if (isWin) {
            String type = ticket.getClass().getSimpleName().replace("Ticket", "");
            DiscountCode code = DiscountCode.generate(ticket.getReduction(), type);

            Label infoLabel = new Label("Gagné avec un " + type + " Ticket !");
            infoLabel.setStyle(STYLE_INFO_LABEL);

            Label codeLabel = new Label("Code de réduction : " + code.getCode());
            codeLabel.setStyle(STYLE_CODE_LABEL);

            layout.getChildren().addAll(infoLabel, codeLabel);
        }

        controller.finalizeRoomRelease(); // je libère la chambre

        // Je ferme la fenêtre précédente s’il y en avait une à fermer
        if (dialogToClose != null) {
            dialogToClose.close();
        }
    }


    // Cette méthode ajoute un bouton "Continuer l’ouverture" dans le jeu Gold (après une étape).
    // Je supprime d'abord un éventuel bouton existant, puis je crée un nouveau avec le bon style.
    // Ce bouton est ajouté dans le layout et dans la map des boutons.
    public void addContinueButton() {
        removeContinueButton(); // par sécurité, j’efface l’ancien bouton s’il existe déjà

        continueButton = new Button(TEXT_CONTINUE);
        continueButton.setStyle(STYLE_CONTINUE_BUTTON);
        continueButton.setMaxWidth(BUTTON_WIDTH_CONTINUE);

        buttons.put(BTN_GOLD_CONTINUE, continueButton); // pour que le contrôleur le récupère facilement

        layout.getChildren().add(continueButton);
    }


    // Cette méthode retire le bouton "Continuer" du layout, s’il est présent.
    // Je l’utilise pour éviter d’avoir deux boutons identiques affichés en même temps.
    public void removeContinueButton() {
        layout.getChildren().remove(continueButton);
    }


    /** Retourne le ticket de loterie en cours d’affichage. */
    public LotteryTicket getTicket() {
        return ticket;
    }


    @Override
    public void onDoorEliminated(int door) {
        removeDoorButton(door);
    }

}

