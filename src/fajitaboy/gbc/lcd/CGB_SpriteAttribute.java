package fajitaboy.gbc.lcd;

/**
 * Sprite Attribute in CGB mode.
 */
public class CGB_SpriteAttribute implements Comparable<CGB_SpriteAttribute> {
    /**
     * Sprite X coordinate + 8
     */
    public int x;
    
    /**
     * Sprite Y coordinate + 16
     */
    public int y;
    
    /**
     * Tile ID for this sprite.
     */
    public int patternNr;
    
    /**
     * True if sprite should be drawn behind Background.
     */
    public boolean behindBG;
    
    /**
     * When true, sprite is flipped horizontally.
     */
    public boolean flipX;
    
    /**
     * When true, sprite is flipped vertically.
     */
    public boolean flipY;
    
    /**
     * Tile VRAM-Bank (0=Bank 0, 1=Bank 1).
     */
    public int vramBank;
    
    /**
     * Color palette number.
     */
    public int paletteNumber;
    
    public int flags;
    
    /**
     * Sprite positin in OAM
     */
    private int oamPosition;
    
    public CGB_SpriteAttribute(int oamPosition) {
        this.oamPosition = oamPosition;
        
        x = 0;
        y = 0;
        patternNr = 0;
        behindBG = false;
        flipX = false;
        flipY = false;
        paletteNumber = 0;
        vramBank = 0;
    }
    
    public void setX( int x ) {
        this.x = x;
    }
    
    public void setY( int y ) {
        this.y = y;
    }
    
    public void setPattern( int p ) {
        patternNr = p;
    }
    
    public void setFlags( int flags ) {
        this.flags = flags;
        behindBG = (flags & 0x80) > 0;
        flipY = (flags & 0x40) > 0;
        flipX = (flags & 0x20) > 0;
        
        vramBank = (flags & 0x08) >> 3;
        paletteNumber = (flags & 0x07);

    }
    
    public int compareTo(CGB_SpriteAttribute other) {
        return this.oamPosition - other.oamPosition;
    }
}
