import java.util.Random;

public class Stone extends GameElement {

    static HelbArmyController controller;
    private static final Random random = new Random();
    private boolean collected = false;  // Indique si la pierre a été collectée

    public Stone(int posX, int posY) {
        super(posX, posY, new String[]{"img/PierrePhilosophaleHELBARMY.drawio.png"});
    }

    /**
     * Applique l'effet de la pierre philosophale à une unité.
     * Une unité a une chance sur deux de mourir ou de devenir invincible.
     * 
     * @param unit L'unité qui entre en contact avec la pierre
     * @param stone La pierre philosohale collectée
     */
    
    public void handleStoneEffect(Unit unit, Stone stone) {
        // Générer un entier aléatoire entre 0 et 1
        int randomValue = random.nextInt(2);  // Génère 0 ou 1
    
        // Si randomValue est 0, l'unité devient invincible, sinon elle meurt
        if (randomValue == 0) {
            unit.setHealth(Double.MAX_VALUE);  // L'unité devient invincible
            System.out.println(unit.getName() + " est devenu invincible après avoir touché la pierre philosophale !");
        } else {
            unit.setHealth(0);  // L'unité meurt
            System.out.println(unit.getName() + " est mort après avoir touché la pierre philosophale !");
        }
    
        stone.setCollected(true);  // Marquer la pierre comme collectée
    }


    public boolean isCollected() {
        return collected;
    }

    public void setCollected(boolean collected) {
        this.collected = collected;
    }
}