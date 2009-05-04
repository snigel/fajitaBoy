package fajitaboy.gb.memory;

import static fajitaboy.constants.AddressConstants.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import fajitaboy.FileIOStreamHelper;
import fajitaboy.gb.StateMachine;


public class NoMBC implements MemoryBankController {
    Eram eram;
	ROM rom;
    boolean eramEnabled;

    public NoMBC (ROM cartridge){
        rom = cartridge;
        int ramType = rom.read(ADDRESS_CARTRIDGE_TYPE);
        switch ( ramType ) {
        case 2:
        	eram = new Eram(ADDRESS_ERAM_START, ADDRESS_ERAM_END, 1);
        	break;
        default:
        	eram = null;
        	break;
        }
        
        System.out.println("This rom has no banks");
    }

    public int forceRead(int address) {
        return read(address);
    }

    public void forceWrite(int address, int data) {
       rom.forceWrite(address, data);
    }

    public int read(int address) {
    	if ( address < 0x8000 ) {
    		return rom.read(address);
    	} else if ( eram != null ) { 
    		return eram.read(address);
    	}
    	return 0;
    }

    public void reset() {
    	rom.setBank(1);
    	eram.setBank(0);
    }

    public void write(int address, int data) {

    }

    /**
     * {@inheritDoc}
     */
    public void readState( FileInputStream fis ) throws IOException {
    	eramEnabled = FileIOStreamHelper.readBoolean(fis );
    	rom.readState(fis);
    	if ( eramEnabled ) {
    		eram.readState(fis);
    	}
    }
    
    /**
     * {@inheritDoc}
     */
    public void saveState( FileOutputStream fos ) throws IOException {
    	FileIOStreamHelper.writeBoolean(fos, eramEnabled );
    	rom.saveState(fos);
    	if ( eramEnabled ) {
    		eram.saveState(fos);
    	}
    }

	public Eram getEram() {
		return eram;
	}
	
	public ROM getRom() {
		return rom;
	}
}
