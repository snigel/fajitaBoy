package fajitaboy.gb.memory;

import static fajitaboy.constants.AddressConstants.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import fajitaboy.FileIOStreamHelper;



public class MBC2 implements MemoryBankController {
    Eram eram;
	ROM rom;
    int romBank;
    int romBanks;
    boolean romBankingMode;
    boolean ramEnable;

    public MBC2 (ROM cartridge){
        rom = cartridge;
        romBanks = rom.getRomBanks();
        eram = new Eram4bit(ADDRESS_ERAM_START, ADDRESS_ERAM_END, 1);
        
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
    	} else if (address >= ADDRESS_ERAM_START && address < ADDRESS_ERAM_4BIT_END && ramEnable) {
    		return eram.read(address);
    	}
    	return 0;
    }

    public void reset() {
        setRomBank(1);
        ramEnable = true;
    }
    private void setRomBank(int bank){
        if (bank == 0) {
            rom.setBank(1);
        } else {
            rom.setBank(bank % romBanks);
        }
    }

    public void write(int address, int data) {
    	
    	if (address >= 0x0000 && address < 0x2000) {
    		/* Writing to this address is supposed to enable/disable RAM. However, Pan Docs
    		 * fails to explain which specific values enable and disable RAM. Therefore, RAM
    		 * is treated as always ON.
    		 */
    		// TODO Find information on this.
    	} else if (address >= 0x2000 && address < 0x4000) {
    		if ( (data & 0x10) == 0 ) {
    			setRomBank(data & 0x0F);
    		}
        } else if (address >= ADDRESS_ERAM_START && address < ADDRESS_ERAM_4BIT_END && ramEnable) {
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
    }
    
    /**
     * {@inheritDoc}
     */
    public void saveState( FileOutputStream fos ) throws IOException {
    	rom.saveState(fos);
    	eram.saveState(fos);
    	FileIOStreamHelper.writeData(fos, (long) romBank, 4 );
    	FileIOStreamHelper.writeData(fos, (long) romBanks, 4 );
    }

	public Eram getEram() {
		return eram;
	}
	
	public ROM getRom() {
		return rom;
	}
}
