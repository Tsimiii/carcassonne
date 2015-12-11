package view;

import controller.CommunicationController;
import java.awt.Point;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

// Az eredményhirdető ablak dinamikus megjelenése
public class FXMLResultScreenController implements Initializable{
    
    private List<Point> list; // A pontok listája, az elemek növekvő sorrendben
    private List<String> names; // A nevek listája
    private Stage stage;
    @FXML protected GridPane resultGridPane;
    @FXML protected Button exitButton; // A kilépés gomb
    @FXML protected Button backToMainMenuButton; // A vissza a menühöz gomb
    
    public CommunicationController delegate;

    public FXMLResultScreenController(List<Point> list, List<String> names, Stage stage) {
        this.list = list;
        this.names = names;
        this.stage = stage;
        stage.setOnCloseRequest(closeWindowAction);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        backToMainMenuButton.setOnMouseClicked(backToMainMenuAction);
        exitButton.setOnMouseClicked(exitAction);
        backToMainMenuButton.setOnMouseEntered(mouseEnter);
        exitButton.setOnMouseEntered(mouseEnter);
        backToMainMenuButton.setOnMouseExited(mouseExit);
        exitButton.setOnMouseExited(mouseExit);
        
        setBorderPaneCenter();
    }
    
    // A sorrend szám, valamint a nevek és pontok megjelenítése a pontok szempontjából csökkenő sorrendben
    private void setBorderPaneCenter() {
        Text[] rankingText = new Text[names.size()];
        Text[] nameText = new Text[names.size()];
        Text[] pointText = new Text[names.size()];
        for (int j=list.size()-1; j>-1; j--) {
            rankingText[list.size()-j-1] = new Text((list.size()-j)+".");
            rankingText[list.size()-j-1].setId("rankingTextResult");
            resultGridPane.add(rankingText[list.size()-j-1], 0, list.size()-j-1);
            nameText[list.size()-j-1] = new Text(names.get(list.get(j).x));
            nameText[list.size()-j-1].setId("nameTextResult");
            resultGridPane.add(nameText[list.size()-j-1], 1, list.size()-j-1);
            pointText[list.size()-j-1] = new Text(list.get(j).y+" pont");
            pointText[list.size()-j-1].setId("pointTextResult");
            resultGridPane.add(pointText[list.size()-j-1], 2, list.size()-j-1);
        }
    }
    
    // A vissza a menühöz gombra kattintás
    private final EventHandler<MouseEvent> backToMainMenuAction = (MouseEvent event) -> {
        try {
            delegate.clickBactToMainMenuAction();
        } catch(IOException e) {
            System.err.println("Hiba a Vissza a menühöz gombra kattintáskor.");
        }
    };
    
    // A kilépés gombra kattintás
    private final EventHandler<MouseEvent> exitAction = (MouseEvent event) -> {
        delegate.clickExitAction();
    };
    
    // Ha gombok fölé megy az egér, ilyenkor a kurzor kéz formájú lesz
    private final EventHandler<MouseEvent> mouseEnter = (MouseEvent event) -> {
        delegate.scene.setCursor(Cursor.HAND);
    };
    
    // Ha gombok fölül kimegy az egér, ilyenkor a kurzor ismét default formájú lesz
    private final EventHandler<MouseEvent> mouseExit = (MouseEvent event) -> {
        delegate.scene.setCursor(Cursor.DEFAULT);
    };
    
    // Az X gombra kattintás következében a játékos kilép az alkalmazásból
    private final EventHandler<WindowEvent> closeWindowAction = (WindowEvent event) -> {
            delegate.clickExitAction();
    };
    
}
