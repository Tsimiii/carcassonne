package view;

import carcassonneclient.CarcassonneClientProperties;
import controller.CommunicationController;
import java.awt.Point;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Group;
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
import view.dialogs.InformationDialog;
import view.dialogs.WarningDialog;
import view.imageloader.LandTileImageLoader;

public class FXMLGameController extends Group implements Initializable {
    
    private final Point[] FOLLOWERDEFAULTPOINTPOSITION = new Point[] {new Point(-50,-40), new Point(-50, 0), new Point(-50, 40), new Point(-40,50), new Point(0, 50), new Point(40,50), new Point(50, 40), new Point(50, 0), new Point(50, -40), new Point(40, -50), new Point(0, -50), new Point(-40, -50), new Point(0, 0)};

    @FXML protected GridPane centerGridPane;
    @FXML protected GridPane rightGridPane;
    @FXML protected ImageView imageView;
    @FXML protected Button leftRotateButton;
    @FXML protected Button rightRotateButton;
    @FXML protected VBox leftVBox;
    private final int RIGHTBUTTONROWNUMBER;
    private final int RIGHTBUTTONCOLUMNNUMBER;
    private final int LANDTILENUMBER;
    private int TABLESIZE;
    private int id;
    private List<String> namesList;
    private Label[] names;
    private Label[] points;
    private Label[] followers;
    private StackPane[][] stackPane;
    private Rectangle[][] centerRectangles;
    private Button[][] rightButtons;
    private Image[] landTiles;
    private Point actualLandTileTablePosition;
    private double degree;
    private List<Point> forbiddenPlacesOnTheTable = new ArrayList<>();
    private List<Point> drawnLandTiles = new ArrayList<>();
    private Map<Point, Circle> circles = new HashMap<>();
    
    public CommunicationController delegate;

    public FXMLGameController(int id, List<String> namesList) throws IOException {
        CarcassonneClientProperties prop = new CarcassonneClientProperties();
        RIGHTBUTTONROWNUMBER = prop.getRightButtonRowNumber();
        RIGHTBUTTONCOLUMNNUMBER = prop.getRightButtonColumnNumber();
        LANDTILENUMBER = prop.getLandTileNumber();
        TABLESIZE = prop.getTableSize();
        this.rightButtons = new Button[RIGHTBUTTONROWNUMBER][RIGHTBUTTONCOLUMNNUMBER];
        this.landTiles = new Image[LANDTILENUMBER];
        this.id = id;
        this.namesList = namesList;
        names = new Label[this.namesList.size()];
        followers = new Label[this.namesList.size()];
        points = new Label[this.namesList.size()];
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
        leftRotateButton.setOnMouseEntered(buttonEnterAction);
        rightRotateButton.setOnMouseEntered(buttonEnterAction);
        leftRotateButton.setOnMouseExited(buttonExitAction);
        rightRotateButton.setOnMouseExited(buttonExitAction);
        
        disableOrEnableEverything(true);
        
        degree = 0;
    }
    
    private void createRectanglesOnTheTable() {
        stackPane = new StackPane[TABLESIZE][TABLESIZE];
        centerRectangles = new Rectangle[TABLESIZE][TABLESIZE];
        for(int i=0; i<TABLESIZE; i++) {
           for(int j=0; j<TABLESIZE; j++) {           
               Rectangle rectangle = new Rectangle(0, 0, 120, 120);
               if(i==TABLESIZE/2 && j == TABLESIZE/2) {
                   rectangle.setDisable(true);
                   rectangle.setFill(new ImagePattern(LandTileImageLoader.getInstance().getStarterLandTile()));
               }
               else if((i==TABLESIZE/2 && (j==TABLESIZE/2-1 || j==TABLESIZE/2+1)) || (j==TABLESIZE/2 && (i==TABLESIZE/2-1 || i==TABLESIZE/2+1))) {
                   rectangle.setFill(Color.GREEN);
               }
               else if(((i<TABLESIZE/2-1 && i > TABLESIZE/2-4) || (i > TABLESIZE/2+1 && i < TABLESIZE/2+4)) && ((j<TABLESIZE/2-1 && j > TABLESIZE/2-6) || (j > TABLESIZE/2+1 && j < TABLESIZE/2+6))) {
                   rectangle.setVisible(false);
                   rectangle.setDisable(true); 
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
        for(int i=0; i<RIGHTBUTTONROWNUMBER-1; i++) {
           for(int j=0; j<RIGHTBUTTONCOLUMNNUMBER; j++) {
               rightButtons[i][j] = new GameButton(i, j);
               rightButtons[i][j].setId("right_buttons");
               rightGridPane.add(rightButtons[i][j], j, i);
               rightButtons[i][j].setOnAction(chooseAction);
               rightButtons[i][j].setOnMouseEntered(buttonEnterAction);
               rightButtons[i][j].setOnMouseExited(buttonExitAction);
           }
        }
        for(int j=0; j<1; j++) {
            rightButtons[RIGHTBUTTONROWNUMBER-1][j] = new GameButton(RIGHTBUTTONROWNUMBER-1, j);
            rightButtons[RIGHTBUTTONROWNUMBER-1][j].setId("right_buttons");
            rightGridPane.add(rightButtons[15-1][j], j, RIGHTBUTTONROWNUMBER-1);
            rightButtons[RIGHTBUTTONROWNUMBER-1][j].setOnAction(chooseAction);
            rightButtons[RIGHTBUTTONROWNUMBER-1][j].setOnMouseEntered(buttonEnterAction);
            rightButtons[RIGHTBUTTONROWNUMBER-1][j].setOnMouseExited(buttonExitAction);
        }
    }
    
    private void createLeftSideOfTheScreen() {
        for(int i=0; i<namesList.size(); i++) {
            GridPane gridPane = new GridPane();
            gridPane.setVgap(15);
            names[i] = new Label();
            names[i].setId("nameLabel");
            names[i].setText(namesList.get(i) + ": ");
            points[i] = new Label("0 pont");
            points[i].setId("pointLabel");
            followers[i] = new Label("7 alattvaló");
            followers[i].setId("followerLabel");
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
            for(int i=0; i<TABLESIZE; i++) {
                for(int j=0; j<TABLESIZE; j++) {
                    if(centerRectangles[i][j] == t.getSource()) {
                        try {
                            int successLocate = delegate.locateLandTileOnTheTable(new Point(i,j));
                            if(successLocate == 2) {
                                delegate.openLocateFollowerWindow(imageView.getImage(), degree);
                            } else if(successLocate == 1) {
                                delegate.countPoints();
                            }
                        } catch (RemoteException ex) {
                            System.err.println("Hiba a kártya elhelyezése során.");
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
            case 0 : return Color.rgb(30,144,255);
            case 1 : return Color.RED;
            case 2 : return Color.rgb(218,165,32);
            case 3 : return Color.rgb(46,139,87);
            case 4 : return Color.BLACK;
            default : System.err.println("Ilyen id nem létezik!"); return null;
        }
    }
    
    private final EventHandler<ActionEvent> chooseAction = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent t) {
            for(int i=0; i<RIGHTBUTTONROWNUMBER; i++) {
                for(int j=0; j<RIGHTBUTTONCOLUMNNUMBER; j++) {
                    if(t.getSource() == rightButtons[i][j]) {
                        try { 
                            delegate.chooseFaceDownLandTile(new Point(i,j));
                        } catch (RemoteException ex) {
                            System.err.println("Hiba az adat küldése során!");
                        }
                        break;
                    } else if(i==RIGHTBUTTONROWNUMBER-1 && j==0) {
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
        for(int i=0; i<TABLESIZE; i++) {
            for(int j=0; j<TABLESIZE; j++) {
                centerRectangles[i][j].setStroke(Color.BLACK);
                centerRectangles[i][j].setStrokeWidth(1);
            }
        }
        delegate.chooseFaceDownLandTileDone();
    }
    
    public void enableRotateButtons() {
        leftRotateButton.setDisable(false);
        rightRotateButton.setDisable(false);
    }
    
    public void chooseLandTileWarningMessage() {
        new WarningDialog("Hiba kártyahúzáskor", "Már húztál egy kártyát!");
    }
    
    public void landTileCantBeLocatednformationMessage() {
        ImageView img = new ImageView(imageView.getImage());
        img.setFitHeight(200);
        img.setFitWidth(200);
        
        new InformationDialog("A kártyát nem lehet elhelyezni", "Az általad kihúzott kártyát nem lehet elhelyezni a táblán. Továbbra is Te következel, húzz egy másikat!", img);
        
        removePreviousIllegalPlacesOnTable();
        imageView.setImage(new Image("file:src/resources/images/empty.jpg"));
        imageView.setRotate(360);
    }
    
    public void locateLandTileUpdate(Point p) {
        actualLandTileTablePosition = p;
        removePreviousIllegalPlacesOnTable();
        centerRectangles[p.x][p.y].setFill(new ImagePattern(imageView.getImage()));
        centerRectangles[p.x][p.y].setDisable(true);
        centerRectangles[p.x][p.y].getTransforms().add(new Rotate(degree, 60, 60));
        centerRectangles[p.x][p.y].setStrokeWidth(2);
        centerRectangles[p.x][p.y].setStroke(Color.INDIANRED);
        expansionOfTheTable(p.x, p.y);
        try {
            delegate.locateLandTileDone();
        } catch (RemoteException ex) {
            Logger.getLogger(FXMLGameController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void locateLandTileWarningMessage() {
        new WarningDialog("Hiba a kártya elhelyezésekor", "Még nem húztál kártyát!");
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
    
    public void countPointUpdate(int[] point) throws RemoteException {
        for(int i=0; i<points.length; i++) {
            points[i].setText(point[i] + " pont");
        }
        imageView.setImage(new Image("file:src/resources/images/empty.jpg"));
        imageView.setRotate(360);
        degree = 0;
        disableOrEnableEverything(true);
    }
    
    public void countPointEndOfTheGameUpdate(int[] point) {
        for(int i=0; i<points.length; i++) {
            int num = Integer.parseInt(points[i].getText().split("\\s+")[0]);
            points[i].setText((point[i] + num) + " pont");
        }
        imageView.setImage(new Image("file:src/resources/images/empty.jpg"));
        imageView.setRotate(360);
        degree = 0;
        disableOrEnableEverything(true);
    }
    
    public void followerNumberUpdate(int[] followerNumbers, List<Point> freeCircles) throws RemoteException {
        for(int i=0; i<followerNumbers.length; i++) {
            followers[i].setText(followerNumbers[i] + " alattvaló");
        }
        for(Point p : freeCircles) {
            if(circles.get(p).getFill().equals(Color.rgb(30,144,255))) {
                circles.get(p).setFill(Color.rgb(176,224,230));
            } else if(circles.get(p).getFill().equals(Color.RED)) {
                circles.get(p).setFill(Color.rgb(255,160,122));
            } else if(circles.get(p).getFill().equals(Color.rgb(218,165,32))) {
                circles.get(p).setFill(Color.rgb(238,232,170));
            } else if(circles.get(p).getFill().equals(Color.rgb(46,139,87))) {
                circles.get(p).setFill(Color.rgb(152,251,152));
            } else if(circles.get(p).getFill().equals(Color.BLACK)) {
                circles.get(p).setFill(Color.rgb(190,190,190));
            }
        }
        delegate.nextPlayersTurn();
    }
        
    private final EventHandler<MouseEvent> ractangleEnterAction = (MouseEvent t) -> {
        for(int i=0; i<TABLESIZE; i++) {
            for(int j=0; j<TABLESIZE; j++) {
                if(centerRectangles[i][j] == t.getSource()) {
                    centerRectangles[i][j].setEffect(new Bloom());
                    delegate.scene.setCursor(Cursor.HAND);
                }
            }
        }
    };
    
    private final EventHandler<MouseEvent> rectangleExitAction = (MouseEvent t) -> {
        for(int i=0; i<143; i++) {
            for(int j=0; j<143; j++) {
                if(centerRectangles[i][j] == t.getSource()) {
                    centerRectangles[i][j].setEffect(null);
                    delegate.scene.setCursor(Cursor.DEFAULT);
                }
            }
        }
    };
    
    private final EventHandler<MouseEvent> buttonEnterAction = (MouseEvent event) -> {
        delegate.scene.setCursor(Cursor.HAND);
    };
    
    private final EventHandler<MouseEvent> buttonExitAction = (MouseEvent event) -> {
        delegate.scene.setCursor(Cursor.DEFAULT);
    };
    
    public void gameIsOverMessageUpdate() throws IOException {
        new WarningDialog("Játék vége", "Sajnos valaki elhagyta a játékot, így a játék véget ért.");
        delegate.endOfGameBecauseSomebodyQuittedClickOnOK();
    }
    
}