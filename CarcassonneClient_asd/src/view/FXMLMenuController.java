/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import carcassonneshared.RemoteObserver;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 *
 * @author Timea
 */
public class FXMLMenuController implements Initializable {

    @FXML protected TextField nameTextField;
    @FXML protected Label errorNameText;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    } 
    
    @FXML private void joinGame(ActionEvent event) {
        if(nameTextField.getText().equals("")) {
            errorNameText.setText("Nem adtál meg nevet!");
        } else {
           // delegate.clickJoinGame(nameTextField.getText());
        }
    }

    /*@Override
    public void update(Object observable, Object updateMsg) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }*/
    
}
