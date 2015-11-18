package carcassonneserver;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class CarcassonneServerProperties {

    private Properties prop;
    String propFileName = "src/resources/config/config.properties";
    InputStream inputStream;
    
    public CarcassonneServerProperties() throws FileNotFoundException, IOException {
        prop = new Properties();
        inputStream = new FileInputStream(propFileName);
        
        if (inputStream != null) {
                prop.load(inputStream);
        } else {
                throw new FileNotFoundException("Property file '" + propFileName + "' not found in the classpath");
        }
    }
    
    public int getPort() {
        return Integer.parseInt(prop.getProperty("port"));
    }
    
    public int getPlayerNumber() {
        return Integer.parseInt(prop.getProperty("playernumber"));
    }
    
    public int getStarterInterval() {
        return Integer.parseInt(prop.getProperty("starterinterval"));
    }
    
}
