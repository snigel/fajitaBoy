package fajitaboy.constants;

public class CartridgeConstants {
	public static final int CART_ROM = 0x01;
	public static final int CART_MBC1 = 0x01;
	public static final int CART_BANKSIZE_ROM = 0x4000;
	public static final int CART_BANKSIZE_RAM = 0x2000;
	public static final int CART_BANKSIZE_RAM_2K = 0x0800;
	
	// Cartridge types
	public static final int CART_TYPE_ROM = 0x00;
	public static final int CART_TYPE_MBC1 = 0x01;
	public static final int CART_TYPE_MBC1_RAM = 0x02;
	public static final int CART_TYPE_MBC1_RAM_BATTERY = 0x03;
	public static final int CART_TYPE_MBC2 = 0x05;
	public static final int CART_TYPE_MBC2_BATTERY = 0x06;
	public static final int CART_TYPE_ROM_RAM = 0x08;
	public static final int CART_TYPE_ROM_RAM_BATTERY = 0x09;
	public static final int CART_TYPE_MMM01 = 0x0B;
	public static final int CART_TYPE_MMM01_RAM = 0x0C;
	public static final int CART_TYPE_MMM01_RAM_BATTERY = 0x0D;
	public static final int CART_TYPE_MBC3_TIMER_BATTERY = 0x0F;
	public static final int CART_TYPE_MBC3_TIMER_RAM_BATTERY = 0x10;
	public static final int CART_TYPE_MBC3 = 0x11;
	public static final int CART_TYPE_MBC3_RAM = 0x12;
	public static final int CART_TYPE_MBC3_RAM_BATTERY = 0x13;
	public static final int CART_TYPE_MBC4 = 0x15;
	public static final int CART_TYPE_MBC4_RAM = 0x16;
	public static final int CART_TYPE_MBC4_RAM_BATTERY = 0x17;
	public static final int CART_TYPE_MBC5 = 0x19;
	public static final int CART_TYPE_MBC5_RAM = 0x1A;
	public static final int CART_TYPE_MBC5_RAM_BATTERY = 0x1B;
	public static final int CART_TYPE_MBC5_RUMBLE = 0x1C;
	public static final int CART_TYPE_MBC5_RUMBLE_RAM = 0x1D;
	public static final int CART_TYPE_MBC5_RUMBLE_RAM_BATTERY = 0x1E;
	public static final int CART_TYPE_POCKET_CAMERA = 0xFC;
	public static final int CART_TYPE_BANDAI_TAMA5 = 0xFD;
	public static final int CART_TYPE_HUC3 = 0xFE;
	public static final int CART_TYPE_MUC1_RAM_BATTERY = 0xFF;
}
