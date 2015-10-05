package view;

import controller.CommunicationController;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.effect.Bloom;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class FXMLGameController extends Group implements Initializable {

    @FXML protected GridPane centerGridPane;
    @FXML protected GridPane rightGridPane;
    private Rectangle[][] centerRectangles;
    private Button[][] rightButtons = new Button[15][5];
    private Image[] landtiles = new Image[72];
    
    public CommunicationController delegate;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        createRectanglesOnTheTable();
        createRectanglesOnTheRightSide();
    }
    
    private void createRectanglesOnTheTable() {
        centerRectangles = new Rectangle[143][143];
        for(int i=0; i<143; i++) {
           for(int j=0; j<143; j++) {           
               Rectangle rectangle = new Rectangle(0, 0, 120, 120);
               if(i==143/2 && j == 143/2) {
                   rectangle.setDisable(true);
                   //rectangle.setFill(new ImagePattern(landtiles[landtiles.length-1]));
                   rectangle.setFill(Color.PINK);
               }
               else if((i==143/2 && (j==143/2-1 || j==143/2+1)) || (j==143/2 && (i==143/2-1 || i==143/2+1))) {
                   rectangle.setFill(Color.GREEN);
               }
               else {
                   rectangle.setHeight(0);
                   rectangle.setWidth(0);
                   rectangle.setVisible(false);
                   rectangle.setDisable(true); 
               }
               rectangle.setStroke(Color.BLACK);
               centerRectangles[i][j] = rectangle;
               rectangle.setOnMouseClicked(rectangleClickAction);
               rectangle.setOnMouseEntered(ractangleEnterAction);       
               rectangle.setOnMouseExited(rectangleExitAction);
               centerGridPane.add(centerRectangles[i][j], j, i);
           }
        }
    }
    
    private void createRectanglesOnTheRightSide() {
        for(int i=0; i<15-1; i++) {
           for(int j=0; j<5; j++) {
               rightButtons[i][j] = new GameButton(i, j);
               rightButtons[i][j].setId("right_buttons");
               rightGridPane.add(rightButtons[i][j], j, i);
              // rightButtons[i][j].setOnAction(chooseAction);
           }
        }
        for(int j=0; j<1; j++) {
            rightButtons[15-1][j] = new GameButton(15-1, j);
            rightButtons[15-1][j].setId("right_buttons");
            rightGridPane.add(rightButtons[15-1][j], j, 15-1);
            //rightButtons[height-1][j].setOnAction(chooseAction);
        }
    }
    
    @FXML private void clickLeftAction(ActionEvent event) {
        //doSomething
    }
    
    @FXML private void clickRightAction(ActionEvent event) {
        //doSomething
    }
    
    private final EventHandler<MouseEvent> rectangleClickAction = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent t) {
                for(int i=0; i<143; i++) {
                    for(int j=0; j<143; j++) {
                        if(centerRectangles[i][j] == t.getSource()) {
                            expansionOfTheTable(i, j);
                        }
                    }
                }
            }
        };
    
    private void expansionOfTheTable(int i, int j) {
        if(i > 0 && centerRectangles[i-1][j].isDisable()) {
            setFieldEnabledOnTheTable(i-1, j);
        }
        if(i < 142 && centerRectangles[i+1][j].isDisable()) {
            setFieldEnabledOnTheTable(i+1, j);
        }
        if(j > 0 && centerRectangles[i][j-1].isDisable()) {
            setFieldEnabledOnTheTable(i, j-1);
        }
        if(j < 142 && centerRectangles[i][j+1].isDisable()) {
            setFieldEnabledOnTheTable(i, j+1);
        }
    }
    
    private void setFieldEnabledOnTheTable(int i, int j) {
        centerRectangles[i][j].setDisable(false);
        centerRectangles[i][j].setVisible(true);
        centerRectangles[i][j].setFill(Color.GREEN);
        centerRectangles[i][j].setWidth(120);
        centerRectangles[i][j].setHeight(120);
    }
        
    private final EventHandler<MouseEvent> ractangleEnterAction = (MouseEvent t) -> {
        for(int i=0; i<143; i++) {
            for(int j=0; j<143; j++) {
                if(centerRectangles[i][j] == t.getSource()) {
                    centerRectangles[i][j].setEffect(new Bloom());
                }
            }
        }
    };
    
    private final EventHandler<MouseEvent> rectangleExitAction = (MouseEvent t) -> {
        for(int i=0; i<143; i++) {
            for(int j=0; j<143; j++) {
                if(centerRectangles[i][j] == t.getSource()) {
                    centerRectangles[i][j].setEffect(null);
                }
            }
        }
    };
    
}
