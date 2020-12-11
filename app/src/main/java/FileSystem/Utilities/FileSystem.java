package FileSystem.Utilities;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FileSystem {
    private HashMap<String, Fichero> data;
    private static FileSystem instance;
    private Directorio actualDirectory;
    private String actualPath;
    private int sectores;
    private int tamano;// tamaño de cada sector

    public static FileSystem getInstance() { // CREATE
        if (instance == null) {
            instance = new FileSystem();
        }
        return instance;
    }

    private FileSystem() {
        data = new HashMap<>();
        data.put("root", new Directorio());
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

    // change dir
    public Directorio ChangeDirUp() {
        String delims = "[/]";
        String[] dirs = actualPath.split(delims);

        actualDirectory = (Directorio) data.get(dirs[0]);// root
        actualPath = dirs[0]+"/";
        Directorio temp;

        for (int i = 1; i < dirs.length - 1; i++) {
            actualPath += dirs[i]+"/";
            temp = (Directorio) actualDirectory;
            actualDirectory = (Directorio) temp.getData(dirs[i]);
        }

        return actualDirectory;
    }

    public Directorio ChangeDirDown(String directory) {
        Directorio temp = (Directorio) actualDirectory;
        actualDirectory = (Directorio) temp.getData(directory);
        actualPath += directory+"/";
        
        return actualDirectory;
    }

    public void addFichero(String name, Fichero fichero) {
        actualDirectory.add(name, fichero);
    }

    public boolean exists(String name) {
        return actualDirectory.contains(name);
    }

    public void remove(String name) {
        actualDirectory.delete(name);
    }

    public void create(int sectores, int tamano) throws IOException {
        this.sectores=sectores;
        this.tamano=tamano;
        
        File file = new File("disk.txt");
        // Si el archivo no existe es creado
        if (!file.exists()) {
            file.createNewFile();
        }
    }
}