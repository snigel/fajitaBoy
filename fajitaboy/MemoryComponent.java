import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

/**
 * Parentclass for all memory components.
 *
 * @author Adam Hulin, Johan Gustafsson
 *
 */
public class MemoryComponent implements MemoryInterface, ForceMemory {

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
    MemoryComponent(final int start, final int end) {
        ram = new int[end - start]; // sets size of ram
        offset = start; // set offset value for addressing
    }

    MemoryComponent(final int start, final String romPath) {
        this.offset = start;
        readRom(romPath);
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
    public void write(final int address, final int data) {
        int addr = address - offset;
        if (addr < 0 || addr > ram.length) {
            throw new ArrayIndexOutOfBoundsException("RamHigh.java");
        }
        ram[addr] = data;
    }

    /**
     * This function reads a rom from a file into the ram array.
     *
     * @param romPath
     *            is a text string containing the path to a gameboy rom, located
     *            in file system.
     */
    private void readRom(final String romPath) {
        try {

            File romFile = new File(romPath);
            ram = new int[(int) romFile.length()];
            FileInputStream fis = new FileInputStream(romFile);
            DataInputStream dis = new DataInputStream(fis);

            for (int i = 0; i < ram.length; i++) {
                ram[i] = dis.readUnsignedByte();
            }

            fis.close();
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public int forceRead(int address) {
        int addr = address - offset;
        if (addr < 0 || addr > ram.length) {
            throw new ArrayIndexOutOfBoundsException("RamLow.java");
        }
        return ram[addr];
    }

    /**
     * {@inheritDoc}
     */
    public void forceWrite(int address, int data) {
        int addr = address - offset;
        if (addr < 0 || addr > ram.length) {
            throw new ArrayIndexOutOfBoundsException("RamHigh.java");
        }
        ram[addr] = data;
    }

}
