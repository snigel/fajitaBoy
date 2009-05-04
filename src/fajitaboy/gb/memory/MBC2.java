package fajitaboy.gb.memory;

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

/*public class MBC2 extends MBC1 {


    public MBC2 (Eram ram, ROM cartridge){
        //super(ram, cartridge);
    }

   
}*/
