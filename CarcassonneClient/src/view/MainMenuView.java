package view;

import controller.Controller;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class MainMenuView extends Group{
    
    public Controller delegate;
    
    private final BorderPane borderpane;
    private final VBox vbox;
    private final VBox insideVbox;
    private final HBox hbox;
    private final Button joinGameButton;
    private TextField nameTextField;
    private Text errorNameText;
    
    public MainMenuView(double width, double height) {
        borderpane = new BorderPane();
        borderpane.prefWidth(width);
        borderpane.prefHeight(height);
        //borderpane.setId("menu_and_loading_pane");

        //borderpane.setStyle("-fx-background-image: classpath('images/stoneWall.jpg');");
        this.getChildren().add(borderpane);
       // borderpane.setStyle("-fx-background-image: url(\"file:images/title.jpg\");-fx-background-size: 500, 500;-fx-background-repeat: no-repeat;");
       // borderpane.setTop(new ImageView(new Image("file:images/title.jpg")));
       // borderpane.setStyle("-fx-background-image: url('http://www.spec-net.com.au/press/0309/images/min040309_img01.jpg');");
        
        vbox = new VBox(80);
        hbox = new HBox(30);
        insideVbox = new VBox(5);
        
        Text nameText = new Text("Név:");
        nameTextField = new TextField();
        errorNameText = new Text("");
        errorNameText.setFill(Color.RED);
        errorNameText.setDisable(true);
        hbox.getChildren().addAll(nameText, nameTextField);
        insideVbox.getChildren().addAll(hbox, errorNameText);
        
        joinGameButton = new Button("Csatlakozás a játékhoz!");
        joinGameButton.setOnAction(joinGame);
        
        vbox.getChildren().addAll(insideVbox, joinGameButton);
        
        borderpane.setCenter(vbox);
    }
    
    EventHandler<ActionEvent> joinGame = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            if(nameTextField.getText().equals("")) {
                errorNameText.setText("Nem adtál meg nevet!");
            } else {
                delegate.clickJoinGame(nameTextField.getText());
            }
        }
    };

}