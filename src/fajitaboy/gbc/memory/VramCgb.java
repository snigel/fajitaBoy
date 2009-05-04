package fajitaboy.gbc.memory;

import static fajitaboy.constants.AddressConstants.*;
import static fajitaboy.constants.LCDConstants.GB_TILES;
import fajitaboy.gb.lcd.Tile;
import fajitaboy.gb.memory.MemoryInterface;
import fajitaboy.gb.memory.Vram;

/**
 * Vram with switchable bank (0-1).
 */
public class VramCgb extends Vram {
    
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
    public VramCgb(final int start, final int end, MemoryInterface memInt) {
        super(start, end);
        this.memInt = memInt;
        bank = 0;
    }

    /**
     * {@inheritDoc}
     */
    public int read(final int address) {
        
        int addr = (address - offset);
        if (addr < 0 || addr > ram.length) {
            throw new ArrayIndexOutOfBoundsException("RamLow.java");
        } else if ( address < TILE_DATA_END ) {
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
        if (address == VRAM_DMA_START) {
            dmaTransfer(data);
        } else if (addr < 0 || addr > ram.length) {
            throw new ArrayIndexOutOfBoundsException("RamHigh.java");
        } else if ( address < TILE_DATA_END ) {
            tiles[(addr / 16) + bank * 2 * GB_TILES].write(address, data);
        } else  {
            ram[addr + bank * length] = data;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void reset() {
        ram = new int[2 * length];
        tiles = new Tile[4*GB_TILES];
        for ( int i = 0; i < 4*GB_TILES; i++) {
            tiles[i] = new Tile();
        }
    }
    
    /**
     * @param newBank bank to use.
     */
    public void setBank(int newBank) {
        bank = newBank;
    }
    
    /**
     * {@inheritDoc}
     */
    public int read(final int address, final int bank) {
        int addr = (address - offset);
        if (addr < 0 || addr > ram.length) {
            throw new ArrayIndexOutOfBoundsException("RamLow.java");
        } else if ( address < TILE_DATA_END ) {
            return tiles[(addr / 16) + bank * 2 * GB_TILES].read(address);
        } else {
            return ram[addr + bank * length];
        }
    }
    
    
    /**
     * Starts VRAM DMA Transfer.
     * This method has not been tested!
     * TODO: test!
     * The data is transfered with no delay as it is now.
     */
    private void dmaTransfer(int data) {
        // what should happen if the src and dest point to wrong area?
        int src  = memInt.read(VRAM_DMA_SOURCE_H) * 0x100 + (memInt.read(VRAM_DMA_SOURCE_L) & 0xF0); 
        int dest = memInt.read(VRAM_DMA_DESTINATION_H) * 0x100 + (memInt.read(VRAM_DMA_DESTINATION_L) & 0xF0);
        
        int length = (data & 0x7F) / 0x10 - 1;
        //int mode = data >>> 7; 
        // TODO make something else if mode 1
        for (int i = 0; i < length; i++) {
            memInt.write(dest + i, memInt.read(src + i));
        }
        
        // bit 7 is 0 and shows that DMA is not Active
        ram[VRAM_DMA_START - offset] = (data & 0x7F);
    }
    
    /**
     * @return Returns current bank.
     */
    public int getBank() {
        return bank;
    }
}
