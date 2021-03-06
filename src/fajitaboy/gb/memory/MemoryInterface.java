package fajitaboy.gb.memory;

import fajitaboy.gb.StateMachine;

/**
 * @author Adam Hulin, Johan Gustafsson This interface handles read and write
 *         commands and are supposed to work for all memory like implementations
 *         used.
 */
public interface MemoryInterface extends StateMachine {

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

    /**
     * Performs a previlegie read from the specified address.
     * @param address
     *            The address that we aim to read from
     * @return int This is supposed to return the memory contents located at
     *         address.
     */
    int forceRead(final int address);

    /**
     * Performs a previlegie read from the specified address.
     * @param address
     *            The memory address we want to write to
     * @param data
     *            The data we want to write to the specified address
     */
    void forceWrite(int address, int data);

    /**
     * Resets memory and initializes any values if required.
     */
    void reset();
}
