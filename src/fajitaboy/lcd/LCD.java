package fajitaboy.lcd;

import static fajitaboy.constants.AddressConstants.*;
import static fajitaboy.constants.MessageConstants.*;
import static fajitaboy.constants.LCDConstants.*;

import fajitaboy.ClockPulseReceiver;
import fajitaboy.memory.AddressBus;
import fajitaboy.memory.MemoryInterface;

/**
 * @author Tobias Svensson
 */
public class LCD implements ClockPulseReceiver {

    /**
     * Pointer to MemoryInterface class.
     */
    AddressBus ram;

    BackgroundMap bgm = new BackgroundMap(BackgroundMap.MapType.BACKGROUND);

    BackgroundMap wnd = new BackgroundMap(BackgroundMap.MapType.WINDOW);

    SpriteAttributeTable sat = new SpriteAttributeTable();

    Screen screen = new Screen();

    LCDC lcdc;

    boolean newScreen = false;

    /*
     * (non-Javadoc)
     * @see ClockPulseReceiver#oscillatorMessage(int)
     */

    /**
     * Creates a new LCD with default values.
     * @param ram
     *            Pointer to an MemoryInterface.
     */
    public LCD(AddressBus ram) {
        this.ram = ram;
        lcdc = new LCDC(ram);
        reset();
    }

    /**
     * Resets LCD to default state.
     */
    public void reset() {

    }

    /**
     * Draws the GameBoy screen once.
     */
    private void drawScreen() {

        // om jag förstår det rätt!
        lcdc.readLCDC();

        
        // om lcdDisplayEnable är false skall skärmen vara vit. 
        // Clear screen
        screen.clear();
        
        if (lcdc.lcdDisplayEnable) {
            // Read OAM (Sprites, Tiles, Background Map...)
        	
        	/* Paint objects in the following order:
        	 * 1. Sprites that are under background & window. (bit7=1 in sprite attribute)
        	 *    Will be painted in order of ascending x-position.
        	 * 2. Background.  
        	 *    BG color 0 is always behind OBJ (Sprite). This means, that color 0 
        	 *    should only be painted, if that pixel is not painted by an sprite in step 1.
        	 * 3. Window.
        	 * 4. Sprites that are above bg & window (bit7=0 in sprite attribute)
        	 *    Will be painted in order of ascending x-position.
        	 */

            // Read and draw sprites that are behind bg&window, if sprites enabled.
            
            
            if (lcdc.objSpriteDisplay) {
                sat.readSpriteAttributes(ram);
                sat.draw(screen, true, ram, lcdc, ram.getVram());
            }
            
            // Read and draw background if enabled.            
            if (lcdc.bgDisplay) {
                bgm.readBackground(ram, lcdc);
                bgm.draw(screen, ram, ram.getVram());
            }

            // Read and draw window if enabled.
            if (lcdc.windowDisplayEnable) {
                wnd.readBackground(ram, lcdc);
                wnd.draw(screen, ram, ram.getVram());
            }
            
            // Read and draw sprites that are above bg & window, if sprites enabled.
            if (lcdc.objSpriteDisplay) {
                sat.draw(screen, false, ram, lcdc, ram.getVram());
            }
            
            /*
            System.out.print(String.format("lcd enabled: %c%c%c%c\n", 
            				lcdc.objSpriteDisplay ? 's' : '#',
            				lcdc.bgDisplay ? 'b' : '#',
            				lcdc.windowDisplayEnable ? 'w' : '#',
            				lcdc.objSpriteDisplay ? 's' : '#'));
            */
        }
    }

    /**
     * Converts 2 bytes into 8 4-color pixels.
     * @param bitsLo
     *            Low bits of pixels
     * @param bitsHi
     *            High bits of pixels
     * @return Array of 8 pixels
     */
    public static int[] convertToPixels(int bitsLo, int bitsHi) {
        int[] pixels = new int[8];

        if ((bitsLo | bitsHi) == 0) {
        	return pixels;
        }
       
        for (int i = 7; i >= 0; i--) {
        	pixels[i] = (bitsLo & 1 | (bitsHi << 1) & 2);
            
            bitsLo >>= 1;
            bitsHi >>= 1;
        }
        
        return pixels;
    }

    /**
     * Receives and handles messages from the Oscillator.
     * @param message
     *            Message constant
     */
    public int oscillatorMessage(int message) {
        if (message == MSG_LCD_VBLANK) {
            // Trigger VBlank interrupt if enabled
            int stat = ram.read(ADDRESS_STAT);
            if ((stat & 0x10) != 0) {
                ram.write(ADDRESS_IF, ram.read(ADDRESS_IF) | 0x02);
            }
            ram.write(ADDRESS_IF, ram.read(ADDRESS_IF) | 0x01);

            ram.forceWrite(ADDRESS_STAT, (ram.read(ADDRESS_STAT) & 0xFC) + 1); // Replace
                                                                                // with
                                                                                // forcedWrite
            drawScreen();
        }

        if (message == MSG_LCD_CHANGE_MODE) {
            int stat = ram.read(ADDRESS_STAT);
            int mode = stat & 0x03;
            stat = stat & 0xFC;

            switch (mode) {
            case 0:
            case 1:
                stat += 2;
                // Trigger LCDSTAT interrupt if Mode 2 OAM interrupt is enabled
                if ((stat & 0x020) != 0) {
                    ram.write(ADDRESS_IF, ram.read(ADDRESS_IF) | 0x02);
                }
                break;
            case 2:
                stat += 3;
                break;
            case 3:
                // Trigger LCDSTAT interrupt if Mode 0 HBlank interrupt is
                // enabled
                if ((stat & 0x08) != 0) {

                    ram.write(ADDRESS_IF, ram.read(ADDRESS_IF) | 0x02);
                }
                break;
            }

            ram.forceWrite(ADDRESS_STAT, stat);
        }

        if (message == MSG_LCD_NEXT_LINE) {
            // Increment LY by one
            int ly = ram.read(ADDRESS_LY) + 0x01;
            int lyc = ram.read(ADDRESS_LYC);
            int stat = ram.read(ADDRESS_STAT);
            ram.forceWrite(ADDRESS_LY, ly);
            if (ly == lyc) {
                // Coincidence bit on
                ram.forceWrite(ADDRESS_STAT, stat | 0x04);
                return MSG_LCD_LYC_HIT;
            } else {
                // Coincidence bit off
                ram.forceWrite(ADDRESS_STAT, stat & 0xFB);
            }
        }

        return 0;
    }

    public boolean newScreenAvailable() {
        return newScreen;
    }

    public int[][] getScreen() {
        newScreen = false;
        return screen.getBits();
    }

}
