package view.imageloader;

import java.io.File;
import java.io.IOException;
import javafx.scene.image.Image;

public class LandTileImageLoader {
    
    private Image starterLandTile;
    private Image[] landTileImages;

    public LandTileImageLoader() throws IOException {
        landTileImages = new Image[71];
        loadImages();
    }
    
    private void loadImages() throws IOException {
        starterLandTile = new Image("file:src/resources/images/landtiles/0.png");
        
        File directory = new File("src/resources/images/landtiles");
        
        if(directory.isDirectory()) {
            File[] files = directory.listFiles();
            for(int i=1; i<72; i++) {
                for (File file : files) {
                    String name = Integer.toString(i);
                    if (file != null && file.getName().startsWith(name)) {
                        landTileImages[i-1] = new Image("file:" + file.getPath());
                        break;
                    }
                }
            }
        } 
    }
    
    public Image[] getLandTileImages() {
        return landTileImages;
    }
    
    public Image getStarterLandTile() {
        return starterLandTile;
    }
}


