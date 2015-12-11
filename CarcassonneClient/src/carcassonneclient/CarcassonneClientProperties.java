package carcassonneclient;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


//A megjelenítés statikus értékeinek betöltését szolgáló osztály
public class CarcassonneClientProperties {
    
    private Properties prop;
    String propFileName = "/resources/config/config.properties"; //a fájl, ahonnan az adatok betöltődnek
    InputStream inputStream;

    public CarcassonneClientProperties() throws FileNotFoundException, IOException {
        prop = new Properties();
        inputStream = this.getClass().getResourceAsStream(propFileName);
        
        if (inputStream != null) {
                prop.load(inputStream); //A prop változóba betölti az adatokat
        } else {
                throw new FileNotFoundException("A " + propFileName + " Property fájl nem található a megadott útvonalon.");
        }
    }
    
    public int getRightButtonRowNumber() {
        return Integer.parseInt(prop.getProperty("rightbuttonrow"));
    }
    
    public int getRightButtonColumnNumber() {
        return Integer.parseInt(prop.getProperty("rightbuttoncolumn"));
    }
    
    public int getLandTileNumber() {
        return Integer.parseInt(prop.getProperty("landTileNumber"));
    }
    
    public int getTableSize() {
        return Integer.parseInt(prop.getProperty("tablesize"));
    }
    
}
