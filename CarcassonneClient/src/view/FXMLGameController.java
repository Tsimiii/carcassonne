package view;

import controller.CommunicationController;
import java.awt.Point;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.Bloom;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import view.imageloader.LandTileImageLoader;

public class FXMLGameController extends Group implements Initializable {
    
    private final Point[] FOLLOWERDEFAULTPOINTPOSITION = new Point[] {new Point(-50,-40), new Point(-50, 0), new Point(-50, 40), new Point(-40,50), new Point(0, 50), new Point(40,50), new Point(50, 40), new Point(50, 0), new Point(50, -40), new Point(40, -50), new Point(0, -50), new Point(-40, -50), new Point(0, 0)};

    @FXML protected GridPane centerGridPane;
    @FXML protected GridPane rightGridPane;
    @FXML protected ImageView imageView;
    @FXML protected Button leftRotateButton;
    @FXML protected Button rightRotateButton;
    @FXML protected VBox leftVBox;
    private int id;
    private List<String> namesList;
    private Label[] names = new Label[2];
    private Label[] points = new Label[2];
    private Label[] followers = new Label[2];
    private StackPane[][] stackPane;
    private Rectangle[][] centerRectangles;
    private Button[][] rightButtons = new Button[15][5];
    private Image[] landTiles = new Image[71];
    private Point actualLandTileTablePosition;
    private double degree;
    private List<Point> forbiddenPlacesOnTheTable = new ArrayList<>();
    private List<Point> drawnLandTiles = new ArrayList<>();
    private Map<Point, Circle> circles = new HashMap<>();
    
    public CommunicationController delegate;

    public FXMLGameController(int id, List<String> names) {
        this.id = id;
        this.namesList = names;
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        actualLandTileTablePosition = new Point(-1,-1);
        landTiles = LandTileImageLoader.getInstance().getLandTileImages();
        
        createRectanglesOnTheTable();
        createRectanglesOnTheRightSide();
        createLeftSideOfTheScreen();
        
        leftRotateButton.setOnMouseClicked(leftRotateButtonClickAction);
        rightRotateButton.setOnMouseClicked(rightRotateButtonClickAction);
        
        disableOrEnableEverything(true);
        
        degree = 0;
    }
    
    private void createRectanglesOnTheTable() {
        stackPane = new StackPane[143][143];
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
               stackPane[i][j] = new StackPane();
               stackPane[i][j].getChildren().add(centerRectangles[i][j]);
               centerGridPane.add(stackPane[i][j], j, i);
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
    
    private void createLeftSideOfTheScreen() {
        for(int i=0; i<namesList.size(); i++) {
            GridPane gridPane = new GridPane();
            gridPane.setVgap(15);
            names[i] = new Label();
            names[i].setText(namesList.get(i) + ": ");
            points[i] = new Label("0 pont");    
            followers[i] = new Label("7 alattvaló");
            followers[i].setTextFill(getColorOfNumber(i));

           gridPane.add(names[i], 0, 0);
           gridPane.add(points[i], 1, 0);
           gridPane.add(followers[i], 0, 1);
           leftVBox.getChildren().add(gridPane);
        }
    }
    
    private void disableOrEnableEverything(boolean boolParam) {
        for(int i=0; i<centerRectangles.length; i++) {
            for(int j=0; j<centerRectangles[i].length; j++) {
                if((!boolParam && centerRectangles[i][j].getFill().equals((Paint)Color.GREEN)) || boolParam) {
                    centerRectangles[i][j].setDisable(boolParam);
                }
            }
        }
        for(int i=0; i<rightButtons.length-1; i++) {
            for(int j=0; j<rightButtons[i].length; j++) {
                if(!drawnLandTiles.contains(new Point(i,j))) {
                    rightButtons[i][j].setDisable(boolParam);
                }
            }
        }
        for(int j=0; j<1; j++) {
            if(!drawnLandTiles.contains(new Point(rightButtons.length-1,j))) {
                rightButtons[rightButtons.length-1][j].setDisable(boolParam);
            }
        }
        if(boolParam) {
            rightRotateButton.setDisable(boolParam);
            leftRotateButton.setDisable(boolParam);
        }
    }
    
    private final EventHandler<MouseEvent> leftRotateButtonClickAction = new EventHandler<MouseEvent>() { 
        @Override
        public void handle(MouseEvent event) {
            try {
                delegate.clickRotateLeft();
            } catch (RemoteException ex) {
                System.err.println("Hiba a kártyalap balra forgatásakor.");
            }
        }
    };
    
    private final EventHandler<MouseEvent> rightRotateButtonClickAction = new EventHandler<MouseEvent>() { 
        @Override
        public void handle(MouseEvent event) {
            try {
                delegate.clickRotateRight();
            } catch (RemoteException ex) {
                System.err.println("Hiba a kártyalap jobbra forgatásakor.");
            }
        }
    };
    
    public void enableEverythingUpdate() {
        disableOrEnableEverything(false);
    }
    
    public void rotateLeftUpdate() {
        degree -= 90;
        imageView.setRotate(degree);
    }
    
    public void rotateRightUpdate() {
        degree += 90;
        imageView.setRotate(degree);
    }
    
    public void illegalPlacesOnTableUpdate(Set<Point> illegalPoints) {
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
                            int successLocate = delegate.locateLandTileOnTheTable(new Point(i,j));
                            if(successLocate == 2) {
                                delegate.openLocateFollowerWindow(imageView.getImage(), degree);
                            } else if(successLocate == 1) {
                                delegate.countPoints();
                            }
                        } catch (RemoteException ex) {
                            System.out.println("Hiba a kártya elhelyezése során.");
                        }
                    }
                }
            }
        }
    };
    
    public void locateFollowerUpdate(int space, int colorNum) throws RemoteException {
        Circle circle = new Circle(8);
        circle.setTranslateX(FOLLOWERDEFAULTPOINTPOSITION[space].x);
        circle.setTranslateY(FOLLOWERDEFAULTPOINTPOSITION[space].y);
        circle.setFill(getColorOfNumber(colorNum));
        circle.setStroke(Color.BLACK);
        circles.put(actualLandTileTablePosition, circle);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                stackPane[actualLandTileTablePosition.x][actualLandTileTablePosition.y].getChildren().add(circle);
            }
        });
    }
    
    private Color getColorOfNumber(int num) {
        switch(num) {
            case 0 : return Color.BLUE;
            case 1 : return Color.RED;
            case 2 : return Color.YELLOW;
            case 3 : return Color.GREEN;
            case 4 : return Color.BLACK;
            default : System.err.println("Ilyen id nem létezik!"); return null;
        }
    }
    
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
    
    public void chooseLandTileUpdate(Point p) throws RemoteException { 
        rightButtons[p.x][p.y].setDisable(true);
        drawnLandTiles.add(p);
        imageView.setImage(landTiles[p.x*5+p.y]);
        delegate.chooseFaceDownLandTileDone();
    }
    
    public void enableRotateButtons() {
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
        actualLandTileTablePosition = p;
        removePreviousIllegalPlacesOnTable();
        centerRectangles[p.x][p.y].setFill(new ImagePattern(imageView.getImage()));
        centerRectangles[p.x][p.y].setDisable(true);
        centerRectangles[p.x][p.y].getTransforms().add(new Rotate(degree, 60, 60));
        System.out.println("degr: " + degree);
        expansionOfTheTable(p.x, p.y);
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
            setFieldVisibledOnTheTable(i-1, j);
        }
        if(i < 142 && !centerRectangles[i+1][j].isVisible()) {
            setFieldVisibledOnTheTable(i+1, j);
        }
        if(j > 0 && !centerRectangles[i][j-1].isVisible()) {
            setFieldVisibledOnTheTable(i, j-1);
        }
        if(j < 142 && !centerRectangles[i][j+1].isVisible()) {
            setFieldVisibledOnTheTable(i, j+1);
        }
    }
    
    private void setFieldVisibledOnTheTable(int i, int j) {
        centerRectangles[i][j].setVisible(true);
        centerRectangles[i][j].setFill(Color.GREEN);
        centerRectangles[i][j].setWidth(120);
        centerRectangles[i][j].setHeight(120);
    }
    
    public void countPointUpdate(int[] point) {
        for(int i=0; i<points.length; i++) {
            points[i].setText(point[i] + " pont");
        }
        imageView.setImage(null);
        imageView.setRotate(360);
        System.out.println(degree);
        degree = 0;
        System.out.println("DE HÁT ITT VAN WTF");
        disableOrEnableEverything(true);
    }
    
    public void followerNumberUpdate(int[] followerNumbers, List<Point> freeCircles) throws RemoteException {
        for(int i=0; i<followerNumbers.length; i++) {
            followers[i].setText(followerNumbers[i] + " alattvaló");
        }
        for(Point p : freeCircles) {
            if(circles.get(p).getFill().equals(Color.BLUE)) {
                circles.get(p).setFill(Color.LIGHTBLUE);
            } else if(circles.get(p).getFill().equals(Color.RED)) {
                circles.get(p).setFill(Color.PINK);
            } else if(circles.get(p).getFill().equals(Color.GREEN)) {
                circles.get(p).setFill(Color.LIGHTGREEN);
            } else if(circles.get(p).getFill().equals(Color.YELLOW)) {
                circles.get(p).setFill(Color.LIGHTYELLOW);
            } else if(circles.get(p).getFill().equals(Color.BLACK)) {
                circles.get(p).setFill(Color.LIGHTGRAY);
            }
        }
        delegate.nextPlayersTurn();
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
