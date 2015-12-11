package view;

import controller.CommunicationController;
import java.awt.Point;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

//Az alattvalók elhelyezésének ablaka, dinamikus megjelenítés
public class FXMLLocateFollowerController implements Initializable {

    // Az alattvalók lehetséges elhelyezéseinek pontjai
    private final Point[] FOLLOWERDEFAULTPOINTPOSITION = new Point[]{new Point(-90, -70), new Point(-90, 0), new Point(-90, 70), new Point(-70, 90), new Point(0, 90), new Point(70, 90), new Point(90, 70), new Point(90, 0), new Point(90, -70), new Point(70, -90), new Point(0, -90), new Point(-70, -90), new Point(0, 0)};
    private List<Integer> positionsFromServer; // A szervertől kapott pontok, ahova alattvalót el lehet helyezni
    private Point[] followerPositions; //A megfelelő pontok alapján az elhelyezéseket tárolja
    private Circle[] circle; //Az alattvaló elhelyezésére szolgáló körök
    private int actualReservedPlace; //Az épp lefoglalt rész indexe
    private double degree;
    private Image image;
    private Stage stage;
    @FXML
    StackPane stackPane;
    @FXML
    protected ImageView imageView; // A kép megjelenésének helye
    @FXML
    protected Button locateButton; // Az alattvaló elhelyezése gomb
    @FXML
    protected Button skipButton; // A kihagyás gomb

    public CommunicationController delegate;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        actualReservedPlace = -1;
        try {
            positionsFromServer = delegate.getFollowerPoints();
            initActualFollowerPositions(positionsFromServer);
        } catch (RemoteException ex) {
            System.err.println("Hiba az alattvalók lehetséges elhelyezésének betöltésekor.");
        }

        imageView.setRotate(degree); // Elforgatja a képet a forgatásoknak megfelelően
        imageView.setImage(image);

        initFollowerPoints();

        locateButton.setOnMouseClicked(locateAction);
        skipButton.setOnMouseClicked(skipAction);
        locateButton.setOnMouseEntered(enterAction);
        skipButton.setOnMouseEntered(enterAction);
        locateButton.setOnMouseExited(exitAction);
        skipButton.setOnMouseExited(exitAction);
    }

    // Meghatározza a szervertől kapott pontok alapján a megjelenítés szempontjából ezeknek megfelelő helyeket
    private void initActualFollowerPositions(List<Integer> positions) {
        followerPositions = new Point[positions.size()];
        for (int i = 0; i < positions.size(); i++) {
            followerPositions[i] = FOLLOWERDEFAULTPOINTPOSITION[positions.get(i)];
        }
    }

    //A megjelenő körök inicializálása
    private void initFollowerPoints() {
        circle = new Circle[followerPositions.length];
        for (int i = 0; i < followerPositions.length; i++) {
            circle[i] = new Circle(9);
            circle[i].setTranslateX(followerPositions[i].x);
            circle[i].setTranslateY(followerPositions[i].y);
            circle[i].setFill(Color.SLATEGREY);
            circle[i].setOnMouseClicked(circleClickAction);
            circle[i].setOnMouseEntered(circleEnterAction);
            circle[i].setOnMouseExited(circleExitAction);
            stackPane.getChildren().add(circle[i]);
        }
    }

    // A körre kattintáskor kiváltódó függvény
    private final EventHandler<MouseEvent> circleClickAction = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent t) {
            for (int i = 0; i < circle.length; i++) {
                circle[i].setFill(Color.SLATEGREY); // Az összes kör színét először szürkére állítja (egyszerre csak egy lehet kijelölve)
                circle[i].setDisable(false); // Az összes kör elérhetővé válik
            }
            for (int i = 0; i < circle.length; i++) {
                if (circle[i] == t.getSource()) {
                    circle[i].setFill(Color.LIGHTGREEN); // A kiválasztott kör zölddé változik
                    circle[i].setDisable(true); // A kiválasztott kör elérését letiltja
                    actualReservedPlace = positionsFromServer.get(i); //beállítja aktuálisan foglaltnak a pozíció indexét
                }
            }
            locateButton.setDisable(false); // Az alattvaló elhelyezése gomb elérhetővé válik
        }
    };

    // Effekt a körre, ha az egér fölémegy; az egér kéz formájú lesz
    private final EventHandler<MouseEvent> circleEnterAction = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent t) {
            
            for (int i = 0; i < circle.length; i++) {
                if (circle[i] == t.getSource()) {
                    stage.getScene().setCursor(Cursor.HAND);
                    circle[i].setEffect(new Glow());
                }
            }
        }
    };

    // Effekt törlése a körről, ha az egér elmegy róla; az egér ismét default formájú lesz
    private final EventHandler<MouseEvent> circleExitAction = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent t) {   
            for (int i = 0; i < circle.length; i++) {
                if (circle[i] == t.getSource()) {
                    stage.getScene().setCursor(Cursor.DEFAULT);
                    circle[i].setEffect(null);
                }
            }
        }
    };

    // Az alattvaló elhelyezése gombra kattintás
    private final EventHandler<MouseEvent> locateAction = new EventHandler<MouseEvent>() {

        @Override
        public void handle(MouseEvent event) {
            try {
                delegate.clickLocateFollowerAction(actualReservedPlace);
            } catch (RemoteException ex) {
                System.err.println("Hiba az alattvaló elhelyezésekor!");
            }
        }

    };

    // A kihagyás gombra kattintás
    private final EventHandler<MouseEvent> skipAction = (MouseEvent event) -> {
        delegate.clickSkipAction();
    };
    
    // Ha gombok fölé megy az egér, ilyenkor a kurzor kéz formájú lesz
    private final EventHandler<MouseEvent> enterAction = (MouseEvent event) -> {
        stage.getScene().setCursor(Cursor.HAND);
    };
    
    // Ha gombok fölül kimegy az egér, ilyenkor a kurzor ismét default formájú lesz
    private final EventHandler<MouseEvent> exitAction = (MouseEvent event) -> {
        stage.getScene().setCursor(Cursor.DEFAULT);
    };
    
    // Az X gombra kattintás, ugyanolyan, mintha a kihagyás gombra kattintana a játékos
    private final EventHandler<WindowEvent> closeWindowAction = new EventHandler<WindowEvent>() {

        @Override
        public void handle(WindowEvent event) {
                delegate.clickSkipAction();
        }

    };
  
    public FXMLLocateFollowerController(double degree, Image image, Stage stage) {
        this.image = image;
        this.degree = degree;
        this.stage = stage;

        stage.setOnCloseRequest(closeWindowAction);
    }

}
