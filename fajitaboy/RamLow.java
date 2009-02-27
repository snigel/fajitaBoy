/**
 * $Rev$ .
 *
 * @author Adam Hulin, Johan Gustafsson RamLow is the lower 4K of the Gameboy
 *         memory It does not have any different banks as the RamHigh may have
 *         in GBC-mode
 *
 */
public class RamLow implements MemoryInterface {
    /**
     * This array holds the memory space of RAM.
     */
    private int[] ram;

    /**
     * The offset value is used for subtracting the high incoming addresses to a
     * value starting at zero. This way the class can start using the array from
     * ram[0] instead of the offset value.
     */
    private int offset;

    /**
     * @param start
     *            , address representing where RamLow begins in memory space
     * @param end
     *            , address representing where RamLow ends in memory space These
     *            two values are used for creating the right size of the RamHigh
     *            array and for setting the offset value
     */
    RamLow(final int start, final int end) {
        ram = new int[end - start]; // sets size of ram
        offset = start; // set offset value for addressing
    }

    /**
     * {@inheritDoc}
     */
    public final int read(final int address) {
        int addr = address - offset;
        if (addr < 0 || addr > ram.length) {
            throw new ArrayIndexOutOfBoundsException("RamLow.java");
        }
        return ram[addr];
    }

    /**
     * {@inheritDoc}
     */
    public final void write(final int address, final int data) {
        int addr = address - offset;
        if (addr < 0 || addr > ram.length) {
            throw new ArrayIndexOutOfBoundsException("RamLow.java");
        }
        ram[addr] = data;
    }

}
