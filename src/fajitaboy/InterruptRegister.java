package fajitaboy;

/**
 * Represent the interrupt enable register.
 *
 * @author Adam Hulin, Johan Gustafsson
 *
 */
public class InterruptRegister implements MemoryInterface {
    /**
     * This single variable represents the memory space of
     * the InterruptRegister.
     */
    private int register;

    /**
     * Constructs the InterruptRegister.
     */
    InterruptRegister() {
        register = 0;
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
    public final void write(final int address, final int data) {
        register = data;
    }

}