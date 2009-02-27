/**
 * Reroutes echo references to another address. Needs echo address, original
 * address and object references to pass on to.
 *
 * @author Adam Hulin, Johan Gustafsson
 *
 */
public class Echo implements MemoryInterface {
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

    @Override
    public final int read(final int address) {
        return mem.read(address - diff);
    }

    @Override
    public final void write(final int address, final int data) {
        mem.write(address - diff, data);
    }

}
