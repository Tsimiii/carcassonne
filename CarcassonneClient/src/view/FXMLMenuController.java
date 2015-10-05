/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import carcassonneshared.RemoteObserver;
import carcassonneshared.RmiService;
import controller.CommunicationController;
import controller.CommunicationController;
import java.io.IOException;
import java.net.URL;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

public class FXMLMenuController implements Initializable {

    @FXML protected Pane content;
    @FXML protected BorderPane borderPane;
    @FXML protected TextField nameTextField;
    @FXML protected Label errorNameText;
    
    public CommunicationController delegate;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    } 
    
    @FXML private void joinGame(ActionEvent event) {
        delegate.clickJoinGame(nameTextField.getText());
           /* try {
                RmiService remoteService = (RmiService) Naming.lookup("//localhost:8080/carcassonneServer");
                remoteService.addObserver(this);
                displayLoadingScreen();
            } catch (Exception ex) {
                ex.printStackTrace();
            }*/
       // }
    }

    /*@Override
    public void update(Object observable, Object updateMsg) throws RemoteException {
        if(updateMsg.equals("startgame")) {
            startGame();
        }
    }
    
    private void startGame() {
        try {
            
            URL resource = this.getClass().getResource("/main/resources/fxml_carcassonne_game.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader();
            Parent fxmlContent = fxmlLoader.load(resource.openStream());
            borderPane.getChildren().clear();
            borderPane.getChildren().add(fxmlContent);
        } catch (IOException ex) {
            System.err.println("Nem sikerült betölteni az fxml-t!");
        }
    }*/
    
}
