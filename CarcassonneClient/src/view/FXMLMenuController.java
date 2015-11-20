package view;

import controller.CommunicationController;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import view.dialogs.WarningDialog;

public class FXMLMenuController implements Initializable {

    @FXML protected Pane content;
    @FXML protected BorderPane borderPane;
    @FXML protected Text carcassonneText;
    @FXML protected TextField nameTextField;
    @FXML protected Label errorNameText;
    @FXML protected Button exitButton;
    
    private Alert alert;
    
    public CommunicationController delegate;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
       nameTextField.setOnKeyPressed(keyPressed);
    }
    
    @FXML private void joinGame(ActionEvent event) {
        joinGameCommands();
    }
    
    private void joinGameCommands() {
        if(!nameTextField.getText().equals("")) {
            delegate.clickJoinGame(nameTextField.getText());
        } else {
            new WarningDialog("Hiányzó név", "Csatlakozás előtt írd be a nevedet!");
            nameTextField.requestFocus();
        }
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
    
    EventHandler<KeyEvent> keyPressed = (KeyEvent e) -> {
       if(e.getCode().equals(KeyCode.ENTER)) {
           joinGameCommands();
       }
    };
    
}
