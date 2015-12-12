package model.landtile;

import java.util.ArrayList;
import java.util.List;
import model.follower.Follower;

// Összefüggő részt implementáló osztály
public class LandTilePart {
    private int[] items; // Azok a kártyaindexek, amik beletartoznak
    private List<Follower> reserved; //Kik által foglalt
    private boolean checkedDuringPointCount; //Levigyált-e

    public LandTilePart(int[] items) {
        this.items = new int[items.length];
        this.items = items;
        this.reserved = new ArrayList<>();
        this.checkedDuringPointCount = false;
    }
    
    // Visszaadja, hogy tartalmazza-e az adott értéket
    public boolean contains(int value) {
        for(int i=0; i<items.length; i++) {
            if(items[i] == value) {
                return true;
            }
        }
        return false;
    }

    // Visszaadja a hozzátartozó indexeket
    public int[] getItems() {
        return items;
    }

    // Visszaadja az adott indexű értéket
    public int getItem(int ind) {
        return items[ind];
    }

    // Beállítja az adott indexű értéket a val változóra
    public void setItem(int ind, int val) {
        this.items[ind] = val;
    }

    // Visszaadja, hogy kik által foglalt
    public List<Follower> getReserved() {
        return reserved;
    }

    // A paraméterben levő alattvaló által foglalttá teszi az összefüggő részt
    public void setReserved(Follower follower) {
       reserved.add(follower);
    }
    
    // Törli a foglaltságot
    public void clearReserved() {
        reserved.clear();
    }

    // Visszaadja, hogy le van-e vizsgálva az összefüggő rész
   public boolean isCheckedDuringPointCount() {
        return checkedDuringPointCount;
    }

   // Levizsgálttá teszi az összefüggő részt
    public void setCheckedDuringPointCount(boolean checkedDuringPointCount) {
        this.checkedDuringPointCount = checkedDuringPointCount;
    }
}
