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

//kliens fő osztály
public class CarcassonneClient extends Application {

    private Scene scene; //a megjelenítés tartalmának konténere

    @Override
    public void start(Stage primaryStage) throws URISyntaxException, RemoteException, IOException {
        Group group = new Group();
        scene = new Scene(group);
        scene.getStylesheets().add(this.getClass().getResource("/resources/css/style.css").toExternalForm()); //style hozzáadása

        CommunicationController controller = new CommunicationController(scene);

        primaryStage.setScene(scene); //az ablak létrehozása
        primaryStage.show();
        
        //a képernyő bal felül jelenik meg
        primaryStage.setX(0);
        primaryStage.setY(0);
        
        //a képernyő méretének beállítása
        primaryStage.setWidth(1920);
        primaryStage.setHeight(1032);
        
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() { //Az X-re kattintáskor bezárja az alkalmazást
           @Override public void handle(WindowEvent t) {
               try {
                   controller.quitFromGame(); //A játékos lecsatlakoztatása a szerverről
                   System.exit(0);
               } catch (RemoteException ex) {
                   System.err.println("A játékos szervertől való lecsatlakoztatása sikertelen!");
               }  
           }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
