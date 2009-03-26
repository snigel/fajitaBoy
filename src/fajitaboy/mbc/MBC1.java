package fajitaboy.mbc;

import fajitaboy.memory.Cartridge;
import static fajitaboy.constants.CartridgeConstants.*;

/**
 * Memory Bank Controller of type MBC1
 *
 * @author Tobias Svensson
 *
 */

public class MBC1 implements MBCInterface {

	/**
	 * Size of ROM. Found at 0x148. The sizes are:
	 * 0x00 -  32KByte (No banks)
	 * 0x01 -  64KByte (4 banks)
	 * 0x02 - 128KByte (8 banks)
	 * 0x03 - 256KByte (16 banks)
	 * 0x04 - 512KByte (32 banks)
	 * 0x05 -   1MByte (64 banks)  - Only 63 are used by MBC1
	 * 0x06 -   2MByte (128 banks) - Only 125 are used by MBC1
	 * 0x07 -   4MByte (256 banks)
	 * 0x52 - 1.1MByte (72 banks)
	 * 0x53 - 1.2MByte (80 banks)
	 * 0x54 - 1.5MByte (96 banks)
	 */
	private int romSize;
	
	/**
	 * Size of battery-backed RAM. Found at 0x149. Sizes are:
	 * 0x00 - None
	 * 0x01 - 2kb  (A000-A7FF)
	 * 0x02 - 8kb
	 * 0x03 - 32kb (4 banks)
	 */
	private int ramSize;
	private int ramSizeInBytes;
	private int noOfRamBanks;
	private int noOfRomBanks;
	private int romBankNr;
	private int ramBankNr;
	private int ramBankUpperLimit;
	boolean ramEnable;
	
	/**
	 * Specifies whether bits written to 4000-5FFF should select
	 * upper two bits of ROM bank no, or RAM bank;
	 * 00h = ROM Banking Mode (up to 8KByte RAM, 2MByte ROM) (default)
  	 * 01h = RAM Banking Mode (up to 32KByte RAM, 512KByte ROM)
	 */
	boolean modeSelect;
	
	private int[][] romBanks;
	private int[][] ramBanks;
	
	private Cartridge cart;
	
	public MBC1(Cartridge cart, int[] cartBytes) {
		this.cart = cart;
		initialiseBanks(cart, cartBytes);
		reset();
	}
	
	private void initialiseBanks(Cartridge cart, int[] bytes) {
		
		romSize = bytes[0x148];
		ramSize = bytes[0x149];
		
		// Initialise ROM banks;
		noOfRomBanks = 1;
		switch ( romSize ) {
		case 0x00:	noOfRomBanks = 2; break;
		case 0x01:	noOfRomBanks = 4; break;
		case 0x02:	noOfRomBanks = 8; break;
		case 0x03:	noOfRomBanks = 16; break;
		case 0x04:	noOfRomBanks = 32; break;
		case 0x05:	noOfRomBanks = 64; break;
		case 0x06:	noOfRomBanks = 128; break;
		case 0x07:	noOfRomBanks = 256; break;
		case 0x52:	noOfRomBanks = 72; break;
		case 0x53:	noOfRomBanks = 80; break;
		case 0x54:	noOfRomBanks = 96; break;
		}
		
		romBanks = new int[noOfRomBanks][CART_BANKSIZE_ROM];
		int n = 0;
		for ( int i = 0; i < noOfRomBanks; i++ ) {
			for ( int j = 0; j < CART_BANKSIZE_ROM; j++ ) {
				romBanks[i][j] = bytes[n];
				n++;
			}
		}
		
		cart.setRomBankLow(romBanks[0]);
		cart.setRomBankHigh(romBanks[1]);
		romBankNr = 1;
		
		// Initialise RAM banks
		if ( bytes[0x147] == 0x01 ) {
			// No RAM
			ramSizeInBytes = 0;
			noOfRamBanks = 0;
			ramBankUpperLimit = 0xA000;
			ramBanks = null;
			cart.setRamBank(null);
		} else if ( ramSize == 0x00 ) {
			// 2K of RAM, 1 bank
			ramSizeInBytes = 0x2000;
			noOfRamBanks = 1;
			ramBankUpperLimit = 0xA800;
			ramBanks = new int[noOfRamBanks][ramSizeInBytes];
			ramBankNr = 0x00;
			cart.setRamBank(ramBanks[ramBankNr]);
		} else if ( ramSize == 0x01 ) {
			// 8K of RAM, 1 bank
			ramSizeInBytes = 0x8000;
			noOfRamBanks = 1;
			ramBankUpperLimit = 0xC000;
			ramBanks = new int[noOfRamBanks][ramSizeInBytes];
			ramBankNr = 0x00;
			cart.setRamBank(ramBanks[ramBankNr]);
		} else if ( ramSize == 0x02 ) {
			// 32K of RAM, 4 banks
			ramSizeInBytes = 0x8000;
			noOfRamBanks = 4;
			ramBankUpperLimit = 0xC000;
			ramBanks = new int[noOfRamBanks][ramSizeInBytes];
			ramBankNr = 0x00;
			cart.setRamBank(ramBanks[ramBankNr]);
		}
	}
	
	public void reset() {
		romBankNr = 0x01;
		ramBankNr = 0x00;
		ramEnable = false;
		modeSelect = false;
		if ( noOfRamBanks > 0 )
			ramBanks = new int[noOfRamBanks][ramSizeInBytes];
	}
	
	public void write(int address, int data) {
        if ( address >= 0xA000 & address < 0xC000 ) {
        	if ( address < ramBankUpperLimit && ramEnable ) {
        		int addr = address - 0xA000;
        		ramBanks[ramBankNr][addr] = data;
        	}
        } else if ( address >= 0x0000 && address <= 0x2000 ) {
        	// Enable or disable RAM
        	if ( (data & 0x0F) == 0x0A ) {
        		ramEnable = true;
        	} else {
        		ramEnable = false;
        	}
        } else if ( address >= 0x2000 && address < 0x4000 ) {
        	// Specify lower 5 bits or ROM bank
        	if ( (data & 0x1F ) == 0x00 ) {
        		romBankNr = (romBankNr & 0xE0) + 0x01;
        	} else {
        		romBankNr = (romBankNr & 0xE0) + (data & 0x1F);
        	}
        	setRomBank();
        } else if ( address >= 0x4000 && address < 0x6000 ) {
        	// Specify RAM bank, OR bits 5-6 of ROM bank no
        	if ( modeSelect ) {
        		ramBankNr = data & 0x03;
        		setRamBank();
        	} else {
        		romBankNr = (romBankNr & 0x1F) + (data & 0x03) * 0x20;
        		setRomBank();
        	}
        } else if ( address >= 0x6000 && address < 0x8000 ) {
        	// MODE select
        	if ( (data & 0x01) == 0 ) {
        		modeSelect = false;
        	} else {
        		modeSelect = true;
        	}
        	setRomBank();
        	setRamBank();
        } else {
            throw new ArrayIndexOutOfBoundsException("RamHigh.java");
        }
    }
	
	public int read( int address ) {
		if ( address >= 0x0000 && address < 0x4000 ) {
			return romBanks[0][address];
		} else if ( address >= 0x4000 && address < 0x8000 ) {
			int addr = address - 0x4000;
			return romBanks[romBankNr][addr];
		} else if ( address >= 0xA000 && address < 0xC000 ) {
			int addr = address - 0xA000;
			if ( address < ramBankUpperLimit && ramEnable ) {
				return ramBanks[ramBankNr][addr];	
			} else {
				return 0x00;
			}
		} else {
	        throw new ArrayIndexOutOfBoundsException("RamLow.java");
		}
	}
	
	public void forceWrite( int address, int data ) {
		if ( address >= 0x0000 && address < 0x4000 ) {
			romBanks[0][address] = data;
		} else if ( address >= 0x4000 && address < 0x8000 ) {
			int addr = address - 0x4000;
			romBanks[romBankNr][addr] = data;
		} else if ( address >= 0xA000 && address < 0xC000 ) {
			int addr = address - 0xA000;
			if ( address < ramBankUpperLimit ) {
				ramBanks[ramBankNr][addr] = data;	
			}
		} else {
	        throw new ArrayIndexOutOfBoundsException("RamLow.java");
		}
	}
	
	public void setRomBank() {
		if ( modeSelect ) {
			// Only banks 0x01 - 0x1f available
			cart.setRomBankHigh(romBanks[romBankNr & 0x1F]);
		} else {
			// All banks available
			cart.setRomBankHigh(romBanks[romBankNr]);
		}
	}
	
	public void setRamBank() {
		if ( ramBankNr == 0) {
			cart.setRamBank(null);
		} else if ( modeSelect ) {
			cart.setRamBank(ramBanks[0]);
		} else {
			cart.setRamBank(ramBanks[ramBankNr]);
		}
	}
}
