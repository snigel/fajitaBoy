package fajitaboy.memory;

/**
 * Att g�ra:
 * 1. St�lla in r�tt bank f�r minne.
 * 2. Att st�lla in minnesl�ge
 * 3. K�ra eram genom MBC?
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

public class MBC2 implements MemoryInterface {
    Eram eram;
    ROM rom;

    public MBC2 (Eram ram, ROM cartridge){
        eram=ram;
        rom=cartridge;
    }

    public int forceRead(int address) {
        return read(address);
    }

    public void forceWrite(int address, int data) {
       rom.forceWrite(address, data);
    }

    public int read(int address) {
        return rom.read(address);
    }

    public void reset() {
        rom.setBank(0);
        eram.setBank(0);
    }

    public void write(int address, int data) {
        if(address>=0x2000 && 0x4000>address){
            rom.setBank(data);
        }
       // if(address<1 && address>0){
        //    eram.setBank(data);
       // }
    }

}
