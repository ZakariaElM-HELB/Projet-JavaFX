import java.util.HashMap;

public abstract class Unit extends GameElement {

    // HashMap centralisée : Attaquant -> (Cible -> Multiplicateur)
    private static final HashMap<String, HashMap<String, Double>> bonusMap = new HashMap<>();

    protected HelbArmyController controller;
    protected Coordinate position; // Coordonnées sur la grille
    protected int health;          // Points de vie
    protected int damage;          // Dégâts de base
    protected int imageIndex;      // Indice de l'image utilisée pour l'affichage
    protected double IMAGE_WIDTH;
    protected double IMAGE_HEIGHT;
    private boolean hasReceivedBonus = false;  // Attribut pour savoir si l'unité a déjà reçu le bonus
    protected boolean isMovingAutomatically = true; // Le mouvement automatique est activé par défaut

    private static final int ATTACK_RADIUS = 1; // Rayon d'attaque autour du Cavalier (1 case)

    public Unit(int posX, int posY, int health, int damage, String[] imagePaths) {
        super(posX, posY, imagePaths);
        this.position = new Coordinate(posX, posY);
        this.health = health;
        this.damage = damage;
    }

    



    // Méthode pour enregistrer les bonus
    protected static void registerBonus(String attackerName, String targetName, double multiplier) {
        bonusMap.putIfAbsent(attackerName, new HashMap<>());
        bonusMap.get(attackerName).put(targetName, multiplier);
    }

    // Méthode pour calculer les dégâts
    public int calculateDamage(Unit target) {
        double multiplier = 1.0; // Multiplicateur par défaut
        String attackerName = this.getName();
        String targetName = target.getName();

        // Vérifie s'il existe un bonus pour ce type d'attaquant et de cible
        if (bonusMap.containsKey(attackerName) && bonusMap.get(attackerName).containsKey(targetName)) {
            multiplier = bonusMap.get(attackerName).get(targetName);
        }

        // Calcul final des dégâts
        return (int) Math.round(this.damage * multiplier);
    }


    // a mettre dans le controller
    public void attackNearbyEnemies(HelbArmyController controller) {
        for (Unit unit : controller.unitsList) {
            // Vérifier si l'unité est un ennemi
            if (unit != this && unit.getImageIndex() != this.getImageIndex()) {
                // Vérifier si l'unité est dans le rayon d'attaque (distance <= ATTACK_RADIUS)
                double distance = this.position.getDistanceWith(unit.position);
                if (distance <= ATTACK_RADIUS) {
                    // Calculer et appliquer les dégâts
                    int totalDamage = calculateDamage(unit);
                     System.out.println(this.getName() + " attacks " + unit.getName() + " with " + totalDamage + " damage!");

                    unit.takeDamage(totalDamage);

                    // Vérifier si l'unité est morte et la retirer de la liste des unités
                    if (unit.health == 0) {
                        unit.setPosX(controller.voidx);
                        unit.setPosY(controller.voidy);
                        // System.out.println(unit.getName() + " is dead. Removing from units list.");
                        controller.removeUnit(unit);
                    }
                }
            }
        }
    }

    public void collectFlag(Unit unit, Flag flag) {
        // Vérifier si le drapeau est déjà collecté
        if (flag.isCollected()) {
            System.out.println("Le drapeau a déjà été collecté, aucun bonus ne sera appliqué.");
            return; // Ne rien faire si le drapeau est déjà collecté
        }
    
        // Calculer la distance entre l'unité et le drapeau
        int distanceX = Math.abs(unit.getPosX() - flag.getPosX());
        int distanceY = Math.abs(unit.getPosY() - flag.getPosY());
        
        // Vérifier si l'unité est dans un rayon de 1 ou 2 cases du drapeau
        if (distanceX <= 1 && distanceY <= 1) {
            // Marquer le drapeau comme collecté
            flag.setCollected(true);
            System.out.println(unit.getName() + " a collecté le drapeau à la position (" + flag.getPosX() + ", " + flag.getPosY() + ")");
            
            // Récupérer l'équipe du collecteur (0 pour l'équipe blanche, 1 pour l'équipe noire)
            int teamIndex = unit.getImageIndex(); // Récupérer l'indice de l'équipe (blanc ou noir)
            
            // Log de l'équipe qui a collecté le drapeau
            if (teamIndex == 0) {
                System.out.println("L'équipe blanche (index 0) a collecté le drapeau.");
            } else if (teamIndex == 1) {
                System.out.println("L'équipe noire (index 1) a collecté le drapeau.");
            }
    
            // Appliquer le bonus à toutes les unités de la même équipe que le collecteur
            for (Unit unitTeam : controller.unitsList) {
                if (unitTeam.getImageIndex() == teamIndex && !unitTeam.hasReceivedBonus()) { // Bonus uniquement pour les unités de la même équipe et qui n'ont pas encore reçu de bonus
                    flag.applyBonus(unitTeam);
                    unitTeam.setHasReceivedBonus(true);  // Marquer cette unité comme ayant reçu le bonus
                }
            }
        } else {
            System.out.println(unit.getName() + " n'est pas assez proche du drapeau pour le collecter.");
        }
    }
    


    // Méthode pour se déplacer aléatoirement
    public void moveRandomly(HelbArmyController controller) {
        // Générer un déplacement aléatoire en X et Y (-1, 0, +1)
        int randomDeltaX = (int) (Math.random() * 3) - 1; // Valeurs possibles : -1, 0, +1
        int randomDeltaY = (int) (Math.random() * 3) - 1;

        // Calculer la nouvelle position
        int newX = this.getPosX() + randomDeltaX;
        int newY = this.getPosY() + randomDeltaY;

        // Vérifier les limites de la carte
        int mapMaxX = HelbArmyController.WIDTH;
        int mapMaxY = HelbArmyController.HEIGHT;
        newX = Math.max(0, Math.min(newX, mapMaxX - 1));
        newY = Math.max(0, Math.min(newY, mapMaxY - 1));

        // Vérifier si la position n'est pas occupée
        if (!controller.isPositionOccupied(newX, newY)) {
            this.moveTo(newX, newY, controller); // Déplacer vers la nouvelle position
        }
    }


    public void takeDamage(int damage) {
        this.health -= damage;
        if (this.health < 0) {
            this.health = 0;
        }
    }

   
    public void setPosition(int x, int y) {
        this.position.x = x;
        this.position.y = y;
        setPosX(x);
        setPosY(y);
    }

    public boolean isAdjacent(GameElement element) {
        return Math.abs(position.x - element.getPosX()) <= 1 && Math.abs(position.y - element.getPosY()) <= 1;
    }

    public void moveAwayFrom(Unit target, HelbArmyController controller) {
        int deltaX = Integer.compare(this.getPosX(), target.getPosX());
        int deltaY = Integer.compare(this.getPosY(), target.getPosY());

        int newX = this.getPosX() + deltaX;
        int newY = this.getPosY() + deltaY;

        int mapMaxX = HelbArmyController.WIDTH;
        int mapMaxY = HelbArmyController.HEIGHT;

        newX = Math.max(0, Math.min(newX, mapMaxX - 1));
        newY = Math.max(0, Math.min(newY, mapMaxY - 1));

        if (!controller.isPositionOccupied(newX, newY)) {
            this.moveTo(newX, newY, controller);
            return;
        }

        int alternativeX = this.getPosX() + deltaX;
        if (alternativeX >= 0 && alternativeX < mapMaxX && !controller.isPositionOccupied(alternativeX, this.getPosY())) {
            this.moveTo(alternativeX, this.getPosY(), controller);
            return;
        }

        int alternativeY = this.getPosY() + deltaY;
        if (alternativeY >= 0 && alternativeY < mapMaxY && !controller.isPositionOccupied(this.getPosX(), alternativeY)) {
            this.moveTo(this.getPosX(), alternativeY, controller);
        }
    }

    public void moveTo(int targetX, int targetY, HelbArmyController controller) {
        // Cherche le drapeau s'il existe et qu'il n'a pas encore été collecté
        
        Flag flag = controller.getFlag();
        if (flag != null && !flag.isCollected()) {
            // Vérification si l'unité est sur la position du drapeau (tolérance de 1 case)
            if (Math.abs(position.x - flag.getPosX()) <= 1 && Math.abs(position.y - flag.getPosY()) <= 1) {
                // L'unité collecte le drapeau seulement quand elle est sur sa position
                collectFlag(this, flag);  // Collecte le drapeau
                controller.removeFlag();  // Enlève le drapeau du jeu une fois collecté
                System.out.println(getName() + " a collecté le drapeau à la position (" + position.x + ", " + position.y + ")");
            } else {
                // Sinon, on continue à se diriger vers le drapeau
                targetX = flag.getPosX();
                targetY = flag.getPosY();
            }
        }
    
        // Calcul du déplacement vers la cible (drapeau ou autre cible)
        int dx = targetX - position.x;
        int dy = targetY - position.y;
    
        if (Math.abs(dx) > Math.abs(dy)) {
            int newX = position.x + (dx > 0 ? 1 : -1);
            if (controller.isValidPosition(newX, position.y, controller)) {
                position.x = newX;
            } else {
                // Si on ne peut pas se déplacer horizontalement, on se déplace verticalement
                if (position.y < HelbArmyController.COLUMN / 2) {
                    position.y++;
                } else {
                    position.y--;
                }
            }
        } else {
            int newY = position.y + (dy > 0 ? 1 : -1);
            if (controller.isValidPosition(position.x, newY, controller)) {
                position.y = newY;
            } else {
                // Si on ne peut pas se déplacer verticalement, on se déplace horizontalement
                int newX = position.x + (dx > 0 ? 1 : -1);
                if (controller.isValidPosition(newX, position.y, controller)) {
                    position.x = newX;
                }
            }
        }
    
        // Met à jour la position de l'unité
        setPosX(position.x);
        setPosY(position.y);
    }
    
    public boolean hasReceivedBonus() {
        return hasReceivedBonus;
    }

    public void setHasReceivedBonus(boolean hasReceivedBonus) {
        this.hasReceivedBonus = hasReceivedBonus;
    }

    public double getHealth() {
        return health;
    }

    public void setHealth(double health) {
        this.health = (int)health;
    }

    public void setImageIndex(int index) {
        this.imageIndex = index;
    }

    public int getImageIndex() {
        return imageIndex;
    }

    public boolean isMovingAutomatically() {
        return isMovingAutomatically;
    }

    // Setter pour activer/désactiver le mouvement automatique
    public void setMovingAutomatically(boolean isMoving) {
        this.isMovingAutomatically = isMoving;
    }

    
    public abstract void moveAutomatically();

    protected abstract String getName();
   
}
