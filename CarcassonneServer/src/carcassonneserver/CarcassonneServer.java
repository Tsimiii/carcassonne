package carcassonneserver;

import carcassonneshared.RemoteObserver;
import carcassonneshared.RmiService;
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
    private final static int PLAYERNUMBER = 1;
    private static List<RemoteObserver> player = new ArrayList<RemoteObserver>();

    public CarcassonneServer() {
        thread.start();
    }

    Thread thread = new Thread() {
        @Override
        public void run() {
            while (true) {
                if (countObservers() == PLAYERNUMBER) {
                    carcassonneGameModel = new CarcassonneGameModel();
                    setChanged();
                    notifyObservers(carcassonneGameModel.getShuffledIdArray());
                    setChanged();
                    notifyObservers("startgame");
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
    public void addObserver(RemoteObserver o) throws RemoteException {
        player.add(o);
        wrappedObserver = new WrappedObserver(o);
        addObserver(wrappedObserver);
        System.out.println("Added observer:" + wrappedObserver);
    }

    @Override
    public String chooseFaceDownLandTile(Point p) throws RemoteException {
        setChanged();
        boolean firstchoose = carcassonneGameModel.chooseFaceDownLandTile(p);
        if (firstchoose) {
            setChanged();
            notifyObservers(p);
            setChanged();
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
}
