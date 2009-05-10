package fajitaboy.gbc.lcd;

import static fajitaboy.constants.AddressConstants.ADDRESS_LY;
import static fajitaboy.constants.AddressConstants.ADDRESS_PALETTE_BG_DATA;
import static fajitaboy.constants.LCDConstants.LCD_H;
import static fajitaboy.constants.LCDConstants.LCD_W;
import fajitaboy.gb.lcd.LCD;
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
     * Conversion table to convert CGB color indexes to RGB values that match what
     * would be displayed on a CGB screen.
     * 
     * "When developing graphics on PCs, note that the RGB values will have different appearance on
     * CGB displays as on VGA monitors:
     * 
	 * The highest intensity will produce Light Gray color rather than White. The intensities
	 * are not linear; the values 10h-1Fh will all appear very bright, while medium and darker
	 * colors are ranged at 00h-0Fh.
	 * 
	 * For example, a color setting of 03EFh (Blue=0, Green=1Fh, Red=0Fh) will appear as Neon Green
	 * on VGA displays, but on the CGB it'll produce a decently washed out Yellow."
     */
    int[] conversionTable;
    
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
        
        // Generate color conversion table, to match colors on CGB displays
        conversionTable = new int[32];
        int midColor = 210;
        int endColor = 240;
        
        double c = 0;
        double step = (double)midColor / 16.0;
        for ( int i = 0; i < 16; i++) {
        	conversionTable[i] = (int)c;
        	c += step;
        }
        c = midColor;
        step = (double)(endColor - midColor) / 16.0;
        for ( int i = 16; i < 31; i++) {
        	conversionTable[i] = (int)c;
        	c += step;
        }
        conversionTable[31] = endColor;
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
			 * Window should Always be above background. But window and
			 * background tiles can have different BG-to-OAM Priority
			 * which make things more complicated. Sprites is always
			 * above background/window color 0.
             */
            
            // Read and draw Sprites behind background and window.
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
                bgm.drawLine(screen, true, ram, ram.getVram(), ly);
            }
            
            // Read and draw window tiles that overrides oam priority.
            if (lcdc.windowDisplayEnable) {
                wnd.drawLine(screen, true, ram, ram.getVram(), ly);
            }
            
        }
        
    }
    
    public int[][] getPixels() {
    	if ( newScreen = true ) {
    		// Translate screen bits to displayable pixels if necessary
    		int data;
    		for ( int x = 0; x < LCD_W; x++ ) {
    			for ( int y = 0; y < LCD_H; y++ ) {
    				data = screen.bits[y][x];
    				int r = conversionTable[data & 0x001F];
    				int g = conversionTable[(data & 0x03E0) >>> 5];
    				int b = conversionTable[(data & 0x7C00) >>> 10];
        			pixels[y][x] = r + (g << 8) + (b << 16);
        		}
    		}
    	}
        newScreen = false;
        return pixels;
    }

}
