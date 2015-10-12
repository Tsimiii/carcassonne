package carcassonneshared;

import java.awt.Point;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface RmiService extends Remote{
    
    void addObserver(RemoteObserver o) throws RemoteException;
    
    String chooseFaceDownLandTile(Point p) throws RemoteException;
    
    void rotateLeftLandTile() throws RemoteException;
    
    void rotateRightLandTile() throws RemoteException;
    
    boolean locateLandTileOnTheTable(Point where) throws RemoteException;
    
    List<Integer> getFollowerPointsOfActualLandTile() throws RemoteException;
    
    void locateFollower(int where) throws RemoteException;
    
    void countPoints() throws RemoteException;
}
