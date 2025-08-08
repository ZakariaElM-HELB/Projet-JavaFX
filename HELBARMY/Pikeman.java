public class Pikeman extends Unit {

    private int[] assignedPosition; // Position assignée
    private Unit currentTarget; // Cible actuelle
    private int selectedImageIndex; // Image de l'unité (blanc ou noir)

    // Vision collective des équipes
    private static int whiteVisionTotal = 0;
    private static int blackVisionTotal = 0;

    private static final int HEALTH = 175;
    private static final int DAMAGE = 15;

    private HelbArmyController controller;

    static {
        // Enregistrement des bonus spécifiques au Piquier
        registerBonus("Pikeman", "Knight", 3.0); // Piquier > Cavalier
    }

    public Pikeman(int posX, int posY, HelbArmyController controller, int imageIndex) {
        super(posX, posY, HEALTH, DAMAGE, new String[]{
                "/img/WhitePiquierHELBARMY.drawio.png",
                "/img/BlackPiquierHELBARMY.drawio.png"
        });
        this.controller = controller;
        this.selectedImageIndex = imageIndex;

        // Mise à jour de la vision collective
        updateVisionOnSpawn();

        // Génération d'une position assignée aléatoire
        this.assignedPosition = new int[]{
                (int) (Math.random() * HelbArmyController.ROWS),
                (int) (Math.random() * HelbArmyController.COLUMN)
        };
    }

    private void updateVisionOnSpawn() {
        if (selectedImageIndex == 0) {
            whiteVisionTotal += 1; // Ajout à la vision collective des piquiers blancs
        } else {
            blackVisionTotal += 1; // Ajout à la vision collective des piquiers noirs
        }
    }

    public void onRemoved() {
        if (selectedImageIndex == 0) {
            whiteVisionTotal -= 1; // Réduction de la vision collective des piquiers blancs
        } else {
            blackVisionTotal -= 1; // Réduction de la vision collective des piquiers noirs
        }
    }

    private int getTeamVision() {
        return selectedImageIndex == 0 ? whiteVisionTotal : blackVisionTotal;
    }

    @Override
    public void moveAutomatically() {
        if (!isMovingAutomatically()) {
            return;
        }

        int visionRange = getTeamVision();

        // Rechercher un ennemi dans le champ de vision
        if (currentTarget == null || !isWithinRange(currentTarget, visionRange)) {
            currentTarget = findEnemyInVision(visionRange);
        }

        // Si une cible est trouvée, vérifier si elle est adjacente et l'attaquer
        if (currentTarget != null) {
            if (isAdjacent(currentTarget)) {
                int totalDamage = calculateDamage(currentTarget);
                currentTarget.takeDamage(totalDamage);

                if (currentTarget.getHealth() == 0) {
                    // La cible est morte, la retirer du jeu
                    currentTarget.setPosX(controller.voidx);
                    currentTarget.setPosY(controller.voidy);
                    System.out.println(currentTarget.getName() + " is dead. Removing from units list.");
                    controller.removeUnit(currentTarget);
                }
            } else {
                // Si non adjacent, se déplacer vers la cible
                moveTowards(currentTarget.getPosX(), currentTarget.getPosY());
            }
        } else {
            // Sinon, retourner à la position assignée
            moveTowards(assignedPosition[0], assignedPosition[1]);
        }
    }

    private Unit findEnemyInVision(int range) {
        for (GameElement element : controller.gameElementsList) {
            if (element instanceof Unit) {
                Unit unit = (Unit) element;

                // Vérifier si l'unité est ennemie et dans le champ de vision
                if (!unit.getPathToImage().equals(this.getPathToImage()) && isWithinRange(unit, range)) {
                    return unit;
                }
            }
        }
        return null; // Aucun ennemi trouvé
    }

    private boolean isAdjacent(Unit target) {
        int dx = Math.abs(this.getPosX() - target.getPosX());
        int dy = Math.abs(this.getPosY() - target.getPosY());
        return dx <= 1 && dy <= 1; // Vérifier si la cible est adjacente (y compris diagonales)
    }

    private boolean isWithinRange(Unit unit, int range) {
        int dx = this.getPosX() - unit.getPosX();
        int dy = this.getPosY() - unit.getPosY();
        return (dx * dx + dy * dy) <= (range * range);
    }

    private void moveTowards(int targetX, int targetY) {
        int dx = Integer.compare(targetX, this.getPosX());
        int dy = Integer.compare(targetY, this.getPosY());

        // Déplacement uniquement si la position est valide
        if (controller.isValidPosition(this.getPosX() + dx, this.getPosY() + dy, controller)) {
            this.setPosX(this.getPosX() + dx);
            this.setPosY(this.getPosY() + dy);
        }
    }

    public void setSelectedImageIndex(int selectedImageIndex) {
        this.selectedImageIndex = selectedImageIndex;
    }

    public String getPathToImage() {
        return getPathToImage(selectedImageIndex);
    }

    @Override
    protected String getName() {
        return "Pikeman";
    }

    public int[] getAssignedPosition() {
        return assignedPosition;
    }
}
