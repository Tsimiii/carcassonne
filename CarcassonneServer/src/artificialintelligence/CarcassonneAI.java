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
    
    private int delay;
    
    private int rule = 100;
    private int rotate = -1;
    private Point tablePosition = new Point(-1, -1);
    private int followerPosition = -1;
    private int connectionNumber = 0;
    private int sizeNumber = 0;

    public CarcassonneAI(int delay) {
        initPointsOfLandTilesCanBeChosed();
        this.delay = delay;
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
        String message;
        do {
            message = delegate.chooseFaceDownLandTile(point);
        }while(message.equals("cantBeLocated"));
        rule = 100;
        rotate = -1;
        tablePosition = new Point(-1,-1);
        followerPosition = -1;
        connectionNumber = 0;
        sizeNumber = 0;
    }
    
    public void decideBestLocation() throws RemoteException, InterruptedException {
        Thread.sleep(delay);
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
                    if(carcassonneGameModel.isTheBuildedRoadOrCityPartDone(index) && (rule > 1 || (rule == 1 && this.connectionNumber < carcassonneGameModel.getNeighbourLandTileNumber(tablePosition)))) {
                        rule = 1;
                        if(!(this.tablePosition.equals(tablePosition) && this.rotate == rotate)) {
                            this.followerPosition = -1;
                        }
                        this.rotate = rotate;
                        this.tablePosition = tablePosition;
                        this.connectionNumber = carcassonneGameModel.getNeighbourLandTileNumber(tablePosition);
                    } else if(rule > 6 || (rule == 6 && this.connectionNumber < carcassonneGameModel.getNeighbourLandTileNumber(tablePosition))) {
                        rule = 6;
                        if(!(this.tablePosition.equals(tablePosition) && this.rotate == rotate)) {
                            this.followerPosition = -1;
                        }
                        this.rotate = rotate;
                        this.tablePosition = tablePosition;
                        this.connectionNumber = carcassonneGameModel.getNeighbourLandTileNumber(tablePosition);
                    }
                } else if(carcassonneGameModel.getReservationOfAComponentOfActualLandTile(index)[carcassonneGameModel.getTurn()] == 0 && otherReservationsAreLower(index)) {
                    if(carcassonneGameModel.isTheBuildedRoadOrCityPartDone(index) && (rule > 3 || (rule < 3 && this.tablePosition.equals(tablePosition) && this.rotate == rotate && followerPosition == -1))) {
                        if(rule >= 3) {
                            rule = 3;
                            this.rotate = rotate;
                            this.tablePosition = tablePosition;
                        }
                        setFollowerPosition(index);
                    } else {
                        if(placeOfConnectionIsProper(index, tablePosition) && (rule > 10 || (rule==10 && this.connectionNumber < carcassonneGameModel.getNeighbourLandTileNumber(tablePosition)) || (rule < 10 && this.tablePosition.equals(tablePosition) && this.rotate == rotate && followerPosition == -1))) {
                            if(rule >= 10) {
                                rule = 10;
                                this.rotate = rotate;
                                this.tablePosition = tablePosition;
                            }
                            this.connectionNumber = carcassonneGameModel.getNeighbourLandTileNumber(tablePosition);
                            setFollowerPosition(index);
                        } else if((rule > 13 || (rule == 13 && this.connectionNumber < carcassonneGameModel.getNeighbourLandTileNumber(tablePosition)) || (rule < 13 && this.tablePosition.equals(tablePosition) && this.rotate == rotate && followerPosition == -1)) && carcassonneGameModel.canTryJoinToAnotherCity(index, tablePosition)) {
                            if(rule >= 13) {
                                rule = 13;
                                this.rotate = rotate;
                                this.tablePosition = tablePosition;
                            }
                            this.connectionNumber = carcassonneGameModel.getNeighbourLandTileNumber(tablePosition);
                            setFollowerPosition(index);
                        }
                    }
                }
            }
        } if(!carcassonneGameModel.getActualLandTileComponentIndex(CLOISTER).isEmpty()) {
            if(rule >= 2 && sizeNumber < carcassonneGameModel.getCloisterSizeNumberOfACertainPlace(tablePosition)) {
                sizeNumber = carcassonneGameModel.getCloisterSizeNumberOfACertainPlace(tablePosition);
                rule = 2;
                this.rotate = rotate;
                this.tablePosition = tablePosition;
                setFollowerPosition(12);
            }
        } if(!carcassonneGameModel.getActualLandTileComponentIndex(ROAD).isEmpty()) {
            List<Integer> indexList = new ArrayList<>(carcassonneGameModel.getActualLandTileComponentIndex(ROAD));
            for(int index : indexList) {
                if(carcassonneGameModel.getReservationOfAComponentOfActualLandTile(index)[carcassonneGameModel.getTurn()] > 0 && otherReservationsAreLower(index)) {
                    if(carcassonneGameModel.isTheBuildedRoadOrCityPartDone(index) && (rule > 4 || (rule == 4 && this.connectionNumber < carcassonneGameModel.getNeighbourLandTileNumber(tablePosition)))) {
                        rule = 4;
                        if(!(this.tablePosition.equals(tablePosition) && this.rotate == rotate)) {
                            this.followerPosition = -1;
                        }
                        this.rotate = rotate;
                        this.tablePosition = tablePosition;
                        this.connectionNumber = carcassonneGameModel.getNeighbourLandTileNumber(tablePosition);
                    } else if(rule > 8) {
                        rule = 8;
                        if(!(this.tablePosition.equals(tablePosition) && this.rotate == rotate)) {
                            this.followerPosition = -1;
                        }
                        this.rotate = rotate;
                        this.tablePosition = tablePosition; 
                    }
                } else if(carcassonneGameModel.getReservationOfAComponentOfActualLandTile(index)[carcassonneGameModel.getTurn()] == 0 && otherReservationsAreLower(index)) {
                    if(carcassonneGameModel.isTheBuildedRoadOrCityPartDone(index) && (rule > 5 || (rule == 5 && this.connectionNumber < carcassonneGameModel.getNeighbourLandTileNumber(tablePosition)) || (rule < 5 && this.tablePosition.equals(tablePosition) && this.rotate == rotate && followerPosition == -1))) {
                        if(rule >= 5) {
                            rule = 5;
                            this.rotate = rotate;
                            this.tablePosition = tablePosition;
                        }
                        this.connectionNumber = carcassonneGameModel.getNeighbourLandTileNumber(tablePosition);
                        setFollowerPosition(index);
                    } else {
                        if(placeOfConnectionIsProper(index, tablePosition) && (rule > 12 || (rule == 12 && this.connectionNumber < carcassonneGameModel.getNeighbourLandTileNumber(tablePosition)) || (rule < 12 && this.tablePosition.equals(tablePosition) && this.rotate == rotate && followerPosition == -1))) {
                            this.connectionNumber = carcassonneGameModel.getNeighbourLandTileNumber(tablePosition);
                            if(carcassonneGameModel.getFreeFollowerNumOfPLayers()[carcassonneGameModel.getTurn()] > 1) {
                                if(!carcassonneGameModel.getActualLandTileComponentIndex(CITY).isEmpty() && !carcassonneGameModel.isActualLandTilePartReserved(carcassonneGameModel.getActualLandTileComponentIndex(CITY).get(0))) {
                                    setFollowerPosition(carcassonneGameModel.getActualLandTileComponentIndex(CITY).get(0));
                                } else if(!carcassonneGameModel.getActualLandTileComponentIndex(CITYWITHPENNANT).isEmpty() && !carcassonneGameModel.isActualLandTilePartReserved(carcassonneGameModel.getActualLandTileComponentIndex(CITYWITHPENNANT).get(0))) {
                                    setFollowerPosition(carcassonneGameModel.getActualLandTileComponentIndex(CITYWITHPENNANT).get(0));
                                }
                                else if(carcassonneGameModel.getFreeFollowerNumOfPLayers()[carcassonneGameModel.getTurn()] > 1) {
                                    setFollowerPosition(index);
                                } else if(!(this.tablePosition.equals(tablePosition) && this.rotate == rotate)) {
                                    this.followerPosition = -1;
                                }
                            }
                            if(rule >= 12) {
                                rule = 12;
                                this.rotate = rotate;
                                this.tablePosition = tablePosition;
                            }
                        } else if((rule > 14 || (rule == 14 && this.connectionNumber < carcassonneGameModel.getNeighbourLandTileNumber(tablePosition)) || (rule < 14 && this.tablePosition.equals(tablePosition) && this.rotate == rotate && followerPosition == -1)) && carcassonneGameModel.canTryJoinToAnotherRoad(index, tablePosition)) {   
                            this.connectionNumber = carcassonneGameModel.getNeighbourLandTileNumber(tablePosition);
                            if(carcassonneGameModel.getFreeFollowerNumOfPLayers()[carcassonneGameModel.getTurn()] > 1) {
                                setFollowerPosition(index);
                            } else if(!(this.tablePosition.equals(tablePosition) && this.rotate == rotate)) {
                                    this.followerPosition = -1;
                            }
                            if(rule >= 14) {
                                rule = 14;
                                this.rotate = rotate;
                                this.tablePosition = tablePosition;
                            }
                        }
                    }
                }
            }
        } if(!carcassonneGameModel.getActualLandTileComponentIndex(FIELD).isEmpty()) {
            List<Integer> indexList = new ArrayList<>(carcassonneGameModel.getActualLandTileComponentIndex(FIELD));
            for(int index : indexList) {
                if((rule > 9 || (rule == 9 && this.sizeNumber < carcassonneGameModel.getNumberOfStartedCitiesInAField(index)) || (rule < 9 && this.tablePosition.equals(tablePosition) && this.rotate == rotate && followerPosition == -1)) && !carcassonneGameModel.isTheComponentReserved(index) && carcassonneGameModel.getNumberOfStartedCitiesInAField(index) > 2) {
                    if(rule >= 9) {
                        rule = 9;
                        this.rotate = rotate;
                        this.tablePosition = tablePosition;
                    }
                    this.sizeNumber = carcassonneGameModel.getNumberOfStartedCitiesInAField(index);
                    if(indexList.contains(12)) {
                        followerPosition = 12;
                    } else {
                        followerPosition = index;
                    }
                }
            }
        } if(carcassonneGameModel.listOfCloistersNearToThePoint(tablePosition).size() > sizeNumber && (rule > 7 | (rule == 7 && this.connectionNumber < carcassonneGameModel.getNeighbourLandTileNumber(tablePosition)))) {
            sizeNumber = carcassonneGameModel.listOfCloistersNearToThePoint(tablePosition).size();
            if(!(this.tablePosition.equals(tablePosition) && this.rotate == rotate)) {
                this.followerPosition = -1;
            }
            rule = 7;
            this.rotate = rotate;
            this.connectionNumber = carcassonneGameModel.getNeighbourLandTileNumber(tablePosition);
            this.tablePosition = tablePosition;
        } else if(rule > 15 || (rule == 15 && this.connectionNumber < carcassonneGameModel.getNeighbourLandTileNumber(tablePosition)) && carcassonneGameModel.roadAndCityAreNotReservedByOthers()) {
            if(!(this.tablePosition.equals(tablePosition) && this.rotate == rotate)) {
                this.followerPosition = -1;
            }
            rule = 15;
            this.rotate = rotate;
            this.connectionNumber = carcassonneGameModel.getNeighbourLandTileNumber(tablePosition);
            this.tablePosition = tablePosition;
        } else if(rule > 16 || (rule == 16 && this.connectionNumber < carcassonneGameModel.getNeighbourLandTileNumber(tablePosition))) {
            if(!(this.tablePosition.equals(tablePosition) && this.rotate == rotate)) {
                this.followerPosition = -1;
            }
            rule = 16;
            this.rotate = rotate;
            this.connectionNumber = carcassonneGameModel.getNeighbourLandTileNumber(tablePosition);
            this.tablePosition = tablePosition;
        }
    }
    
    private void locateOrNotFollowerDecision() {
        if(!carcassonneGameModel.getActualLandTileComponentIndex(CITY).isEmpty() || !carcassonneGameModel.getActualLandTileComponentIndex(CITYWITHPENNANT).isEmpty()) {
            List<Integer> indexList = new ArrayList<>(carcassonneGameModel.getActualLandTileComponentIndex(CITY));
            indexList.addAll(carcassonneGameModel.getActualLandTileComponentIndex(CITYWITHPENNANT));
            for(int index : indexList) {
                if(!carcassonneGameModel.isActualLandTilePartReserved(index)) {
                    setFollowerPosition(index);
                    break;
                }
            }
        } else if(!carcassonneGameModel.getActualLandTileComponentIndex(ROAD).isEmpty()) {
            List<Integer> indexList = new ArrayList<>(carcassonneGameModel.getActualLandTileComponentIndex(ROAD));
            for(int index : indexList) {
                if(!carcassonneGameModel.isActualLandTilePartReserved(index) && carcassonneGameModel.getFreeFollowerNumOfPLayers()[carcassonneGameModel.getTurn()] > 2) {
                    setFollowerPosition(index);
                    break;
                }
            }
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
            if(i != carcassonneGameModel.getTurn() && reservations[i] > reservations[carcassonneGameModel.getTurn()] && reservations[i] > 0) {
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
                    break;
                }
            }
        }
    }
    
    public void locateLandTileJustSomewhere() throws RemoteException{
        while(carcassonneGameModel.getEnabledPlacesOnTheTable().size() == 0) {
            delegate.rotateRightLandTile();
        }
        for (Iterator<Point> it = carcassonneGameModel.getEnabledPlacesOnTheTable().iterator(); it.hasNext(); ) {
            Point p = it.next();
            delegate.locateLandTileOnTheTable(p);
            break;
        } 
    }
    
    public void locateFollower() throws RemoteException, InterruptedException {
        Thread.sleep(delay);
        if(followerPosition == -1) {
            locateOrNotFollowerDecision();
        }
        if(followerPosition > -1) {
            delegate.locateFollower(followerPosition);
        }
        delegate.countPoints();
    }
    
    public void removeFromPointsOfLandTilesCanBeChosed(Point p) {
        pointsOfLandTilesCanBeChosed.remove(p);
    }
    
}