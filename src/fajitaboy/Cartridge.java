package fajitaboy;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

import fajitaboy.mbc.MBC1;
import fajitaboy.mbc.MBCInterface;
import fajitaboy.mbc.RomOnly;

/**
 * Represents the memory which the cartridges ROM is saved.
 *
 * @author Adam Hulin, Johan Gustafsson
 *
 */
public class Cartridge extends MemoryComponent {
    /**
     * @param start
     *            , address representing where vram begins in memory space
     * @param romPath
     *            , is the location of a gameboy rom located in file system
     */
	
	/**
	 * ROM bank occupying 0x0000-0x3FFF
	 */
	int[] romBankLo;
	
	/**
	 * ROM bank occupying 0x4000-0x7FFF
	 */
	int[] romBankHi;
	
	/**
	 * RAM bank occupying 0xA000-0xBFFF
	 */
	int[] ramBank;
	
	/**
	 * Upper address limit of RAM bank
	 */
	int ramBankUpperLimit;
	
	/**
	 * Whether access to RAM is enable
	 */
	boolean ramEnable;
	
	/**
	 * Bytes read from .gb file
	 */
	int[] bytes;
	
	/**
	 * Memory bank controller
	 */
	MBCInterface mbc;
	
	public Cartridge() {
		
	}
	
    public Cartridge(final int start, final String romPath) {
        this.offset = start;
        readRom(romPath);
    }
    
    public void setRomBankLo(int[] bank) {
    	romBankLo = bank;
    }
    
    public void setRomBankHi(int[] bank) {
    	romBankHi = bank;
    }
    
    public void setRamBank(int[] bank) {
    	ramBank = bank;
    	if ( bank == null ) {
    		// Prevent RAM access
    		ramBankUpperLimit = 0xA000;
    	} else {
    	    ramBankUpperLimit = bank.length + 0xA000;
    	}
    }

    /**
     * {@inheritDoc}
     */
    public void write(final int address, final int data) {
        if ( address >= 0xA000 && address < 0xC000 ) {
        	if ( address < ramBankUpperLimit ) {
        		ramBank[address - 0xA000] = data;
        	}
        } else {
        	mbc.write(address, data);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public int read( int address ) {
		if ( address >= 0x0000 && address < 0x4000 ) {
			return romBankLo[address];
		} else if ( address >= 0x4000 && address < 0x8000 ) {
			int addr = address - 0x4000;
			return romBankHi[addr];
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
    
    /**
     * {@inheritDoc}
     */
    public int forceRead( int address ) {
    	if ( address >= 0x0000 && address < 0x4000 ) {
			return romBankLo[address];
		} else if ( address >= 0x4000 && address < 0x8000 ) {
			int addr = address - 0x4000;
			return romBankHi[addr];
		} else if ( address >= 0xA000 && address < 0xC000 ) {
			int addr = address - 0xA000;
			if ( address < ramBankUpperLimit ) {
				return ramBank[addr];	
			} else {
				return 0x00;
			}
		} else {
	        throw new ArrayIndexOutOfBoundsException("RamLow.java");
		}
	}
    
    /**
     * {@inheritDoc}
     */
    public void forceWrite( int address, int data ) {
		if ( address >= 0x0000 && address < 0x4000 ) {
			romBankLo[address] = data;
		} else if ( address >= 0x4000 && address < 0x8000 ) {
			int addr = address - 0x4000;
			romBankHi[addr] = data;
		} else if ( address >= 0xA000 && address < 0xC000 ) {
			int addr = address - 0xA000;
			if ( address < ramBankUpperLimit ) {
				ramBank[addr] = data;	
			}
		} else {
	        throw new ArrayIndexOutOfBoundsException("RamLow.java");
		}
	}
    
    /**
     * {@inheritDoc}
     */
    public void reset() {
        ramEnable = false;
        mbc.reset();
    }

    /**
     * This function reads a rom from a file into the ram array.
     *
     * @param romPath
     *            is a text string containing the path to a gameboy rom, located
     *            in file system.
     */
    private void readRom(final String romPath) {
        try {
        	// Read ROM data from file
            File romFile = new File(romPath);
            ram = new int[(int) romFile.length()];
            FileInputStream fis = new FileInputStream(romFile);
            DataInputStream dis = new DataInputStream(fis);

            for (int i = 0; i < ram.length; i++) {
                ram[i] = dis.readUnsignedByte();
            }

            fis.close();
            
            // Create MBC
            createMBC();
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }
    }

    private void createMBC() {
    	int type = ram[0x147];
    	if ( type == 0x00 ) {
    		// No MBC
    		mbc = new RomOnly(this, ram);
    	} else if ( type == 0x01 || type == 0x02 || type == 0x03 ) {
    		mbc = new MBC1(this, ram);
    	}
    } 
}
