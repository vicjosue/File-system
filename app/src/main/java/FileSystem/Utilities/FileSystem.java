package FileSystem.Utilities;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.common.base.Splitter;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Timestamp;

public class FileSystem {
    private HashMap<String, Fichero> data;
    private static FileSystem instance;
    private Directorio actualDirectory;
    private String actualPath;
    private int sectores;
    private int tamano;// tamaño de cada sector
    public Function<Void, Void> navigateCallback;
    public Function<Void, Void> changesCallback;
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

    public HashMap<String, Fichero> find(String fichero) {
        /*
         * Set initial path and directory for making a call to the method search
         */
        HashMap<String, Fichero> result = new HashMap<>();
        Directorio temp = (Directorio) data.get("root");
        String path = "root";
        if (fichero == "root") {
            result.put(path, temp);
        }
        search(fichero, path, temp, result);
        return result;// value still remains in result
    }

    private void search(String fichero, String path, Directorio searchMap, HashMap<String, Fichero> result) {
        /*
         * Iterates over all tree, if there is a coincidence then add the coincidence
         */
        for (Map.Entry<String, Fichero> data : searchMap.getHashMap().entrySet()) {
            // System.out.println(data.getKey() + " = " + data.getValue());
            if (data.getKey() == fichero) {
                result.put(path, data.getValue());
            }
            if (data.getValue().getType() == Type.DIRECTORIO) {
                Directorio temp = (Directorio) data.getValue();
                search(fichero, path + "/" + data.getValue(), temp, result);

            }
        }
    }

    public Directorio ChangeDir(String path) {
        String delims = "[/]";
        String[] dirs = path.split(delims);

        actualDirectory = (Directorio) data.get(dirs[0]);// root
        actualPath = dirs[0] + "/";
        Directorio temp;

        for (int i = 1; i < dirs.length - 1; i++) {
            actualPath += dirs[i] + "/";
            temp = (Directorio) actualDirectory;
            actualDirectory = (Directorio) temp.getData(dirs[i]);
        }

        return actualDirectory;
    }

    // change dir
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

    public boolean addFichero(String name, Fichero fichero) {
        actualDirectory.add(name, fichero);
        changesCallbackEmit();
        if (fichero.getType() == Type.ARCHIVO) {
            try {
                Archivo file = (Archivo) fichero;
                //file.fechaCreacion = new Timestamp(System.currentTimeMillis());
                addToDisk((Archivo) fichero);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return true;
    }

    private boolean addToDisk(Archivo fichero) throws IOException {
        String serialized=toString(fichero);

        List<String> lines = Files.readAllLines(Paths.get("disk.txt"), StandardCharsets.UTF_8);
        if(lines.size()< (sectores-usedSectors.size())){
            return false; // not enough space
        }
        int i=0;
        for(final String token :
            Splitter
                .fixedLength(tamano/2)
                .split(serialized)){
                    
            while (i<sectores) { //search for a free sector
                i++;
                if(!usedSectors.contains(i)){
                    usedSectors.add(i);
                    fichero.pointers.add(i);
                    lines.set(i, token);
                    break;
                } else {
                    return false; //this should not happen, not enough space
                }
                
            }
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter("disk.txt", true));
        for(String str: lines) {
            writer.write(str + System.lineSeparator());
          }
        writer.close();
        return true;
    }

    public static List<String> splitEqually(String text, int size) {
        // Give the list the right capacity to start with. You could use an array
        // instead if you wanted.
        List<String> ret = new ArrayList<String>((text.length() + size - 1) / size);
    
        for (int start = 0; start < text.length(); start += size) {
            ret.add(text.substring(start, Math.min(text.length(), start + size)));
        }
        return ret;
    }

    private String toString( Serializable o ) throws IOException {
        //1 character = 2 bytes
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream( baos );
         oos.writeObject( o );
         oos.close();
         return Base64.getEncoder().encodeToString(baos.toByteArray()); 
     }
    

    public boolean exists(String name) {
        return actualDirectory.contains(name);
    }

    public void remove(String name) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get("disk.txt"), StandardCharsets.UTF_8);
        Archivo temp = (Archivo) actualDirectory.getHashMap().get(name);
        ArrayList<Integer> sectoresArchivo = temp.pointers;
        for(Integer sector: sectoresArchivo){
            usedSectors.remove(sector);
            lines.set(sector, "");
        }
        actualDirectory.delete(name);
        BufferedWriter writer = new BufferedWriter(new FileWriter("disk.txt", true));
        for(String str: lines) {
            writer.write(str + System.lineSeparator());
          }
        writer.close();
    }

    public void create(int sectores, int tamano) throws IOException {
        this.sectores=sectores;
        this.tamano=tamano;
        
        BufferedWriter writer = new BufferedWriter(new FileWriter("disk.txt"));
        writer.write("");//delete old stuff
        writer.close();
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
    }

    public void changesCallbackEmit() {
        if (navigateCallback != null) {
            this.changesCallback.apply(null);
        }
    }
}