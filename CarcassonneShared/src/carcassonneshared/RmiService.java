package carcassonneshared;

import java.awt.Point;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

// interfész, melynek függvényeit a kliens hívja, a szerver pedig implementálja azokat
public interface RmiService extends Remote{
    
    // egy kliens csatlakozása
    void addObserver(RemoteObserver o, String name) throws RemoteException;
    
    // kliens kérése, hogy ki következik
    void whosTurnIsIt() throws RemoteException;
    
    // kliens kártyahúzása
    String chooseFaceDownLandTile(Point p) throws RemoteException;
    
    // a kliens a kártyahúzás megjelenítésével végzett
    void chooseFaceDownLandTileDone() throws RemoteException;
    
    // klienstől származó balraforgatás kérése
    void rotateLeftLandTile() throws RemoteException;
    
    // klienstől származó jobbraforgatás kérése
    void rotateRightLandTile() throws RemoteException;
    
    // a kártya elhelyezésének kérése
    int locateLandTileOnTheTable(Point where) throws RemoteException;
    
    // a kliesn a kártyaelhelyezés megjelenítésével végzett
    void locateLandTileDone() throws RemoteException;
    
    // a kliens elkéri az alattvalók lehetséges elhelyezéseinek pozícióit
    List<Integer> getFollowerPointsOfActualLandTile() throws RemoteException;
    
    // az alattvaló elhelyezésének kérése
    void locateFollower(int where) throws RemoteException;
    
    // a lépések közötti pontszámítás
    void countPoints() throws RemoteException;
    
    // a kliens játékból való kilépése
    void quitFromGame(RemoteObserver o) throws RemoteException;
}
