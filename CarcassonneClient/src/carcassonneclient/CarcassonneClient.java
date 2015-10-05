package carcassonneclient;

import controller.CommunicationController;
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

        /*URL resource = this.getClass().getResource("/main/resources/fxml_carcassonne_menu.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent fxmlContent = fxmlLoader.load(resource.openStream());*/

        Group group = new Group();
        scene = new Scene(group, 700, 700);
        scene.getStylesheets().add(this.getClass().getResource("/resources/css/style.css").toExternalForm());
        //scene.setRoot(fxmlContent);
        
        /*FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/resources/fxml_carcassonne_menu.fxml"));
        scene.setRoot(loader.load());*/

       CommunicationController controller = new CommunicationController(scene);
        
       Screen screen = Screen.getPrimary();
        
        

        primaryStage.setScene(scene);
        // primaryStage.setFullScreen(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
