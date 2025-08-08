import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.beans.EventHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class HelbArmyController {

    private static final long FLAG_SPAWN_DELAY = 30 * 1000; // Délai de spawn du drapeau (30 secondes)
    private static final int FRAME_RATE = 130;
    private static final double TREE_RESPAWN_TIME = 30000; // Temps en millisecondes pour la réapparition d'un arbre
    
    private long lastFlagSpawnTime = -1;  // Temps du dernier spawn du drapeau
    private int lastSpawnedCityIndex = WHITE_PATH_INDEX;
    private double treeSpawnPercentage = 0.05;  // 5% de chance d'apparition des arbres globalement

    private HelbArmyView view;
    private Random random = new Random();
    private double[][] treeSpawnRatios = new double[ROWS][COLUMN];  // Tableau pour stocker les ratios d'apparition
    private long startTime; // Temps de démarrage du jeu
    private Timeline gameLoop;

    static HelbArmyMain main;
    static City cityToSpawnFrom ;
    static City city;


    public static final int WIDTH = 800;
    public static final int HEIGHT = WIDTH;
    public static final int ROWS = 20;
    public static final int COLUMN = ROWS;
    public static final int SQUARE_SIZE = WIDTH / ROWS;

    public static final int VOID_PATH_INDEX = -1;
    public static final int WHITE_PATH_INDEX = 0;
    public static final int BLACK_PATH_INDEX = 1;

    public static boolean secretOk = false;

    public List<GameElement> gameElementsList = new ArrayList<>();
    public List<Unit> unitsList = new ArrayList<>();
    public List<Tree> treesList = new ArrayList<>();
    public List<City> citiesList = new ArrayList<>();
    public List<Stone> stonesList = new ArrayList<>();
    public List<GameElement> gameElementsListDie = new ArrayList<>();
    public List<Unit> unitsListDie = new ArrayList<>();

    public Flag flag;

    public int voidx = -2;
    public int voidy = -2;

  

    public HelbArmyController(Stage stage) {
        view = new HelbArmyView(stage);
        initializeGame();
    }


    private void initializeGame() {


        // Initialisation des villes
        spawnCity();

        // Initialisation des ratios d'apparition des arbres
        initializeTreeSpawnRatios();

        // Enregistrer l'heure de début
        startTime = System.currentTimeMillis();

        // Appel initial pour spawn des arbres
        spawnNewTree();

        for (int i = 0; i < 2; i++) {
            // Spawn la pierre philosophale si nécessaire
            spawnStone(); // Appelez cette méthode pour spawn une pierre régulièrement ou selon une condition
        }
        
        startGame();
        // Ajoute un écouteur de clavier à votre scène (par exemple, la scène principale du jeu)

        view.scene.setOnKeyPressed(event -> {
            KeyCode code = event.getCode();
            if (code == KeyCode.A) {
                cityToSpawnFrom = citiesList.get(0);
                cityToSpawnFrom.spawnUnit("Collector",this);
            } else if (code == KeyCode.Z) {
                // Génère un déserteur dans la ville nord
                cityToSpawnFrom = citiesList.get(0);
                cityToSpawnFrom.spawnUnit("Deserter",this);
            } else if (code == KeyCode.E) {
                // Génère un cavalier dans la ville nord
                cityToSpawnFrom = citiesList.get(0);
                cityToSpawnFrom.spawnUnit("Knight",this);
            } else if (code == KeyCode.R) {
                // Génère un piquier dans la ville nord
                cityToSpawnFrom = citiesList.get(0);
                cityToSpawnFrom.spawnUnit("Pikeman",this);
            } else if (code == KeyCode.W) {
                // Génère un collecteur dans la ville sud
                cityToSpawnFrom = citiesList.get(1);
                cityToSpawnFrom.spawnUnit("Collector",this);
            } else if (code == KeyCode.X) {
                // Génère un déserteur dans la ville sud
                cityToSpawnFrom = citiesList.get(1);
                cityToSpawnFrom.spawnUnit("Deserter",this);
            } else if (code == KeyCode.C) {
                // Génère un cavalier dans la ville sud
                cityToSpawnFrom = citiesList.get(1);
                cityToSpawnFrom.spawnUnit("Knight",this);
            } else if (code == KeyCode.V) {
                // Génère un piquier dans la ville sud
                cityToSpawnFrom = citiesList.get(1);
                cityToSpawnFrom.spawnUnit("Pikeman",this);
            } else if (code == KeyCode.J) {
                // Stoppe/active le déplacement de tous les collecteurs
                toggleAutomaticMovement("Collector");
                System.out.println("Stop/Activation du déplacement des collecteurs.");
            } else if (code == KeyCode.K) {
                // Stoppe/active le déplacement de tous les déserteurs
                toggleAutomaticMovement("Deserter");
                System.out.println("Stop/Activation du déplacement des déserteurs.");
            } else if (code == KeyCode.L) {
                // Stoppe/active le déplacement de tous les cavaliers
                toggleAutomaticMovement("Knight");
                System.out.println("Stop/Activation du déplacement des cavaliers.");
            } else if (code == KeyCode.M) {
                // Stoppe/active le déplacement de tous les piquiers
                toggleAutomaticMovement("Pikeman");
                System.out.println("Stop/Activation du déplacement des piquiers.");
            } else if (code == KeyCode.U) {
                // Met instantanément à 10 les PV de toutes les unités présentes sur la carte
                setUnitDie();  
            } else if (code == KeyCode.I) {
                //Faire spawn un flag
                spawnFlag();
            } else if (code == KeyCode.O) {
                // Reset de la simulation
                resetGame();
            } else if (code == KeyCode.P) {
                spawnStone(); // Appelez cette méthode pour spawn une pierre régulièrement ou selon une condition
            } else if (code == KeyCode.T) {               
                secretOk = true;
            }
        });   
    }

    public void updateView() {
        view.drawBackground();
        view.drawElements(gameElementsList);
        view.secret();
    }
    
    // Réinitialise le jeu (exemple)
    public void resetGame() {
        unitsList.clear();
        gameElementsList.clear();
        citiesList.clear();
        treesList.clear();
        
        City.resetStoredWood();
        // Réinitialisation des éléments de jeu, comme les unités, villes, etc.
        gameLoop.stop();
        System.out.println("Jeu réinitialisé.");
        initializeGame();
    }

    public void startGame() {
        gameLoop = new Timeline(new KeyFrame(Duration.millis(FRAME_RATE), e -> {
            checkTreeRespawns(); // Vérifie les arbres morts et les réapparaît si nécessaire
            trySpawnUnits();
    
            //permet d'éviter les concurrents modifications
            processRemovals();

            // Vérifier si le délai de 30 secondes est écoulé pour le spawn du drapeau
            long currentTime = System.currentTimeMillis();
            if (currentTime - startTime >= FLAG_SPAWN_DELAY) {
                 spawnFlag();
            }

            // Vérifier les collisions des unités avec la pierre
            checkStoneCollisions(); 
        
            for (Unit unit : unitsList) {
                unit.moveAutomatically();  // Assurez-vous que chaque unité se déplace correctement
            }
    
            updateView();
        }));
    
        gameLoop.setCycleCount(Timeline.INDEFINITE);
        gameLoop.play();
    }

        /**
             ************************************* PARTIE MOUVEMENT *************************************
                                                                                                        **/


    public void toggleAutomaticMovementForUnit(Unit unit) {
        unit.setMovingAutomatically(!unit.isMovingAutomatically());
        System.out.println(unit.getClass().getSimpleName() + " movement automatically " + (unit.isMovingAutomatically() ? "enabled" : "disabled"));
    }


    public void toggleAutomaticMovement(String unitName) {
        for (Unit unit : unitsList) {
            // Vérifie le type d'unité et ajuste son état
            switch (unitName.toLowerCase()) {
                case "collector":
                    if (unit instanceof Collector) {
                        unit.setMovingAutomatically(!unit.isMovingAutomatically());
                        System.out.println("Collector movement automatically " + (unit.isMovingAutomatically() ? "enabled" : "disabled"));
                    }
                    break;
                case "deserter":
                    if (unit instanceof Deserter) {
                        unit.setMovingAutomatically(!unit.isMovingAutomatically());
                        System.out.println("Deserter movement automatically " + (unit.isMovingAutomatically() ? "enabled" : "disabled"));
                    }
                    break;
                case "knight":
                    if (unit instanceof Knight) {
                        unit.setMovingAutomatically(!unit.isMovingAutomatically());
                        System.out.println("Knight movement automatically " + (unit.isMovingAutomatically() ? "enabled" : "disabled"));
                    }
                    break;
                case "pikeman":
                    if (unit instanceof Pikeman) {
                        unit.setMovingAutomatically(!unit.isMovingAutomatically());
                        System.out.println("Pikeman movement automatically " + (unit.isMovingAutomatically() ? "enabled" : "disabled"));
                    }
                    break;
                default:
                    System.out.println("Unknown unit type: " + unitName);
                    break;
            }
        }
    }
    
    
        /**
             *********************** PARTIE AJOUTS DES AJOUTS / SPAWN *************************************
                                                                                                            **/


    private void spawnCity() {
        City northCity = new City("North City", City.CITY_X, City.CITY_NORTH_Y, WHITE_PATH_INDEX);
        citiesList.add(northCity);
        gameElementsList.add(northCity);

        City southCity = new City("South City", City.CITY_X, City.CITY_SOUTH_Y, BLACK_PATH_INDEX);
        citiesList.add(southCity);
        gameElementsList.add(southCity);
    }                                                                                                        

    public void addUnit(Unit unit) {
        unitsList.add(unit);
        gameElementsList.add(unit);
    }

    public void trySpawnUnits() {
        cityToSpawnFrom = (lastSpawnedCityIndex == WHITE_PATH_INDEX) ? citiesList.get(WHITE_PATH_INDEX) : citiesList.get(BLACK_PATH_INDEX);
        cityToSpawnFrom.trySpawn(this);
        lastSpawnedCityIndex = (lastSpawnedCityIndex == WHITE_PATH_INDEX) ? BLACK_PATH_INDEX : WHITE_PATH_INDEX;
    }

    private void spawnNewTree() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLUMN; j++) {
                if (!isPositionOccupied(i, j) && random.nextDouble() < treeSpawnRatios[i][j]) {
                    Tree newTree = new Tree(i, j);
                    treesList.add(newTree);
                    gameElementsList.add(newTree);
                }
            }
        }
    }

    public void spawnStone() {
        int stoneX, stoneY;
        do {
            stoneX = random.nextInt(ROWS);
            stoneY = random.nextInt(COLUMN);
        } while (isPositionOccupied(stoneX, stoneY));  // Vérifier si la position est libre

        Stone newStone = new Stone(stoneX, stoneY);
        stonesList.add(newStone);  // Ajouter la pierre à la liste des éléments du jeu
        gameElementsList.add(newStone);  // Ajouter la pierre à la scène

        // System.out.println("Une nouvelle Pierre Philosophale est apparue à la position (" + stoneX + ", " + stoneY + ")");
    }

    public void spawnFlag() {
        // Si un drapeau est déjà présent, on ne spawn pas un nouveau
        if (getFlag() != null && !getFlag().isCollected()) {
            return;
        }
    
        // Générer une position aléatoire valide pour le drapeau
        int flagX, flagY;
        do {
            flagX = random.nextInt(ROWS);
            flagY = random.nextInt(COLUMN);
        } while (isPositionOccupied(flagX, flagY)); // Vérifie si la position est occupée ou dans une ville
    
        // Créer un nouveau drapeau
        Flag newFlag = new Flag(flagX, flagY);
        gameElementsList.add(newFlag);  // Ajouter le drapeau à la liste des éléments du jeu
        this.flag = newFlag;  // Référence au drapeau actuel
    
        System.out.println("Un nouveau drapeau est apparu à la position (" + flagX + ", " + flagY + ")");
    }
    
         /**
             *********************** PARTIE VERIFICATIONS DES ELEMENTS *************************************
                                                                                                        **/

    private void initializeTreeSpawnRatios() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLUMN; j++) {
                treeSpawnRatios[i][j] = treeSpawnPercentage;  // Applique le pourcentage global à chaque case
            }
        }
    }

    public void checkTreeRespawns() {
        long currentTime = System.currentTimeMillis();

        for (Tree tree : treesList) {
            if (tree.isDead() && tree.getDeathTime() > 0) { // Vérifie si l'arbre est mort
                if (currentTime - tree.getDeathTime() >= TREE_RESPAWN_TIME) { // Si le délai de 5 secondes est écoulé
                    tree.respawn(); // Réapparaît
                    gameElementsList.add(tree); // Réajoute l'arbre à la scène
                    // System.out.println("L'arbre a réapparu après 30 secondes !");
                }
            }
        }
    }

    public boolean isValidPosition(int x, int y, HelbArmyController controller) {
        if (x < 0 || y < 0 || x >= ROWS || y >= COLUMN) return false;
        return !isPositionOccupied(x, y);
    }

    public boolean isPositionOccupied(int x, int y) {
        for (City city : citiesList) {
            if(city.isWithinCityBounds(city, x, y)){
                return true;
            }
        }
        
        for (Stone stone : stonesList) {
            if (stone.getPosX() == x && stone.getPosY() == y) {
                return false;
            }
        }
        for (GameElement element : gameElementsList) {
            if (element.getPosX() == x && element.getPosY() == y) {
                return true;
            }
        }

        return false;
    }

     // Méthode pour vérifier la collision entre les unités et la Pierre Philosophale
     public void checkStoneCollisions() {
        for (Unit unit : unitsList) { // Copie pour éviter ConcurrentModificationException
            for (Stone stone : stonesList) { // Idem pour les pierres
                if (unit.getPosX() == stone.getPosX() && unit.getPosY() == stone.getPosY() && !stone.isCollected()) {
                    stone.handleStoneEffect(unit, stone); // Applique l'effet de la pierre
                    
                    if (unit.getHealth() <= 0) {
                        removeUnit(unit); // Supprime l'unité si elle est morte
                    }
    
                    stone.setCollected(true); // Marque la pierre comme collectée
                    stonesList.remove(stone); // Supprime la pierre collectée
                    gameElementsList.remove(stone);
                    break; // Sort de la boucle après collision
                }
            }
        }
    }
        /**
             *********************** PARTIE SUPPRESSION DES ENTITE *************************************
                                                                                                        **/
    public void removeFlag() {
        gameElementsList.remove(flag);
        startTime = System.currentTimeMillis();
    }

    public void removeUnit(Unit unit) {
        if (unit == null) {
            System.out.println("L'unité à supprimer est nulle.");
            return;
        }
    
        // Appeler la méthode spécifique à Pikeman si l'unité est de ce type
        if (unit instanceof Pikeman) {
            ((Pikeman) unit).onRemoved();
        }
    
        // Marquer l'unité pour suppression
        unitsListDie.add(unit);
        gameElementsListDie.add(unit);

        // Mets à jour la vue après suppression
        updateView();
    }

    public void processRemovals() {
        // Supprimer les unités marquées dans unitsList
        unitsList.removeAll(unitsListDie);
        // Supprimer les éléments marqués dans gameElementsList
        gameElementsList.removeAll(gameElementsListDie);
        // Vider les listes de suppression
        unitsListDie.clear();
        gameElementsListDie.clear();
    }

    public void removeTree(Tree tree) {
        if (tree.isDead()) {
            // System.out.println("L'arbre est marqué comme mort et va réapparaître dans 30 secondes.");
            tree.markAsDead();

            // Retirer temporairement l'arbre de la liste
            gameElementsList.remove(tree);
        }
    }

    public void setUnitDie(){
        for (Unit unit : unitsList) {
            unit.setHealth(0);
            removeUnit(unit);
        }   
    }

    /**
             *********************** PARTIE GETTER / SETTER *************************************
                                                                                                        **/


    public Flag getFlag() {
        for (GameElement element : gameElementsList) {
            if (element instanceof Flag) {
                return (Flag) element;  // Retourne le drapeau s'il est présent
            }
        }
        return null;  // Aucun drapeau n'est présent
    }
}
