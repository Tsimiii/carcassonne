package controller;

import view.FXMLMenuController;
import view.FXMLGameController;
import carcassonneshared.RemoteObserver;
import carcassonneshared.RmiService;
import java.awt.Point;
import java.io.IOException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import view.FXMLLoadingScreenController;
import view.FXMLLocateFollowerController;
import view.imageloader.LandTileImageLoader;

public class CommunicationController extends UnicastRemoteObject implements RemoteObserver {

    private RmiService remoteService;
    private FXMLGameController gameController;
    private FXMLLocateFollowerController locateFollowerController;
    private FXMLLoadingScreenController loadingScreenController = new FXMLLoadingScreenController();

    private Stage stage;
    private Image img;
    private double degree;
    private String name;

    public Scene scene;

    public CommunicationController(Scene scene) throws RemoteException, IOException {
        super();
        this.scene = scene;
        callMenu();
    }

    private void callMenu() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/fxml_carcassonne_menu.fxml"));
        scene.setRoot(loader.load());
        FXMLMenuController controller = (FXMLMenuController) loader.getController();
        controller.delegate = this;
    }

    @Override
    public void update(Object observable, Object updateMsg) throws RemoteException {
        if(updateMsg instanceof Object[] && ((Object[]) updateMsg)[0].equals("timer")) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    loadingScreenController.setTimer(((Object[])updateMsg)[1].toString());
                }
            });
        }
        else if (updateMsg instanceof int[]) {
            int[] shuffledIdArray = (int[]) updateMsg;
            try {
                LandTileImageLoader landTileImageLoader = LandTileImageLoader.getInstance();
                landTileImageLoader.init(shuffledIdArray);
            } catch (IOException ex) {
                System.err.println("A területkártyák képeinek betöltése sikertelen!");
            }
        } else if (updateMsg instanceof Object[] && ((Object[])updateMsg)[0].equals("startgame")) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    startGame((int)(((Object[])updateMsg)[1]), (List<String>)(((Object[])updateMsg)[2]));
                }
            });
        } else if (updateMsg instanceof Point && ((Point) updateMsg).x > -1) {
            gameController.chooseLandTileUpdate((Point) updateMsg);
        } else if (updateMsg.equals("successRotateLeft")) {
            gameController.rotateLeftUpdate();
        } else if (updateMsg.equals("successRotateRight")) {
            gameController.rotateRightUpdate();
        } else if (updateMsg instanceof Set<?>) {
            gameController.illegalPlacesOnTableUpdate((Set<Point>) updateMsg);
        } else if (updateMsg instanceof Object[] && ((Object[]) updateMsg)[0].equals("locateLandTile")) {
            gameController.locateLandTileUpdate((Point) ((Object[]) updateMsg)[1]);
        } else if (updateMsg instanceof Object[] && ((Object[]) updateMsg)[0].equals("locateFollower")) {
            gameController.locateFollowerUpdate((int) ((Object[]) updateMsg)[1], (int) ((Object[]) updateMsg)[2]);
        } else if (updateMsg instanceof Object[] && ((Object[]) updateMsg)[0].equals("countPoint")) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    gameController.countPointUpdate((int[]) ((Object[]) updateMsg)[1]);
                }
            });
        } else if (updateMsg instanceof Object[] && ((Object[]) updateMsg)[0].equals("getFollowerNumber")) {
             Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        gameController.followerNumberUpdate((int[]) ((Object[]) updateMsg)[1], (List<Point>) ((Object[]) updateMsg)[2]);
                    } catch (RemoteException ex) {
                        Logger.getLogger(CommunicationController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
        }else if (updateMsg.equals("YourTurn")) {
            System.out.println(updateMsg);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    gameController.enableEverythingUpdate();
                }
            });
        } else if (updateMsg.equals("rotateButtonEnabled")) {
            gameController.enableRotateButtons();
        }
    }

    public void clickJoinGame(String name) {
        try {
            this.name = name;
            remoteService = (RmiService) Naming.lookup("//localhost:8080/carcassonneServer");
            remoteService.addObserver(this, name);
            displayLoadingScreen();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void displayLoadingScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/fxml_carcassonne_loading_screen.fxml"));
            //loadingScreenController = new FXMLLoadingScreenController();
            loader.setController(loadingScreenController);
            scene.setRoot(loader.load());
        } catch (IOException ex) {
            Logger.getLogger(CommunicationController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void startGame(int id, List<String> names) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/fxml_carcassonne_game.fxml"));
            
            gameController = new FXMLGameController(id, names);
            loader.setController(gameController);
            gameController.delegate = this;
            
            scene.setRoot(loader.load());
           /* gameController = loader.<FXMLGameController>getController();
            gameController.delegate = this;*/
        } catch (IOException ex) {
            System.err.println("Nem sikerült betölteni az fxml-t!");
        }
    }

    public void openLocateFollowerWindow(Image img, double degree) {
        this.img = img;
        this.degree = degree;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/fxml_carcassonne_locate_follower.fxml"));
        try {
            stage = new Stage();
            stage.setTitle("Alattvaló elhelyezése");

            locateFollowerController = new FXMLLocateFollowerController(degree, img, stage);
            loader.setController(locateFollowerController);
            locateFollowerController.delegate = this;

            Scene scene = new Scene(loader.load(), 500, 500);
            stage.setScene(scene);
            stage.show();

        } catch (IOException ex) {
            System.err.println("Nem sikerült betölteni az fxml-t!");
        }
    }

    public void chooseFaceDownLandTile(Point p) throws RemoteException {
        String chooseInformation = remoteService.chooseFaceDownLandTile(p);
        if (chooseInformation.equals("multipleChoose")) {
            gameController.chooseLandTileWarningMessage();
        } else if (chooseInformation.equals("cantBeLocated")) {
            gameController.landTileCantBeLocatednformationMessage();
        }
    }
    
    public void chooseFaceDownLandTileDone() throws RemoteException {
        remoteService.chooseFaceDownLandTileDone();
    }

    public void clickRotateLeft() throws RemoteException {
        remoteService.rotateLeftLandTile();
    }

    public void clickRotateRight() throws RemoteException {
        remoteService.rotateRightLandTile();
    }

    public int locateLandTileOnTheTable(Point p) throws RemoteException {
        int successLocate = remoteService.locateLandTileOnTheTable(p);
        if (successLocate == 0) {
            gameController.locateLandTileWarningMessage();
        }
        return successLocate;
    }

    public List<Integer> getFollowerPoints() throws RemoteException {
        List<Integer> followerPoints = remoteService.getFollowerPointsOfActualLandTile();
        return followerPoints;
    }

    public void clickSkipAction() {
        stage.close();
        try {
            countPoints();
        } catch (RemoteException ex) {
            Logger.getLogger(CommunicationController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void clickLocateFollowerAction(int reservedPlace) throws RemoteException {
        stage.close();
        remoteService.locateFollower(reservedPlace);
        countPoints();
    }

    public void countPoints() throws RemoteException {
        remoteService.countPoints();
    }
    
    public void nextPlayersTurn() throws RemoteException {
        remoteService.whosTurnIsIt();
    }

    public Image getImg() {
        return img;
    }

    public double getDegree() {
        return degree;
    }

}
