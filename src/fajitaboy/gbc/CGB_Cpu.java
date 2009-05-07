package fajitaboy.gbc;

import static fajitaboy.constants.AddressConstants.*;

import fajitaboy.SpeedSwitch;
import fajitaboy.gb.Cpu;
import fajitaboy.gb.memory.MemoryInterface;

/**
 * Cpu in CGB mode·.
 */
public class CGB_Cpu extends Cpu {
    
    /**
     * SpeedSwitch object. Used to change the speed.
     */
    private SpeedSwitch speedSwitch;
    
    /**
     * 
     * @param addressbus
     * @param ss SpeedSwitch object that to use if the speed should be changed.
     */
    public CGB_Cpu(MemoryInterface addressbus, SpeedSwitch ss) {
        super(addressbus);
        speedSwitch = ss;
    }

    /**
     * {@inheritDoc}
     */
    public void reset() {
        super.reset();
        System.out.println("GBC CPU reset");
        // in CGB hardware a starts with the value 0x11 instead of 0x01.
        a = 0x11;
        if (speedSwitch != null) {
            speedSwitch.reset();
        }
    }
    
    /**
     * Steps the CPU one step. Next instruction is executed if no interrupt is
     * fired.
     * @return Returns the number of clock cycles used in this step.
     */
    public int step() {
        int cycleTime = 0;

        // Search for interrupts
        findInterrupts();
        
        // Perform processor operation
        if ( !stop ) {
            int inst = ram.read(pc);
            cycleTime = runInstruction(inst);   
        } else {
            int data = ram.read(ADDRESS_SPEED_SWITCH);
            if ((data & 0x01) == 1 ) {
                speedSwitch.toggleSpeed();
                
                if (speedSwitch.getSpeed() == 2) {
                    ram.write(0x80, ADDRESS_SPEED_SWITCH);
                } else {
                    ram.write(0x00, ADDRESS_SPEED_SWITCH);
                }
                stop = false;
                
            }
            cycleTime = 16; // Must proceed cycles during stop!
        }

        // Handle interrupts
        handleInterrupts();
        
        // Validate CPU registers
        assert ( a >= 0 && a < 0x100 ) : "CPU register A is out of range: " + a;
        assert ( b >= 0 && b < 0x100 ) : "CPU register B is out of range: " + b;
        assert ( c >= 0 && c < 0x100 ) : "CPU register C is out of range: " + c;
        assert ( d >= 0 && d < 0x100 ) : "CPU register D is out of range: " + d;
        assert ( e >= 0 && e < 0x100 ) : "CPU register E is out of range: " + e;
        assert ( h >= 0 && h < 0x100 ) : "CPU register H is out of range: " + h;
        assert ( l >= 0 && l < 0x100 ) : "CPU register L is out of range: " + l;
        assert ( cc >= 0 && cc < 0x100 ) : "CPU register CC is out of range: " + cc;
        assert ( sp >= 0 && sp < 0x10000 ) : "CPU stack pointer SP is out of range: " + sp;
        assert ( pc >= 0 && pc < 0x10000 ) : "CPU program counter PC is out of range: " + pc;
        
        return cycleTime;
    }
    /**
     * Called each time the STOP instruction is executed.
     * If the Prepare Speed Switch is activated the speed is changed
     */
    protected void stopActions() {
        int data = ram.read(ADDRESS_SPEED_SWITCH);
        
        if ((data & 0x01) == 1 ) {
            speedSwitch.toggleSpeed();
            
            if (speedSwitch.getSpeed() == 2) {
                ram.write(0x80, ADDRESS_SPEED_SWITCH);
            } else {
                ram.write(0x00, ADDRESS_SPEED_SWITCH);
            }
            pc++;
            
        }
    }

}
