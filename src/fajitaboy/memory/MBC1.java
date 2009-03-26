package fajitaboy.memory;

public class MBC1 implements MemoryInterface {
    Eram eram;
    ROM rom;

    public MBC1 (Eram ram, ROM cartridge){
        eram=ram;
        rom=cartridge;
        initMBC();
    }
    
    private void initMBC(){
        int ramSize=rom.read(0x0148);
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
        System.out.println(address);
        System.out.println(data);
        if(address>=0x2000 && 0x4000>address){
            rom.setBank(data);
        }
       // if(address<1 && address>0){
        //    eram.setBank(data);
       // }
    }

}
