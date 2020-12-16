package FileSystem.Exceptions;

public class ItemAlreadyExists extends Exception {
    public ItemAlreadyExists() {
        
    }
    public ItemAlreadyExists(String message) {
        super(message);
    }
}