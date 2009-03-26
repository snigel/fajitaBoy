package fajitaboy;

import java.util.LinkedList;
import java.util.List;

import fajitaboy.memory.AddressBus;

/**
 * DebuggerMemoryInterface works like a wrapper around an memory interface, but it
 * allows breakpoints to be added on address spaces. When someone calls read or
 * write on an address corresponding to a breakpoint, an MemoryAction is logged.
 * @author arvid
 *
 */
public class DebuggerMemoryInterface extends AddressBus {
    /**
     * An action type can be READ, WRITE or READ_WRITE. This enum is used
     * to specify different kinds of memory breakpoints, and different kinds
     * of MemoryActions.
     * @author arvid
     *
     */
    public enum ActionType {
        /**
         * Read action.
         */
        READ,
        /**
         * Write action.
         */
        WRITE,
        /**
         * Read or Write action.
         */
        READ_WRITE;

        /**
         * Checks if a read action corresponds to another.
         * @param other other action to check against.
         * @return whether the two actions are compatible or not.
         */
        public boolean compatible(final ActionType other) {
            return (this == READ_WRITE || this == other);
        }

        /** Converts an ActionType to a minimal string representation.
         * @return minimal string representation.
         */
        public String toTinyString() {
            switch (this) {
            case READ:
                return "r";
            case WRITE:
                return "w";
            case READ_WRITE:
                return "rw";
            default:
                return "??";
            }
        }

        /**
         * Converts a minimal string representation into an ActionType.
         * @param tiny Minimal string representation to convert.
         * @return An ActionType corresponding to the input string.
         */
        public static ActionType fromTinyString(final String tiny) {
            if (tiny.equals("r")) {
                return READ;
            } else if (tiny.equals("w")) {
                return WRITE;
            } else if (tiny.equals("rw")) {
                return READ_WRITE;
            } else {
                return null;
            }
        }
    };

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
    public DebuggerMemoryInterface(final String romPath) {
        super(romPath);
        
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

    /** An object of class MemoryBreakpoint represents a breakpoint on a
     * memory space, of a specific ActionType.
     * @author arvid
     */
    public class MemoryBreakpoint {
        /**
         * Lower address bound for this breakpoint.
         */
        private int lower;

        /**
         * Upper address bound for this breakpoint.
         */
        private int upper;

        /**
         * ActionType which this breakpoints breaks at.
         */
        private ActionType onAction;

        /**
         * Creates a MemoryBreakpoint with the given memory bounds and
         * ActionType.
         * @param lwr Lower memory bound.
         * @param upr Upper memory bound.
         * @param onAct ActionType which this breakpoint is triggered by.
         */
        public MemoryBreakpoint(final int lwr, final int upr,
                final ActionType onAct) {
            this.lower = lwr;
            this.upper = upr;
            this.onAction = onAct;
        }

        /** Creates a MemoryBreakpoint encompassing a single memory address.
         * @param address MemoryAddress for this breakpoint.
         * @param onAct ActionType to trigger on.
         */
        public MemoryBreakpoint(final int address, final ActionType onAct) {
            this(address, address, onAct);
        }

        /**
         * @return the lower bound of this MemoryBreakpoint.
         */
        public final int getLower() {
            return lower;
        }

        /**
         * @return the lower bound of this MemoryBreakpoint.
         */
        public final int getUpper() {
            return upper;
        }

        /**
         * @return the ActionType which triggers this breakpoint.
         */
        public final ActionType getOnAction() {
            return onAction;
        }

        @Override
        public final String toString() {
            if (lower == upper) {
                return String.format("%-4s %04x", onAction.toTinyString(),
                        lower);
            } else {
                return String.format("%-4s %04x-%04x", onAction.toTinyString(),
                        lower, upper);
            }
        }
    }

    /**
     * An object of the class MemoryAction represents an Action with a value
     * on the MemoryInterface. This can either be a write, with value being
     * the data written or an read, with value being the value read.
     * @author arvid
     */
    public class MemoryAction {
        /**
         * Type of action.
         */
        private ActionType type;

        /**
         * Adress where this action was performed.
         */
        private int address;

        /**
         * Value that was written or read.
         */
        private int value;

        /** Creates an MemoryAction at specified address of specified ActionType.
         * @param addr Address where action was performed.
         * @param tp Type of action.
         * @param val Value that was written or read.
         */
        public MemoryAction(final int addr, final ActionType tp,
                final int val) {
            this.address = addr;
            this.type = tp;
            this.value = val;
        }

        /**
         * @return the address where this action was performed.
         */
        public final int getAddress() {
            return address;
        }

        /**
         * @return the type of actions that was performed.
         */
        public final ActionType getType() {
            return type;
        }

        /**
         * @return the value that was read or written.
         */
        public final int getValue() {
            return value;
        }

        @Override
        public final String toString() {
            return String.format("%-4s %04x %02x", type.toTinyString(),
                    address, value);
        }
    }
}
