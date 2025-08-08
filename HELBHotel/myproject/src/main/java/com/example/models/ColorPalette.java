package com.example.models;

public class ColorPalette {

    // === Couleurs par type de chambre ===
    public static final String COLOR_LUXE = "#9b59b6";     // Violet
    public static final String COLOR_BUSINESS = "#3498db"; // Bleu
    public static final String COLOR_ECO = "#e67e22";       // Orange
    public static final String COLOR_DEFAULT = "#95a5a6";   // Gris

    public static final String COLOR_HOVER_LUXE = "#8e44ad";
    public static final String COLOR_HOVER_BUSINESS = "#2980b9";
    public static final String COLOR_HOVER_ECO = "#d35400";
    public static final String COLOR_HOVER_DEFAULT = "#7f8c8d";

    // === Couleurs générales ===
    public static final String COLOR_SUCCESS = "#27ae60";   // Vert ✔
    public static final String COLOR_ERROR = "#e74c3c";     // Rouge ✘
    public static final String COLOR_INFO = "#2980b9";      // Bleu info
    public static final String COLOR_WARNING = "#f39c12";   // Jaune / Orange clair

    // === Couleurs UI ===
    public static final String COLOR_TEXT_DEFAULT = "#000000";
    public static final String COLOR_TEXT_MUTED = "#7f8c8d";
    public static final String COLOR_BACKGROUND_LIGHT = "#f4f4f4";
    public static final String COLOR_BORDER_LIGHT = "#ccc";

    // === Couleurs utilisées dans LotteryView ===
    public static final String COLOR_GOLD = "#f1c40f";          // Jaune (Gold ticket)
    public static final String COLOR_GOLD_SELECTED = "#7CFC00"; // Vert clair (sélection)
    public static final String COLOR_CONTINUE = "#3498db";      // Bleu (bouton "Continuer")
    public static final String COLOR_INFO_TEXT = "darkblue";    // Texte informatif
    public static final String COLOR_CODE_TEXT = "darkgreen";   // Texte du code gagné


    

    public static String getColor(String type) {
        switch (type) {
            case "L": return COLOR_LUXE;
            case "B": return COLOR_BUSINESS;
            case "E": return COLOR_ECO;
            default:  return COLOR_DEFAULT;
        }
    }

    public static String getHoverColor(String type) {
        switch (type) {
            case "L": return COLOR_HOVER_LUXE;
            case "B": return COLOR_HOVER_BUSINESS;
            case "E": return COLOR_HOVER_ECO;
            default:  return COLOR_HOVER_DEFAULT;
        }
    }
} 
