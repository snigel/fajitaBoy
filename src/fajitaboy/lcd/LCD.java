package fajitaboy.lcd;

import static fajitaboy.constants.AddressConstants.*;
import static fajitaboy.constants.LCDConstants.*;

import fajitaboy.memory.AddressBus;
import fajitaboy.memory.MemoryInterface;

/**
 * @author Tobias Svensson
 */
public class LCD {

	/**
	 * Pointer to MemoryInterface class.
	 */
	private AddressBus ram;

	/**
	 * Contains the Background.
	 */
	private BackgroundMap bgm = new BackgroundMap();

	/**
	 * Contains the Window.
	 */
	private WindowMap wnd = new WindowMap();

	/**
	 * Contains the Sprite Attribute Table.
	 */
	private SpriteAttributeTable sat = new SpriteAttributeTable();

	/**
	 * Contains the screen pixels.
	 */
	private Screen screen = new Screen();

	/**
	 * Contains the LCDC flags.
	 */
	private LCDC lcdc;

	/**
	 * Flag is true if a new screen has been rendered.
	 */
	private boolean newScreen = false;

	/**
	 * If true, frame will not be rendered.
	 */
	public boolean frameSkip = false;

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
		// Abort if Frame Skip is activated
		if ( frameSkip )
			return;

		// Read OAM (Sprites, Tiles, Background Map...)
		lcdc.readLCDC();

		if (lcdc.lcdDisplayEnable) {

			/*
			 * Draw graphic objects
			 */
			int bgclr = ram.read(PALETTE_BG_DATA) & 0x03;
			int ly = ram.read(ADDRESS_LY);
			screen.clearLine(bgclr, ly);


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
			if (lcdc.objSpriteDisplay) {
				sat.readSpriteAttributes(ram);
				sat.draw(screen, true, ram, lcdc, ram.getVram(), ly);
			}

			// Read and draw background if enabled.            
			if (lcdc.bgDisplay) {
				bgm.readBackground(ram, lcdc);
				bgm.draw(screen, ram, ram.getVram(), ly);
			}

			// Read and draw window if enabled.
			if (lcdc.windowDisplayEnable) {
				wnd.readWindow(ram, lcdc);
				wnd.draw(screen, ram, ram.getVram(), ly);
			}

			// Read and draw sprites that are above bg & window, if sprites enabled.
			if (lcdc.objSpriteDisplay) {
				sat.draw(screen, false, ram, lcdc, ram.getVram(), ly);
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

		//change mode to "During v-blank"
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

	public boolean newScreenAvailable() {
		return newScreen;
	}

	public int[][] getScreen() {
		newScreen = false;
		return screen.getBits();
	}

}
