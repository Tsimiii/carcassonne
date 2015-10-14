package model.landtile;

import java.awt.Point;

public class LandTile {
    
    private int id;
    private int[] components;
    private LandTilePart[] continuousParts;
    private Point positionOnTheTable;

    public LandTile(int id, int[] components, int[][] continuousParts) {
        this.id = id;
        this.components = components;
        this.continuousParts = new LandTilePart[continuousParts.length];
                
        initContinuousParts(continuousParts);
        positionOnTheTable = new Point(-1,-1);
    }
    
    private void initContinuousParts(int[][] parts) {
        for(int i=0; i<parts.length; i++) {
            this.continuousParts[i] = new LandTilePart(parts[i]);
        }
    }

    public int[] getComponents() {
        return components;
    }

    public int[][] getContinuousParts() {
        int[][] temp = new int[continuousParts.length][];
        for(int i=0; i<continuousParts.length; i++) {
            temp[i] = continuousParts[i].getItems();
        }
        return temp;
    }

    public int getId() {
        return id;
    }

    public void setContinuousParts(int value, int ind1, int ind2) {
        continuousParts[ind1].setItem(ind2, value);
    }

    public Point getPositionOnTheTable() {
        return positionOnTheTable;
    }

    public void setPositionOnTheTable(int ind1, int ind2) {
        this.positionOnTheTable = new Point(ind1, ind2);
    }

    public void setComponents(int[] components) {
        this.components = components;
    }
    
    public boolean contains(int ind, int value) {
        return continuousParts[ind].contains(value);
    }

    public boolean getReserved(int val) {
        for(LandTilePart ltp : continuousParts) {
            if(ltp.contains(val)) {
                return ltp.isReserved();
            }
        }
        return false;
    }

    public void setReserved(int val) {
        for(LandTilePart ltp : continuousParts) {
            if(ltp.contains(val)) {
                ltp.setReserved(true);
            }
        }
    }
    
    public int getType(int ind) {
        return components[ind];
    }
    
    public void setDone(int ind, boolean done) {
        continuousParts[ind].setCheckedDuringPointCount(done);
    }
    
    public boolean getDone(int ind) {
        return continuousParts[ind].isCheckedDuringPointCount();
    }
}
