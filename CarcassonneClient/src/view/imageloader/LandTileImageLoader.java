package view.imageloader;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javafx.scene.image.Image;

public class LandTileImageLoader {
    
    private static LandTileImageLoader instance = null;
    private static int[] shuffledIdArray;
    private static Image starterLandTile;
    private static Image[] landTileImages;

    public static LandTileImageLoader getInstance() {
        if(instance == null) {
            instance = new LandTileImageLoader();
            landTileImages = new Image[71];
        }    
        return instance;
    }
    
    public void init(int[] shuffledIdArray) throws IOException {
        this.shuffledIdArray = shuffledIdArray;
        loadImages();
    }
    
    private void loadImages() throws IOException {
        starterLandTile = new Image("/resources/images/landtiles/0.png");
         
        final String path = "resources/images/landtiles";
        final File jarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
         
        if(jarFile.isFile()) {  // Run with JAR file
            final JarFile jar = new JarFile(jarFile);
            for(int i=0; i<71; i++) {
                final Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
                while(entries.hasMoreElements()) {
                    final String name = entries.nextElement().getName();
                    if (name.equals(path + "/" + shuffledIdArray[i] + ".png")) { //filter according to the path
                        landTileImages[i] = new Image("/" + name);
                        System.out.println(name);
                    }    
                }
            }    
            jar.close();
        }
    }
    
    public Image[] getLandTileImages() {
        return landTileImages;
    }
    
    public Image getStarterLandTile() {
        return starterLandTile;
    }
}


