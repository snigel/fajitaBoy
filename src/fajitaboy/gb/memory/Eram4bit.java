package fajitaboy.gb.memory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import fajitaboy.FileIOStreamHelper;

/**
 * External RAM that is located on the cartridge.
 * @author Adam Hulin, Johan Gustafsson
 */
public class Eram4bit extends Eram {

    public Eram4bit() {
    }

    /**
     * @param start
     *            , address representing where ERAM begins in memory space
     * @param end
     *            , address representing where ERAM ends in memory space These
     *            two values are used for creating the right size of the ERAM
     *            array and for setting the offset value
     */
    public Eram4bit(final int start, final int end, final int banks) {
        this.length = 512;
        offset = start; // set offset value for addressing
        reset();
        System.out.println("4bit ERAM, size: " + ram.length + ", banks: " + banks);
    }

    /**
     * {@inheritDoc}
     */
    public int read(final int address) {
        int addr = (address - offset);
        if (addr < 0 || addr > ram.length) {
            throw new ArrayIndexOutOfBoundsException(String.format(
                    "MemoryComponent: could not read 0x%04x", address));
        }
        return (ram[addr] & 0x0F);
    }

    /**
     * {@inheritDoc}
     */
    public void write(final int address, final int data) {
        int addr = (address - offset);
        if (addr < 0 || addr > ram.length) {
            throw new ArrayIndexOutOfBoundsException(String.format(
                    "MemoryComponent: could not write 0x%04x", address));
        }
        ram[addr] = (data & 0x0F);
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
        return read(address);
    }

    /**
     * {@inheritDoc}
     */
    public void forceWrite(final int address, final int data) {
        write(address, data);
    }

    /**
     * {@inheritDoc}
     */
    public void readState( FileInputStream fis ) throws IOException {
    	super.readState(fis);
    }
    
    /**
     * {@inheritDoc}
     */
    public void saveState( FileOutputStream fos ) throws IOException {
    	super.saveState(fos);
    }
}
