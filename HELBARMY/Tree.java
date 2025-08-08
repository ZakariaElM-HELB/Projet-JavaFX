public class Tree extends GameElement {

    private static final int RESTOR_FULL_HEALTH = 100;
    public int healthPoints = RESTOR_FULL_HEALTH;  // Points de vie de l'arbre
    private long deathTime = -1; // Stocke le temps de mort en millisecondes
    private boolean isDead;  // Statut de l'arbre (mort ou vivant)
    boolean respawning;  // Indique si l'arbre est en cours de réapparition
    double IMAGE_SIZE = HelbArmyController.SQUARE_SIZE;

    // Constructeur de l'arbre
    public Tree(int posX, int posY) {
        super(posX, posY, new String[]{"/img/Tree_MidHealth_HELBARMY.drawio.png"}); // Chemin de l'image
        this.isDead = false;  // Initialement, l'arbre est vivant
        this.respawning = false;  // Initialement, l'arbre n'est pas en cours de réapparition
    }

    // Getter pour les points de vie
    public int getHealthPoints() {
        return healthPoints;
    }

    // Méthode pour infliger des dégâts à l'arbre
    public void takeDamage(int damage) {
        this.healthPoints -= damage;
        if (this.healthPoints <= 0) {
            this.healthPoints = 0; // Les points de vie ne peuvent pas être négatifs
            this.isDead = true; // L'arbre est maintenant mort
        }
    }

    // Méthode pour vérifier si l'arbre est mort
    public boolean isDead() {
        return this.isDead;
    }

    public void markAsDead() {
        this.deathTime = System.currentTimeMillis(); // Enregistre l'heure de mort
        this.isDead = true;
    }

    public long getDeathTime() {
        return this.deathTime;
    }

    public void respawn() {
        this.healthPoints = RESTOR_FULL_HEALTH; // Réinitialise les PV
        this.isDead = false;
        this.deathTime = -1; // Réinitialise le temps de mort
        System.out.println("L'arbre a réapparu !");
    }
}
