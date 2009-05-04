package fajitaboy.gb.memory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import fajitaboy.FileIOStreamHelper;

/**
 * External RAM that is located on the cartridge.
 * @author Adam Hulin, Johan Gustafsson
 */
public class NoEram extends MemoryComponent {

    
    public void setBank(int bank) {
    }

    /**
     * {@inheritDoc}
     */
    public int read(final int address) {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public void write(final int address, final int data) {
    }

    /**
     * {@inheritDoc}
     */
    public void reset() {
    }

    /**
     * {@inheritDoc}
     */
    public int forceRead(final int address) {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public void forceWrite(final int address, final int data) {
    }

    /**
     * {@inheritDoc}
     */
    public void readState( FileInputStream fis ) throws IOException {
    }
    
    /**
     * {@inheritDoc}
     */
    public void saveState( FileOutputStream fos ) throws IOException {
    }
}
