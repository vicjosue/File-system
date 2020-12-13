package FileSystem.Utilities;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Function;

import com.google.common.base.Splitter;

import java.nio.charset.StandardCharsets;
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

    // change dir
    public Directorio goToDir(String path) {
        String delims = "[/]";
        String[] dirs = path.split(delims);

        actualDirectory = (Directorio) data.get(dirs[0]);// root
        actualPath = dirs[0] + "/";
        Directorio temp;

        for (int i = 1; i < dirs.length; i++) {
            actualPath += dirs[i] + "/";
            temp = (Directorio) actualDirectory;
            actualDirectory = (Directorio) temp.getData(dirs[i]);
        }

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
        Archivo file = (Archivo) tempDirectory.getHashMap().get(dirs[i]);
        tempDirectory.delete(file.name);// delete old file

        String[] dirs2 = newPath.split(delims);

        Directorio tempDirectory2 = (Directorio) data.get(dirs2[0]);// root
        i = 1;
        for (; i < dirs2.length - 1; i++) {
            temp = (Directorio) tempDirectory2;
            tempDirectory2 = (Directorio) temp.getData(dirs2[i]);
        }
        tempDirectory2.add(file.name, file);

    }

    public void copyFromFileSystem(String originalPath, String newPath) {
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

        String[] dirs2 = newPath.split(delims);

        Directorio tempDirectory2 = (Directorio) data.get(dirs2[0]);// root
        i = 1;
        for (; i < dirs2.length - 1; i++) {
            temp = (Directorio) tempDirectory2;
            tempDirectory2 = (Directorio) temp.getData(dirs2[i]);
        }
        tempDirectory2.add(file.name, file);
    }

    public boolean copyFromComputer(String directoryPath, String virtualPath) {
        File fichero = new File(directoryPath,virtualPath);
        if (directoryPath.substring(directoryPath.length() - 1).equals("/")) { // directory
            copyDirectoryFromComputer(fichero,virtualPath);

        } else { //file
            String delims = "[/]";
            String[] dirs = virtualPath.split(delims);
            String fileName = dirs[dirs.length - 1];
            int index = fileName.lastIndexOf('.');

            Archivo nuevo = new Archivo(fileName.substring(0, index));// name
            nuevo.extension = fileName.substring(index+1);// extension

            Scanner myReader; // add text

            try {
                myReader = new Scanner(fichero);
                while (myReader.hasNextLine()) {
                    nuevo.text += myReader.nextLine();
                }
                myReader.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return addFichero(nuevo,virtualPath);
        }
        return true;
    }

    private boolean copyDirectoryFromComputer(final File folder,String virtualPath) {
        /*if false runout of space */
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                return copyDirectoryFromComputer(fileEntry,virtualPath);
            } else {
                String delims = "[/]";
                String[] dirs = virtualPath.split(delims);
                String fileName = dirs[dirs.length - 1];
                int index = fileName.lastIndexOf('.');
    
                Archivo nuevo = new Archivo(fileName.substring(0, index));// name
                nuevo.extension = fileName.substring(index+1);// extension

                Scanner myReader; // add text

                try {
                    myReader = new Scanner(folder);
                    while (myReader.hasNextLine()) {
                        nuevo.text += myReader.nextLine();
                    }
                    myReader.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                return addFichero(nuevo,virtualPath);
            }
        }
        return true;
    }


    public void copyToComputer(Fichero file, String computerPath) {
       /* Can copy a folder or a file to computer
       computerPath requires slash caracter at the end 
       
       
       */
        if(file.tipo==Type.ARCHIVO){
            try {
                Archivo archivo = (Archivo) file;
                File myObj = new File(computerPath+file.name+"."+archivo.extension);
                myObj.createNewFile();//create file
                FileWriter myWriter = new FileWriter(computerPath+file.name+"."+archivo.extension);
                myWriter.write(archivo.text);//write file
                myWriter.close();
                } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Directorio directorio = (Directorio) file;
            for (Map.Entry<String, Fichero> data : directorio.getHashMap().entrySet()) {
                // System.out.println(data.getKey() + " = " + data.getValue());
                File realDirectorio = new File(computerPath+data.getKey());
                if (!realDirectorio.exists()) {
                    realDirectorio.mkdirs();
                }
                copyToComputer(data.getValue(),computerPath+data.getKey()+"/");
            }
        }
    }
    
    private boolean addFichero(Archivo fichero,String destinyPath) {      
        try {
            Timestamp time = new Timestamp(new java.util.Date().getTime());
            fichero.fechaCreacion = time;
            fichero.fechaModificacion = time;
            String serialized = toString(fichero);
            fichero.tamano = serialized.length()/2;
            if(!addToDisk((Archivo) fichero, serialized)){
                return false;//not enough space
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }

        String delims = "[/]";
        String[] dirs2 = destinyPath.split(delims); //add file to virtual

        Directorio tempDirectory2 = (Directorio) data.get(dirs2[0]);// root
        Directorio temp;
        int i = 1;
        for (; i < dirs2.length - 1; i++) {
            temp = (Directorio) tempDirectory2;
            tempDirectory2 = (Directorio) temp.getData(dirs2[i]);
        }
        tempDirectory2.add(fichero.name, fichero);
        changesCallbackEmit();
        return true;
    }

    public boolean addFichero(String name, Fichero fichero) {        
        if (fichero.getType() == Type.ARCHIVO) {
            try {
                Archivo file = (Archivo) fichero;
                Timestamp time = new Timestamp(new java.util.Date().getTime());
                file.fechaCreacion = time;
                file.fechaModificacion = time;
                String serialized = toString(fichero);
                file.tamano = serialized.length()/2;
                if(!addToDisk((Archivo) fichero, serialized)){
                    return false;//not enough space
                }
                
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        actualDirectory.add(name, fichero);
        changesCallbackEmit();
        return true;
    }

    private boolean addToDisk(Archivo fichero,String serialized) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get("disk.txt"), StandardCharsets.UTF_8);
        if(lines.size()< (sectores - usedSectors.size())){
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
                }
            }
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter("disk.txt", true));
        for(String str: lines) {
            writer.write(str + System.lineSeparator());
          }
        System.out.println("added file "+fichero.name+" in: ");
        System.out.println(fichero.pointers);
        writer.close();
        return true;
    }

    public boolean modifyFichero(String name, Fichero fichero) {        
        /* Return true if succesfully added */
        if (fichero.getType() == Type.ARCHIVO) {
            try {
                Archivo file = (Archivo) fichero;
                Timestamp time = new Timestamp(new java.util.Date().getTime());

                file.fechaModificacion = time;

                String serialized = toString(fichero);
                file.tamano = serialized.length()/2;
                remove(name);//delete from disk
                file.pointers.clear();//delete pointers
                if(!addToDisk((Archivo) fichero, serialized)){ //new pointers
                    return false;//not enough space
                }
                
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        actualDirectory.add(name, fichero);
        changesCallbackEmit();
        return true;
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

    public void remove(String name)  {
        /* Remove a file from virtual disk */
        List<String> lines;
        try {
            lines = Files.readAllLines(Paths.get("disk.txt"), StandardCharsets.UTF_8);
        
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
        } catch (IOException e) {
            e.printStackTrace();
    }
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