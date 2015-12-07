package view;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.text.Text;

public class FXMLLoadingScreenController implements Initializable {
    
    @FXML protected Text timer;
    @FXML protected Text enoughJoin;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }
    
    public void setTimer(Object time) {
        timer.setText(time.toString());
    }
    
    public void setEnoughJoinText(String time) {
        timer.setVisible(false);
        enoughJoin.setVisible(true);
        enoughJoin.setText(time);
    }
    
}
