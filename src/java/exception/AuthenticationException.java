
package exception;


public class AuthenticationException extends RuntimeException{
    public AuthenticationException() {
    }
    public AuthenticationException(String msg) {
        super(msg);
    }
}
