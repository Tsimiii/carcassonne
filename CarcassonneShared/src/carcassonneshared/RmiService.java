package carcassonneshared;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RmiService extends Remote{
    
    void addObserver(RemoteObserver o, String name) throws RemoteException;
    
}
