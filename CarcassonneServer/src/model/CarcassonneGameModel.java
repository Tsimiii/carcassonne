package model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import model.landtile.LandTile;
import model.landtile.LandTileLoader;
import model.tablecell.TableCell;

public class CarcassonneGameModel {
    
    private final int FIELD = 0;
    private final int ROAD = 1;
    private final int CITY = 2;
    private final int CITYWITHPENNANT = 3;
    private final int CLOISTER = 4;
    private final int NOTHING = 5;

    private LandTileLoader landTileLoader;
    private LandTile[] landTiles;
    private LandTile chosenLandTile;
    private TableCell[][] cells;
    private int[] shuffledIdArray;
    private List<LandTile> locatedLandTiles;
    private List<Point> forbiddenPlacesOnTheTable;
    private boolean landTileCanBeLocated;
    private int[] pointsOfFollowers;

    public CarcassonneGameModel() {
        landTileLoader = new LandTileLoader();
        landTiles = landTileLoader.getLandTiles();
        shuffledIdArray = new int[71];
        chosenLandTile = null;
        locatedLandTiles = new ArrayList<>();
        locatedLandTiles.add(landTileLoader.getStarterLandTile());
        forbiddenPlacesOnTheTable = new ArrayList<>();
        initCells();
        shuffleLandTileArray();
    }
    
    private void initCells() {
        cells = new TableCell[143][143];
        for (int i=0; i<143; i++) {
            for(int j=0; j<143; j++) {
                 cells[i][j] = new TableCell();
            }
        }
        cells[143/2][143/2].setLandTile(landTileLoader.getStarterLandTile());
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
        for(int i=0; i<4; i++) {
            rotateLeftLandTile();
            if(landTileCanBeLocated) {
                for(int j=0; j<i+1; j++) {
                    rotateRightLandTile();
                }
                return true;
            }
        }
        chosenLandTile = null;
        return false;
    }
    
    public boolean rotateLeftLandTile() {
        if(chosenLandTile != null) {
            setNewContinuousPartsAfterRotateLeft(); 
            chosenLandTile.setComponents(getLeftRotateArray(chosenLandTile)); 
            forbidIllegalPlaces();
            return true;
        }
        return false;
    }
    
    public boolean rotateRightLandTile() {
        if(chosenLandTile != null) {
            setNewContinuousPartsAfterRotateRight(); 
            chosenLandTile.setComponents(getRightRotateArray(chosenLandTile)); 
            forbidIllegalPlaces();
            return true;
        }
        return false;
    }
    
    private int[] getRightRotateArray(LandTile actualLandTile){
        int[] rotateArray = new int[13];
        int temp1 = actualLandTile.getComponents()[0];
        int temp2 = actualLandTile.getComponents()[1];
        int temp3 = actualLandTile.getComponents()[2];       
        for(int i=0; i<actualLandTile.getComponents().length-4; i++) {
            rotateArray[i] = actualLandTile.getComponents()[i+3];
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
        for(int i=0; i<actualLandTile.getComponents().length-4; i++) {
            rotateArray[i+3] = actualLandTile.getComponents()[i];
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
        for(LandTile lt: locatedLandTiles) {
            x = lt.getPositionOnTheTable().x;
            y = lt.getPositionOnTheTable().y;
            addForbiddenPointsToList(x,y-1,8,0,lt);
            addForbiddenPointsToList(x+1,y,11,3,lt);
            addForbiddenPointsToList(x,y+1,0,8,lt);
            addForbiddenPointsToList(x-1,y,3,11,lt);
        }
    }
    
    private void addForbiddenPointsToList(int x, int y, int index1, int index2, LandTile lt) {
        if(cells[x][y].getLandTile() == null) {
            if(!neighboringComponentsAreEqual(lt, index1, index2)) {
                forbiddenPlacesOnTheTable.add(new Point(x, y));
            } else {
                landTileCanBeLocated = true;
            }
        }
    }
    
    private boolean neighboringComponentsAreEqual(LandTile lt, int index1, int index2) {
        if(index1 >= 8) {
            if(chosenLandTile.getComponents()[index1] != lt.getComponents()[index2] &&
                    !(chosenLandTile.getComponents()[index1] == CITY && lt.getComponents()[index2] == CITYWITHPENNANT) &&
                        !(chosenLandTile.getComponents()[index1] == CITYWITHPENNANT && lt.getComponents()[index2] == CITY)) {
                return false;
            } else if(chosenLandTile.getComponents()[index1-1] != lt.getComponents()[index2+1] &&
                        !(chosenLandTile.getComponents()[index1-1] == CITY && lt.getComponents()[index2+1] == CITYWITHPENNANT) &&
                            !(chosenLandTile.getComponents()[index1-1] == CITYWITHPENNANT && lt.getComponents()[index2+1] == CITY)) {
                return false;
            } else if(chosenLandTile.getComponents()[index1-2] != lt.getComponents()[index2+2] &&
                        !(chosenLandTile.getComponents()[index1-2] == CITY && lt.getComponents()[index2+2] == CITYWITHPENNANT) &&
                            !(chosenLandTile.getComponents()[index1-2] == CITYWITHPENNANT && lt.getComponents()[index2+2] == CITY)) {
                return false;
            }
        } else {
            if(chosenLandTile.getComponents()[index1] != lt.getComponents()[index2] &&
                    !(chosenLandTile.getComponents()[index1] == CITY && lt.getComponents()[index2] == CITYWITHPENNANT) &&
                        !(chosenLandTile.getComponents()[index1] == CITYWITHPENNANT && lt.getComponents()[index2] == CITY)) {
                return false;
            } else if(chosenLandTile.getComponents()[index1+1] != lt.getComponents()[index2-1] &&
                        !(chosenLandTile.getComponents()[index1+1] == CITY && lt.getComponents()[index2-1] == CITYWITHPENNANT) &&
                            !(chosenLandTile.getComponents()[index1+1] == CITYWITHPENNANT && lt.getComponents()[index2-1] == CITY)) {
                return false;
            } else if(chosenLandTile.getComponents()[index1+2] != lt.getComponents()[index2-2] &&
                        !(chosenLandTile.getComponents()[index1+2] == CITY && lt.getComponents()[index2-2] == CITYWITHPENNANT) &&
                            !(chosenLandTile.getComponents()[index1+2] == CITYWITHPENNANT && lt.getComponents()[index2-2] == CITY)) {
                return false;
            }
        }
        return true;
    }
    
    public boolean locateLandTileOnTheTable(Point p) {
        if(chosenLandTile != null) {
            cells[p.x][p.y].setLandTile(chosenLandTile);
            locatedLandTiles.add(chosenLandTile);
            chosenLandTile.setPositionOnTheTable(p.x, p.y);
            checkNeighboringLandTileReservations(p);
            initFollowerPointsOnTheLandTile();
            chosenLandTile = null;
            return true;
        }
        return false;
    }
    
    private void checkNeighboringLandTileReservations(Point landTilePos) { //még nincs tesztelve!!!
        LandTile actualLandTile = cells[landTilePos.x][landTilePos.y].getLandTile();

        setReservedPlaces(actualLandTile, cells[landTilePos.x][landTilePos.y-1].getLandTile(), 0, 8);
        setReservedPlaces(actualLandTile, cells[landTilePos.x+1][landTilePos.y].getLandTile(), 3, 11);
        setReservedPlaces(actualLandTile, cells[landTilePos.x][landTilePos.y+1].getLandTile(), 8, 0);
        setReservedPlaces(actualLandTile, cells[landTilePos.x-1][landTilePos.y].getLandTile(), 11, 3);
    }
    
    private void setReservedPlaces(LandTile actual, LandTile other, int actualStarterPlace, int otherStarterPlace) {
        boolean hasChanged = false;
        if(other != null) {
            if(actualStarterPlace < 8) {
                for(int i=0; i<3; i++) {
                    if(other.getReserved(otherStarterPlace-i) && !actual.getReserved(actualStarterPlace+i)) {
                        actual.setReserved(actualStarterPlace+i);
                        hasChanged = true;
                    }
                }
            } else {
                for(int i=0; i<3; i++) {
                    if(other.getReserved(otherStarterPlace+i) && !actual.getReserved(actualStarterPlace-i)) {
                        actual.setReserved(actualStarterPlace-i);
                        hasChanged = true;
                    }
                }
            }
        }
        if(hasChanged) {
            checkNeighboringLandTileReservations(other.getPositionOnTheTable());
        }
    }
    
    private void initFollowerPointsOnTheLandTile() {
        pointsOfFollowers = new int[chosenLandTile.getContinuousParts().length];
        for(int i=0; i<chosenLandTile.getContinuousParts().length; i++) {
            if(chosenLandTile.contains(i, 12) && !chosenLandTile.getReserved(12)) {
                pointsOfFollowers[i] = 12;
            } else if(chosenLandTile.contains(i, 1) && !chosenLandTile.getReserved(1)) {
                pointsOfFollowers[i] = 1;
            } else if(chosenLandTile.contains(i, 4) && !chosenLandTile.getReserved(4)) {
                pointsOfFollowers[i] = 4;
            } else if(chosenLandTile.contains(i, 7) && !chosenLandTile.getReserved(7)) {
                pointsOfFollowers[i] = 7;
            } else if(chosenLandTile.contains(i, 10) && !chosenLandTile.getReserved(10)) {
                pointsOfFollowers[i] = 10;
            } else if(!chosenLandTile.getReserved(chosenLandTile.getContinuousParts()[i][0])){
                pointsOfFollowers[i] = chosenLandTile.getContinuousParts()[i][0];
            }
        }
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

    public int[] getPointsOfFollowers() {
        return pointsOfFollowers;
    }
}