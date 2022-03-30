public class TokenException extends RuntimeException {
    public TokenException(int offset, char unexpected) {
        this("Unexpected char " + unexpected + " at offset " + offset);
    }

    public TokenException(String message) {
        super(message);
    }
}
