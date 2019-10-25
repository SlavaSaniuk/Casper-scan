package by.bsac.core.exceptions;

/**
 * Main exception class that throws if user illegal use this library.
 */
public class CasperException extends RuntimeException {

    public CasperException(String msg) {
        super(msg);
    }
}
