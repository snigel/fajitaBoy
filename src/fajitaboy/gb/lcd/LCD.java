package fajitaboy.gb.lcd;

import static fajitaboy.constants.AddressConstants.*;
import static fajitaboy.constants.HardwareConstants.*;
import static fajitaboy.constants.LCDConstants.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import fajitaboy.FileIOStreamHelper;
import fajitaboy.gb.StateMachine;
import fajitaboy.gb.memory.AddressBus;

/**
 * @author Tobias Svensson
 */
public class LCD implements StateMachine {

    /**
     * Pointer to MemoryInterface class.
     */
    protected AddressBus ram;

    /**
     * Contains the Background.
     */
    private BackgroundMap bgm = new BackgroundMap();

    /**
     * Contains the Window.
     */
    protected WindowMap wnd = new WindowMap();

    /**
     * Contains the Sprite Attribute Table.
     */
    private SpriteAttributeTable sat;

    /**
     * Contains the screen pixels.
     */
    private Screen screen = new Screen();

    /**
     * Contains the LCDC flags.
     */
    protected LCDC lcdc;

    /**
     * Flag is true if a new screen has been rendered.
     */
    protected boolean newScreen = false;

    /**
     * If true, frame will not be rendered.
     */
    protected boolean frameSkip = false;

    /**
     * Next cycle at which the LCD will proceed to next line.
     */
    private long nextLineInc;

    /**
     * Next cycle at which the LCD will change its mode.
     */
    private long nextModeChange;
    
    protected int[][] pixels;

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
        sat = new SpriteAttributeTable(ram, lcdc, ram.getVram(), screen, ram.getOam() );
        reset();
    }

    public LCD() {
	}

	/**
     * Resets LCD to default state.
     */
    public void reset() {
        nextLineInc = GB_CYCLES_PER_LINE;
        nextModeChange = GB_CYCLES_PER_LINE;
        pixels = new int[LCD_H][LCD_W];
    }

    /**
     * Draws the GameBoy screen once.
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
                sat.draw(true, ly);
            }

            // Read and draw background if enabled.
            if (lcdc.bgDisplay) {
                bgm.readBackgroundWholeLine(ly, ram, lcdc);
                bgm.drawLine(screen, ram, ram.getVram(), ly);
            }

            // Read and draw window if enabled.
            if (lcdc.windowDisplayEnable) {
                wnd.readWindowLine(ly, ram, lcdc);
                wnd.drawLine(screen, ram, ram.getVram(), ly);
            }

            // Read and draw sprites that are above bg & window, if sprites
            // enabled.
            if (lcdc.objSpriteDisplay) {
                sat.draw(false, ly);
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
     * Updates the LCD a given amount of cycles.
     * @param cycles
     *            Oscillator cycles to step
     */
    public void updateLCD(int cycles) {
        int ly;
        boolean lycHit = false;

        // Move LCD to next line
        if (cycles > nextLineInc) {
            lycHit = nextLine();
            nextLineInc += GB_CYCLES_PER_LINE;
            ly = ram.read(ADDRESS_LY);

            // Perform V-Blank
            if (ly == GB_VBLANK_LINE) {
                vblank();
                // if (!frameSkip) {
                newScreen = true;
                // }
            }
        }
        nextLineInc -= cycles;

        if (cycles > nextModeChange) {
            ly = ram.read(ADDRESS_LY);
            if (ly < 144) {
                changeMode();

                // Read current mode and determine wait time from that
                int mode = ram.read(ADDRESS_STAT) & 0x03;
                if (mode == 0) {
                    nextModeChange += GB_HBLANK_PERIOD;
                } else if (mode == 2) {
                    nextModeChange += GB_LCD_OAMSEARCH_PERIOD;
                } else if (mode == 3) {
                    nextModeChange += GB_LCD_TRANSFER_PERIOD;
                }
            } else {
                // This line nothing happens, see what happens next line...
                nextModeChange += GB_CYCLES_PER_LINE;
            }
        }
        nextModeChange -= cycles;

        // Handle LYC hit
        if (lycHit) {
            // Trigger LCDSTAT interrupt if LYC=LY Coincidence Interrupt is
            // enabled
            int stat = ram.read(ADDRESS_STAT);
            if ((stat & 0x40) != 0) {
                ram.write(ADDRESS_IF, ram.read(ADDRESS_IF) | 0x02);
            }
        }
    }

    /**
     * Performs the V-Blank.
     */
    public void vblank() {
        // Trigger VBlank interrupt if enabled
        int stat = ram.read(ADDRESS_STAT);

        // if VBlank is enabled in LCDC
        if ((stat & 0x10) != 0) {
            ram.write(ADDRESS_IF, ram.read(ADDRESS_IF) | 0x02);
        }
        ram.write(ADDRESS_IF, ram.read(ADDRESS_IF) | 0x01);

        // change mode to "During v-blank"
        ram.forceWrite(ADDRESS_STAT, (ram.read(ADDRESS_STAT) & 0xFC) + 1);
    }

    /**
     * Changes the current mode of the LCD.
     */
    public void changeMode() {
        int stat = ram.read(ADDRESS_STAT);
        int mode = stat & 0x03;
        stat = stat & 0xFC;

        switch (mode) {
        case 0:

        case 1:

            stat += 2;
            // Trigger LCDSTAT interrupt if Mode 2 OAM interrupt is enabled
            if ((stat & 0x20) != 0) {
                ram.write(ADDRESS_IF, ram.read(ADDRESS_IF) | 0x02);
            }
            break;
        case 2:
            stat += 3;
            break;
        case 3:
            // Trigger LCDSTAT interrupt if Mode 0 HBlank interrupt is
            // enabled
            drawScreen();
            if ((stat & 0x08) != 0) {
                ram.write(ADDRESS_IF, ram.read(ADDRESS_IF) | 0x02);
            }
            break;
        }

        ram.forceWrite(ADDRESS_STAT, stat);
    }

    /**
     * Makes the LCD step to the next line on the screen.
     * @return Returns whether an LYC hit has occurred.
     */
    public boolean nextLine() {
        // Increment LY by one
        int ly = ram.read(ADDRESS_LY) + 1;
        int lyc = ram.read(ADDRESS_LYC);
        int stat = ram.read(ADDRESS_STAT);
        if (ly >= 154)
            ly = 0;
        ram.forceWrite(ADDRESS_LY, ly);
        if (ly == lyc) {
            // Coincidence bit on
            ram.forceWrite(ADDRESS_STAT, stat | 0x04);
            return true;
        } else {
            // Coincidence bit off
            ram.forceWrite(ADDRESS_STAT, stat & 0xFB);
            return false;
        }
    }

    /**
     * Returns true if a new screen has been rendered, that has not yet been obtained.
     * @return True if new screen has not been obtained, false otherwise.
     */
    public boolean newScreenAvailable() {
        return newScreen;
    }

    /**
     * Returns the pixels that should be drawn onto the screen.
     * @return Pixels in 24bit color form
     */
    public int[][] getPixels() {
    	if ( newScreen = true ) {
    		// Translate screen bits to displayable pixels if necessary
    		int[] translationPalette = new int[4];
    		int bit;
    		translationPalette[0] = 0xFFFFFF;
    		translationPalette[1] = 0xAAAAAA;
    		translationPalette[2] = 0x555555;
    		translationPalette[3] = 0x000000;
    		for ( int x = 0; x < LCD_W; x++ ) {
    			for ( int y = 0; y < LCD_H; y++ ) {
    				bit = screen.bits[y][x];
        			pixels[y][x] = translationPalette[bit];
        		}
    		}
    	}
        newScreen = false;
        return pixels;
    }

    /**
     * Enables frame skip.
     */
    public void enableFrameSkip() {
        frameSkip = true;
    }

    /**
     * Disables frame skip.
     */
    public void disableFrameSkip() {
        frameSkip = false;
    }
    
    /**
     * {@inheritDoc}
     */
    public void saveState( FileOutputStream os ) throws IOException {
    	FileIOStreamHelper.writeBoolean(os, frameSkip);
    	FileIOStreamHelper.writeBoolean(os, newScreen);
    	FileIOStreamHelper.writeData(os, nextLineInc, 8);
    	FileIOStreamHelper.writeData(os, nextModeChange, 8);
    }
    
    /**
     * {@inheritDoc}
     */
    public void readState( FileInputStream is ) throws IOException {
    	frameSkip = FileIOStreamHelper.readBoolean(is);
    	newScreen = FileIOStreamHelper.readBoolean(is);
    	nextLineInc = FileIOStreamHelper.readData(is, 8);
    	nextModeChange = FileIOStreamHelper.readData(is, 8);
    }
}
