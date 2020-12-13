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
    public String text = "";
    ArrayList<Integer> pointers = new ArrayList<>();

    public Archivo(String name, String extension) {
        super(name);
        this.extension = extension;
    }

    public Archivo(String name, String extension, String text) {
        super(name);
        this.extension = extension;
        this.text = text;
    }

    @Override
    public String getName() {
        return super.name + "." + extension;
    }
    
}
