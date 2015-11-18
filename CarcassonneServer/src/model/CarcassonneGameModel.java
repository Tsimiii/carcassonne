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

public class CarcassonneGameModel {

    private final int FIELD = 0;
    private final int ROAD = 1;
    private final int CITY = 2;
    private final int CITYWITHPENNANT = 4;
    private final int CLOISTER = 3;
    private final int NOTHING = 5;

    private final Player[] players;
    private final LandTileLoader landTileLoader;
    private final LandTile[] landTiles;
    private LandTile chosenLandTile;
    private TableCell[][] cells;
    private final int[] shuffledIdArray;
    private List<LandTile> locatedLandTiles;
    private Set<Point> forbiddenPlacesOnTheTable;
    private Set<Point> enabledPlacesOnTheTable;
    private boolean landTileCanBeLocated;
    private List<Integer> pointsOfFollowers;
    private List<Point> freeFollowersAgainPastLocation = new ArrayList<>();
    private List<List<Point>> citiesOnTheEdge = new ArrayList<>();
    private boolean endOfGame = false;
    private int turn;

    public CarcassonneGameModel(int playerNumber) {
        landTileLoader = new LandTileLoader();
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
        for (int i = 0; i < number; i++) {
            players[i] = new Player(i);
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
            System.out.println("chosen lt after choose: " + chosenLandTile);
            forbidIllegalPlaces();
            checkWhetherLandTileCanBeLocatedAfterRotates();
            return true;
        }
        return false;
    }
    
    public boolean isChoosenLandTileNotNull() {
        if(chosenLandTile == null) {
            return false;
        }
        return true;
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

    private void addForbiddenPointsToList(int x, int y, int index1, int index2, LandTile lt) {
        if (cells[x][y].getLandTile() == null) {
            if (!neighboringComponentsAreEqual(lt, index1, index2)) {
                forbiddenPlacesOnTheTable.add(new Point(x, y));
            } else if(isPlaceEnabled(x,y)) {
                enabledPlacesOnTheTable.add(new Point(x,y));
                landTileCanBeLocated = true;
            }
        }
    }
    
    private boolean isPlaceEnabled(int x, int y) {
        if(cells[x][y-1].getLandTile() != null && !neighboringComponentsAreEqual(cells[x][y-1].getLandTile(), 0, 8)) {
            return false;
        } else if(cells[x+1][y].getLandTile() != null && !neighboringComponentsAreEqual(cells[x+1][y].getLandTile(), 3, 11)) {
            return false;
        } else if(cells[x][y+1].getLandTile() != null && !neighboringComponentsAreEqual(cells[x][y+1].getLandTile(), 8, 0)) {
            return false;
        } else if(cells[x-1][y].getLandTile() != null && !neighboringComponentsAreEqual(cells[x-1][y].getLandTile(), 11, 3)) {
            return false;
        }
        return true;
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
            chosenLandTile.setPositionOnTheTable(p.x, p.y);
            locatedLandTiles.add(chosenLandTile); 
            bliblablo(p, true);

            initFollowerPointsOnTheLandTile();
            return true;
        }
        return false;
    }
    
    public boolean locateLandTileJustForTry(Point p) {
        if (chosenLandTile != null) {
            cells[p.x][p.y].setLandTile(chosenLandTile);
            locatedLandTiles.add(chosenLandTile);
            chosenLandTile.setPositionOnTheTable(p.x, p.y);
            bliblablo(p, false);

            initFollowerPointsOnTheLandTile();
            return true;
        }
        return false;
    }
    
    public void removeLocationDatasAfterTrying(Point p) {
        for(int[] contPart : cells[p.x][p.y].getLandTile().getContinuousParts()) {
            cells[p.x][p.y].getLandTile().clearReserved(contPart[0]);
        }  
        cells[p.x][p.y].setLandTile(null);
        locatedLandTiles.remove(chosenLandTile);
        chosenLandTile.setPositionOnTheTable(-1, -1);
    }

   private void checkNeighboringLandTileReservations(Point landTilePos) {
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
                    if (!other.getReserved(otherStarterPlace - i).isEmpty() && actual.getReserved(actualStarterPlace + i).isEmpty()) {
                        actual.setReserved(actualStarterPlace + i, other.getReserved(otherStarterPlace - i));
                        hasChanged = true;
                        System.out.println("itt1");
                    } else if (other.getReserved(otherStarterPlace - i).isEmpty() && !actual.getReserved(actualStarterPlace + i).isEmpty()) {
                        other.setReserved(otherStarterPlace - i, actual.getReserved(actualStarterPlace + i));
                        hasChanged = true;
                        System.out.println("itt2");
                    } else if(!actual.getReserved(actualStarterPlace + i).isEmpty() && actual.getReserved(actualStarterPlace + i).size() > other.getReserved(otherStarterPlace - i).size()
                            && !twoReservedListsAreEquals(actual.getReserved(actualStarterPlace + i), other.getReserved(otherStarterPlace - i)) && !other.equals(chosenLandTile)) {
                        System.out.println("Mielőtt átállítaná: " + other.getReserved());
                        other.clearReserved(otherStarterPlace - i);
                        other.setReserved(otherStarterPlace - i, actual.getReserved(actualStarterPlace + i));
                        System.out.println("Miután átállította: " + other.getReserved());
                        hasChanged = true;
                        System.out.println("itt3");
                    }
                }
            } else {
                for (int i = 0; i < 3; i++) {
                    if (!other.getReserved(otherStarterPlace + i).isEmpty() && actual.getReserved(actualStarterPlace - i).isEmpty()) {
                        actual.setReserved(actualStarterPlace - i, other.getReserved(otherStarterPlace + i));
                        hasChanged = true;
                        System.out.println("itt4");
                    } else if (other.getReserved(otherStarterPlace + i).isEmpty() && !actual.getReserved(actualStarterPlace - i).isEmpty()) {
                        other.setReserved(otherStarterPlace + i, actual.getReserved(actualStarterPlace - i));
                        hasChanged = true;
                        System.out.println("itt5");
                    } else if(!actual.getReserved(actualStarterPlace - i).isEmpty() && actual.getReserved(actualStarterPlace + i).size() > other.getReserved(otherStarterPlace - i).size()
                            && !twoReservedListsAreEquals(actual.getReserved(actualStarterPlace - i), other.getReserved(otherStarterPlace + i)) && !other.equals(chosenLandTile)) {
                        other.clearReserved(otherStarterPlace + i);
                        other.setReserved(otherStarterPlace + i, actual.getReserved(actualStarterPlace - i));
                        hasChanged = true;
                        System.out.println("itt6");
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
    
        private boolean twoReservedListsAreEquals(List<Follower> f1, List<Follower> f2) {
            System.out.println("2 follow: " + f1.toString() + ", " + f2.toString());
        if(f1.size() == f2.size()) {
            for(Follower f : f1) {
                if(!f2.contains(f)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    
    private void bliblablo(Point landTilePos, boolean real) {
        for(int i=0; i<12; i++) {
            if(i == 0 || i == 1 || i == 2) {
                /*if(cells[landTilePos.x][landTilePos.y - 1].getLandTile() != null)
                    System.out.println("bal: " + cells[landTilePos.x][landTilePos.y - 1].getLandTile().getReserved(8-i));*/
                if(cells[landTilePos.x][landTilePos.y - 1].getLandTile() != null && !cells[landTilePos.x][landTilePos.y - 1].getLandTile().getReserved(8-i).isEmpty()) {
                    addReservationToActualLandTile(landTilePos, new Point(landTilePos.x, landTilePos.y - 1), i, 8-i);
                }
            } else if(i==3 || i==4 || i==5) {
                /*if(cells[landTilePos.x+1][landTilePos.y].getLandTile() != null)
                    System.out.println("alul: " + cells[landTilePos.x+1][landTilePos.y].getLandTile().getReserved(14-i));*/
                if(cells[landTilePos.x+1][landTilePos.y].getLandTile() != null && !cells[landTilePos.x+1][landTilePos.y].getLandTile().getReserved(14-i).isEmpty()) {
                    addReservationToActualLandTile(landTilePos, new Point(landTilePos.x+1, landTilePos.y), i, 14-i);
                }
            } else if(i==6 || i==7 || i==8) {
                /*if(cells[landTilePos.x][landTilePos.y +1].getLandTile() != null)
                    System.out.println("jobb: " + cells[landTilePos.x][landTilePos.y+1].getLandTile().getReserved(8-i));*/
                if(cells[landTilePos.x][landTilePos.y+1].getLandTile() != null && !cells[landTilePos.x][landTilePos.y+1].getLandTile().getReserved(8-i).isEmpty()) {
                    addReservationToActualLandTile(landTilePos, new Point(landTilePos.x, landTilePos.y+1), i, 8-i);
                }
            } else if(i==9 || i==10 || i==11) {
                /*if(cells[landTilePos.x-1][landTilePos.y].getLandTile() != null)
                    System.out.println("felul: " + cells[landTilePos.x-1][landTilePos.y].getLandTile().getReserved(14-i));*/
                if(cells[landTilePos.x-1][landTilePos.y].getLandTile() != null && !cells[landTilePos.x-1][landTilePos.y].getLandTile().getReserved(14-i).isEmpty()) {
                    addReservationToActualLandTile(landTilePos, new Point(landTilePos.x-1, landTilePos.y), i, 14-i);
                }
            }
        }
        //System.out.println("nos: " + cells[landTilePos.x][landTilePos.y].getLandTile().getReserved());
        if(real) {
            checkNeighboringLandTileReservations(landTilePos);
        }
        //System.out.println("nos2: " + cells[landTilePos.x][landTilePos.y].getLandTile().getReserved());
    }
    
    private void addReservationToActualLandTile(Point p1, Point p2, int ind1, int ind2) {
        List<Follower> fol = cells[p2.x][p2.y].getLandTile().getReserved(ind2);
        for(Follower f : fol) {
            if(!cells[p1.x][p1.y].getLandTile().containsReservation(ind1, f)) {
                cells[p1.x][p1.y].getLandTile().setReserved(ind1, f);
            }
        }
    }

    private void initFollowerPointsOnTheLandTile() {
        pointsOfFollowers = new ArrayList<>();
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

    public boolean locateFollower(int place) {
        if(players[turn].getFreeFollowerNumber() > 0) {
            System.out.println(players[turn]);
            System.out.println("TURN: " + turn);
            System.out.println("chosen lt when locate follower: " + chosenLandTile);
            System.out.println("Pos: " + chosenLandTile.getPositionOnTheTable());
            players[turn].setFollowerLocationAndContPartInd(chosenLandTile.getPositionOnTheTable(), place);
            chosenLandTile.setReserved(place, players[turn].getFollowerByLocation(chosenLandTile.getPositionOnTheTable()));
            checkNeighboringLandTileReservations(chosenLandTile.getPositionOnTheTable());
            //bliblablo(chosenLandTile.getPositionOnTheTable());
            return true;
        }
        return false;
    }

        public int[] countPoints() {
            System.out.println("chosen lt when count point: " + chosenLandTile);
        freeFollowersAgainPastLocation.clear();
        int[] point = new int[players.length];
        int[] roadAndCityPoint = countRoadAndCityPoints();
        int[] cloisterPoint = checkWhetherThereIsACloister();
        for (int i = 0; i < point.length; i++) {
            if(roadAndCityPoint[i] > 0) {
                players[i].addPoint(roadAndCityPoint[i]);
            }
            if(cloisterPoint[i] > 0) {
                players[i].addPoint(cloisterPoint[i]);
            }
        }
        for(int i=0; i<players.length; i++) {
            point[i] = players[i].getPoint();
        }
        chosenLandTile = null;
        turn = (turn + 1) % players.length;
        return point;
    }

    private List<Point> done = new ArrayList<>();

    private int[] countRoadAndCityPoints() {
        int[] points = new int[players.length];
        done.clear();
        for (int i = 0; i < chosenLandTile.getContinuousParts().length; i++) {
            if (((!chosenLandTile.getReserved(chosenLandTile.getContinuousParts()[i][0]).isEmpty() && chosenLandTile.getType(chosenLandTile.getContinuousParts()[i][0]) == ROAD)
                    || chosenLandTile.getType(chosenLandTile.getContinuousParts()[i][0]) == CITY || chosenLandTile.getType(chosenLandTile.getContinuousParts()[i][0]) == CITYWITHPENNANT)
                    && !done.contains(new Point(chosenLandTile.getId(), i))) {
                int point = roadAndCityPointsRecursive(chosenLandTile, chosenLandTile.getContinuousParts()[i][0]);
                if(!chosenLandTile.getReserved(chosenLandTile.getContinuousParts()[i][0]).isEmpty() && point > 0) {
                List<Integer> freq = mivan(i);
                    for (Integer f : freq) {
                        points[f] += point;
                    }
                }
                if(point > 0) {
                    addDoneCityPartsToList();
                    if(!chosenLandTile.getReserved(chosenLandTile.getContinuousParts()[i][0]).isEmpty()) {
                        for(Follower f : chosenLandTile.getReserved(chosenLandTile.getContinuousParts()[i][0])) {
                            freeFollowersAgainPastLocation.add(f.getLocation());
                            f.setLocation(new Point(-1,-1));
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
    
    private void addDoneCityPartsToList() {
        for(int i=0; i<valueOfContinuousPartToBeSetDone.size(); i++) {
            Point p = tempListForCityEdges.get(i);
            cells[p.x][p.y].getLandTile().setDone(valueOfContinuousPartToBeSetDone.get(i));
        }
        List<Point> landTilePoints = new ArrayList<>();
        for(Point p : tempListForCityEdges) {
            if(!landTilePoints.contains(p)) {
                landTilePoints.add(p);
            }
        }
        if(landTilePoints.size() > 0) {
            citiesOnTheEdge.add(landTilePoints);
        }
    }
    
    private List<Integer> mivan(int index) {
        List<Follower> followers = chosenLandTile.getReserved(chosenLandTile.getContinuousParts()[index][0]);
        List<Integer> colors = new ArrayList<>();
        for(Follower f : followers) {
            colors.add(f.getColor());
        }
        Map<Integer, Integer> frequent = new HashMap<>();
        for(int j=0; j<players.length; j++) {
            frequent.put(j, Collections.frequency(colors, j));
        }
        List<Integer> freq = new ArrayList<>();
        int max = -1;
        for (Map.Entry<Integer, Integer> entry : frequent.entrySet()) {
            if(max == -1 || entry.getValue() == max) {
                freq.add(entry.getKey());
                max = entry.getValue();
            } else if(entry.getValue() > max) {
                freq.clear();
                freq.add(entry.getKey());
                max = entry.getValue();
            }
        }
        return freq;
    }
    
     private List<Integer> mivan(int value, LandTile lt) {
        List<Follower> followers = lt.getReserved(value);
        List<Integer> colors = new ArrayList<>();
        for(Follower f : followers) {
            colors.add(f.getColor());
        }
        Map<Integer, Integer> frequent = new HashMap<>();
        for(int j=0; j<players.length; j++) {
            frequent.put(j, Collections.frequency(colors, j));
        }
        List<Integer> freq = new ArrayList<>();
        int max = -1;
        for (Map.Entry<Integer, Integer> entry : frequent.entrySet()) {
            if(max == -1 || entry.getValue() == max) {
                freq.add(entry.getKey());
                max = entry.getValue();
            } else if(entry.getValue() > max) {
                freq.clear();
                freq.add(entry.getKey());
                max = entry.getValue();
            }
        }
        return freq;
    }
     
     List<Point> tempListForCityEdges = new ArrayList<>();
     List<Integer> valueOfContinuousPartToBeSetDone = new ArrayList<>();
    
    private int roadAndCityPointsRecursive(LandTile actualLandTile, int val) {
        int point = 0;
        int temppoint = 0;
        int ind = getContinuousPartIndexFromValue(actualLandTile, val);
        if((actualLandTile.getType(val) == CITY || actualLandTile.getType(val) == CITYWITHPENNANT)) {
            tempListForCityEdges.add(actualLandTile.getPositionOnTheTable());
            valueOfContinuousPartToBeSetDone.add(val);
        }
        if (!done.contains(new Point(actualLandTile.getId(), ind))) {
            for (int c : actualLandTile.getContinuousParts()[ind]) {
                if (c == 1) {
                    LandTile landTile = cells[actualLandTile.getPositionOnTheTable().x][actualLandTile.getPositionOnTheTable().y - 1].getLandTile();
                    if (landTile == null) {
                        if(!endOfGame) return -1;
                    } else {
                        if (!done.contains(new Point(actualLandTile.getId(), ind))) {
                            done.add(new Point(actualLandTile.getId(), ind));
                        }
                        if (!done.contains(new Point(landTile.getId(), getContinuousPartIndexFromValue(landTile, 7)))) {
                            temppoint = roadAndCityPointsRecursive(landTile, 7);
                        }
                        if (temppoint < 0) {
                            if(!endOfGame) return -1;
                        } else {
                            point += temppoint;
                            temppoint = 0;
                        }
                    }
                } else if (c == 4) {
                    LandTile landTile = cells[actualLandTile.getPositionOnTheTable().x + 1][actualLandTile.getPositionOnTheTable().y].getLandTile();
                    if (landTile == null) {
                       if(!endOfGame) return -1;
                    } else {
                        if (!done.contains(new Point(actualLandTile.getId(), ind))) {
                            done.add(new Point(actualLandTile.getId(), ind));
                        }
                        if (!done.contains(new Point(landTile.getId(), getContinuousPartIndexFromValue(landTile, 10)))) {
                            temppoint = roadAndCityPointsRecursive(landTile, 10);
                        }
                        if (temppoint< 0) {
                            if(!endOfGame) return -1;
                        } else {
                            point += temppoint;
                            temppoint = 0;
                        }
                    }
                } else if (c == 7) {
                    LandTile landTile = cells[actualLandTile.getPositionOnTheTable().x][actualLandTile.getPositionOnTheTable().y + 1].getLandTile();
                    if (landTile == null) {
                       if(!endOfGame) return -1;
                    } else {
                    if (!done.contains(new Point(actualLandTile.getId(), ind))) {
                        done.add(new Point(actualLandTile.getId(), ind));
                    }
                    if (!done.contains(new Point(landTile.getId(), getContinuousPartIndexFromValue(landTile, 1)))) {
                        temppoint = roadAndCityPointsRecursive(landTile, 1);
                    }
                    if (temppoint < 0) {
                        if(!endOfGame) return -1;
                    } else {
                        point += temppoint;
                        temppoint = 0;
                    }
                    }
                } else if (c == 10) {
                    LandTile landTile = cells[actualLandTile.getPositionOnTheTable().x - 1][actualLandTile.getPositionOnTheTable().y].getLandTile();
                    if (landTile == null) {
                       if(!endOfGame) return -1;
                    } else {
                        if (!done.contains(new Point(actualLandTile.getId(), ind))) {
                            done.add(new Point(actualLandTile.getId(), ind));
                        }
                        if (!done.contains(new Point(landTile.getId(), getContinuousPartIndexFromValue(landTile, 4)))) {
                            temppoint = roadAndCityPointsRecursive(landTile, 4);
                        }
                        if (temppoint < 0) {
                            if(!endOfGame) return -1;
                        } else {
                            point += temppoint;
                            temppoint = 0;
                        }
                    }
                }
            }
        }
        if(actualLandTile != null) {
            point += actualLandTile.getType(actualLandTile.getContinuousParts()[ind][0]);
        }
        return point;
    }
    
    private int getContinuousPartIndexFromValue(LandTile landTile, int val) {
        for (int i = 0; i < landTile.getContinuousParts().length; i++) {
            if (landTile.contains(i, val)) {
                return i;
            }
        }
        return -1;
    }
    
    private int[] checkWhetherThereIsACloister() {
        int[] point = new int[players.length];
        int x = chosenLandTile.getPositionOnTheTable().x;
        int y = chosenLandTile.getPositionOnTheTable().y;
        Point[] checkingLandTiles = new Point[]{new Point(x, y), new Point(x - 1, y - 1), new Point(x, y - 1), new Point(x + 1, y - 1), new Point(x + 1, y), new Point(x + 1, y + 1),
            new Point(x, y + 1), new Point(x - 1, y + 1), new Point(x - 1, y)};
        for (Point p : checkingLandTiles) {
            if (cells[p.x][p.y].getLandTile() != null && cells[p.x][p.y].getLandTile().getComponents()[12] == CLOISTER && !cells[p.x][p.y].getLandTile().getReserved(12).isEmpty()) {
                point[cells[p.x][p.y].getLandTile().getReserved(12).get(0).getColor()] += countCloisterPoint(p);
            }
        }
        return point;
    }
    
    private int countCloisterPoint(Point p) {
        int x = p.x;
        int y = p.y;
        int count = 1;
        Point[] neighboringLandTiles = new Point[]{new Point(x - 1, y - 1), new Point(x, y - 1), new Point(x + 1, y - 1), new Point(x + 1, y), new Point(x + 1, y + 1),
            new Point(x, y + 1), new Point(x - 1, y + 1), new Point(x - 1, y)};
        for (Point point : neighboringLandTiles) {
            if (cells[point.x][point.y].getLandTile() == null) {
                if(!endOfGame) return 0;
            } else {
                count++;
            }
        }
        if(!endOfGame) {
            freeFollowersAgainPastLocation.add(p);
        }
        players[cells[p.x][p.y].getLandTile().getReserved(12).get(0).getColor()].setFollowerFree(p);
        return count;
    }
    
    public int[] countPointEndOfTheGame() {
        int[] point = new int[players.length];
        int pointPart;
        for(int i=0; i<players.length; i++) {
            for(Follower f : players[i].getLocatedFollowers()) {
                LandTile lt = cells[f.getLocation().x][f.getLocation().y].getLandTile();
                if(lt.getType(f.getContPartInd()) == ROAD) {
                    pointPart = roadAndCityPointsRecursive(lt, f.getContPartInd());
                    List<Integer> freq = mivan(f.getContPartInd(), lt);
                    for (Integer fr : freq) {
                        point[fr] += pointPart;
                    }
                    for(Follower fr : lt.getReserved(f.getContPartInd())) {
                        fr.setLocation(new Point(-1,-1));
                        fr.setContPartInd(-1);
                    }
                } else if(lt.getType(f.getContPartInd()) == CITY ||lt.getType(f.getContPartInd()) == CITYWITHPENNANT) {
                    pointPart = roadAndCityPointsRecursive(lt, f.getContPartInd()) / 2;
                    List<Integer> freq = mivan(f.getContPartInd(), lt);
                    for (Integer fr : freq) {
                        point[fr] += pointPart;
                    }
                    for(Follower fr : lt.getReserved(f.getContPartInd())) {
                        fr.setLocation(new Point(-1,-1));
                        fr.setContPartInd(-1);
                    }
                } else if(lt.getType(f.getContPartInd()) == CLOISTER) {
                    point[i] += countCloisterPoint(f.getLocation());
                }
            }
        }
        int[] fieldPoints = countFieldPoints();
        for(int i=0; i<point.length; i++) {
            point[i] += fieldPoints[i];
            players[i].addPoint(point[i]);
        }
        System.out.println("VEGSO PONT: elso jatekos: " + point[0] + ", masodik jatekos: " + point[1]);
        return point;
    }
    
    private int[] countFieldPoints() {
        int[] point = new int[players.length];
        List<List<Follower>> fols = new ArrayList<>();
        for(int j=0; j< citiesOnTheEdge.size(); j++) {
            fols.add(new ArrayList<>());
            for(Point p : citiesOnTheEdge.get(j)) {
                for(int i= 0; i < cells[p.x][p.y].getLandTile().getContinuousParts().length; i++) {
                    if(cells[p.x][p.y].getLandTile().getType(cells[p.x][p.y].getLandTile().getContinuousParts()[i][0]) == FIELD && !cells[p.x][p.y].getLandTile().getReserved(cells[p.x][p.y].getLandTile().getContinuousParts()[i][0]).isEmpty() &&
                           (fols.get(j).isEmpty() || !fols.get(j).contains(cells[p.x][p.y].getLandTile().getReserved(cells[p.x][p.y].getLandTile().getContinuousParts()[i][0]).get(0))) && isFieldNeighborWithTheCity(p, cells[p.x][p.y].getLandTile().getContinuousParts()[i])) {
                        List<Integer> freq = mivan(cells[p.x][p.y].getLandTile().getContinuousParts()[i][0], cells[p.x][p.y].getLandTile());
                        for (Integer f : freq) {
                            point[f] += 3;
                        }
                        //fols.set(j, cells[p.x][p.y].getLandTile().getReserved(cells[p.x][p.y].getLandTile().getContinuousParts()[i][0]));
                        //System.out.println(p.x + ", " + p.y + " terulet-ertek: " + cells[p.x][p.y].getLandTile().getContinuousParts()[i][0]);
                        for(Follower f : cells[p.x][p.y].getLandTile().getReserved(cells[p.x][p.y].getLandTile().getContinuousParts()[i][0])) {
                            if(!fols.get(j).contains(f)) {
                                fols.get(j).add(f);
                            }
                        }
                    }
                }
            }
        }
        System.out.println("Mező pont első játékos: " + point[0] + ", második játékos: " + point[1]);
        return point;
    }
    
    private boolean isFieldNeighborWithTheCity(Point cityPoint, int[] array) {
        LandTile lt = cells[cityPoint.x][cityPoint.y].getLandTile();
        Integer[] fieldContPart = new Integer[array.length];
        for (int i=0; i<array.length; i++) {
            fieldContPart[i] = Integer.valueOf(array[i]);
        }
        for(int[] cp : lt.getContinuousParts()) {
            if(lt.getType(cp[0]) == CITY || lt.getType(cp[0]) == CITYWITHPENNANT) {
                for(int val : cp) {
                    if(val == 0 && Arrays.asList(fieldContPart).contains(11)) {
                        return true;   
                    } else if(val == 11 && Arrays.asList(fieldContPart).contains(0)) {
                        return true;
                    } else if(val != 12 && Arrays.asList(fieldContPart).contains(val+1)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    public boolean isGameEnded() {
        for(LandTile lt : landTiles) {
            if(lt.getPositionOnTheTable().equals(new Point(-1,-1))) {
                return false;
            }
        }
        endOfGame = true;
        return true;
    }
    
    public List<Integer> getActualLandTileComponentIndex(int componentType) {
        List<Integer> indexes = new ArrayList<>();
        if(chosenLandTile != null) {
            for(int i=0; i<chosenLandTile.getComponents().length; i++) {
                if(chosenLandTile.getType(i) == componentType) {
                    indexes.add(i);
                }
            }
        }
        return indexes;
    }
    
    public int[] getReservationOfAComponentOfActualLandTile(int value) {
        int[] colors = new int[players.length];
        if(!chosenLandTile.getReserved(value).isEmpty()) {
            for(int i=0; i<chosenLandTile.getContinuousParts().length; i++) {
                if(chosenLandTile.contains(i, value)) {
                    for(int j= 0; j< mivan(i).size(); j++) {
                        colors[mivan(i).get(j)]++;
                    }
                }
            }
        }
        return colors;
    }
    
    public boolean roadAndCityAreNotReservedByOthers() {
        for(int[] contPart : chosenLandTile.getContinuousParts()) {
            for(Follower f : chosenLandTile.getReserved(contPart[0])) {
                if(f.getColor() != turn) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public boolean isTheComponentReserved(int value) {
        return !chosenLandTile.getReserved(value).isEmpty();
    }
    
    public boolean isTheBuildedRoadOrCityPartDone(int val) {
        int point = roadAndCityPointsRecursive(chosenLandTile, val);
        done.clear();
        tempListForCityEdges.clear();
        valueOfContinuousPartToBeSetDone.clear();
        return point > -1;
    }
    
    public boolean doesTablePlaceContainLandTile(Point place) {
        return cells[place.x][place.y].getLandTile() != null;
    }
    
    public int getCloisterSizeNumberOfACertainPlace(Point place) {
        int count = 1;
        int x = place.x;
        int y = place.y;
        if(cells[x-1][y-1].getLandTile() != null) count++;
        if(cells[x-1][y].getLandTile() != null) count++;
        if(cells[x-1][y+1].getLandTile() != null) count++;
        if(cells[x][y-1].getLandTile() != null) count++;
        if(cells[x][y+1].getLandTile() != null) count++;
        if(cells[x+1][y-1].getLandTile() != null) count++;
        if(cells[x+1][y].getLandTile() != null) count++;
        if(cells[x+1][y+1].getLandTile() != null) count++;
        return count;
    }

    private void initShuffledIdArray() {
        for (int i = 0; i < landTiles.length; i++) {
            shuffledIdArray[i] = landTiles[i].getId();
            System.out.println(shuffledIdArray[i]);
        }
    }
            
    public List<Point> listOfCloistersNearToThePoint(Point p) {
        List<Point> points = new ArrayList<>();
        int x = p.x;
        int y = p.y;
        if(cells[x-1][y-1].getLandTile() != null && cells[x-1][y-1].getLandTile().getType(12) == CLOISTER && !cells[x-1][y-1].getLandTile().getReserved(12).isEmpty() && cells[x-1][y-1].getLandTile().getReserved(12).get(0).getColor() == turn) points.add(new Point(x-1, y-1));
        if(cells[x-1][y].getLandTile() != null && cells[x-1][y].getLandTile().getType(12) == CLOISTER && !cells[x-1][y].getLandTile().getReserved(12).isEmpty() && cells[x-1][y].getLandTile().getReserved(12).get(0).getColor() == turn) points.add(new Point(x-1, y));
        if(cells[x-1][y+1].getLandTile() != null && cells[x-1][y+1].getLandTile().getType(12) == CLOISTER && !cells[x-1][y+1].getLandTile().getReserved(12).isEmpty() && cells[x-1][y+1].getLandTile().getReserved(12).get(0).getColor() == turn) points.add(new Point(x-1, y+1));
        if(cells[x][y-1].getLandTile() != null && cells[x][y-1].getLandTile().getType(12) == CLOISTER && !cells[x][y-1].getLandTile().getReserved(12).isEmpty() && cells[x][y-1].getLandTile().getReserved(12).get(0).getColor() == turn) points.add(new Point(x, y-1));
        if(cells[x][y+1].getLandTile() != null && cells[x][y+1].getLandTile().getType(12) == CLOISTER && !cells[x][y+1].getLandTile().getReserved(12).isEmpty() && cells[x][y+1].getLandTile().getReserved(12).get(0).getColor() == turn) points.add(new Point(x, y+1));
        if(cells[x+1][y-1].getLandTile() != null && cells[x+1][y-1].getLandTile().getType(12) == CLOISTER && !cells[x+1][y-1].getLandTile().getReserved(12).isEmpty() && cells[x+1][y-1].getLandTile().getReserved(12).get(0).getColor() == turn) points.add(new Point(x+1, y-1));
        if(cells[x+1][y].getLandTile() != null && cells[x+1][y].getLandTile().getType(12) == CLOISTER && !cells[x+1][y].getLandTile().getReserved(12).isEmpty() && cells[x+1][y].getLandTile().getReserved(12).get(0).getColor() == turn) points.add(new Point(x+1, y));
        if(cells[x+1][y+1].getLandTile() != null && cells[x+1][y+1].getLandTile().getType(12) == CLOISTER && !cells[x+1][y+1].getLandTile().getReserved(12).isEmpty() && cells[x+1][y+1].getLandTile().getReserved(12).get(0).getColor() == turn) points.add(new Point(x+1, y+1));
        return points;
    }
    
    public boolean canTryJoinToAnotherCity(int index, Point p) {
        if(index == 1) {
            if(cells[p.x+1][p.y-1].getLandTile() != null && (cells[p.x+1][p.y-1].getLandTile().getType(10) == CITY || cells[p.x+1][p.y-1].getLandTile().getType(10) == CITYWITHPENNANT)
                    && isTheReservationConvinientToJoin(10)) {
                return true;
            } else if(cells[p.x][p.y-2].getLandTile() != null && (cells[p.x][p.y-2].getLandTile().getType(7) == CITY || cells[p.x][p.y-2].getLandTile().getType(7) == CITYWITHPENNANT)
                    && isTheReservationConvinientToJoin(7)) {
                return true;
            } else if(cells[p.x-1][p.y-1].getLandTile() != null && (cells[p.x-1][p.y-1].getLandTile().getType(4) == CITY || cells[p.x-1][p.y-1].getLandTile().getType(4) == CITYWITHPENNANT)
                    && isTheReservationConvinientToJoin(4)) {
                return true;
            }
        } else if(index == 4) {
            if(cells[p.x+1][p.y-1].getLandTile() != null && (cells[p.x+1][p.y-1].getLandTile().getType(7) == CITY || cells[p.x+1][p.y-1].getLandTile().getType(7) == CITYWITHPENNANT)
                    && isTheReservationConvinientToJoin(7)) {
                return true;
            } else if(cells[p.x+2][p.y].getLandTile() != null && (cells[p.x+2][p.y].getLandTile().getType(10) == CITY || cells[p.x+2][p.y].getLandTile().getType(10) == CITYWITHPENNANT)
                    && isTheReservationConvinientToJoin(10)) {
                return true;
            } else if(cells[p.x+1][p.y+1].getLandTile() != null && (cells[p.x+1][p.y+1].getLandTile().getType(1) == CITY || cells[p.x+1][p.y+1].getLandTile().getType(1) == CITYWITHPENNANT)
                    && isTheReservationConvinientToJoin(1)) {
                return true;
            }
        } else if(index == 7) {
            if(cells[p.x-1][p.y+1].getLandTile() != null && (cells[p.x-1][p.y+1].getLandTile().getType(4) == CITY || cells[p.x-1][p.y+1].getLandTile().getType(4) == CITYWITHPENNANT)
                    && isTheReservationConvinientToJoin(4)) {
                return true;
            } else if(cells[p.x][p.y+2].getLandTile() != null && (cells[p.x][p.y+2].getLandTile().getType(1) == CITY || cells[p.x][p.y+2].getLandTile().getType(1) == CITYWITHPENNANT)
                    && isTheReservationConvinientToJoin(1)) {
                return true;
            } else if(cells[p.x+1][p.y+1].getLandTile() != null && (cells[p.x+1][p.y+1].getLandTile().getType(10) == CITY || cells[p.x+1][p.y+1].getLandTile().getType(10) == CITYWITHPENNANT)
                    && isTheReservationConvinientToJoin(10)) {
                return true;
            }
        } else if(index == 10) {
            if(cells[p.x-1][p.y-1].getLandTile() != null && (cells[p.x-1][p.y-1].getLandTile().getType(7) == CITY || cells[p.x-1][p.y-1].getLandTile().getType(7) == CITYWITHPENNANT)
                    && isTheReservationConvinientToJoin(7)) {
                return true;
            } else if(cells[p.x-2][p.y].getLandTile() != null && (cells[p.x-2][p.y].getLandTile().getType(4) == CITY || cells[p.x-2][p.y].getLandTile().getType(4) == CITYWITHPENNANT)
                    && isTheReservationConvinientToJoin(4)) {
                return true;
            } else if(cells[p.x-1][p.y+1].getLandTile() != null && (cells[p.x-1][p.y+1].getLandTile().getType(1) == CITY || cells[p.x-1][p.y+1].getLandTile().getType(1) == CITYWITHPENNANT)
                    && isTheReservationConvinientToJoin(1)) {
                return true;
            }
        }
        return false;
    }
    
        public boolean canTryJoinToAnotherRoad(int index, Point p) {
        if(index == 1) {
            if(cells[p.x+1][p.y-1].getLandTile() != null && cells[p.x+1][p.y-1].getLandTile().getType(10) == ROAD && isTheReservationConvinientToJoin(10)) {
                return true;
            } else if(cells[p.x][p.y-2].getLandTile() != null && cells[p.x][p.y-2].getLandTile().getType(7) == ROAD && isTheReservationConvinientToJoin(7)) {
                return true;
            } else if(cells[p.x-1][p.y-1].getLandTile() != null && cells[p.x-1][p.y-1].getLandTile().getType(4) == ROAD && isTheReservationConvinientToJoin(4)) {
                return true;
            }
        } else if(index == 4) {
            if(cells[p.x+1][p.y-1].getLandTile() != null && cells[p.x+1][p.y-1].getLandTile().getType(7) == ROAD && isTheReservationConvinientToJoin(7)) {
                return true;
            } else if(cells[p.x+2][p.y].getLandTile() != null && cells[p.x+2][p.y].getLandTile().getType(10) == ROAD && isTheReservationConvinientToJoin(10)) {
                return true;
            } else if(cells[p.x+1][p.y+1].getLandTile() != null && cells[p.x+1][p.y+1].getLandTile().getType(1) == ROAD && isTheReservationConvinientToJoin(1)) {
                return true;
            }
        } else if(index == 7) {
            if(cells[p.x-1][p.y+1].getLandTile() != null && cells[p.x-1][p.y+1].getLandTile().getType(4) == ROAD && isTheReservationConvinientToJoin(4)) {
                return true;
            } else if(cells[p.x][p.y+2].getLandTile() != null && cells[p.x][p.y+2].getLandTile().getType(1) == ROAD && isTheReservationConvinientToJoin(1)) {
                return true;
            } else if(cells[p.x+1][p.y+1].getLandTile() != null && cells[p.x+1][p.y+1].getLandTile().getType(10) == ROAD && isTheReservationConvinientToJoin(10)) {
                return true;
            }
        } else if(index == 10) {
            if(cells[p.x-1][p.y-1].getLandTile() != null && cells[p.x-1][p.y-1].getLandTile().getType(7) == ROAD && isTheReservationConvinientToJoin(7)) {
                return true;
            } else if(cells[p.x-2][p.y].getLandTile() != null && cells[p.x-2][p.y].getLandTile().getType(4) == ROAD && isTheReservationConvinientToJoin(4)) {
                return true;
            } else if(cells[p.x-1][p.y+1].getLandTile() != null && cells[p.x-1][p.y+1].getLandTile().getType(1) == ROAD && isTheReservationConvinientToJoin(1)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isTheReservationConvinientToJoin(int index) {
        boolean good = false;
        for(Follower f : chosenLandTile.getReserved(index)) {
            if(f.getColor() != turn) {
                good = true;
            }
        }
        if(!good) {
            return false;
        }
        List<Integer> reservation = mivan(index, chosenLandTile);
        return reservation.contains(turn);
    }
    
    public int getNeighbourLandTileNumber(Point pos) {
        int count = 0;
        if(cells[pos.x][pos.y-1].getLandTile() != null) count++;
        if(cells[pos.x+1][pos.y].getLandTile() != null) count++;
        if(cells[pos.x][pos.y+1].getLandTile() != null) count++;
        if(cells[pos.x-1][pos.y].getLandTile() != null) count++;
        return count;
    }
    
    public boolean isActualLandTilePartReserved(int value) {
        return !chosenLandTile.getReserved(value).isEmpty();
    }
    
    private List<LandTile> checkedLandTiles = new ArrayList<>();
    
    public int getNumberOfStartedCitiesInAField(int val) {
        done.clear();
        checkedLandTiles.clear();
        int count = countStartedCitiesInAField(chosenLandTile, val);
        return count;
    }
    
    private int countStartedCitiesInAField(LandTile actualLandTile, int val) {
        int count = 0;
        int ind = getContinuousPartIndexFromValue(actualLandTile, val);
        if (!done.contains(new Point(actualLandTile.getId(), ind))) {
            for(int[] contPart : actualLandTile.getContinuousParts()) {
                if((actualLandTile.getType(contPart[0]) == CITY || actualLandTile.getType(contPart[0]) == CITYWITHPENNANT) ) {
                    for(int i : contPart) {
                        for(int in : actualLandTile.getContinuousParts()[ind]) {
                            dinedone.clear();
                            if(in > 0) {
                                if(((i == (in+1)%12 && in!=12) || (i == in-1)/* (actualLandTile.getComponents()[(i+1)%12] == CITY && !actualLandTile.contains(getContinuousPartIndexFromValue(actualLandTile, i), (i+1)%12))*/) && partNotConnectedToACountedOne(actualLandTile, contPart[0])) {
                                    count++;
                                    checkedLandTiles.add(actualLandTile);
                                }
                            } else if(in == 0) {
                                if(((i == (in+1)%12 && in!=12) || (i == 11)/* (actualLandTile.getComponents()[(i+1)%12] == CITY && !actualLandTile.contains(getContinuousPartIndexFromValue(actualLandTile, i), (i+1)%12))*/) && partNotConnectedToACountedOne(actualLandTile, contPart[0])) {
                                    count++;
                                    checkedLandTiles.add(actualLandTile);
                                }
                            }
                        }
                    }
                }
            }
            for (int c : actualLandTile.getContinuousParts()[ind]) {
                if (c == 0 || c == 1 || c == 2) {
                    LandTile landTile = cells[actualLandTile.getPositionOnTheTable().x][actualLandTile.getPositionOnTheTable().y - 1].getLandTile();
                    if (landTile != null) {
                        if (!done.contains(new Point(actualLandTile.getId(), ind))) {
                            done.add(new Point(actualLandTile.getId(), ind));
                        }
                        if (!done.contains(new Point(landTile.getId(), getContinuousPartIndexFromValue(landTile, 8-c)))) {
                            count += countStartedCitiesInAField(landTile, 8-c);
                        }
                    }
                } else if (c== 3 || c == 4 || c == 5) {
                    LandTile landTile = cells[actualLandTile.getPositionOnTheTable().x + 1][actualLandTile.getPositionOnTheTable().y].getLandTile();
                    if (landTile != null) {
                        if (!done.contains(new Point(actualLandTile.getId(), ind))) {
                            done.add(new Point(actualLandTile.getId(), ind));
                        }
                        if (!done.contains(new Point(landTile.getId(), getContinuousPartIndexFromValue(landTile, 14-c)))) {
                            count += countStartedCitiesInAField(landTile, 14-c);
                        }
                    }
                } else if (c == 6 || c == 7 ||c == 8) {
                    LandTile landTile = cells[actualLandTile.getPositionOnTheTable().x][actualLandTile.getPositionOnTheTable().y + 1].getLandTile();
                    if (landTile != null) {
                        if (!done.contains(new Point(actualLandTile.getId(), ind))) {
                            done.add(new Point(actualLandTile.getId(), ind));
                        }
                        if (!done.contains(new Point(landTile.getId(), getContinuousPartIndexFromValue(landTile, 8-c)))) {
                            count += countStartedCitiesInAField(landTile, 8-c);
                        }
                    }
                } else if (c == 9 || c == 10 ||c == 11) {
                    LandTile landTile = cells[actualLandTile.getPositionOnTheTable().x - 1][actualLandTile.getPositionOnTheTable().y].getLandTile();
                    if (landTile != null) {
                        if (!done.contains(new Point(actualLandTile.getId(), ind))) {
                            done.add(new Point(actualLandTile.getId(), ind));
                        }
                        if (!done.contains(new Point(landTile.getId(), getContinuousPartIndexFromValue(landTile, 14-c)))) {
                            count += countStartedCitiesInAField(landTile, 14-c);
                        }
                    }
                }
            }
        }
        return count;
    }
    
    private List<Point> dinedone = new ArrayList<>();
    
    private boolean partNotConnectedToACountedOne(LandTile actualLandTile, int val) {
        if(checkedLandTiles.contains(actualLandTile)) {
            return false;
        }
        int ind = getContinuousPartIndexFromValue(actualLandTile, val);
        if(!dinedone.contains(new Point(actualLandTile.getId(), ind))) {
            for (int c : actualLandTile.getContinuousParts()[ind]) {
                if (c == 1) {
                    LandTile landTile = cells[actualLandTile.getPositionOnTheTable().x][actualLandTile.getPositionOnTheTable().y - 1].getLandTile();
                    if (landTile != null) {
                        dinedone.add(new Point(actualLandTile.getId(), ind));
                        if (!dinedone.contains(new Point(landTile.getId(), getContinuousPartIndexFromValue(landTile, 7))) && !partNotConnectedToACountedOne(landTile, 7)) {
                            return false;
                        }
                    }
                } else if (c == 4) {
                    LandTile landTile = cells[actualLandTile.getPositionOnTheTable().x + 1][actualLandTile.getPositionOnTheTable().y].getLandTile();
                    if (landTile != null)  {
                        dinedone.add(new Point(actualLandTile.getId(), ind));
                        if (!dinedone.contains(new Point(landTile.getId(), getContinuousPartIndexFromValue(landTile, 10))) && !partNotConnectedToACountedOne(landTile, 10)) {
                            return false;
                        }
                    }
                } else if (c == 7) {
                    LandTile landTile = cells[actualLandTile.getPositionOnTheTable().x][actualLandTile.getPositionOnTheTable().y + 1].getLandTile();
                    if (landTile != null) {
                        dinedone.add(new Point(actualLandTile.getId(), ind));
                        if (!dinedone.contains(new Point(landTile.getId(), getContinuousPartIndexFromValue(landTile, 1))) &&!partNotConnectedToACountedOne(landTile, 1)) {
                            return false;
                        }
                    }
                } else if (c == 10) {
                    LandTile landTile = cells[actualLandTile.getPositionOnTheTable().x - 1][actualLandTile.getPositionOnTheTable().y].getLandTile();
                    if (landTile != null) {
                        dinedone.add(new Point(actualLandTile.getId(), ind));
                        if (!dinedone.contains(new Point(landTile.getId(), getContinuousPartIndexFromValue(landTile, 4))) && !partNotConnectedToACountedOne(landTile, 4)) {
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

    public int getTurn() {
        return turn;
    }
    
    public int[] getFreeFollowerNumOfPLayers() {
        int[] followerNums = new int[players.length];
        for(int i=0; i<players.length; i++) {
            followerNums[i] = players[i].getFreeFollowerNumber();
        }
        return followerNums;
    }

    public List<Point> getFreeFollowersAgainPastLocation() {
        return freeFollowersAgainPastLocation;
    }
    
    public boolean playerHasFreeFollower() {
        return players[turn].getFreeFollowerNumber() != 0;
    }
    
    public List<Point> sortPlayersByPoint() {
        Map<Integer, Integer> map = new HashMap<>();
        Map<Integer, Integer> sortedMap = new TreeMap<>(new PlayerPointComparator(map));
        for (Player p : players) {
            map.put(p.getColor(), p.getPoint());
        }
        sortedMap.putAll(map);
        
        List<Point> list = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : sortedMap.entrySet()) {
            System.out.println(entry.getKey()+ " : " + entry.getValue());
            list.add(new Point(entry.getKey(), entry.getValue()));
        }
        
        return list;
    }
}