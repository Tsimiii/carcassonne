package model.tablecell;

import model.landtile.LandTile;

public class TableCell {
    private LandTile landTile;

    public TableCell() {
        landTile = null;
    }

    public LandTile getLandTile() {
        return landTile;
    }

    public void setLandTile(LandTile landTile) {
        this.landTile = landTile;
    }
}
