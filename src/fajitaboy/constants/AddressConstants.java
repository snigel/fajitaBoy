package fajitaboy.constants;

public final class AddressConstants {

	public static final int ADDRESS_IE = 0xFFFF;
	public static final int ADDRESS_IF = 0xFF0F;
	
	public static final int ADDRESS_INT_VBLANK = 0x0040;
	public static final int ADDRESS_INT_LCDSTAT = 0x0048;
	public static final int ADDRESS_INT_TIMER = 0x0050;
	public static final int ADDRESS_INT_SERIAL = 0x0058;
	public static final int ADDRESS_INT_JOYPAD = 0x0060;
	
	public static final int ADDRESS_DIV = 0xFF04;
	public static final int ADDRESS_TIMA = 0xFF05;
	public static final int ADDRESS_TMA = 0xFF06;
	public static final int ADDRESS_TAC = 0xFF07;
	
	public static final int ADDRESS_STAT = 0xFF41;
	public static final int ADDRESS_SCY = 0xFF42;
	public static final int ADDRESS_SCX = 0xFF43;
	public static final int ADDRESS_LY = 0xFF44;
	public static final int ADDRESS_LYC = 0xFF45;
	public static final int ADDRESS_WY = 0xFF4A;
	public static final int ADDRESS_WX = 0xFF4B;
	
	public static final int DEBUG_START = 0x0000;
	public static final int DEBUG_END = 0x10000;
	public static final int CARTRIDGE_START = 0x0000;
	public static final int CARTRIDGE_END = 0x8000;
	public static final int VRAM_START = 0x8000;
	public static final int VRAM_END = 0xA000;
	public static final int ERAM_START = 0xA000;
	public static final int ERAM_END = 0xC000;
	public static final int RAML_START = 0xC000;
	public static final int RAML_END = 0xD000;
	public static final int RAMH_START = 0xD000;
	public static final int RAMH_END = 0xE000;
	public static final int ECHO_START = 0xE000;
	public static final int ECHO_END = 0xFE00;
	public static final int OAM_START = 0xFE00;
	public static final int OAM_END = 0xFEA0;
	public static final int IO_START = 0xFF00;
	public static final int IO_END = 0xFF80;
	public static final int HRAM_START = 0xFF80;
	public static final int HRAM_END = 0xFFFF;
	public static final int INTERRUPT_ADDRESS = 0xFFFF;
	
}
