/**
 * Represents the Sprite table part of the memory.
 * @author Adam Hulin, Johan Gustafsson
 *
 */
public class Oam implements MemoryInterface {

    /**
     * This array holds the memory space of OAM.
     */
    private int[] oam;
    /**
     * The offset value is used for subtracting the high incoming addresses to a
     * value starting at zero. This way the class can start using the array from
     * oam[0] instead of the offset value.
     */
    private int offset;

    /**
     * @param start
     *            , address representing where OAM begins in memory space
     * @param end
     *            , address representing where OAM ends in memory space These
     *            two values are used for creating the right size of the OAM
     *            array and for setting the offset value
     */
    Oam(final int start, final int end) {
        oam = new int[end - start]; // sets size of ram
        offset = start; // set offset value for addressing
    }

    /**
     * {@inheritDoc}
     */
    public final int read(final int address) {
        int addr = address - offset;
        if (addr < 0 || addr > oam.length) {
            throw new ArrayIndexOutOfBoundsException("OAM.java");
        }
        return oam[addr];
    }

    /**
     * {@inheritDoc}
     */
    public final void write(final int address, final int data) {
        int addr = address - offset;
        if (addr < 0 || addr > oam.length) {
            throw new ArrayIndexOutOfBoundsException("OAM.java");
        }
        oam[addr] = data;
    }

}
