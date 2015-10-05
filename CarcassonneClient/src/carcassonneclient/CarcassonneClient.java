package carcassonneclient;

import controller.CommunicationController;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import javafx.scene.Group;
import javafx.stage.Screen;

public class CarcassonneClient extends Application {

    private Scene scene;

    @Override
    public void start(Stage primaryStage) throws URISyntaxException, RemoteException, IOException {

        Group group = new Group();
        scene = new Scene(group, 2000, 1000);
        scene.getStylesheets().add(this.getClass().getResource("/resources/css/style.css").toExternalForm());

       CommunicationController controller = new CommunicationController(scene);
        
       Screen screen = Screen.getPrimary();
       
        primaryStage.setScene(scene);
        primaryStage.setFullScreen(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
