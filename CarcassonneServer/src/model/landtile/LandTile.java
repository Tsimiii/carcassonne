package model.landtile;

import java.awt.Point;

public class LandTile {
    
    private int id;
    private int[] components;
    private int[][] continuousParts;
    private Point positionOnTheTable;

    public LandTile(int id, int[] components, int[][] continuousParts) {
        this.id = id;
        this.components = components;
        this.continuousParts = continuousParts;
        positionOnTheTable = new Point(-1,-1);
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
    
}
