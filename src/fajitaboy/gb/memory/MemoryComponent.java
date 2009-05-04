package fajitaboy.gb.memory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import fajitaboy.FileIOStreamHelper;
import fajitaboy.gb.StateMachine;


/**
 * Parentclass for all memory components.
 *
 * @author Adam Hulin, Johan Gustafsson
 *
 */
public class MemoryComponent implements MemoryInterface, StateMachine {

    /**
     * The size of the ram array.
     */
    protected int length;

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
    public MemoryComponent(final int start, final int end) {
        this.length = end - start;
        offset = start; // set offset value for addressing
        reset();
    }

    /**
     * Default constuctor.
     */
    public MemoryComponent() {
        //Do nothing
    }

    /**
     * {@inheritDoc}
     */
    public int read(final int address) {
        int addr = address - offset;
        if (addr < 0 || addr > ram.length) {
            throw new ArrayIndexOutOfBoundsException(String.format("MemoryComponent: could not read 0x%04x", address));
        }
        return ram[addr];
    }

    /**
     * {@inheritDoc}
     */
    public void write(final int address, final int data) {
        int addr = address - offset;
        if (addr < 0 || addr > ram.length) {
            throw new ArrayIndexOutOfBoundsException(String.format("MemoryComponent: could not write 0x%04x", address));
        }
        ram[addr] = data;
    }

    /**
     * {@inheritDoc}
     */
    public void reset() {
        ram = new int[length];
    }


    /**
     * {@inheritDoc}
     */
    public int forceRead(final int address) {
        int addr = address - offset;
        if (addr < 0 || addr > ram.length) {
            throw new ArrayIndexOutOfBoundsException("RamLow.java");
        }
        return ram[addr];
    }

    /**
     * {@inheritDoc}
     */
    public void forceWrite(final int address, final int data) {
        int addr = address - offset;
        if (addr < 0 || addr > ram.length) {
            throw new ArrayIndexOutOfBoundsException("RamHigh.java");
        }
        ram[addr] = data;
    }
    
    /**
     * {@inheritDoc}
     */
    public void readState( FileInputStream fis ) throws IOException {
    	length = (int) FileIOStreamHelper.readData( fis, 4 );
    	offset = (int) FileIOStreamHelper.readData( fis, 4 );
    	ram = new int[length];
    	for ( int i = 0; i < length; i++ ) {
    		ram[i] = (int) FileIOStreamHelper.readData( fis, 1 );
    	}
    }
    
    /**
     * {@inheritDoc}
     */
    public void saveState( FileOutputStream fos ) throws IOException {
    	FileIOStreamHelper.writeData( fos, (long) length, 4 );
    	FileIOStreamHelper.writeData( fos, (long) offset, 4 );
    	for ( int i = 0; i < length; i++ ) {
    		FileIOStreamHelper.writeData( fos, ram[i], 1 );
    	}
    }

}
