package view;

import controller.CommunicationController;
import java.awt.Point;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class FXMLResultScreenController implements Initializable{
    
    private List<Point> list;
    private List<String> names;
    private Stage stage;
    @FXML protected VBox resultScreneVBox;
    @FXML protected Button exitButton;
    @FXML protected Button backToMainMenuButton;
    
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
    
    private void setBorderPaneCenter() {
        Text[] rankingText = new Text[names.size()];
        Text[] nameText = new Text[names.size()];
        Text[] pointText = new Text[names.size()];
        for (int j=list.size()-1; j>-1; j--) {
            HBox hBox = new HBox();
            rankingText[list.size()-j-1] = new Text((list.size()-j)+".");
            rankingText[list.size()-j-1].setId("rankingTextResult");
            rankingText[list.size()-j-1].setTranslateX(210);
            nameText[list.size()-j-1] = new Text(names.get(list.get(j).x));
            nameText[list.size()-j-1].setId("nameTextResult");
            nameText[list.size()-j-1].setTranslateX(410);
            pointText[list.size()-j-1] = new Text(list.get(j).y+" pont");
            pointText[list.size()-j-1].setId("pointTextResult");
            pointText[list.size()-j-1].setTranslateX(610);
            hBox.getChildren().addAll(rankingText[list.size()-j-1], nameText[list.size()-j-1], pointText[list.size()-j-1]);
            resultScreneVBox.getChildren().add(hBox);
        }
    }
    
    private final EventHandler<MouseEvent> backToMainMenuAction = (MouseEvent event) -> {
        try {
            delegate.clickBactToMainMenuAction();
        } catch(IOException e) {
            System.err.println("IO exception");
        }
    };
    
    private final EventHandler<MouseEvent> exitAction = (MouseEvent event) -> {
        delegate.clickExitAction();
    };
    
    private final EventHandler<MouseEvent> mouseEnter = (MouseEvent event) -> {
        delegate.scene.setCursor(Cursor.HAND);
    };
    
    private final EventHandler<MouseEvent> mouseExit = (MouseEvent event) -> {
        delegate.scene.setCursor(Cursor.DEFAULT);
    };
    
    private final EventHandler<WindowEvent> closeWindowAction = (WindowEvent event) -> {
        try {
            delegate.clickBactToMainMenuAction();
        } catch(IOException e) {
            System.err.println("IO exception");
        }
    };
    
}
