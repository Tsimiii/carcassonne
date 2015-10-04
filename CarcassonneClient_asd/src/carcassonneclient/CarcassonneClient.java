package carcassonneclient;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.RemoteException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.stage.Screen;

public class CarcassonneClient extends Application {
        
    private Scene scene;
    
    @Override
    public void start(Stage primaryStage) throws URISyntaxException, RemoteException, IOException {
        
        URL resource = this.getClass().getResource("/main/resources/fxml_carcassonne_game.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent fxmlContent = fxmlLoader.load(resource.openStream());
        
        Group group = new Group();
        scene = new Scene(group, 1154, 768); //átírni majd
        scene.getStylesheets().add(this.getClass().getResource("/resources/css/style.css").toExternalForm());
        //Controller controller = new Controller(scene);
       // scene.setRoot(FXMLLoader.load(getClass().getClassLoader().getResource("/main/resources/carcassonne_game.fxml")));
        
       // scene.setRoot(fxmlContent);
        scene.setRoot(fxmlContent);
        Screen screen = Screen.getPrimary();

        primaryStage.setScene(scene);
       // primaryStage.setFullScreen(true);
        primaryStage.show(); 
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
