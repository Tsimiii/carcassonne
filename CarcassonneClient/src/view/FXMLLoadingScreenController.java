package view;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.text.Text;

public class FXMLLoadingScreenController implements Initializable {
    
    @FXML protected Text timer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }
    
    public void setTimer(String time) {
        timer.setText(time);
    }
    
}