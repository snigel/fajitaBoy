package fajitaboy.lcd;

import static fajitaboy.constants.AddressConstants.*;
import static fajitaboy.constants.MessageConstants.*;
import static fajitaboy.constants.LCDConstants.*;

import fajitaboy.ClockPulseReceiver;
import fajitaboy.MemoryInterface;

/**
 * @author Tobias Svensson
 */
public class LCD implements ClockPulseReceiver {

    /**
     * Pointer to MemoryInterface class.
     */
    MemoryInterface ram;

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
    public LCD(MemoryInterface ram) {
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
            /* Måla objekt i följande ordning:
             * 1. Sprites som ligger under bg & window (bit7=1 i sprite attribute)
             *    Skall målas i ordning efter ökande x-position.
             * 2. Background. BG color 0 is always behind Sprite :S
             * 3. Window. Tror inte samma som gäller för background gäller för window
             * 4. Sprites som ligger över bg & window. (bit7=0 i sprite attribute)
             *    Skall målas i ordning efter ökande x-position.
             */

            // Read and draw sprites that are behind bg&window, if sprites enabled.
            //System.out.print(String.format("lcd enabled: %02x", ram.read(LCDC_REGISTER)));
            
            if (lcdc.objSpriteDisplay) {
                sat.readSprites(ram);
                sat.draw(screen, true, ram, lcdc);
            }
            
            // Read and draw background if enabled.            
            if (lcdc.bgDisplay) {
                bgm.readBackground(ram, lcdc);
                bgm.draw(screen, ram);
            }

            // Read and draw window if enabled.
            if (lcdc.windowDisplayEnable) {
                wnd.readBackground(ram, lcdc);
                wnd.draw(screen, ram);
            }
            
            // Read and draw sprites that are above bg & window, if sprites enabled.
            if (lcdc.objSpriteDisplay) {
                sat.readSprites(ram);
                sat.draw(screen, false, ram, lcdc);
            }
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

        if ((bitsLo & 0x80) != 0)
            pixels[0] += 1;
        if ((bitsLo & 0x40) != 0)
            pixels[1] += 1;
        if ((bitsLo & 0x20) != 0)
            pixels[2] += 1;
        if ((bitsLo & 0x10) != 0)
            pixels[3] += 1;
        if ((bitsLo & 0x08) != 0)
            pixels[4] += 1;
        if ((bitsLo & 0x04) != 0)
            pixels[5] += 1;
        if ((bitsLo & 0x02) != 0)
            pixels[6] += 1;
        if ((bitsLo & 0x01) != 0)
            pixels[7] += 1;

        if ((bitsHi & 0x80) != 0)
            pixels[0] += 2;
        if ((bitsHi & 0x40) != 0)
            pixels[1] += 2;
        if ((bitsHi & 0x20) != 0)
            pixels[2] += 2;
        if ((bitsHi & 0x10) != 0)
            pixels[3] += 2;
        if ((bitsHi & 0x08) != 0)
            pixels[4] += 2;
        if ((bitsHi & 0x04) != 0)
            pixels[5] += 2;
        if ((bitsHi & 0x02) != 0)
            pixels[6] += 2;
        if ((bitsHi & 0x01) != 0)
            pixels[7] += 2;

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
