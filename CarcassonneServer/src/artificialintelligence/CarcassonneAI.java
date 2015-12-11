package artificialintelligence;

import carcassonneserver.CarcassonneServer;
import java.awt.Point;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import model.CarcassonneGameModel;

// A mesterséges intelligenciát megvalósító osztály
public class CarcassonneAI {
    
    // A különböző kártyaelemek azonosítói
    private final int FIELD = 0;
    private final int ROAD = 1;
    private final int CITY = 2;
    private final int CITYWITHPENNANT = 4;
    private final int CLOISTER = 3;
    
    private Random random = new Random();
    private List<Point> pointsOfLandTilesCanBeChosed = new ArrayList<>(); // Ezekből a kártyákból húzhat az MI
    private CarcassonneGameModel carcassonneGameModel;
    public CarcassonneServer delegate;
    
    private int delay;
    
    private int rule = 100; // Az eltárolt szabály sorszáma
    private int rotate = -1; // A jobbraforgatás mennyisége
    private Point tablePosition = new Point(-1, -1); // A táblán levő pozíció
    private int followerPosition = -1; // Az alattvaló pozíciója a kártyán
    private int connectionNumber = 0; // Hány kártyával csatlakozik
    private int sizeNumber = 0; // Mekkora az épített vár, út, kolostor mérete

    public CarcassonneAI(int delay) {
        initPointsOfLandTilesCanBeChosed();
        this.delay = delay;
    }
    
    // Kezdetben az összes lefordított kártyát beleteszi a kihúzható kártyák listájába
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
    
    // A maradék lefordított területkártyák közül véletlenszerű húzás
    public void chooseLandTile() throws RemoteException, InterruptedException {
        String message;
        // Addig húz, amíg a kártyát le tudja valahova tenni
        do {
            int ind = random.nextInt(pointsOfLandTilesCanBeChosed.size()); //kiválaszt egy random számot, a lista ezen indexű kártyáját húzza ki
            Point point = pointsOfLandTilesCanBeChosed.get(ind);
            System.out.println("itt vagyok: " + delegate); 
            message = delegate.chooseFaceDownLandTile(point);
            System.out.println("itt vagyok: " + message);
        }while(!message.equals("success") && !message.equals("multipleChoose"));
        System.out.println("RÁADÁSUL KI IS LÉPETT");
        rule = 100;
        rotate = -1;
        tablePosition = new Point(-1,-1);
        followerPosition = -1;
        connectionNumber = 0;
        sizeNumber = 0;
    }
    
    //Kiválasztja a szabályok alapján a kihúzott kártya legjobb elhelyezését
    public void decideBestLocation() throws RemoteException, InterruptedException {
        Thread.sleep(delay);
        // 4 különböző pozícióban megnézi a lehetséges helyeket (4 jobbra forgatás)
        for(int i=0; i<4; i++) {
            // Az összes lehetséges helyre megpróbálja elhelyezni a kártyát
            for(Iterator<Point> it = carcassonneGameModel.getEnabledPlacesOnTheTable().iterator(); it.hasNext(); ) {
                Point tablePosition = it.next();
                carcassonneGameModel.locateLandTileJustForTry(tablePosition);
                checkBestRuleMatches(tablePosition, i);
                carcassonneGameModel.removeLocationDatasAfterTrying(tablePosition);
            }
            carcassonneGameModel.rotateRightLandTile();
        }
        // Ebben az esetben nem talált megfelelő szabályt az MI
        if(rule == 100) {
            System.out.println("AI Itt nincs rule");
            locateLandTileJustSomewhere(); //Egy véletlenszerű szabályos helyre elhelyezi
        // Ebben az esetben az MI talált megfelelő szabályt
        } else {
            System.out.println("AI talált rulet: " + rule);
            for(int i=0; i<rotate; i++) {
                delegate.rotateRightLandTile(); // Az eltárolt forgatásnak megfelelően elforgatja a kártyát
            }
            locateLandTile(tablePosition); // Elhelyezi a kártyát az eltárolt helyre
        }
    }
    
    // Az MI szabályrendszere
    public void checkBestRuleMatches(Point tablePosition, int rotate) {
        //Ha a kártyán van várrész, belép
        if(!carcassonneGameModel.getActualLandTileComponentIndex(CITY).isEmpty() || !carcassonneGameModel.getActualLandTileComponentIndex(CITYWITHPENNANT).isEmpty()) {
            List<Integer> indexList = new ArrayList<>(carcassonneGameModel.getActualLandTileComponentIndex(CITY)); // A várhoz tartozó indexek listája
            indexList.addAll(carcassonneGameModel.getActualLandTileComponentIndex(CITYWITHPENNANT)); // A címeres várhoz tartozó indexek listája
            for(int index : indexList) {
                //Ha az adott várrészen rajta áll legalább annyi alattvalóval, mint a vele konkurens játékos (ha van)
                if(carcassonneGameModel.getReservationOfAComponentOfActualLandTile(index)[carcassonneGameModel.getTurn()] > 0 && otherReservationsAreLower(index)) {
                    // Ha az elhelyezéssel be tudja fejezni a játékot, vagy ha már ugyanez a szabály el van tárolva, akkor a szomszédos metők száma nagyobb, mint az eltároltnál
                    if(carcassonneGameModel.isTheBuildedRoadOrCityPartDone(index) && (rule > 1 || (rule == 1 && this.connectionNumber < carcassonneGameModel.getNeighbourLandTileNumber(tablePosition)))) {
                        rule = 1;
                        if(!(this.tablePosition.equals(tablePosition) && this.rotate == rotate)) {
                            this.followerPosition = -1;
                        }
                        this.rotate = rotate;
                        this.tablePosition = tablePosition;
                        this.connectionNumber = carcassonneGameModel.getNeighbourLandTileNumber(tablePosition);
                    // Ha csak folytatni tudja a vár építését, illetve ha már eltárolt ilyen szabályt, akkor a szomszédos mezők száma nagyobb, mint az eltároltnál
                    } else if(rule > 6 || (rule == 6 && this.connectionNumber < carcassonneGameModel.getNeighbourLandTileNumber(tablePosition))) {
                        rule = 6;
                        if(!(this.tablePosition.equals(tablePosition) && this.rotate == rotate)) {
                            this.followerPosition = -1;
                        }
                        this.rotate = rotate;
                        this.tablePosition = tablePosition;
                        this.connectionNumber = carcassonneGameModel.getNeighbourLandTileNumber(tablePosition);
                    }
                // Ha sem ő, sem más nem áll rajta a várrészen
                } else if(carcassonneGameModel.getReservationOfAComponentOfActualLandTile(index)[carcassonneGameModel.getTurn()] == 0 && otherReservationsAreLower(index)) {
                    // Ha be tudja fejezni az adott várrészt; és ha már egy eltárolt jobb elhelyezéssel is be tud ide lépni
                    if(carcassonneGameModel.isTheBuildedRoadOrCityPartDone(index) && (rule > 3 || (rule < 3 && this.tablePosition.equals(tablePosition) && this.rotate == rotate && followerPosition == -1))) {
                        if(rule >= 3) {
                            rule = 3;
                            this.rotate = rotate;
                            this.tablePosition = tablePosition;
                        }
                        setFollowerPosition(index); // rak alattvalót
                    // Ha nem tudja befejezni az adott várrészt
                    } else {
                        //Ha csatlakozik már egy meglévő várrészhez, mely nem foglalt más által; és ha már eltárolt egy ilyen elhelyezést, de a jelenlegi több szomszédos mezővel bír, akkor belép
                        if(placeOfConnectionIsProper(index, tablePosition) && carcassonneGameModel.roadAndCityAreNotReservedByOthers() && (rule > 10 || (rule==10 && this.connectionNumber < carcassonneGameModel.getNeighbourLandTileNumber(tablePosition)) || (rule < 10 && this.tablePosition.equals(tablePosition) && this.rotate == rotate && followerPosition == -1))) {
                            if(rule >= 10) {
                                rule = 10;
                                this.rotate = rotate;
                                this.tablePosition = tablePosition;
                            }
                            this.connectionNumber = carcassonneGameModel.getNeighbourLandTileNumber(tablePosition);
                            setFollowerPosition(index); //rárak alattvalót
                        // Ha nem csatlakozik egy meglévő várrészhez sem, akkor megpróbál hozzáépítkezni egy más által foglalt várhoz; ha már eltárolt egy ilyen elhelyezést, de a
                        // jelenlegi több szomszédos mezővel bír, vagy ha van már egy ennél jobb elhelyezés eltárolva, de azt nem sértve ide is be tud lépni
                        } else if((rule > 12 || (rule == 12 && this.connectionNumber < carcassonneGameModel.getNeighbourLandTileNumber(tablePosition)) || (rule < 12 && this.tablePosition.equals(tablePosition) && this.rotate == rotate && followerPosition == -1)) && carcassonneGameModel.roadAndCityAreNotReservedByOthers() && carcassonneGameModel.canTryJoinToAnotherCity(index, tablePosition)) {
                            if(rule >= 12) {
                                rule = 12;
                                this.rotate = rotate;
                                this.tablePosition = tablePosition;
                            }
                            this.connectionNumber = carcassonneGameModel.getNeighbourLandTileNumber(tablePosition);
                            setFollowerPosition(index); // alattvaló elhelyezése
                        }
                    }
                }
            }
        // Ha a kihúzott kártyán van kolostor
        } if(!carcassonneGameModel.getActualLandTileComponentIndex(CLOISTER).isEmpty()) {
            // Oda helyezi el a kártyát, mely a 8 szomszédos elem közül a legtöbbel rendelkezik
            if(rule >= 2 && sizeNumber < carcassonneGameModel.getCloisterSizeNumberOfACertainPlace(tablePosition)) {
                sizeNumber = carcassonneGameModel.getCloisterSizeNumberOfACertainPlace(tablePosition);
                rule = 2;
                this.rotate = rotate;
                this.tablePosition = tablePosition;
                setFollowerPosition(12); // Az alattvalót lerakja a kolostorra
            }
        // Ha a kihúzott kártyán van út
        } if(!carcassonneGameModel.getActualLandTileComponentIndex(ROAD).isEmpty()) {
            List<Integer> indexList = new ArrayList<>(carcassonneGameModel.getActualLandTileComponentIndex(ROAD)); // Az utas indexek listája
            for(int index : indexList) {
                //Ha az adott úton rajta áll legalább annyi alattvalóval, mint a vele konkurens játékos (ha van)
                if(carcassonneGameModel.getReservationOfAComponentOfActualLandTile(index)[carcassonneGameModel.getTurn()] > 0 && otherReservationsAreLower(index)) {
                    // Ha a játékos be tudja fejezni az adott utat, akkor belép, és ha már egy ilyen elhelyezés el van tárolva, de a jelenlegi több szomszédos mezővel rendelkezik, akkor is belép
                    if(carcassonneGameModel.isTheBuildedRoadOrCityPartDone(index) && (rule > 4 || (rule == 4 && this.connectionNumber < carcassonneGameModel.getNeighbourLandTileNumber(tablePosition)))) {
                        rule = 4;
                        if(!(this.tablePosition.equals(tablePosition) && this.rotate == rotate)) {
                            this.followerPosition = -1;
                        }
                        this.rotate = rotate;
                        this.tablePosition = tablePosition;
                        this.connectionNumber = carcassonneGameModel.getNeighbourLandTileNumber(tablePosition);
                    // Ha csak folytatni tudja az adott utat
                    } else if(rule > 8) {
                        rule = 8;
                        if(!(this.tablePosition.equals(tablePosition) && this.rotate == rotate)) {
                            this.followerPosition = -1;
                        }
                        this.rotate = rotate;
                        this.tablePosition = tablePosition; 
                    }
                //Ha sem ő, sem más nem áll rajta az úton
                } else if(carcassonneGameModel.getReservationOfAComponentOfActualLandTile(index)[carcassonneGameModel.getTurn()] == 0 && otherReservationsAreLower(index)) {
                    // Ha a játékos be tudja fejezni az adott utat, akkor belép, és ha már egy ilyen elhelyezés el van tárolva, de a jelenlegi több szomszédos mezővel rendelkezik, akkor is belép
                    if(carcassonneGameModel.isTheBuildedRoadOrCityPartDone(index) && (rule > 5 || (rule == 5 && this.connectionNumber < carcassonneGameModel.getNeighbourLandTileNumber(tablePosition)) || (rule < 5 && this.tablePosition.equals(tablePosition) && this.rotate == rotate && followerPosition == -1))) {
                        if(rule >= 5) {
                            rule = 5;
                            this.rotate = rotate;
                            this.tablePosition = tablePosition;
                        }
                        this.connectionNumber = carcassonneGameModel.getNeighbourLandTileNumber(tablePosition);
                        setFollowerPosition(index); // Ráak egy alattvalót
                    // Ha nem tudja befejezni az utat
                    } else {
                        // Ha csatlakozik egy meglévő útszakaszhoz, akkor belép
                        // Ha már eltárolt egy ilyen szabályt, de a jelenleginek a szobszédos mezőinek a száma nagyobb, akkor belép
                        // Ha ennél jobb elhelyezést eltárolt, de azt nem sértve ide is be tud lépni, akkor belép
                        if(placeOfConnectionIsProper(index, tablePosition) && carcassonneGameModel.roadAndCityAreNotReservedByOthers() && (rule > 11 || (rule == 11 && this.connectionNumber < carcassonneGameModel.getNeighbourLandTileNumber(tablePosition)) || (rule < 11 && this.tablePosition.equals(tablePosition) && this.rotate == rotate && followerPosition == -1))) {
                            this.connectionNumber = carcassonneGameModel.getNeighbourLandTileNumber(tablePosition);
                            //Ha van a játékosnak szabad alattvalója, akkor belép
                            if(carcassonneGameModel.getFreeFollowerNumOfPLayers()[carcassonneGameModel.getTurn()] > 1) {
                                //Ha a kártyán van nem foglalt vár, akkor belép
                                if(!carcassonneGameModel.getActualLandTileComponentIndex(CITY).isEmpty() && !carcassonneGameModel.isActualLandTilePartReserved(carcassonneGameModel.getActualLandTileComponentIndex(CITY).get(0))) {
                                    setFollowerPosition(carcassonneGameModel.getActualLandTileComponentIndex(CITY).get(0));
                                //Ha a kártyán van nem foglalt címeser vár, akkor belép
                                } else if(!carcassonneGameModel.getActualLandTileComponentIndex(CITYWITHPENNANT).isEmpty() && !carcassonneGameModel.isActualLandTilePartReserved(carcassonneGameModel.getActualLandTileComponentIndex(CITYWITHPENNANT).get(0))) {
                                    setFollowerPosition(carcassonneGameModel.getActualLandTileComponentIndex(CITYWITHPENNANT).get(0));
                                }
                                else if(carcassonneGameModel.getFreeFollowerNumOfPLayers()[carcassonneGameModel.getTurn()] > 1) {
                                    setFollowerPosition(index);
                                } else if(!(this.tablePosition.equals(tablePosition) && this.rotate == rotate)) {
                                    this.followerPosition = -1;
                                }
                            }
                            if(rule >= 11) {
                                rule = 11;
                                this.rotate = rotate;
                                this.tablePosition = tablePosition;
                            }
                        // Ha nem csatlakozik egy meglévő útrészhez sem, akkor megpróbál hozzáépítkezni egy más által foglalt úthoz
                        // Ha már eltárolt egy ilyen elhelyezést, de a jelenlegi több szomszédos mezővel bír, akkor belép
                        // Ha van már egy ennél jobb elhelyezés eltárolva, de azt nem sértve ide is be tud lépni
                        } else if((rule > 13 || (rule == 13 && this.connectionNumber < carcassonneGameModel.getNeighbourLandTileNumber(tablePosition)) || (rule < 13 && this.tablePosition.equals(tablePosition) && this.rotate == rotate && followerPosition == -1)) && carcassonneGameModel.roadAndCityAreNotReservedByOthers() && carcassonneGameModel.canTryJoinToAnotherRoad(index, tablePosition)) {   
                            this.connectionNumber = carcassonneGameModel.getNeighbourLandTileNumber(tablePosition);
                            if(carcassonneGameModel.getFreeFollowerNumOfPLayers()[carcassonneGameModel.getTurn()] > 1) {
                                setFollowerPosition(index); // Alattvaló elhelyezése, ha az MI-nek legalább 2 van
                            } else if(!(this.tablePosition.equals(tablePosition) && this.rotate == rotate)) {
                                    this.followerPosition = -1;
                            }
                            if(rule >= 13) {
                                rule = 13;
                                this.rotate = rotate;
                                this.tablePosition = tablePosition;
                            }
                        }
                    }
                }
            }
        // Ha van a kihúzott kártyán mező
        } if(!carcassonneGameModel.getActualLandTileComponentIndex(FIELD).isEmpty()) {
            List<Integer> indexList = new ArrayList<>(carcassonneGameModel.getActualLandTileComponentIndex(FIELD)); //A mezős indexek listája
            for(int index : indexList) {
                // Ha a mezőn legalább 3 megkezdett vár van, akkor belép
                // Ha már eltárolt egy ilyen elhelyezést, de a jelenlegi mezőn több vár szerepel
                // Ha van már egy ennél jobb elhelyezés eltárolva, de azt nem sértve ide is be tud lépni
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
        // Ha az elhelyezés közelében van kolostor, melyet nem foglal már és az elhelyezéssel építi azt, akkor belép
        // Ha már eltárolt egy ilyen elhelyezést, de a jelenlegi elhelyezéssel az adott kolostor több pontot ér, mint a másik, akkor belép
        } if(carcassonneGameModel.listOfCloistersNearToThePoint(tablePosition).size() > sizeNumber  && carcassonneGameModel.roadAndCityAreNotReservedByOthers() && (rule > 7 || (rule == 7 && this.connectionNumber < carcassonneGameModel.getNeighbourLandTileNumber(tablePosition)))) {
            sizeNumber = carcassonneGameModel.listOfCloistersNearToThePoint(tablePosition).size();
            if(!(this.tablePosition.equals(tablePosition) && this.rotate == rotate)) {
                this.followerPosition = -1;
            }
            rule = 7;
            this.rotate = rotate;
            this.connectionNumber = carcassonneGameModel.getNeighbourLandTileNumber(tablePosition);
            this.tablePosition = tablePosition;
        // Ha a vár vagy út nem foglalt más által, akkor belép
        // Ha már eltárolt egy ilyen elhelyezést, de a jelenlegi elhelyezéssel a szomszédos mezők száma több, akkor belép
        } else if((rule > 14 || (rule == 14 && this.connectionNumber < carcassonneGameModel.getNeighbourLandTileNumber(tablePosition))) && carcassonneGameModel.roadAndCityAreNotReservedByOthers()) {
            if(!(this.tablePosition.equals(tablePosition) && this.rotate == rotate)) {
                this.followerPosition = -1;
            }
            rule = 14;
            this.rotate = rotate;
            this.connectionNumber = carcassonneGameModel.getNeighbourLandTileNumber(tablePosition);
            this.tablePosition = tablePosition;
        // Egy legtöbb csatlakozásos helyre leteszi a kártyát
        } else if(rule > 15 || (rule == 15 && this.connectionNumber < carcassonneGameModel.getNeighbourLandTileNumber(tablePosition))) {
            if(!(this.tablePosition.equals(tablePosition) && this.rotate == rotate)) {
                this.followerPosition = -1;
            }
            rule = 15;
            this.rotate = rotate;
            this.connectionNumber = carcassonneGameModel.getNeighbourLandTileNumber(tablePosition);
            this.tablePosition = tablePosition;
        }
    }
    
    // Az alattvaló elhelyezéséről döntő metódus
    private void locateOrNotFollowerDecision() {
        // Ha van a kártyán vár, akkor belép
        if(!carcassonneGameModel.getActualLandTileComponentIndex(CITY).isEmpty() || !carcassonneGameModel.getActualLandTileComponentIndex(CITYWITHPENNANT).isEmpty()) {
            List<Integer> indexList = new ArrayList<>(carcassonneGameModel.getActualLandTileComponentIndex(CITY));
            indexList.addAll(carcassonneGameModel.getActualLandTileComponentIndex(CITYWITHPENNANT));
            for(int index : indexList) {
                // Ha talál olyan várrészt, ami nem foglalt, akkor elhelyezi az alattvalót
                if(!carcassonneGameModel.isActualLandTilePartReserved(index)) {
                    setFollowerPosition(index);
                    break;
                }
            }
        // Ha van a kártyán út, akkor belép
        } else if(!carcassonneGameModel.getActualLandTileComponentIndex(ROAD).isEmpty()) {
            List<Integer> indexList = new ArrayList<>(carcassonneGameModel.getActualLandTileComponentIndex(ROAD));
            for(int index : indexList) {
                // Ha a szabad alattvalók száma legalább 3, akkor belép
                if(!carcassonneGameModel.isActualLandTilePartReserved(index) && carcassonneGameModel.getFreeFollowerNumOfPLayers()[carcassonneGameModel.getTurn()] > 2) {
                    setFollowerPosition(index);
                    break;
                }
            }
        }
    }
  
    // Levizsgálja, hogy a tábla adott elhelyezésekor egy meghatározott oldala csatlakozik-e egy másik területkártyához
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
    
    // Beállítja az alattvaló pozícióját az eltárolt értéknek megfelelően, hogy a megjelenítő réteg esztétikusabban tudja kirajzolni
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
    
    // Levizsgálja, hogy egy kártyarészt az MI legalább annyi alattvalóval foglalja, mint egy másik játékos 
    public boolean otherReservationsAreLower(int index) {
        int[] reservations = carcassonneGameModel.getReservationOfAComponentOfActualLandTile(index);
        for(int i=0; i<reservations.length; i++) {
            if(i != carcassonneGameModel.getTurn() && reservations[i] > reservations[carcassonneGameModel.getTurn()] && reservations[i] > 0) {
                return false;
            }
        }
        return true;
    }
    
    // A kártya igazi elhelyezése az asztalon
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
    
    // A kártya a legelső szabad helyre való elhelyezése
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
    
    // Az alattvaló elhelyezése a kártyán
    public void locateFollower() throws RemoteException, InterruptedException {
        Thread.sleep(delay);
        if(followerPosition == -1) {
            locateOrNotFollowerDecision();
        }
        if(followerPosition > -1) {
            delegate.locateFollower(followerPosition);
        }
        delegate.countPoints();
        delegate.whosTurnIsIt();
    }
    
    // A kihúzható kártyapozíciók listájából törli a paraméterben meghatározott elemet
    public void removeFromPointsOfLandTilesCanBeChosed(Point p) {
        pointsOfLandTilesCanBeChosed.remove(p);
    }
    
}