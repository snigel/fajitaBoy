package fajitaboy.gbc.lcd;

import fajitaboy.gb.memory.MemoryInterface;

/**
 * Sprite Attribute in CGB mode.
 */
public class CGB_SpriteAttribute {
    /**
     * Sprite X coordinate + 8
     */
    int x;
    
    /**
     * Sprite Y coordinate + 16
     */
    int y;
    
    /**
     * Tile ID for this sprite.
     */
    int patternNr;
    
    /**
     * True if sprite should be drawn behind Background.
     */
    boolean behindBG;
    
    /**
     * When true, sprite is flipped horizontally.
     */
    boolean flipX;
    
    /**
     * When true, sprite is flipped vertically.
     */
    boolean flipY;
    
    /**
     * Tile VRAM-Bank (0=Bank 0, 1=Bank 1).
     */
    int vramBank;
    
    /**
     * Color palette number.
     */
    int paletteNumber;
    
    public void read(MemoryInterface ram, int addr) {
        y = ram.read(addr);
        x = ram.read(addr + 1);
        patternNr = ram.read(addr + 2);
        
        int flags = ram.read(addr + 3);
        behindBG = (flags & 0x80) > 0;
        flipY = (flags & 0x40) > 0;
        flipX = (flags & 0x20) > 0;
        
        vramBank = (flags & 0x08) >> 3;
        paletteNumber = (flags & 0x07);

    }
}
