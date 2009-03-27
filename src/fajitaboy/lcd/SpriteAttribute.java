package fajitaboy.lcd;

import fajitaboy.memory.MemoryInterface;
import static fajitaboy.constants.AddressConstants.*;

public class SpriteAttribute implements Comparable {
    int x, y, patternNr;
    
    boolean behindBG;
    boolean flipX;
    boolean flipY;
    int paletteAddr;
    
    public void read(MemoryInterface ram, int addr) {
        y = ram.read(addr);
        x = ram.read(addr + 1);
        patternNr = ram.read(addr + 2);
        
        int flags = ram.read(addr + 3);
        behindBG = (flags & 0x80) > 0;
        flipY = (flags & 0x40) > 0;
        flipY = (flags & 0x20) > 0; 
        paletteAddr = (flags % 0x10) > 0 ? PALETTE_SPRITE0_DATA : PALETTE_SPRITE1_DATA;
    }
    
    public int compareTo(Object other) {
        if (other != null) {
            SpriteAttribute so = (SpriteAttribute)other;
            return new Integer(this.x).compareTo(new Integer(so.x));
        } else {
            throw new NullPointerException();
        }
    }
}
