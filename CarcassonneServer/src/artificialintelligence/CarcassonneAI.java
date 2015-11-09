package artificialintelligence;

import carcassonneserver.CarcassonneServer;
import java.awt.Point;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import model.CarcassonneGameModel;

public class CarcassonneAI {
    
    private final int FIELD = 0;
    private final int ROAD = 1;
    private final int CITY = 2;
    private final int CITYWITHPENNANT = 4;
    private final int CLOISTER = 3;
    
    private Random random = new Random();
    private List<Point> pointsOfLandTilesCanBeChosed = new ArrayList<>();
    private CarcassonneGameModel carcassonneGameModel;
    public CarcassonneServer delegate;
    
    private int rule = 100;
    private int rotate = -1;
    private Point tablePosition = new Point(-1, -1);
    private int followerPosition = -1;
    private int connectionNumber = 0;
    private int sizeNumber = 0;

    public CarcassonneAI() {
        System.out.println("KÉSZ EGY AI");
        initPointsOfLandTilesCanBeChosed();
    }
    
    private void initPointsOfLandTilesCanBeChosed() {
        for(int i=0; i<14; i++) {
            for(int j=0; j<5; j++) {
                pointsOfLandTilesCanBeChosed.add(new Point(i,j));
            }
        }
        pointsOfLandTilesCanBeChosed.add(new Point(14,0));
    }
    
    public void setGameModel(CarcassonneGameModel carcassonneGameModel) {
        this.carcassonneGameModel = carcassonneGameModel;
    }
    
    public void chooseLandTile() throws RemoteException, InterruptedException {
        int ind = random.nextInt(pointsOfLandTilesCanBeChosed.size());
        Point point = pointsOfLandTilesCanBeChosed.get(ind);
        System.out.println("Ezt húzta az ai: " + point);
        delegate.chooseFaceDownLandTile(point);
        rule = 100;
        rotate = -1;
        tablePosition = new Point(-1,-1);
        followerPosition = -1;
        connectionNumber = 0;
        sizeNumber = 0;
    }
    
    public void decideBestLocation() throws RemoteException {
        for(int i=0; i<4; i++) {
            for(Iterator<Point> it = carcassonneGameModel.getEnabledPlacesOnTheTable().iterator(); it.hasNext(); ) {
                Point tablePosition = it.next();
                carcassonneGameModel.locateLandTileJustForTry(tablePosition);
                checkBestRuleMatches(tablePosition, i);
                carcassonneGameModel.removeLocationDatasAfterTrying(tablePosition);
            }
            carcassonneGameModel.rotateRightLandTile();
        }
        if(rule == 100) {
            System.out.println("AI Itt nincs rule");
            locateLandTileJustSomewhere();
        } else {
            System.out.println("AI talált rulet: " + rule);
            for(int i=0; i<rotate; i++) {
                delegate.rotateRightLandTile();
            }
            locateLandTile(tablePosition);
        }
    }
    
    public void checkBestRuleMatches(Point tablePosition, int rotate) {
        if(!carcassonneGameModel.getActualLandTileComponentIndex(CITY).isEmpty() || !carcassonneGameModel.getActualLandTileComponentIndex(CITYWITHPENNANT).isEmpty()) {
            List<Integer> indexList = new ArrayList<>(carcassonneGameModel.getActualLandTileComponentIndex(CITY));
            indexList.addAll(carcassonneGameModel.getActualLandTileComponentIndex(CITYWITHPENNANT));
            for(int index : indexList) {
                if(carcassonneGameModel.getReservationOfAComponentOfActualLandTile(index)[carcassonneGameModel.getTurn()] > 0 && otherReservationsAreLower(index)) {
                    if(carcassonneGameModel.isTheBuildedRoadOrCityPartDone(index)) {
                        rule = 1;
                        this.rotate = rotate;
                        this.tablePosition = tablePosition;
                        this.followerPosition = -1;
                    } else if(rule > 6 || (rule == 6 && this.connectionNumber < carcassonneGameModel.getNeighbourLandTileNumber(tablePosition))) {
                        rule = 6;
                        this.rotate = rotate;
                        this.tablePosition = tablePosition;
                        this.connectionNumber = carcassonneGameModel.getNeighbourLandTileNumber(tablePosition);
                        this.followerPosition = -1;
                    }
                } else if(carcassonneGameModel.getReservationOfAComponentOfActualLandTile(index)[carcassonneGameModel.getTurn()] == 0 && otherReservationsAreLower(index)) {
                    if(carcassonneGameModel.isTheBuildedRoadOrCityPartDone(index) && rule > 4) {
                        rule = 4;
                        this.rotate = rotate;
                        this.tablePosition = tablePosition;
                        setFollowerPosition(index);
                    } else {
                        if(placeOfConnectionIsProper(index, tablePosition) && (rule > 9 || (rule==9 && this.connectionNumber < carcassonneGameModel.getNeighbourLandTileNumber(tablePosition)))) {
                            rule = 9;
                            this.rotate = rotate;
                            this.tablePosition = tablePosition;
                            this.connectionNumber = carcassonneGameModel.getNeighbourLandTileNumber(tablePosition);
                            setFollowerPosition(index);
                        } else if((rule > 12 || (rule == 12 && this.connectionNumber < carcassonneGameModel.getNeighbourLandTileNumber(tablePosition))) && carcassonneGameModel.canTryJoinToAnotherCity(index, tablePosition)) {
                            rule = 12;
                            this.rotate = rotate;
                            this.tablePosition = tablePosition;
                            this.connectionNumber = carcassonneGameModel.getNeighbourLandTileNumber(tablePosition);
                            setFollowerPosition(index);
                        }
                    }
                }
            }
        } else if(!carcassonneGameModel.getActualLandTileComponentIndex(CLOISTER).isEmpty()) {
            if(rule >= 2 && sizeNumber < carcassonneGameModel.getCloisterSizeNumberOfACertainPlace(tablePosition)) {
                sizeNumber = carcassonneGameModel.getCloisterSizeNumberOfACertainPlace(tablePosition);
                rule = 2;
                this.rotate = rotate;
                this.tablePosition = tablePosition;
                setFollowerPosition(12);
            }
        }else if(!carcassonneGameModel.getActualLandTileComponentIndex(ROAD).isEmpty()) {
            List<Integer> indexList = new ArrayList<>(carcassonneGameModel.getActualLandTileComponentIndex(ROAD));
            for(int index : indexList) {
                if(carcassonneGameModel.getReservationOfAComponentOfActualLandTile(index)[carcassonneGameModel.getTurn()] > 0 && otherReservationsAreLower(index)) {
                    if(carcassonneGameModel.isTheBuildedRoadOrCityPartDone(index) && (rule > 3 || (rule == 3 && this.connectionNumber < carcassonneGameModel.getNeighbourLandTileNumber(tablePosition)))) {
                        rule = 3;
                        this.rotate = rotate;
                        this.tablePosition = tablePosition;
                        this.connectionNumber = carcassonneGameModel.getNeighbourLandTileNumber(tablePosition);
                        this.followerPosition = -1;
                    } else if(rule > 8) {
                        rule = 8;
                        this.rotate = rotate;
                        this.tablePosition = tablePosition;
                        this.followerPosition = -1;
                    }
                } else if(carcassonneGameModel.getReservationOfAComponentOfActualLandTile(index)[carcassonneGameModel.getTurn()] == 0 && otherReservationsAreLower(index)) {
                    if(carcassonneGameModel.isTheBuildedRoadOrCityPartDone(index) && (rule > 5 || (rule == 5 && this.connectionNumber < carcassonneGameModel.getNeighbourLandTileNumber(tablePosition)))) {
                        rule = 5;
                        this.rotate = rotate;
                        this.tablePosition = tablePosition;
                        this.connectionNumber = carcassonneGameModel.getNeighbourLandTileNumber(tablePosition);
                        setFollowerPosition(index);
                    } else {
                        if(placeOfConnectionIsProper(index, tablePosition) && (rule > 11 && (rule == 11 && this.connectionNumber < carcassonneGameModel.getNeighbourLandTileNumber(tablePosition)))) {
                            rule = 11;
                            this.rotate = rotate;
                            this.tablePosition = tablePosition;
                            this.connectionNumber = carcassonneGameModel.getNeighbourLandTileNumber(tablePosition);
                            if(carcassonneGameModel.getFreeFollowerNumOfPLayers()[carcassonneGameModel.getTurn()] > 1) {
                                setFollowerPosition(index);
                            }
                        } else if((rule > 13 || (rule == 13 && this.connectionNumber < carcassonneGameModel.getNeighbourLandTileNumber(tablePosition))) && carcassonneGameModel.canTryJoinToAnotherRoad(index, tablePosition)) {
                            rule = 13;
                            this.rotate = rotate;
                            this.tablePosition = tablePosition;
                            this.connectionNumber = carcassonneGameModel.getNeighbourLandTileNumber(tablePosition);
                            setFollowerPosition(index);
                        }
                    }
                }
            }
        } else if(carcassonneGameModel.listOfCloistersNearToThePoint(tablePosition).size() > sizeNumber && (rule > 7 | (rule == 7 && this.connectionNumber < carcassonneGameModel.getNeighbourLandTileNumber(tablePosition)))) {
            sizeNumber = carcassonneGameModel.listOfCloistersNearToThePoint(tablePosition).size();
            rule = 7;
            this.rotate = rotate;
            this.connectionNumber = carcassonneGameModel.getNeighbourLandTileNumber(tablePosition);
            this.tablePosition = tablePosition;
        }
    }
    
    public boolean placeOfConnectionIsProper(int index, Point tablePosition) {
        if(index > -1 && index < 3 && carcassonneGameModel.doesTablePlaceContainLandTile(new Point(tablePosition.x, tablePosition.y-1))) {
            return true;            
        } else if(index > 2 && index < 6 && carcassonneGameModel.doesTablePlaceContainLandTile(new Point(tablePosition.x+1, tablePosition.y))) {
            return true; 
        } else if(index > 5 && index < 9 && carcassonneGameModel.doesTablePlaceContainLandTile(new Point(tablePosition.x, tablePosition.y+1))) {
            return true; 
        } else if(index > 8 && index < 12 && carcassonneGameModel.doesTablePlaceContainLandTile(new Point(tablePosition.x-1, tablePosition.y))) {
            return true; 
        }
        return false;
    }
    
    public void setFollowerPosition(int index) {
        if(index == 0 || index == 1 || index == 2) {
            followerPosition = 1;
        } else if(index == 3 || index == 4 || index == 5) {
            followerPosition = 4;
        } else if(index == 6 || index == 7 || index == 8) {
            followerPosition = 7;
        } else if(index == 9 || index == 10 || index == 11) {
            followerPosition = 10;
        } else {
            followerPosition = index;
        }
    }
    
    public boolean otherReservationsAreLower(int index) {
        int[] reservations = carcassonneGameModel.getReservationOfAComponentOfActualLandTile(index);
        for(int i=0; i<reservations.length; i++) {
            if(i != carcassonneGameModel.getTurn() && reservations[i] >= reservations[carcassonneGameModel.getTurn()] && reservations[i] > 0) {
                return false;
            }
        }
        return true;
    }
    
    public void locateLandTile(Point point) throws RemoteException{
        if(carcassonneGameModel.getEnabledPlacesOnTheTable().size() > 0) {
            for (Iterator<Point> it = carcassonneGameModel.getEnabledPlacesOnTheTable().iterator(); it.hasNext(); ) {
                Point p = it.next();
                if(point.x == p.x && point.y == p.y) {
                    delegate.locateLandTileOnTheTable(p);
                    if(followerPosition > -1) {
                        delegate.locateFollower(followerPosition);
                    }
                    break;
                }
            }
            delegate.countPoints();
        }
    }
    
    public void locateLandTileJustSomewhere() throws RemoteException{
        if(carcassonneGameModel.getEnabledPlacesOnTheTable().size() > 0) {
            for (Iterator<Point> it = carcassonneGameModel.getEnabledPlacesOnTheTable().iterator(); it.hasNext(); ) {
                Point p = it.next();
                delegate.locateLandTileOnTheTable(p);
                break;
            }
            delegate.countPoints();
        }
    }
    
    public void removeFromPointsOfLandTilesCanBeChosed(Point p) {
        pointsOfLandTilesCanBeChosed.remove(p);
    }
    
}
