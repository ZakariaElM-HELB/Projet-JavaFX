import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelbArmyMain extends Application {

    @Override
    public void start(Stage primaryStage) {
        HelbArmyController controller = new HelbArmyController(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
