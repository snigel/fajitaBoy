package fajitaboy.gb.memory;

import static fajitaboy.constants.AddressConstants.*;
import static fajitaboy.constants.CartridgeConstants.*;
import static fajitaboy.constants.HardwareConstants.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import fajitaboy.FileIOStreamHelper;
import fajitaboy.constants.CartridgeConstants;
import fajitaboy.gb.StateMachine;
import fajitaboy.gb.memory.DebugMemory;

/**
 * Addressbus class that handles the whole address space. Through it all the
 * differents areas in the memory can be reached.
 *
 * @author Adam Hulin, Johan Gustafsson
 */
/**
 * @author Adam Hulin, Johan Gustafsson
 *
 */
public class AddressBus implements MemoryInterface, StateMachine {
    /**
     * The array which contains all the memory.
     */
    protected MemoryInterface[] module = new MemoryInterface[GB_ADDRESS_SPACE];

    // All modules be declared here

    /**
     * The interrupt register part of the memory.
     */
    protected InterruptRegister interruptRegister;
    /**
     * Only used for debug purposes, to return zero instead null.
     */
    protected DebugMemory debug;
    /**
     * Work RAM bank 0.
     */
    protected RamLow raml;
    /**
     * The echo part of the memory. Which is a exact replica of low ram.
     */
    protected Echo echo;
    /**
     * The high RAM part of the memory.
     */
    protected RamHigh ramh;
    /**
     * Work RAM bank 1.
     */
    protected ROM rom;
    /**
     * The Video RAM part of the memory.
     */
    protected Vram vram;
    /**
     * The Sprite Attribute Table.
     */
    protected Oam oam;
    /**
     * The High RAM.
     *
     */
    protected Hram hram;

    /**
     * All the input/output addresses.
     */
    protected IO io;
    
    protected MemoryBankController mbc;

    /**
     * Creates the addressbus and all the parts of the memory.
     *
     * @param romPath
     *            The ROM-file
     */
    public AddressBus(final String romPath) {
        initializeModule(romPath);
    }
    
    protected void initializeModule(final String romPath) {
        // All modules must be initialized here
        debug = new DebugMemory();
        initialize(debug, ADDRESS_DEBUG_START, ADDRESS_DEBUG_END);

        io = new IO(ADDRESS_IO_START, ADDRESS_IO_END);
        initialize(io, ADDRESS_IO_START, ADDRESS_IO_END);

        ramh = new RamHigh(ADDRESS_RAMH_START, ADDRESS_RAMH_END);
        initialize(ramh, ADDRESS_RAMH_START, ADDRESS_RAMH_END);

        raml = new RamLow(ADDRESS_RAML_START, ADDRESS_RAML_END);
        initialize(raml, ADDRESS_RAML_START, ADDRESS_RAML_END);

        echo = new Echo(this, ADDRESS_RAML_START, ADDRESS_ECHO_START);
        initialize(echo, ADDRESS_ECHO_START, ADDRESS_ECHO_END);

        vram = new Vram(ADDRESS_VRAM_START, ADDRESS_VRAM_END);
        initialize(vram, ADDRESS_VRAM_START, ADDRESS_VRAM_END);

        hram = new Hram(ADDRESS_HRAM_START, ADDRESS_HRAM_END);
        initialize(hram, ADDRESS_HRAM_START, ADDRESS_HRAM_END);

        oam = new Oam(ADDRESS_OAM_START, ADDRESS_OAM_END, this);
        initialize(oam, ADDRESS_OAM_START, ADDRESS_OAM_END);
        
        rom = new ROM(ADDRESS_CARTRIDGE_START, romPath);
        mbc = setMBC();
        initialize(mbc, ADDRESS_CARTRIDGE_START, ADDRESS_CARTRIDGE_END);
        initialize(mbc, ADDRESS_ERAM_START, ADDRESS_ERAM_END);
        
        interruptRegister = new InterruptRegister();
        module[ADDRESS_INTERRUPT] = interruptRegister;

        module[ADDRESS_DMA] = oam;
    }

    protected MemoryBankController setMBC(){
        switch(rom.getMBC()){
        case CART_TYPE_ROM: System.out.println("MBC: None"); return new NoMBC(rom);
        case CART_TYPE_ROM_RAM: System.out.println("MBC: ROM+RAM"); return new NoMBC(rom);
        case CART_TYPE_ROM_RAM_BATTERY: System.out.println("MBC: ROM+RAM+BATTERY"); return new NoMBC(rom);
        case CART_TYPE_MBC1:System.out.println("MBC: MBC1");  return new MBC1(rom);
        case CART_TYPE_MBC1_RAM: System.out.println("MBC: MBC1+RAM");  return new MBC1(rom);
        case CART_TYPE_MBC1_RAM_BATTERY:System.out.println("MBC: MBC1+RAM+BAT");  return new MBC1(rom);
        case CART_TYPE_MBC2: System.out.println("MBC: MBC2"); return new MBC2(rom);
        case CART_TYPE_MBC2_BATTERY: System.out.println("MBC: MBC2_BATTERY"); return new MBC2(rom);
        case CART_TYPE_MBC3_TIMER_BATTERY: System.out.println("MBC: MBC3+TIMER+BATTERY"); return new MBC3(rom);
        case CART_TYPE_MBC3_TIMER_RAM_BATTERY: System.out.println("MBC: MBC3+TIMER+RAM_BATTERY"); return new MBC3(rom);
        case CART_TYPE_MBC3: System.out.println("MBC: MBC3"); return new MBC3(rom);
        case CART_TYPE_MBC3_RAM: System.out.println("MBC: MBC3+RAM"); return new MBC3(rom);
        case CART_TYPE_MBC3_RAM_BATTERY: System.out.println("MBC: MBC3+RAM+BATTERY"); return new MBC3(rom);
        case CART_TYPE_MBC4: System.out.println("MBC: MBC4"); return null;
        case CART_TYPE_MBC4_RAM: System.out.println("MBC: MBC4+RAM"); return null;
        case CART_TYPE_MBC4_RAM_BATTERY: System.out.println("MBC: MBC4+RAM+BATTERY"); return null;
        case CART_TYPE_MBC5: System.out.println("MBC: MBC5"); return null;
        case CART_TYPE_MBC5_RAM: System.out.println("MBC: MBC5+RAM"); return null;
        case CART_TYPE_MBC5_RAM_BATTERY: System.out.println("MBC: MBC5+RAM+BATTERY"); return null;
        case CART_TYPE_MBC5_RUMBLE: System.out.println("MBC: MBC5+RUMBLE"); return null;
        case CART_TYPE_MBC5_RUMBLE_RAM: System.out.println("MBC: MBC5+RUMBLE+RAM"); return null;
        case CART_TYPE_MBC5_RUMBLE_RAM_BATTERY: System.out.println("MBC: MBC5+RUMBLE+RAM+BATTERY"); return null;
        case CART_TYPE_MMM01: System.out.println("MBC: MMM01"); return null;
        case CART_TYPE_MMM01_RAM: System.out.println("MBC: MMM01+RAM"); return null;
        case CART_TYPE_MMM01_RAM_BATTERY: System.out.println("MBC: MMM01+RAM+BATTERY"); return null;
        case CART_TYPE_POCKET_CAMERA: System.out.println("MBC: POCKET CAMERA"); return null;
        case CART_TYPE_BANDAI_TAMA5: System.out.println("MBC: BANDAI TAMA5"); return null;
        case CART_TYPE_HUC3: System.out.println("MBC: HuC3"); return null;
        case CART_TYPE_MUC1_RAM_BATTERY: System.out.println("MBC: HuC1+RAM+BATTERY"); return null;
        default: System.out.println("MBC not supported!"); return null;
        }
    }
    /**
     * @param object
     *            The type of memory that should reside in specified part of
     *            memory.
     * @param start
     *            The start address.
     * @param end
     *            The exclusive end address.
     */
    protected void initialize(final MemoryInterface object,
            final int start, final int end) {
        for (int i = start; i < end; i++) {
            module[i] = object;
        }
    }

    /**
     * {@inheritDoc}
     */
    public int read(int address) {
        if (address < 0 || address > module.length-1) {
            System.out.println("incorrect memory address "+address+" wrapping..");
            address &= 0xFFFF;
        }
        return module[address].read(address);
    }


    /**
     * {@inheritDoc}
     */
    public void write(int address, final int data) {
        if (address < 0 || address > module.length-1) {
            System.out.println("incorrect memory address "+address+" wrapping..");
            address &= 0xFFFF;
        }
        module[address].write(address, data);
    }

    /**
     * {@inheritDoc}
     */
    public int forceRead(final int address) {
        if (address < 0 || address > module.length) {
            throw new ArrayIndexOutOfBoundsException("Addressbus.java");
        }
        return module[address].forceRead(address);
    }

    /**
     * {@inheritDoc}
     */
    public void forceWrite(final int address, final int data) {
        if (address < 0 || address > module.length) {
            throw new ArrayIndexOutOfBoundsException("AddressBus.java");
        }
        module[address].forceWrite(address, data);
    }

    /**
     * Resets the memory to zero. The ROM is left untouched.
     */
    public final void reset() {
        interruptRegister.reset();
        debug.reset();
        raml.reset();
        echo.reset();
        ramh.reset();
        rom.reset();
        vram.reset();
        oam.reset();
        hram.reset();
        io.reset();
    }

    public Vram getVram() {
    	return vram;
    }

    public IO.JoyPad getJoyPad() {
    	return io.getJoyPad();
    }

    /**
     * {@inheritDoc}
     */
    public void readState( FileInputStream fis ) throws IOException {
    	interruptRegister.readState(fis);
        raml.readState(fis);
        echo.readState(fis);
        ramh.readState(fis);
        rom.readState(fis);
        vram.readState(fis);
        oam.readState(fis);
        hram.readState(fis);
        io.readState(fis);
    }
    
    /**
     * {@inheritDoc}
     */
    public void saveState( FileOutputStream fos ) throws IOException {
    	interruptRegister.saveState(fos);
        raml.saveState(fos);
        echo.saveState(fos);
        ramh.saveState(fos);
        rom.saveState(fos);
        vram.saveState(fos);
        oam.saveState(fos);
        hram.saveState(fos);
        io.saveState(fos);
    }
}
