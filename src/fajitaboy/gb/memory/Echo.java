package fajitaboy.gb.memory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import fajitaboy.FileIOStreamHelper;
import fajitaboy.gb.StateMachine;

/**
 * Reroutes echo references to another address. Needs echo address, original
 * address and object references to pass on to.
 *
 * @author Adam Hulin, Johan Gustafsson
 *
 */
public class Echo implements MemoryInterface, StateMachine {
    /**
     * The difference between the low ram and echo addresses.
     */
    private int diff;
    /**
     * An memoryinterface object.
     */
    private MemoryInterface mem;

    /**
     * Creates a replica of the content from the memoryinterface object.
     * to the specified location.
     * @param object
     *          An MemoryInterface object.
     * @param objAddr
     *          The start address of the MemoryInterface object.
     * @param echoAddr
     *          The start address of the echo memory.
     */
    public Echo(final MemoryInterface object, final int objAddr,
            final int echoAddr) {
        diff = echoAddr - objAddr;
        mem = object;
    }

    /**
     * {@inheritDoc}
     */
    public final int read(final int address) {
        return mem.read(address - diff);
    }

    /**
     * {@inheritDoc}
     */
    public final int forceRead(final int address) {
        return mem.forceRead(address - diff);
    }

    /**
     * {@inheritDoc}
     */
    public final void write(final int address, final int data) {
        mem.write(address - diff, data);
    }

    /**
     * {@inheritDoc}
     */
    public final void forceWrite(final int address, final int data) {
        mem.forceWrite(address - diff, data);
    }

    /**
     * {@inheritDoc}
     */
    public final void reset() {
        //mem.reset();
        //this should not need to be used. Use reset on the real object.
    }
    
    public void readState( FileInputStream fis ) throws IOException {
    	diff = (int)FileIOStreamHelper.readData( fis, 4 );
    }
    
    public void saveState( FileOutputStream fos ) throws IOException {
    	FileIOStreamHelper.writeData( fos, (long)diff, 4 );
    }
}
