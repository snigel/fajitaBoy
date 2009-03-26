package fajitaboy.memory;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

public class ROM implements MemoryInterface, MemoryBankInterface {

    /**
     * The size of the ram array.
     */
    protected int length;

    /**
     * This array holds the memory space of RAM.
     */
    protected int[][] ram;

    /**
     * The offset value is used for subtracting the high incoming addresses to a
     * value starting at zero. This way the class can start using the array from
     * ram[0] instead of the offset value.
     */
    protected int offset;
    
    /**
     * Chooses which Rom bank to use. Default is 1.
     */
    private int bank;

    public void setBank(int bank) { //For MBC
        this.bank = bank;
    }

    /**
     * 1. Läs 0-16KB till low rom 2. Läs 16KB -> slutet från cartridge 3. Gör en
     * metod för att returnera.
     */

    public ROM(final int start, final String romPath) {
        this.offset = start;
        readRom(romPath);
    }

    public int forceRead(int address) {
        // TODO Auto-generated method stub
        return 0;
    }

    public void forceWrite(int address, int data) {
        // TODO Auto-generated method stub

    }

    public int read(int address) {
        return ram[address][bank];
    }

    public void reset() {
        // Reset not necessary ignore
    }

    public void write(int address, int data) {
        // writing not allowed. ignore.
    }

    /**
     * This function reads a rom from a file into the ram array.
     * @param romPath
     *            is a text string containing the path to a game boy rom,
     *            located in file system.
     */
    private void readRom(final String romPath) {
        try {
            // Read ROM data from file
            File romFile = new File(romPath);
             int banks = (int) (romFile.length()/0x4000);
            //int banks = 125; // max of MBC 1 :D
            ram = new int[0x4000][banks];

            FileInputStream fis = new FileInputStream(romFile);
            DataInputStream dis = new DataInputStream(fis);
            for (int j = 0; j < banks; j++) {
                for (int i = 0; i < 0x4000; i++) {
                    ram[i][j] = dis.readUnsignedByte();
                }
            }

            fis.close();

        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }
    }

}
