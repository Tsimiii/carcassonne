package model;

import java.awt.Point;
import java.util.Random;
import model.landtile.LandTile;
import model.landtile.LandTileLoader;

public class CarcassonneGameModel {
    
    private LandTileLoader landTileLoader;
    private LandTile[] landTiles;
    private LandTile chosenLandTile;

    public CarcassonneGameModel() {
        landTileLoader = new LandTileLoader();
        landTiles = landTileLoader.getLandTiles();
        chosenLandTile = null;
        //shuffleArray();
    }
    
    private void shuffleArray() {
        Random rnd = new Random();
        for (int i = landTiles.length - 2; i > 0; i--)
        {
          int ind = rnd.nextInt(i + 1);
          LandTile landTile = landTiles[ind];
          landTiles[ind] = landTiles[i];
          landTiles[i] = landTile;
        }
    }
    
    public boolean chooseFaceDownLandTile(Point p) {
        if(chosenLandTile == null) {
            chosenLandTile = landTiles[p.x*5+p.y];
            return true;
        }
        return false;
    }
}
