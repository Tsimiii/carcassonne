package model.landtile;

public class LandTile {
    
    private int id;
    private int[] components;
    private int[][] continuousParts;

    public LandTile(int id, int[] components, int[][] continuousParts) {
        this.id = id;
        this.components = components;
        this.continuousParts = continuousParts;
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
    
}
