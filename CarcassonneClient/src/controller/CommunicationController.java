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
import java.util.Optional;
import java.util.Set;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import view.FXMLLoadingScreenController;
import view.FXMLLocateFollowerController;
import view.FXMLResultScreenController;
import view.imageloader.LandTileImageLoader;

public class CommunicationController extends UnicastRemoteObject implements RemoteObserver {

    private RmiService remoteService; //A CarcassonneShared interfészének példánya
    private FXMLGameController gameController; //A játék kontroller példánya
    private FXMLLocateFollowerController locateFollowerController; //Az alattvaló elhelyezése kontroller példánya
    private FXMLLoadingScreenController loadingScreenController = new FXMLLoadingScreenController(); //a betöltő felület kontrollerének példánya
    private FXMLResultScreenController resultScreenController; //Az eredményhirdető ablak kontrollerének példánya

    private Stage stage;
    private Image img;
    private double degree;
    private String name;
    private String chooseInformation = null;

    public int port;
    public Scene scene;

    public CommunicationController(Scene scene) throws RemoteException, IOException {
        super();
        this.scene = scene;
        callMenu(); //meghívja a menüt
    }

    // A menü betöltése
    private void callMenu() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/fxml_carcassonne_menu.fxml"));
        scene.setRoot(loader.load());
        FXMLMenuController controller = (FXMLMenuController) loader.getController();
        controller.delegate = this;
    }

    // A szerver által az összes kliensnek küldött üzenetek fogadása
    @Override
    public void update(Object observable, Object updateMsg) throws RemoteException {
        if(updateMsg instanceof Object[] && ((Object[]) updateMsg)[0].equals("timer")) { // Az aktuális másodperc fogadása a betöltő felülethez
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    loadingScreenController.setTimer(((Object[])updateMsg)[1]);
                }
            });
        }
        else if(updateMsg instanceof Object[] && ((Object[]) updateMsg)[0].equals("timer2")) { // A kiírása fogadása a betöltő felülethez, ha csatlakozott elég játékos
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    loadingScreenController.setEnoughJoinText(((Object[])updateMsg)[1].toString());
                }
            });
        }
        else if (updateMsg instanceof int[]) { // Az összekevert kártyaid-k fogadása
            int[] shuffledIdArray = (int[]) updateMsg;
            try {
                LandTileImageLoader landTileImageLoader = LandTileImageLoader.getInstance();
                landTileImageLoader.init(shuffledIdArray);
            } catch (IOException ex) {
                System.err.println("A területkártyák képeinek betöltése sikertelen!");
            }
        } else if (updateMsg instanceof Object[] && ((Object[])updateMsg)[0].equals("startgame")) { //A játék kezdetének utasítását fogadja
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    startGame((int)(((Object[])updateMsg)[1]), (List<String>)(((Object[])updateMsg)[2]));
                }
            });
        } else if (updateMsg instanceof Point && ((Point) updateMsg).x > -1) { // A kártyahúzás utáni változás fogadása
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    try { 
                        gameController.chooseLandTileUpdate((Point) updateMsg);
                    } catch (RemoteException ex) {
                        System.err.println("Hiba a kártyahúzás frissítésekor kliensoldalon.");
                    }
                }
            });
        } else if (updateMsg.equals("successRotateLeft")) { // A balra forgatás utáni változás fogadása
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    gameController.rotateLeftUpdate();
                }
            });
        } else if (updateMsg.equals("successRotateRight")) { // A jobbra forgatás utáni változás fogadása
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    gameController.rotateRightUpdate();
                }
            });
        } else if (updateMsg instanceof Set<?>) { // A letiltott mezők pozíciójának fogadása
            gameController.illegalPlacesOnTableUpdate((Set<Point>) updateMsg);
        } else if (updateMsg instanceof Object[] && ((Object[]) updateMsg)[0].equals("locateLandTile")) { // A kártya elhelyezése utáni változás fogadása
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    gameController.locateLandTileUpdate((Point) ((Object[]) updateMsg)[1]);
                }
            });
        } else if (updateMsg instanceof Object[] && ((Object[]) updateMsg)[0].equals("locateFollower")) { // Az alattvaló elhelyezése utáni változás fogadása
            gameController.locateFollowerUpdate((int) ((Object[]) updateMsg)[1], (int) ((Object[]) updateMsg)[2]);
        } else if (updateMsg instanceof Object[] && ((Object[]) updateMsg)[0].equals("countPoint")) { // A pontszámítás utáni változás fogadása
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        gameController.countPointUpdate((int[]) ((Object[]) updateMsg)[1]);
                    } catch (RemoteException ex) {
                        System.err.println("A játék közbeni pontszámok frissítésekor hiba történt a kliensoldalon.");
                    }
                }
            });
        } else if (updateMsg instanceof Object[] && ((Object[]) updateMsg)[0].equals("getFollowerNumber")) { // Az alattvalók számának fogadása
             Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        gameController.followerNumberUpdate((int[]) ((Object[]) updateMsg)[1], (List<Point>) ((Object[]) updateMsg)[2]);
                    } catch (RemoteException ex) {
                        System.err.println("Az alattvalók számának frissítésekor hiba történt a kliensoldalon.");
                    }
                }
            });
        }else if (updateMsg.equals("YourTurn")) { // Fogadja, hogy melyik játékos következik épp
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    gameController.enableEverythingUpdate();
                }
            });
        } else if (updateMsg.equals("rotateButtonEnabled")) { // A forgatás gombok elérhetővé tételének utasítását fogadja
            gameController.enableRotateButtons();
        } else if(updateMsg instanceof Object[] && ((Object[]) updateMsg)[0].equals("countPointEndOfTheGame")) { // A záróértékelés eredményének fogadása
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    gameController.countPointEndOfTheGameUpdate((int[]) ((Object[]) updateMsg)[1]);
                }
            });
        } else if(updateMsg instanceof Object[] && ((Object[]) updateMsg)[0].equals("sortedPoints")) { // A végső pontok fogadása a pontszám szerint növekvő sorrendben
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    displayResultScreen((List<Point>) ((Object[]) updateMsg)[1], (List<String>)((Object[]) updateMsg)[2]);
                }
            });
        } else if(updateMsg.equals("gameIsOver")) { // A játék hirtelen végét jelző üzenet, amikor valaki játék közben kilépett a játékból
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        gameController.gameIsOverMessageUpdate();
                    } catch (IOException ex) {
                        System.err.println("Valaki kilépett a játékból, de ennek kliensoldali közvetítése sikertelen.");
                    }
                }
            });
        }
    }

    // A játékohoz való csatlakozásra kattintás
    public void clickJoinGame(String name) {        
            try {
                this.name = name;
                remoteService = (RmiService) Naming.lookup("//localhost:" + port + "/carcassonneServer"); // A távoli objektumhoz csatlakozás
                remoteService.addObserver(this, name); // A kliens becsatlakoztatása
                displayLoadingScreen(); // A betöltő felület meghívása
            } catch (Exception ex) {
                System.err.println("Hiba a távoli objektumhoz való csatlakozáskor kliensoldalon.");
            }
    }
    
    // Az alkalmazásból való kilépés
    public void clickExitAction() {
         System.exit(0);
    }
    
    // A Vissza a menühöz gombra kattintás az eredményhirdető ablakon
    public void clickBactToMainMenuAction() throws IOException {
         stage.close();
         callMenu();
    }

    // Megjeleníti a betöltő felületet
    public void displayLoadingScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/fxml_carcassonne_loading_screen.fxml")); // A betöltő felület betöltése
            loader.setController(loadingScreenController);
            scene.setRoot(loader.load());
        } catch (IOException ex) {
            System.err.println("Nem sikerült betölteni a betöltő felület fxml-jét.");
        }
    }

    // A játék megjelenítésének betöltése
    private void startGame(int id, List<String> names) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/fxml_carcassonne_game.fxml")); // Az fxml betöltése
            
            gameController = new FXMLGameController(id, names);
            loader.setController(gameController);
            gameController.delegate = this;
            
            scene.setRoot(loader.load());

        } catch (IOException ex) {
            System.err.println("Nem sikerült betölteni a játék fxml-jét!");
        }
    }

    // Az alattvalók elhelyezésére szolgáló ablak megnyitása
    public void openLocateFollowerWindow(Image img, double degree) {
        
        this.img = img;
        this.degree = degree;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/fxml_carcassonne_locate_follower.fxml")); // Az fxml betöltése
        try {
            stage = new Stage(); //Új ablak nyitása
            stage.setTitle("Alattvaló elhelyezése");

            locateFollowerController = new FXMLLocateFollowerController(degree, img, stage);
            loader.setController(locateFollowerController);
            locateFollowerController.delegate = this;

            Scene scene = new Scene(loader.load(), 500, 500);
            scene.getStylesheets().add(this.getClass().getResource("/resources/css/style.css").toExternalForm());
            stage.setScene(scene);
            stage.show();

        } catch (IOException ex) {
            System.err.println("Nem sikerült betölteni az alattvalók elhelyezése fxml-t!");
        }
    }

    // A kártyahúzással kapcsolatos szerver-kliens kommunikáció
    public void chooseFaceDownLandTile(Point p) throws RemoteException {
        chooseInformation = remoteService.chooseFaceDownLandTile(p);
        if (chooseInformation != null && chooseInformation.equals("multipleChoose")) {
            gameController.chooseLandTileWarningMessage();
        }
    }
    // A kártyaelhelyezés sikerességét jelzi a szervernek
    public void chooseFaceDownLandTileDone() throws RemoteException {
        if (chooseInformation != null && chooseInformation.equals("cantBeLocated")) {
            gameController.landTileCantBeLocatednformationMessage();
        }
        remoteService.chooseFaceDownLandTileDone();
    }

    // A jobbra forgatás gombra kattintás - kérés a szerverhez
    public void clickRotateLeft() throws RemoteException {
        remoteService.rotateLeftLandTile();
    }

    // A balra forgatás gombra kattintás - kérés a szerverhez
    public void clickRotateRight() throws RemoteException {
        remoteService.rotateRightLandTile();
    }

    // A kártya asztalon való elhelyezésének szerver-kliens kommunikációja
    public int locateLandTileOnTheTable(Point p) throws RemoteException {
        int successLocate = remoteService.locateLandTileOnTheTable(p);
        if (successLocate == 0) {
            gameController.locateLandTileWarningMessage();
        }
        return successLocate;
    }
    
    // A kártya sikeres elhelyezését elküldi a szervernek
    public void locateLandTileDone() throws RemoteException {
        remoteService.locateLandTileDone();
    }

    // Lekéri a szervertől a az alattvalók elhelyezésének lehetséges helyeit
    public List<Integer> getFollowerPoints() throws RemoteException {
        List<Integer> followerPoints = remoteService.getFollowerPointsOfActualLandTile();
        return followerPoints;
    }

    // Az alattvaló elhelyezése ablakon a kihagyás gombra kattintás
    public void clickSkipAction() {
        stage.close();
        try {
            countPoints();
        } catch (RemoteException ex) {
            System.err.println("Hiba a lépések közötti pontszám lekérésekor kliensoldalon.");
        }
    }

    // Az alattvaló elhelyezése ablakon az alattvaló elhelyezése gombra kattintás
    public void clickLocateFollowerAction(int reservedPlace) throws RemoteException {
        stage.close();
        remoteService.locateFollower(reservedPlace);
        countPoints();
    }

    // A játék közbeni pontszámítás lekérése a szervertől
    public void countPoints() throws RemoteException {
        remoteService.countPoints();
        nextPlayersTurn();
    }
    
    // A következő játékos lekérése a szervertől
    public void nextPlayersTurn() throws RemoteException {
        remoteService.whosTurnIsIt();
    }

    public Image getImg() {
        return img;
    }

    public double getDegree() {
        return degree;
    }
    
    
    // Az eredményhirdető ablak megjelenítése
    private void displayResultScreen(List<Point> list, List<String> names) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/fxml_result_screen.fxml")); // Az eredményhirdető fxml betöltése
        try {
            stage = new Stage();
            stage.setTitle("Eredmények");

            resultScreenController = new FXMLResultScreenController(list, names, stage);
            loader.setController(resultScreenController);
            resultScreenController.delegate = this;

            Scene scene = new Scene(loader.load(), 1200, 700);
            scene.getStylesheets().add(this.getClass().getResource("/resources/css/style.css").toExternalForm());
            stage.setScene(scene);
            stage.show();

        } catch (IOException ex) {
            System.err.println("Nem sikerült betölteni az eredményhirdető ablak fxml-jét!");
        }
    }
    
    //A kliens kilépését elküldi a szervernek
    public void quitFromGame() throws RemoteException {
        if(remoteService != null) {
            remoteService.quitFromGame(this);
        }
    }
    
    
    // A játék kliens kilépése miatti végekor a warning message OK gombjára kattintás, menübe felület újra megjelenik
    public void endOfGameBecauseSomebodyQuittedClickOnOK() throws IOException {
        if(stage != null) {
            stage.close();
        }
        callMenu();
    }

}
