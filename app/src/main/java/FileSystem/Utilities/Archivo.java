package FileSystem.Utilities;

import java.sql.Timestamp;
import java.util.ArrayList;

public class Archivo extends Fichero {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    
    public String extension;
    public Timestamp fechaCreacion;
    public Timestamp fechaModificacion;
    public int tamano;
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
    public void changeName(String name) {
        int iend = name.indexOf(".");
        this.name = name.substring(0 , iend);
        this.extension = name.substring(iend+1);
    }

    @Override
    public String getName() {
        return super.name + "." + extension;
    }

    @Override
    public String toString() {
        return super.name + "." + extension + " fechaCreacion: "+ fechaCreacion.toString() +
        " fechaModificacion: " + fechaModificacion.toString() + " tamano: "+ Integer.toString(tamano)+ 
        " text: " + text;
    }
    
}
