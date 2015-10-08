package carcassonneshared;

import java.awt.Point;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RmiService extends Remote{
    
    void addObserver(RemoteObserver o) throws RemoteException;
    
    boolean chooseFaceDownLandTile(Point p) throws RemoteException;
    
    void rotateLeftLandTile() throws RemoteException;
    
    void rotateRightLandTile() throws RemoteException;
    
    boolean locateLandTileOnTheTable(Point where) throws RemoteException;
    
}
