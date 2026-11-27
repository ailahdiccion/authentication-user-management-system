
package exception;

public class NullValueException extends RuntimeException{

    public NullValueException() {
    }
    public NullValueException(String msg) {
        super(msg);
    }
}
