package carcassonneserver;

import artificialintelligence.CarcassonneAI;
import carcassonneshared.RemoteObserver;
import carcassonneshared.RmiService;
import java.awt.Point;
import java.io.Console;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.CarcassonneGameModel;

// A Carcassonne szerver osztálya
public class CarcassonneServer extends Observable implements RmiService {

    private CarcassonneGameModel carcassonneGameModel; // A logikai réteg példánya

    private WrappedObserver wrappedObserver;

    private static boolean timesUp = false;
    private static int PLAYERNUMBER; // A játékosok száma
    private static List<WrappedObserver> playerObservers = new ArrayList<>(); // A játékba csatlakozott felhasználókat tároló lista
    private List<CarcassonneAI> artificialIntelligences = new ArrayList<>(); // A mesterséges intelligenciákat tároló lista
    private List<String> names = new ArrayList<>(); // A játékosok neveit tároló lista
    private Thread joinPlayersThread; // A játékosok csatlakozását kezelő szál
    private static Timer timer;
    private static int STARTERINTERVAL; // A csatlakozási idő kezdőértéke
    private int interval;
    private boolean gameIsNotStartedOrEnded;
    private CarcassonneServer carser = this;

    public CarcassonneServer(int playerNumber, int starterInterval) {
        PLAYERNUMBER = playerNumber;
        STARTERINTERVAL = starterInterval;
        this.interval = STARTERINTERVAL;
        gameIsNotStartedOrEnded = true;
        if(countObservers() == 0) {
            createAndStartThreadAndStartTimer();
        } 
    }
    
    // A játékosok csatlakozásához és az időzítőhöz tartozó szál létrehozása
    private void createAndStartThreadAndStartTimer() {
        joinPlayersThread = new JoinPlayersThread();
        int delay = 1000;
        int period = 1000;
        interval=STARTERINTERVAL;
        timer = new Timer();
        timer.scheduleAtFixedRate(new CarcassonneTimer(), delay, period);
    }
    
    private int setInterval() {
        if (countObservers() == PLAYERNUMBER || interval == 1) {
            timer.cancel();
        }
        return --interval;
    }
    
    public class CarcassonneTimer extends TimerTask{
        @Override
        public void run() {
            if(countObservers() > 0) {
                setInterval();
                System.out.println("interval: " + interval);
                notifyObservers(new Object[] {"timer", interval});
                setChanged();

                if(interval == 0 && countObservers() < PLAYERNUMBER && countObservers() > 0) {
                    for(int i=0; i<PLAYERNUMBER-countObservers(); i++) {
                        CarcassonneAI carcassonneAI = new CarcassonneAI(500);
                        artificialIntelligences.add(carcassonneAI);
                    }
                }
                if(interval == 0 || countObservers() == PLAYERNUMBER) {
                    if(interval > 0) {
                        notifyObservers(new Object[] {"timer2", "Megfelelő számú játékos csatlakozott!"});
                        setChanged();
                    }
                    joinPlayersThread.start();
                }
            }

        }
    }  
    
    public class JoinPlayersThread extends Thread {
        @Override
        public void run() {           
            carcassonneGameModel = new CarcassonneGameModel(PLAYERNUMBER);
            for(CarcassonneAI ai : artificialIntelligences) {
                ai.delegate = carser;
                ai.setGameModel(carcassonneGameModel);
            }
            setChanged();
            notifyObservers(carcassonneGameModel.getShuffledIdArray());

            for(int i=0; i<PLAYERNUMBER-countObservers(); i++) {
                names.add("Gépi játékos " + (i+1));
            }
            gameIsNotStartedOrEnded = false;
            setChanged();
            for(int i=0; i<playerObservers.size(); i++) {
                playerObservers.get(i).update(carser, new Object[] {"startgame", i, names});
            }
            setChanged();
            playerObservers.get(0).update(carser, "YourTurn");
        }
    }
    
    private class WrappedObserver implements Observer, Serializable {

        private static final long serialVersionUID = 1L;

        private RemoteObserver ro = null;
        
        private String name = null;

        public WrappedObserver(RemoteObserver ro, String name) {
            this.ro = ro;
            this.name = name;
        }

        @Override
        public void update(Observable o, Object arg) {
            try {
                ro.update(o.toString(), arg);
            } catch (RemoteException e) {
            }
        }
    }

    @Override
    public void addObserver(RemoteObserver o, String name) throws RemoteException {
        names.add(name);
        wrappedObserver = new WrappedObserver(o, name);
        playerObservers.add(wrappedObserver);    
        addObserver(wrappedObserver);
        System.out.println("Added observer:" + wrappedObserver);
    }
    
    @Override
    public void quitFromGame(RemoteObserver o) throws RemoteException {
        System.out.println("Remote exception removing observer:" + this);
        WrappedObserver w = null;
        for(WrappedObserver wo : playerObservers) {
            if(wo.ro.equals(o)) {
                w = wo;
                names.remove(wo.name);
            }
        }
        playerObservers.remove(w);
        deleteObserver(w);
        if(countObservers() == 0) {
            joinPlayersThread.interrupt();
            interval = STARTERINTERVAL;
        }
        if(!gameIsNotStartedOrEnded) {
            gameIsOverBecauseSomebodyQuitted();
        }
    }
    
    private void gameIsOverBecauseSomebodyQuitted() {
        setChanged();
        notifyObservers("gameIsOver");
        
        gameIsNotStartedOrEnded = true;
        joinPlayersThread.interrupt();
        deleteObservers();
        playerObservers.clear();
        artificialIntelligences.clear();
        names.clear();
        createAndStartThreadAndStartTimer();
    }
    
    @Override
    public void whosTurnIsIt() throws RemoteException {      
        if(!carcassonneGameModel.isGameEnded()) {
            if(carcassonneGameModel.getTurn() < playerObservers.size()) {
                setChanged();
                playerObservers.get(carcassonneGameModel.getTurn()).update(this, "YourTurn");
            } else {
                try {
                    artificialIntelligences.get(carcassonneGameModel.getTurn()-playerObservers.size()).chooseLandTile();
                } catch (InterruptedException ex) {
                    Logger.getLogger(CarcassonneServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else {
            int[] point = carcassonneGameModel.countPointEndOfTheGame();
            setChanged();
            notifyObservers(new Object[] {"countPointEndOfTheGame", point});
            setChanged();
            notifyObservers(new Object[] {"sortedPoints", carcassonneGameModel.sortPlayersByPoint(), names});
            
            gameIsNotStartedOrEnded = true;
            joinPlayersThread.interrupt();
            deleteObservers();
            playerObservers.clear();
            artificialIntelligences.clear();
            names.clear();
            createAndStartThreadAndStartTimer();
        }
    }

    @Override
    public String chooseFaceDownLandTile(Point p) throws RemoteException{
        setChanged();
        boolean firstchoose = carcassonneGameModel.chooseFaceDownLandTile(p);
        if (firstchoose) {
            if((carcassonneGameModel.getTurn() >= playerObservers.size() && carcassonneGameModel.isLandTileCanBeLocated()) || carcassonneGameModel.getTurn() < playerObservers.size()) {
                setChanged();
                notifyObservers(p);
            }
            if(carcassonneGameModel.getTurn() < playerObservers.size()) {
                setChanged();
                playerObservers.get(carcassonneGameModel.getTurn()).update(this, "rotateButtonEnabled");
                setChanged();
                playerObservers.get(carcassonneGameModel.getTurn()).update(this, carcassonneGameModel.getForbiddenPlacesOnTheTable());
            }
            if(!carcassonneGameModel.isLandTileCanBeLocated()) {
                for(CarcassonneAI ai : artificialIntelligences) {
                    ai.removeFromPointsOfLandTilesCanBeChosed(p);
                }
                return "cantBeLocated";
            }
            for(CarcassonneAI ai : artificialIntelligences) {
                ai.removeFromPointsOfLandTilesCanBeChosed(p);
            }
            return "success";
        }
        return "multipleChoose";
    }
    
    private int asd = 0;
    
    @Override
    public void chooseFaceDownLandTileDone() throws RemoteException {
        System.out.println("Végzet a kihúzással");
        asd++;
        System.out.println("asd: " + asd); 
        if(!gameIsNotStartedOrEnded && carcassonneGameModel.getTurn() >= playerObservers.size() && asd%countObservers() == 0 ) {
            System.out.println("és még ide is belépett.");
            try {
                artificialIntelligences.get(carcassonneGameModel.getTurn()-playerObservers.size()).decideBestLocation();
            } catch (InterruptedException ex) {
                Logger.getLogger(CarcassonneServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }

    @Override
    public void rotateLeftLandTile() throws RemoteException {
        boolean successRotateLeft = carcassonneGameModel.rotateLeftLandTile();
        if(successRotateLeft) {
            setChanged();
            notifyObservers("successRotateLeft");
            if(carcassonneGameModel.getTurn() < playerObservers.size()) {
                setChanged();
                playerObservers.get(carcassonneGameModel.getTurn()).update(this, carcassonneGameModel.getForbiddenPlacesOnTheTable());
            }
        }
    }

    @Override
    public void rotateRightLandTile() throws RemoteException {
        boolean successRotateRight = carcassonneGameModel.rotateRightLandTile();
        if(successRotateRight) {
            setChanged();
            notifyObservers("successRotateRight");
            if(carcassonneGameModel.getTurn() < playerObservers.size()) {
                setChanged();
                playerObservers.get(carcassonneGameModel.getTurn()).update(this, carcassonneGameModel.getForbiddenPlacesOnTheTable());
            }
        }
    }
    
    @Override
    public int locateLandTileOnTheTable(Point where) throws RemoteException {
        boolean successLocate = carcassonneGameModel.locateLandTileOnTheTable(where);
        if(successLocate && carcassonneGameModel.playerHasFreeFollower() && !carcassonneGameModel.getPointsOfFollowers().isEmpty()) {
            setChanged();
            notifyObservers(new Object[] {"locateLandTile", where});
            return 2;
        } else if(successLocate && (!carcassonneGameModel.playerHasFreeFollower() || carcassonneGameModel.getPointsOfFollowers().isEmpty())) {
            setChanged();
            notifyObservers(new Object[] {"locateLandTile", where});
            return 1;
        }
        return 0;
    }
    
    @Override
    public void locateLandTileDone() throws RemoteException {
        asd++;
        if(!gameIsNotStartedOrEnded && carcassonneGameModel.getTurn() >= playerObservers.size() && asd%countObservers() == 0 && carcassonneGameModel.isChoosenLandTileNotNull()) {
            try {
                artificialIntelligences.get(carcassonneGameModel.getTurn()-playerObservers.size()).locateFollower();
            } catch (InterruptedException ex) {
                Logger.getLogger(CarcassonneServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    @Override
    public List<Integer> getFollowerPointsOfActualLandTile() throws RemoteException {
        return carcassonneGameModel.getPointsOfFollowers();
    }
    
    @Override
    public void locateFollower(int where) throws RemoteException {
        boolean success = carcassonneGameModel.locateFollower(where);
        if(success) {
            setChanged();
            notifyObservers(new Object[] {"locateFollower", where, carcassonneGameModel.getTurn()});
        }
    }
  
    @Override
    public void countPoints() throws RemoteException {
        int[] point = carcassonneGameModel.countPoints();
        setChanged();
        notifyObservers(new Object[] {"countPoint", point});
        setChanged();
        notifyObservers(new Object[] {"getFollowerNumber", carcassonneGameModel.getFreeFollowerNumOfPLayers(), carcassonneGameModel.getFreeFollowersAgainPastLocation()});
    }

    public static void main(String[] args) throws IOException {
        Console c = System.console();
        int playerNumber = Integer.parseInt(c.readLine("Játékosok száma: "));
        int starterInterval = Integer.parseInt(c.readLine("Csatlakozási idö: "));
        
        boolean success = false;
        do {
            try {
                int port = Integer.parseInt(c.readLine("Portszám: "));
                Registry reg = LocateRegistry.createRegistry(port);
                RmiService carcassonneServer = (RmiService) UnicastRemoteObject.exportObject(new CarcassonneServer(playerNumber, starterInterval), port);
                reg.rebind("carcassonneServer", carcassonneServer);
                success = true;
                System.out.println("Elindult a szerver.");
            } catch (RemoteException ex) {
                System.err.println("Ezen a porton már fut szerver! Adj meg egy másikat!");
            }
        } while(!success);
    }  
}
