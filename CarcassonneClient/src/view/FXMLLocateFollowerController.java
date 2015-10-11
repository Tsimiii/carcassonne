package view;

import controller.CommunicationController;
import java.awt.Point;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ResourceBundle;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class FXMLLocateFollowerController implements Initializable {
    
    private final Point[] FOLLOWERDEFAULTPOINTPOSITION = new Point[] {new Point(-90,-70), new Point(-90, 0), new Point(-90, 70), new Point(-70,90), new Point(0, 90), new Point(70,90), new Point(90, 70), new Point(90, 0), new Point(90, -70), new Point(70, -90), new Point(0, -90), new Point(-70, -90), new Point(0, 0)};;
    private Point[] followerPositions;
    private Circle[] circle;
    private double degree;
    private Image image;
    @FXML StackPane stackPane;
    @FXML protected ImageView imageView;
    @FXML protected Button locateButton;
    @FXML protected Button skipButton;
    
    public CommunicationController delegate;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            initActualFollowerPositions(delegate.getFollowerPoints());
        } catch (RemoteException ex) {
            System.err.println("Hiba az alattvalók lehetséges elhelyezésének betöltésekor.");
        }
        
        imageView.setRotate(degree);
        imageView.setImage(image);
        
        initFollowerPoints();
        
        locateButton.setOnMouseClicked(locateAction);
        skipButton.setOnMouseClicked(skipAction);
    }
    
    private void initActualFollowerPositions(int[] positions) {
        followerPositions = new Point[positions.length];
        for(int i=0; i<positions.length; i++) {
            followerPositions[i] = FOLLOWERDEFAULTPOINTPOSITION[positions[i]];
        }
    }
    
    private void initFollowerPoints() {
        circle = new Circle[followerPositions.length];
        for(int i=0; i<followerPositions.length; i++) {
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
            for(int i=0; i<circle.length; i++) {
                circle[i].setFill(Color.SLATEGREY);
                circle[i].setDisable(false);
            }   
            for(int i=0; i<circle.length; i++) {
                if(circle[i] == t.getSource()) {
                    circle[i].setFill(Color.LIGHTGREEN);
                    circle[i].setDisable(true);
                   /* actualReservedPlaces = tempL.get(i);
                    actualPositionOfCircle = tempPositionOfCircles[i];*/
                }
            }
            locateButton.setDisable(false);
        }
    };
    
        private final EventHandler<MouseEvent> circleEnterAction = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent t) {
            for(int i=0; i<circle.length; i++) {
                if(circle[i] == t.getSource()) {
                    circle[i].setEffect(new Glow());
                }
            }
        }
    };
    
    private final EventHandler<MouseEvent> circleExitAction = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent t) {
            for(int i=0; i<circle.length; i++) {
                if(circle[i] == t.getSource()) {
                    circle[i].setEffect(null);
                }
            }
        }
    };
    
    private final EventHandler<MouseEvent> locateAction = new EventHandler<MouseEvent>() {

        @Override
        public void handle(MouseEvent event) {
            delegate.clickLocateFollowerAction();
        }
        
    };
    
    private final EventHandler<MouseEvent> skipAction = new EventHandler<MouseEvent>() {

        @Override
        public void handle(MouseEvent event) {
            delegate.clickSkipAction();
        }
        
    };
    
    public FXMLLocateFollowerController(double degree, Image image) {
        this.image = image;
        this.degree = degree;
    }
    
}
