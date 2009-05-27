package fajitaboy.gb.lcd;

import static fajitaboy.constants.AddressConstants.*;

public class SpriteAttribute implements Comparable<SpriteAttribute> {
	
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
    
    public int flags;
    
    /**
     * Address to sprite palette.
     */
    public int paletteAddr;
    
    public SpriteAttribute() {
    	x = 0;
    	y = 0;
    	patternNr = 0;
    	behindBG = false;
    	flipX = false;
    	flipY = false;
    	paletteAddr = ADDRESS_PALETTE_SPRITE0_DATA;
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
        paletteAddr = (flags & 0x10) > 0 ? ADDRESS_PALETTE_SPRITE1_DATA : ADDRESS_PALETTE_SPRITE0_DATA;
    }
    
    public int compareTo(SpriteAttribute other) {
    	return other.x - this.x;
    }
}
