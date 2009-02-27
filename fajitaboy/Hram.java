/**
 * Represents the High RAM part of the memory.
 *
 * @author Adam Hulin, Johan Gustafsson
 *
 */
public class Hram implements MemoryInterface {

    /**
     * This array holds the memory space of HRAM.
     */
    private int[] hram;
    /**
     * The offset value is used for subtracting the high incoming addresses to a
     * value starting at zero. This way the class can start using the array from
     * ram[0] instead of the offset value.
     */
    private int offset;

    /**
     * @param start
     *            , address representing where HRAM begins in memory space
     * @param end
     *            , address representing where HRAM ends in memory space These
     *            two values are used for creating the right size of the HRAM
     *            array and for setting the offset value
     */
    Hram(final int start, final int end) {
        hram = new int[end - start]; // sets size of hram
        offset = start; // set offset value for addressing
    }

    /**
     * {@inheritDoc}
     */
    public final int read(final int address) {
        int addr = address - offset;
        if (addr < 0 || addr > hram.length) {
            throw new ArrayIndexOutOfBoundsException("hram.java");
        }
        return hram[addr];
    }

    /**
     * {@inheritDoc}
     */
    public final void write(final int address, final int data) {
        int addr = address - offset;
        if (addr < 0 || addr > hram.length) {
            throw new ArrayIndexOutOfBoundsException("hram.java");
        }
        hram[addr] = data;
    }
}
