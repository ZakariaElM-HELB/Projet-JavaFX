public class City extends GameElement {

        static HelbArmyController controller;
        static HelbArmyController spawnUnit;
        public static final int CITY_HEIGHT = 5;
        public static final int CITY_WIDTH = 5;
    
        // public static final int CITY_END_CASE = CITY_HEIGHT - 1;
        public static final int CITY_X = (HelbArmyController.COLUMN / 2) - 3;
        public static final int CITY_NORTH_Y = 0;
        public static final int CITY_SOUTH_Y = HelbArmyController.ROWS - CITY_HEIGHT;
        public static final int CITY_NORTH_DEPOSIT = CITY_NORTH_Y+2;
        public static final int CITY_SOUTH_DEPOSIT = CITY_SOUTH_Y+2;
    
        public static final int SPAWN_UNIT_X = (HelbArmyController.COLUMN / 2) - 1;
        public static final int SPAWN_UNIT_NORTH_Y = CITY_NORTH_Y + CITY_HEIGHT;
        public static final int SPAWN_UNIT_SOUTH_Y = CITY_SOUTH_Y - 1;
    
        public static int storedWoodNorth = 0; // Quantité de bois stockée dans la ville
        public static int storedWoodSouth = 0; // Quantité de bois stockée dans la ville
        private String name;
        private int cityIndex; // Remplace isNorthCity
        private boolean isSpawning = false;
    
        
    
        private static final long DELAY_COLLECTOR = 5000;
        private static final long DELAY_DESERTER = 10000;
        private static final long DELAY_PIKEMAN = 5000;
        private static final long DELAY_KNIGHT = 15000;
    
        private static final int NUMBER_OF_CHOICE = 4;
    
        private static final int CHOICE_COLLECTOR = 0;
        private static final int CHOICE_DESERTER = 1;
        private static final int CHOICE_KNIGHT = 2;
        private static final int CHOICE_PIKEMAN = 3;
    
        private static final int COST_DESERTER = 50;
        private static final int COST_PIKEMAN = 75;
        private static final int COST_KNIGHT = 100;

        private long spawnStartTime = 0; // Temps où le spawn a commencé
        private long spawnDelay = 0;     // Délai en cours pour le spawn
        private String unitInPreparation = null; // Type de l'unité en préparation
        double IMAGE_SIZE = HelbArmyController.SQUARE_SIZE * City.CITY_HEIGHT;

    
        public City(String name, int posX, int posY, int cityIndex) {
            super(posX, posY, cityIndex == 0
                    ? new String[]{"/img/WhiteCampHELBARMY.drawio.png"}
                    : new String[]{"/img/BlackCampHELBARMY.drawio.png"});
            this.name = name;
            this.cityIndex = cityIndex;

        }
    
        public void trySpawn(HelbArmyController controller) {
            spawnUnit = controller;
            long currentTime = System.currentTimeMillis();
        
            // Si une unité est en préparation, vérifier si le délai est écoulé
            if (isSpawning) {
                if (currentTime - spawnStartTime >= spawnDelay) {
                    // System.out.println("[LOG] Unit " + unitInPreparation + " is ready and spawned in city: " + name);
                    spawnUnit(unitInPreparation, controller);
                    isSpawning = false; // Fin de la préparation
                    unitInPreparation = null; // Réinitialisation de l'unité en préparation
                } else {
                    // System.out.println("[LOG] Unit " + unitInPreparation + " is still in preparation for city: " + name);
                }
                return; // Ne pas choisir une autre unité tant que l'actuelle n'est pas prête
            }
        
            // Si aucune unité n'est en préparation, effectuer un choix aléatoire
            int randomChoice = (int) (Math.random() * NUMBER_OF_CHOICE); // Choix aléatoire : 0, 1, 2, 3
            // System.out.println("[LOG] Random choice: " + randomChoice + " for city: " + name);
        
            switch (randomChoice) {
                case CHOICE_COLLECTOR:
                    // System.out.println("[LOG] Chosen unit: Collector. Starting preparation for 5 seconds.");
                    prepareSpawn("Collector", DELAY_COLLECTOR, currentTime);
                    break;
        
                case CHOICE_DESERTER:
                    if (canSpawn(COST_DESERTER)) {
                        // System.out.println("[LOG] Chosen unit: Deserter. Starting preparation for 10 seconds.");
                        prepareSpawn("Deserter", DELAY_DESERTER, currentTime);
                        useWood(COST_DESERTER);
                    } else {
                        // System.out.println("[LOG] Not enough wood to spawn Deserter in city: " + name);
                    }
                    break;
        
                case CHOICE_KNIGHT:
                    if (canSpawn(COST_KNIGHT)) {
                        // System.out.println("[LOG] Chosen unit: Knight. Starting preparation for 15 seconds.");
                        prepareSpawn("Knight", DELAY_KNIGHT, currentTime);
                        useWood(COST_KNIGHT);
                    } else {
                        // System.out.println("[LOG] Not enough wood to spawn Knight in city: " + name);
                    }
                    break;
        
                case CHOICE_PIKEMAN:
                    if (canSpawn(COST_PIKEMAN)) {
                        // System.out.println("[LOG] Chosen unit: Pikeman. Starting preparation for 7 seconds.");
                        prepareSpawn("Pikeman", DELAY_PIKEMAN, currentTime);
                        useWood(COST_PIKEMAN);
                    } else {
                        // System.out.println("[LOG] Not enough wood to spawn Pikeman in city: " + name);
                    }
                    break;
        
                default:
                    throw new IllegalStateException("[LOG] Unexpected random choice: " + randomChoice);
            }
        }

        private void prepareSpawn(String unitType, long delay, long currentTime) {
            this.isSpawning = true;
            this.unitInPreparation = unitType;
            this.spawnStartTime = currentTime;
            this.spawnDelay = delay;
            // System.out.println("[LOG] Starting preparation for unit: " + unitType + " in city: " + name + " (delay: " + delay + " ms).");
        }
        
        
    
        public void spawnUnit(String unitType, HelbArmyController controller) {
            Unit unit;
        
            int posX = SPAWN_UNIT_X;
            int posY = (cityIndex == 0) ? SPAWN_UNIT_NORTH_Y : SPAWN_UNIT_SOUTH_Y;
        
            switch (unitType.toLowerCase()) {
                case "collector":
                    unit = new Collector(posX, posY, controller, cityIndex);
                    break;
                case "deserter":
                    unit = new Deserter(posX, posY, controller, cityIndex);
                    break;
                case "knight":
                    unit = new Knight(posX, posY, controller, cityIndex);
                    break;
                case "pikeman":
                    unit = new Pikeman(posX, posY, controller, cityIndex);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown unit type: " + unitType);
            }
        
            controller.addUnit(unit); // Ajoute l'unité au contrôleur
            isSpawning = false; // Débloque la ville après le spawn
        }
    
    
    
        public boolean isWithinCityBounds(City city, int x, int y) {
            int cityStartX = city.getPosX();
            int cityStartY = city.getPosY();
            int cityEndX = cityStartX + CITY_HEIGHT;
            int cityEndY = cityStartY + CITY_HEIGHT;
    
            return x >= cityStartX && x <= cityEndX && y >= cityStartY && y <= cityEndY;
        }
    
        public boolean canSpawn(int unitCost) {
            return (cityIndex == HelbArmyController.WHITE_PATH_INDEX) ? storedWoodNorth >= unitCost : storedWoodSouth >= unitCost;
        }
        
        public void useWood(int unitCost) {
            if (cityIndex == HelbArmyController.WHITE_PATH_INDEX) storedWoodNorth -= unitCost;
            else storedWoodSouth -= unitCost;
        }
    
        public String getName() {
            return name;
        }
    
        public int getStoredWood() {
            return (cityIndex == HelbArmyController.WHITE_PATH_INDEX) ? storedWoodNorth : storedWoodSouth;
        }
    
        public void addWood(int woodAmount) {
            if (cityIndex == 0) storedWoodNorth += woodAmount;
            else storedWoodSouth += woodAmount;
        }

        public static void resetStoredWood() {
            storedWoodNorth = 0;
            storedWoodSouth = 0;
            System.out.println("[LOG] Stored wood reset: North = " + storedWoodNorth + ", South = " + storedWoodSouth);
        }

        public static HelbArmyController getController() {
            return spawnUnit;
        }

        public void finishSpawn() {
            isSpawning = false;
        }

        @Override
        public double getIMAGE_SIZE() {
            return IMAGE_SIZE;
        }

        
}
