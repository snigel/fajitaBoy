package fajitaboy.gbc;

import static fajitaboy.constants.AddressConstants.*;

import fajitaboy.gb.Cpu;
import fajitaboy.gb.memory.MemoryInterface;

/**
 * Cpu in CGB mode·.
 */
public class CGB_Cpu extends Cpu {
    
    /**
     * SpeedSwitch object. Used to change the speed.
     */
    public SpeedSwitch speedSwitch;
    
    /**
     * 
     * @param addressbus
     * @param ss SpeedSwitch object that to use if the speed should be changed.
     */
    public CGB_Cpu(MemoryInterface addressbus) {
        super(addressbus);
        speedSwitch = new SpeedSwitch();
        reset();
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
                    ram.write(ADDRESS_SPEED_SWITCH, 0x80);
                } else {
                    ram.write(ADDRESS_SPEED_SWITCH, 0x00);
                }
                stop = false;
                
            }
            cycleTime = 16; // Must proceed cycles during stop!
        }

        // Handle interrupts
        handleInterrupts();
        
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
