package carcassonneserver;

import carcassonneshared.RemoteObserver;
import carcassonneshared.RmiService;
import com.sun.javafx.scene.control.skin.VirtualFlow;
import java.awt.Point;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import model.CarcassonneGameModel;

public class CarcassonneServer extends Observable implements RmiService {

    private CarcassonneGameModel carcassonneGameModel;

    private WrappedObserver wrappedObserver;

    private static boolean timesUp = false;
    private final static int PLAYERNUMBER = 2;
    private static List<WrappedObserver> playerObservers = new ArrayList<>();
    private List<String> names = new ArrayList<>();
    private CarcassonneServer carser = this;

    public CarcassonneServer() {
        thread.start();
        
    }

    Thread thread = new Thread() {
        @Override
        public void run() {
            while (true) {
                if (countObservers() == PLAYERNUMBER) {
                    carcassonneGameModel = new CarcassonneGameModel(PLAYERNUMBER);
                    setChanged();
                    notifyObservers(carcassonneGameModel.getShuffledIdArray());
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
    };
    
        private class WrappedObserver implements Observer, Serializable {

        private static final long serialVersionUID = 1L;

        private RemoteObserver ro = null;

        public WrappedObserver(RemoteObserver ro) {
            this.ro = ro;
        }

        @Override
        public void update(Observable o, Object arg) {
            try {
                ro.update(o.toString(), arg);
            } catch (RemoteException e) {
                System.out.println("Remote exception removing observer:" + this);
                o.deleteObserver(this);
            }
        }
    }

    @Override
    public void addObserver(RemoteObserver o, String name) throws RemoteException {
        names.add(name);
        wrappedObserver = new WrappedObserver(o);
        playerObservers.add(wrappedObserver);    
        addObserver(wrappedObserver);
        System.out.println("Added observer:" + wrappedObserver);
    }
    
    @Override
    public void whosTurnIsIt() {
        setChanged();
        playerObservers.get(carcassonneGameModel.getTurn()).update(this, "YourTurn");
    }

    @Override
    public String chooseFaceDownLandTile(Point p) throws RemoteException {
        setChanged();
        boolean firstchoose = carcassonneGameModel.chooseFaceDownLandTile(p);
        if (firstchoose) {
            setChanged();
            notifyObservers(p);
            setChanged();
            setChanged();
            playerObservers.get(carcassonneGameModel.getTurn()).update(this, "rotateButtonEnabled");
            notifyObservers(carcassonneGameModel.getForbiddenPlacesOnTheTable());
            if(!carcassonneGameModel.isLandTileCanBeLocated()) {
                return "cantBeLocated";
            }
            return "success";
        }
        return "multipleChoose";
    }

    @Override
    public void rotateLeftLandTile() throws RemoteException {
        boolean successRotateLeft = carcassonneGameModel.rotateLeftLandTile();
        if(successRotateLeft) {
            setChanged();
            notifyObservers("successRotateLeft");
            setChanged();
            notifyObservers(carcassonneGameModel.getForbiddenPlacesOnTheTable());
        }
    }

    @Override
    public void rotateRightLandTile() throws RemoteException {
        boolean successRotateRight = carcassonneGameModel.rotateRightLandTile();
        if(successRotateRight) {
            setChanged();
            notifyObservers("successRotateRight");
            setChanged();
            notifyObservers(carcassonneGameModel.getForbiddenPlacesOnTheTable());
        }
    }
    
    @Override
    public boolean locateLandTileOnTheTable(Point where) throws RemoteException {
        boolean successLocate = carcassonneGameModel.locateLandTileOnTheTable(where);
        if(successLocate) {
            setChanged();
            notifyObservers(new Object[] {"locateLandTile", where});
            return true;
        }
        return false;
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
        notifyObservers(new Object[] {"getFollowerNumber", carcassonneGameModel.getFreeFollowerNumOfPLayers()});
        setChanged();
        playerObservers.get(carcassonneGameModel.getTurn()).update(this, "YourTurn");
    }

    public static void main(String[] args) {
        try {
            Registry reg = LocateRegistry.createRegistry(8080);
            RmiService carcassonneServer = (RmiService) UnicastRemoteObject.exportObject(new CarcassonneServer(), 8080);
            reg.rebind("carcassonneServer", carcassonneServer);
        } catch (RemoteException ex) {
            System.err.println("Szerver oldali hiba!");
        }
        System.out.println("Elindult a szerver.");
    }  
}
