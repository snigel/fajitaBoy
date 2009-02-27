/**
 * Represent the I/0 part of the memory.
 * @author Adam Hulin, Johan Gustafsson
 *
 */
public class IO implements MemoryInterface {

    /**
     * This array holds the memory space of IO.
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
     *            , address representing where IO begins in memory space
     * @param end
     *            , address representing where IO ends in memory space These two
     *            values are used for creating the right size of the IO array
     *            and for setting the offset value
     */
    IO(final int start, final int end) {
        ram = new int[end - start]; // sets size of ram
        offset = start; // set offset value for addressing
    }

    @Override
    public final int read(final int address) {
        int addr = address - offset;
        if (addr < 0 || addr > ram.length) {
            throw new ArrayIndexOutOfBoundsException("IO.java");
        }
        return ram[addr];
    }

    @Override
    public final void write(final int address, final int data) {
        int addr = address - offset;
        if (addr < 0 || addr > ram.length) {
            throw new ArrayIndexOutOfBoundsException("IO.java");
        }
        ram[addr] = data;

    }

}
