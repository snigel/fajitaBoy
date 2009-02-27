/**
 * Represent the I/0 part of the memory.
 * @author Adam Hulin, Johan Gustafsson
 *
 */
public class IO extends MemoryComponent {

    /**
     * @param start
     *            , address representing where IO begins in memory space
     * @param end
     *            , address representing where IO ends in memory space These two
     *            values are used for creating the right size of the IO array
     *            and for setting the offset value
     */
    IO(final int start, final int end) {
        super(start, end);
    }

}
