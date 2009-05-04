package fajitaboy.gb.memory;


/**
 * This class is for debugging purposes only. So that the memory returns zero
 * instead of null pointer.
 * @author Adam Hulin, Johan Gustafsson
 */
public class DebugMemory implements MemoryInterface {

    /**
     * {@inheritDoc}
     */
    public final int read(final int address) {
        return 0;
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
        // This one does nothing. Data should always be zero.
    }

    /**
     * {@inheritDoc}
     */
    public final void forceWrite(final int address, final int data) {
        // same as write(), thus doing nothing;
    }

    /**
     * {@inheritDoc}
     */
    public void reset() {
        // Do nothing since we don't have any data structures

    }

}
