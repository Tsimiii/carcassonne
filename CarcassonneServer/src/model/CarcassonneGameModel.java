package model;

import java.awt.Point;
import java.util.Random;
import model.landtile.LandTile;
import model.landtile.LandTileLoader;

public class CarcassonneGameModel {

    private LandTileLoader landTileLoader;
    private LandTile[] landTiles;
    private LandTile chosenLandTile;
    private int[] shuffledIdArray;

    public CarcassonneGameModel() {
        landTileLoader = new LandTileLoader();
        landTiles = landTileLoader.getLandTiles();
        shuffledIdArray = new int[71];
        chosenLandTile = null;
        shuffleLandTileArray();
    }

    private void shuffleLandTileArray() {
        Random rnd = new Random();
        for (int i = landTiles.length - 1; i > 0; i--) {
            int ind = rnd.nextInt(i + 1);
            LandTile landTile = landTiles[ind];
            landTiles[ind] = landTiles[i];
            landTiles[i] = landTile;
        }
        initShuffledIdArray();
    }

    public boolean chooseFaceDownLandTile(Point p) {
        if (chosenLandTile == null) {
            chosenLandTile = landTiles[p.x * 5 + p.y];
            return true;
        }
        return false;
    }

    public boolean setNewContinuousPartsAfterRotateRight() {
        if(chosenLandTile != null) {
        int contPartNum;
            for (int i = 0; i < chosenLandTile.getContinuousParts().length; i++) {
                for (int j = 0; j < chosenLandTile.getContinuousParts()[i].length; j++) {
                    contPartNum = chosenLandTile.getContinuousParts()[i][j];
                    if (contPartNum == 0 || contPartNum == 1 || contPartNum == 2) {
                        chosenLandTile.setContinuousParts(contPartNum + 9, i, j);
                    } else if (contPartNum != 12) {
                        chosenLandTile.setContinuousParts(contPartNum - 3, i, j);
                    }
                }
            }
            return true;
        }
        return false;
    }

    public boolean setNewContinuousPartsAfterRotateLeft() {
        if(chosenLandTile != null) {
        int contPartNum;
            for (int i = 0; i < chosenLandTile.getContinuousParts().length; i++) {
                for (int j = 0; j < chosenLandTile.getContinuousParts()[i].length; j++) {
                    contPartNum = chosenLandTile.getContinuousParts()[i][j];
                    if (contPartNum == 9 || contPartNum == 10 || contPartNum == 11) {
                        chosenLandTile.setContinuousParts(contPartNum - 9, i, j);
                    } else if (contPartNum != 12) {
                        chosenLandTile.setContinuousParts(contPartNum + 3, i, j);
                    }
                }
            }
            return true;
        }
        return false;
    }

    private void initShuffledIdArray() {
        for (int i = 0; i < landTiles.length; i++) {
            shuffledIdArray[i] = landTiles[i].getId();
        }
    }

    public int[] getShuffledIdArray() {
        return shuffledIdArray;
    }
}
