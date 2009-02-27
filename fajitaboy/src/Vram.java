/**
 * Represents the graphic ram, also known as VRAM.
 *
 * @author Adam Hulin, Johan Gustafsson
 */
public class Vram implements MemoryInterface {

    /**
     * This array holds the memory space of VRAM.
     */
    private int[] vram;
    /**
     * The offset value is used for subtracting the high incoming addresses to a
     * value starting at zero. This way the class can start using the array from
     * vram[0] instead of the offset value.
     */
    private int offset;

    /**
     * @param start
     *            , address representing where vram begins in memory space
     * @param end
     *            , address representing where vram ends in memory space These
     *            two values are used for creating the right size of the vram
     *            array and for setting the offset value
     */
    Vram(final int start, final int end) {
        vram = new int[end - start]; // sets size of vram
        offset = start; // set offset value for addressing
    }

    @Override
    public final int read(final int address) {
        int addr = address - offset;
        if (addr < 0 || addr > vram.length) {
            throw new ArrayIndexOutOfBoundsException("vram.java");
        }
        return vram[addr];
    }

    @Override
    public final void write(final int address, final int data) {
        int addr = address - offset;
        if (addr < 0 || addr > vram.length) {
            throw new ArrayIndexOutOfBoundsException("vram.java");
        }
        vram[address] = data;
    }
}
