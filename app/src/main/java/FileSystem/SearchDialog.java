package FileSystem;

import java.util.HashMap;

import FileSystem.Utilities.Fichero;
import FileSystem.Utilities.FileSystem;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.FlatDialog;

public class SearchDialog extends FlatDialog {
    FileSystem fileSystem = FileSystem.getInstance();
    ObservableList<String> items;
    TextField searchField;
    HashMap<String, Fichero> result;

    SearchDialog(Stage primaryStage) {
        super();
        initOwner(primaryStage);
        setTitle("Search");
        getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);

        VBox content = new VBox();
        searchField = new TextField();
        searchField.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                search();
            }
        });

        ListView listView = new ListView<String>();
        items = FXCollections.observableArrayList();
        listView.setItems(items);

        content.getChildren().addAll(searchField, listView);

        Platform.runLater(() -> searchField.requestFocus());


        getDialogPane().setContent(content);
    }

    private void search() {
        System.out.println("Search");
        result = fileSystem.find(searchField.getText());
        items.clear();
        items.addAll(result.keySet());
    }
}
