package model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import model.landtile.LandTile;
import model.landtile.LandTileLoader;
import model.player.Player;
import model.tablecell.TableCell;

public class CarcassonneGameModel {

    private final int FIELD = 0;
    private final int ROAD = 1;
    private final int CITY = 2;
    private final int CITYWITHPENNANT = 4;
    private final int CLOISTER = 3;
    private final int NOTHING = 5;

    private Player[] players;
    private LandTileLoader landTileLoader;
    private LandTile[] landTiles;
    private LandTile chosenLandTile;
    private TableCell[][] cells;
    private int[] shuffledIdArray;
    private List<LandTile> locatedLandTiles;
    private List<Point> forbiddenPlacesOnTheTable;
    private boolean landTileCanBeLocated;
    private List<Integer> pointsOfFollowers;
    private int turn;

    public CarcassonneGameModel(int playerNumber) {
        landTileLoader = new LandTileLoader();
        landTiles = landTileLoader.getLandTiles();
        shuffledIdArray = new int[71];
        chosenLandTile = null;
        locatedLandTiles = new ArrayList<>();
        locatedLandTiles.add(landTileLoader.getStarterLandTile());
        forbiddenPlacesOnTheTable = new ArrayList<>();
        initCells();
        players = new Player[playerNumber];
        initPlayers(playerNumber);
        turn = 0;
        shuffleLandTileArray();
    }

    private void initCells() {
        cells = new TableCell[143][143];
        for (int i = 0; i < 143; i++) {
            for (int j = 0; j < 143; j++) {
                cells[i][j] = new TableCell();
            }
        }
        cells[143 / 2][143 / 2].setLandTile(landTileLoader.getStarterLandTile());
    }
    
    private void initPlayers(int number) {
        for(int i=0; i<number; i++) {
            players[i] = new Player();
        }
    }

    private void shuffleLandTileArray() {
        Collections.shuffle(Arrays.asList(landTiles));
        initShuffledIdArray();
    }

    public boolean chooseFaceDownLandTile(Point p) {
        if (chosenLandTile == null) {
            landTileCanBeLocated = false;
            chosenLandTile = landTiles[p.x * 5 + p.y];
            forbidIllegalPlaces();
            checkWhetherLandTileCanBeLocatedAfterRotates();
            return true;
        }
        return false;
    }

    private boolean checkWhetherLandTileCanBeLocatedAfterRotates() {
        for (int i = 0; i < 4; i++) {
            rotateLeftLandTile();
            if (landTileCanBeLocated) {
                for (int j = 0; j < i + 1; j++) {
                    rotateRightLandTile();
                }
                return true;
            }
        }
        chosenLandTile = null;
        return false;
    }

    public boolean rotateLeftLandTile() {
        if (chosenLandTile != null) {
            setNewContinuousPartsAfterRotateLeft();
            chosenLandTile.setComponents(getLeftRotateArray(chosenLandTile));
            forbidIllegalPlaces();
            return true;
        }
        return false;
    }

    public boolean rotateRightLandTile() {
        if (chosenLandTile != null) {
            setNewContinuousPartsAfterRotateRight();
            chosenLandTile.setComponents(getRightRotateArray(chosenLandTile));
            forbidIllegalPlaces();
            return true;
        }
        return false;
    }

    private int[] getRightRotateArray(LandTile actualLandTile) {
        int[] rotateArray = new int[13];
        int temp1 = actualLandTile.getComponents()[0];
        int temp2 = actualLandTile.getComponents()[1];
        int temp3 = actualLandTile.getComponents()[2];
        for (int i = 0; i < actualLandTile.getComponents().length - 4; i++) {
            rotateArray[i] = actualLandTile.getComponents()[i + 3];
        }
        rotateArray[9] = temp1;
        rotateArray[10] = temp2;
        rotateArray[11] = temp3;
        rotateArray[12] = actualLandTile.getComponents()[12];
        return rotateArray;
    }

    private int[] getLeftRotateArray(LandTile actualLandTile) {
        int[] rotateArray = new int[13];
        int temp1 = actualLandTile.getComponents()[9];
        int temp2 = actualLandTile.getComponents()[10];
        int temp3 = actualLandTile.getComponents()[11];
        for (int i = 0; i < actualLandTile.getComponents().length - 4; i++) {
            rotateArray[i + 3] = actualLandTile.getComponents()[i];
        }
        rotateArray[0] = temp1;
        rotateArray[1] = temp2;
        rotateArray[2] = temp3;
        rotateArray[12] = actualLandTile.getComponents()[12];
        return rotateArray;
    }

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

    private void forbidIllegalPlaces() {
        forbiddenPlacesOnTheTable.clear();
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

    private void addForbiddenPointsToList(int x, int y, int index1, int index2, LandTile lt) {
        if (cells[x][y].getLandTile() == null) {
            if (!neighboringComponentsAreEqual(lt, index1, index2)) {
                forbiddenPlacesOnTheTable.add(new Point(x, y));
            } else {
                landTileCanBeLocated = true;
            }
        }
    }

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

    public boolean locateLandTileOnTheTable(Point p) {
        if (chosenLandTile != null) {
            cells[p.x][p.y].setLandTile(chosenLandTile);
            locatedLandTiles.add(chosenLandTile);
            chosenLandTile.setPositionOnTheTable(p.x, p.y);
            checkNeighboringLandTileReservations(p);
            initFollowerPointsOnTheLandTile();
            return true;
        }
        return false;
    }

    private void checkNeighboringLandTileReservations(Point landTilePos) { //még nincs tesztelve!!!
        LandTile actualLandTile = cells[landTilePos.x][landTilePos.y].getLandTile();

        if (actualLandTile != null) {
            setReservedPlaces(actualLandTile, cells[landTilePos.x][landTilePos.y - 1].getLandTile(), 0, 8);
            setReservedPlaces(actualLandTile, cells[landTilePos.x + 1][landTilePos.y].getLandTile(), 3, 11);
            setReservedPlaces(actualLandTile, cells[landTilePos.x][landTilePos.y + 1].getLandTile(), 8, 0);
            setReservedPlaces(actualLandTile, cells[landTilePos.x - 1][landTilePos.y].getLandTile(), 11, 3);
        }
    }

    private void setReservedPlaces(LandTile actual, LandTile other, int actualStarterPlace, int otherStarterPlace) {
        boolean hasChanged = false;
        if (other != null) {
            if (actualStarterPlace < 8) {
                for (int i = 0; i < 3; i++) {
                    if (other.getReserved(otherStarterPlace - i) && !actual.getReserved(actualStarterPlace + i)) {
                        actual.setReserved(actualStarterPlace + i);
                        hasChanged = true;
                    } else if (!other.getReserved(otherStarterPlace - i) && actual.getReserved(actualStarterPlace + i)) {
                        other.setReserved(otherStarterPlace - i);
                        hasChanged = true;
                    }
                }
            } else {
                for (int i = 0; i < 3; i++) {
                    if (other.getReserved(otherStarterPlace + i) && !actual.getReserved(actualStarterPlace - i)) {
                        actual.setReserved(actualStarterPlace - i);
                        hasChanged = true;
                    } else if (!other.getReserved(otherStarterPlace + i) && actual.getReserved(actualStarterPlace - i)) {
                        other.setReserved(otherStarterPlace + i);
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

    private void initFollowerPointsOnTheLandTile() {
        pointsOfFollowers = new ArrayList<>();
        for (int i = 0; i < chosenLandTile.getContinuousParts().length; i++) {
            if (chosenLandTile.contains(i, 12) && !chosenLandTile.getReserved(12)) {
                pointsOfFollowers.add(12);
            } else if (chosenLandTile.contains(i, 1) && !chosenLandTile.getReserved(1)) {
                pointsOfFollowers.add(1);
            } else if (chosenLandTile.contains(i, 4) && !chosenLandTile.getReserved(4)) {
                pointsOfFollowers.add(4);
            } else if (chosenLandTile.contains(i, 7) && !chosenLandTile.getReserved(7)) {
                pointsOfFollowers.add(7);
            } else if (chosenLandTile.contains(i, 10) && !chosenLandTile.getReserved(10)) {
                pointsOfFollowers.add(10);
            } else if (!chosenLandTile.getReserved(chosenLandTile.getContinuousParts()[i][0])) {
                pointsOfFollowers.add(chosenLandTile.getContinuousParts()[i][0]);
            }
        }
    }

    public void locateFollower(int place) {
        chosenLandTile.setReserved(place);
        checkNeighboringLandTileReservations(chosenLandTile.getPositionOnTheTable());
    }

    public int countPoints() {
        int point = 0;
        point += countRoadAndCityPoints();
        point += checkWhetherThereIsACloister();
        chosenLandTile = null;
        turn = (turn + 1) % players.length;
        return point;
    }

    private List<Point> done = new ArrayList<>();

    private int countRoadAndCityPoints() {
        int point = 0;
        done.clear();
        for (int i = 0; i < chosenLandTile.getContinuousParts().length; i++) {
            if (chosenLandTile.getReserved(chosenLandTile.getContinuousParts()[i][0]) && (chosenLandTile.getType(chosenLandTile.getContinuousParts()[i][0]) == ROAD
                    || chosenLandTile.getType(chosenLandTile.getContinuousParts()[i][0]) == CITY || chosenLandTile.getType(chosenLandTile.getContinuousParts()[i][0]) == CITYWITHPENNANT)
                    && !done.contains(new Point(chosenLandTile.getId(), i))) {
                point += roadAndCityPointsRecursive(chosenLandTile, chosenLandTile.getContinuousParts()[i][0]);
                System.out.println("MITTOMÉN: " + point);
            }
        }
        return point;
    }

    private int roadAndCityPointsRecursive(LandTile actualLandTile, int val) {
        int point = 0;
        int temppoint = 0;
        int ind = getContinuousPartIndexFromValue(actualLandTile, val);
        if (!done.contains(new Point(actualLandTile.getId(), ind))) {
            for (int c : actualLandTile.getContinuousParts()[ind]) {
                if (c == 1) {
                    LandTile landTile = cells[actualLandTile.getPositionOnTheTable().x][actualLandTile.getPositionOnTheTable().y - 1].getLandTile();
                    if (landTile == null) {
                        return -1;
                    }
                    if (!done.contains(new Point(actualLandTile.getId(), ind))) {
                        done.add(new Point(actualLandTile.getId(), ind));
                    }
                    if (!done.contains(new Point(landTile.getId(), getContinuousPartIndexFromValue(landTile, 7)))) {
                        temppoint = roadAndCityPointsRecursive(landTile, 7);
                    }
                    if (temppoint < 0) {
                        return -1;
                    } else {
                        point += temppoint;
                        temppoint = 0;
                    }
                } else if (c == 4) {
                    LandTile landTile = cells[actualLandTile.getPositionOnTheTable().x + 1][actualLandTile.getPositionOnTheTable().y].getLandTile();
                    if (landTile == null) {
                        return -1;
                    }
                    if (!done.contains(new Point(actualLandTile.getId(), ind))) {
                        done.add(new Point(actualLandTile.getId(), ind));
                    }
                    if (!done.contains(new Point(landTile.getId(), getContinuousPartIndexFromValue(landTile, 10)))) {
                        temppoint = roadAndCityPointsRecursive(landTile, 10);
                    }
                    if (temppoint < 0) {
                        return -1;
                    } else {
                        point += temppoint;
                        temppoint = 0;
                    }
                } else if (c == 7) {
                    LandTile landTile = cells[actualLandTile.getPositionOnTheTable().x][actualLandTile.getPositionOnTheTable().y + 1].getLandTile();
                    if (landTile == null) {
                        return -1;
                    }
                    if (!done.contains(new Point(actualLandTile.getId(), ind))) {
                        done.add(new Point(actualLandTile.getId(), ind));
                    }
                    if (!done.contains(new Point(landTile.getId(), getContinuousPartIndexFromValue(landTile, 1)))) {
                        temppoint = roadAndCityPointsRecursive(landTile, 1);
                    }
                    if (temppoint < 0) {
                        return -1;
                    } else {
                        point += temppoint;
                        temppoint = 0;
                    }
                } else if (c == 10) {
                    LandTile landTile = cells[actualLandTile.getPositionOnTheTable().x - 1][actualLandTile.getPositionOnTheTable().y].getLandTile();
                    if (landTile == null) {
                        return -1;
                    }
                    if (!done.contains(new Point(actualLandTile.getId(), ind))) {
                        done.add(new Point(actualLandTile.getId(), ind));
                    }
                    if (!done.contains(new Point(landTile.getId(), getContinuousPartIndexFromValue(landTile, 4)))) {
                        temppoint = roadAndCityPointsRecursive(landTile, 4);
                    }
                    if (temppoint < 0) {
                        return -1;
                    } else {
                        point += temppoint;
                        temppoint = 0;
                    }
                }
            }
        }
        point += actualLandTile.getType(actualLandTile.getContinuousParts()[ind][0]);
        return point;
    }
    
    /*private int asd(LandTile actualLandTile, LandTile otherLandTile, int ind, int num) {
        int point = 0;
        if (otherLandTile == null) {
            return -1;
        }
        if (!done.contains(new Point(actualLandTile.getId(), ind))) {
            done.add(new Point(actualLandTile.getId(), ind));
        }
        if (!done.contains(new Point(otherLandTile.getId(), getContinuousPartIndexFromValue(otherLandTile, num)))) {
            point = roadAndCityPointsRecursive(otherLandTile, num);
        }
        return point;
    }*/

    private int getContinuousPartIndexFromValue(LandTile landTile, int val) {
        for (int i = 0; i < landTile.getContinuousParts().length; i++) {
            if (landTile.contains(i, val)) {
                return i;
            }
        }
        return -1;
    }

    private int checkWhetherThereIsACloister() {
        int point = 0;
        int x = chosenLandTile.getPositionOnTheTable().x;
        int y = chosenLandTile.getPositionOnTheTable().y;
        Point[] checkingLandTiles = new Point[]{new Point(x, y), new Point(x - 1, y - 1), new Point(x, y - 1), new Point(x + 1, y - 1), new Point(x + 1, y), new Point(x + 1, y + 1),
            new Point(x, y + 1), new Point(x - 1, y + 1), new Point(x - 1, y)};
        for (Point p : checkingLandTiles) {
            if (cells[p.x][p.y].getLandTile() != null && cells[p.x][p.y].getLandTile().getComponents()[12] == CLOISTER && cells[p.x][p.y].getLandTile().getReserved(12)) {
                point += countCloisterPoint(p);
            }
        }
        return point;
    }

    private int countCloisterPoint(Point p) {
        int x = p.x;
        int y = p.y;
        Point[] neighboringLandTiles = new Point[]{new Point(x - 1, y - 1), new Point(x, y - 1), new Point(x + 1, y - 1), new Point(x + 1, y), new Point(x + 1, y + 1),
            new Point(x, y + 1), new Point(x - 1, y + 1), new Point(x - 1, y)};
        for (Point point : neighboringLandTiles) {
            if (cells[point.x][point.y].getLandTile() == null) {
                return 0;
            }
        }
        return 9;
    }

    private void initShuffledIdArray() {
        for (int i = 0; i < landTiles.length; i++) {
            shuffledIdArray[i] = landTiles[i].getId();
        }
    }

    public int[] getShuffledIdArray() {
        return shuffledIdArray;
    }

    public List<Point> getForbiddenPlacesOnTheTable() {
        return forbiddenPlacesOnTheTable;
    }

    public boolean isLandTileCanBeLocated() {
        return landTileCanBeLocated;
    }

    public List<Integer> getPointsOfFollowers() {
        return pointsOfFollowers;
    }

    public int getTurn() {
        return turn;
    }
}
