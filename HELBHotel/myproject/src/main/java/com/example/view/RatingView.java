package com.example.view;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import java.util.ArrayList;
import java.util.List;

public class RatingView {

    // === Constantes ===
    private static final int NB_NOTES = 5;
    private static final int LAYOUT_SPACING = 15;
    private static final int LAYOUT_PADDING = 20;
    private static final int CHECKBOX_SPACING = 10;
    private static final int DEFAULT_RATING = 5;

    public Dialog<Void> dialog;
    public VBox checkboxColumn;
    public Button confirmButton;
    public List<CheckBox> checkBoxes = new ArrayList<>();
    public int selectedRating = DEFAULT_RATING;

    /** Construit l'interface de la fenêtre d'évaluation avec NB_NOTES notes possibles. */
    public void build() {
        dialog = new Dialog<>();
        dialog.setTitle("Évaluation du séjour");

        VBox layout = new VBox(LAYOUT_SPACING);
        layout.setPadding(new Insets(LAYOUT_PADDING));
        layout.setAlignment(Pos.CENTER_LEFT);

        Label instruction = new Label("Cochez une seule note de satisfaction :");

        checkboxColumn = new VBox(CHECKBOX_SPACING);

        for (int i = 1; i <= NB_NOTES; i++) {
            CheckBox cb = new CheckBox("Note " + i);
            cb.setUserData(i);
            checkBoxes.add(cb);
            checkboxColumn.getChildren().add(cb);
        }

        confirmButton = new Button("Valider");

        layout.getChildren().addAll(instruction, checkboxColumn, confirmButton);
        dialog.getDialogPane().setContent(layout);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
    }

    public void show() {
        dialog.showAndWait();
    }
}


