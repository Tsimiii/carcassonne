package view;

import javafx.scene.Group;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class LoadingScreen extends Group{
    
    private VBox vbox;
    private Text text;
    private Text timer;
    
    public LoadingScreen() {
        vbox = new VBox(30);
        this.getChildren().add(vbox);
        text = new Text("Itt lesz egy lodingos pörgős vacak.");
        timer = new Text("Itt lesz egy visszaszámláló");
        vbox.getChildren().addAll(text, timer);
    }
    
}
