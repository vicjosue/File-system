package FileSystem.UiComponents;
import FileSystem.Utilities.Archivo;
import FileSystem.Utilities.Directorio;
import FileSystem.Utilities.Fichero;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import jfxtras.styles.jmetro.MDL2IconFont;

public class ExplorerCell extends ListCell<Fichero> {
    @Override
    public void updateItem(Fichero item, boolean empty) {
        super.updateItem(item, empty);
        MDL2IconFont iconFont1;
        if (item instanceof Archivo) {
            iconFont1 = new MDL2IconFont("\uE8A5");
        } else if (item instanceof Directorio) {
            iconFont1 = new MDL2IconFont("\uE8B7");
        } else {
            iconFont1 = new MDL2IconFont("\uF22E");
        }
         
        Label label = new Label("");
        HBox row = new HBox(10);
        row.getChildren().add(iconFont1);
        row.getChildren().add(label);
        
        if (item != null && !empty) {
            label.setText(item.getName());
            setGraphic(row);
        } else {
            setGraphic(null);
            label.setText("");
        }
    }
}