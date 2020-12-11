package FileSystem.Utilities;

import java.security.Timestamp;

public class Archivo extends Fichero {
    String extension;
    Timestamp fechaCreacion;
    Timestamp fechaModificacion;
    int tamano;
    String text;
    //lista(punteros)

    public Archivo(String name) {
        super(name);
    }
}
