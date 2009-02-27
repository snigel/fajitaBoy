package fajitaboy;

/**
 * $Rev$ .
 *
 * @author Adam Hulin, Johan Gustafsson RamLow is the lower 4K of the Gameboy
 *         memory It does not have any different banks as the RamHigh may have
 *         in GBC-mode
 *
 */
public class RamLow extends MemoryComponent {

    /**
     * @param start
     *            , address representing where RamLow begins in memory space
     * @param end
     *            , address representing where RamLow ends in memory space These
     *            two values are used for creating the right size of the RamHigh
     *            array and for setting the offset value
     */
    RamLow(final int start, final int end) {
        super(start, end);
    }
    
}
