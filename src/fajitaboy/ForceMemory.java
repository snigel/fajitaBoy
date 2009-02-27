package fajitaboy;

/**
 * Interface for Previlegie read and writes to memory.
 * That ignores all read and writes rules.
 * @author Adam Hulin, Johan Gustafsson
 *
 */
public interface ForceMemory {

    /**
     * Performs a previlegie read from the specified address.
     * @param address
     *            The address that we aim to read from
     * @return int This is supposed to return the memory contents located at
     *         address.
     */
    public int forceRead(int address);

    /**
     * Performs a previlegie read from the specified address.
     * @param address
     *            The memory address we want to write to
     * @param data
     *            The data we want to write to the specified address
     */
    public void forceWrite(int address, int data);

}
