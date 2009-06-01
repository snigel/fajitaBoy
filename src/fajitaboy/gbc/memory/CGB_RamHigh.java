package fajitaboy.gbc.memory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import fajitaboy.FileIOStreamHelper;
import fajitaboy.gb.memory.RamHigh;

/**
 * RamHigh (WRAM) memory in CGB mode.
 * Switchable banks 1-7.
 */
public class CGB_RamHigh extends RamHigh {

    /**
     * Current bank used.
     */
    private int bank;
    
    /**
     * {@inheritDoc}
     */
    public CGB_RamHigh(final int start, final int end) {
        super(start, end);
        ram = new int[8 * length];
        bank = 1;
    }
    
    public void reset() {
    	ram = new int[8 * length];
    }
    
    /**
     * Changes the bank.
     */
    public void setBank(int newBank) {
        if(newBank == 0) {
            bank = 1;
        } else {
            bank = newBank;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public int read(final int address) {
        int addr = address - offset;
        if (addr < 0 || addr > ram.length) {
            throw new ArrayIndexOutOfBoundsException(String.format("MemoryComponent: could not read 0x%04x", address));
        }
        return ram[addr + (length * (bank-1))];
    }

    /**
     * {@inheritDoc}
     */
    public void write(final int address, final int data) {
        int addr = address - offset;
        if (addr < 0 || addr > ram.length) {
            throw new ArrayIndexOutOfBoundsException(String.format("MemoryComponent: could not write 0x%04x", address));
        }
        ram[addr + (length * (bank-1))] = data;
    }
    
    /**
     * {@inheritDoc}
     */
    public void readState( FileInputStream fis ) throws IOException {
    	length = (int) FileIOStreamHelper.readData( fis, 4 );
    	offset = (int) FileIOStreamHelper.readData( fis, 4 );
    	bank = (int) FileIOStreamHelper.readData( fis, 1 );
    	ram = new int[length*8];
    	for ( int i = 0; i < length*8; i++ ) {
    		ram[i] = (int) FileIOStreamHelper.readData( fis, 1 );
    	}
    }
    
    /**
     * {@inheritDoc}
     */
    public void saveState( FileOutputStream fos ) throws IOException {
    	FileIOStreamHelper.writeData( fos, (long) length, 4 );
    	FileIOStreamHelper.writeData( fos, (long) offset, 4 );
    	FileIOStreamHelper.writeData( fos, (long) bank, 1 );
    	for ( int i = 0; i < length*8; i++ ) {
    		FileIOStreamHelper.writeData( fos, ram[i], 1 );
    	}
    }

}
