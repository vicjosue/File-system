package FileSystem.Utilities;

import java.sql.Timestamp;
import java.util.ArrayList;

public class Archivo extends Fichero {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    
    String extension;
    Timestamp fechaCreacion;
    Timestamp fechaModificacion;
    int tamano;
    String text = "";
    ArrayList<Integer> pointers = new ArrayList<>();

    public Archivo(String name) {
        super(name);
    }

    public Archivo(String name, String text) {
        super(name);
        this.text = text;
    }

    @Override
    public String getName() {
        return super.name + "." + extension;
    }
    
}
