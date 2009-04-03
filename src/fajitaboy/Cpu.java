package fajitaboy;

import static fajitaboy.constants.AddressConstants.*;
import static fajitaboy.constants.HardwareConstants.*;
import fajitaboy.memory.MemoryInterface;

/**
 * CPU class for emulating the Game Boy CPU.
 * @author Tobias S, Peter O
 */
public final class Cpu {

    /**
     * Address bus to access the memory.
     */
    private MemoryInterface ram;

    /**
     * Program counter/pointer 8bit register.
     */
    private int pc;

    /**
     * Flag Register. ZNHC0000. This register is the same as register F.
     * ZNHC0000
     */
    private int cc;

    /**
     * Stack Pointer 16bit register.
     */
    private int sp;

    /**
     * Interrupt Master Enable.
     */
    private boolean ime;

    // 8bit registers:
    /**Det gör den faktiskt nu, grejen är att det kommer ändå att komma negativa värden till
     * The H register.
     */
    private int h;

    /**
     * The L register.
     */
    private int l;

    /**
     * The A register.
     */
    private int a;

    /**
     * The B register.
     */
    private int b;

    /**
     * The C register.
     */
    private int c;

    /**
     * The D register.
     */
    private int d;

    /**
     * The E register.
     */
    private int e;

    // Internal variables:
    /**
     * temp variables to use in runInstruction().
     */
    private int temp;

    /**
     * to know if an interrupt should be executed.
     */
    private boolean executeInterrupt;
    
    /**
     * Address to jump to during interrupts.
     */
    private int interruptJumpAddress;

    /**
     * Bit to turn off on interrupt execution
     */
    private int interruptBit;
    
    private int ieReg;
    private int ifReg;
    
    /**
     * Creates a new CPU with default values.
     * @param addressbus
     *            The addressBus
     */
    
    /**
     * Halts the CPU until an interrupt occurs if active.
     */
    private boolean stop;
    
    public Cpu(final MemoryInterface addressbus) {
        ram = addressbus;
        reset();
    }

    /**
     * Sets CPU to start state.
     */
    public void reset() {
        pc = 0x0100;
        cc = 0xB0;
        a = 0x01;
        b = 0x00;
        c = 0x13;
        d = 0x00;
        e = 0xD8;
        h = 0x01;
        l = 0x4D;
        sp = 0xfffe;
        ime = true;
        stop = false;
        executeInterrupt = false;
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
    
    public void findInterrupts() {
    	interruptJumpAddress = 0x0000;
    	interruptBit = 0xFF;
    	executeInterrupt = false;
    	int bit = 0x00;

    	// Look for interrupts
    	ieReg = ram.read(ADDRESS_IE);
    	ifReg = ram.read(ADDRESS_IF);

    	if ((ieReg & 0x01) != 0 && (ifReg & 0x01) != 0) {
    		// V-Blank interrupt
    		interruptJumpAddress = ADDRESS_INT_VBLANK;
    		interruptBit = 0xFE;
    	} else if ((ieReg & 0x02) != 0 && (ifReg & 0x02) != 0) {
    		// LCD Status interrupt
    		interruptJumpAddress = ADDRESS_INT_LCDSTAT;
    		interruptBit = 0xFD;
    	} else if ((ieReg & 0x04) != 0 && (ifReg & 0x04) != 0) {
    		// Timer interrupt
    		interruptJumpAddress = ADDRESS_INT_TIMER;
    		interruptBit = 0xFB;
    	} else if ((ieReg & 0x08) != 0 && (ifReg & 0x08) != 0) {
    		// Serial interrupt
    		interruptJumpAddress = ADDRESS_INT_SERIAL;
    		interruptBit = 0xF7;
    	} else if ((ieReg & 0x10) != 0 && (ifReg & 0x10) != 0) {
    		// Joypad interrupt
    		interruptJumpAddress = ADDRESS_INT_JOYPAD;
    		interruptBit = 0xEF;
    	}

    	if ( interruptJumpAddress != 0x000 ) {
    		stop = false;
    		if ( ime ) {
    			executeInterrupt = true;
    		}
    	}
    }

    /**
     * Handle interrupts.
     */
    private void handleInterrupts() {
        if (executeInterrupt) {
        	ifReg = ram.read(ADDRESS_IF);
        	ram.write(ADDRESS_IF, ifReg & interruptBit); // clear interrupt bit
            ime = false;
            stop = false;
            push(pc);
            pc = interruptJumpAddress;
        }
    }

    /**
     * Runs specified instruction.
     * @param instruction
     *            The opcode of the instruction to run.
     * @return Clockcycles for this instruction.
     */
    private int runInstruction(final int instruction) {
        int cycleTime = 0;
        switch (instruction) {
        case 0x00: // NOP
            pc++;
            cycleTime += 4;
            break;
        case 0x01: // LD BC,nn
            setBC(readnn());
            pc += 3;
            cycleTime += 12;
            break;
        case 0x02: // LD (BC), A
            ram.write(getBC(), a);
            pc++;
            cycleTime += 8;
            break;
        case 0x03: // INC BC
            setBC(getBC() + 1);
            pc++;
            cycleTime += 8;
            break;
        case 0x04: // INC B
            b = inc(b);
            pc++;
            cycleTime += 4;
            break;
        case 0x05: // DEC B
            b = dec(b);
            pc++;
            cycleTime += 4;
            break;

        case 0x06: // LD B, n
            b = readn();
            pc += 2;
            cycleTime += 8;
            break;
        case 0x07: // RLCA
            setZ(0);
            setN(0);
            setH(0);
            a = a << 1;
            if (a > 0xFF) {
                setC(1);
                a = (a & 0xFF) + 1;
            } else {
                setC(0);
            }
            pc++;
            cycleTime += 4;
            break;
        case 0x08: // LD (nn),SP
            dblwrite(readnn(), sp);
            pc += 3;
            cycleTime += 20;
            break;
        case 0x09: // ADD HL,BC
            temp = getHL() + getBC();
            setH(((getHL() & 0x0FFF) + (getBC() & 0x0FFF)) > 0x0FFF);
            setHL(temp);
            setN(0);
            setC(temp > 0xFFFF);
            pc++;
            cycleTime += 8;
            break;
        case 0x0a: // LD A,(BC)
            a = ram.read(getBC());
            pc++;
            cycleTime += 8;
            break;
        case 0x0b: // DEC BC
            setBC(getBC() - 1);
            pc++;
            cycleTime += 8;
            break;
        case 0x0c: // INC C
            c = inc(c);
            pc++;
            cycleTime += 4;
            break;
        case 0x0d: // DEC C
            c = dec(c);
            pc++;
            cycleTime += 4;
            break;
        case 0x0e: // LD C,n
            c = readn();
            pc += 2;
            cycleTime += 8;
            break;
        case 0x0f: // RRCA
        	if (getC() == 1) {
                a = a | 0x0100;
            }
        	setC(a & 0x01);
            a = a >>> 1;
            setZ(0);
            setN(0);
            setH(0);
            pc++;
            cycleTime += 4;
            break;
        case 0x10: // STOP
        	stop = true;
        	cycleTime += 4;
        	pc++;
        	;break;
        case 0x11: // LD DE,nn
            setDE(readnn());
            pc += 3;
            cycleTime += 12;
            break;
        case 0x12: // LD (DE),A
            ram.write(getDE(), a);
            pc++;
            cycleTime += 8;
            break;
        case 0x13: // INC DE
            setDE(getDE() + 1);
            pc++;
            cycleTime += 8;
            break;
        case 0x14: // INC D
            d = inc(d);
            pc++;
            cycleTime += 4;
            break;
        case 0x15: // DEC D
            d = dec(d);
            pc++;
            cycleTime += 4;
            break;
        case 0x16: // LD D,n
            d = readn();
            pc += 2;
            cycleTime += 8;
            break;
        case 0x17: // RLA
            setZ(0);
            setN(0);
            setH(0);
            a = a << 1;
            a += getC();
            if (a > 0xFF) {
                setC(1);
                a = a & 0xFF;
            } else {
                setC(0);
            }
            pc++;
            cycleTime += 4;
            break;
        case 0x18: // JR d
            pc = pc + (byte) ram.read(pc + 1) + 2;
            cycleTime += 12;
            break;
        case 0x19: // ADD HL,DE
            temp = getHL() + getDE();
            setH(((getHL() & 0x0FFF) + (getDE() & 0x0FFF)) > 0x0FFF);
            setHL(temp);
            setN(0);
            setC(temp > 0xFFFF);
            pc++;
            cycleTime += 8;
            break;
        case 0x1a: // LD A,(DE)
            a = ram.read(getDE());
            pc++;
            cycleTime += 8;
            break;
        case 0x1b: // DEC DE
            setDE(getDE() - 1);
            pc++;
            cycleTime += 8;
            break;
        case 0x1c: // INC e
            e = inc(e);
            pc++;
            cycleTime += 4;
            break;
        case 0x1d: // DEC e
            e = dec(e);
            pc++;
            cycleTime += 4;
            break;
        case 0x1e: // LD E,n
            e = readn();
            pc += 2;
            cycleTime += 8;
            break;
        case 0x1f: // RRA
            setZ(0);
            setN(0);
            setH(0);
            if (getC() == 1) {
                a = a | 0x100;
            }
            setC(a & 0x01);
            a = a >>> 1;
            pc++;
            cycleTime += 4;
            break;

        case 0x20: // JR NZ+e
            if (getZ() == 0) {
                pc += (byte) readn() + 2;
                cycleTime += 12;
            } else {
                pc += 2;
                cycleTime += 8;
            }
            break;
        case 0x21: // LD HL,nn
            setHL(readnn());
            pc += 3;
            cycleTime += 12;
            break;
        case 0x22: // LDI (HL),A
            ram.write(getHL(), a);
            setHL(getHL() + 1);
            pc++;
            cycleTime += 8;
            break;
        case 0x23: // INC HL
            setHL(getHL() + 1);
            pc++;
            cycleTime += 8;
            break;
        case 0x24: // INC H
            h = inc(h);
            pc++;
            cycleTime += 4;
            break;
        case 0x25: // DEC H
            h = dec(h);
            pc++;
            cycleTime += 4;
            break;
        case 0x26: // LD H,n
            h = readn();
            pc += 2;
            cycleTime += 8;
            break;
        case 0x27: // DAA - algorithm found at
            // http://www.worldofspectrum.org/faq/reference/z80reference.htm#DAA
            int correctionFactor;
            if (a > 0x99 || getC() == 1) {
                correctionFactor = 0x60;
                setC(1);
            } else {
                correctionFactor = 0x00;
                setC(0);
            }
            if ((a & 0x0F) > 0x09 || getH() == 1) {
                correctionFactor |= 0x06;
            }
            if (getN() == 0) {
                calcH(a, correctionFactor);
                a = (a + correctionFactor) & 0xFF;
            } else {
                calcHsub(a, correctionFactor);
                a = (a - correctionFactor) & 0xFF;
            }
            setZ(a == 0);
            pc++;
            cycleTime += 4;
            break;
        case 0x28: // JR Z,d (Jump+n if Z=1)
            if (getZ() == 1) {
                pc = pc + (byte) readn() + 2;
                cycleTime += 12;
            } else {
                pc += 2;
                cycleTime += 8;
            }
            break;

        case 0x29: // ADD HL,HL
            temp = getHL() + getHL();
            setH(((getHL() & 0x0FFF) + (getHL() & 0x0FFF)) > 0x0FFF);
            setN(0);
            setC(temp > 0xFFFF);
            setHL(temp);
            pc++;
            cycleTime += 8;
            break;
        case 0x2a: // LDI A,(HL)
            a = ram.read(getHL());
            setHL(getHL() + 1);
            pc++;
            cycleTime += 8;
            break;
        case 0x2b: // DEC HL
            setHL(getHL() - 1);
            pc++;
            cycleTime += 8;
            break;
        case 0x2c: // INC L
            l = inc(l);
            pc++;
            cycleTime += 4;
            break;
        case 0x2d: // DEC L
            l = dec(l);
            pc++;
            cycleTime += 4;
            break;
        case 0x2e: // LD l,n
            l = readn();
            pc += 2;
            cycleTime += 8;
            break;
        case 0x2f: // CPL
            a = ~a & 0xff;
            setN(1);
            setH(1);
            pc++;
            cycleTime += 4;
            break;

        case 0x30: // JR NC,d
            if (getC() == 0) {
                pc = pc + (byte) readn() + 2;
                cycleTime += 12;
            } else {
                pc += 2;
                cycleTime += 8;
            }
            break;
        case 0x31: // LD SP,nn
            sp = readnn();
            pc += 3;
            cycleTime += 12;
            break;
        case 0x32: // LDD (HL),A; HL-
            ram.write(getHL(), a);
            setHL(getHL() - 1);
            pc++;
            cycleTime += 8;
            break;
        case 0x33: // INC SP
            sp = (sp + 1) & 0xFFFF;
            pc++;
            cycleTime += 8;
            break;
        case 0x34: // INC (HL)
            ram.write(getHL(), inc(ram.read(getHL())));
            pc++;
            cycleTime += 12;
            break;
        case 0x35: // DEC (HL)
            ram.write(getHL(), dec(ram.read(getHL())));
            pc++;
            cycleTime += 12;
            break;
        case 0x36: // LD (HL),n
            ram.write(getHL(), readn());
            pc += 2;
            cycleTime += 12;
            break;
        case 0x37: // SCF
            setC(1);
            setN(0);
            setH(0);
            pc++;
            cycleTime += 4;
            break;
        case 0x38: // JR C,d
            if (getC() == 1) {
                pc = pc + (byte) readn() + 2;
                cycleTime += 12;
            } else {
                pc += 2;
                cycleTime += 8;
            }
            break;
        case 0x39: // ADD HL,SP
            temp = getHL() + sp;
            setH(((getHL() & 0x0FFF) + (sp & 0x0FFF)) > 0x0FFF);
            setHL(temp);
            setN(0);
            setC(temp > 0xFFFF);
            pc++;
            cycleTime += 8;
            break;
        case 0x3a: // LDD A,(HL)
            a = ram.read(getHL());
            setHL(getHL() - 1);
            pc++;
            cycleTime += 8;
            break;
        case 0x3b: // DEC SP
            sp = (sp - 1) & 0xFFFF;
            pc++;
            cycleTime += 8;
            break;
        case 0x3c: // INC A
            a = inc(a);
            pc++;
            cycleTime += 4;
            break;
        case 0x3d: // DEC A
            a = dec(a);
            pc++;
            cycleTime += 4;
            break;
        case 0x3e: // LD A, n
            a = readn();
            pc += 2;
            cycleTime += 8;
            break;
        case 0x3f: // CCF
            setC(getC() == 0);
            setN(0);
            setH(0);
            pc++;
            cycleTime += 4;
            break;
        case 0x40: // LD B,B
            // b = b;
            pc++;
            cycleTime += 4;
            break;
        case 0x41: // LD B,C
            b = c;
            pc++;
            cycleTime += 4;
            break;
        case 0x42: // LD B,D
            b = d;
            pc++;
            cycleTime += 4;
            break;
        case 0x43: // LD B,E
            b = e;
            pc++;
            cycleTime += 4;
            break;
        case 0x44: // LD B,H
            b = h;
            pc++;
            cycleTime += 4;
            break;
        case 0x45: // LD B,L
            b = l;
            pc++;
            cycleTime += 4;
            break;
        case 0x46: // LD B,(HL)
            b = ram.read(getHL());
            pc++;
            cycleTime += 8;
            break;
        case 0x47: // LD B,A
            b = a;
            cycleTime += 4;
            pc++;
            break;
        case 0x48: // LD C,B
            c = b;
            pc++;
            cycleTime += 4;
            break;
        case 0x49: // LD C,C
            // c=c;
            pc++;
            cycleTime += 4;
            break;
        case 0x4a: // LD C,D
            c = d;
            pc++;
            cycleTime += 4;
            break;
        case 0x4b: // LD C,E
            c = e;
            pc++;
            cycleTime += 4;
            break;
        case 0x4c: // LD C,H
            c = h;
            pc++;
            cycleTime += 4;
            break;
        case 0x4d: // LD C,L
            c = l;
            pc++;
            cycleTime += 4;
            break;
        case 0x4e: // LD C,(HL)
            c = ram.read(getHL());
            pc++;
            cycleTime += 8;
            break;
        case 0x4f: // LD C,A
            c = a;
            pc++;
            cycleTime += 4;
            break;
        case 0x50: // LD D,B
            d = b;
            pc++;
            cycleTime += 4;
            break;
        case 0x51: // LD D,C
            d = c;
            pc++;
            cycleTime += 4;
            break;
        case 0x52: // LD D,D
            // d=d;
            pc++;
            cycleTime += 4;
            break;
        case 0x53: // LD D,E
            d = e;
            pc++;
            cycleTime += 4;
            break;
        case 0x54: // LD D,H
            d = h;
            pc++;
            cycleTime += 4;
            break;
        case 0x55: // LD D,L
            d = l;
            pc++;
            cycleTime += 4;
            break;
        case 0x56: // LD D,(HL)
            d = ram.read(getHL());
            pc++;
            cycleTime += 8;
            break;
        case 0x57: // LD D,A
            d = a;
            cycleTime += 4;
            pc++;
            break;
        case 0x58: // LD E,B
            e = b;
            pc++;
            cycleTime += 4;
            break;
        case 0x59: // LD E,C
            e = c;
            pc++;
            cycleTime += 4;
            break;
        case 0x5A: // LD E,D
            e = d;
            pc++;
            cycleTime += 4;
            break;
        case 0x5b: // LD E,E
            // e=e;
            pc++;
            cycleTime += 4;
            break;
        case 0x5c: // LD E,H
            e = h;
            pc++;
            cycleTime += 4;
            break;
        case 0x5d: // LD E,L
            e = l;
            pc++;
            cycleTime += 4;
            break;
        case 0x5e: // LD E,(HL)
            e = ram.read(getHL());
            pc++;
            cycleTime += 8;
            break;
        case 0x5f: // LD E,A
            e = a;
            pc++;
            cycleTime += 4;
            break;
        case 0x60: // LD H,B
            h = b;
            pc++;
            cycleTime += 4;
            break;
        case 0x61: // LD H,C
            h = c;
            pc++;
            cycleTime += 4;
            break;
        case 0x62: // LD H,D
            h = d;
            pc++;
            cycleTime += 4;
            break;
        case 0x63: // LD H,E
            h = e;
            pc++;
            cycleTime += 4;
            break;
        case 0x64: // LD H,H
            // h=h;
            pc++;
            cycleTime += 4;
            break;
        case 0x65: // LD H,L
            h = l;
            pc++;
            cycleTime += 4;
            break;
        case 0x66: // LD H,(HL)
            h = ram.read(getHL());
            pc++;
            cycleTime += 8;
            break;
        case 0x67: // LD H,A
            h = a;
            pc++;
            cycleTime += 4;
            break;
        case 0x68: // LD L,B
            l = b;
            pc++;
            cycleTime += 4;
            break;
        case 0x69: // LD L,C
            l = c;
            pc++;
            cycleTime += 4;
            break;
        case 0x6a: // LD L,D
            l = d;
            pc++;
            cycleTime += 4;
            break;
        case 0x6b: // LD L,E
            l = e;
            pc++;
            cycleTime += 4;
            break;
        case 0x6c: // LD L,H
            l = h;
            pc++;
            cycleTime += 4;
            break;
        case 0x6d: // LD L,L
            // l=l;
            pc++;
            cycleTime += 4;
            break;
        case 0x6e: // LD L,(HL)
            l = ram.read(getHL());
            pc++;
            cycleTime += 8;
            break;
        case 0x6f: // LD L,A
            l = a;
            pc++;
            cycleTime += 4;
            break;

        case 0x70: // LD (HL),B
            ram.write(getHL(), b);
            pc++;
            cycleTime += 8;
            break;
        case 0x71: // LD (HL),C
            ram.write(getHL(), c);
            pc++;
            cycleTime += 8;
            break;
        case 0x72: // LD (HL),D
            ram.write(getHL(), d);
            pc++;
            cycleTime += 8;
            break;
        case 0x73: // LD (HL),E
            ram.write(getHL(), e);
            pc++;
            cycleTime += 8;
            break;
        case 0x74: // LD (HL),H
            ram.write(getHL(), h);
            pc++;
            cycleTime += 8;
            break;
        case 0x75: // LD (HL),L
            ram.write(getHL(), l);
            pc++;
            cycleTime += 8;
            break;
        case 0x76: // HALT  (Implemented identically to STOP)
        	stop = true;
        	cycleTime += 4;
        	pc++;
        	break;
        case 0x77: // LD (HL),A
            ram.write(getHL(), a);
            pc++;
            cycleTime += 8;
            break;
        case 0x78: // LD A,B
            a = b;
            pc++;
            cycleTime += 4;
            break;
        case 0x79: // LD A,C
            a = c;
            pc++;
            cycleTime += 4;
            break;
        case 0x7A: // LD A,D
            a = d;
            pc++;
            cycleTime += 4;
            break;
        case 0x7B: // LD A,E
            a = e;
            pc++;
            cycleTime += 4;
            break;
        case 0x7c: // LD A,H
            a = h;
            pc++;
            cycleTime += 4;
            break;
        case 0x7d: // LD A,L
            a = l;
            pc++;
            cycleTime += 4;
            break;
        case 0x7e: // LD A,(HL)
            a = ram.read(getHL());
            pc++;
            cycleTime += 8;
            break;
        case 0x7f: // LD A,A
            // a=a;
            pc++;
            cycleTime += 4;
            break;

        case 0x80: // ADD A,B
            add(b);
            pc++;
            cycleTime += 4;
            break;
        case 0x81: // ADD A,C
            add(c);
            pc++;
            cycleTime += 4;
            break;
        case 0x82: // ADD A,D
            add(d);
            pc++;
            cycleTime += 4;
            break;
        case 0x83: // ADD A,E
            add(e);
            pc++;
            cycleTime += 4;
            break;
        case 0x84: // ADD A,H
            add(h);
            pc++;
            cycleTime += 4;
            break;
        case 0x85: // ADD A,L
            add(l);
            pc++;
            cycleTime += 4;
            break;
        case 0x86: // ADD A,(HL)
            add(ram.read(getHL()) & 0xFF);
            pc++;
            cycleTime += 8;
            break;
        case 0x87: // ADD A,A
            add(a);
            pc++;
            cycleTime += 4;
            break;

        case 0x88: // ADC A,B
            add(b + getC());
            pc++;
            cycleTime += 4;
            break;
        case 0x89: // ADC A,C
            add(c + getC());
            pc++;
            cycleTime += 4;
            break;
        case 0x8a: // ADC A,D
            add(d + getC());
            pc++;
            cycleTime += 4;
            break;
        case 0x8b: // ADC A,E
            add(e + getC());
            pc++;
            cycleTime += 4;
            break;
        case 0x8c: // ADC A,H
            add(h + getC());
            pc++;
            cycleTime += 4;
            break;
        case 0x8d: // ADC A,L
            add(l + getC());
            pc++;
            cycleTime += 4;
            break;
        case 0x8e: // ADC A,(HL)
            add((ram.read(getHL()) & 0xff) + getC());
            pc++;
            cycleTime += 8;
            break;
        case 0x8f: // ADC A,A
            add(a + getC());
            pc++;
            cycleTime += 4;
            break;

        case 0x90: // SUB B
            sub(b);
            pc++;
            cycleTime += 4;
            break;
        case 0x91: // SUB C
            sub(c);
            pc++;
            cycleTime += 4;
            break;
        case 0x92: // SUB D
            sub(d);
            pc++;
            cycleTime += 4;
            break;
        case 0x93: // SUB E
            sub(e);
            pc++;
            cycleTime += 4;
            break;
        case 0x94: // SUB H
            sub(h);
            pc++;
            cycleTime += 4;
            break;
        case 0x95: // SUB L
            sub(l);
            pc++;
            cycleTime += 4;
            break;
        case 0x96: // SUB (HL)
            sub(ram.read(getHL()));
            pc++;
            cycleTime += 8;
            break;
        case 0x97: // SUB A
            sub(a);
            pc++;
            cycleTime += 4;
            break;

        case 0x98: // SBC A,B
            sub(b + getC());
            pc++;
            cycleTime += 4;
            break;
        case 0x99: // SBC A,C
            sub(c + getC());
            pc++;
            cycleTime += 4;
            break;
        case 0x9A: // SBC A,D
            sub(d + getC());
            pc++;
            cycleTime += 4;
            break;
        case 0x9B: // SBC A,E
            sub(e + getC());
            pc++;
            cycleTime += 4;
            break;
        case 0x9C: // SBC A,H
            sub(h + getC());
            pc++;
            cycleTime += 4;
            break;
        case 0x9D: // SBC A,L
            sub(l + getC());
            pc++;
            cycleTime += 4;
            break;
        case 0x9e: // SBC A,(HL)
            sub((ram.read(getHL()) & 0xFF) + getC());
            pc++;
            cycleTime += 8;
            break;
        case 0x9f: // SBC A,A
            sub(a + getC());
            pc++;
            cycleTime += 4;
            break;

        case 0xa0: // AND B
            and(b);
            pc++;
            cycleTime += 4;
            break;
        case 0xa1: // AND C
            and(c);
            pc++;
            cycleTime += 4;
            break;
        case 0xa2: // AND D
            and(d);
            pc++;
            cycleTime += 4;
            break;
        case 0xa3: // AND E
            and(e);
            pc++;
            cycleTime += 4;
            break;
        case 0xa4: // AND H
            and(h);
            pc++;
            cycleTime += 4;
            break;
        case 0xa5: // AND L
            and(l);
            pc++;
            cycleTime += 4;
            break;
        case 0xa6: // AND (HL)
            and(ram.read(getHL()));
            pc++;
            cycleTime += 8;
            break;
        case 0xa7: // AND A
            and(a);
            pc++;
            cycleTime += 4;
            break;

        case 0xa8: // XOR B
            xor(b);
            pc++;
            cycleTime += 4;
            break;
        case 0xa9: // XOR C
            xor(c);
            pc++;
            cycleTime += 4;
            break;
        case 0xaa: // XOR D
            xor(d);
            pc++;
            cycleTime += 4;
            break;
        case 0xab: // XOR E
            xor(e);
            pc++;
            cycleTime += 4;
            break;
        case 0xac: // XOR H
            xor(h);
            pc++;
            cycleTime += 4;
            break;
        case 0xad: // XOR L
            xor(l);
            pc++;
            cycleTime += 4;
            break;
        case 0xAE: // XOR (HL)
            xor(ram.read(getHL()));
            pc++;
            cycleTime += 8;
            break;
        case 0xAF: // XOR A
            xor(a);
            pc++;
            cycleTime += 4;
            break;

        case 0xB0: // OR B
            or(b);
            pc++;
            cycleTime += 4;
            break;
        case 0xB1: // OR C
            or(c);
            pc++;
            cycleTime += 4;
            break;
        case 0xB2: // OR D
            or(d);
            pc++;
            cycleTime += 4;
            break;
        case 0xb3: // OR E
            or(e);
            pc++;
            cycleTime += 4;
            break;
        case 0xb4: // OR H
            or(h);
            pc++;
            cycleTime += 4;
            break;
        case 0xb5: // OR L
            or(l);
            pc++;
            cycleTime += 4;
            break;
        case 0xb6: // OR (HL)
            or(ram.read(getHL()));
            pc++;
            cycleTime += 8;
            break;
        case 0xb7: // OR A
            or(a);
            pc++;
            cycleTime += 4;
            break;

        case 0xb8: // CP B
            cp(b);
            pc++;
            cycleTime += 4;
            break;
        case 0xb9: // CP C
            cp(c);
            pc++;
            cycleTime += 4;
            break;
        case 0xba: // CP D
            cp(d);
            pc++;
            cycleTime += 4;
            break;
        case 0xbb: // CP E
            cp(e);
            pc++;
            cycleTime += 4;
            break;
        case 0xbc: // CP H
            cp(h);
            pc++;
            cycleTime += 4;
            break;
        case 0xbd: // CP L
            cp(l);
            pc++;
            cycleTime += 4;
            break;
        case 0xbe: // CP (HL)
            cp(ram.read(getHL()));
            pc++;
            cycleTime += 8;
            break;
        case 0xbf: // CP A
            cp(a);
            pc++;
            cycleTime += 4;
            break;

        case 0xc0: // RET NZ
            if (getZ() == 0) {
                ret();
                cycleTime += 20;
            } else {
                pc++;
                cycleTime += 8;
            }
            break;
        case 0xc1: // POP BC
            setBC(pop());
            pc++;
            cycleTime += 12;
            break;
        case 0xc2: // JP NZ,nn
            if (getZ() == 0) {
                pc = readnn();
                cycleTime += 16;
            } else {
                pc += 3;
                cycleTime += 12;
            }
            break;
        case 0xc3: // JP nn
            pc = readnn();
            cycleTime += 16;
            break;
        case 0xc4: // CALL NZ,nn
            if (getZ() == 0) {
                call();
                cycleTime += 24;
            } else {
                pc += 3;
                cycleTime += 12;
            }
            break;
        case 0xc5: // PUSH BC
            push(getBC());
            pc++;
            cycleTime += 16;
            break;
        case 0xc6: // ADD A,n
            add(readn());
            pc += 2;
            cycleTime += 8;
            break;
        case 0xc7: // RST 0
            push(pc + 1);
            pc = 0;
            cycleTime += 16;
            break;
        case 0xc8: // RET Z
            if (getZ() == 1) {
                ret();
                cycleTime += 20;
            } else {
                pc++;
                cycleTime += 8;
            }
            break;
        case 0xc9: // RET
            ret();
            cycleTime += 16;
            break;
        case 0xca: // JP Z,nn
            if (getZ() == 1) {
                pc = readnn();
                cycleTime += 16;
            } else {
                pc += 3;
                cycleTime += 12;
            }
            break;
        case 0xcb: // CB Prefix
            int cbOp = readn();
            switch (cbOp & 0x07) {
            case 0: // B
                b = prefixCB(cbOp, b);
                cycleTime += 8;
                break;
            case 1: // C
                c = prefixCB(cbOp, c);
                cycleTime += 8;
                break;
            case 2: // D
                d = prefixCB(cbOp, d);
                cycleTime += 8;
                break;
            case 3: // E
                e = prefixCB(cbOp, e);
                cycleTime += 8;
                break;
            case 4: // H
                h = prefixCB(cbOp, h);
                cycleTime += 8;
                break;
            case 5: // L
                l = prefixCB(cbOp, l);
                cycleTime += 8;
                break;
            case 6: // (HL)
                ram.write(getHL(), prefixCB(cbOp, ram.read(getHL())));

                if (cbOp > 0x3f && cbOp < 0x80) {
                    // instruction is BIT
                    cycleTime += 12;
                } else {
                    // instruction is SET or RES
                    cycleTime += 16;
                }
                break;
            case 7: // A
                a = prefixCB(cbOp, a);
                cycleTime += 8;
                break;
            }
            pc += 2;
            break;
        case 0xcc: // CALL Z,nn
            if (getZ() == 1) {
                call();
                cycleTime += 24;
            } else {
                pc += 3;
                cycleTime += 12;
            }
            break;
        case 0xcd: // CALL nn
            call();
            cycleTime += 24;
            break;
        case 0xce: // ADC A,n
            add(readn() + getC());
            pc += 2;
            cycleTime += 8;
            break;
        case 0xcf: // RST 8
            push(pc + 1);
            pc = 8;
            cycleTime += 16;
            break;
        case 0xd0: // RET NC
            if (getC() == 0) {
                ret();
                cycleTime += 20;
            } else {
                pc++;
                cycleTime += 8;
            }
            break;
        case 0xd1: // POP DE
            setDE(pop());
            pc++;
            cycleTime += 12;
            break;
        case 0xd2: // JP NC,nn
            if (getC() == 0) {
                pc = readnn();
                cycleTime += 16;
            } else {
                pc += 3;
                cycleTime += 12;
            }
            break;
        case 0xd3: // NOP
            pc++;
            cycleTime += 4;
            break;
        case 0xd4: // CALL NC,nn
            if (getC() == 0) {
                call();
                cycleTime += 24;
            } else {
                pc += 3;
                cycleTime += 12;
            }
            break;
        case 0xd5: // PUSH DE
            push(getDE());
            pc++;
            cycleTime += 16;
            break;
        case 0xd6: // SUB n
            sub(readn());
            pc += 2;
            cycleTime += 8;
            break;
        case 0xd7: // RST 10H
            push(pc + 1);
            pc = 0x10;
            cycleTime += 16;
            break;
        case 0xd8: // RET C
            if (getC() == 1) {
                ret();
                cycleTime += 20;
            } else {
                pc++;
                cycleTime += 8;
            }
            break;
        case 0xd9: // RETI
            ret();
            ime = true;
            cycleTime += 16;
            break;
        case 0xda: // JP C,nn
            if (getC() == 1) {
                pc = readnn();
                cycleTime += 16;
            } else {
                pc += 3;
                cycleTime += 12;
            }
            break;
        case 0xdb: // NOP
            pc++;
            cycleTime += 4;
            break;
        case 0xdc: // CALL C,nn
            if (getC() == 1) {
                call();
                cycleTime += 24;
            } else {
                pc += 3;
                cycleTime += 12;
            }
            break;
        case 0xdd: // NOP
            pc++;
            cycleTime += 4;
            break;
        case 0xde: // SBC A,n
            sub(readn() + getC());
            pc += 2;
            cycleTime += 8;
            break;
        case 0xDF: // RST 18H
            push(pc + 1);
            pc = 0x18;
            cycleTime += 16;
            break;
        case 0xE0: // LD (FF00+n),A
            ram.write((0xFF00 + readn()), a);
            pc += 2;
            cycleTime += 12;
            break;
        case 0xE1: // POP HL
            setHL(pop());
            pc++;
            cycleTime += 12;
            break;
        case 0xe2: // LD (FF00+C),A
            ram.write((0xFF00 + c), a);
            pc++;
            cycleTime += 8;
            break;
        case 0xE3: // NOP
            pc++;
            cycleTime += 4;
            break;
        case 0xE4: // NOP
            pc++;
            cycleTime += 4;
            break;
        case 0xe5: // PUSH HL
            push(getHL());
            pc++;
            cycleTime += 16;
            break;
        case 0xE6: // AND n
            and(readn());
            pc += 2;
            cycleTime += 8;
            break;
        case 0xe7: // RST 20H
            push(pc + 1);
            pc = 0x20;
            cycleTime += 16;
            break;
        case 0xe8: // ADD SP,dd
            temp = (byte) readn();
            setZ(0);
            setN(0);
            if (temp < 0) { // Negative
                setC((sp + temp) < 0);
                calcHsub(sp, -temp);

            } else { // Positive
                setC((sp + temp) > 0xFFFF);
                calcH(sp, temp);
            }
            sp = sp + temp;
            pc += 2;
            cycleTime += 16;
            break;
        case 0xe9: // JP (HL)
            pc = getHL();
            cycleTime += 4;
            break;
        case 0xEA: // LD (nn),A
            ram.write(readnn(), a);
            pc += 3;
            cycleTime += 16;
            break;
        case 0xEB: // NOP
            pc++;
            cycleTime += 4;
            break;
        case 0xEC: // NOP
            pc++;
            cycleTime += 4;
            break;
        case 0xed: // NOP
            pc++;
            cycleTime += 4;
            break;
        case 0xee: // XOR n
            xor(readn());
            pc += 2;
            cycleTime += 8;
            break;
        case 0xef: // RST 28H
            push(pc + 1);
            pc = 0x28;
            cycleTime += 16;
            break;

        case 0xF0: // LD A,(FF00+n)
            a = ram.read(0xFF00 + readn());
            pc += 2;
            cycleTime += 12;
            break;
        case 0xF1: // POP AF
            setAF(pop());
            pc++;
            cycleTime += 12;
            break;
        case 0xF2: // LD A,(FF00+C)
            a = ram.read(0xFF00 + c);
            pc++;
            cycleTime += 8;
            break;
        case 0xf3: // DI
            ime = false;
            pc++;
            cycleTime += 4;
            break;
        case 0xF4: // NOP
            pc++;
            cycleTime += 4;
            break;
        case 0xf5: // PUSH AF
            push(getAF());
            pc++;
            cycleTime += 16;
            break;
        case 0xf6: // OR n
            or(readn());
            pc += 2;
            cycleTime += 8;
            break;
        case 0xf7: // RST 30H
            push(pc + 1);
            pc = 0x30;
            cycleTime += 16;
            break;
        case 0xf8: // LD HL,SP+dd
            temp = (byte) readn();
            setZ(0);
            setN(0);
            setHL(sp + temp);
            if (temp < 0) { // Negative
                setC((sp + temp) < 0);
                calcHsub(sp, -temp);

            } else { // Positive
                setC((sp + temp) > 0xFFFF);
                calcH(sp, temp);
            }
            pc += 2;
            cycleTime += 12;
            break;
        case 0xf9: // LD SP,HL
            sp = getHL();
            pc++;
            cycleTime += 8;
            break;
        case 0xfa: // LD A,(nn)
            a = ram.read(readnn());
            pc += 3;
            cycleTime += 16;
            break;
        case 0xfb: // EI
            ime = true;
            pc++;
            cycleTime += 4;
            break;
        case 0xFC: // NOP
            pc++;
            cycleTime += 4;
            break;
        case 0xFD: // NOP
            pc++;
            cycleTime += 4;
            break;
        case 0xFE: // CP, n
        	cp(readn());
            pc += 2;
            cycleTime += 8;
            break;
        case 0xff: // RST 38h
            push(pc + 1);
            pc = 0x38;
            cycleTime += 16;
            break;

        default:
            System.out.println("implement step "
                    + Integer.toHexString(ram.read(pc)));
            while (true)
                ; // Debug
        }
        
        // Cap pc to correct area
        pc &= 0xFFFF;
        
        return cycleTime;
    }

    /**
     * prefixCB handles the instructions with the prefix 0xCB.
     * @param op
     *            Opcode that is followed by the CB prefix.
     * @param r
     *            8bit register value or memory space.
     * @return The new value of the register or memory space.
     */
    private int prefixCB(int op, int r) {
        if (op < 0x40) {
            switch (op >>> 3) {
            case 0: // RLC
                setN(0);
                setH(0);
                r = r << 1;
                if (r > 0xFF) {
                    setC(1);
                    r = (r & 0xFF) + 1;
                } else {
                    setC(0);
                }
                setZ(r == 0);
                break;
            case 1: // RRC
                setN(0);
                setH(0);
                setC((r & 0x01) == 1);
                r = r >>> 1;
                if (getC() == 1) {
                    r = r | 0x80;
                }
                setZ(r == 0);
                break;
            case 2: // RL
                setN(0);
                setH(0);
                r = r << 1;
                if (getC() == 1) {
                    r++;
                }
                if (r > 0xFF) {
                    setC(1);
                    r = r & 0xFF;
                } else {
                    setC(0);
                }
                setZ(r == 0);
                break;
            case 3: // RR
                setN(0);
                setH(0);
                if (getC() == 1) {
                    r = r | 0x100;
                }
                setC((r & 0x01) == 1);
                r = r >>> 1;
                setZ(r == 0);
                break;
            case 4: // SLA
                setN(0);
                setH(0);
                r = r << 1;
                if (r > 0xFF) {
                    setC(1);
                    r = r & 0xFF;
                } else {
                    setC(0);
                }
                setZ(r == 0);
                break;
            case 5: // SRA
                setN(0);
                setH(0);
                int b7 = r & 0x80;
                setC((r & 0x01) == 1);
                r = r >>> 1;
                r += b7;
                setZ(r == 0);
                break;
            case 6: // SWAP
                setN(0);
                setH(0);
                setC(0);
                int highNibble = r >>> 4;
                int lowNibble = r & 0x0F;
                r = lowNibble * 0x10 + highNibble;
                setZ(r == 0);
                break;
            case 7: // SRL
                setN(0);
                setH(0);
                setC((r & 0x01) == 1);
                r = r >>> 1;
                setZ(r == 0);
                break;
            }
        } else {
            int bit = (op >>> 3) & 0x07;
            if (op < 0x80) { // BIT
                bit(bit, r);
            } else if (op < 0xc0) { // RES
                r = res(bit, r);
            } else { // SET
                r = set(bit, r);
            }
        }
        return r;
    }

    /**
     * Reads the byte after pc.
     * @return the byte after pc.
     */
    private int readn() {
        return ram.read(pc + 1);
    }

    /**
     * Reads the two bytes after pc.
     * @return the two byte after pc as a 16bit value.
     */
    private int readnn() {
        return (ram.read(pc + 1) + ram.read(pc + 2) * 0x100);
    }

    /**
     * Reads the 16bit value where sp is pointing.
     * @return A 16 bit value
     */
    private int readSP() {
        return (ram.read(sp) + ram.read(sp + 1) * 0x100);
    }

    /**
     * The 8bit INC operation (s <- s + 1).
     * @param i
     *            the register value to increment
     * @return new value of the register
     */
    private int inc(final int i) {
        int t = (i + 1) & 0xFF;
        setZ(t == 0);
        setN(0);
        setH((t & 0x0F) == 0x00);
        return t;
    }

    /**
     * The 8bit DEC operation (s <- s - 1).
     * @param i
     *            register value to decrement
     * @return new value of the register
     */
    private int dec(final int i) {
        int t = (i - 1) & 0xFF;
        setZ(t == 0);
        setN(1);
        setH((t & 0x0F) == 0x0F);
        return t;
    }

    /**
     * Operation ADD A,s (s is a 8bit value) (A <- A + s).
     * @param s
     *            The register value to add
     */
    private void add(final int s) {
        setN(0);
        calcH(a, s);
        a += s;
        if (a > 0xFF) {
            setC(1);
            a = a & 0xFF;
        } else {
            setC(0);
        }
        setZ(a == 0);
    }

    /**
     * Operation SUB s (s is a 8bit value) (A <- A - s).
     * @param s
     *            The register value to subtract
     */
    private void sub(final int s) {
        setN(1);
        calcHsub(a, s);
        a -= s;
        if (a < 0) {
            setC(1);
            a = a & 0xFF;
        } else {
            setC(0);
        }
        setZ(a == 0);

    }

    /**
     * Operation AND s (s is a 8bit value) (A <- A & s).
     * @param s
     *            The register value.
     */
    private void and(final int s) {
        setN(0);
        setH(1);
        setC(0);
        a = a & s;
        setZ(a == 0);
    }

    /**
     * 8bit XOR instruction (A <- A XOR s).
     * @param s
     *            Register value.
     */
    private void xor(final int s) {
        a = (a ^ s) & 0xFF;
        setZ(a == 0);
        setN(0);
        setH(0);
        setC(0);
    }

    /**
     * 8bit OR instruction (A <- A | s).
     * @param s
     *            Register value.
     */
    private void or(final int s) {
        a = a | s;
        setZ(a == 0);
        setN(0);
        setH(0);
        setC(0);
    }

    /**
     * Compares a value with register A (A - s).
     * @param s
     *            Register value.
     */
    private void cp(final int s) {
        int t = a - s;
        setN(1);
        calcHsub(a, s);
        setC(t < 0);
        setZ(t == 0);
    }

    /**
     * RET instruction. get (PC <- (SP), SP <- SP + 2)
     */
    private void ret() {
        pc = readSP();
        sp += 2;
    }

    /**
     * PUSH instruction.
     * @param s
     *            Value to push to the stack.
     */
    private void push(final int s) {
        sp -= 2;
        dblwrite(sp, s);
    }

    /**
     * POP instruction.
     * @return Value popped from stack.
     */
    private int pop() {
        int s = readSP();
        sp += 2;
        return s;
    }

    /**
     * CALL instruction.
     */
    private void call() {
        push(pc + 3);
        pc = readnn();
    }

    /**
     * BIT instruction. The Z flag is set to the same value as bit at position b
     * in s. (Z <- /sb).
     * @param bit
     *            bit number
     * @param s
     *            register value
     */
    private void bit(final int bit, final int s) {
        setN(0);
        setH(1);
        if ( ((s >>> bit) & 0x01) == 0 ) {
            setZ(1);
        } else {
            setZ(0);
        }
    }

    /**
     * RES instruction. Sets bit b in s to 0.
     * @param bit
     *            bit number
     * @param s
     *            register value
     * @return new value of s.
     */
    private int res(final int bit, final int s) {
        return s & (~(1 << bit));
    }

    /**
     * SET instruction. Sets bit b in s to 1.
     * @param bit
     *            bit number
     * @param s
     *            register value
     * @return new value of s.
     */
    private int set(final int bit, final int s) {
        return s | (1 << bit);
    }

    // Z
    /**
     * Changes the Z flag.
     * @param value
     *            If true the flag is set to 1, else the flag is set to 0.
     */
    private void setZ(final boolean value) {
        if (value) {
            cc = cc | 0x80;
        } else {
            cc = cc & 0x7F;
        }
    }

    /**
     * Changes the Z flag.
     * @param i
     *            If 0 the flag is set to 0, else the flag is set to 1.
     */
    private void setZ(final int i) {
        setZ(i != 0);
    }

    /**
     * Get value of flag Z.
     * @return the value of Z.
     */
    private int getZ() {
        return (cc >>> 7) & 0x01;
    }

    // N
    /**
     * Changes the N flag.
     * @param value
     *            If true the flag is set to 1, else the flag is set to 0.
     */
    private void setN(final boolean value) {
        if (value) {
            cc = cc | 0x40;
        } else {
            cc = cc & 0xBF;
        }
    }

    /**
     * Changes the N flag.
     * @param i
     *            If 0 the flag is set to 0, else the flag is set to 1.
     */
    private void setN(final int i) {
        setN(i != 0);
    }

    /**
     * Get value of flag N.
     * @return the value of N.
     */
    private int getN() {
        return (cc >>> 6) & 0x01;
    }

    // H
    /**
     * Changes the H flag.
     * @param value
     *            If true the flag is set to 1, else the flag is set to 0.
     */
    private void setH(final boolean value) {
        if (value) {
            cc = cc | 0x20;
        } else {
            cc = cc & 0xDF;
        }
    }

    /**
     * Changes the H flag.
     * @param i
     *            If 0 the flag is set to 0, else the flag is set to 1.
     */
    private void setH(final int i) {
        setH(i != 0);
    }

    /**
     * Get value of flag H.
     * @return the value of H.
     */
    private int getH() {
        return (cc >>> 5) & 0x01;
    }

    /**
     * Calculates the H flag for addition: v1 + v2.
     * @param v1
     *            value
     * @param v2
     *            another value
     */
    private void calcH(final int v1, final int v2) {
        setH(((v1 & 0x0F) + (v2 & 0x0F)) > 0x0F);
    }

    /**
     * Calculates the H flag for subtraction: v1 - v2.
     * @param v1
     *            value to subtract from
     * @param v2
     *            value to subtract
     */
    private void calcHsub(final int v1, final int v2) {
        setH(((v1 & 0x0F) - (v2 & 0x0F)) < 0x00);
    }

    // C
    /**
     * Changes the C flag.
     * @param value
     *            If true the flag is set to 1, else the flag is set to 0.
     */
    private void setC(final boolean value) {
        if (value) {
            cc = cc | 0x10;
        } else {
            cc = cc & 0xEF;
        }
    }

    /**
     * Changes the C flag.
     * @param i
     *            If 0 the flag is set to 0, else the flag is set to 1.
     */
    private void setC(final int i) {
        setC(i != 0);
    }

    /**
     * Get value of flag C.
     * @return the value of C.
     */
    private int getC() {
        return (cc >>> 4) & 0x01;
    }

    /**
     * Writes a 16bit value to memory.
     * @param address
     *            Address to write to
     * @param data16bit
     *            16bit value to be written.
     */
    private void dblwrite(final int address, final int data16bit) {
        ram.write(address, data16bit & 0xFF);
        ram.write(address + 1, (data16bit >>> 8) & 0xFF);
    }

    /**
     * Calculates the double register of two registers.
     * @param rl
     *            the left part of the double register.
     * @param rr
     *            the right part of the double register.
     * @return the value of the double register.
     */
    private int dblreg(final int rl, final int rr) {
        return rl * 0x100 + rr;
    }

    /*
     * Get and set methods for double registers
     */
    /**
     * Returns the value of the 16bit HL register.
     * @return The value of the HL register.
     */
    public int getHL() {
        return dblreg(h, l);
    }

    /**
     * Change the value of the double register HL.
     * @param largeInt
     *            The new 16bit value of HL.
     */
    public void setHL(final int largeInt) {
        h = (largeInt >> 8) & 0xFF;
        l = largeInt & 0xFF;
    }

    /**
     * Returns the value of the 16bit BC register.
     * @return The value of the BC register.
     */
    public int getBC() {
        return dblreg(b, c);
    }

    /**
     * Change the value of the double register BC.
     * @param largeInt
     *            The new 16bit value of BC.
     */
    public void setBC(final int largeInt) {
        b = (largeInt >> 8) & 0xFF;
        c = largeInt & 0xFF;
    }

    /**
     * Returns the value of the 16bit DE register.
     * @return The value of the DE register.
     */
    public int getDE() {
        return dblreg(d, e);
    }

    /**
     * Change the value of the double register DE.
     * @param largeInt
     *            The new 16bit value of DE.
     */
    public void setDE(final int largeInt) {
        d = (largeInt >> 8) & 0xFF;
        e = largeInt & 0xFF;
    }

    /**
     * Change the value of the double register AF. Note that F is the same as
     * register cc.
     * @param largeInt
     *            The new 16bit value of AF.
     */
    private void setAF(final int largeInt) {
        a = (largeInt >> 8) & 0xFF;
        cc = largeInt & 0xFF;
    }

    /**
     * Returns the value of the 16bit AF register. Note that F is the same as
     * register cc.
     * @return The value of the AF register.
     */
    private int getAF() {
        return dblreg(a, cc);
    }

    /**
     * Sets the A register.
     * @param smallInt
     *            New 8bit value of A
     */
    public void setA(final int smallInt) {
        a = smallInt;
    }

    /**
     * Returns the value of register A.
     * @return Value of register A.
     */
    public int getA() {
        return a;
    }

    /**
     * Sets the F (cc) register.
     * @param smallInt
     *            New 8bit value of F
     */
    public void setF(final int smallInt) {
        cc = smallInt;
    }

    /**
     * Returns the value of register F.
     * @return Value of register F.
     */
    public int getF() {
        return cc;
    }

    /**
     * Sets the SP register.
     * @param largeInt
     *            New 16bit value of SP.
     */
    public void setSP(final int largeInt) {
        sp = largeInt;
    }

    /**
     * Returns the value of SP.
     * @return Value of SP.
     */
    public int getSP() {
        return sp;
    }

    /**
     * Returns the value of PC.
     * @return Value of PC.
     */
    public int getPC() {
        return pc;
    }

    /**
     * Sets the PC register.
     * @param largeInt
     *            New 16bit value of PC.
     */
    public void setPC(final int largeInt) {
        pc = largeInt;
    }

    /**
     * Returns the state of the IME flag.
     * @return true if the IME flag is active, otherwise false.
     */
    public boolean getIME() {
        return ime;
    }

    /**
     * Changes the IME flag.
     * @param value
     *            New value of the IME flag.
     */
    public void setIME(final boolean value) {
        ime = value;
    }

    /**
     * Returns executeInterrupt.
     * @return executeInterrupt.
     */
    public boolean getExecuteInterrupt() {
        return executeInterrupt;
    }
}

