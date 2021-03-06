package fajitaboy.gb.memory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import fajitaboy.FileIOStreamHelper;
import fajitaboy.gb.StateMachine;

import static fajitaboy.constants.AddressConstants.*;

public class ROM implements MemoryInterface, MemoryBankInterface, StateMachine {

    /**
     * This array holds the memory space of RAM.
     */
    protected int[] ram;

    /**
     * The offset value is used for subtracting the high incoming addresses to a
     * value starting at zero. This way the class can start using the array from
     * ram[0] instead of the offset value.
     */
    protected int offset;

    /**
     * Chooses which Rom bank to use. Default is 1.
     */
    private int bank;

    public void setBank(int bank) { // For MBC
        this.bank = bank;
    }

    /**
     * 1. Read 0-16 KB to low rom
     * 2. Read 16 kb and forard from cartridge 3
     * 3. Make a method for returning
     */
    public ROM(final int start, final int[] cartridge ) {
    	ram = cartridge;
        this.offset = start;
        setBank(1);
    }

    public int forceRead(int address) {
        return read(address);
    }

    public void forceWrite(int address, int data) {
        // No, you cant.
    }

    public int read(int address) {
        if (address < 0x4000)
            return ram[address];
        else
            return ram[address + (bank - 1) * 0x4000];
    }

    public void reset() {
        // Reset not necessary ignore
    }

    public void write(int address, int data) {
        // writing not allowed. ignore.
    }

    public int getMBC() {
        return ram[ADDRESS_CARTRIDGE_TYPE];
    }

    public int getRomBanks() {
        switch (ram[ADDRESS_ROM_SIZE]) {
        case 0x00:
            return 2;
        case 0x01:
            return 4;
        case 0x02:
            return 8;
        case 0x03:
            return 16;
        case 0x04:
            return 32;
        case 0x05:
            return 64;
        case 0x06:
            return 128;
        case 0x07:
            return 256;
        case 0x52:
            return 72;
        case 0x53:
            return 80;
        case 0x54:
            return 96;
        default:
            return 0;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void readState( FileInputStream fis ) throws IOException {
    	bank = (int) FileIOStreamHelper.readData( fis, 4 );
    	
    	// No need to write the ROM itself to the save state!!
    	/*
    	int length = (int) FileIOStreamHelper.readData( fis, 4 );
    	offset = (int) FileIOStreamHelper.readData( fis, 4 );
    	ram = new int[length];
    	for ( int i = 0; i < length; i++ ) {
    		ram[i] = (int) FileIOStreamHelper.readData( fis, 1 );
    	} */
    }
    
    /**
     * {@inheritDoc}
     */
    public void saveState( FileOutputStream fos ) throws IOException {
    	FileIOStreamHelper.writeData( fos, (long) bank, 4 );
    	
    	// No need to write the ROM itself to the save state!!
    	/*
    	FileIOStreamHelper.writeData( fos, (long) ram.length, 4 );
    	FileIOStreamHelper.writeData( fos, (long) offset, 4 );
    	for ( int i = 0; i < ram.length; i++ ) {
    		FileIOStreamHelper.writeData( fos, ram[i], 1 );
    	} */
    }
}
