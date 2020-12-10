package FileSystem.Utilities;

enum Type {
    DIRECTORIO,
    ARCHIVO
}

public abstract class Fichero{
    Type tipo;
    
    public void setType(Type tipo) {
        this.tipo = tipo; 
    }
    
    public Type getType() {
        return this.tipo;
    }

}