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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import view.LoadingScreen;
import view.imageloader.LandTileImageLoader;

public class CommunicationController extends UnicastRemoteObject implements RemoteObserver {

    private RmiService remoteService;
    private FXMLGameController gameController;
    
    private LoadingScreen loadingScreen;
    public Scene scene;

    public CommunicationController(Scene scene) throws RemoteException, IOException {
        super();
        this.scene = scene;
        callMenu();
    }

    private void callMenu() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/resources/fxml_carcassonne_menu.fxml"));
        scene.setRoot(loader.load());
        FXMLMenuController controller = (FXMLMenuController) loader.getController();
        controller.delegate = this;
    }

    @Override
    public void update(Object observable, Object updateMsg) throws RemoteException {
        if(updateMsg instanceof int[]) {
            int[] shuffledIdArray = (int[])updateMsg;
            try {
                LandTileImageLoader landTileImageLoader = LandTileImageLoader.getInstance();
                landTileImageLoader.init(shuffledIdArray);
            } catch (IOException ex) {
                System.err.println("A területkártyák képeinek betöltése sikertelen!");
            }
        }
        else if (updateMsg.equals("startgame")) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    startGame();
                }
            });
        } else if(((Point)updateMsg).x > -1) {
            gameController.chooseLandTileUpdate((Point)updateMsg);
        }
    }

    public void clickJoinGame(String name) {
        try {
            remoteService = (RmiService) Naming.lookup("//localhost:8080/carcassonneServer");
            remoteService.addObserver(this);
            displayLoadingScreen();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public void displayLoadingScreen() {
        loadingScreen = new LoadingScreen();
        scene.setRoot(loadingScreen);
    }

    private void startGame() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/resources/fxml_carcassonne_game.fxml"));
            scene.setRoot(loader.load());
            gameController = loader.<FXMLGameController>getController();
            gameController.delegate = this;
        } catch (IOException ex) {
            System.err.println("Nem sikerült betölteni az fxml-t!");
        }
    }
    
    public void chooseFaceDownLandTile(Point p) throws RemoteException {
        boolean successChoose = remoteService.chooseFaceDownLandTile(p);
        if(!successChoose) {
            gameController.chooseLandTileWarningMessage();
        }
    }

}
