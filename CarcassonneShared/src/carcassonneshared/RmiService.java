package carcassonneshared;

import java.awt.Point;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface RmiService extends Remote{
    
    void addObserver(RemoteObserver o, String name) throws RemoteException;
    
    void whosTurnIsIt() throws RemoteException;
    
    String chooseFaceDownLandTile(Point p) throws RemoteException;
    
    void chooseFaceDownLandTileDone() throws RemoteException;
    
    void rotateLeftLandTile() throws RemoteException;
    
    void rotateRightLandTile() throws RemoteException;
    
    int locateLandTileOnTheTable(Point where) throws RemoteException;
    
    List<Integer> getFollowerPointsOfActualLandTile() throws RemoteException;
    
    void locateFollower(int where) throws RemoteException;
    
    void countPoints() throws RemoteException;
}
