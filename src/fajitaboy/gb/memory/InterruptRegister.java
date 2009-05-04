package fajitaboy.gb.memory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import fajitaboy.FileIOStreamHelper;
import fajitaboy.gb.StateMachine;

/**
 * Represent the interrupt enable register.
 * @author Adam Hulin, Johan Gustafsson
 */
public class InterruptRegister implements MemoryInterface, StateMachine {
    /**
     * This single variable represents the memory space of the
     * InterruptRegister.
     */
    private int register;

    /**
     * Constructs the InterruptRegister.
     */
    public InterruptRegister() {
        reset();
    }

    /**
     * {@inheritDoc}
     */
    public final int read(final int address) {
        return register;
    }

    /**
     * {@inheritDoc}
     */
    public final int forceRead(final int address) {
        return this.read(address);
    }

    /**
     * {@inheritDoc}
     */
    public final void write(final int address, final int data) {
        register = data;
    }

    /**
     * {@inheritDoc}
     */
    public final void forceWrite(final int address, final int data) {
        this.write(address, data);
    }

    /**
     * {@inheritDoc}
     */
    public final void reset() {
        register = 0;
    }
    
    public void readState( FileInputStream fis ) throws IOException {
    	register = (int) FileIOStreamHelper.readData( fis, 1 );
    }
    
    public void saveState( FileOutputStream fos ) throws IOException {
    	FileIOStreamHelper.writeData( fos, (long) register, 1 );
    }
}
