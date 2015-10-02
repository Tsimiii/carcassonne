package carcassonneclient;

import carcassonneshared.RmiService;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.scene.Scene;
import javafx.stage.Stage;

import controller.Controller;
import java.net.URISyntaxException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import javafx.scene.Group;
import javafx.stage.Screen;

public class CarcassonneClient extends Application {
        
    private Scene scene;
    
    @Override
    public void start(Stage primaryStage) throws URISyntaxException, RemoteException {
         
        //System.out.println(getClass().getClassLoader().getResource("main/resources/fxml_carcassonne_game.fxml"));
        Group group = new Group();
        scene = new Scene(group, 700, 700);
        scene.getStylesheets().add(this.getClass().getResource("/resources/css/style.css").toExternalForm());
        

       Controller controller = new Controller(scene);
        
        Screen screen = Screen.getPrimary();

        primaryStage.setScene(scene);
       // primaryStage.setFullScreen(true);
        primaryStage.show(); 
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
