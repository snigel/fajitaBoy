package fajitaboy.memory;


/**
 * Parentclass for all memory components.
 *
 * @author Adam Hulin, Johan Gustafsson
 *
 */
public class MemoryComponent implements MemoryInterface {

    /**
     * The size of the ram array.
     */
    protected int length;

    /**
     * This array holds the memory space of RAM.
     */
    protected int[] ram;

    /**
     * The offset value is used for subtracting the high incoming addresses to a
     * value starting at zero. This way the class can start using the array from
     * ram[0] instead of the offset value.
     */
    protected int offset;

    /**
     * @param start
     *            , address representing where beginning of the memory space
     * @param end
     *            , address representing where the end in memory space These two
     *            values are used for creating the right size of the
     *            memorycomponent array and for setting the offset value.
     */
    public MemoryComponent(final int start, final int end) {
        this.length = end - start;
        offset = start; // set offset value for addressing
        reset();
    }

    /**
     * Default constuctor.
     */
    public MemoryComponent() {
        //Do nothing
    }

    /**
     * {@inheritDoc}
     */
    public int read(final int address) {
        int addr = address - offset;
        if (addr < 0 || addr > ram.length) {
            throw new ArrayIndexOutOfBoundsException(String.format("MemoryComponent: could not read 0x%04x", address));
        }
        return ram[addr];
    }

    /**
     * {@inheritDoc}
     */
    public void write(final int address, final int data) {
        int addr = address - offset;
        if (addr < 0 || addr > ram.length) {
            throw new ArrayIndexOutOfBoundsException(String.format("MemoryComponent: could not write 0x%04x", address));
        }
        ram[addr] = data;
    }

    /**
     * {@inheritDoc}
     */
    public void reset() {
        ram = new int[length];
    }


    /**
     * {@inheritDoc}
     */
    public int forceRead(final int address) {
        int addr = address - offset;
        if (addr < 0 || addr > ram.length) {
            throw new ArrayIndexOutOfBoundsException("RamLow.java");
        }
        return ram[addr];
    }

    /**
     * {@inheritDoc}
     */
    public void forceWrite(final int address, final int data) {
        int addr = address - offset;
        if (addr < 0 || addr > ram.length) {
            throw new ArrayIndexOutOfBoundsException("RamHigh.java");
        }
        ram[addr] = data;
    }

}