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
import javafx.scene.Scene;
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

public class FXMLLocateFollowerController implements Initializable {

    private final Point[] FOLLOWERDEFAULTPOINTPOSITION = new Point[]{new Point(-90, -70), new Point(-90, 0), new Point(-90, 70), new Point(-70, 90), new Point(0, 90), new Point(70, 90), new Point(90, 70), new Point(90, 0), new Point(90, -70), new Point(70, -90), new Point(0, -90), new Point(-70, -90), new Point(0, 0)};
    private List<Integer> positionsFromServer;
    private Point[] followerPositions;
    private Circle[] circle;
    private int actualReservedPlace;
    private double degree;
    private Image image;
    private Stage stage;
    @FXML
    StackPane stackPane;
    @FXML
    protected ImageView imageView;
    @FXML
    protected Button locateButton;
    @FXML
    protected Button skipButton;

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

        imageView.setRotate(degree);
        imageView.setImage(image);

        initFollowerPoints();

        locateButton.setOnMouseClicked(locateAction);
        skipButton.setOnMouseClicked(skipAction);
        locateButton.setOnMouseEntered(enterAction);
        skipButton.setOnMouseEntered(enterAction);
        locateButton.setOnMouseExited(exitAction);
        skipButton.setOnMouseExited(exitAction);
    }

    private void initActualFollowerPositions(List<Integer> positions) {
        followerPositions = new Point[positions.size()];
        for (int i = 0; i < positions.size(); i++) {
            followerPositions[i] = FOLLOWERDEFAULTPOINTPOSITION[positions.get(i)];
        }
    }

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

    private final EventHandler<MouseEvent> circleClickAction = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent t) {
            for (int i = 0; i < circle.length; i++) {
                circle[i].setFill(Color.SLATEGREY);
                circle[i].setDisable(false);
            }
            for (int i = 0; i < circle.length; i++) {
                if (circle[i] == t.getSource()) {
                    circle[i].setFill(Color.LIGHTGREEN);
                    circle[i].setDisable(true);
                    actualReservedPlace = positionsFromServer.get(i);
                }
            }
            locateButton.setDisable(false);
        }
    };

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

    private final EventHandler<MouseEvent> skipAction = (MouseEvent event) -> {
        delegate.clickSkipAction();
    };
    
    private final EventHandler<MouseEvent> enterAction = (MouseEvent event) -> {
        stage.getScene().setCursor(Cursor.HAND);
    };
    
    private final EventHandler<MouseEvent> exitAction = (MouseEvent event) -> {
        stage.getScene().setCursor(Cursor.DEFAULT);
    };
    
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
