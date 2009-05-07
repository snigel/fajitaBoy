package fajitaboy.gb.memory;

import static fajitaboy.constants.AddressConstants.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import fajitaboy.FileIOStreamHelper;
import fajitaboy.gb.StateMachine;

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

public class MBC3 implements MemoryBankController {
    Eram eram;
	ROM rom;
    int romBanks;
    int ramBanks;
    int ramSize;
    int eramEnd;
    boolean romBankingMode;
    boolean ramTimerEnable;
    
    // Variables for Real Time Clock
    int RTCSecs;
    int RTCMins;
    int RTCHours;
    int RTCDaysLo;
    int RTCDaysHi;
    int RTCRegisterSelect;
    boolean enableRTCReadWrite;
    Date prevDate; // Use ONLY for comparing Days passed (All other Date features are decrepit)

    public MBC3 (ROM cartridge){
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
    	} else if (address >= ADDRESS_ERAM_START && address < eramEnd && ramTimerEnable ) {
    		if ( enableRTCReadWrite ) {
    			return readRTC();
    		} else if ( eram != null ) {
    			return eram.read(address);
    		}
    	}
    	return 0;
    }

    public void reset() {
        setRomBank(1);
        setRamBank(0);
        romBankingMode = true;
        ramTimerEnable = false;
        enableRTCReadWrite = false;
        RTCSecs = 0;
        RTCMins = 0;
        RTCHours = 0;
        RTCDaysLo = 0;
        RTCDaysHi = 0;
        updateRTC();
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

    /**
     * Reads the previously selected RTC register
     * @return Register contents
     */
    private int readRTC() {
    	switch (RTCRegisterSelect) {
    	case 0x08: return RTCSecs; // Seconds
    	case 0x09: return RTCMins; // Minutes
    	case 0x0A: return RTCHours; // Hours
    	case 0x0B: return RTCDaysLo; // Days
    	case 0x0C: return RTCDaysHi; // Days Hi + Status Register
    	default: return 0;
    	}
    }
    
    /**
     * Writes to previously selected RTC register
     * @param data Data to write
     */
    private void writeRTC(int data) {
    	switch (RTCRegisterSelect) {
    	case 0x08: RTCSecs = data; break; // Seconds
    	case 0x09: RTCMins = data; break; // Minutes
    	case 0x0A: RTCHours = data; break; // Hours
    	case 0x0B: RTCDaysLo = data; break; // Days
    	case 0x0C: RTCDaysHi = data; break; // Days Hi + Status Register
    	}
    }
    
    /**
     * Updates the RTC registers with current time values
     */
    // TODO Support for Stop Timer bit in RTCDaysHi register
    public void updateRTC() {
    	// Init if first time executing
    	if ( prevDate == null ) {
    		prevDate = new Date();
    	}
    	
    	// Step days forward
    	Date currentDate = new Date();
    	int currentDay = RTCDaysLo + ((RTCDaysHi & 0x01) << 8);
    	int dayDiff = currentDate.compareTo(prevDate);
    	if ( dayDiff < 0 ) {
    		// Not a fatal error, but print an alert and do not update day.
    		System.out.println("Comparison error in MBC3 Real Time Clock, or, you have travelled backwards in time!");
    	} else {
    		currentDay += dayDiff;
    		if ( currentDay >= 512 ) {
    			RTCDaysHi |= 0x80;
    			currentDay %= 512;
    		}
    		RTCDaysLo = currentDay & 0xFF;
    		RTCDaysHi = (RTCDaysHi & 0xFE) + ((currentDay & 0x100) >>> 8);
    	}
    	
    	prevDate = currentDate;
    	
    	// Calculate the rest of the registers
    	RTCSecs = Calendar.SECOND;
    	RTCMins = Calendar.MINUTE;
    	RTCHours = Calendar.HOUR;
    }

    public void write(int address, int data) {
    	
    	if (address >= 0x0000 && address < 0x2000) {
    		if ( (data & 0x0F) == 0x0A ) {
    			ramTimerEnable = true;
    		} else {
    			ramTimerEnable = false;
    		}
    	} else if (address >= 0x2000 && address < 0x4000) {
    		if ( (data & 0x7F) == 0 ) {
    			setRomBank(1);
    		} else {
    			setRomBank(data & 0x7F);
    		}
        } else if (address >= 0x4000 && address < 0x6000) {
            if ( data <= 0x03 ) {
            	// Map RAM bank and select RAM
            	setRamBank(data & 0x03);
            	enableRTCReadWrite = false;
            } else if ( data >= 0x08 && data <= 0x0C ){
            	RTCRegisterSelect = data;
            	enableRTCReadWrite = true;
            }
        } else if (address >= 0x6000 && address < 0x8000) {
            if ( ( data & 0x01 ) == 1 ) {
            	updateRTC();
            }
        } else if (address >= ADDRESS_ERAM_START && address < eramEnd && ramTimerEnable ) {
    		if ( enableRTCReadWrite ) {
    			writeRTC(data);
    		} else if ( eram != null ) {
    			eram.write(address, data);
    		}
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
    	
    	// Load Real Time Clock
    	enableRTCReadWrite = FileIOStreamHelper.readBoolean(fis );
    	RTCSecs = (int) FileIOStreamHelper.readData(fis, 1 );
    	RTCMins = (int) FileIOStreamHelper.readData(fis, 1 );
    	RTCHours = (int) FileIOStreamHelper.readData(fis, 1 );
    	RTCDaysLo = (int) FileIOStreamHelper.readData(fis, 1 );
    	RTCDaysHi = (int) FileIOStreamHelper.readData(fis, 1 );
    	
    	// Load previous date
    	int prevTimeMillis = (int) FileIOStreamHelper.readData(fis, 8 );
    	prevDate = new Date(prevTimeMillis);
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
    	
    	// Save Real Time Clock
    	FileIOStreamHelper.writeBoolean(fos, enableRTCReadWrite );
    	FileIOStreamHelper.writeData(fos, (long) RTCSecs, 1 );
    	FileIOStreamHelper.writeData(fos, (long) RTCMins, 1 );
    	FileIOStreamHelper.writeData(fos, (long) RTCHours, 1 );
    	FileIOStreamHelper.writeData(fos, (long) RTCDaysLo, 1 );
    	FileIOStreamHelper.writeData(fos, (long) RTCDaysHi, 1 );
    	
    	// Save current date
    	FileIOStreamHelper.writeData(fos, System.currentTimeMillis(), 8);
    }

	public Eram getEram() {
		return eram;
	}
	
	public ROM getRom() {
		return rom;
	}
}
