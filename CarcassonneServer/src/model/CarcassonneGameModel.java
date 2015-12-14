package model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import model.follower.Follower;
import model.landtile.LandTile;
import model.landtile.LandTileLoader;
import model.player.Player;
import model.tablecell.TableCell;

// A Carcassonne játék logikai rétegét megvalósító osztály
public class CarcassonneGameModel {

    // A különböző kártyaelemek azonosítói
    private final int FIELD = 0;
    private final int ROAD = 1;
    private final int CITY = 2;
    private final int CITYWITHPENNANT = 4;
    private final int CLOISTER = 3;
    private final int NOTHING = 5;

    private final Player[] players; // A játékosokat tároló tömb
    private final LandTileLoader landTileLoader;
    private final LandTile[] landTiles; // A területkártyákat tároló tömb
    private LandTile chosenLandTile; // Az aktuálisan kihúzott kártya
    private TableCell[][] cells; // Az asztalon levő cellák mátrixa
    private final int[] shuffledIdArray; // Az összekevert kártyák azonosítójának tömbje
    private List<LandTile> locatedLandTiles; // Az elhelyezett kártyák listája
    private Set<Point> forbiddenPlacesOnTheTable; // Az asztal kártya elhelyezés szemopontjából letiltott cellák a táblán
    private Set<Point> enabledPlacesOnTheTable; // Az asztal elérhető cellái a kártya elhelyezés szempontjából
    private boolean landTileCanBeLocated;
    private List<Integer> pointsOfFollowers; // Az adott kártyán az alattvaló lehetséges elhelyezésének pontjai
    private List<Point> freeFollowersAgainPastLocation = new ArrayList<>(); // A kártya elhelyezés után az ismét szabaddá vált alattvalók listája
    private List<List<Point>> citiesOnTheEdge = new ArrayList<>(); // Az egyes várak szélső elemei
    private boolean endOfGame = false;
    private int turn; // Azt jelzi, hogy melyik játékos következik

    public CarcassonneGameModel(int playerNumber) {
        landTileLoader = new LandTileLoader(); // a területkártyák inicializálása
        landTiles = landTileLoader.getLandTiles();
        shuffledIdArray = new int[71];
        chosenLandTile = null;
        locatedLandTiles = new ArrayList<>();
        locatedLandTiles.add(landTileLoader.getStarterLandTile());
        forbiddenPlacesOnTheTable = new HashSet<>();
        enabledPlacesOnTheTable = new HashSet<>();
        initCells();
        players = new Player[playerNumber];
        initPlayers(playerNumber);
        turn = 0;
        shuffleLandTileArray(); // A kártyák keverése
    }

    // Az asztalon levő cellák inicializálása
    private void initCells() {
        cells = new TableCell[143][143]; // Ha egy sorba/oszlopba kerül az összes területkártya, akkor is elég legyen biztosan a hely
        for (int i = 0; i < 143; i++) {
            for (int j = 0; j < 143; j++) {
                cells[i][j] = new TableCell();
            }
        }
        cells[143 / 2][143 / 2].setLandTile(landTileLoader.getStarterLandTile()); // A középső cellára helyezi a kezdőkártyát
    }

    // A játékosok inicializálása
    private void initPlayers(int number) {
        for (int i = 0; i < number; i++) {
            players[i] = new Player(i);
        }
    }

    // A területkártyák összekeverése
    private void shuffleLandTileArray() {
        Collections.shuffle(Arrays.asList(landTiles));
        initShuffledIdArray();
    }

    // A kártyahúzásért felelős függvény
    public boolean chooseFaceDownLandTile(Point p) {
        System.out.println("BELÉP");
        // Ha még nem volt kihúzva kártya az adott körbe
        if (chosenLandTile == null) {
            landTileCanBeLocated = false;
            chosenLandTile = landTiles[p.x * 5 + p.y]; // az 5 oszlopos mátrixban így kapjuk meg sorfolytonosan az elemet
            System.out.println("\nchosen lt after choose: " + chosenLandTile);
            forbidIllegalPlaces();
            checkWhetherLandTileCanBeLocatedAfterRotates();
            return true;
        }
        return false;
    }

    // Visszaadja, hogy az aktuális körben húztak-e már kártyát
    public boolean isChoosenLandTileNotNull() {
        if (chosenLandTile == null) {
            return false;
        }
        return true;
    }

    // Levizsgálja, hogy a kihúzott kártyát el lehet-e szabályosan helyezni az asztalon
    private boolean checkWhetherLandTileCanBeLocatedAfterRotates() {
        // Minegyik irányban levizsgálja (4 forgatás balra)
        for (int i = 0; i < 4; i++) {
            rotateLeftLandTile();
            // Amint el lehet helyezni, nem vizsgálja tovább, hanem visszaállítja a kártyát a forgatás szempontjából eredeti helyzetbe
            if (landTileCanBeLocated) {
                for (int j = 0; j < i + 1; j++) {
                    rotateRightLandTile();
                }
                return true;
            }
        }
        chosenLandTile.setPositionOnTheTable(-2, -2); // Ha a kártyát nem lehet elhelyezni, értéke (-2, -2) lesz
        chosenLandTile = null;
        return false;
    }

    // A kártya balra forgatása
    public boolean rotateLeftLandTile() {
        // Ha már kihúzták és még nem rakták le a kártyát
        if (chosenLandTile != null && !locatedLandTiles.contains(chosenLandTile)) {
            setNewContinuousPartsAfterRotateLeft(); // Módosítja az összefüggő területeket a kártyán
            chosenLandTile.setComponents(getLeftRotateArray(chosenLandTile)); // Beállítja az elforgatásnak megfelelően a kártyaelemeket
            forbidIllegalPlaces(); // Letiltja a szabálytalan elhelyezéseket
            return true;
        }
        return false;
    }

    // A kártya jobbra forgatása
    public boolean rotateRightLandTile() {
        // Ha már kihúzták és még nem rakták le a kártyát
        if (chosenLandTile != null && !locatedLandTiles.contains(chosenLandTile)) {
            setNewContinuousPartsAfterRotateRight(); // Módosítja az összefüggő területeket a kártyán
            chosenLandTile.setComponents(getRightRotateArray(chosenLandTile)); // Beállítja az elforgatásnak megfelelően a kártyaelemeket
            forbidIllegalPlaces(); // Letiltja a szabálytalan elhelyezéseket
            return true;
        }
        return false;
    }

    // Jobbraforgatás után visszaadja a kártyaelemek új tömbjét
    private int[] getRightRotateArray(LandTile actualLandTile) {
        int[] rotateArray = new int[13];
        int temp1 = actualLandTile.getComponents()[0];
        int temp2 = actualLandTile.getComponents()[1];
        int temp3 = actualLandTile.getComponents()[2];
        // 0-tól a 8. indexig a 3 indexszel az elemek balra csúsznak a tömbben
        for (int i = 0; i < actualLandTile.getComponents().length - 4; i++) {
            rotateArray[i] = actualLandTile.getComponents()[i + 3];
        }
        rotateArray[9] = temp1; // A 9. indexre kerül az eddigi 0. indexű elem
        rotateArray[10] = temp2; // A 10. indexre kerül az eddigi 1. indexű elem
        rotateArray[11] = temp3; // A 11. indexre kerül az eddigi 2. indexű elem
        rotateArray[12] = actualLandTile.getComponents()[12]; // A 12. indexű elem ugyanott marad
        return rotateArray;
    }

    // Balraforgatás után visszaadja a kártyaelemek új tömbjét
    private int[] getLeftRotateArray(LandTile actualLandTile) {
        int[] rotateArray = new int[13];
        int temp1 = actualLandTile.getComponents()[9];
        int temp2 = actualLandTile.getComponents()[10];
        int temp3 = actualLandTile.getComponents()[11];
        // 0-tól a 8. indexig a 3 indexszel az elemek jobbra csúsznak a tömbben
        for (int i = 0; i < actualLandTile.getComponents().length - 4; i++) {
            rotateArray[i + 3] = actualLandTile.getComponents()[i];
        }
        rotateArray[0] = temp1; // A 0. indexre kerül az eddigi 9. indexű elem
        rotateArray[1] = temp2; // A 1. indexre kerül az eddigi 10. indexű elem
        rotateArray[2] = temp3; // A 2. indexre kerül az eddigi 11. indexű elem
        rotateArray[12] = actualLandTile.getComponents()[12]; // A 12. indexű elem ugyanott marad
        return rotateArray;
    }

    // Jobbraforgatás után visszaadja az új összefüggő területeket
    private void setNewContinuousPartsAfterRotateRight() {
        int contPartNum;
        for (int i = 0; i < chosenLandTile.getContinuousParts().length; i++) {
            for (int j = 0; j < chosenLandTile.getContinuousParts()[i].length; j++) {
                contPartNum = chosenLandTile.getContinuousParts()[i][j];
                if (contPartNum == 0 || contPartNum == 1 || contPartNum == 2) {
                    chosenLandTile.setContinuousParts(contPartNum + 9, i, j);
                } else if (contPartNum != 12) {
                    chosenLandTile.setContinuousParts(contPartNum - 3, i, j);
                }
            }
        }
    }

    // Balraforgatás után visszaadja az új összefüggő területeket
    private void setNewContinuousPartsAfterRotateLeft() {
        int contPartNum;
        for (int i = 0; i < chosenLandTile.getContinuousParts().length; i++) {
            for (int j = 0; j < chosenLandTile.getContinuousParts()[i].length; j++) {
                contPartNum = chosenLandTile.getContinuousParts()[i][j];
                if (contPartNum == 9 || contPartNum == 10 || contPartNum == 11) {
                    chosenLandTile.setContinuousParts(contPartNum - 9, i, j);
                } else if (contPartNum != 12) {
                    chosenLandTile.setContinuousParts(contPartNum + 3, i, j);
                }
            }
        }
    }

    // A helytelen elhelyezések letiltása
    private void forbidIllegalPlaces() {
        // Törli a régebbi eltárolt elemeket
        forbiddenPlacesOnTheTable.clear();
        enabledPlacesOnTheTable.clear();
        int x;
        int y;
        for (LandTile lt : locatedLandTiles) {
            x = lt.getPositionOnTheTable().x;
            y = lt.getPositionOnTheTable().y;
            addForbiddenPointsToList(x, y - 1, 8, 0, lt);
            addForbiddenPointsToList(x + 1, y, 11, 3, lt);
            addForbiddenPointsToList(x, y + 1, 0, 8, lt);
            addForbiddenPointsToList(x - 1, y, 3, 11, lt);
        }
    }

    // Hozzáad elhelyezéseket a letiltott és az elérhető helyeket tároló listákhoz
    private void addForbiddenPointsToList(int x, int y, int index1, int index2, LandTile lt) {
        // Ha a vizsgált cellán nincs még kártya
        if (cells[x][y].getLandTile() == null) {
            // Ha a szomszédos hely csatlakozás szempontjából nem jó
            if (!neighboringComponentsAreEqual(lt, index1, index2)) {
                forbiddenPlacesOnTheTable.add(new Point(x, y)); // Hozzáasja a letiltott listához az adott cella pozíciót
            // Ha a szomszédos hely csatlakozás szempontjából jó
            } else if (isPlaceEnabled(x, y)) {
                enabledPlacesOnTheTable.add(new Point(x, y)); // Hozzáasja az elérhető listához az adott cella pozíciót
                landTileCanBeLocated = true;
            }
        }
    }

    // Levizsgálja, hogy az adott cella elérhető-e a kihúzott kártya elhelyezésének szempontjából
    private boolean isPlaceEnabled(int x, int y) {
        if (cells[x][y - 1].getLandTile() != null && !neighboringComponentsAreEqual(cells[x][y - 1].getLandTile(), 0, 8)) {
            return false;
        } else if (cells[x + 1][y].getLandTile() != null && !neighboringComponentsAreEqual(cells[x + 1][y].getLandTile(), 3, 11)) {
            return false;
        } else if (cells[x][y + 1].getLandTile() != null && !neighboringComponentsAreEqual(cells[x][y + 1].getLandTile(), 8, 0)) {
            return false;
        } else if (cells[x - 1][y].getLandTile() != null && !neighboringComponentsAreEqual(cells[x - 1][y].getLandTile(), 11, 3)) {
            return false;
        }
        return true;
    }

    // Levizsgálja, hogy egy elhelyezett kártya egy adott oldal és a kihúzott kártya egy adott oldala megegyezik-e az elemek szempontjából
    // LandTile lt : a vizsgált elhelyezett kártya
    // int index1 : a kihúzott kártya vizsgálandó oldalának egy indexe
    // int index2 : az lt vizsgálandó oldalának egy indexe
    private boolean neighboringComponentsAreEqual(LandTile lt, int index1, int index2) {
        if (index1 >= 8) {
            if (chosenLandTile.getComponents()[index1] != lt.getComponents()[index2]
                    && !(chosenLandTile.getComponents()[index1] == CITY && lt.getComponents()[index2] == CITYWITHPENNANT)
                    && !(chosenLandTile.getComponents()[index1] == CITYWITHPENNANT && lt.getComponents()[index2] == CITY)) {
                return false;
            } else if (chosenLandTile.getComponents()[index1 - 1] != lt.getComponents()[index2 + 1]
                    && !(chosenLandTile.getComponents()[index1 - 1] == CITY && lt.getComponents()[index2 + 1] == CITYWITHPENNANT)
                    && !(chosenLandTile.getComponents()[index1 - 1] == CITYWITHPENNANT && lt.getComponents()[index2 + 1] == CITY)) {
                return false;
            } else if (chosenLandTile.getComponents()[index1 - 2] != lt.getComponents()[index2 + 2]
                    && !(chosenLandTile.getComponents()[index1 - 2] == CITY && lt.getComponents()[index2 + 2] == CITYWITHPENNANT)
                    && !(chosenLandTile.getComponents()[index1 - 2] == CITYWITHPENNANT && lt.getComponents()[index2 + 2] == CITY)) {
                return false;
            }
        } else {
            if (chosenLandTile.getComponents()[index1] != lt.getComponents()[index2]
                    && !(chosenLandTile.getComponents()[index1] == CITY && lt.getComponents()[index2] == CITYWITHPENNANT)
                    && !(chosenLandTile.getComponents()[index1] == CITYWITHPENNANT && lt.getComponents()[index2] == CITY)) {
                return false;
            } else if (chosenLandTile.getComponents()[index1 + 1] != lt.getComponents()[index2 - 1]
                    && !(chosenLandTile.getComponents()[index1 + 1] == CITY && lt.getComponents()[index2 - 1] == CITYWITHPENNANT)
                    && !(chosenLandTile.getComponents()[index1 + 1] == CITYWITHPENNANT && lt.getComponents()[index2 - 1] == CITY)) {
                return false;
            } else if (chosenLandTile.getComponents()[index1 + 2] != lt.getComponents()[index2 - 2]
                    && !(chosenLandTile.getComponents()[index1 + 2] == CITY && lt.getComponents()[index2 - 2] == CITYWITHPENNANT)
                    && !(chosenLandTile.getComponents()[index1 + 2] == CITYWITHPENNANT && lt.getComponents()[index2 - 2] == CITY)) {
                return false;
            }
        }
        return true;
    }

    // A kártya asztalon való elhelyezésének függvénye
    public boolean locateLandTileOnTheTable(Point p) {
        // Ha már kihúztak, de még nem raktak le kártyát az adott körben
        if (chosenLandTile != null && !locatedLandTiles.contains(chosenLandTile)) {
            cells[p.x][p.y].setLandTile(chosenLandTile); // Beállítja a paraméterben kapott pozícióban az előzőleg kihúzott krátyát
            chosenLandTile.setPositionOnTheTable(p.x, p.y); // Beállítja az aktuális kártyának a pozícióját
            locatedLandTiles.add(chosenLandTile); // Hozáadja az kártyát az elhelyezett kártyák listájához
            setReservationOfTheLocatedLandTile(p, true);
            initFollowerPointsOnTheLandTile();
            return true;
        }
        return false;
    }

    // A kárya csak próbaképp való elhelyezése (amikor az MI próbálgat)
    public boolean locateLandTileJustForTry(Point p) {
        if (chosenLandTile != null && !locatedLandTiles.contains(chosenLandTile)) {
            cells[p.x][p.y].setLandTile(chosenLandTile);
            locatedLandTiles.add(chosenLandTile);
            chosenLandTile.setPositionOnTheTable(p.x, p.y);
            setReservationOfTheLocatedLandTile(p, false);
            initFollowerPointsOnTheLandTile();
            return true;
        }
        return false;
    }

    // A próbálgatás után az elhelyezés törlése (MI-hez)
    public void removeLocationDatasAfterTrying(Point p) {
        for (int[] contPart : cells[p.x][p.y].getLandTile().getContinuousParts()) {
            cells[p.x][p.y].getLandTile().clearReserved(contPart[0]); // Törli a kártya foglalásait
        }
        cells[p.x][p.y].setLandTile(null); // Törli az adott celláról a táblát
        locatedLandTiles.remove(chosenLandTile); // Törli az elhelyezett kártyák listájából a kártyát
        chosenLandTile.setPositionOnTheTable(-1, -1); // A kártya pozíciója ismét (-1, -1) lesz (nem elhelyezett)
    }

    // Levizsgálja a paraméterben megadott kártyapozíció alapján a mellette levő kártyák foglaltságait, és az alapján beállítja az aktuális kártyának is
    private void checkNeighboringLandTileReservations(Point landTilePos) {
        LandTile actualLandTile = cells[landTilePos.x][landTilePos.y].getLandTile();

        if (actualLandTile != null) {
            setReservedPlaces(actualLandTile, cells[landTilePos.x][landTilePos.y - 1].getLandTile(), 0, 8);
            setReservedPlaces(actualLandTile, cells[landTilePos.x + 1][landTilePos.y].getLandTile(), 3, 11);
            setReservedPlaces(actualLandTile, cells[landTilePos.x][landTilePos.y + 1].getLandTile(), 8, 0);
            setReservedPlaces(actualLandTile, cells[landTilePos.x - 1][landTilePos.y].getLandTile(), 11, 3);
        }
    }

    // Levizsgálja, hogy a paraméterben átadott két kártya a szintén paraméterben átadott pozíciók (valamint az ugyanazon oldalakon levő további pozíciók)
    // foglaltságaik megegyeznek-e, és ha nem, akkor beállítja őket a megfelelőre (rekurzív, ha történt változás, akkor végiggörgeti az összes csatlakozáson)
    private void setReservedPlaces(LandTile actual, LandTile other, int actualStarterPlace, int otherStarterPlace) {
        boolean hasChanged = false;
        if (other != null) {
            if (actualStarterPlace < 8) {
                for (int i = 0; i < 3; i++) {
                    if (!other.getReserved(otherStarterPlace - i).isEmpty() && actual.getReserved(actualStarterPlace + i).isEmpty()) {
                        actual.setReserved(actualStarterPlace + i, other.getReserved(otherStarterPlace - i));
                        hasChanged = true;
                    } else if (other.getReserved(otherStarterPlace - i).isEmpty() && !actual.getReserved(actualStarterPlace + i).isEmpty()) {
                        other.setReserved(otherStarterPlace - i, actual.getReserved(actualStarterPlace + i));
                        hasChanged = true;
                    } else if (!actual.getReserved(actualStarterPlace + i).isEmpty() && actual.getReserved(actualStarterPlace + i).size() > other.getReserved(otherStarterPlace - i).size()
                            && !twoReservedListsAreEquals(actual.getReserved(actualStarterPlace + i), other.getReserved(otherStarterPlace - i)) && !other.equals(chosenLandTile)) {
                        other.clearReserved(otherStarterPlace - i);
                        other.setReserved(otherStarterPlace - i, actual.getReserved(actualStarterPlace + i));
                        hasChanged = true;
                    }
                }
            } else {
                for (int i = 0; i < 3; i++) {
                    if (!other.getReserved(otherStarterPlace + i).isEmpty() && actual.getReserved(actualStarterPlace - i).isEmpty()) {
                        actual.setReserved(actualStarterPlace - i, other.getReserved(otherStarterPlace + i));
                        hasChanged = true;
                    } else if (other.getReserved(otherStarterPlace + i).isEmpty() && !actual.getReserved(actualStarterPlace - i).isEmpty()) {
                        other.setReserved(otherStarterPlace + i, actual.getReserved(actualStarterPlace - i));
                        hasChanged = true;
                    } else if (!actual.getReserved(actualStarterPlace - i).isEmpty() && actual.getReserved(actualStarterPlace - i).size() > other.getReserved(otherStarterPlace + i).size()
                            && !twoReservedListsAreEquals(actual.getReserved(actualStarterPlace - i), other.getReserved(otherStarterPlace + i)) && !other.equals(chosenLandTile)) {
                        other.clearReserved(otherStarterPlace + i);
                        other.setReserved(otherStarterPlace + i, actual.getReserved(actualStarterPlace - i));
                        hasChanged = true;
                    }
                }
            }
        }
        if (hasChanged) {
            int x = actual.getPositionOnTheTable().x;
            int y = actual.getPositionOnTheTable().y;
            checkNeighboringLandTileReservations(new Point(x, y - 1));
            checkNeighboringLandTileReservations(new Point(x + 1, y));
            checkNeighboringLandTileReservations(new Point(x, y + 1));
            checkNeighboringLandTileReservations(new Point(x - 1, y));
        }
    }

    // Levizsgálja, hogy két foglaltságot jellemző lista (alattvalókat tartalmaz) megyegyezik-e
    private boolean twoReservedListsAreEquals(List<Follower> f1, List<Follower> f2) {
        if (f1.size() == f2.size()) {
            for (Follower f : f1) {
                if (!f2.contains(f)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    // Az aktuálisan elhelyezett kártya foglaltságát beállítja a szomszédainak megfelelően
    private void setReservationOfTheLocatedLandTile(Point landTilePos, boolean real) {
        // Minden oldal minden indexére beállítja a szomszédos kártya foglaltságát, ha az létezik és nem üres
        for (int i = 0; i < 12; i++) {
            if (i == 0 || i == 1 || i == 2) {
                if (cells[landTilePos.x][landTilePos.y - 1].getLandTile() != null && !cells[landTilePos.x][landTilePos.y - 1].getLandTile().getReserved(8 - i).isEmpty()) {
                    addReservationToActualLandTile(landTilePos, new Point(landTilePos.x, landTilePos.y - 1), i, 8 - i);
                }
            } else if (i == 3 || i == 4 || i == 5) {
                if (cells[landTilePos.x + 1][landTilePos.y].getLandTile() != null && !cells[landTilePos.x + 1][landTilePos.y].getLandTile().getReserved(14 - i).isEmpty()) {
                    addReservationToActualLandTile(landTilePos, new Point(landTilePos.x + 1, landTilePos.y), i, 14 - i);
                }
            } else if (i == 6 || i == 7 || i == 8) {
                if (cells[landTilePos.x][landTilePos.y + 1].getLandTile() != null && !cells[landTilePos.x][landTilePos.y + 1].getLandTile().getReserved(8 - i).isEmpty()) {
                    addReservationToActualLandTile(landTilePos, new Point(landTilePos.x, landTilePos.y + 1), i, 8 - i);
                }
            } else if (i == 9 || i == 10 || i == 11) {
                if (cells[landTilePos.x - 1][landTilePos.y].getLandTile() != null && !cells[landTilePos.x - 1][landTilePos.y].getLandTile().getReserved(14 - i).isEmpty()) {
                    addReservationToActualLandTile(landTilePos, new Point(landTilePos.x - 1, landTilePos.y), i, 14 - i);
                }
            }
        }
        if (real) {
            checkNeighboringLandTileReservations(landTilePos);
        }

    }

    // A p1 pozíción levő kártya ind1 összefüggő területének foglaltságához hozzáadja a p2 pozíción levő kártyának az ind2 indexű összefüggő területén
    // álló olyan alattvalót, aki még eddig nem szerepelt a p1 kártya in1 összefüggő terület foglaltságában
    private void addReservationToActualLandTile(Point p1, Point p2, int ind1, int ind2) {
        List<Follower> fol = cells[p2.x][p2.y].getLandTile().getReserved(ind2);
        for (Follower f : fol) {
            if (!cells[p1.x][p1.y].getLandTile().containsReservation(ind1, f)) {
                cells[p1.x][p1.y].getLandTile().setReserved(ind1, f);
            }
        }
    }

    // Meghatározza az alattvaló elhelyezéseinek szabályos pontjait
    private void initFollowerPointsOnTheLandTile() {
        pointsOfFollowers = new ArrayList<>();
        // Az összes nem foglalt összefüggő területrészből belerak a listába egy indexet (megjelenítés szempontjából a legkedvezőbbet)
        for (int i = 0; i < chosenLandTile.getContinuousParts().length; i++) {
            if (chosenLandTile.contains(i, 12) && chosenLandTile.getReserved(12).isEmpty()) {
                pointsOfFollowers.add(12);
            } else if (chosenLandTile.contains(i, 1) && chosenLandTile.getReserved(1).isEmpty()) {
                pointsOfFollowers.add(1);
            } else if (chosenLandTile.contains(i, 4) && chosenLandTile.getReserved(4).isEmpty()) {
                pointsOfFollowers.add(4);
            } else if (chosenLandTile.contains(i, 7) && chosenLandTile.getReserved(7).isEmpty()) {
                pointsOfFollowers.add(7);
            } else if (chosenLandTile.contains(i, 10) && chosenLandTile.getReserved(10).isEmpty()) {
                pointsOfFollowers.add(10);
            } else if (chosenLandTile.getReserved(chosenLandTile.getContinuousParts()[i][0]).isEmpty()) {
                pointsOfFollowers.add(chosenLandTile.getContinuousParts()[i][0]);
            }
        }
    }

    // Az alattvaló elhelyezésének függvénye
    public boolean locateFollower(int place) {
        // Ha a játékos rendelkezik szabad alattvalóval
        if (players[turn].getFreeFollowerNumber() > 0) {
            players[turn].setFollowerLocationAndContPartInd(chosenLandTile.getPositionOnTheTable(), place); // Beállítja az alattvaló új helyét, és hogy melyik összefüggő részen szerepel
            chosenLandTile.setReserved(place, players[turn].getFollowerByLocation(chosenLandTile.getPositionOnTheTable())); // Beállítja a kártya új foglaltságát
            checkNeighboringLandTileReservations(chosenLandTile.getPositionOnTheTable()); // Átállítja a csatlakozások foglaltságát is
            return true;
        }     
        return false;
    }

    // A lépések közbeni pontszámításért felelős függvény
    public int[] countPoints() {
        freeFollowersAgainPastLocation.clear();
        int[] point = new int[players.length];
        int[] roadAndCityPoint = countRoadAndCityPoints(); // Az utakból és várakból származó pont
        int[] cloisterPoint = checkWhetherThereIsACloister(); // A kolostorokból származó pont
        for (int i = 0; i < point.length; i++) {
            if (roadAndCityPoint[i] > 0) {
                players[i].addPoint(roadAndCityPoint[i]);
            }
            if (cloisterPoint[i] > 0) {
                players[i].addPoint(cloisterPoint[i]);
            }
        }
        for (int i = 0; i < players.length; i++) {
            point[i] = players[i].getPoint();
        }
        chosenLandTile = null;
        nextTurn(); // A következő játékos következik
        return point;
    }

    private List<Point> done = new ArrayList<>();

    // Az utak és várak pontszámítása
    private int[] countRoadAndCityPoints() {
        int[] points = new int[players.length];
        done.clear();
        // Az elhelyezett kártya minden összefüggő részét megvizsgálja
        for (int i = 0; i < chosenLandTile.getContinuousParts().length; i++) {
            // Ha a területrész várból áll, vagy foglalt és útból áll, és még nem volt levizsgálva
            if (((!chosenLandTile.getReserved(chosenLandTile.getContinuousParts()[i][0]).isEmpty() && chosenLandTile.getType(chosenLandTile.getContinuousParts()[i][0]) == ROAD)
                    || chosenLandTile.getType(chosenLandTile.getContinuousParts()[i][0]) == CITY || chosenLandTile.getType(chosenLandTile.getContinuousParts()[i][0]) == CITYWITHPENNANT)
                    && !done.contains(new Point(chosenLandTile.getId(), i))) {
                int point = roadAndCityPointsRecursive(chosenLandTile, chosenLandTile.getContinuousParts()[i][0]);
                // Ha  területrész foglalt, és a pont az nagyobb mint 0
                if (!chosenLandTile.getReserved(chosenLandTile.getContinuousParts()[i][0]).isEmpty() && point > 0) {
                    List<Integer> freq = getColorOfMostFrequentFollowersOfAContinuousPart(i); // Levizsgálja, hogy mely sorszámú játékosoknak jár a pont
                    for (Integer f : freq) {
                        points[f] += point;
                    }
                }
                // Ha a pont az nagyobb, mint 0
                if (point > 0) {
                    addDoneCityPartsToList(); // A kész várakat eltárolja (mezők pontszámításához kell majd)
                    // Ha a területrész foglalt is
                    if (!chosenLandTile.getReserved(chosenLandTile.getContinuousParts()[i][0]).isEmpty()) {
                        // A befejezett rész alattvalóinak felszabadítása
                        for (Follower f : chosenLandTile.getReserved(chosenLandTile.getContinuousParts()[i][0])) {
                            freeFollowersAgainPastLocation.add(f.getLocation());
                            f.setLocation(new Point(-1, -1));
                            f.setContPartInd(-1);
                        }
                    }
                }
                tempListForCityEdges.clear();
                valueOfContinuousPartToBeSetDone.clear();
            }
        }
        System.out.println("JKNDKJDN: " + citiesOnTheEdge.toString());
        return points;
    }

    // Eltárolja a befejezett várak széletit egy listában
    private void addDoneCityPartsToList() {
        for (int i = 0; i < valueOfContinuousPartToBeSetDone.size(); i++) {
            Point p = tempListForCityEdges.get(i);
            cells[p.x][p.y].getLandTile().setDone(valueOfContinuousPartToBeSetDone.get(i));
        }
        List<Point> landTilePoints = new ArrayList<>();
        for (Point p : tempListForCityEdges) {
            if (!landTilePoints.contains(p)) {
                landTilePoints.add(p);
            }
        }
        if (landTilePoints.size() > 0) {
            citiesOnTheEdge.add(landTilePoints);
        }
    }

    // Visszaadja a körben kihúzott kártya a paraméterben megkapott indexű összefüggő területéről, hogy melyek azok a játékosok, akik a legtöbb
    // alattvalóval foglalják azt (egyenlőség esetén mindegyiket)
    private List<Integer> getColorOfMostFrequentFollowersOfAContinuousPart(int index) {
        List<Follower> followers = chosenLandTile.getReserved(chosenLandTile.getContinuousParts()[index][0]);
        List<Integer> colors = new ArrayList<>();
        for (Follower f : followers) {
            colors.add(f.getColor()); // eltárolja az alattvalók színeit
        }
        Map<Integer, Integer> frequent = new HashMap<>();
        for (int j = 0; j < players.length; j++) {
            frequent.put(j, Collections.frequency(colors, j)); // Az eltárolt színek alapján visszaadja, hogy melyik játékosnak hány alattvalója van az adott területrészen
        }
        List<Integer> freq = new ArrayList<>();
        int max = -1;
        // A területrészen a legtöbb alattvalóval rendelkező játékos(ok) sorszámát eltárolja egy listában
        for (Map.Entry<Integer, Integer> entry : frequent.entrySet()) {
            if (max == -1 || entry.getValue() == max) {
                freq.add(entry.getKey());
                max = entry.getValue();
            } else if (entry.getValue() > max) {
                freq.clear();
                freq.add(entry.getKey());
                max = entry.getValue();
            }
        }
        return freq;
    }

    // Visszaadja a paraméterben megadott kártya azon összefüggő területéről, melyben a paraméterben szereplő érték benne van, hogy melyek azok a játékosok, akik a legtöbb
    // alattvalóval foglalják azt (egyenlőség esetén mindegyiket)
    private List<Integer> getColorOfMostFrequentFollowersOfAContinuousPart(int value, LandTile lt) {
        List<Follower> followers = lt.getReserved(value);
        List<Integer> colors = new ArrayList<>();
        for (Follower f : followers) {
            colors.add(f.getColor()); // eltárolja az alattvalók színeit
        }
        Map<Integer, Integer> frequent = new HashMap<>();
        for (int j = 0; j < players.length; j++) {
            frequent.put(j, Collections.frequency(colors, j)); // Az eltárolt színek alapján visszaadja, hogy melyik játékosnak hány alattvalója van az adott területrészen
        }
        List<Integer> freq = new ArrayList<>();
        int max = -1;
        // A területrészen a legtöbb alattvalóval rendelkező játékos(ok) sorszámát eltárolja egy listában
        for (Map.Entry<Integer, Integer> entry : frequent.entrySet()) {
            if (max == -1 || entry.getValue() == max) {
                freq.add(entry.getKey());
                max = entry.getValue();
            } else if (entry.getValue() > max) {
                freq.clear();
                freq.add(entry.getKey());
                max = entry.getValue();
            }
        }
        return freq;
    }

    List<Point> tempListForCityEdges = new ArrayList<>();
    List<Integer> valueOfContinuousPartToBeSetDone = new ArrayList<>();

    // Rekurzívan kiszámolja az út vagy a vár pontját
    private int roadAndCityPointsRecursive(LandTile actualLandTile, int val) {
        int point = 0;
        int temppoint = 0;
        int ind = getContinuousPartIndexFromValue(actualLandTile, val);
        if ((actualLandTile.getType(val) == CITY || actualLandTile.getType(val) == CITYWITHPENNANT)) {
            tempListForCityEdges.add(actualLandTile.getPositionOnTheTable()); 
            valueOfContinuousPartToBeSetDone.add(val);
        }
        // Ha az aktuális területkártya adott indexű összefüggő része még nem volt levizsgálva a jelenlegi pontszámítás során
        if (!done.contains(new Point(actualLandTile.getId(), ind))) {
            for (int c : actualLandTile.getContinuousParts()[ind]) {
                if (c == 1) {
                    LandTile landTile = cells[actualLandTile.getPositionOnTheTable().x][actualLandTile.getPositionOnTheTable().y - 1].getLandTile(); // A kártya bal oldalán levő kártya
                    // Ha a vizsgált kártya bal oldalán levő csatlakozáson nincs kártya
                    if (landTile == null) {
                        // Ha még nem ért véget a játék
                        if (!endOfGame) {
                            return -1; // A területrész nem fejeződött be, nem jár érte pont, -1 jelzi a sikertelenséget
                        }
                    // Ha a vizsgált kártya bal oldalán levő csatlakozáson van kártya
                    } else {
                        // Ha az aktuális kártya még nem volt levizsgálva a jelenlegi pontszámítás során
                        if (!done.contains(new Point(actualLandTile.getId(), ind))) {
                            done.add(new Point(actualLandTile.getId(), ind)); // Hozzáadjuk a done listához, hogy már le lett vizsgálva a pontszámítás során
                        }
                        // Ha az akutális kártya bal oldalán levő kártya még nem volt levizsgálva a jelenlegi pontszámítás során
                        if (!done.contains(new Point(landTile.getId(), getContinuousPartIndexFromValue(landTile, 7)))) {
                            temppoint = roadAndCityPointsRecursive(landTile, 7); // Visszaadja az innen származó ideiglenes pontszámot
                        }
                        // Ha a visszakapott pontszám negatív (vagyis valahol már nem volt befejezve az összefüggő rész)
                        if (temppoint < 0) {
                            // Ha még nem ért véget a játék
                            if (!endOfGame) {
                                return -1; // A területrész nem fejeződött be, nem jár érte pont, -1 jelzi a sikertelenséget
                            }
                        } else {
                            point += temppoint; // Az eddigi pontot hozzáadja a point változóhoz
                            temppoint = 0;
                        }
                    }
                } else if (c == 4) {
                    LandTile landTile = cells[actualLandTile.getPositionOnTheTable().x + 1][actualLandTile.getPositionOnTheTable().y].getLandTile(); // A kártya alatt levő kártya
                    // Ha a vizsgált kártya alatt levő csatlakozáson nincs kártya
                    if (landTile == null) {
                        // Ha még nem ért véget a játék
                        if (!endOfGame) {
                            return -1; // A területrész nem fejeződött be, nem jár érte pont, -1 jelzi a sikertelenséget
                        }
                    // Ha a vizsgált kártya alatt levő csatlakozáson van kártya
                    } else {
                        // Ha az aktuális kártya még nem volt levizsgálva a jelenlegi pontszámítás során
                        if (!done.contains(new Point(actualLandTile.getId(), ind))) {
                            done.add(new Point(actualLandTile.getId(), ind)); // Hozzáadjuk a done listához, hogy már le lett vizsgálva a pontszámítás során
                        }
                        // Ha az akutális kártya alatt levő kártya még nem volt levizsgálva a jelenlegi pontszámítás során
                        if (!done.contains(new Point(landTile.getId(), getContinuousPartIndexFromValue(landTile, 10)))) {
                            temppoint = roadAndCityPointsRecursive(landTile, 10); // Visszaadja az innen származó ideiglenes pontszámot
                        }
                        // Ha a visszakapott pontszám negatív (vagyis valahol már nem volt befejezve az összefüggő rész)
                        if (temppoint < 0) {
                            // Ha még nem ért véget a játék
                            if (!endOfGame) {
                                return -1; // A területrész nem fejeződött be, nem jár érte pont, -1 jelzi a sikertelenséget
                            }
                        } else {
                            point += temppoint; // Az eddigi pontot hozzáadja a point változóhoz
                            temppoint = 0;
                        }
                    }
                } else if (c == 7) {
                    LandTile landTile = cells[actualLandTile.getPositionOnTheTable().x][actualLandTile.getPositionOnTheTable().y + 1].getLandTile(); // A kártya jobb oldalán levő kártya
                    // Ha a vizsgált kártya jobb oldalán levő csatlakozáson nincs kártya
                    if (landTile == null) {
                        // Ha még nem ért véget a játék
                        if (!endOfGame) {
                            return -1; // A területrész nem fejeződött be, nem jár érte pont, -1 jelzi a sikertelenséget
                        }
                    // Ha a vizsgált kártya jobb oldalán levő csatlakozáson van kártya
                    } else {
                        // Ha az aktuális kártya még nem volt levizsgálva a jelenlegi pontszámítás során
                        if (!done.contains(new Point(actualLandTile.getId(), ind))) {
                            done.add(new Point(actualLandTile.getId(), ind)); // Hozzáadjuk a done listához, hogy már le lett vizsgálva a pontszámítás során
                        }
                        // Ha az akutális kártya jobb oldalán levő kártya még nem volt levizsgálva a jelenlegi pontszámítás során
                        if (!done.contains(new Point(landTile.getId(), getContinuousPartIndexFromValue(landTile, 1)))) {
                            temppoint = roadAndCityPointsRecursive(landTile, 1); // Visszaadja az innen származó ideiglenes pontszámot
                        }
                        // Ha a visszakapott pontszám negatív (vagyis valahol már nem volt befejezve az összefüggő rész)
                        if (temppoint < 0) {
                            // Ha még nem ért véget a játék
                            if (!endOfGame) {
                                return -1; // A területrész nem fejeződött be, nem jár érte pont, -1 jelzi a sikertelenséget
                            }
                        } else {
                            point += temppoint; // Az eddigi pontot hozzáadja a point változóhoz
                            temppoint = 0;
                        }
                    }
                } else if (c == 10) {
                    LandTile landTile = cells[actualLandTile.getPositionOnTheTable().x - 1][actualLandTile.getPositionOnTheTable().y].getLandTile(); // A kártya felett levő kártya
                    // Ha a vizsgált kártya felett levő csatlakozáson nincs kártya
                    if (landTile == null) {
                        // Ha még nem ért véget a játék
                        if (!endOfGame) {
                            return -1; // A területrész nem fejeződött be, nem jár érte pont, -1 jelzi a sikertelenséget
                        }
                    // Ha a vizsgált kártya felett levő csatlakozáson van kártya
                    } else {
                        // Ha az aktuális kártya még nem volt levizsgálva a jelenlegi pontszámítás során
                        if (!done.contains(new Point(actualLandTile.getId(), ind))) {
                            done.add(new Point(actualLandTile.getId(), ind)); // Hozzáadjuk a done listához, hogy már le lett vizsgálva a pontszámítás során
                        }
                        // Ha az akutális kártya felett levő kártya még nem volt levizsgálva a jelenlegi pontszámítás során
                        if (!done.contains(new Point(landTile.getId(), getContinuousPartIndexFromValue(landTile, 4)))) {
                            temppoint = roadAndCityPointsRecursive(landTile, 4); // Visszaadja az innen származó ideiglenes pontszámot
                        }
                        // Ha a visszakapott pontszám negatív (vagyis valahol már nem volt befejezve az összefüggő rész)
                        if (temppoint < 0) {
                            // Ha még nem ért véget a játék
                            if (!endOfGame) {
                                return -1; // A területrész nem fejeződött be, nem jár érte pont, -1 jelzi a sikertelenséget
                            }
                        } else {
                            point += temppoint; // Az eddigi pontot hozzáadja a point változóhoz
                            temppoint = 0;
                        }
                    }
                }
            }
        }
        // Ha az aktuális kártya létezik
        if (actualLandTile != null) {
            point += actualLandTile.getType(actualLandTile.getContinuousParts()[ind][0]); // A ponthoz hozzáadja az elem típusának logikailag tárolt értékét
                                                                                          //(út esetén 1, város estén 2, címeses város esetén 4 pont)
        }
        return point;
    }

    // Érték alapján visszaadja, hogy az melyik összefüggő részhez tartozik
    private int getContinuousPartIndexFromValue(LandTile landTile, int val) {
        for (int i = 0; i < landTile.getContinuousParts().length; i++) {
            if (landTile.contains(i, val)) {
                return i;
            }
        }
        return -1;
    }

    // Levizsgálja, hogy az elhelyezett lap közelében (önmaga és a körülötte levő 8 cella) van-e kolostoros kártya
    private int[] checkWhetherThereIsACloister() {
        int[] point = new int[players.length];
        int x = chosenLandTile.getPositionOnTheTable().x;
        int y = chosenLandTile.getPositionOnTheTable().y;
        // A levizsgálandó pozíciók
        Point[] checkingLandTiles = new Point[]{new Point(x, y), new Point(x - 1, y - 1), new Point(x, y - 1), new Point(x + 1, y - 1), new Point(x + 1, y), new Point(x + 1, y + 1),
            new Point(x, y + 1), new Point(x - 1, y + 1), new Point(x - 1, y)};
        for (Point p : checkingLandTiles) {
            // Ha a vizsgált cellán van kártya, és annak közepén kolostor van, ami le van foglalva valaki által
            if (cells[p.x][p.y].getLandTile() != null && cells[p.x][p.y].getLandTile().getComponents()[12] == CLOISTER && !cells[p.x][p.y].getLandTile().getReserved(12).isEmpty()) {
                point[cells[p.x][p.y].getLandTile().getReserved(12).get(0).getColor()] += countCloisterPoint(p);
            }
        }
        return point;
    }

    // Kolostoros pontszámítás
    private int countCloisterPoint(Point p) {
        int x = p.x;
        int y = p.y;
        int count = 1;
        // A kártya körüli vizsgálandó pozíciók
        Point[] neighboringLandTiles = new Point[]{new Point(x - 1, y - 1), new Point(x, y - 1), new Point(x + 1, y - 1), new Point(x + 1, y), new Point(x + 1, y + 1),
            new Point(x, y + 1), new Point(x - 1, y + 1), new Point(x - 1, y)};
        for (Point point : neighboringLandTiles) {
            // Ha valamelyik cellán nincs még kártya
            if (cells[point.x][point.y].getLandTile() == null) {
                // Ha a játék még nem ért véget
                if (!endOfGame) {
                    return 0; // 0 pontot ér, mert a kolostor még nem fejeződött be
                }
            } else {
                count++; // növeljük 1-gyel a pontszámot
            }
        }
        if (!endOfGame) {
            freeFollowersAgainPastLocation.add(p); // Kész kolostor esetén az alattvalók felszabadítása
        }
        players[cells[p.x][p.y].getLandTile().getReserved(12).get(0).getColor()].setFollowerFree(p);
        return count;
    }

    // A záróértékelés, amikor a játék véget ért
    public int[] countPointEndOfTheGame() {
        int[] point = new int[players.length]; // A végső pontokat tároló tömb (az elemek a játékosok záróértékeléséből származó pontjai)
        int pointPart;
        for (int i = 0; i < players.length; i++) { // Végigmegy az összes játékoson
            for (Follower f : players[i].getLocatedFollowers()) { // Végigmegy az összes nem szabad alattvalón
                if(f.getLocation().x != -1 && f.getLocation().y != -1) { // Ha valóban el vannak helyezve (időközben felszabadulhattak a záróértékelés egy előbbi pontszámítása során)
                    LandTile lt = cells[f.getLocation().x][f.getLocation().y].getLandTile();
                    if (lt.getType(f.getContPartInd()) == ROAD) { // Ha az adott kártya alattvaló által foglalt összefüggő része út
                        pointPart = roadAndCityPointsRecursive(lt, f.getContPartInd()); // Út pontszámítása: a játék közbeni pontszámítás függvénye
                        List<Integer> freq = getColorOfMostFrequentFollowersOfAContinuousPart(f.getContPartInd(), lt); // Visszaadja, hogy mely játékosoknak jár a pont
                        for (Integer fr : freq) {
                            point[fr] += pointPart; //Pont hozzáadása a point tömb megfelelő eleméhez
                        }
                        // A levizsgált alattvaló felszabadítása
                        for (Follower fr : lt.getReserved(f.getContPartInd())) {
                            fr.setLocation(new Point(-1, -1));
                            fr.setContPartInd(-1);
                        }
                    } else if (lt.getType(f.getContPartInd()) == CITY || lt.getType(f.getContPartInd()) == CITYWITHPENNANT) { // Ha az adott kártya alattvaló által foglalt összefüggő része vár vagy címeres vár
                        pointPart = roadAndCityPointsRecursive(lt, f.getContPartInd()) / 2; // Vár pontszámítása: a játék közbeni pontszámítás függvénye osztva 2-vel (szabály alapján)
                        List<Integer> freq = getColorOfMostFrequentFollowersOfAContinuousPart(f.getContPartInd(), lt); // Visszaadja, hogy mely játékosoknak jár a pont
                        for (Integer fr : freq) {
                            point[fr] += pointPart; //Pont hozzáadása a point tömb megfelelő eleméhez
                        }
                        // A levizsgált alattvaló felszabadítása
                        for (Follower fr : lt.getReserved(f.getContPartInd())) {
                            fr.setLocation(new Point(-1, -1));
                            fr.setContPartInd(-1);
                        }
                    } else if (lt.getType(f.getContPartInd()) == CLOISTER) { // Ha az adott kártya alattvaló által foglalt összefüggő része kolostor
                        point[i] += countCloisterPoint(f.getLocation()); // Kolostor pontszámítása: a játék közbeni pontszámítás függvénye
                    }
                }
            }
        }
        int[] fieldPoints = countFieldPoints(); // A mezők pontszámítása
        for (int i = 0; i < point.length; i++) {
            point[i] += fieldPoints[i]; //Pont hozzáadása a point tömb megfelelő eleméhez
            players[i].addPoint(point[i]); // A játékosok pontjainak frissítése a záróértékelés alapján
        }
        System.out.println("VEGSO PONT: elso jatekos: " + point[0] + ", masodik jatekos: " + point[1]);
        return point;
    }

    // A mezők pontszámítása
    private int[] countFieldPoints() {
        int[] point = new int[players.length];
        List<List<Follower>> fols = new ArrayList<>(); // Eltárolja, hogy mely alattvalókat vizsgálta már le (kész váranként új lista kerül a listába)
        for (int j = 0; j < citiesOnTheEdge.size(); j++) {
            fols.add(new ArrayList<>());
            // Végigmegy az elkészült várak szélső elemein
            for (Point p : citiesOnTheEdge.get(j)) {
                // Végigmegy az adott kártya összefüggő területein
                for (int i = 0; i < cells[p.x][p.y].getLandTile().getContinuousParts().length; i++) {
                    // Ha az összefüggő területrész mező és foglalt és még nem volt levizsgálva és a kártyán vizsgált vérrész közvetlen szomszédja
                    if (cells[p.x][p.y].getLandTile().getType(cells[p.x][p.y].getLandTile().getContinuousParts()[i][0]) == FIELD && !cells[p.x][p.y].getLandTile().getReserved(cells[p.x][p.y].getLandTile().getContinuousParts()[i][0]).isEmpty()
                            && (fols.get(j).isEmpty() || !fols.get(j).contains(cells[p.x][p.y].getLandTile().getReserved(cells[p.x][p.y].getLandTile().getContinuousParts()[i][0]).get(0))) && isFieldNeighborWithTheCity(p, cells[p.x][p.y].getLandTile().getContinuousParts()[i])) { 
                        List<Integer> freq = getColorOfMostFrequentFollowersOfAContinuousPart(cells[p.x][p.y].getLandTile().getContinuousParts()[i][0], cells[p.x][p.y].getLandTile()); // Visszaadja, hogy mely játékosoknak jár a pont
                        for (Integer f : freq) {
                            point[f] += 3; // kész váranként 3 ponttal növeli az adott játékos pontját
                        }
                        for (Follower f : cells[p.x][p.y].getLandTile().getReserved(cells[p.x][p.y].getLandTile().getContinuousParts()[i][0])) {
                            if (!fols.get(j).contains(f)) {
                                fols.get(j).add(f); // Hozzáadja a levizsgált alattvalót a fols listához
                            }
                        }
                    }
                }
            }
        }
        System.out.println("Mező pont első játékos: " + point[0] + ", második játékos: " + point[1]);
        return point;
    }

    // Levizsgálja, hogy a mező területrész (array tömb) szomszédos-e a vizsgált várral
    private boolean isFieldNeighborWithTheCity(Point cityPoint, int[] array) {
        LandTile lt = cells[cityPoint.x][cityPoint.y].getLandTile();
        Integer[] fieldContPart = new Integer[array.length];
        for (int i = 0; i < array.length; i++) {
            fieldContPart[i] = Integer.valueOf(array[i]);
        }
        // Végigmegy a vizsgált kártya összefüggő részein
        for (int[] cp : lt.getContinuousParts()) {
            // Ha a vizsgált rész vár vagy címeres vár
            if (lt.getType(cp[0]) == CITY || lt.getType(cp[0]) == CITYWITHPENNANT) {
                // Levizsgálja, hogy van-e a várnak és a mezőnek szomszédos indexe
                for (int val : cp) {
                    if (val == 0 && Arrays.asList(fieldContPart).contains(11)) {
                        return true;
                    } else if (val == 11 && Arrays.asList(fieldContPart).contains(0)) {
                        return true;
                    } else if (val != 12 && Arrays.asList(fieldContPart).contains(val + 1)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // Visszaadja, hogy véget ért-e a játék
    public boolean isGameEnded() {
        for (LandTile lt : landTiles) { // Végigmegy az összes területkrátyán
            if (lt.getPositionOnTheTable().equals(new Point(-1, -1))) { // Ha valamelyik pozíciójának az értéke (-1, -1), azaz még nem volt kihúzva
                return false;
            }
        }
        endOfGame = true;
        return true;
    }

    // MI által használ függvény
    // Visszaadja, hogy a paraméterben megadott típushoz mely kártyaindexek tartoznak (az aktuális kihúzott kártyát nézi)
    public List<Integer> getActualLandTileComponentIndex(int componentType) {
        List<Integer> indexes = new ArrayList<>();
        if (chosenLandTile != null) {
            for (int i = 0; i < chosenLandTile.getComponents().length; i++) {
                if (chosenLandTile.getType(i) == componentType) {
                    indexes.add(i);
                }
            }
        }
        return indexes;
    }

    // MI által használ függvény
    // Visszaadja, hogy az adott indexet tartalmazó összefüggő részt mely alattvalók foglalják ténylegesen (egyenlőség esetén többet)
    public int[] getReservationOfAComponentOfActualLandTile(int value) {
        int[] colors = new int[players.length];
        if (!chosenLandTile.getReserved(value).isEmpty()) { // Ha a megadott index már foglalt
            for (int i = 0; i < chosenLandTile.getContinuousParts().length; i++) { // Megkeresi, hogy melyik indexű összefüggő részhez tartozik a megadott index
                if (chosenLandTile.contains(i, value)) {
                    for (int j = 0; j < getColorOfMostFrequentFollowersOfAContinuousPart(i).size(); j++) {
                        colors[getColorOfMostFrequentFollowersOfAContinuousPart(i).get(j)]++; //A ténylegesen foglaló színek értéke 1 lesz
                    }
                }
            }
        }
        return colors;
    }

    // MI által használ függvény
    // Visszaadja, hogy az út és várrészek foglaltak-e a soron levő játékoson kívül más által
    public boolean roadAndCityAreNotReservedByOthers() {
        for (int[] contPart : chosenLandTile.getContinuousParts()) {
            for (Follower f : chosenLandTile.getReserved(contPart[0])) {
                if ((chosenLandTile.getType(contPart[0]) == ROAD || chosenLandTile.getType(contPart[0]) == CITY || chosenLandTile.getType(contPart[0]) == CITYWITHPENNANT)
                        && f.getColor() != turn) {
                    return false;
                }
            }
        }
        return true;
    }

    // MI által használ függvény
    // Visszaadja, hogy az adott indexű elem foglalt-e
    public boolean isTheComponentReserved(int value) {
        return !chosenLandTile.getReserved(value).isEmpty();
    }

    // MI által használ függvény
    // Visszaadja, hogy az adott indexet tartalmazó út vagy vár készen van-e
    public boolean isTheBuildedRoadOrCityPartDone(int val) {
        int point = roadAndCityPointsRecursive(chosenLandTile, val);
        done.clear();
        tempListForCityEdges.clear();
        valueOfContinuousPartToBeSetDone.clear();
        return point > -1; //Ha a pontszámítás -1-nél nagyobb értéket ad vissza, akkor befejezőtött a vizsgált terület
    }

    // MI által használ függvény
    // Visszaadja, hogy a paraméterben megadott pozíción van-e kártya elhelyezve
    public boolean doesTablePlaceContainLandTile(Point place) {
        return cells[place.x][place.y].getLandTile() != null;
    }

    // MI által használ függvény
    // Visszaadja, hogy egy adott pozíció melletti 8 cella közül mennyin van kártya elhelyezve
    public int getCloisterSizeNumberOfACertainPlace(Point place) {
        int count = 1;
        int x = place.x;
        int y = place.y;
        if (cells[x - 1][y - 1].getLandTile() != null) {
            count++;
        }
        if (cells[x - 1][y].getLandTile() != null) {
            count++;
        }
        if (cells[x - 1][y + 1].getLandTile() != null) {
            count++;
        }
        if (cells[x][y - 1].getLandTile() != null) {
            count++;
        }
        if (cells[x][y + 1].getLandTile() != null) {
            count++;
        }
        if (cells[x + 1][y - 1].getLandTile() != null) {
            count++;
        }
        if (cells[x + 1][y].getLandTile() != null) {
            count++;
        }
        if (cells[x + 1][y + 1].getLandTile() != null) {
            count++;
        }
        return count;
    }

    // Eltárolja a már összekevert kártyaindexeket sorfolytonosan
    private void initShuffledIdArray() {
        for (int i = 0; i < landTiles.length; i++) {
            shuffledIdArray[i] = landTiles[i].getId();
            System.out.println(shuffledIdArray[i]);
        }
    }

    // MI által használ függvény
    // Visszaadja, hogy az adott pozíció melletti 8 cellán hány olyan cella van, melyen van olyan kolostoros kártya, amely az épp soron levő játékos által foglalt
    public List<Point> listOfCloistersNearToThePoint(Point p) {
        List<Point> points = new ArrayList<>();
        int x = p.x;
        int y = p.y;
        if (cells[x - 1][y - 1].getLandTile() != null && cells[x - 1][y - 1].getLandTile().getType(12) == CLOISTER && !cells[x - 1][y - 1].getLandTile().getReserved(12).isEmpty() && cells[x - 1][y - 1].getLandTile().getReserved(12).get(0).getColor() == turn) {
            points.add(new Point(x - 1, y - 1));
        }
        if (cells[x - 1][y].getLandTile() != null && cells[x - 1][y].getLandTile().getType(12) == CLOISTER && !cells[x - 1][y].getLandTile().getReserved(12).isEmpty() && cells[x - 1][y].getLandTile().getReserved(12).get(0).getColor() == turn) {
            points.add(new Point(x - 1, y));
        }
        if (cells[x - 1][y + 1].getLandTile() != null && cells[x - 1][y + 1].getLandTile().getType(12) == CLOISTER && !cells[x - 1][y + 1].getLandTile().getReserved(12).isEmpty() && cells[x - 1][y + 1].getLandTile().getReserved(12).get(0).getColor() == turn) {
            points.add(new Point(x - 1, y + 1));
        }
        if (cells[x][y - 1].getLandTile() != null && cells[x][y - 1].getLandTile().getType(12) == CLOISTER && !cells[x][y - 1].getLandTile().getReserved(12).isEmpty() && cells[x][y - 1].getLandTile().getReserved(12).get(0).getColor() == turn) {
            points.add(new Point(x, y - 1));
        }
        if (cells[x][y + 1].getLandTile() != null && cells[x][y + 1].getLandTile().getType(12) == CLOISTER && !cells[x][y + 1].getLandTile().getReserved(12).isEmpty() && cells[x][y + 1].getLandTile().getReserved(12).get(0).getColor() == turn) {
            points.add(new Point(x, y + 1));
        }
        if (cells[x + 1][y - 1].getLandTile() != null && cells[x + 1][y - 1].getLandTile().getType(12) == CLOISTER && !cells[x + 1][y - 1].getLandTile().getReserved(12).isEmpty() && cells[x + 1][y - 1].getLandTile().getReserved(12).get(0).getColor() == turn) {
            points.add(new Point(x + 1, y - 1));
        }
        if (cells[x + 1][y].getLandTile() != null && cells[x + 1][y].getLandTile().getType(12) == CLOISTER && !cells[x + 1][y].getLandTile().getReserved(12).isEmpty() && cells[x + 1][y].getLandTile().getReserved(12).get(0).getColor() == turn) {
            points.add(new Point(x + 1, y));
        }
        if (cells[x + 1][y + 1].getLandTile() != null && cells[x + 1][y + 1].getLandTile().getType(12) == CLOISTER && !cells[x + 1][y + 1].getLandTile().getReserved(12).isEmpty() && cells[x + 1][y + 1].getLandTile().getReserved(12).get(0).getColor() == turn) {
            points.add(new Point(x + 1, y + 1));
        }
        return points;
    }

    // MI által használ függvény
    // Levizsgálja, hogy van-e lehetősége az MI-nek egy meglévő várterülethez csatlakozni
    public boolean canTryJoinToAnotherCity(int index, Point p) {
        // Az adott elhelyezés adott indexéről levizsgálja, hogy vár/ címeser vár-e, és ha igen, akkor a azon az oldalon a mellette levő mező melletti, alatti és feletti celláról
        // levizsgálja, hogy van-e rajta kártya, és hogy a megfelelő helyeken ugyanúgy vár/címeres vár van-e, és hogy a foglaltság szempontjából van-e értelme megpróbálkozni csatlakozni másnak a várához
        if (index == 1) { // Ha a kártya bal oldalán van a vizsgált vérrész
            if (cells[p.x + 1][p.y - 1].getLandTile() != null && (cells[p.x + 1][p.y - 1].getLandTile().getType(10) == CITY || cells[p.x + 1][p.y - 1].getLandTile().getType(10) == CITYWITHPENNANT)
                    && isTheReservationConvinientToJoin(10)) { // A tőle 1-gyel balra és 1-gyel lejjebb levő kártyát vizsgálja
                return true;
            } else if (cells[p.x][p.y - 2].getLandTile() != null && (cells[p.x][p.y - 2].getLandTile().getType(7) == CITY || cells[p.x][p.y - 2].getLandTile().getType(7) == CITYWITHPENNANT)
                    && isTheReservationConvinientToJoin(7)) { // A tőle 2-vel balra levő kártyát vizsgálja
                return true;
            } else if (cells[p.x - 1][p.y - 1].getLandTile() != null && (cells[p.x - 1][p.y - 1].getLandTile().getType(4) == CITY || cells[p.x - 1][p.y - 1].getLandTile().getType(4) == CITYWITHPENNANT)
                    && isTheReservationConvinientToJoin(4)) { // A tőle 1-gyel balra és 1-gyel feljebb levő kártyát vizsgálja
                return true;
            }
        } else if (index == 4) { // Ha a kártya alján van a vizsgált vérrész
            if (cells[p.x + 1][p.y - 1].getLandTile() != null && (cells[p.x + 1][p.y - 1].getLandTile().getType(7) == CITY || cells[p.x + 1][p.y - 1].getLandTile().getType(7) == CITYWITHPENNANT)
                    && isTheReservationConvinientToJoin(7)) { // A tőle 1-gyel balra és 1-gyel lejjebb levő kártyát vizsgálja
                return true;
            } else if (cells[p.x + 2][p.y].getLandTile() != null && (cells[p.x + 2][p.y].getLandTile().getType(10) == CITY || cells[p.x + 2][p.y].getLandTile().getType(10) == CITYWITHPENNANT)
                    && isTheReservationConvinientToJoin(10)) { // A tőle 2-vel lejjebb levő kártyát vizsgálja
                return true;
            } else if (cells[p.x + 1][p.y + 1].getLandTile() != null && (cells[p.x + 1][p.y + 1].getLandTile().getType(1) == CITY || cells[p.x + 1][p.y + 1].getLandTile().getType(1) == CITYWITHPENNANT)
                    && isTheReservationConvinientToJoin(1)) { // A tőle 1-gyel jobbra és 1-gyel lejjebb levő kártyát vizsgálja
                return true;
            }
        } else if (index == 7) { // Ha a kártya jobb oldalán van a vizsgált vérrész
            if (cells[p.x - 1][p.y + 1].getLandTile() != null && (cells[p.x - 1][p.y + 1].getLandTile().getType(4) == CITY || cells[p.x - 1][p.y + 1].getLandTile().getType(4) == CITYWITHPENNANT)
                    && isTheReservationConvinientToJoin(4)) { // A tőle 1-gyel jobbra és 1-gyel feljebb levő kártyát vizsgálja
                return true;
            } else if (cells[p.x][p.y + 2].getLandTile() != null && (cells[p.x][p.y + 2].getLandTile().getType(1) == CITY || cells[p.x][p.y + 2].getLandTile().getType(1) == CITYWITHPENNANT)
                    && isTheReservationConvinientToJoin(1)) { // A tőle 2-vel jobbra levő kártyát vizsgálja
                return true;
            } else if (cells[p.x + 1][p.y + 1].getLandTile() != null && (cells[p.x + 1][p.y + 1].getLandTile().getType(10) == CITY || cells[p.x + 1][p.y + 1].getLandTile().getType(10) == CITYWITHPENNANT)
                    && isTheReservationConvinientToJoin(10)) { // A tőle 1-gyel jobbra és 1-gyel lejjebb levő kártyát vizsgálja
                return true;
            }
        } else if (index == 10) { // Ha a kártya tetején van a vizsgált vérrész
            if (cells[p.x - 1][p.y - 1].getLandTile() != null && (cells[p.x - 1][p.y - 1].getLandTile().getType(7) == CITY || cells[p.x - 1][p.y - 1].getLandTile().getType(7) == CITYWITHPENNANT)
                    && isTheReservationConvinientToJoin(7)) { // A tőle 1-gyel balra és 1-gyel feljebb levő kártyát vizsgálja
                return true;
            } else if (cells[p.x - 2][p.y].getLandTile() != null && (cells[p.x - 2][p.y].getLandTile().getType(4) == CITY || cells[p.x - 2][p.y].getLandTile().getType(4) == CITYWITHPENNANT)
                    && isTheReservationConvinientToJoin(4)) { // A tőle 2-vel feljebb levő kártyát vizsgálja
                return true;
            } else if (cells[p.x - 1][p.y + 1].getLandTile() != null && (cells[p.x - 1][p.y + 1].getLandTile().getType(1) == CITY || cells[p.x - 1][p.y + 1].getLandTile().getType(1) == CITYWITHPENNANT)
                    && isTheReservationConvinientToJoin(1)) { // A tőle 1-gyel jobbra és 1-gyel feljebb levő kártyát vizsgálja
                return true;
            }
        }
        return false;
    }

    // MI által használ függvény
    // Levizsgálja, hogy van-e lehetősége az MI-nek egy meglévő úthoz csatlakozni
    public boolean canTryJoinToAnotherRoad(int index, Point p) {
        // Az adott elhelyezés adott indexéről levizsgálja, hogy út-e, és ha igen, akkor a azon az oldalon a mellette levő mező melletti, alatti és feletti celláról
        // levizsgálja, hogy van-e rajta kártya, és hogy a megfelelő helyeken ugyanúgy út van-e, és hogy a foglaltság szempontjából van-e értelme megpróbálkozni csatlakozni másnak a várához
        if (index == 1) { // Ha a kártya bal oldalán van a vizsgált út
            // A tőle 1-gyel balra és 1-gyel lejjebb levő kártyát vizsgálja
            if (cells[p.x + 1][p.y - 1].getLandTile() != null && cells[p.x + 1][p.y - 1].getLandTile().getType(10) == ROAD && isTheReservationConvinientToJoin(10)) {
                return true;
             // A tőle 2-vel balra levő kártyát vizsgálja
            } else if (cells[p.x][p.y - 2].getLandTile() != null && cells[p.x][p.y - 2].getLandTile().getType(7) == ROAD && isTheReservationConvinientToJoin(7)) {
                return true;
            // A tőle 1-gyel balra és 1-gyel feljebb levő kártyát vizsgálja
            } else if (cells[p.x - 1][p.y - 1].getLandTile() != null && cells[p.x - 1][p.y - 1].getLandTile().getType(4) == ROAD && isTheReservationConvinientToJoin(4)) {
                return true;
            }
        } else if (index == 4) { // Ha a kártya alján van a vizsgált út
            // A tőle 1-gyel balra és 1-gyel lejjebb levő kártyát vizsgálja
            if (cells[p.x + 1][p.y - 1].getLandTile() != null && cells[p.x + 1][p.y - 1].getLandTile().getType(7) == ROAD && isTheReservationConvinientToJoin(7)) {
                return true;
            // A tőle 2-vel lejjebb levő kártyát vizsgálja
            } else if (cells[p.x + 2][p.y].getLandTile() != null && cells[p.x + 2][p.y].getLandTile().getType(10) == ROAD && isTheReservationConvinientToJoin(10)) {
                return true;
            // A tőle 1-gyel jobbra és 1-gyel lejjebb levő kártyát vizsgálja  
            } else if (cells[p.x + 1][p.y + 1].getLandTile() != null && cells[p.x + 1][p.y + 1].getLandTile().getType(1) == ROAD && isTheReservationConvinientToJoin(1)) {
                return true;
            }
        } else if (index == 7) { // Ha a kártya jobb oldalán van a vizsgált út
            // A tőle 1-gyel jobbra és 1-gyel feljebb levő kártyát vizsgálja
            if (cells[p.x - 1][p.y + 1].getLandTile() != null && cells[p.x - 1][p.y + 1].getLandTile().getType(4) == ROAD && isTheReservationConvinientToJoin(4)) {
                return true;
            // A tőle 2-vel jobbra levő kártyát vizsgálja
            } else if (cells[p.x][p.y + 2].getLandTile() != null && cells[p.x][p.y + 2].getLandTile().getType(1) == ROAD && isTheReservationConvinientToJoin(1)) {
                return true;
            // A tőle 1-gyel jobbra és 1-gyel lejjebb levő kártyát vizsgálja
            } else if (cells[p.x + 1][p.y + 1].getLandTile() != null && cells[p.x + 1][p.y + 1].getLandTile().getType(10) == ROAD && isTheReservationConvinientToJoin(10)) {
                return true;
            }
        } else if (index == 10) { // Ha a kártya tetején van a vizsgált út
            // A tőle 1-gyel balra és 1-gyel feljebb levő kártyát vizsgálja
            if (cells[p.x - 1][p.y - 1].getLandTile() != null && cells[p.x - 1][p.y - 1].getLandTile().getType(7) == ROAD && isTheReservationConvinientToJoin(7)) {
                return true;
            // A tőle 2-vel feljebb levő kártyát vizsgálja
            } else if (cells[p.x - 2][p.y].getLandTile() != null && cells[p.x - 2][p.y].getLandTile().getType(4) == ROAD && isTheReservationConvinientToJoin(4)) {
                return true;
            // A tőle 1-gyel jobbra és 1-gyel feljebb levő kártyát vizsgálja
            } else if (cells[p.x - 1][p.y + 1].getLandTile() != null && cells[p.x - 1][p.y + 1].getLandTile().getType(1) == ROAD && isTheReservationConvinientToJoin(1)) {
                return true;
            }
        }
        return false;
    }

    public boolean isTheReservationConvinientToJoin(int index) {
        boolean good = false;
        for (Follower f : chosenLandTile.getReserved(index)) {
            if (f.getColor() != turn) {
                good = true;
            }
        }
        if (!good) {
            return false;
        }
        List<Integer> reservation = getColorOfMostFrequentFollowersOfAContinuousPart(index, chosenLandTile);
        return reservation.contains(turn);
    }

    // MI által használ függvény
    // Megadja, hogy a 4 szomszédos cella közül mennyin van kártya elhelyezve
    public int getNeighbourLandTileNumber(Point pos) {
        int count = 0;
        if (cells[pos.x][pos.y - 1].getLandTile() != null) {
            count++;
        }
        if (cells[pos.x + 1][pos.y].getLandTile() != null) {
            count++;
        }
        if (cells[pos.x][pos.y + 1].getLandTile() != null) {
            count++;
        }
        if (cells[pos.x - 1][pos.y].getLandTile() != null) {
            count++;
        }
        return count;
    }

    // MI által használ függvény
    // Megadja, hogy az aktuális kártyán az az összefüggő területrész, melyben a paraméterben szereplő index benne van, foglalt-e
   public boolean isActualLandTilePartReserved(int value) {
        return !chosenLandTile.getReserved(value).isEmpty();
    }

    private List<LandTile> checkedLandTiles = new ArrayList<>();

    // MI által használ függvény
    // Megadja, hogy a kihúzott területkártyán egy adott indexen fekvő mező hány befejezetlen várral érintkezik
    public int getNumberOfStartedCitiesInAField(int val) {
        done.clear();
        checkedLandTiles.clear();
        int count = countStartedCitiesInAField(chosenLandTile, val);
        return count;
    }

    // Megszámolja, hogy a kihúzott kártya val indexű mezője hány legalább megkezdett várral érintkezik (rekurzív)
    private int countStartedCitiesInAField(LandTile actualLandTile, int val) {
        int count = 0;
        int ind = getContinuousPartIndexFromValue(actualLandTile, val); // Megkeresi az a területrész indexet, melyben a val érték benne van
        // Ha az adott területkártya még nem volt levizsgálva
        if (!done.contains(new Point(actualLandTile.getId(), ind))) {
            // Végigmegy az összes összefüggő területén
            for (int[] contPart : actualLandTile.getContinuousParts()) {
                // Ha az adott területrész vár vagy címeres vár
                if ((actualLandTile.getType(contPart[0]) == CITY || actualLandTile.getType(contPart[0]) == CITYWITHPENNANT)) {
                    for (int i : contPart) {
                        for (int in : actualLandTile.getContinuousParts()[ind]) {
                            done2.clear();
                            // Ha a vizsgált mezőnek és a várnak van szomszédos pontja, ami még nem volt levizsgálva
                            if (in > 0) {
                                if (((i == (in + 1) % 12 && in != 12) || (i == in - 1)) && partNotConnectedToACountedOne(actualLandTile, contPart[0])) {
                                    count++; // növeli az értéket
                                    checkedLandTiles.add(actualLandTile);
                                }
                            } else if (in == 0) {
                                if (((i == (in + 1) % 12 && in != 12) || (i == 11)) && partNotConnectedToACountedOne(actualLandTile, contPart[0])) {
                                    count++; // növeli az értéket
                                    checkedLandTiles.add(actualLandTile);
                                }
                            }
                        }
                    }
                }
            }
            // Végigmegy azon a területrészen, melyben a paraméterben szereplő val index benne van
            for (int c : actualLandTile.getContinuousParts()[ind]) {
                // A kártya bal széle
                if (c == 0 || c == 1 || c == 2) {
                    LandTile landTile = cells[actualLandTile.getPositionOnTheTable().x][actualLandTile.getPositionOnTheTable().y - 1].getLandTile(); // A kártya bal oldalán levő kártya
                    if (landTile != null) { // Ha a bal oldali kártya létezik
                        if (!done.contains(new Point(actualLandTile.getId(), ind))) { // Ha az aktuális kártya ind indexű területrésze még nem volt levizsgálva
                            done.add(new Point(actualLandTile.getId(), ind));
                        }
                        if (!done.contains(new Point(landTile.getId(), getContinuousPartIndexFromValue(landTile, 8 - c)))) { //Ha a bal oldali kártya adott indexű területrésze még nem volt levizsgálva
                            count += countStartedCitiesInAField(landTile, 8 - c); // Rekurzívan a pont összeszámolása
                        }
                    }
                // A kártya alja
                } else if (c == 3 || c == 4 || c == 5) {
                    LandTile landTile = cells[actualLandTile.getPositionOnTheTable().x + 1][actualLandTile.getPositionOnTheTable().y].getLandTile(); // A kártya alatt levő kártya
                    if (landTile != null) { // Ha az alsó kártya létezik
                        if (!done.contains(new Point(actualLandTile.getId(), ind))) { // Ha az aktuális kártya ind indexű területrésze még nem volt levizsgálva
                            done.add(new Point(actualLandTile.getId(), ind));
                        }
                        if (!done.contains(new Point(landTile.getId(), getContinuousPartIndexFromValue(landTile, 14 - c)))) { //Ha az alsó kártya adott indexű területrésze még nem volt levizsgálva
                            count += countStartedCitiesInAField(landTile, 14 - c); // Rekurzívan a pont összeszámolása
                        }
                    }
                // A kártya jobb széle
                } else if (c == 6 || c == 7 || c == 8) {
                    LandTile landTile = cells[actualLandTile.getPositionOnTheTable().x][actualLandTile.getPositionOnTheTable().y + 1].getLandTile(); // A kártya jobb oldalán levő kártya
                    if (landTile != null) { // Ha a jobb oldali kártya létezik
                        if (!done.contains(new Point(actualLandTile.getId(), ind))) { // Ha az aktuális kártya ind indexű területrésze még nem volt levizsgálva
                            done.add(new Point(actualLandTile.getId(), ind));
                        }
                        if (!done.contains(new Point(landTile.getId(), getContinuousPartIndexFromValue(landTile, 8 - c)))) { //Ha a jobb oldali kártya adott indexű területrésze még nem volt levizsgálva
                            count += countStartedCitiesInAField(landTile, 8 - c); // Rekurzívan a pont összeszámolása
                        }
                    }
                // A kártya teteje
                } else if (c == 9 || c == 10 || c == 11) {
                    LandTile landTile = cells[actualLandTile.getPositionOnTheTable().x - 1][actualLandTile.getPositionOnTheTable().y].getLandTile();  // A kártya felett levő kártya
                    if (landTile != null) { // Ha a felső kártya létezik
                        if (!done.contains(new Point(actualLandTile.getId(), ind))) { // Ha az aktuális kártya ind indexű területrésze még nem volt levizsgálva
                            done.add(new Point(actualLandTile.getId(), ind));
                        }
                        if (!done.contains(new Point(landTile.getId(), getContinuousPartIndexFromValue(landTile, 14 - c)))) { //Ha a felső kártya adott indexű területrésze még nem volt levizsgálva
                            count += countStartedCitiesInAField(landTile, 14 - c); // Rekurzívan a pont összeszámolása
                        }
                    }
                }
            }
        }
        return count;
    }

    private List<Point> done2 = new ArrayList<>();

    // Levizsgálja, hogy a paraméterben megkapott kártya val indexe nem része-e egy már összeszámolt várnak (rekurzív)
    private boolean partNotConnectedToACountedOne(LandTile actualLandTile, int val) {
        if (checkedLandTiles.contains(actualLandTile)) {
            return false;
        }
        int ind = getContinuousPartIndexFromValue(actualLandTile, val); //visszaadja, hogy melyik indexű összefüggő részben van benne az adott területkártya val indexe
        // Ha ez még nem volt levizsgálva
        if (!done2.contains(new Point(actualLandTile.getId(), ind))) {
            // Végigmegy a területrész indexein
            for (int c : actualLandTile.getContinuousParts()[ind]) {
                // A kártya bal oldala
                if (c == 1) {
                    LandTile landTile = cells[actualLandTile.getPositionOnTheTable().x][actualLandTile.getPositionOnTheTable().y - 1].getLandTile(); // A bal oldali kártya
                    if (landTile != null) { // Ha a bal oldali kártya létezik
                        done2.add(new Point(actualLandTile.getId(), ind)); // Hozzáadja az aktuális kártyát, hogy már le van vizsgálva
                        if (!done2.contains(new Point(landTile.getId(), getContinuousPartIndexFromValue(landTile, 7))) && !partNotConnectedToACountedOne(landTile, 7)) { // Ha még nem volt levizsgálva és része egy összeszámolt várnak
                            return false;
                        }
                    }
                // A kártya alja
                } else if (c == 4) {
                    LandTile landTile = cells[actualLandTile.getPositionOnTheTable().x + 1][actualLandTile.getPositionOnTheTable().y].getLandTile(); // A bal oldali kártya
                    if (landTile != null) { // Ha a bal oldali kártya létezik
                        done2.add(new Point(actualLandTile.getId(), ind)); // Hozzáadja az aktuális kártyát, hogy már le van vizsgálva
                        if (!done2.contains(new Point(landTile.getId(), getContinuousPartIndexFromValue(landTile, 10))) && !partNotConnectedToACountedOne(landTile, 10)) { // Ha még nem volt levizsgálva és része egy összeszámolt várnak
                            return false;
                        }
                    }
                // A kártya jobb oldala
                } else if (c == 7) {
                    LandTile landTile = cells[actualLandTile.getPositionOnTheTable().x][actualLandTile.getPositionOnTheTable().y + 1].getLandTile(); // A jobb oldali kártya
                    if (landTile != null) { // Ha a jobb oldali kártya létezik
                        done2.add(new Point(actualLandTile.getId(), ind)); // Hozzáadja az aktuális kártyát, hogy már le van vizsgálva
                        if (!done2.contains(new Point(landTile.getId(), getContinuousPartIndexFromValue(landTile, 1))) && !partNotConnectedToACountedOne(landTile, 1)) { // Ha még nem volt levizsgálva és része egy összeszámolt várnak
                            return false;
                        }
                    }
                // A kártya teteje
                } else if (c == 10) {
                    LandTile landTile = cells[actualLandTile.getPositionOnTheTable().x - 1][actualLandTile.getPositionOnTheTable().y].getLandTile();  // Az alsó kártya
                    if (landTile != null) { // Ha az alsó kártya létezik
                        done2.add(new Point(actualLandTile.getId(), ind)); // Hozzáadja az aktuális kártyát, hogy már le van vizsgálva
                        if (!done2.contains(new Point(landTile.getId(), getContinuousPartIndexFromValue(landTile, 4))) && !partNotConnectedToACountedOne(landTile, 4)) { // Ha még nem volt levizsgálva és része egy összeszámolt várnak
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public int[] getShuffledIdArray() {
        return shuffledIdArray;
    }

    public Set<Point> getForbiddenPlacesOnTheTable() {
        return forbiddenPlacesOnTheTable;
    }

    public Set<Point> getEnabledPlacesOnTheTable() {
        return enabledPlacesOnTheTable;
    }

    public boolean isLandTileCanBeLocated() {
        return landTileCanBeLocated;
    }

    public List<Integer> getPointsOfFollowers() {
        return pointsOfFollowers;
    }

    // A kör növelése 1-gyel, a körben következő játékos következik
    public void nextTurn() {
        turn = (turn + 1) % players.length;
    }

    // Visszaadja, hogy hanyadik játékos következik a körben
    public int getTurn() {
        return turn;
    }

    // Visszaadja egy tömbben az összes játékosra, hogy hány szabad alattvalója maradt
    public int[] getFreeFollowerNumOfPLayers() {
        int[] followerNums = new int[players.length];
        for (int i = 0; i < players.length; i++) {
            followerNums[i] = players[i].getFreeFollowerNumber();
        }
        return followerNums;
    }

    //Visszaadja, hogy a kör végekor mely alattvalók szabadultak fel
    public List<Point> getFreeFollowersAgainPastLocation() {
        return freeFollowersAgainPastLocation;
    }

    // Visszaadja, hogy a soron levő játékosnak van-e szabad alattvalója
    public boolean playerHasFreeFollower() {
        return players[turn].getFreeFollowerNumber() != 0;
    }

    // A játékosokat növekvő sorrendbe teszi pontszám alapján
    public List<Point> sortPlayersByPoint() {
        Map<Integer, Integer> map = new HashMap<>();
        Map<Integer, Integer> sortedMap = new TreeMap<>(new PlayerPointComparator(map)); //PlayerPointComparator saját osztály oldja meg az összehasonlítást, az alapján fog rendezni
        for (Player p : players) {
            map.put(p.getColor(), p.getPoint()); // Beleteszi a játékos színét és a pontszámát a Map-be
        }
        sortedMap.putAll(map); // A Map rendezése

        List<Point> list = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : sortedMap.entrySet()) {
            list.add(new Point(entry.getKey(), entry.getValue())); // A listába beleteszi a Map elemeit páronként (szín, pont)
        }
        return list;
    }
}
