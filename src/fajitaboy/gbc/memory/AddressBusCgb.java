package fajitaboy.gbc.memory;

import static fajitaboy.constants.AddressConstants.*;
import fajitaboy.gb.memory.AddressBus;
import fajitaboy.gb.memory.DebugMemory;
import fajitaboy.gb.memory.Echo;
import fajitaboy.gb.memory.Eram;
import fajitaboy.gb.memory.Hram;
import fajitaboy.gb.memory.InterruptRegister;
import fajitaboy.gb.memory.Oam;
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
public class AddressBusCgb extends AddressBus {

    private PaletteMemory backgroundPaletteMemory;
    private PaletteMemory spritePaletteMemory;
    
    private RamHighCgb ramh;
    private VramCgb vram;
    
    public AddressBusCgb(String romPath) {
        super(romPath);
    }
    
    protected void initializeModule(final String romPath) {
        
        // All modules must be initialized here
        debug = new DebugMemory();
        initialize(debug, DEBUG_START, DEBUG_END);

        ramh = new RamHighCgb(RAMH_START, RAMH_END);
        initialize(ramh, RAMH_START, RAMH_END);

        raml = new RamLow(RAML_START, RAML_END);
        initialize(raml, RAML_START, RAML_END);

        echo = new Echo(this, RAML_START, ECHO_START);
        initialize(echo, ECHO_START, ECHO_END);

        vram = new VramCgb(VRAM_START, VRAM_END, this);
        initialize(vram, VRAM_START, VRAM_END);

        hram = new Hram(HRAM_START, HRAM_END);
        initialize(hram, HRAM_START, HRAM_END);

        oam = new Oam(OAM_START, OAM_END, this);
        initialize(oam, OAM_START, OAM_END);
        
        rom = new ROM(CARTRIDGE_START, romPath);
        mbc = setMBC();
        
        initialize(mbc, CARTRIDGE_START, CARTRIDGE_END);
        
        io = new IOCgb(IO_START, IO_END, ramh, vram);
        initialize(io, IO_START, IO_END);
        
        interruptRegister = new InterruptRegister();
        module[INTERRUPT_ADDRESS] = interruptRegister;

        eram = new Eram(ERAM_START, ERAM_END);
        initialize(eram, ERAM_START, ERAM_END);
        
        module[ADDRESS_DMA] = oam;        
        module[VRAM_DMA_START] = vram;
        
        backgroundPaletteMemory = new PaletteMemory(C_PALETTE_BACKGROUND_INDEX, 
                                                    C_PALETTE_BACKGROUND_DATA);
        spritePaletteMemory = new PaletteMemory(C_PALETTE_SPRITE_INDEX, 
                                                    C_PALETTE_SPRITE_DATA);
        module[C_PALETTE_BACKGROUND_INDEX] = backgroundPaletteMemory;
        module[C_PALETTE_BACKGROUND_DATA] = backgroundPaletteMemory;
        module[C_PALETTE_SPRITE_INDEX] = spritePaletteMemory;
        module[C_PALETTE_SPRITE_DATA] = spritePaletteMemory;
        
        
        
        // test VramCgb
        write(0xFF4F, 0);
        for (int addr= 0x8000; addr < 0xA000; addr++) {
            write(addr,(addr & 0xff));
            //write(addr,0xac);
        }
        for (int addr= 0x8000; addr < 0xA000; addr++) {
            if( read(addr) != (addr & 0xff)) {
                System.out.println("Fail addr: "+Integer.toHexString(addr)+" data: "+Integer.toHexString(read(addr)));
            }
            //write(addr,(addr & 0xff));
        }
        
        for (int addr= 0x8000; addr < 0xA000; addr++) {
            if( vram.read(addr,0) != (addr & 0xff)) {
                System.out.println("Fail addr: "+Integer.toHexString(addr)+" data: "+Integer.toHexString(vram.read(addr, 0)));
            }
            //write(addr,(addr & 0xff));
        }
        System.out.println("vram bank:"+vram.getBank());
        write(0xFF4F, 1);
        System.out.println("vram bank:"+vram.getBank());
        for (int addr= 0x8000; addr < 0xA000; addr++) {
            if( read(addr) != 0) {
                System.out.println("Fail bank1: "+Integer.toHexString(addr)+" data: "+Integer.toHexString(read(addr)));
            }
            //write(addr,(addr & 0xff));
        }
        
        for (int addr= 0x8000; addr < 0xA000; addr++) {
            if( vram.read(addr, 1) != 0) {
                System.out.println("Fail bank1: "+Integer.toHexString(addr)+" data: "+Integer.toHexString(vram.read(addr, 1)));
            }
            //write(addr,(addr & 0xff));
        }
        for (int addr= 0x8000; addr < 0xA000; addr++) {
            if( vram.read(addr,0) != (addr & 0xff)) {
                System.out.println("Fail addr: "+Integer.toHexString(addr)+" data: "+Integer.toHexString(vram.read(addr, 0)));
            }
            //write(addr,(addr & 0xff));
        }
        
        for (int addr= 0x8000; addr < 0xA000; addr++) {
            write(addr,((addr+8) & 0xff));
        }
        
        for (int addr= 0x8000; addr < 0xA000; addr++) {
            if( read(addr) != ((addr+8) & 0xff)) {
                System.out.println("Fail addr: "+Integer.toHexString(addr)+" data: "+Integer.toHexString(read(addr)));
            }
            //write(addr,(addr & 0xff));
        }

        System.out.flush();
        
      /*  write(0x9000, 1);
        read(0x9000);

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        */
        
        
    }
    
    
    public VramCgb getVram() { 
        return vram;
    }
    
    public PaletteMemory getBackgroundPaletteMemory() {
        return backgroundPaletteMemory;
    }
    public PaletteMemory getSpritePaletteMemory() {
        return spritePaletteMemory;
    }

}
