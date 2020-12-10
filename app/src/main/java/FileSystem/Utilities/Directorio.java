package FileSystem.Utilities;

import java.util.HashMap;

public class Directorio extends Fichero {
    private HashMap<String, Fichero> hashMap;

    public Directorio(){
        hashMap = new HashMap<>();
    }

    public HashMap<String, Fichero> getHashMap() {
        return hashMap;
    }
    public Fichero getData(String fichero) {
        return hashMap.get(fichero);
    }
    public boolean contains(String name){
        /* 
        Return false if is already defined
        Returns true if is added succesfully
        */
        return hashMap.containsKey(name);
    }
    public void add(String name, Fichero fichero) {
        hashMap.put(name,fichero);
	}

	public void delete(String name) {
        hashMap.remove(name);
	}
    
}