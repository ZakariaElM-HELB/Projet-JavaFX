package com.example.view;

import com.example.HELBHotelController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

/**
 * Vue responsable de l'affichage du champ de réduction et de sa vérification.
 */
public class DiscountView {

    // === Constantes d'interface ===
    private static final int DIALOG_SPACING = 15;                // Espace entre les éléments verticaux
    private static final int DIALOG_PADDING = 20;                // Padding interne du layout

    // === Constantes réutilisables ===
    /** Longueur attendue d’un code de réduction */
    public static final int DISCOUNT_CODE_LENGTH = 10;

    // === Référence au contrôleur ===
    public HELBHotelController controller;

    // === Composants JavaFX accessibles depuis le contrôleur ===
    public Dialog<Void> dialog;
    public Button verifyButton;
    public TextField inputField;
    public Label resultLabel;

    /** Constructeur principal : initialise la vue avec le contrôleur */
    public DiscountView(HELBHotelController controller) {
        this.controller = controller;
    }

    /** Construit le contenu de la boîte de dialogue de vérification */
    public void buildDiscountDialog() {
        dialog = new Dialog<>();
        dialog.setTitle("Vérification du code de réduction");

        VBox layout = new VBox(DIALOG_SPACING);
        layout.setPadding(new Insets(DIALOG_PADDING));
        layout.setAlignment(Pos.CENTER_LEFT);

        Label instruction = new Label("Entrez un code de réduction (" + DISCOUNT_CODE_LENGTH + " caractères) :");

        inputField = new TextField();
        inputField.setPromptText("Code ici...");
        inputField.setPrefColumnCount(12);

        resultLabel = new Label("");
        resultLabel.setStyle("-fx-font-size: 14px;");

        verifyButton = new Button("Vérifier");

        layout.getChildren().addAll(instruction, inputField, verifyButton, resultLabel);
        dialog.getDialogPane().setContent(layout);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
    }

    /** Affiche la boîte de dialogue en mode modal */
    public void showDialog() {
        dialog.showAndWait();
    }

    /** Getters publics (au cas où le contrôleur ne veut pas accéder directement aux champs) */
    public TextField getInputField() { return inputField; }
    public Button getVerifyButton() { return verifyButton; }
    public Label getResultLabel() { return resultLabel; }
    public Dialog<Void> getDialog() { return dialog; }
}
