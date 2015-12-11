package view;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.text.Text;

// A betöltő felület dinamikus megjelenítése
public class FXMLLoadingScreenController implements Initializable {
    
    @FXML protected Text timer;
    @FXML protected Text enoughJoin;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }
    
    // A másodpercek kiírásának frissítése
    public void setTimer(Object time) {
        timer.setText(time.toString());
    }
    
    // A paraméterben kapott szöveg kiírása, ha megfelelő számú játékos csatlakozott
    public void setEnoughJoinText(String time) {
        timer.setVisible(false);
        enoughJoin.setVisible(true);
        enoughJoin.setText(time);
    }
    
}
