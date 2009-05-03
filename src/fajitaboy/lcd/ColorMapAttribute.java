package fajitaboy.lcd;

import fajitaboy.memory.MemoryInterface;

public class ColorMapAttribute {

    /**
     * Bit 0-2  Background Palette number  (BGP0-7)
     */
    int PaletteNo;
    
    /**
     * Bit 3    Tile VRAM Bank number      (0=Bank 0, 1=Bank 1)
     */
    int vramBank;
    
    /**
     * Bit 5    Horizontal Flip            (0=Normal, 1=Mirror horizontally)
     */
    boolean flipX;
    
    /**
     * Bit 6    Vertical Flip              (0=Normal, 1=Mirror vertically)
     */
    boolean flipY;
    
    /**
     * Bit 7    BG-to-OAM Priority         (0=Use OAM priority bit, 1=BG Priority)
     */
    boolean priority;
    
    /**
     * Updates values.
     */
    public void update(int flags) {
        PaletteNo = flags & 0x07;
        vramBank = (flags & 0x08) >> 3;
        flipX = (flags & 0x20) > 0;
        flipY = (flags & 0x40) > 0;
        priority = (flags & 0x80) > 0;
    }
    
}
