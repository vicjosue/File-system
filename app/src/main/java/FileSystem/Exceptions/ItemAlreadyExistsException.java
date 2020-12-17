package FileSystem.Exceptions;

public class ItemAlreadyExistsException extends Exception {
    public ItemAlreadyExistsException() {
        
    }
    public ItemAlreadyExistsException(String message) {
        super(message);
    }
}