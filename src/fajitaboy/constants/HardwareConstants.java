package fajitaboy.constants;

// Constant prefix GB/CGB shows which hardware type the constant regards.

public final class HardwareConstants {
	public static final int GB_CLOCK_FREQUENCY = 4194304; // MHz
	public static final int GB_CYCLES_PER_LINE = 456;
	public static final int GB_CYCLES_PER_FRAME = 70224;  // OBS! Only true if no write to LY occurs!
	public static final int GB_NANOS_PER_FRAME = 16742706;
	public static final int GB_VBLANK_LINE = 144;
	public static final int GB_VBLANK_PERIOD = 4560;
	public static final int GB_HBLANK_PERIOD = 207;
	public static final int GB_LCD_TRANSFER_PERIOD = 77;
	public static final int GB_LCD_OAMSEARCH_PERIOD = 172;
	public static final int GB_TIMER_CLOCK_0 = 1024;
	public static final int GB_TIMER_CLOCK_1 = 16;
	public static final int GB_TIMER_CLOCK_2 = 64;
	public static final int GB_TIMER_CLOCK_3 = 256;
	public static final int GB_DIV_CLOCK = 64;
	public static final int GB_ADDRESS_SPACE = 0x10000;
	public static final int GB_SPRITES = 192;
    public static final int GB_SPRITE_ATTRIBUTES = 40;
	public static final int GB_TILES = 192;
	public static final int GB_TILE_W = 8;
	public static final int GB_TILE_H = 8;
	public static final int CGB_TILES = 384;
	
}
