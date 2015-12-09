package carcassonneclient;

import controller.CommunicationController;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.stage.Screen;
import javafx.stage.WindowEvent;

public class CarcassonneClient extends Application {

    private Scene scene;

    @Override
    public void start(Stage primaryStage) throws URISyntaxException, RemoteException, IOException {
        Group group = new Group();
        scene = new Scene(group);
        scene.getStylesheets().add(this.getClass().getResource("/resources/css/style.css").toExternalForm());

        CommunicationController controller = new CommunicationController(scene);

        primaryStage.setScene(scene);
        primaryStage.show();
        
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();
        System.out.println(bounds.getMinX() + " " + bounds.getMinY() + " " + bounds.getWidth() + " " + bounds.getHeight());
        primaryStage.setX(0);
        primaryStage.setY(0);
        primaryStage.setWidth(1920);
        primaryStage.setHeight(1032);
        
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
           @Override public void handle(WindowEvent t) {
               try {
                   controller.quitFromGame();
               } catch (RemoteException ex) {
                   Logger.getLogger(CarcassonneClient.class.getName()).log(Level.SEVERE, null, ex);
               }
               System.exit(0);
           }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
