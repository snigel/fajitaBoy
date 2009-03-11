package fajitaboy;

/**
 * Represents the memory which the cartridges ROM is saved.
 *
 * @author Adam Hulin, Johan Gustafsson
 *
 */
public class Cartridge extends MemoryComponent {
    /**
     * @param start
     *            , address representing where vram begins in memory space
     * @param romPath
     *            , is the location of a gameboy rom located in file system
     */
    Cartridge(final int start, final String romPath) {
        super(start, romPath);
    }
    
    public void write(int address, int data) {
        throw new RomWriteException("Catridge.java, adress:" + address);
    }
    
    public final void reset() {
    	//do nothing. 
    	//reloading the cartridge is not needed
    	//reset MBC if it will be implemented
    }
}
/*
 * Some code for reading out title or type testread(0x134, 16,
 * "Cartridge title"); testread(0x148, 1, "Cartridge type");
 *
 * }
 *
 * private void testread(int start, int length, String type){ String output="";
 * for(int i=0; i<length; i++){ output=output+addressBus.read(i+start); }
 * System.out.println(type+": "+output); }
 */
