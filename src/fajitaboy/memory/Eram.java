package fajitaboy.memory;

/**
 * External RAM that is located on the cartridge.
 * @author Adam Hulin, Johan Gustafsson
 */
public class Eram extends MemoryComponent {

    /**
     * The size of the ram array.
     */
    protected int length;

    /**
     * This array holds the memory space of RAM.
     */
    protected int[][] ram;

    private int bank;

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

    /**
     * @param start
     *            , address representing where ERAM begins in memory space
     * @param end
     *            , address representing where ERAM ends in memory space These
     *            two values are used for creating the right size of the ERAM
     *            array and for setting the offset value
     */
    public Eram(final int start, final int end) {
        this.length = end - start;
        offset = start; // set offset value for addressing
        reset();
    }

    public void setBank(int bank) {
        this.bank = bank;
    }

    /**
     * {@inheritDoc}
     */
    public int read(final int address) {
        int addr = address - offset;
        if (addr < 0 || addr > ram.length) {
            throw new ArrayIndexOutOfBoundsException(String.format(
                    "MemoryComponent: could not read 0x%04x", address));
        }
        return ram[addr][bank];
    }

    /**
     * {@inheritDoc}
     */
    public void write(final int address, final int data) {
        int addr = address - offset;
        if (addr < 0 || addr > ram.length) {
            throw new ArrayIndexOutOfBoundsException(String.format(
                    "MemoryComponent: could not write 0x%04x", address));
        }
        ram[addr][bank] = data;
    }

    /**
     * {@inheritDoc}
     */
    public void reset() {
        bank = 0;
        ram = new int[length][4]; // 4 is max number.
    }

    /**
     * {@inheritDoc}
     */
    public int forceRead(final int address) {
        int addr = address - offset;
        if (addr < 0 || addr > ram.length) {
            throw new ArrayIndexOutOfBoundsException("RamLow.java");
        }
        return ram[addr][bank];
    }

    /**
     * {@inheritDoc}
     */
    public void forceWrite(final int address, final int data) {
        int addr = address - offset;
        if (addr < 0 || addr > ram.length) {
            throw new ArrayIndexOutOfBoundsException("RamHigh.java");
        }
        ram[addr][bank] = data;
    }

}