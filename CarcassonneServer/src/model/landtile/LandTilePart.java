package model.landtile;

public class LandTilePart {
    private int[] items;
    private boolean reserved;
    private boolean checkedDuringPointCount;

    public LandTilePart(int[] items) {
        this.items = new int[items.length];
        this.items = items;
        this.reserved = false;
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

    public boolean isReserved() {
        return reserved;
    }

    public void setReserved(boolean reserved) {
        this.reserved = reserved;
    }

    public boolean isCheckedDuringPointCount() {
        return checkedDuringPointCount;
    }

    public void setCheckedDuringPointCount(boolean checkedDuringPointCount) {
        this.checkedDuringPointCount = checkedDuringPointCount;
    }
}
