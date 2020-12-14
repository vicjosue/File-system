/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package FileSystem;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.FlatDialog;
import jfxtras.styles.jmetro.FlatTextInputDialog;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Optional;

import FileSystem.Exceptions.PathNotFoundException;
import FileSystem.UiComponents.ExplorerCell;
import FileSystem.Utilities.Archivo;
import FileSystem.Utilities.Directorio;
import FileSystem.Utilities.Fichero;
import FileSystem.Utilities.FileSystem;
import FileSystem.Utilities.Triplet;
import javafx.scene.control.ListCell;
import javafx.util.Callback;
import javafx.util.Pair;

public class App extends Application {
    FileSystem fileSystem = FileSystem.getInstance();
    TextField navigationTextField;
    ObservableList<Fichero> listItems;
    ToolBar navigationToolBar;
    BorderPane border;

    @Override
    public void start(Stage stage) {
        Button navigateUpButton = new Button("Up");
        navigateUpButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                fileSystem.ChangeDirUp();
            }
        });
        navigationTextField = new TextField();
        navigationTextField.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                navigateToDir();
            }
        });

        Button navigateGoButton = new Button("Go");
        navigateGoButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                navigateToDir();
            }
        });

        Button actionImportFileButton = new Button("Import file");
        actionImportFileButton.setOnAction(event -> importFile(stage));

        Button actionImportDirButton = new Button("Import directory");
        actionImportDirButton.setOnAction(event -> importDirectory(stage));

        Button actionNewDirButton = new Button("New Dir");

        actionNewDirButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                createDir(stage);
            }
        });

        Button actionNewFileButton = new Button("New File");
        actionNewFileButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                createFile(stage);
            }
        });

        navigationToolBar = new ToolBar(
            navigateUpButton,
            navigationTextField,
            navigateGoButton,
            new Separator(),
            actionImportFileButton,
            actionImportDirButton,
            new Separator(),
            actionNewDirButton,
            actionNewFileButton
        );
       
        Button createFSButton = new Button("Create File System");
        createFSButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                createFS(stage);
            }
        });

        ToolBar initialToolbar = new ToolBar(
            createFSButton
        );
        
        ListView<Fichero> list = new ListView<Fichero>();
        Directorio actualDir = fileSystem.ChangeDirUp();
        listItems = FXCollections.observableArrayList (
            actualDir.getHashMap().values()
        );
        list.setItems(listItems);
        list.setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent click) {
        
                if (click.getClickCount() == 2) {
                   //Use ListView's getSelected Item
                   Fichero selected = list.getSelectionModel().getSelectedItem();
                   if (selected != null) {
                       openFichero(stage, selected);
                   }
                }
                list.getSelectionModel().clearSelection();
            }
        });

        list.setCellFactory(new Callback<ListView<Fichero>, 
            ListCell<Fichero>>() {
                @Override 
                public ListCell<Fichero> call(ListView<Fichero> list) {
                    ExplorerCell cell = new ExplorerCell();
                    
                    ContextMenu contextMenu = new ContextMenu();


                    MenuItem openItem = new MenuItem();
                    openItem.setText("Open");
                    openItem.setOnAction(event -> {
                        openFichero(stage, cell.getItem());
                    });

                    MenuItem copyItem = new MenuItem();
                    copyItem.setText("Copy");
                    copyItem.setOnAction(event -> copyFile(stage, cell.getItem()));

                    MenuItem moveItem = new MenuItem();
                    moveItem.setText("Move");
                    moveItem.setOnAction(event -> moveFile(stage, cell.getItem()));

                    MenuItem exportItem = new MenuItem();
                    exportItem.setText("Export");
                    exportItem.setOnAction(event -> exportFile(stage, cell.getItem()));

                    MenuItem propertiesItem = new MenuItem();
                    propertiesItem.setText("Properties");
                    propertiesItem.setOnAction(event -> filePropertiesDialog(stage, (Archivo) cell.getItem()));


                    MenuItem deleteItem = new MenuItem();
                    deleteItem.setText("Remove");
                    deleteItem.setOnAction(event -> removeFichero(cell.getItem()));


                    cell.itemProperty().addListener((obs, oldItem, newItem) -> {
                        if (newItem == null) {
                            contextMenu.getItems().clear();
                        } else {
                            contextMenu.getItems().clear();
                            if (newItem instanceof Archivo) {
                                contextMenu.getItems().addAll(openItem, propertiesItem, new SeparatorMenuItem(), moveItem, copyItem, exportItem, new SeparatorMenuItem(), deleteItem);
                            } else {
                                contextMenu.getItems().addAll(openItem, new SeparatorMenuItem(), moveItem, copyItem, exportItem, new SeparatorMenuItem(), deleteItem);
                            }
                        }
                    });



                    cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
                        if (isNowEmpty) {
                            cell.setContextMenu(null);
                        } else {
                            cell.setContextMenu(contextMenu);
                        }
                    });
                    return cell ;
                }
            }
        );

        border = new BorderPane();
        border.setTop(initialToolbar);
        border.setCenter(list);

        refreshView();
        fileSystem.changesCallback = fileSystem.navigateCallback = (Void) -> { refreshView(); return Void; };
        
        Scene scene = new Scene(border, 640, 480);

        JMetro jMetro = new JMetro(scene, Style.DARK);

        stage.setScene(scene);
        stage.show();
    }

    private void refreshView() {
        navigationTextField.setText(fileSystem.getActualPath());
        listItems.clear();
        listItems.addAll(fileSystem.getActualDirectory().getHashMap().values());
    }

    private void createDir(Stage owner) {
        FlatTextInputDialog dialog = new FlatTextInputDialog();
        dialog.initOwner(owner);
        dialog.setTitle("Create a new directory");
        dialog.setHeaderText("Create a new directory");
        dialog.setContentText("Enter directory name:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()){
            fileSystem.addFichero(result.get(), new Directorio(result.get()));
        }

    }

    private void createFile(Stage owner) {
        FlatDialog<Triplet<String, String, String>> dialog = new FlatDialog<Triplet<String, String, String>>();
        dialog.initOwner(owner);
        dialog.setTitle("Create a new file");
        dialog.setHeaderText("Create a new file");
        
        // Set the button types.
        ButtonType createButtonType = new ButtonType("Create", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        // Create the username and password labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField name = new TextField();
        TextField extension = new TextField();
        TextArea content = new TextArea();

        grid.add(new Label("Name:"), 0, 0);
        grid.add(name, 1, 0);
        grid.add(new Label("Extension:"), 0, 1);
        grid.add(extension, 1, 1);
        grid.add(new Label("Content:"), 0, 2);
        grid.add(content, 1, 2);

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(() -> name.requestFocus());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                return new Triplet<>(name.getText(), extension.getText(), content.getText());
            }
            return null;
        });

        Optional<Triplet<String, String, String>> result = dialog.showAndWait();

        if (result.isPresent()) {
            boolean res = fileSystem.addFichero(result.get().getFirst() + "." + result.get().getSecond(), new Archivo(result.get().getFirst(), result.get().getSecond(), result.get().getThird()));
            if (!res) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.initOwner(owner);
                alert.setTitle("Error Creando Archivo");
                alert.setHeaderText("No se ha podido crear el archivo");
                alert.setContentText("No ha sido posible crear el archivo debido a que no hay suficiente espacio disponible.");
                alert.showAndWait();
            }
        }
    }

    private void editFile(Stage owner, Archivo file) {
        FlatDialog<String> dialog = new FlatDialog<String>();
        dialog.initOwner(owner);
        dialog.setTitle("View: " + file.getName());
        dialog.setHeaderText("Viewing: " + file.getName());
        
        // Set the button types.
        ButtonType saveButtonType = new ButtonType("Save", ButtonData.APPLY);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CLOSE);

        VBox pane = new VBox();

        TextArea content = new TextArea();

        content.setText(file.text);

        pane.getChildren().add(content);

        dialog.getDialogPane().setContent(pane);

        Platform.runLater(() -> content.requestFocus());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return content.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {
            file.text = result.get();
            fileSystem.modifyFichero(file.getName(), file);
        }
    }
    
    private void filePropertiesDialog(Stage owner, Archivo file) {
        FlatDialog<String> dialog = new FlatDialog<String>();
        dialog.initOwner(owner);
        dialog.setTitle("Properties: " + file.getName());
        dialog.setHeaderText("Properties");

        // Set the button types.
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        Label name = new Label(file.name);
        Label extension = new Label(file.extension);
        TextArea content = new TextArea(file.text);
        Label creationDate = new Label(dateFormat.format(file.fechaCreacion));
        Label modificationDate = new Label(dateFormat.format(file.fechaModificacion));
        Label size = new Label(String.valueOf(file.tamano));
        content.setDisable(true);

        grid.add(new Label("Name:"), 0, 0);
        grid.add(name, 1, 0);
        grid.add(new Label("Extension:"), 0, 1);
        grid.add(extension, 1, 1);
        grid.add(new Label("Size:"), 0, 2);
        grid.add(size, 1, 2);
        grid.add(new Label("Creation date:"), 0, 3);
        grid.add(creationDate, 1, 3);
        grid.add(new Label("Modification date:"), 0, 4);
        grid.add(modificationDate, 1, 4);
        grid.add(new Label("Content:"), 0, 5);
        grid.add(content, 1, 5);

        dialog.getDialogPane().setContent(grid);


        dialog.showAndWait();
    }

    private void createFS(Stage owner) {
        FlatDialog<Pair<String, String>> dialog = new FlatDialog<Pair<String, String>>();
        dialog.initOwner(owner);
        dialog.setTitle("Create a new FileSystem");
        dialog.setHeaderText("Create a new FileSystem");
        
        // Set the button types.
        ButtonType createButtonType = new ButtonType("Create", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        // Create the username and password labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField sectorCount = new TextField();
        TextField sectorSize = new TextField();

        sectorCount.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d{0,7}")) {
                    sectorCount.setText(oldValue);
                }
            }
        });

        sectorSize.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d{0,7}")) {
                    sectorSize.setText(oldValue);
                }
            }
        });

        

        grid.add(new Label("Sector count:"), 0, 0);
        grid.add(sectorCount, 1, 0);
        grid.add(new Label("Sector size:"), 0, 1);
        grid.add(sectorSize, 1, 1);

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(() -> sectorCount.requestFocus());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                return new Pair<>(sectorCount.getText(), sectorSize.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        if (result.isPresent()) {
            try {
                fileSystem.create(Integer.parseInt(result.get().getKey()), Integer.parseInt(result.get().getValue()));
                initToolbar();
            } catch(IOException e) {
                // TODO: Show exception
                System.out.println(e);
            }
            
        }

    }

    private void initToolbar() {
        border.setTop(navigationToolBar);
    }

    private void openFichero(Stage owner, Fichero item) {
        if (item instanceof Directorio) {
            fileSystem.ChangeDirDown(item.getName());
        } else if (item instanceof Archivo) {
            editFile(owner, (Archivo) item);
        }
    }

    private void removeFichero(Fichero item) {
        fileSystem.remove(item.getName());
    }

    private void navigateToDir() {
        try {
            fileSystem.goToDir(navigationTextField.getText());
        } catch (PathNotFoundException e) {
            navigationTextField.setText(fileSystem.getActualPath());
        }
    }

    private void importFile(Stage owner) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open file to import");
        File selectedFile = fileChooser.showOpenDialog(owner);
        if (selectedFile != null) {
            FlatTextInputDialog dialog = new FlatTextInputDialog();
            dialog.initOwner(owner);
            dialog.setTitle("Enter destination");
            dialog.setHeaderText("Enter destination for selected file (including it)");
            dialog.setContentText("Destination:");
            dialog.getEditor().setText(fileSystem.getActualPath() + selectedFile.getName());

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()){
                fileSystem.copyFromComputer(selectedFile, result.get());
            }
        }
    }

    private void importDirectory(Stage owner) {
        DirectoryChooser fileChooser = new DirectoryChooser();
        fileChooser.setTitle("Open directory to import");
        File selectedFile = fileChooser.showDialog(owner);
        if (selectedFile != null) {
            FlatTextInputDialog dialog = new FlatTextInputDialog();
            dialog.initOwner(owner);
            dialog.setTitle("Enter destination");
            dialog.setHeaderText("Enter destination for selected directory (including it)");
            dialog.setContentText("Destination:");
            dialog.getEditor().setText(fileSystem.getActualPath() + selectedFile.getName());

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()){
                fileSystem.copyFromComputer(selectedFile, result.get());
            }
        }
    }

    private void copyFile(Stage owner, Fichero file) {
        FlatTextInputDialog dialog = new FlatTextInputDialog();
        dialog.initOwner(owner);
        dialog.setTitle("Copy selection");
        dialog.setHeaderText("Enter destination for selection (including it)");
        dialog.setContentText("Destination:");
        dialog.getEditor().setText(fileSystem.getActualPath() + file.getName());

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()){
            fileSystem.copyFromFileSystem(fileSystem.getActualPath() + file.getName(), result.get());
        }
    }

    private void moveFile(Stage owner, Fichero file) {
        FlatTextInputDialog dialog = new FlatTextInputDialog();
        dialog.initOwner(owner);
        dialog.setTitle("Move selection");
        dialog.setHeaderText("Enter destination for selection (including it)");
        dialog.setContentText("Destination:");
        dialog.getEditor().setText(fileSystem.getActualPath() + file.getName());

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()){
            fileSystem.move(fileSystem.getActualPath() + file.getName(), result.get());
        }
    }

    private void exportFile(Stage owner, Fichero file) {
        DirectoryChooser fileChooser = new DirectoryChooser();
        fileChooser.setTitle("Choose directory to export to");
        File selectedFile = fileChooser.showDialog(owner);
        if (selectedFile != null) {
            FlatTextInputDialog dialog = new FlatTextInputDialog();
            dialog.initOwner(owner);
            dialog.setTitle("Export destination");
            dialog.setHeaderText("Enter export destination");
            dialog.setContentText("Destination:");
            dialog.getEditor().setText(selectedFile.getAbsolutePath() + selectedFile.getName());

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()){
                fileSystem.copyToComputer(file, result.get());
            }
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
