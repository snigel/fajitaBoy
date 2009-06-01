package fajitaboy.gbc.memory;

import static fajitaboy.constants.AddressConstants.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import fajitaboy.gb.memory.AddressBus;
import fajitaboy.gb.memory.DebugMemory;
import fajitaboy.gb.memory.Echo;
import fajitaboy.gb.memory.Hram;
import fajitaboy.gb.memory.InterruptRegister;
import fajitaboy.gb.memory.PaletteMemory;
import fajitaboy.gb.memory.ROM;
import fajitaboy.gb.memory.RamLow;

/**
 * AddressBus in CGB mode. 
 */
/*
 * A problem is that you don't want to open the file and look if it's a CGB game.
 * The ROM reads the file but is created by the AddressBus so it's too late.
 * One way is to create the ROM from outside and give it as an argument to the 
 * constructor. Maybe that is not very nice. Another way is to have one AddressBus
 * that handles both cases but it feels wrong. xD
 */
public class CGB_AddressBus extends AddressBus {

    private PaletteMemory backgroundPaletteMemory;
    private PaletteMemory spritePaletteMemory;
    
    private CGB_RamHigh ramh;
    private CGB_Vram vram;
    
    public CGB_AddressBus(final int[] cartridge) {
        super(cartridge);
    }
    
    protected void initializeModule(final int[] cartridge) {
        
        // All modules must be initialised here
        debug = new DebugMemory();
        initialize(debug, ADDRESS_DEBUG_START, ADDRESS_DEBUG_END);

        ramh = new CGB_RamHigh(ADDRESS_RAMH_START, ADDRESS_RAMH_END);
        initialize(ramh, ADDRESS_RAMH_START, ADDRESS_RAMH_END);

        raml = new RamLow(ADDRESS_RAML_START, ADDRESS_RAML_END);
        initialize(raml, ADDRESS_RAML_START, ADDRESS_RAML_END);

        echo = new Echo(this, ADDRESS_RAML_START, ADDRESS_ECHO_START);
        initialize(echo, ADDRESS_ECHO_START, ADDRESS_ECHO_END);

        vram = new CGB_Vram(ADDRESS_VRAM_START, ADDRESS_VRAM_END, this);
        initialize(vram, ADDRESS_VRAM_START, ADDRESS_VRAM_END);

        hram = new Hram(ADDRESS_HRAM_START, ADDRESS_HRAM_END);
        initialize(hram, ADDRESS_HRAM_START, ADDRESS_HRAM_END);

        oam = new CGB_Oam(ADDRESS_OAM_START, ADDRESS_OAM_END, this);
        initialize(oam, ADDRESS_OAM_START, ADDRESS_OAM_END);
        
        rom = new ROM(ADDRESS_CARTRIDGE_START, cartridge);
        mbc = setMBC();
        initialize(mbc, ADDRESS_CARTRIDGE_START, ADDRESS_CARTRIDGE_END);
        initialize(mbc, ADDRESS_ERAM_START, ADDRESS_ERAM_END);
        
        setIO(new CGB_IO(ADDRESS_IO_START, ADDRESS_IO_END, ramh, vram));
        initialize(getIO(), ADDRESS_IO_START, ADDRESS_IO_END);
        
        interruptRegister = new InterruptRegister();
        module[ADDRESS_INTERRUPT] = interruptRegister;

        module[ADDRESS_DMA] = getOam();        
        module[ADDRESS_VRAM_DMA_START] = vram;
        
        backgroundPaletteMemory = new PaletteMemory(ADDRESS_PALETTE_BACKGROUND_INDEX, 
                                                    ADDRESS_PALETTE_BACKGROUND_DATA);
        spritePaletteMemory = new PaletteMemory(ADDRESS_PALETTE_SPRITE_INDEX, 
                                                    ADDRESS_PALETTE_SPRITE_DATA);
        module[ADDRESS_PALETTE_BACKGROUND_INDEX] = backgroundPaletteMemory;
        module[ADDRESS_PALETTE_BACKGROUND_DATA] = backgroundPaletteMemory;
        module[ADDRESS_PALETTE_SPRITE_INDEX] = spritePaletteMemory;
        module[ADDRESS_PALETTE_SPRITE_DATA] = spritePaletteMemory;   
    }
    
    /**
     * Resets the memory to zero. The ROM is left untouched.
     */
    public void reset() {
        interruptRegister.reset();
        debug.reset();
        raml.reset();
        echo.reset();
        ramh.reset();
        rom.reset();
        vram.reset();
        getOam().reset();
        hram.reset();
        getIO().reset();
    }
    
    
    public CGB_Vram getVram() { 
        return vram;
    }
    
    public PaletteMemory getBackgroundPaletteMemory() {
        return backgroundPaletteMemory;
    }
    public PaletteMemory getSpritePaletteMemory() {
        return spritePaletteMemory;
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
        backgroundPaletteMemory.readState(fis);
        spritePaletteMemory.readState(fis);
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
        backgroundPaletteMemory.saveState(fos);
        spritePaletteMemory.saveState(fos);
    }
}
