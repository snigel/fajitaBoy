package fajitaboy.memory;

import static fajitaboy.constants.AddressConstants.*;
import static fajitaboy.constants.HardwareConstants.*;
import fajitaboy.memory.DebugMemory;

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
public class AddressBus implements MemoryInterface {
    /**
     * The array which contains all the memory.
     */
    private MemoryInterface[] module = new MemoryInterface[GB_ADDRESS_SPACE];

    // All modules be declared here

    /**
     * The interrupt register part of the memory.
     */
    private InterruptRegister interruptRegister;
    /**
     * Only used for debug purposes, to return zero instead null.
     */
    private DebugMemory debug;
    /**
     * Work RAM bank 0.
     */
    private RamLow raml;
    /**
     * The echo part of the memory. Which is a exact replica of low ram.
     */
    private Echo echo;
    /**
     * The high RAM part of the memory.
     */
    private RamHigh ramh;
    /**
     * Work RAM bank 1.
     */
    private ROM rom;
    /**
     * The Video RAM part of the memory.
     */
    private Vram vram;
    /**
     * The Sprite Attribute Table.
     */
    private Oam oam;
    /**
     * The High RAM.
     *
     */
    private Hram hram;
    /**
     * The external RAM that resides in the cartridge.
     */
    private Eram eram;
    /**
     * All the input/output addresses.
     */
    private IO io;
    
    private MemoryInterface mbc;

    /**
     * Creates the addressbus and all the parts of the memory.
     *
     * @param romPath
     *            The ROM-file
     */
    public AddressBus(final String romPath) {
        // All modules must be initialized here
        debug = new DebugMemory();
        initialize(debug, DEBUG_START, DEBUG_END);

        io = new IO(IO_START, IO_END);
        initialize(io, IO_START, IO_END);

        ramh = new RamHigh(RAMH_START, RAMH_END);
        initialize(ramh, RAMH_START, RAMH_END);

        raml = new RamLow(RAML_START, RAML_END);
        initialize(raml, RAML_START, RAML_END);

        echo = new Echo(this, RAML_START, ECHO_START);
        initialize(echo, ECHO_START, ECHO_END);

        vram = new Vram(VRAM_START, VRAM_END);
        initialize(vram, VRAM_START, VRAM_END);

        hram = new Hram(HRAM_START, HRAM_END);
        initialize(hram, HRAM_START, HRAM_END);

        oam = new Oam(OAM_START, OAM_END, this);
        initialize(oam, OAM_START, OAM_END);
        
        rom = new ROM(CARTRIDGE_START, romPath);
        mbc = setMBC();
        
        initialize(mbc, CARTRIDGE_START, CARTRIDGE_END);
        
        interruptRegister = new InterruptRegister();
        module[INTERRUPT_ADDRESS] = interruptRegister;

        module[ADDRESS_DMA] = oam;
        eram = new Eram(ERAM_START, ERAM_END);
        initialize(eram, ERAM_START, ERAM_END);
    }

    private MemoryInterface setMBC(){
        switch(rom.getMBC()){
        case ROM: System.out.println("MBC: None"); return rom;
        case MBC1:System.out.println("MBC: MBC1");  return new MBC1(eram,rom);
        case MBC1_RAM: System.out.println("MBC: MBC1+RAM");  return new MBC1(eram, rom);
        case MBC1_RAM_BATTERY:System.out.println("MBC: MBC1+RAM+BAT");  return new MBC1(eram, rom);
        case MBC2: System.out.println("MBC: MBC2"); return new MBC2(eram, rom);
        case MBC2_BATTERY: System.out.println("MBC: MBC2"); return new MBC2(eram, rom);
        default: System.out.println("MBC not supported!");return rom;
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
    private void initialize(final MemoryInterface object,
            final int start, final int end) {
        for (int i = start; i < end; i++) {
            module[i] = object;
        }
    }

    /**
     * {@inheritDoc}
     */
    public int read(final int address) {
        if (address < 0 || address > module.length-1) {
            throw new ArrayIndexOutOfBoundsException("AddressBus.java: "+address);
        }
        return module[address].read(address);
    }


    /**
     * {@inheritDoc}
     */
    public void write(final int address, final int data) {
        if (address < 0 || address > module.length-1) {
            throw new ArrayIndexOutOfBoundsException("AddressBus.java: "+address);
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
        eram.reset();
        io.reset();
    }

    /**
     * DJ_BISSE WAS HERE
     * @return Returns 
     */
    public Vram getVram() {
    	return vram;
    }

    public IO.JoyPad getJoyPad() {
    	return io.getJoyPad();
    }

}
