package view.dialogs;

import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;

public class WarningDialog {
    
    private String title;
    private String text;

    public WarningDialog(String title, String text) {
        this.title = title;
        this.text = text;
        
        showWarningDialog();
    }
    
    private void showWarningDialog() {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(this.getClass().getResource("/resources/css/style.css").toExternalForm());
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(text);
            alert.showAndWait();
    }
    
}
