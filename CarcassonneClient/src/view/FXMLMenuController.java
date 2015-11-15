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
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class FXMLMenuController implements Initializable {

    @FXML protected Pane content;
    @FXML protected BorderPane borderPane;
    @FXML protected Text carcassonneText;
    @FXML protected TextField nameTextField;
    @FXML protected Label errorNameText;
    @FXML protected Button exitButton;
    
    public CommunicationController delegate;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
   
    }
    
    @FXML private void joinGame(ActionEvent event) {
        delegate.clickJoinGame(nameTextField.getText());
    }
    
    @FXML private void exitAction(ActionEvent event) {
        delegate.clickExitAction();
    }
    
    @FXML
    private void mouseEnter(MouseEvent event){
        delegate.scene.setCursor(Cursor.HAND);
    }
    
    @FXML
    private void mouseExit(MouseEvent event){
        delegate.scene.setCursor(Cursor.DEFAULT);
    }
}
