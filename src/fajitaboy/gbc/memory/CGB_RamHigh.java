package fajitaboy.gbc.memory;

import fajitaboy.gb.memory.RamHigh;

/**
 * RamHigh (WRAM) memory in CGB mode.
 * Switchable banks 1-7.
 */
public class CGB_RamHigh extends RamHigh {

    /**
     * Current bank used.
     */
    private int bank;
    
    /**
     * {@inheritDoc}
     */
    public CGB_RamHigh(final int start, final int end) {
        super(start, end);
        ram = new int[8 * length];
        bank = 1;
    }
    
    public void reset() {
    	ram = new int[8 * length];
    }
    
    /**
     * Changes the bank.
     */
    public void setBank(int newBank) {
        if(newBank == 0) {
            bank = 1;
        } else {
            bank = newBank;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public int read(final int address) {
        int addr = address - offset;
        if (addr < 0 || addr > ram.length) {
            throw new ArrayIndexOutOfBoundsException(String.format("MemoryComponent: could not read 0x%04x", address));
        }
        return ram[addr + (length * (bank-1))];
    }

    /**
     * {@inheritDoc}
     */
    public void write(final int address, final int data) {
        int addr = address - offset;
        if (addr < 0 || addr > ram.length) {
            throw new ArrayIndexOutOfBoundsException(String.format("MemoryComponent: could not write 0x%04x", address));
        }
        ram[addr + (length * (bank-1))] = data;
    }

}
