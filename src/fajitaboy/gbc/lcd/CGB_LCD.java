package fajitaboy.gbc.lcd;

import static fajitaboy.constants.AddressConstants.ADDRESS_LY;
import static fajitaboy.constants.AddressConstants.ADDRESS_PALETTE_BG_DATA;
import fajitaboy.gb.lcd.BackgroundMap;
import fajitaboy.gb.lcd.LCD;
import fajitaboy.gb.lcd.WindowMap;
import fajitaboy.gb.memory.AddressBus;
import fajitaboy.gbc.memory.CGB_AddressBus;
/**
 * 
 */
public class CGB_LCD extends LCD {
    

    private CGB_SpriteAttributeTable sat;

    private CGB_AddressBus ram;
    
    private CGB_Screen screen;
    
    private CGB_BackgroundMap bgm;
    
    private CGB_WindowMap wnd;
    
    /**
     * Creates a new ColorLCD with default values.
     * @param ram
     *            Pointer to an AddressBusCgb.
     */
    public CGB_LCD(CGB_AddressBus ram) {
        super(ram);
        this.ram = ram;
        sat = new CGB_SpriteAttributeTable();
        screen = new CGB_Screen( ram.getBackgroundPaletteMemory(), ram.getSpritePaletteMemory());
        bgm = new CGB_BackgroundMap();
        wnd = new CGB_WindowMap();
    }
    
    /**
     * {@inheritDoc}
     */
    protected void drawScreen() {
        // Abort if Frame Skip is activated
        if (frameSkip)
            return;

        // Read OAM (Sprites, Tiles, Background Map...)
        lcdc.readLCDC();

        if (lcdc.lcdDisplayEnable) {

            /*
             * Draw graphic objects
             */
            // TODO find out what color to clear line with.
            int bgclr = ram.read(ADDRESS_PALETTE_BG_DATA) & 0x03;
            int ly = ram.read(ADDRESS_LY);
            screen.clearLine(bgclr, ly);

            /*
             * Paint objects in the following order: 1. Sprites that are under
             * background & window. (bit7=1 in sprite attribute) Will be painted
             * in order of ascending x-position. 2. Background. BG color 0 is
             * always behind OBJ (Sprite). This means, that color 0 should only
             * be painted, if that pixel is not painted by an sprite in step 1.
             * 3. Window. 4. Sprites that are above bg & window (bit7=0 in
             * sprite attribute) Will be painted in order of ascending
             * x-position.
             */
            if (lcdc.objSpriteDisplay) {
                sat.readSpriteAttributes(ram);
                sat.draw(screen, true, ram, lcdc, ram.getVram(), ly);
            }

            // Read and draw background if enabled.
            if (lcdc.bgDisplay) {
                bgm.readBackgroundWholeLine(ly, ram, ram.getVram(), lcdc);
                bgm.drawLine(screen, false, ram, ram.getVram(), ly);
            }

            // Read and draw window if enabled.
            if (lcdc.windowDisplayEnable) {
                // not color compatible yet.
                wnd.readWindowLine(ly, ram, ram.getVram(), lcdc);
                wnd.drawLine(screen, false, ram, ram.getVram(), ly);
            }

            // Read and draw sprites that are above bg & window, if sprites
            // enabled.
            if (lcdc.objSpriteDisplay) {
                sat.draw(screen, false, ram, lcdc, ram.getVram(), ly);
            }
            
            // Read and draw background tiles that overrides oam priority.
            if (lcdc.bgDisplay) {
                bgm.readBackgroundWholeLine(ly, ram, ram.getVram(), lcdc);
                bgm.drawLine(screen, true, ram, ram.getVram(), ly);
            }
            
            // Read and draw window tiles that overrides oam priority.
            if (lcdc.windowDisplayEnable) {
                // not color compatible yet.
                wnd.readWindowLine(ly, ram, ram.getVram(), lcdc);
                wnd.drawLine(screen, true, ram, ram.getVram(), ly);
            }
            
        }
        
    }
    
    /**
     * {@inheritDoc}
     */
    public int[][] getScreen() {
        newScreen = false;
        return screen.getBits();
    }

}
