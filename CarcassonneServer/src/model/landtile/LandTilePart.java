package model.landtile;

import java.util.ArrayList;
import java.util.List;

public class LandTilePart {
    private int[] items;
    private List<Integer> reserved;
    private boolean checkedDuringPointCount;

    public LandTilePart(int[] items) {
        this.items = new int[items.length];
        this.items = items;
        this.reserved = new ArrayList<>();
        this.checkedDuringPointCount = false;
    }
    
    public boolean contains(int value) {
        for(int i=0; i<items.length; i++) {
            if(items[i] == value) {
                return true;
            }
        }
        return false;
    }

    public int[] getItems() {
        return items;
    }

    public int getItem(int ind) {
        return items[ind];
    }

    public void setItem(int ind, int val) {
        this.items[ind] = val;
    }

    public List<Integer> getReserved() {
        return reserved;
    }

    public void setReserved(int follower) {
       reserved.add(follower);
    }

    public boolean isCheckedDuringPointCount() {
        return checkedDuringPointCount;
    }

    public void setCheckedDuringPointCount(boolean checkedDuringPointCount) {
        this.checkedDuringPointCount = checkedDuringPointCount;
    }
}
