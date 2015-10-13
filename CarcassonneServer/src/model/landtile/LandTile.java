package model.landtile;

import java.awt.Point;

public class LandTile {
    
    private int id;
    private int[] components;
    private int[][] continuousParts;
    private boolean reserved[];
    private Point positionOnTheTable;

    public LandTile(int id, int[] components, int[][] continuousParts) {
        this.id = id;
        this.components = components;
        this.continuousParts = continuousParts;
        this.reserved = new boolean[13];
        initReserved();
        positionOnTheTable = new Point(-1,-1);
    }
    
    private void initReserved() {
        for(boolean r : reserved) {
            r = false;
        }
    }

    public int[] getComponents() {
        return components;
    }

    public int[][] getContinuousParts() {
        return continuousParts;
    }

    public int getId() {
        return id;
    }

    public void setContinuousParts(int value, int ind1, int ind2) {
        continuousParts[ind1][ind2] = value;
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
        for(int i=0; i<continuousParts[ind].length; i++) {
            if(continuousParts[ind][i] == value) {
                return true;
            }
        }
        return false;
    }

    public boolean getReserved(int ind) {
        return reserved[ind];
    }

    public void setReserved(int ind) {
        this.reserved[ind] = true;
        for(int i=0; i<continuousParts.length; i++) {
            if(contains(i, ind)) {
                setReservedThisContinuousPart(i);
                break;
            }
        }
    }
    
    private void setReservedThisContinuousPart(int ind) {
        for(int val : continuousParts[ind]) {
            reserved[val] = true;
        }
    }
}
