package fajitaboy.gb.memory;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
     * 1. LŠs 0-16KB till low rom 2. LŠs 16KB -> slutet frŒn cartridge 3. Gšr en
     * metod fšr att returnera.
     */

    public ROM(final int start, final String romPath) {
        this.offset = start;
        setBank(1);
        readRom(romPath);
    }

    public int forceRead(int address) {
        return read(address);
    }

    public void forceWrite(int address, int data) {
        // Nej det får du inte.
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
        case 0:
            return 2;
        case 1:
            return 4;
        case 2:
            return 8;
        case 3:
            return 16;
        case 4:
            return 32;
        case 5:
            return 64;
        case 6:
            return 128;
        case 52:
            return 72;
        case 53:
            return 80;
        case 54:
            return 96;
        default:
            return 0;
        }
    }
    
    /**
     * This function reads a rom from a file into the ram array.
     * @param romPath
     *            is a text string containing the path to a game boy rom,
     *            located in file system.
     */

    private void readRom(final String romPath) {
        try {
            // Read ROM data from file

            
            DataInputStream dis;
            FileInputStream fis = new FileInputStream(romPath);
            ZipInputStream zis;
         // if zipfile
            // InputStream in=url.openStream ();
            
            if (romPath.substring(romPath.length() - 3, romPath.length())
                    .equals("zip")) {
                zis = new ZipInputStream(fis);
                ZipEntry entry = zis.getNextEntry();
                ram = new int[(int) entry.getSize()];
                dis = new DataInputStream(zis);
                for (int i = 0; i < ram.length; i++) {
                    ram[i] = dis.readUnsignedByte();
                }
                zis.close();
            } else {
                File romFile = new File(romPath);
                ram = new int[(int) romFile.length()];
                System.out.println("romfile length " + romFile.length());
              
                dis = new DataInputStream(fis);
                for (int i = 0; i < ram.length; i++) {
                    ram[i] = dis.readUnsignedByte();
                }
                fis.close();
            }

        } catch (Exception e) {
            System.out.println("Exception: " + e);
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
