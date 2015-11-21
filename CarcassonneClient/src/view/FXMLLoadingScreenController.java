package view;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.text.Text;

public class FXMLLoadingScreenController implements Initializable {
    
    @FXML protected Text timer;
    @FXML protected Text timer2;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }
    
    public void setTimer(Object time) {
        timer.setText(time.toString());
    }
    
    public void setTimer(String time) {
        timer.setVisible(false);
        timer2.setVisible(true);
        timer2.setText(time);
    }
    
}
