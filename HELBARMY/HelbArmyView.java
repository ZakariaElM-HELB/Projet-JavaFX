import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.List;

public class HelbArmyView {

    private Canvas canvas;
    private GraphicsContext gc;
    public Scene scene;

    public HelbArmyView(Stage primaryStage) {
        Group root = new Group();
        canvas = new Canvas(HelbArmyController.WIDTH, HelbArmyController.WIDTH);
        root.getChildren().add(canvas);
        scene = new Scene(root);

        primaryStage.setTitle("Helb Army Game");
        primaryStage.setScene(scene);
        primaryStage.show();

        gc = canvas.getGraphicsContext2D();
    }

    public void drawBackground() {

        for (int i = 0; i < HelbArmyController.ROWS; i++) {
            for (int j = 0; j < HelbArmyController.COLUMN; j++) {
                gc.setFill((i + j) % 2 == 0 ? Color.web("AAD751") : Color.web("A2D149"));
                gc.fillRect(i * HelbArmyController.SQUARE_SIZE, j * HelbArmyController.SQUARE_SIZE, HelbArmyController.SQUARE_SIZE, HelbArmyController.SQUARE_SIZE);
            }
        }
    }
    
    public void drawElements(List<GameElement> elements) {
        for (GameElement element : elements) {
                gc.drawImage(new Image(getClass().getResource(element.getPathToImage()).toString()),
                element.getPosX() * HelbArmyController.SQUARE_SIZE,
                element.getPosY() * HelbArmyController.SQUARE_SIZE,
                element.getIMAGE_SIZE(), element.getIMAGE_SIZE());
            
        }
    }

    public void secret(){
        if(HelbArmyController.secretOk == true){
            gc.drawImage(new Image("img/secret.jpeg"),0 * HelbArmyController.SQUARE_SIZE,
            0 * HelbArmyController.SQUARE_SIZE,
            800, 800);
        }      
    }

}
