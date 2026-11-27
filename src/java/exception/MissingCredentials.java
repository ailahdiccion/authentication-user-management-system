package exception;

public class MissingCredentials extends RuntimeException {
    public MissingCredentials() {}
    public MissingCredentials(String msg) {
        super(msg);
    }
}