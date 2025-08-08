public class Knight extends Unit {

    private static final int HEALTH = 200;
    private static final int DAMAGE = 10;

    private int selectedImageIndex = 0;

    static {
        // Enregistrement des bonus spécifiques au Cavalier
        registerBonus("Knight", "Deserter", 2.0); // Cavalier > Déserteur
    }

    public Knight(int posX, int posY, HelbArmyController controller, int imageIndex) {
        super(posX, posY, HEALTH, DAMAGE, new String[]{
            "/img/WhiteCavalierHELBARMY.drawio.png",
            "/img/BlackCavalierHELBARMY.drawio.png"
        });
        this.controller = controller;
        setImageIndex(imageIndex);  // Définit l'index de l'image
    }

    public String getPathToImage() {
        return getPathToImage(selectedImageIndex);
    }

    public int getImageIndex() {
        return selectedImageIndex;
    }

    public void setImageIndex(int index) {
        if (index >= 0 && index < getPathToImageLen()) {
            selectedImageIndex = index;
        }
    }

    public void maintainSafetyDistanceWithAllies() {
        double safetyDistance = 1.0; // Distance de sécurité initiale
        Knight closestAlly = new Knight(-1, -1, controller, HelbArmyController.VOID_PATH_INDEX);
        double closestDistance = Double.MAX_VALUE;
    
        // Parcourir toutes les unités pour trouver les cavaliers alliés proches
        for (Unit unit : controller.unitsList) {
            if (unit instanceof Knight && unit.getImageIndex() == this.getImageIndex()) {
                Knight ally = (Knight) unit;
    
                // Calculer la distance avec cet allié
                double distanceToAlly = this.position.getDistanceWith(ally.position);
    
                if (distanceToAlly < closestDistance && !this.equals(ally)) {
                    closestDistance = distanceToAlly;
                    closestAlly = ally;
                }
            }
        }
    
        // Vérifier si un allié a été trouvé
        if (closestAlly == this) {
            if (closestDistance < safetyDistance) {
                // Trop proche, reculer dans la direction opposée
                int dx = this.position.x - closestAlly.position.x;
                int dy = this.position.y - closestAlly.position.y;
    
                int newX = this.position.x + (dx != 0 ? (dx / Math.abs(dx)) : 0);
                int newY = this.position.y + (dy != 0 ? (dy / Math.abs(dy)) : 0);
    
                moveTo(newX, newY, controller);
            } else if (closestDistance > safetyDistance) {
                // Trop loin, se rapprocher
                moveTo(closestAlly.position.x, closestAlly.position.y, controller);
            }
        }
    }
    
    @Override
    public void moveAutomatically() {
        if (!isMovingAutomatically()) {
            return; // Si le mouvement automatique est désactivé, ne rien faire
        }
    
        // Maintenir une distance de sécurité avec les alliés
        maintainSafetyDistanceWithAllies();
    
        // Chercher un Déserteur ennemi et le suivre s'il est trouvé
        if (huntAndFollowDeserter()) {
            return; // Si un déserteur a été trouvé et suivi, l'action est terminée
        }
    
        // Attaquer toutes les unités ennemies dans le rayon d'attaque
        attackNearbyEnemies(controller);
    
        // Se déplacer aléatoirement si aucune unité n'a été attaquée
        moveRandomly(controller);
    }

    private boolean huntAndFollowDeserter() {
        // Instanciation d'un Deserter par défaut pour les cas où aucun n'est trouvé
        Deserter targetDeserter = new Deserter(-1, -1, controller ,HelbArmyController.VOID_PATH_INDEX);
        double minDistance = Double.MAX_VALUE;
    
        for (Unit unit : controller.unitsList) {
            if (unit instanceof Deserter) {
                Deserter deserter = (Deserter) unit;
    
                // Vérifier si c'est un déserteur ennemi
                if ((this.getImageIndex() == HelbArmyController.WHITE_PATH_INDEX && deserter.getImageIndex() == HelbArmyController.BLACK_PATH_INDEX) ||
                    (this.getImageIndex() == HelbArmyController.BLACK_PATH_INDEX && deserter.getImageIndex() == HelbArmyController.WHITE_PATH_INDEX)) {
    
                    double distanceToDeserter = this.position.getDistanceWith(deserter.position);
                    if (distanceToDeserter < minDistance) {
                        minDistance = distanceToDeserter;
                        targetDeserter = deserter; // Mise à jour du déserteur cible
                    }
                }
            }
        }
    
        // Vérification si un déserteur réel a été trouvé
        if (targetDeserter.getImageIndex() != HelbArmyController.VOID_PATH_INDEX) {
            // Si le déserteur est adjacent, l'attaquer
            if (isAdjacent(targetDeserter)) {
                int totalDamage = calculateDamage(targetDeserter);
                targetDeserter.takeDamage(totalDamage);
    
                if (targetDeserter.health == 0) {
                    targetDeserter.setPosX(controller.voidx);
                    targetDeserter.setPosY(controller.voidy);
                    System.out.println(targetDeserter.getName() + " is dead. Removing from units list.");
                    controller.removeUnit(targetDeserter);
                }
            } else {
                // Sinon, se déplacer vers le déserteur
                moveTo(targetDeserter.position.x, targetDeserter.position.y, controller);
            }
            return true; // Action effectuée, déserteur trouvé et suivi
        }
    
        return false; // Aucun déserteur trouvé
    }
    

    @Override
    protected String getName() {
        return "Knight";
    }
}
