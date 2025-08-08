public class Flag extends GameElement {

    static HelbArmyController controller;
    private boolean isCollected;
    long appearTime;  // Temps d'apparition du drapeau
    boolean collected = false;  // Le drapeau peut être collecté une seule fois
    private static final double BONUS_PERCENTAGE = 0.5;

    public Flag(int posX, int posY) {
        super(posX, posY, new String[] {"img/RedFlagHELBARMY.drawio.png"});
        this.isCollected = false;
        this.appearTime = System.currentTimeMillis(); // Temps initial de l'apparition
    }

    public void applyBonus(Unit unit) {
        // Appliquer un bonus de 50% aux points de vie de l'unité
        double currentHealth = unit.health;
        double bonusHealth = currentHealth * BONUS_PERCENTAGE; // Bonus de 50%
        unit.setHealth(currentHealth + bonusHealth); // Mettre à jour les points de vie
        
        // Log des unités qui reçoivent le bonus
        System.out.println(unit.getName() + " de l'équipe " + (unit.getImageIndex() == 0 ? "blanche" : "noire") +
                " a reçu le bonus de santé, maintenant à " + unit.health + " HP");
    }

    public void setCollected(boolean collected) {
        this.collected = collected;
    }

    public boolean isCollected() {
        return isCollected;
    }

    // Récupérer la position du drapeau
    public int getFlagX() {
        return getPosX();
    }

    public int getFlagY() {
        return getPosY();
    }
}
