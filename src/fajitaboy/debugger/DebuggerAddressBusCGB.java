package fajitaboy.debugger;

import java.util.LinkedList;
import java.util.List;

import fajitaboy.gbc.memory.CGB_AddressBus;

/**
 * DebuggerMemoryInterface works like a wrapper around an memory interface, but it
 * allows breakpoints to be added on address spaces. When someone calls read or
 * write on an address corresponding to a breakpoint, an MemoryAction is logged.
 * @author arvid
 *
 */
public class DebuggerAddressBusCGB extends CGB_AddressBus implements DebuggerMemoryInterface {
    
    /**
     * A list of memory breakpoints.
     */
    private List<MemoryBreakpoint> mBreakPoints;

    /**
     * A list of memory actions that have been logged.
     */
    private List<MemoryAction> dirtyActions;

    /**
     * Create an DebuggerMemoryInterface using the argument as underlying
     * MemoryInterface.
     * @param wmi underlying memory interface.
     */
    public DebuggerAddressBusCGB(final int[] cartridge) {
        super(cartridge);
        
        mBreakPoints = new LinkedList<MemoryBreakpoint>();
        dirtyActions = new LinkedList<MemoryAction>();
    }

    /**
     * Adds a breakpoint.
     * @param mb breakpoint to add.
     */
    public final void addBreakpoint(final MemoryBreakpoint mb) {
        mBreakPoints.add(mb);
    }

    /**
     * Removese the breakpoint with given index.
     * @param index index of breakpoint to remove.
     */
    public final void removeBreakpoint(final int index) {
        mBreakPoints.remove(index); // will throw if faulty argument.
    }

    /**
     * @return the current list of memory breakpoints.
     */
    public final List<MemoryBreakpoint> getMBreakPoints() {
        return mBreakPoints;
    }

    /**
     * Returns the list of MemoryActions that have been logged.
     * @return list of MemoryActions that have been logged.
     */
    public final List<MemoryAction> getDirtyActions() {
        return dirtyActions;
    }

    /**
     * Clear all logged MemoryActions.
     */
    public final void clearDirtyActions() {
        dirtyActions.clear();
    }

    /**
     * Returns true if there is a breakpoint containing the specified address,
     * with a compatible ActionType.
     * @param address Address which to see if breaked.
     * @param at ActionType to look for.
     * @return if that address is breaked.
     */
    private boolean isBreaked(final int address, final ActionType at) {
        for (MemoryBreakpoint b : mBreakPoints) {
            if (address >= b.getLower() && address <= b.getUpper()
                    && b.getOnAction().compatible(at)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Logs a MemoryAction, if the specified address is breaked with the
     * specified ActionType.
     * @param address Address to log.
     * @param at Actiontype to log.
     * @param value Read or written value to log.
     */
    private void addIfBreaked(final int address, final ActionType at,
            final int value) {
        if (isBreaked(address, at)) {
            dirtyActions.add(new MemoryAction(address, at, value));
        }
    }

    /**
     * Performs a forcedRead at specified address, and logs if that address
     * is breaked.
     * @param address Address to read.
     * @return Read value.
     */
    public final int forceRead(final int address) {
        int r = super.forceRead(address);
        addIfBreaked(address, ActionType.READ, r);
        return r;
    }

    /** Performs a forcedWrite at specified address, with specified data.
     * Logs the action if that address is breaked.
     * @param address Address to write data to.
     * @param data Data to write.
     */
    public final void forceWrite(final int address, final int data) {
        super.forceWrite(address, data);
        addIfBreaked(address, ActionType.WRITE, data);
    }

    /**
     * Performs a read at specified address, and logs if that address
     * is breaked.
     * @param address Address to read.
     * @return Read value.
     */
    public final int read(final int address) {
        int r = super.read(address);
        addIfBreaked(address, ActionType.READ, r);
        return r;
    }

    /** Performs a write at specified address, with specified data.
     * Logs the action if that address is breaked.
     * @param address Address to write data to.
     * @param data Data to write.
     */
    public final void write(final int address, final int data) {
        /*
         * write first, then perhaps add dirty action. the write action might
         * throw.
         */
        super.write(address, data);
        addIfBreaked(address, ActionType.WRITE, data);
    }

    
}
