package fajitaboy.mbc;

import fajitaboy.memory.Cartridge;
import static fajitaboy.constants.CartridgeConstants.*;
//Kommenterat bort ej kompilerande metoder. Alla commits ska kompilera!
//adam.


//public class MBC1_backup extends Cartridge {
public class MBC1_backup extends Cartridge{

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
	private int[] romBank;
	private int[] ramBank;
	
	public MBC1_backup(int start, String romPath) {
		super(start, romPath);
		romSize = ram[0x148];
		ramSize = ram[0x149];
		initialiseBanks();
	}
	
	/*public MBC1_backup(int start, int[] data) {
		offset = start;
		ram = data;
		romSize = ram[0x148];
		ramSize = ram[0x149];
		initialiseBanks();
		reset();
	}*/
	
	private void initialiseBanks() {
		// Initialise ROM banks;
		noOfRomBanks = 1;
		switch ( romSize ) {
		case 0x00:	noOfRomBanks = 1; break;
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
		
		if ( noOfRomBanks == 1 ) {
			romBanks = new int[noOfRomBanks][0x8000];
			int n = 0;
			for ( int i = 0; i < noOfRomBanks; i++ ) {
				for ( int j = 0; j < 0x8000; j++ ) {
					romBanks[i][j] = ram[n];
					n++;
				}
			}
		} else {
			romBanks = new int[noOfRomBanks][CART_BANKSIZE_ROM];
			int n = 0;
			for ( int i = 0; i < noOfRomBanks; i++ ) {
				for ( int j = 0; j < CART_BANKSIZE_ROM; j++ ) {
					romBanks[i][j] = ram[n];
					n++;
				}
			}
		}
		
		if ( noOfRomBanks > 1 )
			romBank = romBanks[romBankNr];
		else
			romBank = new int[0x4000];
		
		// Initialise RAM banks
		if ( ramSize == 0x00 ) {
			ramSizeInBytes = 0x2000;
			noOfRamBanks = 1;
			ramBankUpperLimit = 0xA800;
			ramBanks = new int[noOfRamBanks][ramSizeInBytes];
		} else if ( ramSize == 0x01 ) {
			ramSizeInBytes = 0x8000;
			noOfRamBanks = 1;
			ramBankUpperLimit = 0xC000;
			ramBanks = new int[noOfRamBanks][ramSizeInBytes];
		} else if ( ramSize == 0x02 ) {
			ramSizeInBytes = 0x8000;
			noOfRamBanks = 4;
			ramBankUpperLimit = 0xC000;
			ramBanks = new int[noOfRamBanks][ramSizeInBytes];
		}
		
		ramBankNr = 0x00;
		ramBank = ramBanks[ramBankNr];
	}
	
	/*public void reset() {
		romBankNr = 0x01;
		ramBankNr = 0x00;
		ramEnable = false;
		modeSelect = false;
		ramBanks = new int[noOfRamBanks][ramSizeInBytes];
	}
	
	public void write(int address, int data) {
        if ( address >= 0xA000 & address < 0xC000 ) {
        	if ( address < ramBankUpperLimit && ramEnable ) {
        		int addr = address - 0xA000;
        		ramBank[addr] = data;
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
    }*/
	
	/*public int read( int address ) {
		if ( address >= 0x0000 && address < 0x4000 ) {
			return romBanks[0][address];
		} else if ( address >= 0x4000 && address < 0x8000 ) {
			int addr = address - 0x4000;
			return romBank[addr];
		} else if ( address >= 0xA000 && address < 0xC000 ) {
			int addr = address - 0xA000;
			if ( address < ramBankUpperLimit && ramEnable ) {
				return ramBank[addr];	
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
			romBank[addr] = data;
		} else if ( address >= 0xA000 && address < 0xC000 ) {
			int addr = address - 0xA000;
			if ( address < ramBankUpperLimit ) {
				ramBank[addr] = data;	
			}
		} else {
	        throw new ArrayIndexOutOfBoundsException("RamLow.java");
		}
	}*/
	
	/*public int forceRead( int address ) {
		return read(address);
	}
	
	private void setRomBank() {
		if ( modeSelect ) {
			// Only banks 0x01 - 0x1f available
			romBank = romBanks[romBankNr & 0x1F];
		} else {
			// All banks available
			romBank = romBanks[romBankNr];
		}
	}
	*/
	private void setRamBank() {
		if ( modeSelect ) {
			ramBank = ramBanks[0];
		} else {
			ramBank = ramBanks[ramBankNr];
		}
	}
}
