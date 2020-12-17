package FileSystem;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.JMetroStyleClass;
import jfxtras.styles.jmetro.Style;
import FileSystem.Utilities.FileSystem;

public class FileTreeWindow  {

    Label tree;
    FileSystem fileSystem = FileSystem.getInstance();

    FileTreeWindow() {
        VBox box = new VBox();
        tree = new Label();
        box.getChildren().add(tree);
        box.getStyleClass().add(JMetroStyleClass.BACKGROUND);

        Scene scene = new Scene(box, 200, 480);

        
        Stage stage = new Stage();
        stage.setTitle("FS Tree");

        JMetro jMetro = new JMetro(scene, Style.DARK);

        fileSystem.treeCallback = (Void) -> { refreshTree(); return Void; };

        refreshTree();
        
        stage.setScene(scene);
        stage.show();

    }

    private void refreshTree() {
        tree.setText(fileSystem.getTree());
    }
    
}
