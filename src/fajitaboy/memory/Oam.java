package fajitaboy.memory;

import static fajitaboy.constants.AddressConstants.*;

/**
 * Represents the Sprite table part of the memory.
 *
 * @author Adam Hulin, Johan Gustafsson
 *
 */
public class Oam extends MemoryComponent {



    /**
     * Used in dma transfer.
     */
    private MemoryInterface memInt;

    /**
     * @param start
     *            , address representing where OAM begins in memory space
     * @param end
     *            , address representing where OAM ends in memory space These
     *            two values are used for creating the right size of the OAM
     *            array and for setting the offset value
     */
    public Oam(final int start, final int end, MemoryInterface memInt) {
        super(start, end);
        this.memInt = memInt;
    }

    /**
     * {@inheritDoc}
     */
    public void write(final int address, final int data) {
        int addr = address - offset;
        if (address == ADDRESS_DMA) {
            dmaTransfer(data);
        } else if (addr < 0 || addr > ram.length) {
            throw new ArrayIndexOutOfBoundsException("Oam.java");
        } else {
            ram[addr] = data;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public int read(final int address, final int data) {
        int addr = address - offset;
        if (address == ADDRESS_DMA) {
            return 0x00;
        } else if (addr < 0 || addr > ram.length) {
            throw new ArrayIndexOutOfBoundsException("Oam.java");
        } else {
            return ram[addr];
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public int forceRead(final int address) {
        int addr = address - offset;
        if (address == ADDRESS_DMA) {
            return 0x00;
        } else if (addr < 0 || addr > ram.length) {
            throw new ArrayIndexOutOfBoundsException("Oam.java");
        } else {
            return ram[addr];
        }
    }

    /**
     * {@inheritDoc}
     */
    public void forceWrite(final int address, final int data) {
        int addr = address - offset;
        if (address == ADDRESS_DMA) {
            dmaTransfer(data);
        } else if (addr < 0 || addr > ram.length) {
            throw new ArrayIndexOutOfBoundsException("Oam.java");
        } else {
            ram[addr] = data;
        }
    }

    /**
     * Performs an OAM DMA transfer. This copies the area 0xXX00-0xXXFF from
     * memory, where 0xXX is specified in the input variable data, into the area
     * 0xFE00 - 0xFEFF.
     *
     * @param data
     *            Memory area to copy. Must be in range 0x00 - 0xF1
     */
    private void dmaTransfer(int data) {
    	//System.out.println(String.format("DMA Transfer, start address: %04x", data*0x100));
        if (data < 0 || data > 0xF1)
            return;

        int targAddr = data * 0x100;
        int destination = 0;
        while (destination < 0xA0) {
            ram[destination] = memInt.read(targAddr);
            targAddr++;
            destination++;
        }
    }
}