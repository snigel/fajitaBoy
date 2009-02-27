/**
 * External RAM that is located on the cartridge.
 * @author Adam Hulin, Johan Gustafsson
 *
 */
public class Eram implements MemoryInterface {

    /*
     * TODO check cartridge type to see if hardware is supposed to be here at
     * all
     */

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
     *            , address representing where ERAM begins in memory space
     * @param end
     *            , address representing where ERAM ends in memory space These
     *            two values are used for creating the right size of the ERAM
     *            array and for setting the offset value
     */
    Eram(final int start, final int end) {
        ram = new int[end - start]; // sets size of ram
        offset = start; // set offset value for addressing
    }

    @Override
    public final int read(final int address) {
        int addr = address - offset;
        if (addr < 0 || addr > ram.length) {
            throw new ArrayIndexOutOfBoundsException("Eram.java");
        }
        return ram[addr];
    }

    @Override
    public final void write(final int address, final int data) {
        int addr = address - offset;
        if (addr < 0 || addr > ram.length) {
            throw new ArrayIndexOutOfBoundsException("Eram.java");
        }
        ram[addr] = data;
    }

}
