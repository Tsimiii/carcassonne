package carcassonneserver;

import carcassonneshared.RemoteObserver;
import carcassonneshared.RmiService;
import java.io.Serializable;
import java.net.ServerSocket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class CarcassonneServer extends Observable implements RmiService {

    private static boolean timesUp = false;
    private static int playerNumber = 2;

    public CarcassonneServer() {
        thread.start();

        /*   System.out.println("EZAZZZZZZZZ, kezdődik a játék!");
         for (Socket gamer : gamers) {
         OutputStream out = null;
         try {
         out = gamer.getOutputStream();
         } catch (IOException ex) {
         Logger.getLogger(CarcassonneServer.class.getName()).log(Level.SEVERE, null, ex);
         }
         PrintWriter pw = new PrintWriter(out);
         pw.println("kesz");
         pw.flush();
         }*/
    }

    Thread thread = new Thread() {
        @Override
        public void run() {
            while (true) {
                if (countObservers() == playerNumber) {
                    setChanged();
                    notifyObservers("indul a játék");
                    break;
                }
            }
        }
    ;

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
                System.out
                        .println("Remote exception removing observer:" + this);
                o.deleteObserver(this);
            }
        }
    }

    @Override
    public void addObserver(RemoteObserver o) throws RemoteException {
        WrappedObserver mo = new WrappedObserver(o);
        addObserver(mo);
        System.out.println("Added observer:" + mo);
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
