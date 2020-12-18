package FileSystem.Utilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

import com.google.common.io.CharStreams;

import FileSystem.Exceptions.InsufficientSpaceException;
import FileSystem.Exceptions.ItemAlreadyExistsException;
import FileSystem.Exceptions.PathNotFoundException;
import javafx.util.Pair;

import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.mozilla.universalchardet.ReaderFactory;

import java.nio.charset.StandardCharsets;
import java.nio.charset.UnmappableCharacterException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;

public class FileSystem {
    private HashMap<String, Fichero> data;
    private static FileSystem instance;
    private Directorio actualDirectory;
    private String actualPath;
    private int sectores;
    private int tamano;// tamaño de cada sector
    public Function<Void, Void> navigateCallback;
    public Function<Void, Void> changesCallback;
    public Function<Void, Void> treeCallback;
    ArrayList<Integer> usedSectors = new ArrayList<>();

    public static FileSystem getInstance() { // CREATE
        if (instance == null) {
            instance = new FileSystem();
        }
        return instance;
    }

    private FileSystem() {
        data = new HashMap<>();
        data.put("root", new Directorio("root"));
        actualPath = "root/";
    }

    public ArrayList<Pair<String, Fichero>> find(String fichero) {
        /*
         * Set initial path and directory for making a call to the method search
         */
        ArrayList<Pair<String, Fichero>> result = new ArrayList<>();

        Directorio temp = (Directorio) data.get("root");
        String path = "root/";
        if (fichero.equals("root")) {
            result.add(new Pair<>(path, temp));
        }
        search(fichero, path, temp, result);
        return result;// value still remains in result
    }

    private String stringToPattern(String string) {
        String nuevo;
        nuevo = string.replace(".", "\\.");
        nuevo = nuevo.replace("*", ".*");
        return nuevo + "$";// end of string
    }

    private void search(String fichero, String path, Directorio searchMap, ArrayList<Pair<String, Fichero>> result) {
        /*
         * Iterates over all tree, if there is a coincidence then add the coincidence
         */
        for (Map.Entry<String, Fichero> data : searchMap.getHashMap().entrySet()) {
            double jWS = new JaroWinklerSimilarity().apply(fichero, data.getValue().getName());
            if (jWS > 0.75 || data.getValue().getName().equals(fichero) || Pattern.compile(stringToPattern(fichero)).matcher(data.getKey()).find()) {
                result.add(new Pair<String, Fichero>(path, data.getValue()));
            }
            
            if (data.getValue() instanceof Directorio) {
                if (data.getKey().equals(fichero)) {
                    result.add(new Pair<String, Fichero>(path, data.getValue()));
                }
                Directorio temp = (Directorio) data.getValue();
                search(fichero, path + data.getKey() + "/", temp, result);

            }
        }
    }

    // change dir
    public Directorio goToDir(String path) throws PathNotFoundException {
        String delims = "[/]";
        String[] dirs = path.split(delims);

        Directorio actual = (Directorio) data.get(dirs[0]);// root
        String tempPath = dirs[0] + "/";
        Directorio temp;

        for (int i = 1; i < dirs.length; i++) {
            tempPath += dirs[i] + "/";
            temp = (Directorio) actual;
            if (temp.contains(dirs[i]) && temp.getData(dirs[i]) instanceof Directorio) {
                actual = (Directorio) temp.getData(dirs[i]);
            } else {
                throw new PathNotFoundException();
            }
        }
        actualPath = tempPath;
        actualDirectory = actual;
        navigateCallbackEmit();
        return actualDirectory;
    }

    public Directorio ChangeDirUp() {
        String delims = "[/]";
        String[] dirs = actualPath.split(delims);

        actualDirectory = (Directorio) data.get(dirs[0]);// root
        actualPath = dirs[0] + "/";
        Directorio temp;

        for (int i = 1; i < dirs.length - 1; i++) {
            actualPath += dirs[i] + "/";
            temp = (Directorio) actualDirectory;
            actualDirectory = (Directorio) temp.getData(dirs[i]);
        }

        navigateCallbackEmit();
        return actualDirectory;
    }

    public Directorio ChangeDirDown(String directory) {
        Directorio temp = (Directorio) actualDirectory;
        actualDirectory = (Directorio) temp.getData(directory);
        actualPath += directory + "/";

        navigateCallbackEmit();

        return actualDirectory;
    }

    public void move(String oldPath, String newPath) {
        /* this function doesn't change anything from the virtual disk */
        String delims = "[/]";
        String[] dirs = oldPath.split(delims);

        Directorio tempDirectory = (Directorio) data.get(dirs[0]);// root
        Directorio temp;
        int i = 1;
        for (; i < dirs.length - 1; i++) {
            temp = (Directorio) tempDirectory;
            tempDirectory = (Directorio) temp.getData(dirs[i]);
        }

        Archivo file = (Archivo) tempDirectory.getHashMap().get(dirs[i]);//
        tempDirectory.delete(dirs[i]);
        List<String> lines;
        try {
            lines = Files.readAllLines(Paths.get("disk.txt"), StandardCharsets.UTF_8);
            ArrayList<Integer> sectoresArchivo = file.pointers;
            for (Integer sector : sectoresArchivo) {
                usedSectors.remove(sector);
                lines.set(sector, "");
            }
            file.pointers.clear();
            BufferedWriter writer = new BufferedWriter(new FileWriter("disk.txt"));
            writer.write("");// delete old stuff
            writer.close();
            writer = new BufferedWriter(new FileWriter("disk.txt", true));
            for (String str : lines) {
                writer.write(str + System.lineSeparator());
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] dirs2 = newPath.split(delims);

        Directorio tempDirectory2 = (Directorio) data.get(dirs2[0]);// root
        i = 1;
        for (; i < dirs2.length - 1; i++) {
            temp = (Directorio) tempDirectory2;
            tempDirectory2 = (Directorio) temp.getData(dirs2[i]);
        }
        try {
            addToDisk((Archivo) file, file.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        tempDirectory2.add(file.getName(), file);
        changesCallbackEmit();
    }

    public void copyFromFileSystem(String originalPath, String newPath) throws InsufficientSpaceException, IOException {
        String delims = "[/]";
        String[] dirs = originalPath.split(delims);

        Directorio tempDirectory = (Directorio) data.get(dirs[0]);// root
        Directorio temp;
        int i = 1;
        for (; i < dirs.length - 1; i++) {
            temp = (Directorio) tempDirectory;
            tempDirectory = (Directorio) temp.getData(dirs[i]);
        }
        Archivo file = (Archivo) tempDirectory.getHashMap().get(dirs[i]);
        Archivo nuevo = new Archivo(file.name, file.extension, file.name);
        Timestamp time = new Timestamp(new java.util.Date().getTime());
        nuevo.fechaCreacion = time;
        nuevo.fechaModificacion = time;
        String serialized = nuevo.toString();
        nuevo.tamano = serialized.length();
        if (!addToDisk((Archivo) nuevo, serialized)) {
            throw new InsufficientSpaceException();
        }

        String[] dirs2 = newPath.split(delims);

        Directorio tempDirectory2 = (Directorio) data.get(dirs2[0]);// root
        i = 1;
        for (; i < dirs2.length - 1; i++) {
            temp = (Directorio) tempDirectory2;
            tempDirectory2 = (Directorio) temp.getData(dirs2[i]);
        }
        tempDirectory2.add(nuevo.getName(), nuevo);
        changesCallbackEmit();
    }

    public boolean copyFromComputer(File fichero, String virtualPath) throws InsufficientSpaceException, IOException {
        String delims = "[/]";
        String[] dirs = virtualPath.split(delims);
        Directorio tempDirectory = (Directorio) data.get(dirs[0]);// root
        Directorio temp;
        int i = 1;
        for (; i < dirs.length - 1; i++) {
            temp = (Directorio) tempDirectory;
            tempDirectory = (Directorio) temp.getData(dirs[i]);
        }
        if (fichero.isDirectory()) { // directory
            Directorio nuevo = new Directorio(dirs[dirs.length - 1]);
            tempDirectory.add(nuevo.getName(), nuevo);// agregar directorio
            return copyDirectoryFromComputer(fichero, nuevo);

        } else { // file
            String fileName = dirs[dirs.length - 1];
            int index = fileName.lastIndexOf('.');

            Archivo nuevo = new Archivo(fileName.substring(0, index), fileName.substring(index + 1));// name
            nuevo.text = readFile(fichero);

            return addFichero(nuevo, virtualPath);
        }
    }

    private String readFile(File file) throws IOException {
        Reader reader = ReaderFactory.createBufferedReader(file);
        try {
            return CharStreams.toString(reader);
        } catch (UnmappableCharacterException e) {
            return e.toString();
        }
    }

    private boolean copyDirectoryFromComputer(File folder, Directorio directory) throws InsufficientSpaceException, IOException {
        /*
         * if false runout of space
         */
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                Directorio nuevo = new Directorio(fileEntry.getName());
                directory.add(nuevo.getName(), nuevo);// agregar directorio
                if (!copyDirectoryFromComputer(fileEntry, nuevo)) {
                    throw new InsufficientSpaceException();
                }
            } else {
                String fileName = fileEntry.getName();
                int index = fileName.lastIndexOf('.');

                Archivo nuevo = new Archivo(fileName.substring(0, index), fileName.substring(index + 1));// name

                nuevo.text = readFile(fileEntry);

                try {
                    Timestamp time = new Timestamp(new java.util.Date().getTime());
                    nuevo.fechaCreacion = time;
                    nuevo.fechaModificacion = time;
                    String serialized = nuevo.toString();
                    nuevo.tamano = serialized.length();
                    if (!addToDisk(nuevo, serialized)) {
                        throw new InsufficientSpaceException();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                directory.add(nuevo.getName(), nuevo);
                changesCallbackEmit();
            }
        }
        return true;
    }

    public void copyToComputer(Fichero file, String computerPath) {
        /*
         * Can copy a folder or a file to computer computerPath requires slash caracter
         * at the end
         * 
         * 
         */
        if (file instanceof Archivo) {
            try {
                Archivo archivo = (Archivo) file;
                File myObj = new File(computerPath + file.getName());
                myObj.createNewFile();// create file
                FileWriter myWriter = new FileWriter(computerPath + file.getName());
                myWriter.write(archivo.text);// write file
                myWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Directorio directorio = (Directorio) file;
            for (Map.Entry<String, Fichero> data : directorio.getHashMap().entrySet()) {
                File realDirectorio = new File(computerPath + data.getKey());
                if (!realDirectorio.exists()) {
                    realDirectorio.mkdirs();
                }
                copyToComputer(data.getValue(), computerPath + data.getKey() + "/");
            }
        }
    }

    private boolean addFichero(Archivo fichero, String destinyPath) throws InsufficientSpaceException {
        try {
            Timestamp time = new Timestamp(new java.util.Date().getTime());
            fichero.fechaCreacion = time;
            fichero.fechaModificacion = time;
            String serialized = fichero.toString();
            fichero.tamano = serialized.length();
            if (!addToDisk((Archivo) fichero, serialized)) {
                throw new InsufficientSpaceException();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String delims = "[/]";
        String[] dirs2 = destinyPath.split(delims); // add file to virtual

        Directorio tempDirectory2 = (Directorio) data.get(dirs2[0]);// root
        Directorio temp;
        int i = 1;
        for (; i < dirs2.length - 1; i++) {
            temp = (Directorio) tempDirectory2;
            tempDirectory2 = (Directorio) temp.getData(dirs2[i]);
        }
        tempDirectory2.add(fichero.getName(), fichero);
        changesCallbackEmit();
        return true;
    }

    public boolean addFichero(String name, Fichero fichero, boolean reemplazar) throws InsufficientSpaceException, ItemAlreadyExistsException {
        if(!reemplazar && actualDirectory.contains(name)){
            throw new ItemAlreadyExistsException();
        }
        if (fichero instanceof Archivo) {
            try {
                Archivo file = (Archivo) fichero;
                Timestamp time = new Timestamp(new java.util.Date().getTime());
                file.fechaCreacion = time;
                file.fechaModificacion = time;
                String serialized = fichero.toString();
                file.tamano = serialized.length();
                if (reemplazar && actualDirectory.contains(name)) {
                    remove(name);
                }
                if (!addToDisk((Archivo) fichero, serialized)) {
                    throw new InsufficientSpaceException();// not enough space
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        actualDirectory.add(name, fichero);
        changesCallbackEmit();
        return true;
    }

    private List<String> splitEqually(String text, int size) {
        // Give the list the right capacity to start with. You could use an array
        // instead if you wanted.
        List<String> ret = new ArrayList<String>((text.length() + size - 1) / size);

        for (int start = 0; start < text.length(); start += size) {
            ret.add(text.substring(start, Math.min(text.length(), start + size)));
        }

        return ret;
    }

    private boolean addToDisk(Archivo fichero, String serialized) throws IOException {
        serialized = removeLineBreaks(serialized);
        List<String> lines = Files.readAllLines(Paths.get("disk.txt"), StandardCharsets.UTF_8);
        List<String> splittedFile = splitEqually(serialized, this.tamano);
        if (splittedFile.size() > (sectores - usedSectors.size())) {
            return false; // not enough space
        }
        int i = 0;
        for (String token : splittedFile) {
            while (usedSectors.contains(i)) {
                i++;// find free space
            }
            usedSectors.add(i);
            fichero.pointers.add(i);
            lines.set(i, token);
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter("disk.txt"));
        writer.write("");// delete old stuff
        writer.close();
        writer = new BufferedWriter(new FileWriter("disk.txt", true));
        for (String str : lines) {
            writer.write(str + System.lineSeparator());
        }
        System.out.println("added file " + fichero.getName() + " in: ");
        System.out.println(fichero.pointers);
        writer.close();
        return true;
    }

    public boolean modifyFichero(String name, Fichero fichero) {
        /* Return true if succesfully added */
        if (fichero instanceof Archivo) {
            try {
                Archivo file = (Archivo) fichero;
                Timestamp time = new Timestamp(new java.util.Date().getTime());
                file.fechaModificacion = time;
                String serialized = fichero.toString();
                file.tamano = serialized.length();
                remove(name);// delete from disk
                file.pointers.clear();// delete pointers
                if (!addToDisk((Archivo) fichero, serialized)) { // new pointers
                    return false;// not enough space
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        actualDirectory.add(name, fichero);
        changesCallbackEmit();
        return true;
    }

    public boolean exists(String name) {
        return actualDirectory.contains(name);
    }

    public void remove(String name) {
        /* Remove a file from virtual disk */
        Fichero fichero = actualDirectory.getHashMap().get(name);

        if (fichero instanceof Archivo) {
            List<String> lines;
            try {
                lines = Files.readAllLines(Paths.get("disk.txt"), StandardCharsets.UTF_8);
                Archivo temp = (Archivo) fichero;
                ArrayList<Integer> sectoresArchivo = temp.pointers;
                for (Integer sector : sectoresArchivo) {
                    usedSectors.remove(sector);
                    lines.set(sector, "");
                }
                actualDirectory.delete(name);
                BufferedWriter writer = new BufferedWriter(new FileWriter("disk.txt"));
                writer.write("");// delete old stuff
                writer.close();
                writer = new BufferedWriter(new FileWriter("disk.txt", true));
                for (String str : lines) {
                    writer.write(str + System.lineSeparator());
                }
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Directorio directorio = (Directorio) fichero;
            for (Map.Entry<String, Fichero> data : directorio.getHashMap().entrySet()) {
                removeRecursive(data.getValue());
                directorio.delete(data.getKey());
            }
            actualDirectory.delete(name);
        }
        changesCallbackEmit();
    }

    private void removeRecursive(Fichero fichero) {
        if (fichero instanceof Archivo) {
            List<String> lines;
            try {
                lines = Files.readAllLines(Paths.get("disk.txt"), StandardCharsets.UTF_8);
                Archivo temp = (Archivo) fichero;
                ArrayList<Integer> sectoresArchivo = temp.pointers;
                for (Integer sector : sectoresArchivo) {
                    usedSectors.remove(sector);
                    lines.set(sector, "");
                }
                BufferedWriter writer = new BufferedWriter(new FileWriter("disk.txt"));
                writer.write("");// delete old stuff
                writer.close();
                writer = new BufferedWriter(new FileWriter("disk.txt", true));
                for (String str : lines) {
                    writer.write(str + System.lineSeparator());
                }
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Directorio directorio = (Directorio) fichero;
            for (Map.Entry<String, Fichero> data : directorio.getHashMap().entrySet()) {
                if (data.getValue() instanceof Directorio) {
                    removeRecursive(data.getValue());
                    directorio.delete(data.getKey());
                } else {
                    directorio.delete(data.getKey());
                }
            }
        }
    }

    public void create(int sectores, int tamano) throws IOException {
        this.sectores = sectores;// inicia en 0
        this.tamano = tamano;
        BufferedWriter writer = new BufferedWriter(new FileWriter("disk.txt"));
        writer.write("");// delete old stuff
        writer.close();

        File file = new File("disk.txt");
        FileWriter fr = new FileWriter(file, true);
        for (int i = 0; i < sectores; i++) {
            fr.write(System.lineSeparator());
        }

        fr.close();
    }

    public String getActualPath() {
        return actualPath;
    }

    public Directorio getActualDirectory() {
        return actualDirectory;
    }

    public void navigateCallbackEmit() {
        if (navigateCallback != null) {
            this.navigateCallback.apply(null);
        }
        treeCallbackEmit();
    }

    public void changesCallbackEmit() {
        if (navigateCallback != null) {
            this.changesCallback.apply(null);
        }
        treeCallbackEmit();
    }

    public void treeCallbackEmit() {
        if (treeCallback != null) {
            this.treeCallback.apply(null);
        }
    }

    public String getTree() {
        String tree = "";
        for (Map.Entry<String, Fichero> dat : data.entrySet()) {
            if (dat.getValue() instanceof Directorio) {
                tree += getTreeRecursive(dat.getValue(), tree, " \u2514\u2500 ");
            } else {
                tree += dat.getKey() + "\n";
            }
        }

        return tree;
    }

    private String getTreeRecursive(Fichero fichero, String tree, String level) {
        tree += level+fichero.getName()+"\n";
        if (fichero instanceof Directorio) {
            Directorio dir = (Directorio) fichero;
            for (Map.Entry<String, Fichero> data : dir.getHashMap().entrySet()) {
                tree = getTreeRecursive(data.getValue(),tree,"  |    "+level);
            }
        }
        return tree;
    }

    private String removeLineBreaks(String str) {
        String res = str;
        res = res.replaceAll("(\\r|\\n|\\r\\n)+", "\\\\n");
        return res;
    }
}