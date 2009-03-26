package fajitaboy.memory;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;


import static fajitaboy.constants.AddressConstants.*;
import static fajitaboy.constants.HardwareConstants.*;
import fajitaboy.memory.MemoryComponent;
import fajitaboy.mbc.MBC1;
import fajitaboy.mbc.MBCInterface;
import fajitaboy.mbc.RomOnly;

/**
 * Represents the memory which the cartridges ROM is saved.
 * @author Adam Hulin, Johan Gustafsson
 */
public class Cartridge implements MemoryInterface {
    /**
     * @param start
     *            , address representing where vram begins in memory space
     * @param romPath
     *            , is the location of a gameboy rom located in file system
     */
    /**
     * The size of the ram array.
     */
    protected int length;

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
     * ROM bank occupying ROMLOW_START-0x3FFF.
     */
    private int[] romBankLow;

    /**
     * ROM bank occupying 0x4000-0x7FFF.
     */
    private int[] romBankHigh;

    /**
     * RAM bank occupying ERAM_START-0xBFFF.
     */
    private int[] ramBank;

    /**
     * Upper address limit of RAM bank.
     */
    private int ramBankUpperLimit;

    /**
     * Whether access to RAM is enabled or not.
     */
    private boolean ramEnable;

     /**
     * Bytes read from .gb file.
     */
    int[] bytes;

    /**
     * Memory bank controller.
     */
    private MBCInterface mbc;

    public Cartridge(final int start, final String romPath) {
        this.offset = start;
        readRom(romPath);
        mbc = createMBC();
    }

    public final void setRomBankLow(int[] bank) {
        romBankLow = bank;
    }

    public final void setRomBankHigh(int[] bank) {
        romBankHigh = bank;
    }

    public final void setRamBank(int[] bank) {
        ramBank = bank;
        if (bank == null) {
            // Prevent RAM access
            ramBankUpperLimit = ERAM_START;
        } else {
            ramBankUpperLimit = bank.length + ERAM_START;
        }
    }

    /**
     * {@inheritDoc}
     */
    public final void write(final int address, final int data) {
        if (address >= ERAM_START && address < ERAM_END) {
            if (address < ramBankUpperLimit) {
                ramBank[address - ERAM_START] = data;
            }
        } else {
            mbc.write(address, data);
        }
    }

    /**
     * {@inheritDoc}
     */
    public final int read(final int address) {
        if (address >= ROMLOW_START && address < ROMLOW_END) {
            return romBankLow[address];
        } else if (address >= ROMHIGH_START && address < ROMHIGH_END) {
            return romBankHigh[address - ROMHIGH_START];

        } else if (address >= ERAM_START && address < ERAM_END) {

            if (address < ramBankUpperLimit && ramEnable) {
                return ramBank[address - ERAM_START];
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
    public final int forceRead(final int address) {

        if (address >= ROMLOW_START && address < ROMLOW_END) {
            return romBankLow[address];

        } else if (address >= ROMHIGH_START && address < ROMHIGH_END) {
            return romBankHigh[address - ROMHIGH_START];

        } else if (address >= ERAM_START && address < ERAM_END) {

            if (address < ramBankUpperLimit) { // Make sure there is no out of
                // bounds
                return ramBank[address - ERAM_START];
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
    public final void forceWrite(final int address, final int data) {
        if (address >= ROMLOW_START && address < ROMLOW_END) {
            romBankLow[address] = data;
        } else if (address >= ROMHIGH_START && address < ROMHIGH_END) {
            int addr = address - ROMHIGH_START;
            romBankHigh[addr] = data;
        } else if (address >= ERAM_START && address < ERAM_END) {
            int addr = address - ERAM_START;
            if (address < ramBankUpperLimit) {
                ramBank[addr] = data;
            }
        } else {
            throw new ArrayIndexOutOfBoundsException("RamLow.java");
        }
    }

    /**
     * {@inheritDoc}
     */
    public final void reset() {
        ramEnable = false;
        mbc.reset();
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
            File romFile = new File(romPath);
            ram = new int[(int) romFile.length()];
            FileInputStream fis = new FileInputStream(romFile);
            DataInputStream dis = new DataInputStream(fis);

            for (int i = 0; i < ram.length; i++) {
                ram[i] = dis.readUnsignedByte();
            }

            fis.close();

        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }
    }

    /**
     * Choose right MBC type.
     * @return Returns the correct MBC type.
     */
    private MBCInterface createMBC() {
        int type = ram[CARTRIDGE_TYPE];
        switch (type) {
        case ROM:
            return new RomOnly(this, ram);
            case MBC1:
            return new MBC1(this, ram);
        case MBC1_RAM:
            return new MBC1(this, ram);
        case MBC1_RAM_BATTERY:
            return new MBC1(this, ram);
        default: // No supported MBC do something fail like trying MBC1
            return new MBC1(this, ram);
        }
    }
}
