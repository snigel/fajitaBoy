package fajitaboy;

/**
 * Exception for then memory is accessed out of bounds.
 * @author Adam Hulin, Johan Gustafsson
 *
 */
public class RomWriteException extends RuntimeException {

    /**
     * This class generates an exception and is used for representing when a
     * write is done against the read only memory.
     */
    private static final long serialVersionUID = 3306222239365615100L;

    /**
     * @param message
     *            is supposed to contain at least some information of what class
     *            and perhaps which method that is throwing the error.
     */
    RomWriteException(final String message) {
        super(message);
    }
}
