package fajitaboy.memory;

/**
 * Att g�ra:
 * 1. St�lla in r�tt bank f�r minne.
 * 2. Att st�lla in minnesl�ge
 * 3. K�ra eram genom MBC?
 * R�kna antalet banker och se till att den inte anropar fler.
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

public class MBC1 implements MemoryInterface {
    Eram eram;
    ROM rom;
    int banks;

    public MBC1 (Eram ram, ROM cartridge){
        eram=ram;
        rom=cartridge;
        banks=rom.getBanks();
        System.out.println("This rom has "+banks+" banks");
    }

    public int forceRead(int address) {
        return read(address);
    }

    public void forceWrite(int address, int data) {
       rom.forceWrite(address, data);
    }

    public int read(int address) {
        //System.out.println("read from ad "+address);
        return rom.read(address);
    }

    public void reset() {
        setRomBank(0);
        setRamBank(0);
    }
    private void setRomBank(int bank){
        if(bank == 0){ //0 is not allowed on MBC1.
            rom.setBank(1);
        }
        else{
           rom.setBank(bank&banks-1);
        }
        //System.out.println("rom bank set to "+(bank%banks)+" tried "+bank);
    }
    private void setRamBank(int bank){
        System.out.println("ram bank set to "+bank);
        eram.setBank(bank);
    }


    public void write(int address, int data) {
        if(address>=0x2000 && 0x4000>address){
            setRomBank(data);
        }
        else
            System.out.println("address: "+address+" data: "+data);
        if(address>=0x4000 && 0x6000>address){
            System.out.println("ram bank change :D");
        }
        if(address>=0x6000 && 0x8000>address){
            System.out.println("mode change :D");
        }
    }

}
