/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import controller.CommunicationController;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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
    }    
}
