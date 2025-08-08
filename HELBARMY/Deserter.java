public class Deserter extends Unit {

    private static final int HEALTH = 125;
    private static final int DAMAGE = 10;

    private int selectedImageIndex = 0;

    static {
        // Enregistrement des bonus spécifiques au Déserteur
        registerBonus("Deserter", "Pikeman", 1.5);     // Déserteur > Piquier
        registerBonus("Deserter", "Deserter", 1.25);  // Déserteur > Déserteur
    }

    public Deserter(int posX, int posY, HelbArmyController controller, int imageIndex) {
        super(posX, posY, HEALTH, DAMAGE, new String[]{
            "/img/WhiteDeserteurHELBARMY.drawio.png",
            "/img/BlackDeserteurHELBARMY.drawio.png"
        });
        this.controller = controller;
        setImageIndex(imageIndex);  // Définit l'index de l'image
    }

    public String getPathToImage() {
        return getPathToImage(selectedImageIndex);
    }

    public void setImageIndex(int index) {
        if (index >= 0 && index < getPathToImageLen()) {
            selectedImageIndex = index;
        }
    }

    public int getImageIndex() {
        return selectedImageIndex;
    }


    @Override
    public void moveAutomatically() {
        if (!isMovingAutomatically()) {
            return; // Si le mouvement automatique est désactivé, ne rien faire
        }
    
        // Appel à la méthode pour attaquer tous les ennemis à proximité
        attackNearbyEnemies(controller);
    
        // Objets sentinelles par défaut
        Collector targetCollector = new Collector(-1, -1, controller, HelbArmyController.VOID_PATH_INDEX);
        Unit closestEnemyUnit = new Deserter(-1, -1, controller, HelbArmyController.VOID_PATH_INDEX);
        double minCollectorDistance = Double.MAX_VALUE;
        double minEnemyDistance = Double.MAX_VALUE;
    
        for (Unit unit : controller.unitsList) {
            if (unit instanceof Collector) {
                Collector collector = (Collector) unit;
    
                if ((this.getImageIndex() == HelbArmyController.WHITE_PATH_INDEX && collector.getImageIndex() == HelbArmyController.BLACK_PATH_INDEX) ||
                    (this.getImageIndex() == HelbArmyController.BLACK_PATH_INDEX && collector.getImageIndex() == HelbArmyController.WHITE_PATH_INDEX)) {
    
                    double distanceToCollector = this.position.getDistanceWith(collector.position);
                    if (distanceToCollector < minCollectorDistance) {
                        minCollectorDistance = distanceToCollector;
                        targetCollector = collector;
                    }
                }
            } else if (unit.getImageIndex() != this.getImageIndex() && unit != this) {
                double distanceToEnemy = this.position.getDistanceWith(unit.position);
                if (distanceToEnemy < minEnemyDistance) {
                    minEnemyDistance = distanceToEnemy;
                    closestEnemyUnit = unit;
                }
            }
        }
    
        // Priorité : ennemi le plus proche
        if (minEnemyDistance < minCollectorDistance) {
            moveAwayFrom(closestEnemyUnit, controller);
        } else {
            if (isAdjacent(targetCollector)) {
                // Attaque l'ennemi directement
                int totalDamage = calculateDamage(targetCollector);
    
                targetCollector.takeDamage(totalDamage);
    
                if (targetCollector.health == 0) {
                    targetCollector.setPosX(controller.voidx);
                    targetCollector.setPosY(controller.voidy);
                    controller.removeUnit(targetCollector);
                }
            } else {
                moveTo(targetCollector.position.x, targetCollector.position.y, controller);
            }
        }
        
    }
    

    @Override
    protected String getName() {
        return "Deserter";
    }
}
