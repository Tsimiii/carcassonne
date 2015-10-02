package controller;

import carcassonneshared.RemoteObserver;
import carcassonneshared.RmiService;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import javafx.scene.Scene;
import view.CarcassonneGameView;
import view.LoadingScreen;
/*import model.Model;
 import view.GameView;*/
import view.MainMenuView;
//import view.ViewDelegate;

public class Controller extends UnicastRemoteObject implements RemoteObserver {

    private MainMenuView mainMenuView;
    private LoadingScreen loadingScreen;
    private CarcassonneGameView carcassonneGameView;
    /* private GameView gameview;
     private Model model;*/
    public Scene scene;

    /*private int centerHeight;
     private int centerWidth;
     private int rightHeight;
     private int rightWidth;*/
    public Controller(Scene scene) throws RemoteException {
        super();

        this.scene = scene;
        /*centerHeight = 21;
         centerWidth = 21;
         rightHeight = 18;
         rightWidth = 4;   */
        mainMenuView = new MainMenuView(scene.getWidth(), scene.getHeight());
        mainMenuView.delegate = this;
        scene.setRoot(mainMenuView);

    }

    @Override
    public void update(Object observable, Object updateMsg) throws RemoteException {
        System.out.println(updateMsg);
    }

    public void clickJoinGame(String name) {
        try {
            RmiService remoteService = (RmiService) Naming.lookup("//localhost:8080/carcassonneServer");
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
}
