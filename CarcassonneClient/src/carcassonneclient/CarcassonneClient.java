package carcassonneclient;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.scene.Scene;
import javafx.stage.Stage;

import controller.Controller;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.Group;
import javafx.stage.Screen;

public class CarcassonneClient extends Application {
        
    private Scene scene;
    private Controller controller;
    
    @Override
    public void start(Stage primaryStage) {
        Group group = new Group();
        scene = new Scene(group, 700, 700);
        scene.getStylesheets().add(this.getClass().getResource("/resources/css/style.css").toExternalForm());
        controller = new Controller(scene); 
        
        Screen screen = Screen.getPrimary();

        primaryStage.setScene(scene); 
        //primaryStage.setFullScreen(true);
        primaryStage.show(); 
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
