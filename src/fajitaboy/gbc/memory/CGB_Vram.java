package fajitaboy.gbc.memory;

import static fajitaboy.constants.AddressConstants.*;
import static fajitaboy.constants.HardwareConstants.*;
import fajitaboy.gb.lcd.Tile;
import fajitaboy.gb.memory.MemoryBankInterface;
import fajitaboy.gb.memory.MemoryInterface;
import fajitaboy.gb.memory.Vram;

/**
 * Vram with switchable bank (0-1).
 */
public class CGB_Vram extends Vram implements MemoryBankInterface {
    
    /**
     * Current switch bank used.
     */
    private int bank;
    
    private MemoryInterface memInt;
    
    /**
     * Creates a new Vram in CGB mode. 
     * @param start 
     * @param end
     * @param memInt 
     */
    public CGB_Vram(final int start, final int end, MemoryInterface memInt) {
        super(start, end);
        this.memInt = memInt;
        bank = 0;
    }
    
    /**
     * {@inheritDoc}
     */
    public void reset() {
        ram = new int[length*2];
        tiles = new Tile[2*CGB_TILES];
        for ( int i = 0; i < 2*CGB_TILES; i++) {
        	tiles[i] = new Tile();
        }
    }

    /**
     * {@inheritDoc}
     */
    public int read(final int address) {
        
        int addr = (address - offset);
        if (addr < 0 || addr > ram.length) {
            throw new ArrayIndexOutOfBoundsException("RamLow.java");
        } else if ( address < ADDRESS_TILE_DATA_END ) {
            return tiles[(addr / 16) + bank * 2 * GB_TILES].read(address);
        } else {
            return ram[addr + bank * length];
        }
    }
    

    /**
     * {@inheritDoc}
     */
    public void write(final int address, final int data) {
        int addr = (address - offset);
        if (address == ADDRESS_VRAM_DMA_START) {
            dmaTransfer(data);
        } else if (addr < 0 || addr > ram.length) {
            throw new ArrayIndexOutOfBoundsException("RamHigh.java");
        } else if ( address < ADDRESS_TILE_DATA_END ) {
            tiles[(addr / 16) + bank * 2 * GB_TILES].write(address, data);
        } else  {
            ram[addr + bank * length] = data;
        }
    }
    
    /**
     * @param newBank bank to use.
     */
    public void setBank(int newBank) {
        bank = newBank & 0x01;
    }
    
    /**
     * {@inheritDoc}
     */
    public int read(final int address, final int bank) {
        int addr = (address - offset);
        if (addr < 0 || addr > ram.length) {
            throw new ArrayIndexOutOfBoundsException("RamLow.java");
        } else if ( address < ADDRESS_TILE_DATA_END ) {
            return tiles[(addr / 16) + bank * 2 * GB_TILES].read(address);
        } else {
            return ram[addr + bank * length];
        }
    }
    
    
    /**
     * Starts Vram Data Transfer
     * 
     * @param data Data written to 0xFF55
     */
    // TODO Implement support for H-Blank Transfer
    private void dmaTransfer(int data) {
        
        int src  = memInt.read(ADDRESS_VRAM_DMA_SOURCE_H) * 0x100 + (memInt.read(ADDRESS_VRAM_DMA_SOURCE_L) & 0xF0); 
        int dest = memInt.read(ADDRESS_VRAM_DMA_DEST_H) * 0x100 + (memInt.read(ADDRESS_VRAM_DMA_DEST_L) & 0xF0);
        
        if ( dest < 0x8000 || dest >= 0xA000 )
        	return;  // Erroneous destination
        if ( src >= 0xE000 || (src < 0xA000 && src >= 0x8000) )
        	return;  // Erroneous destination
        
        
        int length = ((data & 0x7F) * 0x10) + 0x10;
        for (int i = 0; i < length; i++) {
            memInt.write(dest + i, memInt.read(src + i));
        }
        
        // bit 7 is 0 and shows that DMA is not Active
        ram[ADDRESS_VRAM_DMA_START - offset] = (data & 0x7F);
    }
    
    /**
     * @return Returns current bank.
     */
    public int getBank() {
        return bank;
    }
}
