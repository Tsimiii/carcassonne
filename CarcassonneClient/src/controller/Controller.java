package controller;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.Scene;
import view.CarcassonneGameView;
import view.LoadingScreen;
/*import model.Model;
 import view.GameView;*/
import view.MainMenuView;
import view.MenuDelegate;
//import view.ViewDelegate;

public class Controller implements MenuDelegate {

    private MainMenuView mainMenuView;
    private LoadingScreen loadingScreen;
    private CarcassonneGameView carcassonneGameView;
    private Socket client = null;
    /* private GameView gameview;
     private Model model;*/
    public Scene scene;

    /*private int centerHeight;
     private int centerWidth;
     private int rightHeight;
     private int rightWidth;*/
    public Controller(Scene scene) {
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
    public void joinGame(String name) {
        try {
            client = new Socket("localhost", 8080);
        } catch (IOException ex) {
        }

        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(client.getOutputStream());
            oos.writeObject(name);
            oos.flush();
        } catch (FileNotFoundException ex) {
            // complain to user
        } catch (IOException ex) {
            // notify user
        }
    }

    @Override
    public void displayLoadingScreen() {
        myRunnable runnable = new myRunnable(client);
        runnable.delegate = this;
        Thread myThread = new Thread(runnable);
        myThread.start();
        loadingScreen = new LoadingScreen();
        scene.setRoot(loadingScreen);
    }

    @Override
    public void startGame() {
        carcassonneGameView = new CarcassonneGameView();
        carcassonneGameView.delegate = this;
        scene.setRoot(carcassonneGameView);
    }
}

class myRunnable extends Thread {

    public Controller delegate;
    private Socket socket;

    public myRunnable(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        InputStream is = null;
        try {
            is = socket.getInputStream();
        } catch (IOException ex) {
            Logger.getLogger(myRunnable.class.getName()).log(Level.SEVERE, null, ex);
        }
        BufferedReader ois = new BufferedReader(new InputStreamReader(is));
        while (true) {
            try {
                String msg = ois.readLine();
                if (msg.equals("kesz")) {
                    delegate.startGame();
                    break;
                }
            } catch (IOException ex) {
            }
        }
    }
}
