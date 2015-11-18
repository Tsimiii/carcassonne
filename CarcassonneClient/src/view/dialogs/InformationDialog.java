package view.dialogs;

import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.image.ImageView;

public class InformationDialog {
    private String title;
    private String text;
    private ImageView img;

    public InformationDialog(String title, String text, ImageView img) {
        this.title = title;
        this.text = text;
        this.img = img;
        
        showInformationDialog();
    }
    
    private void showInformationDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(this.getClass().getResource("/resources/css/style.css").toExternalForm());
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setGraphic(img);
        alert.setContentText(text);
        alert.showAndWait();
    }
}
