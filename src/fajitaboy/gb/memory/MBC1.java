package fajitaboy.gb.memory;

import static fajitaboy.constants.AddressConstants.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import fajitaboy.FileIOStreamHelper;

/**
 * Todo:
 *
 * 1. Setup correct bank for memory
 * 2. Setup memory mode
 * 3. Emulate Eram through MBC?
 * Count number of banks, make sure that it doesn't call on more.
 *
 * @author snigel
 *
 */

/**
 * The MBC1 defaults to 16Mbit ROM/8KByte RAM mode 
  on power up. Writing a value (XXXXXXXS - X = Don't 
  care, S = Memory model select) into 6000-7FFF area 
  will select the memory model to use. S = 0 selects 
  16/8 mode. S = 1 selects 4/32 mode.
 */

public class MBC1 implements MemoryBankController {
    Eram eram;
	ROM rom;
    int romBank;
    int romBanks;
    int ramBank;
    int ramBanks;
    int ramSize;
    int eramEnd;
    boolean romBankingMode;
    boolean ramEnable;

    public MBC1 (ROM cartridge){
        rom = cartridge;
        romBanks = rom.getRomBanks();
        int ramType = rom.read(ADDRESS_CARTRIDGE_TYPE);
        
        switch ( ramType ) {
        case 1:
        	ramBanks = 1;
        	ramSize = 0x0800;
        	eramEnd = ADDRESS_ERAM_START + 0x0800;
        	break;
        case 2:
        	ramBanks = 1;
        	ramSize = 0x2000;
        	eramEnd = ADDRESS_ERAM_END;
        	break;
        case 3:
        	ramBanks = 4;
        	ramSize = 0x8000;
        	eramEnd = ADDRESS_ERAM_END;
        	break;
        default:
        	ramBanks = 0;
        	ramSize = 0;
        	eramEnd = ADDRESS_ERAM_START;
        	break;
        }
        
        if ( ramSize > 0 ) {
        	eram = new Eram(ADDRESS_ERAM_START, eramEnd, ramBanks);
        }
        System.out.println("This rom has "+romBanks+" banks");
    }

    public int forceRead(int address) {
        return read(address);
    }

    public void forceWrite(int address, int data) {
       rom.forceWrite(address, data);
    }

    public int read(int address) {
    	if ( address >= 0x0000 && address < 0x8000 ) {
    		return rom.read(address);
    	} else if (address >= ADDRESS_ERAM_START && address < eramEnd && ramEnable && eram != null) {
    		return eram.read(address);
    	}
    	return 0;
    }

    public void reset() {
        setRomBank(1);
        setRamBank(0);
        romBankingMode = true;
        ramEnable = false;
    }
    private void setRomBank(int bank){
    	bank = bank % romBanks;
        if (bank == 0) {
            rom.setBank(1);
        } else {
            rom.setBank(bank);
        }
    }
    private void setRamBank(int bank){
    	if (eram != null)
    		eram.setBank(bank);
    }


    public void write(int address, int data) {
    	
    	if (address >= 0x0000 && address < 0x2000) {
    		if ( (data & 0x0F) == 0x0A ) {
    			ramEnable = true;
    		} else {
    			ramEnable = false;
    		}
    	} else if (address >= 0x2000 && address < 0x4000) {
    		if ( (data & 0x1F) == 0 ) {
    			setRomBank((romBank & 0xE0) + 1);
    		} else {
    			setRomBank((romBank & 0xE0) + (data & 0x1F));
    		}
        } else if (address >= 0x4000 && address < 0x6000) {
            if ( romBankingMode ) {
            	setRomBank(((data & 0x03) << 5) + (romBank & 0x1F));
            } else if (eram != null) {
            	setRamBank(data & 0x03);
            }
        } else if (address >= 0x6000 && address < 0x8000) {
            romBankingMode = ((data & 0x01) == 0);
        } else if (address >= ADDRESS_ERAM_START && address < eramEnd && ramEnable && eram != null ) {
        	eram.write(address, data);
        }
    	
    }

    /**
     * {@inheritDoc}
     */
    public void readState( FileInputStream fis ) throws IOException {
    	rom.readState(fis);
    	eram.readState(fis);
    	romBank = (int) FileIOStreamHelper.readData(fis, 4 );
    	romBanks = (int) FileIOStreamHelper.readData(fis, 4 );
    	ramBank = (int) FileIOStreamHelper.readData(fis, 4 );
    	ramBanks = (int) FileIOStreamHelper.readData(fis, 4 );
    	ramSize = (int) FileIOStreamHelper.readData(fis, 4 );
    	eramEnd = (int) FileIOStreamHelper.readData(fis, 4 );
    }
    
    /**
     * {@inheritDoc}
     */
    public void saveState( FileOutputStream fos ) throws IOException {
    	rom.saveState(fos);
    	eram.saveState(fos);
    	FileIOStreamHelper.writeData(fos, (long) romBank, 4 );
    	FileIOStreamHelper.writeData(fos, (long) romBanks, 4 );
    	FileIOStreamHelper.writeData(fos, (long) ramBank, 4 );
    	FileIOStreamHelper.writeData(fos, (long) ramBanks, 4 );
    	FileIOStreamHelper.writeData(fos, (long) ramSize, 4 );
    	FileIOStreamHelper.writeData(fos, (long) eramEnd, 4 );
    }

	public Eram getEram() {
		return eram;
	}
	
	public ROM getRom() {
		return rom;
	}
}
