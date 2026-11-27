
package exception;

public class RightEmailWrongPass extends RuntimeException{
    public RightEmailWrongPass() {
    }
    public RightEmailWrongPass(String msg) {
        super(msg);
    }
}
