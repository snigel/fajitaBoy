package fajitaboy.gb.memory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import fajitaboy.FileIOStreamHelper;

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
    protected int[] ram;

    protected int bank;
    protected int banks;

    /**
     * The offset value is used for subtracting the high incoming addresses to a
     * value starting at zero. This way the class can start using the array from
     * ram[0] instead of the offset value.
     */
    protected int offset;

    public Eram() {
    }

    /**
     * @param start
     *            , address representing where ERAM begins in memory space
     * @param end
     *            , address representing where ERAM ends in memory space These
     *            two values are used for creating the right size of the ERAM
     *            array and for setting the offset value
     */
    public Eram(final int start, final int end, final int banks) {
        this.length = end - start;
        this.banks = banks;
        offset = start; // set offset value for addressing
        reset();
        //System.out.println("ERAM size: " + ram.length + ", banks: " + banks);
    }
    
    public void setBank(int bank) {
        this.bank = bank % banks;
    }

    /**
     * {@inheritDoc}
     */
    public int read(final int address) {
        int addr = address - offset + 0x2000 * bank;
        if (addr < 0 || addr > ram.length) {
            throw new ArrayIndexOutOfBoundsException(String.format(
                    "MemoryComponent: could not read 0x%04x", address));
        }
        return ram[addr];
    }

    /**
     * {@inheritDoc}
     */
    public void write(final int address, final int data) {
        int addr = address - offset + 0x2000 * bank;
        if (addr < 0 || addr > ram.length) {
            throw new ArrayIndexOutOfBoundsException(String.format(
                    "MemoryComponent: could not write 0x%04x", address));
        }
        ram[addr] = data;
    }

    /**
     * {@inheritDoc}
     */
    public void reset() {
        bank = 0;
        ram = new int[length * banks];
    }

    /**
     * {@inheritDoc}
     */
    public int forceRead(final int address) {
        int addr = address - offset + 0x2000 * bank;
        if (addr < 0 || addr > ram.length) {
            throw new ArrayIndexOutOfBoundsException("RamLow.java");
        }
        return ram[addr];
    }

    /**
     * {@inheritDoc}
     */
    public void forceWrite(final int address, final int data) {
        int addr = address - offset + 0x2000 * bank;
        if (addr < 0 || addr > ram.length) {
            throw new ArrayIndexOutOfBoundsException("RamHigh.java");
        }
        ram[addr] = data;
    }

    /**
     * {@inheritDoc}
     */
    public void readState( FileInputStream fis ) throws IOException {
    	super.readState(fis);
    	bank = (int) FileIOStreamHelper.readData( fis, 4 );
    	banks = (int) FileIOStreamHelper.readData( fis, 4 );
    }
    
    /**
     * {@inheritDoc}
     */
    public void saveState( FileOutputStream fos ) throws IOException {
    	super.saveState(fos);
    	FileIOStreamHelper.writeData( fos, (long) bank, 4 );
    	FileIOStreamHelper.writeData( fos, (long) banks, 4 );
    }
}
