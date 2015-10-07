package model.landtile;

public class LandTileLoader {
    
    private final int FIELD = 0;
    private final int ROAD = 1;
    private final int CITY = 2;
    private final int CITYWITHPENNANT = 3;
    private final int CLOISTER = 4;
    private final int NOTHING = 5;
    
    private LandTile starterLandTile;
    private LandTile[] landTiles;

    public LandTileLoader() {
        landTiles = new LandTile[71];
        starterLandTile = new LandTile(-1, new int[] {FIELD, ROAD, FIELD, FIELD, FIELD, FIELD, FIELD, ROAD, FIELD, CITY, CITY, CITY, ROAD}, new int[][] {new int[]{0,8}, new int[]{1,7,12}, new int[]{2,3,4,5,6}, new int[]{9,10,11}});
        initLandTiles();
    }
    
    private void initLandTiles() {
        int ind = 0;
        for(int i=0; i<8; i++) {
            landTiles[ind] = new LandTile(ind, new int[] {FIELD, FIELD, FIELD, FIELD, ROAD, FIELD, FIELD, FIELD, FIELD, FIELD, ROAD, FIELD, ROAD}, new int[][] {new int[]{0,1,2,3,11}, new int[]{4,10,12}, new int[]{5,6,7,8,9}});
            ind++;
        }
        for(int i=0; i<3; i++) {
            landTiles[ind] = new LandTile(ind, new int[] {FIELD, ROAD, FIELD, FIELD, ROAD, FIELD, FIELD, ROAD, FIELD, CITY, CITY, CITY, NOTHING}, new int[][] {new int[]{0,8}, new int[]{1}, new int[]{2,3}, new int[]{4}, new int[]{5,6}, new int[]{7}, new int[]{9,10,11}});
            ind++;
        }
        for(int i=0; i<5; i++) {
            landTiles[ind] = new LandTile(ind, new int[] {FIELD, FIELD, FIELD, FIELD, FIELD, FIELD, FIELD, FIELD, FIELD, CITY, CITY, CITY, FIELD}, new int[][] {new int[]{0,1,2,3,4,5,6,7,8,12}, new int[]{9,10,11}});
            ind++;
        }
        for(int i=0; i<9; i++) {
            landTiles[ind] = new LandTile(ind, new int[] {FIELD, ROAD, FIELD, FIELD, ROAD, FIELD, FIELD, FIELD, FIELD, FIELD, FIELD, FIELD, ROAD}, new int[][] {new int[]{0,5,6,7,8,9,10,11}, new int[]{1,4,12},new int[]{2,3}});
            ind++;
        }
        for(int i=0; i<3; i++) {
            landTiles[ind] = new LandTile(ind, new int[] {CITY, CITY, CITY, FIELD, ROAD, FIELD, FIELD, ROAD, FIELD, CITY, CITY, CITY, FIELD}, new int[][] {new int[]{0,1,2,9,10,11}, new int[]{3,8,12}, new int[]{4,7}, new int[]{5,6}});
            ind++;
        }
        for(int i=0; i<4; i++) {
            landTiles[ind] = new LandTile(ind, new int[] {FIELD, ROAD, FIELD, FIELD, ROAD, FIELD, FIELD, ROAD, FIELD, FIELD, FIELD, FIELD, NOTHING}, new int[][] {new int[]{0,8,9,10,11}, new int[]{1}, new int[]{2,3}, new int[]{4}, new int[]{5,6}, new int[]{7}});
            ind++;
        }
        for(int i=0; i<4; i++) {
            landTiles[ind] = new LandTile(ind, new int[] {FIELD, FIELD, FIELD, FIELD, FIELD, FIELD, FIELD, FIELD, FIELD, FIELD, FIELD, FIELD, CLOISTER}, new int[][] {new int[]{0,1,2,3,4,5,6,7,8,9,10,11}, new int[]{12}});
            ind++;
        }
        for(int i=0; i<2; i++) {
            landTiles[ind] = new LandTile(ind, new int[] {FIELD, FIELD, FIELD, FIELD, ROAD, FIELD, FIELD, FIELD, FIELD, FIELD, FIELD, FIELD, CLOISTER}, new int[][] {new int[]{0,1,2,3,5,6,7,8,9,10,11}, new int[]{4}, new int[]{12}});
            ind++;
        }
        for(int i=0; i<2; i++) {
            landTiles[ind] = new LandTile(ind, new int[] {CITYWITHPENNANT, CITYWITHPENNANT, CITYWITHPENNANT, FIELD, ROAD, FIELD, CITYWITHPENNANT, CITYWITHPENNANT, CITYWITHPENNANT, CITYWITHPENNANT, CITYWITHPENNANT, CITYWITHPENNANT, CITYWITHPENNANT}, new int[][] {new int[]{0,1,2,6,7,8,9,10,11,12}, new int[]{3,5}, new int[]{4}});
            ind++;
        }
        for(int i=0; i<3; i++) {
            landTiles[ind] = new LandTile(ind, new int[] {CITY, CITY, CITY, FIELD, FIELD, FIELD, CITY, CITY, CITY, CITY, CITY, CITY, CITY}, new int[][] {new int[]{0,1,2,6,7,8,9,10,11,12}, new int[]{3,4,5}});
            ind++;
        }
        for(int i=0; i<3; i++) {
            landTiles[ind] = new LandTile(ind, new int[] {FIELD, FIELD, FIELD, CITY, CITY, CITY, FIELD, FIELD, FIELD, CITY, CITY, CITY, FIELD}, new int[][] {new int[]{0,1,2,6,7,8,12}, new int[]{3,4,5}, new int[]{9,10,11}});
            ind++;
        }
        for(int i=0; i<3; i++) {
            landTiles[ind] = new LandTile(ind, new int[] {FIELD, ROAD, FIELD, FIELD, ROAD, FIELD, FIELD, FIELD, FIELD, CITY, CITY, CITY, FIELD}, new int[][] {new int[]{0,5,6,7,8,12}, new int[]{1,4}, new int[]{2,3}, new int[]{9,10,11}});
            ind++;
        }
        for(int i=0; i<3; i++) {
            landTiles[ind] = new LandTile(ind, new int[] {FIELD, FIELD, FIELD, FIELD, ROAD, FIELD, FIELD, ROAD, FIELD, CITY, CITY, CITY, FIELD}, new int[][] {new int[]{0,1,2,3,8,12}, new int[]{4,7}, new int[]{5,6}, new int[]{9,10,11}});
            ind++;
        }
        for(int i=0; i<2; i++) {
            landTiles[ind] = new LandTile(ind, new int[] {CITYWITHPENNANT, CITYWITHPENNANT, CITYWITHPENNANT, FIELD, ROAD, FIELD, FIELD, ROAD, FIELD, CITYWITHPENNANT, CITYWITHPENNANT, CITYWITHPENNANT, FIELD}, new int[][] {new int[]{0,1,2,9,10,11}, new int[]{3,8,12}, new int[]{4,7}, new int[]{5,6}});
            ind++;
        }
        for(int i=0; i<3; i++) {
            landTiles[ind] = new LandTile(ind, new int[] {CITY, CITY, CITY, FIELD, FIELD, FIELD, FIELD, FIELD, FIELD, CITY, CITY, CITY, FIELD}, new int[][] {new int[]{0,1,2,9,10,11}, new int[]{3,4,5,6,7,8,12}});
            ind++;
        }
        for(int i=0; i<2; i++) {
            landTiles[ind] = new LandTile(ind, new int[] {CITY, CITY, CITY, FIELD, FIELD, FIELD, FIELD, FIELD, FIELD, CITY, CITY, CITY, FIELD}, new int[][] {new int[]{0,1,2}, new int[]{9,10,11}, new int[]{3,4,5,6,7,8,12}});
            ind++;
        }
        for(int i=0; i<2; i++) {
            landTiles[ind] = new LandTile(ind, new int[] {CITYWITHPENNANT, CITYWITHPENNANT, CITYWITHPENNANT, FIELD, FIELD, FIELD, CITYWITHPENNANT, CITYWITHPENNANT, CITYWITHPENNANT, FIELD, FIELD, FIELD, CITYWITHPENNANT}, new int[][] {new int[]{0,1,2,6,7,8,12}, new int[]{3,4,5}, new int[]{9,10,11}});
            ind++;
        }
        for(int i=0; i<2; i++) {
            landTiles[ind] = new LandTile(ind, new int[] {CITYWITHPENNANT, CITYWITHPENNANT, CITYWITHPENNANT, FIELD, FIELD, FIELD, FIELD, FIELD, FIELD, CITYWITHPENNANT, CITYWITHPENNANT, CITYWITHPENNANT, FIELD}, new int[][] {new int[]{0,1,2,9,10,11}, new int[]{3,4,5,6,7,8,12}});
            ind++;
        }
        for(int i=0; i<3; i++) {
            landTiles[ind] = new LandTile(ind, new int[] {FIELD, ROAD, FIELD, FIELD, FIELD, FIELD, FIELD, ROAD, FIELD, CITY, CITY, CITY, ROAD}, new int[][] {new int[]{0,8}, new int[]{1,7,12}, new int[]{2,3,4,5,6}, new int[]{9,10,11}});
            ind++;
        }
        landTiles[ind] = new LandTile(ind, new int[] {CITY, CITY, CITY, FIELD, FIELD, FIELD, CITY, CITY, CITY, FIELD, FIELD, FIELD, CITY}, new int[][] {new int[]{0,1,2,6,7,8,12}, new int[]{3,4,5}, new int[]{9,10,11}});
        ind++;
        landTiles[ind] = new LandTile(ind, new int[] {FIELD, ROAD, FIELD, FIELD, ROAD, FIELD, FIELD, ROAD, FIELD, FIELD, ROAD, FIELD, NOTHING}, new int[][] {new int[]{0,11}, new int[]{1}, new int[]{2,3}, new int[]{4}, new int[]{5,6}, new int[]{7}, new int[]{8,9}, new int[]{10}});
        ind++;
        landTiles[ind] = new LandTile(ind, new int[] {CITY, CITY, CITY, FIELD, ROAD, FIELD, CITY, CITY, CITY, CITY, CITY, CITY, CITY}, new int[][] {new int[]{0,1,2,6,7,8,9,10,11,12}, new int[]{1}, new int[]{3}, new int[]{4}, new int[]{5}});
        ind++;
        landTiles[ind] = new LandTile(ind, new int[] {CITYWITHPENNANT, CITYWITHPENNANT, CITYWITHPENNANT, CITYWITHPENNANT, CITYWITHPENNANT, CITYWITHPENNANT, CITYWITHPENNANT, CITYWITHPENNANT, CITYWITHPENNANT, CITYWITHPENNANT, CITYWITHPENNANT, CITYWITHPENNANT, CITYWITHPENNANT}, new int[][] {new int[]{0,1,2,3,4,5,6,7,8,9,10,11,12}});
        ind++;
        landTiles[ind] = new LandTile(ind, new int[] {CITYWITHPENNANT, CITYWITHPENNANT, CITYWITHPENNANT, FIELD, FIELD, FIELD, CITYWITHPENNANT, CITYWITHPENNANT, CITYWITHPENNANT, CITYWITHPENNANT, CITYWITHPENNANT, CITYWITHPENNANT, CITYWITHPENNANT}, new int[][] {new int[]{0,1,2,6,7,8,9,10,11,12}, new int[]{3,4,5}});
    }

    public LandTile getStarterLandTile() {
        return starterLandTile;
    }

    public LandTile[] getLandTiles() {
        return landTiles;
    }
}


