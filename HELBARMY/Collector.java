public class Collector extends Unit {

    private static final int HEALTH = 150;
    private static final int  DAMAGE = 5;
    
    private static final int WOOD_COLLECTION_GOAL = 25; // Objectif de collecte
    
    public int collectedWood = 0; // Quantité de bois collectée

    private int selectedImageIndex = 0; // Image par défaut

    public Collector(int posX, int posY, HelbArmyController controller, int imageIndex) {
        super(posX, posY, HEALTH, DAMAGE, new String[]{
            "/img/WhiteCollecteurHELBARMY.drawio.png",
            "/img/BlackCollecteurHELBARMY.drawio.png"
        });
        this.controller = controller;
        setImageIndex(imageIndex);  // Définit l'index de l'image
    }

    public void setImageIndex(int index) {
        if (index >= 0 && index < getPathToImageLen()) {
            selectedImageIndex = index;
        }
    }

    public String getPathToImage() {
        return getPathToImage(selectedImageIndex);
    }

    public int getImageIndex() {
        return selectedImageIndex;
    }

    public void moveAutomatically() {
        if (!isMovingAutomatically()) {
            return;  // Si le mouvement automatique est désactivé, ne rien faire
        }
        if (collectedWood >= WOOD_COLLECTION_GOAL) {
            // Retour à la base
            int collectX = City.CITY_X - 1;
            int collectY = (selectedImageIndex == HelbArmyController.WHITE_PATH_INDEX)
                ? City.CITY_NORTH_DEPOSIT
                : City.CITY_SOUTH_DEPOSIT;
    
            if (position.x == collectX && position.y == collectY) {
                // Dépose le bois à la base
                City targetCity = (selectedImageIndex == HelbArmyController.WHITE_PATH_INDEX)
                    ? controller.citiesList.get(HelbArmyController.WHITE_PATH_INDEX) // Ville nord
                    : controller.citiesList.get(HelbArmyController.BLACK_PATH_INDEX); // Ville sud
                depositWood(targetCity);
            } else {
                moveTo(collectX, collectY, controller);
            }
        } else {
            // Recherche et interaction avec les arbres
            Tree nearestTree = findNearestTree();
            if (nearestTree.isDead()) { // Si aucun arbre valide trouvé
                return; 
            }
    
            if (isAdjacent(nearestTree)) {
                attackTree(nearestTree);
            } else {
                moveTo(nearestTree.getPosX(), nearestTree.getPosY(), controller);
            }
        }
    }
    
    private Tree findNearestTree() {
        Tree sentryTree = new Tree(-1, -1); // Objet sentinelle par défaut
        int minDistance = Integer.MAX_VALUE;
    
        for (Tree tree : controller.treesList) {
            if (tree.isDead()) {
                continue; // Ignore les arbres morts
            }
            int distance = Math.abs(position.x - tree.getPosX()) + Math.abs(position.y - tree.getPosY());
            if (distance < minDistance) {
                minDistance = distance;
                sentryTree = tree;
            }
        }
    
        return sentryTree;
    }


    private void depositWood(City city) {
        city.addWood(collectedWood);
        collectedWood = 0;
    }

    public void attackTree(Tree tree) {
        if (tree == null || tree.isDead()) {
            // System.out.println("L'arbre est mort ou invalide. Le collector ne peut pas attaquer.");
            return; // Arrête l'attaque si l'arbre est mort ou invalide
        }
    
        int woodGathered = Math.min(damage, tree.getHealthPoints());
        addWood(woodGathered);
        tree.takeDamage(damage);
    
        if (tree.getHealthPoints() == 0) {
            controller.removeTree(tree);
        }
    }

    public void addWood(int woodAmount) {
        collectedWood += woodAmount;
    }

    @Override
    protected String getName() {
        return "collector";
    }
}
