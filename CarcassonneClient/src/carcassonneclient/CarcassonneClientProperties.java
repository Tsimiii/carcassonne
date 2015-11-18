package carcassonneclient;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class CarcassonneClientProperties {
    
    private Properties prop;
    String propFileName = "src/resources/config/config.properties";
    InputStream inputStream;

    public CarcassonneClientProperties() throws FileNotFoundException, IOException {
        prop = new Properties();
        inputStream = new FileInputStream(propFileName);
        
        if (inputStream != null) {
                prop.load(inputStream);
        } else {
                throw new FileNotFoundException("Property file '" + propFileName + "' not found in the classpath");
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
