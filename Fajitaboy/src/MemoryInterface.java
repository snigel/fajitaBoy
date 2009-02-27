/**
 * @author Adam Hulin, Johan Gustafsson This interface handles read and write
 *         commands and are supposed to work for all memory like implementations
 *         used.
 */
public interface MemoryInterface {

    /**
     * @param address
     *            The address that we aim to read from
     * @return int This is supposed to return the memory contents located at
     *         address.
     */
    int read(int address);

    /**
     * @param address
     *            The memory address we want to write to
     * @param data
     *            The data we want to write to the specified address
     */
    void write(int address, int data);
}
