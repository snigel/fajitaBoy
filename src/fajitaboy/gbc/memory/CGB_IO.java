package fajitaboy.gbc.memory;

import static fajitaboy.constants.AddressConstants.*;
import fajitaboy.gb.memory.IO;

public class CGB_IO extends IO {

    CGB_RamHigh wram;
    CGB_Vram vram;
    
    public CGB_IO(int start, int end, CGB_RamHigh wram, CGB_Vram vram) {
        super(start, end);
        this.wram = wram;
        this.vram = vram;
        
        // Undocumented registers initial values
        ram[0xFF6C - offset] = 0xFE;
        ram[0xFF75 - offset] = 0x8F;
    }
    
    public void write(final int address, final int data) {
        int addr = address - offset;
        switch (address) {
            case ADDRESS_SVBK: ram[addr] = data;wram.setBank(data & 0x07); break;
            case ADDRESS_VRAM_BANK: vram.setBank(data & 0x01); break;
            /*case VRAM_DMA_START: 
                // start VRAM DMA transfer
                int src = 
                break;*/
            
            // Undocumented registers
            case 0xFF6C: ram[addr] |= (data & 0x01);
            case 0xFF75: ram[addr] |= (data & 0x70);
            default: super.write(address, data); break;
        }
    }

}
