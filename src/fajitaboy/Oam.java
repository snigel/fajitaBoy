package fajitaboy;

/**
 * Represents the Sprite table part of the memory.
 * @author Adam Hulin, Johan Gustafsson
 *
 */
public class Oam extends MemoryComponent {

    /**
     * @param start
     *            , address representing where OAM begins in memory space
     * @param end
     *            , address representing where OAM ends in memory space These
     *            two values are used for creating the right size of the OAM
     *            array and for setting the offset value
     */
    public Oam(final int start, final int end) {
        super(start, end);
    }
}
