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

    @Override
    public final int read(final int address) {
        return register;
    }

    @Override
    public final void write(final int address, final int data) {
        register = data;
    }

}
