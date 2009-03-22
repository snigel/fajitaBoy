package fajitaboy.lcd;

import fajitaboy.MemoryInterface;

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
        paletteAddr = (flags % 0x10) > 0 ? 0xFF48 : 0xFF49;
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
