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
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import view.CarcassonneGameView;
import view.LoadingScreen;

public class CommunicationController extends UnicastRemoteObject implements RemoteObserver {

    private RmiService remoteService;
    
    private LoadingScreen loadingScreen;
    private CarcassonneGameView carcassonneGameView;
    public Scene scene;

    public CommunicationController(Scene scene) throws RemoteException, IOException {
        super();

        this.scene = scene;
        /*centerHeight = 21;
         centerWidth = 21;
         rightHeight = 18;
         rightWidth = 4;   */

        callMenu();

        /*mainMenuView = new MainMenuView(scene.getWidth(), scene.getHeight());
         mainMenuView.delegate = this;
         scene.setRoot(mainMenuView);*/
    }

    private void callMenu() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/resources/fxml_carcassonne_menu.fxml"));
        scene.setRoot(loader.load());
        FXMLMenuController controller = (FXMLMenuController) loader.getController();
        controller.delegate = this;
    }

    @Override
    public void update(Object observable, Object updateMsg) throws RemoteException {
        if (updateMsg.equals("startgame")) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    startGame();
                }
            });
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
            FXMLGameController controller = loader.<FXMLGameController>getController();
            controller.delegate = this;
        } catch (IOException ex) {
            System.err.println("Nem sikerült betölteni az fxml-t!");
        }
    }
    
    public void chooseFaceDownLandTile(int index) throws RemoteException {
        remoteService.chooseFaceDownLandTile(index);
    }

}
