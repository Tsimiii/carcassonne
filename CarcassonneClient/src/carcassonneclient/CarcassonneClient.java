package carcassonneclient;

import controller.Controller;
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
         
            /*File file = new File("fxml_carcassonne_game.fxml");
            String path = file.getAbsolutePath(); System.out.println(path);
        
            FXMLLoader fx = new FXMLLoader();
            fx.setLocation(Paths.get(path).toUri().toURL());
            System.out.println(fx.getLocation());
            fx.load();*/
        
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
