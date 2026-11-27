
package exception;

public class InvalidSession extends RuntimeException{

    public InvalidSession() {
    }

    
    public InvalidSession(String msg) {
        super(msg);
    }
}
