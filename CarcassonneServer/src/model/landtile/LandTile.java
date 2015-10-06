package model.landtile;

public class LandTile {
    
    private int[] components;
    private int[][] continuousParts;

    public LandTile(int[] components, int[][] continuousParts) {
        this.components = components;
        this.continuousParts = continuousParts;
    }

    public int[] getComponents() {
        return components;
    }

    public int[][] getContinuousParts() {
        return continuousParts;
    }
    
}
