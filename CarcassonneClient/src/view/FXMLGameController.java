package view;

import controller.CommunicationController;
import java.awt.Point;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.effect.Bloom;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import view.imageloader.LandTileImageLoader;

public class FXMLGameController extends Group implements Initializable {

    @FXML protected GridPane centerGridPane;
    @FXML protected GridPane rightGridPane;
    @FXML protected ImageView imageView;
    @FXML protected Button leftRotateButton;
    @FXML protected Button rightRotateButton;
    private Rectangle[][] centerRectangles;
    private Button[][] rightButtons = new Button[15][5];
    private Image[] landTiles = new Image[71];
    private double degree;
    private List<Point> forbiddenPlacesOnTheTable = new ArrayList<>();
    
    public CommunicationController delegate;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        landTiles = LandTileImageLoader.getInstance().getLandTileImages();
        
        createRectanglesOnTheTable();
        createRectanglesOnTheRightSide();
        
        degree = 0;
    }
    
    private void createRectanglesOnTheTable() {
        centerRectangles = new Rectangle[143][143];
        for(int i=0; i<143; i++) {
           for(int j=0; j<143; j++) {           
               Rectangle rectangle = new Rectangle(0, 0, 120, 120);
               if(i==143/2 && j == 143/2) {
                   rectangle.setDisable(true);
                   rectangle.setFill(new ImagePattern(LandTileImageLoader.getInstance().getStarterLandTile()));
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
               rightButtons[i][j].setOnAction(chooseAction);
           }
        }
        for(int j=0; j<1; j++) {
            rightButtons[15-1][j] = new GameButton(15-1, j);
            rightButtons[15-1][j].setId("right_buttons");
            rightGridPane.add(rightButtons[15-1][j], j, 15-1);
            rightButtons[15-1][j].setOnAction(chooseAction);
        }
    }
    
    @FXML private void clickLeftAction(ActionEvent event) throws RemoteException {
        delegate.clickRotateLeft();
    }
    
    @FXML private void clickRightAction(ActionEvent event) throws RemoteException {
        delegate.clickRotateRight();
    }
    
    public void rotateLeftUpdate() {
        degree -= 90;
        imageView.setRotate(degree);
    }
    
    public void rotateRightUpdate() {
        degree += 90;
        imageView.setRotate(degree);
    }
    
    public void illegalPlacesOnTableUpdate(List<Point> illegalPoints) {
        removePreviousIllegalPlacesOnTable();
        for(Point p : illegalPoints) {
            centerRectangles[p.x][p.y].setDisable(true);
            centerRectangles[p.x][p.y].setFill(Color.ORANGE);
            forbiddenPlacesOnTheTable.add(p);
        }
    }
    
    private void removePreviousIllegalPlacesOnTable() {
        for(Point p : forbiddenPlacesOnTheTable) {
            centerRectangles[p.x][p.y].setDisable(false);
            centerRectangles[p.x][p.y].setFill(Color.GREEN);
        }
        forbiddenPlacesOnTheTable.clear();
    }
    
    private final EventHandler<MouseEvent> rectangleClickAction = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent t) {
            for(int i=0; i<143; i++) {
                for(int j=0; j<143; j++) {
                    if(centerRectangles[i][j] == t.getSource()) {
                        try {
                            delegate.locateLandTileOnTheTable(new Point(i,j));
                        } catch (RemoteException ex) {
                            System.out.println("Hiba a kártya elhelyezése során.");
                        }
                    }
                }
            }
        }
    };
    
    private final EventHandler<ActionEvent> chooseAction = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent t) {
            for(int i=0; i<15; i++) {
                for(int j=0; j<5; j++) {
                    if(t.getSource() == rightButtons[i][j]) {
                        try { 
                            delegate.chooseFaceDownLandTile(new Point(i,j));
                        } catch (RemoteException ex) {
                            System.err.println("Hiba az adat küldése során!");
                        }
                        break;
                    } else if(i==14 && j==0) {
                        break;
                    }
                }
            }
        }
    };
    
    public void chooseLandTileUpdate(Point p) {
        rightButtons[p.x][p.y].setDisable(true);
        imageView.setImage(landTiles[p.x*5+p.y]);
        leftRotateButton.setDisable(false);
        rightRotateButton.setDisable(false);
    }
    
    public void chooseLandTileWarningMessage() {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle("Hiba kártyahúzáskor");
        alert.setHeaderText(null);
        alert.setContentText("Már húztál egy kártyát!");
        alert.showAndWait();
    }
    
    public void landTileCantBeLocatednformationMessage() {
        ImageView img = new ImageView(imageView.getImage());
        img.setFitHeight(200);
        img.setFitWidth(200);
        
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("A kártyát nem lehet elhelyezni");
        alert.setHeaderText(null);
        alert.setGraphic(img);
        alert.setContentText("A kártyát nem lehet elhelyezni a táblán. Továbbra is Te következel, húzz egy másik kártyát!");
        alert.showAndWait();
        
        removePreviousIllegalPlacesOnTable();
        imageView.setImage(null);
        imageView.setRotate(360);
    }
    
    public void locateLandTileUpdate(Point p) {
        removePreviousIllegalPlacesOnTable();
        centerRectangles[p.x][p.y].setFill(new ImagePattern(imageView.getImage()));
        centerRectangles[p.x][p.y].setDisable(true);
        centerRectangles[p.x][p.y].getTransforms().add(new Rotate(degree, 60, 60));
        imageView.setImage(null);
        imageView.setRotate(360);
        expansionOfTheTable(p.x, p.y);
        degree = 0;
    }
    
    public void locateLandTileWarningMessage() {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle("Hiba a kártya elhelyezésekor");
        alert.setHeaderText(null);
        alert.setContentText("Még nem húztál kártyát!");
        alert.showAndWait();
    }  
    
    private void expansionOfTheTable(int i, int j) {
        if(i > 0 && !centerRectangles[i-1][j].isVisible()) {
            setFieldEnabledOnTheTable(i-1, j);
        }
        if(i < 142 && !centerRectangles[i+1][j].isVisible()) {
            setFieldEnabledOnTheTable(i+1, j);
        }
        if(j > 0 && !centerRectangles[i][j-1].isVisible()) {
            setFieldEnabledOnTheTable(i, j-1);
        }
        if(j < 142 && !centerRectangles[i][j+1].isVisible()) {
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
