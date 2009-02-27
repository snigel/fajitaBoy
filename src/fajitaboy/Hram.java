package fajitaboy;

/**
 * Represents the High RAM part of the memory.
 *
 * @author Adam Hulin, Johan Gustafsson
 *
 */
public class Hram extends MemoryComponent {

    /**
     * @param start
     *            , address representing where HRAM begins in memory space
     * @param end
     *            , address representing where HRAM ends in memory space These
     *            two values are used for creating the right size of the HRAM
     *            array and for setting the offset value
     */
    Hram(final int start, final int end) {
        super(start, end);
    }
}
