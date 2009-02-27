/*
 * RamHigh is the upper 4K of the Gameboy memory
 * It may have different banks if it's designed
 * for GBC-compatibility.
 *
 */
/**
 * RamHigh is the upper 4K of the Gameboy memory.
 * It may have different banks if it's designed
 * for GBC-compatibility.
 * @author Adam Hulin, Johan Gustafsson
 *
 */
public class RamHigh implements MemoryInterface {

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
     *            , address representing where RamHigh begins in memory space
     * @param end
     *            , address representing where RamHigh ends in memory space
     *            These two values are used for creating the right size of the
     *            RamHigh array and for setting the offset value
     */
    RamHigh(final int start, final int end) {
        ram = new int[end - start]; // sets size of ram
        offset = start; // set offset value for addressing
    }

    @Override
    public final int read(final int address) {
        int addr = address - offset;
        if (addr < 0 || addr > ram.length) {
            throw new ArrayIndexOutOfBoundsException("RamHigh.java");
        }
        return ram[addr];
    }

    @Override
    public final void write(final int address, final int data) {
        int addr = address - offset;
        if (addr < 0 || addr > ram.length) {
            throw new ArrayIndexOutOfBoundsException("RamHigh.java");
        }
        ram[addr] = data;
    }

}
