package view;

import controller.Controller;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

public class CarcassonneGameView extends Group {
    public Controller delegate;
    
    private BorderPane borderpane;
    private StackPane stackPane;
    
    private Rectangle[][] centerRectangles;
    private Button[][] rightButtons;
    
    private ImageView imageview;
    private Image[] landtiles;
    private Button rotateLeft;
    private Button rotateRight;

    public CarcassonneGameView() {
        borderpane = new BorderPane();
        borderpane.setCenter(setScrollPaneCenter(21,21));
        borderpane.setRight(setGridPaneRight(18,4));
        landtiles = new Image[72];
        this.getChildren().add(borderpane);
    }
    
        private ScrollPane setScrollPaneCenter(int width, int height) {
        ScrollPane scrollPane = new ScrollPane();
        stackPane = new StackPane();
        scrollPane.setPrefSize(680, 680);
        GridPane gridPaneCenter = new GridPane();
        gridPaneCenter.setPadding(new Insets(10, 10, 10, 10));
        centerRectangles = new Rectangle[height][width];
        gridPaneCenter.setAlignment(Pos.CENTER);
        for(int i=0; i<height; i++) {
           for(int j=0; j<width; j++) {           
               Rectangle rectangle = new Rectangle(0, 0, 120, 120);
               if(i==height/2 && j == width/2) {
                   rectangle.setDisable(true);
                   rectangle.setFill(/*new ImagePattern(landtiles[landtiles.length-1])*/Color.CYAN);            
               }
               else if((i==height/2 && (j==width/2-1 || j==width/2+1)) || (j==width/2 && (i==height/2-1 || i==height/2+1))) {
                   rectangle.setFill(Color.GREEN);
               }
               else {
                   rectangle.setHeight(0);
                   rectangle.setWidth(0);
                   rectangle.setVisible(false);
                   rectangle.setDisable(true);
                   rectangle.setFill(Color.RED); 
               }
               rectangle.setStroke(Color.BLACK);
               centerRectangles[i][j] = rectangle;
               /*rectangle.setOnMouseClicked(rectangleClickAction);
               rectangle.setOnMouseEntered(ractangleEnterAction);       
               rectangle.setOnMouseExited(rectangleExitAction);*/
               gridPaneCenter.add(centerRectangles[i][j], j, i);
           }
        }
        stackPane.getChildren().add(gridPaneCenter);
        scrollPane.setContent(stackPane);
        return scrollPane;
    }
        
        private VBox setGridPaneRight(int height, int width) {
        VBox vb = new VBox();
        GridPane gridPaneRight = new GridPane();
        rightButtons = new Button[height][width];
        for(int i=0; i<height-1; i++) {
           for(int j=0; j<width; j++) {
               rightButtons[i][j] = new GameButton(i, j);
              /* rightButtons[i][j].setGraphic(new ImageView(new Image("file:src/resources/images/landtileback.png")));
               rightButtons[i][j].setScaleX(0.2);
               rightButtons[i][j].setScaleY(0.2);*/
               gridPaneRight.add(rightButtons[i][j], j, i);
              // rightButtons[i][j].setOnAction(chooseAction);
           }
        }
        for(int j=0; j<3; j++) {
            rightButtons[height-1][j] = new GameButton(height-1, j);
           /* rightButtons[height-1][j].setGraphic(new ImageView(new Image("file:src/resources/images/landtileback.png")));
            rightButtons[height-1][j].setScaleX(0.2);
            rightButtons[height-1][j].setScaleY(0.2);*/
            gridPaneRight.add(rightButtons[height-1][j], j, height-1);
            //rightButtons[height-1][j].setOnAction(chooseAction);
        }
        vb.getChildren().add(gridPaneRight);
        imageview = new ImageView();
        imageview.setImage(new Image("file:src/resources/images/landtileback.png"));
        imageview.setFitWidth(200);
        imageview.setFitHeight(200);
        vb.getChildren().add(imageview);
        
        rotateLeft = new Button("bal");
        rotateRight = new Button("jobb");
        HBox hb = new HBox();
        hb.getChildren().addAll(rotateLeft, rotateRight);
        /*rotateLeft.setOnAction(clickLeftAction);
        rotateRight.setOnAction(clickRightAction);*/
        vb.getChildren().add(hb);
        return vb;
    }
    
}
