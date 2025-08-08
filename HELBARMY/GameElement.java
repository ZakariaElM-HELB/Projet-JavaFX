public abstract class GameElement {

    private int posX;
    private int posY;
    private String[] IMAGE_PATHS;
    private double IMAGE_SIZE = HelbArmyController.SQUARE_SIZE;;

    public GameElement(int posX, int posY, String[] imagePaths) {
        this.posX = posX;
        this.posY = posY;
        this.IMAGE_PATHS = imagePaths;
    }

    public int getPosX() {
        return posX;
    }

    public void setPosX(int newPosX) {
        this.posX = newPosX;
    }

    public int getPosY() {
        return posY;
    }

    public void setPosY(int newPosY) {
        this.posY = newPosY;
    }

    public String getPathToImage() {
        return IMAGE_PATHS[0];
    }

    public String getPathToImage(int index) {
        return IMAGE_PATHS[index];
    }

    public int getPathToImageLen() {
        return IMAGE_PATHS.length;
    }

    public double getIMAGE_SIZE() {
        return IMAGE_SIZE;
    }
    // public abstract void triggerAction(HelbArmyMain gameBoard);
}
