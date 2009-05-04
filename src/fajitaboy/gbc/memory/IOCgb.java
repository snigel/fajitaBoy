package fajitaboy.gbc.memory;

import static fajitaboy.constants.AddressConstants.*;
import fajitaboy.gb.memory.IO;

public class IOCgb extends IO {

    RamHighCgb wram;
    VramCgb vram;
    
    public IOCgb(int start, int end, RamHighCgb wram, VramCgb vram) {
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
            case SVBK_REGISTER: ram[addr] = data;wram.setBank(data & 0x07); break;
            case VRAM_BANK: vram.setBank(data & 0x01); break;
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
