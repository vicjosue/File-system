package FileSystem.UiComponents;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import jfxtras.styles.jmetro.MDL2IconFont;

public class ExplorerCell extends ListCell<String> {
    @Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        MDL2IconFont iconFont1 = new MDL2IconFont("\uE8A5");
        Label label = new Label("");
        HBox row = new HBox(10);
        row.getChildren().add(iconFont1);
        row.getChildren().add(label);
        
        if (item != null) {
            label.setText(item);
            setGraphic(row);
        }
    }
}