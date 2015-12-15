package view;

import controller.CommunicationController;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import view.dialogs.WarningDialog;

// A menü dinamikus megjelenítése
public class FXMLMenuController implements Initializable {

    @FXML protected Pane content;
    @FXML protected BorderPane borderPane;
    @FXML protected TextField nameTextField; // A név beírására szolgáló mező
    @FXML protected Button exitButton; // A kilépés gomb
    
    public CommunicationController delegate;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
       nameTextField.setOnKeyPressed(keyPressed); // A névmezőn egyből villog a kurzor, és kész a név fogadására
    }
    
    // A játékhoz való csatlakozás gombra kattintás
    @FXML private void joinGame(ActionEvent event) {
        joinGameCommands();
    }
    
    private void joinGameCommands() {
        // Ha a név meg van adva, a program továbblép
        if(!nameTextField.getText().equals("")) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Portszám megadása");
            dialog.setHeaderText(null);
            dialog.setContentText("Add meg a portszámot:");
            Optional<String> port = dialog.showAndWait(); // A portszám bekérése kliensoldalon
            if(port.isPresent()) {
                delegate.port = Integer.parseInt(port.get());
                delegate.clickJoinGame(nameTextField.getText());
            } else {
                nameTextField.requestFocus();
            }
        // Ha nincs megadva név, hibaüzenet jön
        } else {
            new WarningDialog("Hiányzó név", "Csatlakozás előtt írd be a nevedet!");
            nameTextField.requestFocus();
        }
    }
    
    // A kilépés gombra kattintás
    @FXML private void exitAction(ActionEvent event) {
        delegate.clickExitAction();
    }
    
    // Ha egy gomb fölé megy az egér, a kurzor kéz formájúvá változik
    @FXML
    private void mouseEnter(MouseEvent event){
        delegate.scene.setCursor(Cursor.HAND);
    }
    
    // Ha egy gombról kimegy az egér, akkor a kurzor visszavált a default formájára
    @FXML
    private void mouseExit(MouseEvent event){
        delegate.scene.setCursor(Cursor.DEFAULT);
    }
    
    // Enter lenyomásával is engedélyezi a csatlakozást
    EventHandler<KeyEvent> keyPressed = (KeyEvent e) -> {
       if(e.getCode().equals(KeyCode.ENTER)) {
           joinGameCommands();
       }
    };
    
}
