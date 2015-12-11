package view.dialogs;

import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.image.ImageView;

// Az értesítő üzenetek megjelenítése
public class InformationDialog {
    private String title; // Az ablak neve
    private String text; // Az ablakon megjelenő szöveg
    private ImageView img; // Az ablakon megjelenő kép

    public InformationDialog(String title, String text, ImageView img) {
        this.title = title;
        this.text = text;
        this.img = img;
        
        showInformationDialog();
    }
    
    // Megjeleníti az új értesítő ablakot
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
