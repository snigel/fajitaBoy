package fajitaboy.gb.memory;

import static fajitaboy.constants.AddressConstants.ADDRESS_CARTRIDGE_TYPE;
import static fajitaboy.constants.AddressConstants.ADDRESS_ERAM_END;
import static fajitaboy.constants.AddressConstants.ADDRESS_ERAM_START;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import fajitaboy.FileIOStreamHelper;

public class MBC5 implements MemoryBankController {

    Eram eram;
	ROM rom;
    int romBanks;
    int romBank;
    int ramBanks;
    int ramSize;
    int eramEnd;
    boolean ramEnable;

    public MBC5 (ROM cartridge){
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
        	ramBanks = 16;
        	ramSize = 0x20000;
        	eramEnd = ADDRESS_ERAM_END;
        	break;
        }
        
        if ( ramSize > 0 ) {
        	eram = new Eram(ADDRESS_ERAM_START, eramEnd, ramBanks);
        }
        System.out.println("ROM has "+romBanks+" banks. Size: " + (romBanks*16) + "KB");
        System.out.println("RAM has "+ramBanks+" banks. Size: " + (ramBanks*8) + "KB");
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
    	} else if (address >= ADDRESS_ERAM_START && address < eramEnd ) {
    		return eram.read(address);
    	}
    	return 0;
    }

    public void reset() {
        setRomBank(0);
        setRamBank(0);
        ramEnable = true;
    }
    
    private void setRomBank(int bank){
    	romBank = bank % romBanks;
        rom.setBank(romBank);
    }

    private void setRamBank(int bank){
    	if (eram != null)
    		eram.setBank(bank);
    }

    public void write(int address, int data) {
    	
    	if (address >= 0x0000 && address < 0x2000) {
    		if ( (data & 0x0F) == 0x0A ) {
    			ramEnable = true;
    		} else if ( (data & 0x0F) == 0x00 ) {
    			ramEnable = false;
    		}
    	} else if (address >= 0x2000 && address < 0x3000) {
   			setRomBank((romBank & 0x100 ) + data);
    	} else if (address >= 0x3000 && address < 0x4000) {
    		setRomBank((romBank & 0xFF ) + ((data & 0x01) << 8));
        } else if (address >= 0x4000 && address < 0x6000) {
            if ( data <= 0x0F ) {
            	// Map RAM bank
            	setRamBank(data & 0x0F);
            }
        } else if (address >= ADDRESS_ERAM_START && address < eramEnd && ramEnable ) {
  			eram.write(address, data);
    	}
    	
    }

    /**
     * {@inheritDoc}
     */
    public void readState( FileInputStream fis ) throws IOException {
    	rom.readState(fis);
    	eram.readState(fis);
    	romBanks = (int) FileIOStreamHelper.readData(fis, 4 );
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
    	FileIOStreamHelper.writeData(fos, (long) romBanks, 4 );
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
