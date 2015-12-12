package carcassonneshared;

import java.rmi.Remote;
import java.rmi.RemoteException;

// interfész, melyen keresztül a szerver értesíti az összes klienset (akik implementálják ezt az interfészt)
public interface RemoteObserver extends Remote {
    
    // üzenet küldése az összes kliens felé
    void update(Object observable, Object updateMsg) throws RemoteException;
}
