package FileSystem.UiComponents;

import FileSystem.Utilities.Archivo;
import FileSystem.Utilities.Directorio;
import FileSystem.Utilities.Fichero;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.util.Pair;
import jfxtras.styles.jmetro.MDL2IconFont;

public class SearchCell extends ListCell<Pair<String, Fichero>> {
    @Override
    public void updateItem(Pair<String, Fichero> item, boolean empty) {
        super.updateItem(item, empty);

        if (item == null || empty) {
            setGraphic(null);
            return;
        }

        MDL2IconFont iconFont1;
        if (item.getValue() instanceof Archivo) {
            iconFont1 = new MDL2IconFont("\uE8A5");
        } else if (item.getValue() instanceof Directorio) {
            iconFont1 = new MDL2IconFont("\uE8D5");
        } else {
            iconFont1 = new MDL2IconFont("\uF22E");
        }
         
        Label label = new Label("");
        HBox row = new HBox(10);
        row.getChildren().add(iconFont1);
        row.getChildren().add(label);
        
        label.setText(item.getKey() + item.getValue().getName());
        setGraphic(row);
    }
}