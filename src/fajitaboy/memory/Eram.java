package fajitaboy.memory;

/**
 * External RAM that is located on the cartridge.
 * @author Adam Hulin, Johan Gustafsson
 *
 */
public class Eram extends MemoryComponent {

    /*
     * TODO check cartridge type to see if hardware is supposed to be here at
     * all
     */
    /**
     * @param start
     *            , address representing where ERAM begins in memory space
     * @param end
     *            , address representing where ERAM ends in memory space These
     *            two values are used for creating the right size of the ERAM
     *            array and for setting the offset value
     */
    public Eram(final int start, final int end) {
        super(start, end);
    }

}
