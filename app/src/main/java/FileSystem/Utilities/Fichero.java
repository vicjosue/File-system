package FileSystem.Utilities;

public abstract class Fichero implements java.io.Serializable{
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    
    public String name;

    public Fichero(String name) {
        this.name = name;
    }
    

    public String getName() {
        return this.name;
    }

}