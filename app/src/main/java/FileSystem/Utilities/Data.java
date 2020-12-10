package FileSystem.Utilities;

import java.util.HashMap;
import java.util.Map;

public class Data{
    private HashMap<String, Fichero> data;
    private static Data instance;
    private Directorio actualDirectory;
    private String actualPath;

    public static Data getInstance() { //CREATE
        if (instance==null) {
            instance = new Data();
        }
        return instance;
    }

    private Data() {
        data = new HashMap<>();
    }

    public HashMap<String, Fichero> find(String fichero){
        /*
        Set initial path and directory 
        for making a call to the method search
        */
        HashMap<String, Fichero> result = new HashMap<>();
        Directorio temp = (Directorio) data.get("root"); 
        String path="root";       
        if(fichero=="root"){
            result.put(path,temp);
        }
        search(fichero,path,temp,result);
        return result;//value still remains in result
    }

	private void search(String fichero, String path, Directorio searchMap,HashMap<String, Fichero> result){
        /*
        Iterates over all tree, if there is a coincidence then add the coincidence
        */
        for (Map.Entry<String, Fichero> data : searchMap.getHashMap().entrySet()) {
            //System.out.println(data.getKey() + " = " + data.getValue());
            if(data.getKey()==fichero){
                result.put(path,data.getValue());
            }
            if(data.getValue().getType()==Type.DIRECTORIO){
                Directorio temp = (Directorio) data.getValue();
                search(fichero,path+"/"+data.getValue(),temp,result);
                
            } 
        }
    }

    //change dir
    public Fichero ChangeDirUp() {
        String delims = "[/]";
        String[] dirs = actualPath.split(delims);

        actualDirectory = (Directorio) data.get(dirs[0]);//root
        actualPath=dirs[0];
        Directorio temp;

        for (int i = 1; i < dirs.length-1; i++){
            actualPath=dirs[i];
            temp = (Directorio) actualDirectory;
            actualDirectory = (Directorio) temp.getData(dirs[i]);
        }
        
        return actualDirectory;
    }

    public Fichero ChangeDirDown(String directory) {
        Directorio temp = (Directorio) actualDirectory;
        actualDirectory = (Directorio) temp.getData(directory);
        return actualDirectory;
    }

    public void addFichero(String name,Fichero fichero){
        actualDirectory.add(name,fichero);
    }

    public void exists(String name){
        actualDirectory.contains(name);
    }
}