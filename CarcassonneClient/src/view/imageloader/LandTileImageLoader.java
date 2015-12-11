package view.imageloader;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javafx.scene.image.Image;

// A területkártyák képeinek betöltéséért felelős osztály
public class LandTileImageLoader {
    
    private static LandTileImageLoader instance = null;
    private static int[] shuffledIdArray; // A kártyaID-k megkevert tömbje
    private static Image starterLandTile; // A kezdőkártya képe
    private static Image[] landTileImages; // A többi kártya képe

    //Singleton osztály, hogy a képeket csak egyszer lehessen betölteni egy játék során
    public static LandTileImageLoader getInstance() {
        if(instance == null) {
            instance = new LandTileImageLoader();
            landTileImages = new Image[71];
        }    
        return instance;
    }
    
    // Beállítja a megkevert id-kat a szervertől elkérteknek megfelelőre
    public void init(int[] shuffledIdArray) throws IOException {
        this.shuffledIdArray = shuffledIdArray;
        loadImages();
    }
    
    // Betölti a képeket
    private void loadImages() throws IOException {
        starterLandTile = new Image("/resources/images/landtiles/0.png");
         
        final String path = "resources/images/landtiles";
        final File jarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
         
        if(jarFile.isFile()) { 
            final JarFile jar = new JarFile(jarFile);
            for(int i=0; i<71; i++) {
                final Enumeration<JarEntry> entries = jar.entries(); // A jar fájl minden tartalmát visszaadja
                while(entries.hasMoreElements()) {
                    final String name = entries.nextElement().getName();
                    if (name.equals(path + "/" + shuffledIdArray[i] + ".png")) { //Az útvonal és a képID alapján szűri, hogy melyik a megfelelő kép
                        landTileImages[i] = new Image("/" + name);
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


