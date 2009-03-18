package fajitaboy;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

/**
 * Represents the memory which the cartridges ROM is saved.
 *
 * @author Adam Hulin, Johan Gustafsson
 *
 */
public class Cartridge extends MemoryComponent {
    /**
     * @param start
     *            , address representing where vram begins in memory space
     * @param romPath
     *            , is the location of a gameboy rom located in file system
     */
    public Cartridge(final int start, final String romPath) {
        this.offset = start;
        readRom(romPath);
    }

    /**
     * {@inheritDoc}
     */
    public final void write(final int address, final int data) {
        System.out.println("\n Write to cartridge at: "
                + Integer.toHexString(address));
    }

    /**
     * {@inheritDoc}
     */
    public final void reset() {
        //reloading the cartridge is not needed
        //reset MBC if it will be implemented
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

}
