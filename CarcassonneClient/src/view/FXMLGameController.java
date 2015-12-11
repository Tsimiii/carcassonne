package view;

import view.button.GameButton;
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

// A játék dinamikus megjelenítése
public class FXMLGameController extends Group implements Initializable {
    
    // A lehetséges alattvaló pontok helyei
    private final Point[] FOLLOWERDEFAULTPOINTPOSITION = new Point[] {new Point(-50,-40), new Point(-50, 0), new Point(-50, 40), new Point(-40,50), new Point(0, 50), new Point(40,50), new Point(50, 40), new Point(50, 0), new Point(50, -40), new Point(40, -50), new Point(0, -50), new Point(-40, -50), new Point(0, 0)};

    @FXML protected GridPane centerGridPane;
    @FXML protected GridPane rightGridPane;
    @FXML protected ImageView imageView; // A kihúzott kártya betöltésének helye
    @FXML protected Button leftRotateButton; // A balra forgatás gombja
    @FXML protected Button rightRotateButton; // A jobbraforgatás gombja
    @FXML protected VBox leftVBox;
    private final int RIGHTBUTTONROWNUMBER;
    private final int RIGHTBUTTONCOLUMNNUMBER;
    private final int LANDTILENUMBER;
    private int TABLESIZE;
    private int id;
    private List<String> namesList;
    private Label[] names; // A játékosok neveinek kiírása
    private Label[] points; //A játékosok pontszámainak kiírása
    private Label[] followers; // A játékosok alattvalóinak számának kiírása
    private StackPane[][] stackPane;
    private Rectangle[][] centerRectangles; // Az asztalon levő cellák
    private Button[][] rightButtons; // A jobb oldalon levő lefordított területkártyák
    private Image[] landTiles; // A kártyák képei
    private Point actualLandTileTablePosition; // Az aktuálisan kihúzott kártya asztalon levő pozíciója
    private double degree; // A kártya elforgatásának mértéke
    private List<Point> forbiddenPlacesOnTheTable = new ArrayList<>(); // Az asztalton letiltott helyek listája
    private List<Point> drawnLandTiles = new ArrayList<>(); // A kihúzott kártyák listája
    private Map<Point, Circle> circles = new HashMap<>(); // Az alattvalók helyeinek Map-je
    
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
        landTiles = LandTileImageLoader.getInstance().getLandTileImages(); // A kártyaképek betöltése
        
        createRectanglesOnTheTable();
        createRectanglesOnTheRightSide();
        createLeftSideOfTheScreen();
        
        leftRotateButton.setOnMouseClicked(leftRotateButtonClickAction);
        rightRotateButton.setOnMouseClicked(rightRotateButtonClickAction);
        leftRotateButton.setOnMouseEntered(buttonEnterAction);
        rightRotateButton.setOnMouseEntered(buttonEnterAction);
        leftRotateButton.setOnMouseExited(buttonExitAction);
        rightRotateButton.setOnMouseExited(buttonExitAction);
        
        disableOrEnableEverything(true); //Minden letiltása
        
        degree = 0;
    }
    
    // Az asztalon levő cellák betöltése
    private void createRectanglesOnTheTable() {
        
        stackPane = new StackPane[TABLESIZE][TABLESIZE];
        centerRectangles = new Rectangle[TABLESIZE][TABLESIZE];
        for(int i=0; i<TABLESIZE; i++) {
           for(int j=0; j<TABLESIZE; j++) {           
               Rectangle rectangle = new Rectangle(0, 0, 120, 120);
               // A középső cella a kezdőlap a megfelelő képpel
               if(i==TABLESIZE/2 && j == TABLESIZE/2) {
                   rectangle.setDisable(true);
                   rectangle.setFill(new ImagePattern(LandTileImageLoader.getInstance().getStarterLandTile()));
               }
               //A kezdőlap melletti cellák zöldek
               else if((i==TABLESIZE/2 && (j==TABLESIZE/2-1 || j==TABLESIZE/2+1)) || (j==TABLESIZE/2 && (i==TABLESIZE/2-1 || i==TABLESIZE/2+1))) {
                   rectangle.setFill(Color.GREEN);
               }
               // A többi cella nem látható, megjelenítés miatt itt szükség van a magasság/szélesség megtartására még úgy is, hogy nem láthatóak
               else if(((i<TABLESIZE/2-1 && i > TABLESIZE/2-4) || (i > TABLESIZE/2+1 && i < TABLESIZE/2+4)) && ((j<TABLESIZE/2-1 && j > TABLESIZE/2-6) || (j > TABLESIZE/2+1 && j < TABLESIZE/2+6))) {
                   rectangle.setVisible(false);
                   rectangle.setDisable(true); 
               }
               // A többi cella nem látható, megjelenítés miatt itt nincs szükség van a magasság/szélesség megtartására
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
    
    // A lefordított kártyák inicializálása
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
    
    
    // A képernyő bal oldalán levő nevek, pontok, alattvalók számának inicializálása
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
    
    //Paramétertől függően letiltja vagy elérhetővé teszi a megfelelő elemeket
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
    
    // A balraforgatás gombra kattintás
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
    
    // A jobbraforgatás gombra kattintás
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
    
    // A balraforgatás megjelenítése a szerver műveletei után
    public void rotateLeftUpdate() {
        degree -= 90;
        imageView.setRotate(degree);
    }
    
    // A jobbraforgatás megjelenítése a szerver műveletei után
    public void rotateRightUpdate() {
        degree += 90;
        imageView.setRotate(degree);
    }
    
    // A szabálytalan elhelyezések letiltása és átszínezése
    public void illegalPlacesOnTableUpdate(Set<Point> illegalPoints) {
        removePreviousIllegalPlacesOnTable();
        for(Point p : illegalPoints) {
            centerRectangles[p.x][p.y].setDisable(true);
            centerRectangles[p.x][p.y].setFill(Color.ORANGE);
            forbiddenPlacesOnTheTable.add(p);
        }
    }
    // A régebbi letiltott helyek törlése, színűk és elérhetésük visszaállítása
    private void removePreviousIllegalPlacesOnTable() {
        for(Point p : forbiddenPlacesOnTheTable) {
            centerRectangles[p.x][p.y].setDisable(false);
            centerRectangles[p.x][p.y].setFill(Color.GREEN);
        }
        forbiddenPlacesOnTheTable.clear();
    }
    
    // Az asztalon levő egy szabályos cellára kattintás a kártya elhelyezése céljából
    private final EventHandler<MouseEvent> rectangleClickAction = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent t) {
            for(int i=0; i<TABLESIZE; i++) {
                for(int j=0; j<TABLESIZE; j++) {
                    if(centerRectangles[i][j] == t.getSource()) {
                        try {
                            int successLocate = delegate.locateLandTileOnTheTable(new Point(i,j));
                            // A kártyaelhelyezés sikerült, és van lehetőség alattvaló elhelyezésére 
                            if(successLocate == 2) {
                                delegate.openLocateFollowerWindow(imageView.getImage(), degree); 
                            //A kártyaelhelyezés sikerült, de nincs lehetőség alattvaló elhelyezésére
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
    
    // Az alattvalók elhelyezésének megjelenítése a szerveroldali változások után
    public void locateFollowerUpdate(int space, int colorNum) throws RemoteException {
        Circle circle = new Circle(8);
        // A kör megfelelő helyre tolása
        circle.setTranslateX(FOLLOWERDEFAULTPOINTPOSITION[space].x);
        circle.setTranslateY(FOLLOWERDEFAULTPOINTPOSITION[space].y);
        // A kör az alattvalót birtokló játékosnak megfelelő színű lesz
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
    
    // A körben levő sor alapján (mely a játékosok id-ja) visszaadja a játékos színét
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
    
    
    // A lefordított területkártyák egyikére kattintás
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
    
    // A kártyahúzás megjelenítése a szerveroldali műveletek elvégzése után
    public void chooseLandTileUpdate(Point p) throws RemoteException { 
        rightButtons[p.x][p.y].setDisable(true); // A kihúzott kártyalap gombja letiltottá válik
        drawnLandTiles.add(p);
        imageView.setImage(landTiles[p.x*5+p.y]); //Megjelenik a kártya felfordítva
        for(int i=0; i<TABLESIZE; i++) {
            for(int j=0; j<TABLESIZE; j++) {
                centerRectangles[i][j].setStroke(Color.BLACK);
                centerRectangles[i][j].setStrokeWidth(1);
            }
        }
        delegate.chooseFaceDownLandTileDone();
    }
    
    // A forgatás gombok elérhetővé tétele
    public void enableRotateButtons() {
        leftRotateButton.setDisable(false);
        rightRotateButton.setDisable(false);
    }
    
    // Warning message, ha többször akarna húzni egymás után a játékos
    public void chooseLandTileWarningMessage() {
        new WarningDialog("Hiba kártyahúzáskor", "Már húztál egy kártyát!");
    }
    
    
    //Information message, ha a kihúzott kártyát nem lehet elhelyezni
    public void landTileCantBeLocatednformationMessage() {
        ImageView img = new ImageView(imageView.getImage());
        img.setFitHeight(200);
        img.setFitWidth(200);
        
        new InformationDialog("A kártyát nem lehet elhelyezni", "Az általad kihúzott kártyát nem lehet elhelyezni a táblán. Továbbra is Te következel, húzz egy másikat!", img);
        
        removePreviousIllegalPlacesOnTable();
        imageView.setImage(new Image("/resources/images/empty.jpg"));
        imageView.setRotate(360);
    }
    
    
    // Kártyaelhelyezés megjelenítése, miután a szerver elvégezte a műveleteit
    public void locateLandTileUpdate(Point p) {
        actualLandTileTablePosition = p;
        removePreviousIllegalPlacesOnTable();
        centerRectangles[p.x][p.y].setFill(new ImagePattern(imageView.getImage())); // A cellán megjelenik a megfelelő kép
        centerRectangles[p.x][p.y].setDisable(true); // A cella letiltásra kerül
        centerRectangles[p.x][p.y].getTransforms().add(new Rotate(degree, 60, 60)); // A kártyát az előző forgatásnak megfelelően elforgatja
        centerRectangles[p.x][p.y].setStrokeWidth(2);
        centerRectangles[p.x][p.y].setStroke(Color.INDIANRED); // Az utolsóként elhelyezett kártya színe piros lesz
        expansionOfTheTable(p.x, p.y); //A tábla mérete nő
        try {
            delegate.locateLandTileDone();
        } catch (RemoteException ex) {
            System.err.println("Hiba a kártyaelhelyezés kész művelet szervernek való elküldésekor.");
        }
    }
    
    // Warning message, ha a játékos kártyahúzás előtt próbálna elhelyezni egy kártyát
    public void locateLandTileWarningMessage() {
        new WarningDialog("Hiba a kártya elhelyezésekor", "Még nem húztál kártyát!");
    }  
    
    // Az épp elhelyezett kártya melletti cellák megjelenítése
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
    
    // Az épp elhelyezett kártya melletti cellák megjelenítése
    private void setFieldVisibledOnTheTable(int i, int j) {
        centerRectangles[i][j].setVisible(true); //láthatóvá válik
        centerRectangles[i][j].setFill(Color.GREEN); //színe zöld lesz
        centerRectangles[i][j].setWidth(120);
        centerRectangles[i][j].setHeight(120);
    }
    
    // A megfelelő pontok megjelenítése a szerver számolása után
    public void countPointUpdate(int[] point) throws RemoteException {
        for(int i=0; i<points.length; i++) {
            points[i].setText(point[i] + " pont"); // A pontok megjelenítésének frissítése
        }
        imageView.setImage(new Image("/resources/images/empty.jpg")); // A kihúzott kártya helye ismét üres kép lesz
        imageView.setRotate(360); // A kihúzott kártya helyének visszaforgatása a kiinduló állapotba
        degree = 0;
        disableOrEnableEverything(true);
    }
    
    // A szerver záróértékelése után a pontok frissítése
    public void countPointEndOfTheGameUpdate(int[] point) {
        for(int i=0; i<points.length; i++) {
            int num = Integer.parseInt(points[i].getText().split("\\s+")[0]);
            points[i].setText((point[i] + num) + " pont"); // A pontok kiírásának frissítése
        }
        imageView.setImage(new Image("/resources/images/empty.jpg"));
        imageView.setRotate(360);
        degree = 0;
        disableOrEnableEverything(true);
    }
    
    // Az alattvalók számának frissítése
    public void followerNumberUpdate(int[] followerNumbers, List<Point> freeCircles) throws RemoteException {
        for(int i=0; i<followerNumbers.length; i++) {
            followers[i].setText(followerNumbers[i] + " alattvaló"); // Az alattvalók számának frissítése a kiíratásnál
        }
        // A felszabadult alattvalók színe halványabbá válik a kártyákon
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
        //delegate.nextPlayersTurn();
    }
    
    // A cella világosabb színűvé változik, ha főlé viszik az egeret
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
    
    // A cella eredeti színére vált, ha főlé az eget kiviszik felőle
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
    
    // Az egér kézzé változik, ha gomb fölé megy
    private final EventHandler<MouseEvent> buttonEnterAction = (MouseEvent event) -> {
        delegate.scene.setCursor(Cursor.HAND);
    };
    
    // Az egér visszaáll defult megjelenítésre, ha kimegy a gomb felől
    private final EventHandler<MouseEvent> buttonExitAction = (MouseEvent event) -> {
        delegate.scene.setCursor(Cursor.DEFAULT);
    };
    
    // Warning message, ha kilép valaki a játékból, visszalépés a menübe
    public void gameIsOverMessageUpdate() throws IOException {
        new WarningDialog("Játék vége", "Sajnos valaki elhagyta a játékot, így a játék véget ért.");
        delegate.endOfGameBecauseSomebodyQuittedClickOnOK();
    }
    
}