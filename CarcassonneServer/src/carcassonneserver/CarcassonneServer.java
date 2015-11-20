package carcassonneserver;

import artificialintelligence.CarcassonneAI;
import carcassonneshared.RemoteObserver;
import carcassonneshared.RmiService;
import java.awt.Point;
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

public class CarcassonneServer extends Observable implements RmiService {
    
    private static CarcassonneServerProperties prop;

    private CarcassonneGameModel carcassonneGameModel;

    private WrappedObserver wrappedObserver;

    private static boolean timesUp = false;
    private static int PLAYERNUMBER;
    private static List<WrappedObserver> playerObservers = new ArrayList<>();
    private List<CarcassonneAI> artificialIntelligences = new ArrayList<>();
    private List<String> names = new ArrayList<>();
    private Thread thread;
    private static Timer timer;
    private static int STARTERINTERVAL;
    int interval;
    private CarcassonneServer carser = this;

    public CarcassonneServer(CarcassonneServerProperties prop) {
        this.prop = prop;
        PLAYERNUMBER = prop.getPlayerNumber();
        STARTERINTERVAL = prop.getStarterInterval();
        this.interval = STARTERINTERVAL;
        if(countObservers() == 0) {
            createAndStartThreadAndStartTimer();
        }
        
    }
    
    private void createAndStartThreadAndStartTimer() {
        thread = new MyThread();
        thread.start();
        int delay = 1000;
        int period = 1000;
        interval=STARTERINTERVAL;
        timer = new Timer();
        timer.scheduleAtFixedRate(new MyTimerTask(), delay, period);
    }
    
    private int setInterval() {
        if (countObservers() == PLAYERNUMBER || interval == 1) {
            timer.cancel();
        }
        return --interval;
    }
    
    public class MyTimerTask extends TimerTask{
        @Override
            public void run() {
                if(countObservers() > 0) {
                    setInterval();
                    System.out.println("interval: " + interval);
                    notifyObservers(new Object[] {"timer", interval});
                    setChanged();

                    if(interval == 0 && countObservers() < PLAYERNUMBER && countObservers() > 0) {
                        for(int i=0; i<PLAYERNUMBER-countObservers(); i++) {
                            CarcassonneAI carcassonneAI = new CarcassonneAI(prop.getAIDelay());
                            artificialIntelligences.add(carcassonneAI);
                        }
                    }
                }

            }
    }
    
    
    public class MyThread extends Thread {
        @Override
        public void run() {
            while (true) {
                if (countObservers() > 0 && (countObservers() == PLAYERNUMBER || interval == 0)) {
                    carcassonneGameModel = new CarcassonneGameModel(PLAYERNUMBER);
                    for(CarcassonneAI ai : artificialIntelligences) {
                        ai.delegate = CarcassonneServer.this;
                        ai.setGameModel(carcassonneGameModel);
                    }
                    setChanged();
                    notifyObservers(carcassonneGameModel.getShuffledIdArray());
                    
                    for(int i=0; i<PLAYERNUMBER-countObservers(); i++) {
                        names.add("Gépi játékos " + (i+1));
                    }
                    setChanged();
                    for(int i=0; i<playerObservers.size(); i++) {
                        playerObservers.get(i).update(carser, new Object[] {"startgame", i, names});
                    }
                    setChanged();
                    playerObservers.get(0).update(carser, "YourTurn");
                    break;
                }
            }
        }
    }

    /*Thread thread = new Thread() {
        @Override
        public void run() {
            while (true) {
                if (countObservers() > 0 && (countObservers() == PLAYERNUMBER || interval == 0)) {
                    carcassonneGameModel = new CarcassonneGameModel(PLAYERNUMBER);
                    for(CarcassonneAI ai : artificialIntelligences) {
                        ai.delegate = CarcassonneServer.this;
                        ai.setGameModel(carcassonneGameModel);
                    }
                    setChanged();
                    notifyObservers(carcassonneGameModel.getShuffledIdArray());
                    
                    for(int i=0; i<PLAYERNUMBER-countObservers(); i++) {
                        names.add("Gépi játékos " + (i+1));
                    }
                    setChanged();
                    for(int i=0; i<playerObservers.size(); i++) {
                        playerObservers.get(i).update(carser, new Object[] {"startgame", i, names});
                    }
                    setChanged();
                    playerObservers.get(0).update(carser, "YourTurn");
                    break;
                }
            }
        };
    };*/
    
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
                System.out.println("Remote exception removing observer:" + this);
                o.deleteObserver(this);
                if(countObservers() == 0) {
                    thread.interrupt();
                    interval = STARTERINTERVAL;
                }
                playerObservers.remove(this);
                for(int i=0; i<names.size(); i++) {
                    if(names.get(i).equals(this.name)) {
                        names.remove(i);
                    }
                }
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
            
            thread.interrupt();
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
            setChanged();
            notifyObservers(p);
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
        asd++;
        if(carcassonneGameModel.getTurn() >= playerObservers.size() && asd%countObservers() == 0) {
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
        if(carcassonneGameModel.getTurn() >= playerObservers.size() && asd%countObservers() == 0 && carcassonneGameModel.isChoosenLandTileNotNull()) {
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
        CarcassonneServerProperties prop = new CarcassonneServerProperties();
        
        try {
            Registry reg = LocateRegistry.createRegistry(8080);
            RmiService carcassonneServer = (RmiService) UnicastRemoteObject.exportObject(new CarcassonneServer(prop), prop.getPort());
            reg.rebind("carcassonneServer", carcassonneServer);
        } catch (RemoteException ex) {
            System.err.println("Szerver oldali hiba!");
        }
        System.out.println("Elindult a szerver.");
    }  
}
