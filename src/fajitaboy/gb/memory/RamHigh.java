package fajitaboy.gb.memory;


/**
 * RamHigh is the upper 4K of the Gameboy memory.
 * It may have different banks if it's designed
 * for GBC-compatibility.
 * @author Adam Hulin, Johan Gustafsson
 *
 */
public class RamHigh extends MemoryComponent {

    /**
     * @param start
     *            , address representing where RamHigh begins in memory space
     * @param end
     *            , address representing where RamHigh ends in memory space
     *            These two values are used for creating the right size of the
     *            RamHigh array and for setting the offset value
     */
    public RamHigh(final int start, final int end) {
        super(start, end);
    }

}
