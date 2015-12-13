package carcassonneserver;

import artificialintelligence.CarcassonneAI;
import carcassonneshared.RemoteObserver;
import carcassonneshared.RmiService;
import java.awt.Point;
import java.io.Console;
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
import model.CarcassonneGameModel;

// A Carcassonne szerver osztálya
public class CarcassonneServer extends Observable implements RmiService {

    private CarcassonneGameModel carcassonneGameModel; // A logikai réteg példánya

    private WrappedObserver wrappedObserver;

    private static boolean timesUp = false;
    private static int PLAYERNUMBER; // A játékosok száma
    private static List<WrappedObserver> playerObservers = new ArrayList<>(); // A játékba csatlakozott felhasználókat tároló lista
    private List<CarcassonneAI> artificialIntelligences = new ArrayList<>(); // A mesterséges intelligenciákat tároló lista
    private List<String> names = new ArrayList<>(); // A játékosok neveit tároló lista
    private Thread joinPlayersThread; // A játékosok csatlakozását kezelő szál
    private static Timer timer;
    private static int STARTERINTERVAL; // A csatlakozási idő kezdőértéke
    private int interval; // A csatlakozási idő aktuális időpontja
    private boolean gameIsNotStartedOrEnded; //logikai változó, értéke igaz, ha a játék még nem kezdődött el, vagy már véget ért
    private int clientAnswerDuringAGame = 0;
    private CarcassonneServer carser = this;

    public CarcassonneServer(int playerNumber, int starterInterval) {
        PLAYERNUMBER = playerNumber;
        STARTERINTERVAL = starterInterval;
        this.interval = STARTERINTERVAL;
        gameIsNotStartedOrEnded = true;
        if(countObservers() == 0) {
            createAndStartThreadAndStartTimer();
        } 
    }
    
    // A játék indításához és az időzítőhöz tartozó szál létrehozása
    private void createAndStartThreadAndStartTimer() {
        joinPlayersThread = new JoinPlayersThread(); // A játékosok csatlakozásához tartozó szál létrehozása
        int delay = 1000;
        int period = 1000;
        interval=STARTERINTERVAL;
        timer = new Timer();
        timer.scheduleAtFixedRate(new CarcassonneTimer(), delay, period); // Időzítőhöz tartozó szál
    }
    
    // Beállítja az aktuális csatlakozási időt (másodpercenként hívódik meg a függvény)
    private int setInterval() {
        // Ha megfelelő számú játékos csatlakozott, vagy lejárt az idő, akkor leállítja az időzítőt
        if (countObservers() == PLAYERNUMBER || interval == 1) {
            timer.cancel();
        }
        return --interval; // Az idő egy egységgel csökken
    }
    
    // Az időzítő szál
    public class CarcassonneTimer extends TimerTask{
        @Override
        public void run() {
            // Akkor indul el, ha legalább egy játékos már csatlakozott
            if(countObservers() > 0) {
                setInterval();
                System.out.println("interval: " + interval);
                
                // Elküldi a klienseknek az aktuális időpontot
                notifyObservers(new Object[] {"timer", interval});
                setChanged();

                // Ha lejárt az idő és csatlakozott már játékos, de kevesebb, mint amennyire szükség van a játékhoz
                if(interval == 0 && countObservers() < PLAYERNUMBER && countObservers() > 0) {
                    for(int i=0; i<PLAYERNUMBER-countObservers(); i++) {
                        CarcassonneAI carcassonneAI = new CarcassonneAI(500); // A maradék helyekre MI-k indítása
                        artificialIntelligences.add(carcassonneAI);
                    }
                }
                // Ha lejárt az idő, vagy elég számú kliens csatlakozott
                if(interval == 0 || countObservers() == PLAYERNUMBER) {
                    if(interval > 0) {
                        notifyObservers(new Object[] {"timer2", "Megfelelő számú játékos csatlakozott!"});
                        setChanged();
                    }
                    joinPlayersThread.start(); // A játék indításához tartozó szál elindítása
                }
            }

        }
    }  
    
    // A játék indításához tartozó szál, inicializálja és beállítja a kezdeti 
    public class JoinPlayersThread extends Thread {
        @Override
        public void run() {           
            carcassonneGameModel = new CarcassonneGameModel(PLAYERNUMBER);
            for(CarcassonneAI ai : artificialIntelligences) {
                ai.delegate = carser;
                ai.setGameModel(carcassonneGameModel);
            }
            // elküldi a klienseknek az összekevert kártyalapok azonosítójátsorfolytonosan
            setChanged();
            notifyObservers(carcassonneGameModel.getShuffledIdArray());

            for(int i=0; i<PLAYERNUMBER-countObservers(); i++) {
                names.add("Gépi játékos " + (i+1));
            }
            gameIsNotStartedOrEnded = false;
            
            // Elküldi a klienseknek, hogy a játék elkezdődött
            setChanged();
            for(int i=0; i<playerObservers.size(); i++) {
                playerObservers.get(i).update(carser, new Object[] {"startgame", i, names});
            }
            
            // A sorban az első kliensnek elküldi, hogy ő következik a sorban
            setChanged();
            playerObservers.get(0).update(carser, "YourTurn");
        }
    }
    
    // A csatlakozott játékosok példánya (Observable osztály miatt szükséges, mert Observer típusokat kezel)
    private class WrappedObserver implements Observer, Serializable {

        private static final long serialVersionUID = 1L;
        private RemoteObserver remoteObserver = null; 
        private String name = null;

        public WrappedObserver(RemoteObserver ro, String name) {
            this.remoteObserver = ro;
            this.name = name;
        }

        // Ezáltal minden kliensnek egyesével is lehet üzenetet küldeni
        @Override
        public void update(Observable o, Object arg) {
            try {
                remoteObserver.update(o.toString(), arg); // Meghívja a RemoteObserver interfész függvényét
            } catch (RemoteException e) {
                System.err.println("Üzenetküldés a kliensnek sikertelen.");
            }
        }
    }

    // A játékos csatlakozását végzi el
    @Override
    public void addObserver(RemoteObserver o, String name) throws RemoteException {
        names.add(name); // A nevet hozzáadja a névlistához
        wrappedObserver = new WrappedObserver(o, name);
        playerObservers.add(wrappedObserver); // A nyilvántartott játékosokhoz hozzáadja az erre szolgáló wrappedObserver példányokat
        addObserver(wrappedObserver);  // Observable osztály függvénye: játékos tárolása
        System.out.println("Hozzáadott játékos példánya: " + wrappedObserver + ", neve: " + name);
    }
    
    // A játékos kilépését végzi el
    @Override
    public void quitFromGame(RemoteObserver o) throws RemoteException {
        System.out.println("Remote exception removing observer:" + this);
        WrappedObserver wrappedObserver = null;
        for(WrappedObserver wo : playerObservers) {
            if(wo.remoteObserver.equals(o)) {
                wrappedObserver = wo;
                names.remove(wo.name); // A megfelelő kliens nevét törli a listából
            }
        }
        playerObservers.remove(wrappedObserver); // A játékosok listájából törli a megfelelő játékost
        deleteObserver(wrappedObserver); // Observable osztály függvénye: játékos törlése
        
        // Ha a játékosok száma ismét 0
        if(countObservers() == 0) {
            joinPlayersThread.interrupt(); // Szünetelteti a játékindítás szálat
            interval = STARTERINTERVAL; // A csatlakozási idő ismét a kezdeti időre vált
        }
        
        // Ha a játék már elkezdőtött
        if(!gameIsNotStartedOrEnded) {
            gameIsOverBecauseSomebodyQuitted();
        }
    }
    
    // Ha játék közben valaki kilépett, akkor az összes kliens számára véget ért a játék
    private void gameIsOverBecauseSomebodyQuitted() {
        // Az összes kliensnek elküldi, hogy a játék véget ért
        setChanged();
        notifyObservers("gameIsOver");
        
        gameIsNotStartedOrEnded = true;
        joinPlayersThread.interrupt(); // A játékindítás szálat szünetelteti
        deleteObservers(); // Törli az összes játékost az Observable osztályból
        playerObservers.clear(); // Törli az összes játékost a listából
        artificialIntelligences.clear(); // Törli az összes MI-t
        names.clear(); // Törli az összes nevet
        createAndStartThreadAndStartTimer(); // Újraindítja az időzítő szálat
    }
    
    // A megfelelő játékosnak elküldi, hogy ő következik, illetve a játék végén elvégzi a megfelelő műveleteket
    @Override
    public void whosTurnIsIt() throws RemoteException {   
        // Ha a játék még nem ért véget
        if(!carcassonneGameModel.isGameEnded()) {
            // Ha egy felhasználó következik
            if(carcassonneGameModel.getTurn() < playerObservers.size()) {
                // Elküldi a megfelelő kliensnek, hogy ő következik
                setChanged();
                playerObservers.get(carcassonneGameModel.getTurn()).update(this, "YourTurn");
            // Ha MI következik
            } else {
                try {
                    // A megfelelő MI kártyahúzása
                    artificialIntelligences.get(carcassonneGameModel.getTurn()-playerObservers.size()).chooseLandTile();
                } catch (InterruptedException ex) {
                    System.err.println("Hiba a mesterséges intelligencia kártyahúzásakor.");
                }
            }
        // Ha a játék véget ért
        } else {
            int[] point = carcassonneGameModel.countPointEndOfTheGame();
            setChanged();
            notifyObservers(new Object[] {"countPointEndOfTheGame", point}); // Elküldi a klienseknek a záróértékelésből adódó pontokat
            setChanged();
            notifyObservers(new Object[] {"sortedPoints", carcassonneGameModel.sortPlayersByPoint(), names}); // Elküldi a klienseknek a pontokat növekvő sorrendben
            
            gameIsNotStartedOrEnded = true;
            joinPlayersThread.interrupt(); // A játékindítás szálat szünetelteti
            deleteObservers(); // Törli az összes játékost az Observable osztályból
            playerObservers.clear(); // Törli az összes játékost a listából
            artificialIntelligences.clear(); // Törli az összes MI-t
            names.clear(); // Törli az összes nevet
            createAndStartThreadAndStartTimer(); // Újraindítja az időzítő szálat
        }
    }

    // A kártyahúzás kérésének fogadása és a válasz elküldése
    @Override
    public String chooseFaceDownLandTile(Point p) throws RemoteException{
        setChanged();
        boolean firstchoose = carcassonneGameModel.chooseFaceDownLandTile(p); // Logikai rétegben a kártyahúzás elvégzése
        // Ha a játékornak ez volt az első húzása az adott körön belül
        if (firstchoose) {
            if((carcassonneGameModel.getTurn() >= playerObservers.size() && carcassonneGameModel.isLandTileCanBeLocated()) || carcassonneGameModel.getTurn() < playerObservers.size()) {
                // Elküldi az összes kliensnek a kihúzott kártya pozícióját
                setChanged();
                notifyObservers(p);
            }
            if(carcassonneGameModel.getTurn() < playerObservers.size()) {
                // A forgatásgombok elérhetővé tétele a megfelelő kliens számára
                setChanged();
                playerObservers.get(carcassonneGameModel.getTurn()).update(this, "rotateButtonEnabled");
                // A letiltott mezők elküldése a megfelelő kliens számára
                setChanged();
                playerObservers.get(carcassonneGameModel.getTurn()).update(this, carcassonneGameModel.getForbiddenPlacesOnTheTable());
            }
            // Ha a kártyát nem lehet lerakni
            if(!carcassonneGameModel.isLandTileCanBeLocated()) {
                for(CarcassonneAI ai : artificialIntelligences) {
                    ai.removeFromPointsOfLandTilesCanBeChosed(p); // Törli az MI kihúzható kártyái közül ezt a kártyát
                }
                return "cantBeLocated"; // Az aktuális kliensnek visszatérési értékként megadja, hogy a kártyát nem lehet elhelyezni
            }
            for(CarcassonneAI ai : artificialIntelligences) {
                ai.removeFromPointsOfLandTilesCanBeChosed(p); // Törli az MI kihúzható kártyái közül ezt a kártyát
            }
            return "success"; // Az aktuális kliensnek visszatérési értékként megadja, hogy a kártyaelhelyezés sikeres volt
        }
        return "multipleChoose"; // Az aktuális kliensnek visszatérési értékként megadja, hogy már húzott egy kártyát
    }
    
    // A kliens végzett a kihúzott kártya megjelenítésével
    @Override
    public void chooseFaceDownLandTileDone() throws RemoteException {
        System.out.println("Végzett a kihúzással");
        clientAnswerDuringAGame++;
        System.out.println("chooseNumberDuringAGame: " + clientAnswerDuringAGame);
        // Ha a játék megy, MI van soron és minden kliens végzett a kártya kihúzásának megjelenítésével
        if(!gameIsNotStartedOrEnded && carcassonneGameModel.getTurn() >= playerObservers.size() && clientAnswerDuringAGame%countObservers() == 0 ) {
            System.out.println("és még ide is belépett.");
            try {
                artificialIntelligences.get(carcassonneGameModel.getTurn()-playerObservers.size()).decideBestLocation(); // Az MI dönt a legjobb elhelyezésről
            } catch (InterruptedException ex) {
                System.err.println("Hiba a mesterséges intelligencia megfelelő elhelyezés kiválasztásakor.");
            }
        }
        
    }

    // A kártya balra forgatásának kérése és a válasz
    @Override
    public void rotateLeftLandTile() throws RemoteException {
        boolean successRotateLeft = carcassonneGameModel.rotateLeftLandTile(); // A kártya balra forgatása a logikai rétegben
        // Ha sikeres volt
        if(successRotateLeft) {
            // Elküldi az összes kliensnek, hogy a kihúzott kártya elfordult balra 90°-kal
            setChanged();
            notifyObservers("successRotateLeft");
            // Ha épp egy felhasználó van soron
            if(carcassonneGameModel.getTurn() < playerObservers.size()) {
                // Az aktuális kliensnek elküldi az új letiltott mezők listáját
                setChanged();
                playerObservers.get(carcassonneGameModel.getTurn()).update(this, carcassonneGameModel.getForbiddenPlacesOnTheTable());
            }
        }
    }

    // A kártya jobbra forgatásának kérése és a válasz
    @Override
    public void rotateRightLandTile() throws RemoteException {
        boolean successRotateRight = carcassonneGameModel.rotateRightLandTile(); // A kártya jobbra forgatása a logikai rétegben
        // Ha sikeres volt
        if(successRotateRight) {
            // Elküldi az összes kliensnek, hogy a kihúzott kártya elfordult jobbra 90°-kal
            setChanged();
            notifyObservers("successRotateRight");
            // Ha épp egy felhasználó van soron
            if(carcassonneGameModel.getTurn() < playerObservers.size()) {
                // Az aktuális kliensnek elküldi az új letiltott mezők listáját
                setChanged();
                playerObservers.get(carcassonneGameModel.getTurn()).update(this, carcassonneGameModel.getForbiddenPlacesOnTheTable());
            }
        }
    }
    
    // A kártya elhelyezésének kérése és válasza
    @Override
    public int locateLandTileOnTheTable(Point where) throws RemoteException {
        boolean successLocate = carcassonneGameModel.locateLandTileOnTheTable(where); // A kártya elhelyezése a logikai rétegben
        // Ha az elhelyezés sikeres és a kliensnek van lehetősége alattvalót elhelyeznie
        if(successLocate && carcassonneGameModel.playerHasFreeFollower() && !carcassonneGameModel.getPointsOfFollowers().isEmpty()) {
            // Elküldi az összes kliensnek a kártya elhelyezését
            setChanged();
            notifyObservers(new Object[] {"locateLandTile", where});
            return 2; // 2 jelenti, hogy meg kell jelenítenie a megfelelő kliensnek az alattvaló elhelyezésére szolgáló ablakot
        // Ha az elhelyezés sikeres és a kliensnek nincs lehetősége alattvalót elhelyeznie
        } else if(successLocate && (!carcassonneGameModel.playerHasFreeFollower() || carcassonneGameModel.getPointsOfFollowers().isEmpty())) {
            // Elküldi az összes kliensnek a kártya elhelyezését
            setChanged();
            notifyObservers(new Object[] {"locateLandTile", where});
            return 1; // 1 jelenti, hogy nem kell megjelenítenie a megfelelő kliensnek az alattvaló elhelyezésére szolgáló ablakot
        }
        return 0; // A kártyaelhelyezés sikertelen volt
    }
    // A kliens végzett a kihúzott kártya elhelyezésével
    @Override
    public void locateLandTileDone() throws RemoteException {
        clientAnswerDuringAGame++;
        // Ha a játék megy, MI van soron és minden kliens végzett a kártya elhelyezésének megjelenítésével
        if(!gameIsNotStartedOrEnded && carcassonneGameModel.getTurn() >= playerObservers.size() && clientAnswerDuringAGame%countObservers() == 0 && carcassonneGameModel.isChoosenLandTileNotNull()) {
            try {
                artificialIntelligences.get(carcassonneGameModel.getTurn()-playerObservers.size()).locateFollower(); // Az MI elhelyez(het) alattvalót
            } catch (InterruptedException ex) {
                System.err.println("Hiba a mesterséges intelligencial alattvaló elhelyezésekor.");
            }
        }
    }
    
    // Visszaadja az adott kártyán azokat a pontokat, ahova alattvaló elhelyezhető
    @Override
    public List<Integer> getFollowerPointsOfActualLandTile() throws RemoteException {
        return carcassonneGameModel.getPointsOfFollowers();
    }
    
    // Az alattvaló elhelyezésének kérése és válasza
    @Override
    public void locateFollower(int where) throws RemoteException {
        boolean success = carcassonneGameModel.locateFollower(where); // Az alattvaló elhelyezése a logikai rétegben
        // Ha sikeres volt az elhelyezés
        if(success) {
            // Az összes klienset értesíti az alattvaló elhelyezéséről
            setChanged();
            notifyObservers(new Object[] {"locateFollower", where, carcassonneGameModel.getTurn()});
        }
    }
  
    // A pontszámítás kérése és a válasz
    @Override
    public void countPoints() throws RemoteException {
        int[] point = carcassonneGameModel.countPoints(); // A lépések közötti pontszámítás a logikai rétegben
        // Elküldi az összes kliensnek a friss pontokat
        setChanged();
        notifyObservers(new Object[] {"countPoint", point});
        // Elküldi az összes kliensnek a frissített alattvalók számát
        setChanged();
        notifyObservers(new Object[] {"getFollowerNumber", carcassonneGameModel.getFreeFollowerNumOfPLayers(), carcassonneGameModel.getFreeFollowersAgainPastLocation()});
    }

    public static void main(String[] args) throws IOException {     
        int playerNumber; // Játékosok száma
        int starterInterval; // A csatlakozási idő
        
        // Consolról bekéri a szerver indításához szükséges adatokat
        Console c = System.console();
        do {
            playerNumber = Integer.parseInt(c.readLine("Játékosok száma (2-5 játékos): "));
        } while(playerNumber < 2 || playerNumber > 5);
        do {
            starterInterval = Integer.parseInt(c.readLine("Csatlakozási idö: "));
        } while(starterInterval < 0);
        
        boolean success = false;
        do {
            try {
                int port = Integer.parseInt(c.readLine("Portszám: ")); // Portszám
                // Távoli objektum regisztrálása és megvalósítása
                Registry reg = LocateRegistry.createRegistry(port);
                RmiService carcassonneServer = (RmiService) UnicastRemoteObject.exportObject(new CarcassonneServer(playerNumber, starterInterval), port);
                reg.rebind("carcassonneServer", carcassonneServer);
                success = true;
                System.out.println("Elindult a szerver.");
            } catch (RemoteException ex) {
                System.err.println("Ezen a porton már fut szerver! Adj meg egy másikat!");
            }
        } while(!success);
    }  
}
