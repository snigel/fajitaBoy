import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

/**
 * Represents the memory which the cartridges ROM is saved.
 *
 * @author Adam Hulin, Johan Gustafsson
 *
 */
public class Cartridge implements MemoryInterface {
    /**
     * This array holds the memory space of the cartridge.
     */
    private int[] cartridge;
    /**
     * The offset value is used for subtracting the high incoming addresses to a
     * value starting at zero. This way the class can start using the array from
     * cartridge[0] instead of the offset value.
     */
    private int offset;

    /**
     * @param start
     *            , address representing where vram begins in memory space
     * @param romPath
     *            , is the location of a gameboy rom located in file system
     */
    Cartridge(final int start, final String romPath) {
        this.offset = start;
        readRom(romPath);
    }

    @Override
    public final int read(final int address) {
        int addr = address - offset;
        if (addr < 0 || addr > cartridge.length) {
            throw new ArrayIndexOutOfBoundsException("Cartridge.java");
        }
        return cartridge[addr];
    }

    @Override
    public final void write(final int address, final int data) {
        throw new RomWriteException("Cartridge.java, address:" + address);
    }

    /**
     * This function reads a rom from a file into the cartridge array.
     *
     * @param romPath
     *            is a text string containing the path to a gameboy rom, located
     *            in file system.
     */
    private void readRom(final String romPath) {
        try {

            File romFile = new File(romPath);
            cartridge = new int[(int) romFile.length()];
            FileInputStream fis = new FileInputStream(romFile);
            DataInputStream dis = new DataInputStream(fis);

            for (int i = 0; i < cartridge.length; i++) {
                cartridge[i] = dis.readUnsignedByte();
            }

            fis.close();
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }
    }

}
/*
 * Some code for reading out title or type testread(0x134, 16,
 * "Cartridge title"); testread(0x148, 1, "Cartridge type");
 *
 * }
 *
 * private void testread(int start, int length, String type){ String output="";
 * for(int i=0; i<length; i++){ output=output+addressBus.read(i+start); }
 * System.out.println(type+": "+output); }
 */
