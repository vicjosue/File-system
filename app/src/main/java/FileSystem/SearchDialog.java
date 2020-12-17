package FileSystem;

import java.util.HashMap;
import java.util.Map.Entry;

import FileSystem.Exceptions.PathNotFoundException;
import FileSystem.UiComponents.SearchCell;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.FlatDialog;

public class SearchDialog extends FlatDialog {
    FileSystem fileSystem = FileSystem.getInstance();
    ObservableList<Entry<String, Fichero>> items;
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

        ListView<Entry<String, Fichero>> listView = new ListView<Entry<String, Fichero>>();
        items = FXCollections.observableArrayList();
        listView.setItems(items);

        listView.setCellFactory(searchListView -> new SearchCell());

        listView.setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent click) {
        
                if (click.getClickCount() == 2) {
                   //Use ListView's getSelected Item
                   Entry<String, Fichero> selected = listView.getSelectionModel().getSelectedItem();
                   if (selected != null) {
                       openFichero(selected);
                   }
                }
                listView.getSelectionModel().clearSelection();
            }
        });

        content.getChildren().addAll(searchField, listView);

        Platform.runLater(() -> searchField.requestFocus());


        getDialogPane().setContent(content);
    }

    private void search() {
        result = fileSystem.find(searchField.getText());
        items.clear();
        items.addAll(result.entrySet());
    }

    private void openFichero(Entry<String, Fichero> item) {
        try {
            fileSystem.goToDir(item.getKey());
        } catch (PathNotFoundException e) {

        }
    }
}
