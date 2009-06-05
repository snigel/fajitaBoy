package fajitaboy.gb;

import static fajitaboy.constants.AddressConstants.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import fajitaboy.FileIOStreamHelper;
import fajitaboy.gb.memory.MemoryInterface;

/**
 * CPU class for emulating the Game Boy CPU.
 * @author Tobias S, Peter O
 */
public class Cpu implements StateMachine {

    /**
     * Address bus to access the memory.
     */
    protected MemoryInterface ram;

    /**
     * Program counter/pointer 8bit register.
     */
    protected int pc;

    /**
     * Flags in the flag register. ZNHC0000. These flags represent
     * the four upper bits in register F: ZNHC0000
     */
    private boolean flagZ;
    private boolean flagN;
    private boolean flagH;
    private boolean flagC;

    /**
     * Stack Pointer 16bit register.
     */
    protected int sp;

    /**
     * Interrupt Master Enable.
     */
    protected boolean ime;

    // 8bit registers:
    /**Det g�r den faktiskt nu, grejen �r att det kommer �nd� att komma negativa v�rden till
     * The H register.
     */
    protected int h;

    /**
     * The L register.
     */
    protected int l;

    /**
     * The A register.
     */
    protected int a;

    /**
     * The B register.
     */
    protected int b;

    /**
     * The C register.
     */
    protected int c;

    /**
     * The D register.
     */
    protected int d;

    /**
     * The E register.
     */
    protected int e;

    // Internal variables:
    /**
     * temp variables to use in runInstruction().
     */
    protected int temp;

    /**
     * to know if an interrupt should be executed.
     */
    protected boolean executeInterrupt;
    
    /**
     * Address to jump to during interrupts.
     */
    protected int interruptJumpAddress;

    /**
     * Bit to turn off on interrupt execution
     */
    protected int interruptBit;
    
    protected int ieReg;
    protected int ifReg;
    
    /**
     * Creates a new CPU with default values.
     * @param addressbus
     *            The addressBus
     */
    
    /**
     * Halts the CPU until an interrupt occurs if active.
     */
    protected boolean stop;
    
    public Cpu(final MemoryInterface addressbus) {
        ram = addressbus;
        reset();
    }

    /**
     * Sets CPU to start state.
     */
    public void reset() {
        pc = 0x0100;
        flagZ = true;
        flagN = false;
        flagH = true;
        flagC = true;
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

        return cycleTime;
    }
    
    public void findInterrupts() {
        interruptJumpAddress = 0x0000;
        interruptBit = 0xFF;
        executeInterrupt = false;

        // Look for interrupts
        ieReg = ram.read(ADDRESS_IE);
        ifReg = ram.read(ADDRESS_IF);
        if ( ifReg != 0 && ieReg != 0) {
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
    protected void handleInterrupts() {
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
    protected int runInstruction(final int instruction) {
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
            flagZ = false;
            flagN = false;
            flagH = false;
            a = a << 1;
            if (a > 0xFF) {
                flagC = true;
                a = (a & 0xFF) + 1;
            } else {
                flagC = false;
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
            flagH = (((getHL() & 0x0FFF) + (getBC() & 0x0FFF)) > 0x0FFF);
            setHL(temp);
            flagN = false;
            flagC = temp > 0xFFFF;
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
            if ((a & 0x01) == 1) {
                a = a | 0x0100;
                flagC = true;
            } else {
                flagC = false;
            }
            a = a >>> 1;
            //cc &= 0x1f;  // Performance!
            flagZ = false;
            flagN = false;
            flagH = false;
            pc++;
            cycleTime += 4;
            break;
        case 0x10: // STOP
            stop = true;
            cycleTime += 4;
            pc++;           
            break;
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
            flagZ = false;
            flagN = false;
            flagH = false;
            a = a << 1;
            if (flagC) {
                a++;
            }
            if (a > 0xFF) {
                flagC = true;
                a = a & 0xFF;
            } else {
                flagC = false;
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
            flagH = (((getHL() & 0x0FFF) + (getDE() & 0x0FFF)) > 0x0FFF);
            setHL(temp);
            flagN = false;
            flagC = temp > 0xFFFF;
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
            flagZ = false;
            flagN = false;
            flagH = false;
            if (flagC) {
                a = a | 0x100;
            }
            flagC = (a & 0x01) == 1;
            a = a >>> 1;
            pc++;
            cycleTime += 4;
            break;

        case 0x20: // JR NZ+e
            if (!flagZ) {
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
            if (a > 0x99 || flagC) {
                correctionFactor = 0x60;
                flagC = true;
            } else {
                correctionFactor = 0x00;
                flagC = false;
            }
            if ((a & 0x0F) > 0x09 || flagH) {
                correctionFactor |= 0x06;
            }
            if (!flagN) {
                calcH(a, correctionFactor);
                a = (a + correctionFactor) & 0xFF;
            } else {
                calcHsub(a, correctionFactor);
                a = (a - correctionFactor) & 0xFF;
            }
            flagZ = a == 0;
            pc++;
            cycleTime += 4;
            break;
        case 0x28: // JR Z,d (Jump+n if Z=1)
            if (flagZ) {
                pc = pc + (byte) readn() + 2;
                cycleTime += 12;
            } else {
                pc += 2;
                cycleTime += 8;
            }
            break;

        case 0x29: // ADD HL,HL
            temp = getHL() + getHL();
            flagH = (((getHL() & 0x0FFF) + (getHL() & 0x0FFF)) > 0x0FFF);
            flagN = false;
            flagC = temp > 0xFFFF;
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
            flagN = true;
            flagH = true;
            pc++;
            cycleTime += 4;
            break;

        case 0x30: // JR NC,d
            if (!flagC) {
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
            flagC = true;
            flagN = false;
            flagH = false;
            pc++;
            cycleTime += 4;
            break;
        case 0x38: // JR C,d
            if (flagC) {
                pc = pc + (byte) readn() + 2;
                cycleTime += 12;
            } else {
                pc += 2;
                cycleTime += 8;
            }
            break;
        case 0x39: // ADD HL,SP
            temp = getHL() + sp;
            flagH = ((getHL() & 0x0FFF) + (sp & 0x0FFF)) > 0x0FFF;
            setHL(temp);
            flagN = false;
            flagC = temp > 0xFFFF;
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
            flagC = !flagC;
            flagN = false;
            flagH = false;
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
            adc(b);
            pc++;
            cycleTime += 4;
            break;
        case 0x89: // ADC A,C
            adc(c);
            pc++;
            cycleTime += 4;
            break;
        case 0x8a: // ADC A,D
            adc(d);
            pc++;
            cycleTime += 4;
            break;
        case 0x8b: // ADC A,E
            adc(e);
            pc++;
            cycleTime += 4;
            break;
        case 0x8c: // ADC A,H
            adc(h);
            pc++;
            cycleTime += 4;
            break;
        case 0x8d: // ADC A,L
            adc(l);
            pc++;
            cycleTime += 4;
            break;
        case 0x8e: // ADC A,(HL)
            adc(ram.read(getHL()) & 0xff);
            pc++;
            cycleTime += 8;
            break;
        case 0x8f: // ADC A,A
            adc(a);
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
            sbc(b);
            pc++;
            cycleTime += 4;
            break;
        case 0x99: // SBC A,C
            sbc(c);
            pc++;
            cycleTime += 4;
            break;
        case 0x9A: // SBC A,D
            sbc(d);
            pc++;
            cycleTime += 4;
            break;
        case 0x9B: // SBC A,E
            sbc(e);
            pc++;
            cycleTime += 4;
            break;
        case 0x9C: // SBC A,H
            sbc(h);
            pc++;
            cycleTime += 4;
            break;
        case 0x9D: // SBC A,L
            sbc(l);
            pc++;
            cycleTime += 4;
            break;
        case 0x9e: // SBC A,(HL)
            sbc(ram.read(getHL()) & 0xFF);
            pc++;
            cycleTime += 8;
            break;
        case 0x9f: // SBC A,A
            sbc(a);
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
            if (!flagZ) {
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
            if (!flagZ) {
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
            if (!flagZ) {
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
            if (flagZ) {
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
            if (flagZ) {
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
            if (flagZ) {
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
            adc(readn());
            pc += 2;
            cycleTime += 8;
            break;
        case 0xcf: // RST 8
            push(pc + 1);
            pc = 8;
            cycleTime += 16;
            break;
        case 0xd0: // RET NC
            if (!flagC) {
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
            if (!flagC) {
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
            if (!flagC) {
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
            if (flagC) {
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
            if (flagC) {
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
            if (flagC) {
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
            sbc(readn());
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
            flagZ = false;
            flagN = false;
            if (temp < 0) { // Negative
                flagC = (sp + temp) < 0;
                calcHsub(sp, -temp);
            } else { // Positive
                flagC = (sp + temp) > 0xFFFF;
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
            flagZ = false;
            flagN = false;
            setHL(sp + temp);
            if (temp < 0) { // Negative
                flagC = (sp + temp) < 0;
                calcHsub(sp, -temp);

            } else { // Positive
                flagC = (sp + temp) > 0xFFFF;
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
    protected int prefixCB(int op, int r) {
        if (op < 0x40) {
            switch (op >>> 3) {
            case 0: // RLC
                flagN = false;
                flagH = false;
                r = r << 1;
                if (r > 0xFF) {
                    flagC = true;
                    r = (r & 0xFF) + 1;
                } else {
                    flagC = false;
                }
                flagZ = r == 0;
                break;
            case 1: // RRC
                flagN = false;
                flagH = false;
                flagC = (r & 0x01) == 1;
                r = r >>> 1;
                if (flagC) {
                    r = r | 0x80;
                }
                flagZ = r == 0;
                break;
            case 2: // RL
                flagN = false;
                flagH = false;
                r = r << 1;
                if (flagC) {
                    r++;
                }
                if (r > 0xFF) {
                    flagC = true;
                    r = r & 0xFF;
                } else {
                    flagC = false;
                }
                flagZ = r == 0;
                break;
            case 3: // RR
                flagN = false;
                flagH = false;
                if (flagC) {
                    r = r | 0x100;
                }
                flagC = (r & 0x01) == 1;
                r = r >>> 1;
                flagZ = r == 0;
                break;
            case 4: // SLA
                flagN = false;
                flagH = false;
                r = r << 1;
                if (r > 0xFF) {
                    flagC = true;
                    r = r & 0xFF;
                } else {
                    flagC = false;
                }
                flagZ = r == 0;
                break;
            case 5: // SRA
                flagN = false;
                flagH = false;
                int b7 = r & 0x80;
                flagC = (r & 0x01) == 1;
                r = r >>> 1;
                r += b7;
                flagZ = r == 0;
                break;
            case 6: // SWAP
                flagN = false;
                flagH = false;
                flagC = false;
                int highNibble = r >>> 4;
                int lowNibble = r & 0x0F;
                r = lowNibble * 0x10 + highNibble;
                flagZ = r == 0;
                break;
            case 7: // SRL
                flagN = false;
                flagH = false;
                flagC = (r & 0x01) == 1;
                r = r >>> 1;
                flagZ = r == 0;
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
    protected int readn() {
        return ram.read(pc + 1);
    }

    /**
     * Reads the two bytes after pc.
     * @return the two byte after pc as a 16bit value.
     */
    protected int readnn() {
        return (ram.read(pc + 1) + ram.read(pc + 2) * 0x100);
    }

    /**
     * Reads the 16bit value where sp is pointing.
     * @return A 16 bit value
     */
    protected int readSP() {
        return (ram.read(sp) + ram.read(sp + 1) * 0x100);
    }

    /**
     * The 8bit INC operation (s <- s + 1).
     * @param i
     *            the register value to increment
     * @return new value of the register
     */
    protected int inc(final int i) {
        int t = (i + 1) & 0xFF;
        flagZ = t == 0;
        flagN = false;
        flagH = (t & 0x0F) == 0x00;
        return t;
    }

    /**
     * The 8bit DEC operation (s <- s - 1).
     * @param i
     *            register value to decrement
     * @return new value of the register
     */
    protected int dec(final int i) {
        int t = (i - 1) & 0xFF;
        flagZ = t == 0;
        flagN = true;
        flagH = (t & 0x0F) == 0x0F;
        return t;
    }

    /**
     * Operation ADD A,s (s is a 8bit value) (A <- A + s).
     * @param s
     *            The register value to add
     */
    protected void add(final int s) {
        flagN = false;
        calcH(a, s);
        a += s;
        if (a > 0xFF) {
            flagC = true;
            a = a & 0xFF;
        } else {
            flagC = false;
        }
        flagZ = a == 0;
    }
    
    /**
     * Operation ADC A,s (s is a 8bit value) (A <- A + s + c).
     * @param s
     *            The register value to add
     */
    protected void adc(final int s) {        
         flagN = false;
         calcH(a, s);
         a += s;
         if (flagC) {
             if (!flagH) {
            	 flagH = (a & 0x0F) == 0x0F;
             }
             a++;
         }
         if (a > 0xFF) {
             flagC = true;
             a = a & 0xFF;
         } else {
             flagC = false;
         }
         flagZ = a == 0;
    }    

    /**
     * Operation SUB s (s is a 8bit value) (A <- A - s).
     * @param s
     *            The register value to subtract
     */
    protected void sub(final int s) {
        flagN = true;
        calcHsub(a, s);
        a -= s;
        if (a < 0) {
            flagC = true;
            a = a & 0xFF;
        } else {
            flagC = false;
        }
        flagZ = a == 0;
    }
    
    /**
     * Operation SBC s (s is a 8bit value) (A <- A - s - c).
     * @param s
     *            The register value to subtract
     */
    protected void sbc(final int s) {
        flagN = true;
        calcHsub(a, s);
        a -= s;
        if (flagC) {
            if (!flagH) {
            	flagH = (a & 0x0F) == 0;
            }
            a--;
        }
        if (a < 0) {
            flagC = true;
            a = a & 0xFF;
        } else {
            flagC = false;
        }
        flagZ = a == 0;
    }

    /**
     * Operation AND s (s is a 8bit value) (A <- A & s).
     * @param s
     *            The register value.
     */
    protected void and(final int s) {
        flagN = false;
        flagH = true;
        flagC = false;
        a = a & s;
        flagZ = a == 0;
    }

    /**
     * 8bit XOR instruction (A <- A XOR s).
     * @param s
     *            Register value.
     */
    protected void xor(final int s) {
        a = (a ^ s) & 0xFF;
        flagZ = a == 0;
        flagN = false;
        flagH = false;
        flagC = false;
    }

    /**
     * 8bit OR instruction (A <- A | s).
     * @param s
     *            Register value.
     */
    protected void or(final int s) {
        a = a | s;
        flagZ = a == 0;
        flagN = false;
        flagH = false;
        flagC = false;
    }

    /**
     * Compares a value with register A (A - s).
     * @param s
     *            Register value.
     */
    protected void cp(final int s) {
        int t = a - s;
        flagN = true;
        calcHsub(a, s);
        flagC = t < 0;
        flagZ = t == 0;
    }

    /**
     * RET instruction. get (PC <- (SP), SP <- SP + 2)
     */
    protected void ret() {
        pc = readSP();
        sp += 2;
    }

    /**
     * PUSH instruction.
     * @param s
     *            Value to push to the stack.
     */
    protected void push(final int s) {
        sp -= 2;
        dblwrite(sp, s);
    }

    /**
     * POP instruction.
     * @return Value popped from stack.
     */
    protected int pop() {
        int s = readSP();
        sp += 2;
        return s;
    }

    /**
     * CALL instruction.
     */
    protected void call() {
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
    protected void bit(final int bit, final int s) {
        flagN = false;
        flagH = true;
        if ( ((s >>> bit) & 0x01) == 0 ) {
            flagZ = true;
        } else {
            flagZ = false;
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
    protected int res(final int bit, final int s) {
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
    protected int set(final int bit, final int s) {
        return s | (1 << bit);
    }

    /**
     * Calculates the H flag for addition: v1 + v2.
     * @param v1
     *            value
     * @param v2
     *            another value
     */
    protected void calcH(final int v1, final int v2) {
        flagH = ((v1 & 0x0F) + (v2 & 0x0F)) > 0x0F;
    }

    /**
     * Calculates the H flag for subtraction: v1 - v2.
     * @param v1
     *            value to subtract from
     * @param v2
     *            value to subtract
     */
    protected void calcHsub(final int v1, final int v2) {
        flagH = ((v1 & 0x0F) - (v2 & 0x0F)) < 0x00;
    }


    /**
     * Writes a 16bit value to memory.
     * @param address
     *            Address to write to
     * @param data16bit
     *            16bit value to be written.
     */
    protected void dblwrite(final int address, final int data16bit) {
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
    protected int dblreg(final int rl, final int rr) {
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
    protected void setAF(final int largeInt) {
        a = (largeInt >> 8) & 0xFF;
        setF(largeInt & 0xFF);
    }

    /**
     * Returns the value of the 16bit AF register. Note that F is the same as
     * register cc.
     * @return The value of the AF register.
     */
    protected int getAF() {
        return dblreg(a, getF());
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
        flagZ = (smallInt & 0x80) != 0;
        flagN = (smallInt & 0x40) != 0;
        flagH = (smallInt & 0x20) != 0;
        flagC = (smallInt & 0x10) != 0;
    }

    /**
     * Returns the value of register F.
     * @return Value of register F.
     */
    public int getF() {
        return (flagZ ? 0x80 : 0) | (flagN ? 0x40 : 0) | 
               (flagH ? 0x20 : 0) | (flagC ? 0x10 : 0);
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
    
    /**
     * {@inheritDoc}
     */
    public void saveState( FileOutputStream os ) throws IOException {
        FileIOStreamHelper.writeData(os, pc, 2);
        FileIOStreamHelper.writeData(os, sp, 2);
        
        // Write registers
        os.write(a);
        os.write(b);
        os.write(c);
        os.write(d);
        os.write(e);
        os.write(getF());
        os.write(h);
        os.write(l);
        
        // Write flags
        FileIOStreamHelper.writeBoolean(os, ime);
        FileIOStreamHelper.writeBoolean(os, stop);
    }
    
    /**
     * {@inheritDoc}
     */
    public void readState( FileInputStream is ) throws IOException {
        // Read PC and SP
        pc = (int)FileIOStreamHelper.readData(is, 2);
        sp = (int)FileIOStreamHelper.readData(is, 2);
        
        // Read registers
        a = is.read();
        b = is.read();
        c = is.read();
        d = is.read();
        e = is.read();
        setF(is.read());
        h = is.read();
        l = is.read();
        
        // Read flags
        ime = FileIOStreamHelper.readBoolean(is);
        stop = FileIOStreamHelper.readBoolean(is);
    }
}
