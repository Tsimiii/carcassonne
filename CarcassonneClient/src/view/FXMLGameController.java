package view;

import controller.CommunicationController;
import controller.CommunicationController;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import view.GameButton;

public class FXMLGameController implements Initializable {

    @FXML protected GridPane centerGridPane;
    @FXML protected GridPane rightGridPane;
    private Rectangle[][] centerRectangles = new Rectangle[143][143];
    private Button[][] rightButtons = new Button[18][4];
    private Image[] landtiles = new Image[72];
    
    public CommunicationController delegate;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        createRectanglesOnTheTable();
        createRectanglesOnTheRightSide();
    }
    
    private void createRectanglesOnTheTable() {
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
               /*rectangle.setOnMouseClicked(rectangleClickAction);
               rectangle.setOnMouseEntered(ractangleEnterAction);       
               rectangle.setOnMouseExited(rectangleExitAction);*/
               centerGridPane.add(centerRectangles[i][j], j, i);
           }
        }
    }
    
    private void createRectanglesOnTheRightSide() {
        for(int i=0; i<18-1; i++) {
           for(int j=0; j<4; j++) {
               rightButtons[i][j] = new GameButton(i, j);
              /* rightButtons[i][j].setGraphic(new ImageView(new Image("file:src/resources/images/landtileback.png")));
               rightButtons[i][j].setScaleX(0.2);
               rightButtons[i][j].setScaleY(0.2);*/
               rightGridPane.add(rightButtons[i][j], j, i);
              // rightButtons[i][j].setOnAction(chooseAction);
           }
        }
        for(int j=0; j<3; j++) {
            rightButtons[18-1][j] = new GameButton(18-1, j);
           /* rightButtons[height-1][j].setGraphic(new ImageView(new Image("file:src/resources/images/landtileback.png")));
            rightButtons[height-1][j].setScaleX(0.2);
            rightButtons[height-1][j].setScaleY(0.2);*/
            rightGridPane.add(rightButtons[18-1][j], j, 18-1);
            //rightButtons[height-1][j].setOnAction(chooseAction);
        }
    }
    
    @FXML private void clickLeftAction(ActionEvent event) {
        //doSomething
    }
    
    @FXML private void clickRightAction(ActionEvent event) {
        //doSomething
    }
    
}
