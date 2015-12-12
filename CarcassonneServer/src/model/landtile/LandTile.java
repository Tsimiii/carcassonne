package model.landtile;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import model.follower.Follower;

// A területkártya osztálya
public class LandTile {
    
    private int id; // Egyedi azonosító
    private int[] components; // A kártyán levő elemek tömbje
    private LandTilePart[] continuousParts; // A kártyán az összefüggő részek tömbje
    private Point positionOnTheTable; // A kártya asztalon való helye

    // A területkártyák azonosítóból, meghatározott sorrendben eltárolt elemekből és összefüggő részekből állnak
    public LandTile(int id, int[] components, int[][] continuousParts) {
        this.id = id;
        this.components = components;
        this.continuousParts = new LandTilePart[continuousParts.length];
                
        initContinuousParts(continuousParts);
        positionOnTheTable = new Point(-1,-1);
    }
    
    // Az összefüggő részek inicializálása
    private void initContinuousParts(int[][] parts) {
        for(int i=0; i<parts.length; i++) {
            this.continuousParts[i] = new LandTilePart(parts[i]);
        }
    }

    // Visszaadja a kártya elemeinek tömbjét
    public int[] getComponents() {
        return components;
    }

    // Visszaadja a kártya összefüggő részeit
    public int[][] getContinuousParts() {
        int[][] temp = new int[continuousParts.length][];
        for(int i=0; i<continuousParts.length; i++) {
            temp[i] = continuousParts[i].getItems();
        }
        return temp;
    }
    
    // Azt az összefüggő részt adja vissza, amelyben a paraméterben szereplő index benne van
    public int[] getContinuousPart(int num) {
        for(int i=0; i<continuousParts.length; i++) {
            if(continuousParts[i].contains(num)) {
                return continuousParts[i].getItems();
            }
        }
        return null;
    }

    // Visszaadja a kártya egyedi azonosítóját
    public int getId() {
        return id;
    }

    // Az összefüggő része egy elemének megváltoztatása a paraméterben szereplő value-ra
    public void setContinuousParts(int value, int ind1, int ind2) {
        continuousParts[ind1].setItem(ind2, value);
    }

    // Visszaadja a táblán levő pozíciót
    public Point getPositionOnTheTable() {
        return positionOnTheTable;
    }

    // Beállítja a táblán levő pozíciót
    public void setPositionOnTheTable(int ind1, int ind2) {
        this.positionOnTheTable = new Point(ind1, ind2);
    }

    // Visszaadja a kártyaelemek tömbjét
    public void setComponents(int[] components) {
        this.components = components;
    }
    
    // Levizsgálja, hogy az adott összefüggő rész tartalmazza-e a value értéket
    public boolean contains(int ind, int value) {
        return continuousParts[ind].contains(value);
    }
    
    // Levizsgálja, hogy az ind indexű összefüggő részen ellenőrzi, hogy rajta áll-e az f alattvaló
    public boolean containsReservation(int ind, Follower f) {
        for(Follower fol : getReserved(ind)) {
            if(fol.equals(f)) {
                return true;
            }
        }
        return false;
    }

    // Visszaadja, hogy foglalt-e az az összefüggő rész, melyben benne van a val index
    public List<Follower> getReserved(int val) {
        for(LandTilePart ltp : continuousParts) {
            if(ltp.contains(val)) {
                return ltp.getReserved();
            }
        }
        return new ArrayList<Follower>();
    }
    
    // Visszaadja az összes összefüggő részről, hogy mely alattvalók által foglaltak
    public List<List<Follower>> getReserved() {
        List<List<Follower>> asd = new ArrayList<>();
        for(LandTilePart ltp : continuousParts) {
                asd.add(ltp.getReserved());
        }
        return asd;
    }

    // A paraméterként adott alattvaló által foglalttá teszi azt az összefüggő részt, melyben a val érték benne van
    public void setReserved(int val, Follower follower) {
        for(LandTilePart ltp : continuousParts) {
            if(ltp.contains(val)) {
                ltp.setReserved(follower);
            }
        }
    }
    
    // A paraméterként adott több alattvaló által foglalttá teszi azt az összefüggő részt, melyben a val érték benne van
    public void setReserved(int val, List<Follower> followers) {
        for(LandTilePart ltp : continuousParts) {
            if(ltp.contains(val)) {
                for(Follower f : followers) {
                    ltp.setReserved(f);
                }
            }
        }
    }
    
    // Törli annak az összefüggő résznek a foglaltságát, amely tartalmazza a val indexet
    public void clearReserved(int val) {
        for(LandTilePart ltp : continuousParts) {
            if(ltp.contains(val)) {
                ltp.clearReserved();
            }
        }
    }
    
    // Index alapján visszaadja az elem típusát (vár, út, kolostor vagy mező)
    public int getType(int ind) {
        return components[ind];
    }
    
    // Levizsgálttá teszi azt az összefüggő részt, mely tartalmazza a val indexet
    public void setDone(int val) {
        for(LandTilePart ltp : continuousParts) {
            if(ltp.contains(val)) {
                ltp.setCheckedDuringPointCount(true);
                break;
            }
        }
    }
    
    // Lekéri egy paraméterként adott indexű összefüggő részről, hogy levizsgált-e
    public boolean getDone(int ind) {
        return continuousParts[ind].isCheckedDuringPointCount();
    }
}
