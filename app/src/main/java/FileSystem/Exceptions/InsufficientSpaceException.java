package FileSystem.Exceptions;

public class InsufficientSpaceException extends Exception {
    public InsufficientSpaceException() {
        
    }
    public InsufficientSpaceException(String message) {
        super(message);
    }
}
