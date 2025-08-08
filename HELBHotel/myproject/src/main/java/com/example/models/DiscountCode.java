package com.example.models;

import java.util.Random;

import com.example.models.lottery.BronzeTicket;
import com.example.models.lottery.GoldTicket;
import com.example.models.lottery.SilverTicket;

/**
 * Représente un code de réduction unique, encodé avec un caractère final représentant le type.
 * Exemple : ABCDEFGHI**A** → Ticket Bronze
 */
public class DiscountCode {

    // === Constantes d'encodage de type ===
    private static final char ENCODED_BRONZE = 'A';
    private static final char ENCODED_SILVER = 'B';
    private static final char ENCODED_GOLD = 'C';
    private static final char ENCODED_UNKNOWN = 'X';

    // === Taille et index ===
    public static final int CODE_LENGTH = 10;
    private static final int BASE_LENGTH = 9; // 9 caractères aléatoires
    private static final int REDUCTION_CHAR_INDEX = 9; // 10e caractère (index 9)

    // === Données du code ===
    private final String code;
    private final int reduction; // 25 / 50 / 100
    private final String type;   // "Bronze", "Silver", "Gold"

    public DiscountCode(String code, int reduction, String type) {
        this.code = code;
        this.reduction = reduction;
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public int getReduction() {
        return reduction;
    }

    public String getType() {
        return type;
    }

    /**
     * Génère un code de réduction à partir d’un type et d’un pourcentage.
     * Exemple : "abcdefghA" (où A encode 25% pour Bronze).
     */
    public static DiscountCode generate(int reduction, String type) {
        String base = generateRandomBase(BASE_LENGTH);
        char encoded = encodeReduction(reduction);
        String fullCode = base + encoded;
        return new DiscountCode(fullCode, reduction, type);
    }

    /** Génère une base de code aléatoire (9 caractères base64 safe) */
    private static String generateRandomBase(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        String result = "";
        Random random = new Random();

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(chars.length());
            result += chars.charAt(index);
        }

        return result;
    }


    /**
     * Encode la réduction (en pourcentage) dans un caractère unique à la fin du code.
     */
    private static char encodeReduction(int reduction) {
        if (reduction == BronzeTicket.BRONZE_REDUCTION) return ENCODED_BRONZE;
        if (reduction == SilverTicket.SILVER_REDUCTION) return ENCODED_SILVER;
        if (reduction == GoldTicket.GOLD_REDUCTION) return ENCODED_GOLD;
        return ENCODED_UNKNOWN;
    }

    /**
     * Décode depuis un code complet la réduction qui y est encodée.
     */
    public static int decodeReduction(String code) {
        if (code == null || code.length() < CODE_LENGTH) return -1;

        char last = code.charAt(REDUCTION_CHAR_INDEX);
        switch (last) {
            case ENCODED_BRONZE: return BronzeTicket.BRONZE_REDUCTION;
            case ENCODED_SILVER: return SilverTicket.SILVER_REDUCTION;
            case ENCODED_GOLD:   return GoldTicket.GOLD_REDUCTION;
            default:             return -1;
        }
    }

    public static int validateCode(String code) {
        // Précondition : code ne doit jamais être null
        return code.isBlank() || code.length() != CODE_LENGTH
            ? -1
            : decodeReduction(code);
    }
    

    /**
     * Vérifie si un code est valide :
     * - 10 caractères,
     * - 9 alphanumériques (base64 URL-safe),
     * - 1 caractère de type valide (A/B/C).
     */
    public static boolean isAuthenticCode(String code) {
        if (code == null || code.length() != CODE_LENGTH) return false;

        String basePart = code.substring(0, BASE_LENGTH);
        char reductionChar = code.charAt(REDUCTION_CHAR_INDEX);

        boolean baseValid = basePart.matches("[A-Za-z0-9_-]{" + BASE_LENGTH + "}");
        boolean reductionValid = reductionChar == ENCODED_BRONZE ||
                                 reductionChar == ENCODED_SILVER ||
                                 reductionChar == ENCODED_GOLD;

        return baseValid && reductionValid;
    }

    @Override
    public String toString() {
        return type + " Ticket - Code : " + code + " (" + reduction + "%)";
    }
}
