package FileSystem.Utilities;

enum Type {
    DIRECTORIO,
    ARCHIVO
}

public abstract class Fichero implements java.io.Serializable{
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    
    Type tipo;
    String name;

    public Fichero(String name) {
        this.name = name;
    }
    
    public void setType(Type tipo) {
        this.tipo = tipo; 
    }
    
    public Type getType() {
        return this.tipo;
    }

    public String getName() {
        return this.name;
    }

}