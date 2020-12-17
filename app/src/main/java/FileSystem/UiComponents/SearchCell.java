package FileSystem.UiComponents;

import java.util.Map.Entry;

import FileSystem.Utilities.Archivo;
import FileSystem.Utilities.Directorio;
import FileSystem.Utilities.Fichero;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import jfxtras.styles.jmetro.MDL2IconFont;

public class SearchCell extends ListCell<Entry<String, Fichero>> {
    @Override
    public void updateItem(Entry<String, Fichero> item, boolean empty) {
        super.updateItem(item, empty);
        MDL2IconFont iconFont1;
        if (item.getValue() instanceof Archivo) {
            iconFont1 = new MDL2IconFont("\uE8A5");
        } else if (item.getValue() instanceof Directorio) {
            iconFont1 = new MDL2IconFont("\uE8B7");
        } else {
            iconFont1 = new MDL2IconFont("\uF22E");
        }
         
        Label label = new Label("");
        HBox row = new HBox(10);
        row.getChildren().add(iconFont1);
        row.getChildren().add(label);
        
        if (item != null && !empty) {
            System.out.println(item.getKey());
            label.setText(item.getKey() + "/" + item.getValue().getName());
            setGraphic(row);
        } else {
            System.out.println("empty");
            setGraphic(null);
            label.setText("");
        }
    }
}