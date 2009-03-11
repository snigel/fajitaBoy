package fajitaboy;

import static fajitaboy.constants.AddressConstants.*;
import static fajitaboy.constants.HardwareConstants.*;

/**
 * CPU class for emulating the Game Boy CPU.
 * 
 * @author Tobias S, Peter O
 */
public final class Cpu {

    /**
     * Address bus to access the memory.
     */
    private AddressBus ram;

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
    /**
     * The H register.
     */
    private int h = 0;

    /**
     * The L register.
     */
    private int l = 0;

    /**
     * The A register.
     */
    private int a = 0;

    /**
     * The B register.
     */
    private int b = 0;

    /**
     * The C register.
     */
    private int c = 0;

    /**
     * The D register.
     */
    private int d = 0;

    /**
     * The E register.
     */
    private int e = 0;

    // Internal variables:
    /**
     * temp variables to use in runInstruction().
     */
    private int t1;

    /**
     * to know if an interrupt should be executed.
     */
    private boolean executeInterrupt;

    /**
     * CPU cycle counter.
     */
    private int cycleTime;

    /**
     * Creates a new CPU with default values.
     * 
     * @param ram
     *            The addressBus
     */
    public Cpu(AddressBus ram) {
        this.ram = ram;
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
        cycleTime = 0;
        ime = true;
        executeInterrupt = false;
    }

    /**
     * Steps the CPU one step.
     * Next instruction is executed if no interrupt is fired.
     * @return Returns the number of clock cycles used in this step.
     */
    public int step() {
        // Initialize vars
        cycleTime = 0;
        executeInterrupt = false;
        int jumpAddress = 0x0000;

        // Interrupt handler
        if (ime) {
            // Look for interrupts
            int ieReg = ram.read(ADDRESS_IE);
            int ifReg = ram.read(ADDRESS_IF);

            if ((ieReg & 0x01) != 0 && (ifReg & 0x01) != 0) {
                // V-Blank interrupt
                executeInterrupt = true;
                jumpAddress = ADDRESS_INT_VBLANK;
                ram.write(ADDRESS_IF, ifReg & 0xFE);
            } else if ((ieReg & 0x02) != 0 && (ifReg & 0x02) != 0) {
                // LCD Status interrupt
                executeInterrupt = true;
                jumpAddress = ADDRESS_INT_LCDSTAT;
                ram.write(ADDRESS_IF, ifReg & 0xFD);
            } else if ((ieReg & 0x04) != 0 && (ifReg & 0x04) != 0) {
                // Timer interrupt
                executeInterrupt = true;
                jumpAddress = ADDRESS_INT_TIMER;
                ram.write(ADDRESS_IF, ifReg & 0xFB);
            } else if ((ieReg & 0x08) != 0 && (ifReg & 0x08) != 0) {
                // Serial interrupt
                executeInterrupt = true;
                jumpAddress = ADDRESS_INT_SERIAL;
                ram.write(ADDRESS_IF, ifReg & 0xF7);
            } else if ((ieReg & 0x10) != 0 && (ifReg & 0x10) != 0) {
                // Joypad interrupt
                executeInterrupt = true;
                jumpAddress = ADDRESS_INT_JOYPAD;
                ram.write(ADDRESS_IF, ifReg & 0xEF);
            }
        }

        if (executeInterrupt) {
            ime = false;
            sp -= 2;
            dblwrite(sp, pc);
            pc = jumpAddress;
        } else {
            // Perform processor operation
            int inst = ram.read(pc);
            runInstruction(inst);
        }

        return cycleTime;
    }

    /**
     * Runs specified instruction.
     * @param instruction The opcode of the instruction to run.
     */
    private void runInstruction(final int instruction) {
        switch (instruction) {
        case 0x00: // NOP
            pc++;
            addCycles(4);
            break;
        case 0x01: // LD BC,nn
            setBC(readnn());
            pc += 3;
            addCycles(12);
            break;
        case 0x02: // LD (BC), A
            ram.write(getBC(), a);
            pc++;
            addCycles(8);
            break;
        case 0x03: // INC BC
            setBC(getBC() + 1);
            pc++;
            addCycles(8);
            break;
        case 0x04: // INC B
            b = inc(b);
            pc++;
            addCycles(4);
            break;
        case 0x05: // DEC B
            b = dec(b);
            pc++;
            addCycles(4);
            break;

        case 0x06: // LD B, n
            b = readn();
            pc += 2;
            addCycles(8);
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
            addCycles(4);
            break;
        case 0x08: // LD (nn),SP
            dblwrite(readnn(), sp);
            pc += 3;
            addCycles(20);
            break;
        case 0x09: // ADD HL,BC
            t1 = getHL() + getBC();
            setH(((getHL() & 0x0F) + (getBC() & 0x0F)) > 0x0F);
            setHL(t1);
            setN(0);
            setC(t1 > 0xFFFF);
            pc++;
            addCycles(8);
            break;
        case 0x0a: // LD A,(BC)
            a = ram.read(getBC());
            pc++;
            addCycles(8);
            break;
        case 0x0b: // DEC BC
            setBC(getBC() - 1);
            pc++;
            addCycles(8);
            break;
        case 0x0c: // INC C
            c = inc(c);
            pc++;
            addCycles(4);
            break;
        case 0x0d: // DEC C
            c = dec(c);
            pc++;
            addCycles(4);
            break;
        case 0x0e: // LD C,n
            c = readn();
            pc += 2;
            addCycles(8);
            break;
        case 0x0f: // RRCA
            setC((a & 0x01) == 1);
            a = a >>> 1;
            if (getC() == 1) {
                a = a | 0x80;
            }
            setZ(0);
            setN(0);
            setH(0);
            pc++;
            addCycles(4);
            break;
        // case 0x10:logln("STOP");break;
        case 0x11: // LD DE,nn
            setDE(readnn());
            pc += 3;
            addCycles(12);
            break;
        case 0x12: // LD (DE),A
            ram.write(getDE(), a);
            pc++;
            addCycles(8);
            break;
        case 0x13: // INC DE
            setDE(getDE() + 1);
            pc++;
            addCycles(8);
            break;
        case 0x14: // INC D
            d = inc(d);
            pc++;
            addCycles(4);
            break;
        case 0x15: // DEC D
            d = dec(d);
            pc++;
            addCycles(4);
            break;
        case 0x16: // LD D,n
            d = readn();
            pc += 2;
            addCycles(8);
            break;
        case 0x17: // RLA
            setZ(0);
            setN(0);
            setH(0);
            a = a << 1;
            if (getC() == 1) {
                a++;
            }
            if (a > 0xFF) {
                setC(1);
                a = (a & 0xFF);
            } else {
                setC(0);
            }
            pc++;
            addCycles(4);
            break;
        case 0x18: // JR d
            pc = pc + (byte) ram.read(pc + 1) + 2;
            addCycles(12);
            break;
        case 0x19: // ADD HL,DE
            t1 = getHL() + getDE();
            setH(((getHL() & 0x0F) + (getDE() & 0x0F)) > 0x0F);
            setHL(t1);
            setN(0);
            setC(t1 > 0xFFFF);
            pc++;
            addCycles(8);
            break;
        case 0x1a: // LD A,(DE)
            a = ram.read(getDE());
            pc++;
            addCycles(8);
            break;
        case 0x1b: // DEC DE
            setDE(getDE() - 1);
            pc++;
            addCycles(8);
            break;
        case 0x1c: // INC e
            e = inc(e);
            pc++;
            addCycles(4);
            break;
        case 0x1d: // DEC e
            e = dec(e);
            pc++;
            addCycles(4);
            break;
        case 0x1e: // LD E,n
            e = readn();
            pc += 2;
            addCycles(8);
            break;
        case 0x1f: // RRA
            setZ(0);
            setN(0);
            setH(0);
            if (getC() == 1) {
                a = a | 0x100;
            }
            setC((a & 0x01) == 1);
            a = a >>> 1;
            pc++;
            addCycles(4);
            break;

        case 0x20: // JR NZ+e
            if (getZ() == 0) {
                pc += (byte) readn() + 2;
                addCycles(12);
            } else {
                pc += 2;
                addCycles(8);
            }
            break;
        case 0x21: // LD HL,nn
            setHL(readnn());
            pc += 3;
            addCycles(12);
            break;
        case 0x22: // LDI (HL),A
            ram.write(getHL(), a);
            setHL(getHL() + 1);
            pc++;
            addCycles(8);
            break;
        case 0x23: // INC HL
            setHL(getHL() + 1);
            pc++;
            addCycles(8);
            break;
        case 0x24: // INC H
            h = inc(h);
            pc++;
            addCycles(4);
            break;
        case 0x25: // DEC H
            h = dec(h);
            pc++;
            addCycles(4);
            break;
        case 0x26: // LD H,n
            h = readn();
            pc += 2;
            addCycles(8);
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
            addCycles(4);
            break;
        case 0x28: // JR Z,d (Jump+n if Z=1)
            if (getZ() == 1) {
                pc = pc + (byte) readn() + 2;
                addCycles(12);
            } else {
                pc += 2;
                addCycles(8);
            }
            break;

        case 0x29: // ADD HL,HL
            t1 = getHL() * 2;
            calcH(getHL(), getHL());
            setN(0);
            setC(t1 > 0xFFFF);
            setHL(t1);
            pc++;
            addCycles(8);
            break;
        case 0x2a: // LDI A,(HL)
            a = ram.read(getHL());
            setHL(getHL() + 1);
            pc++;
            addCycles(8);
            break;
        case 0x2b: // DEC HL
            setHL(getHL() - 1);
            pc++;
            addCycles(8);
            break;
        case 0x2c: // INC L
            l = inc(l);
            pc++;
            addCycles(4);
            break;
        case 0x2d: // DEC L
            l = dec(l);
            pc++;
            addCycles(4);
            break;
        case 0x2e: // LD l,n
            l = readn();
            pc += 2;
            addCycles(8);
            break;
        case 0x2f: // CPL
            a = ~a & 0xff;
            setN(1);
            setH(1);
            pc++;
            addCycles(4);
            break;

        case 0x30: // JR NC,d
            if (getC() == 0) {
                pc = pc + (byte) readn() + 2;
                addCycles(12);
            } else {
                pc += 2;
                addCycles(8);
            }
            break;
        case 0x31: // LD SP,nn
            sp = readnn();
            pc += 3;
            addCycles(12);
            break;
        case 0x32: // LDD (HL),A; HL-
            ram.write(getHL(), a);
            setHL(getHL() - 1);
            pc++;
            addCycles(8);
            break;
        case 0x33: // INC SP
            sp = (sp + 1) & 0xFFFF;
            pc++;
            addCycles(8);
            break;
        case 0x34: // INC (HL)
            ram.write(getHL(), inc(ram.read(getHL())));
            pc++;
            addCycles(12);
            break;
        case 0x35: // DEC (HL)
            ram.write(getHL(), dec(ram.read(getHL())));
            pc++;
            addCycles(12);
            break;
        case 0x36: // LD (HL),n
            ram.write(getHL(), readn());
            pc++;
            addCycles(12);
            break;
        case 0x37: // SCF
            setC(1);
            setN(0);
            setH(0);
            addCycles(4);
            break;
        case 0x38: // JR C,d
            if (getC() == 1) {
                pc = pc + (byte) readn() + 2;
                addCycles(12);
            } else {
                pc += 2;
                addCycles(8);
            }
            break;
        case 0x39: // ADD HL,SP
            t1 = getHL() + sp;
            calcH(getHL(), sp);
            setHL(t1);
            setN(0);
            setC(t1 > 0xFFFF);
            pc++;
            addCycles(8);
            break;
        case 0x3a: // LDD A,(HL)
            a = ram.read(getHL());
            setHL(getHL() - 1);
            pc++;
            addCycles(8);
            break;
        case 0x3b: // DEC SP
            sp = (sp - 1) & 0xFFFF;
            pc++;
            addCycles(8);
            break;
        case 0x3c: // INC A
            a = inc(a);
            pc++;
            addCycles(4);
            break;
        case 0x3d: // DEC A
            a = dec(a);
            pc++;
            addCycles(4);
            break;
        case 0x3e: // LD A, n
            a = readn();
            pc += 2;
            addCycles(8);
            break;
        case 0x3f: // CCF
            setC(getC() == 0);
            setN(0);
            setH(0);
            pc++;
            addCycles(4);
            break;
        case 0x40: // LD B,B
            // b = b;
            pc++;
            addCycles(4);
            break;
        case 0x41: // LD B,C
            b = c;
            pc++;
            addCycles(4);
            break;
        case 0x42: // LD B,D
            b = d;
            pc++;
            addCycles(4);
            break;
        case 0x43: // LD B,E
            b = e;
            pc++;
            addCycles(4);
            break;
        case 0x44: // LD B,H
            b = h;
            pc++;
            addCycles(4);
            break;
        case 0x45: // LD B,L
            b = l;
            pc++;
            addCycles(4);
            break;
        case 0x46: // LD B,(HL)
            b = ram.read(getHL());
            pc++;
            addCycles(8);
            break;
        case 0x47: // LD B,A
            b = a;
            pc++;
            break;
        case 0x48: // LD C,B
            c = b;
            pc++;
            addCycles(4);
            break;
        case 0x49: // LD C,C
            // c=c;
            pc++;
            addCycles(4);
            break;
        case 0x4a: // LD C,D
            c = d;
            pc++;
            addCycles(4);
            break;
        case 0x4b: // LD C,E
            c = e;
            pc++;
            addCycles(4);
            break;
        case 0x4c: // LD C,H
            c = h;
            pc++;
            addCycles(4);
            break;
        case 0x4d: // LD C,L
            c = l;
            pc++;
            addCycles(4);
            break;
        case 0x4e: // LD C,(HL)
            c = ram.read(getHL());
            pc++;
            addCycles(8);
            break;
        case 0x4f: // LD C,A
            c = a;
            pc++;
            addCycles(4);
            break;
        case 0x50: // LD D,B
            d = b;
            pc++;
            addCycles(4);
            break;
        case 0x51: // LD D,C
            d = c;
            pc++;
            addCycles(4);
            break;
        case 0x52: // LD D,D
            // d=d;
            pc++;
            addCycles(4);
            break;
        case 0x53: // LD D,E
            d = e;
            pc++;
            addCycles(4);
            break;
        case 0x54: // LD D,H
            d = h;
            pc++;
            addCycles(4);
            break;
        case 0x55: // LD D,L
            d = l;
            pc++;
            addCycles(4);
            break;
        case 0x56: // LD D,(HL)
            d = ram.read(getHL());
            pc++;
            addCycles(8);
            break;
        case 0x57: // LD D,A
            d = a;
            pc++;
            break;
        case 0x58: // LD E,B
            e = b;
            pc++;
            addCycles(4);
            break;
        case 0x59: // LD E,C
            e = c;
            pc++;
            addCycles(4);
            break;
        case 0x5A: // LD E,D
            e = d;
            pc++;
            addCycles(4);
            break;
        case 0x5b: // LD E,E
            // e=e;
            pc++;
            addCycles(4);
            break;
        case 0x5c: // LD E,H
            e = h;
            pc++;
            addCycles(4);
            break;
        case 0x5d: // LD E,L
            e = l;
            pc++;
            addCycles(4);
            break;
        case 0x5e: // LD E,(HL)
            e = ram.read(getHL());
            pc++;
            addCycles(8);
            break;
        case 0x5f: // LD E,A
            e = a;
            pc++;
            addCycles(4);
            break;
        case 0x60: // LD H,B
            h = b;
            pc++;
            addCycles(4);
            break;
        case 0x61: // LD H,C
            h = c;
            pc++;
            addCycles(4);
            break;
        case 0x62: // LD H,D
            d = c;
            pc++;
            addCycles(4);
            break;
        case 0x63: // LD H,E
            h = e;
            pc++;
            addCycles(4);
            break;
        case 0x64: // LD H,H
            // h=h;
            pc++;
            addCycles(4);
            break;
        case 0x65: // LD H,L
            h = l;
            pc++;
            addCycles(4);
            break;
        case 0x66: // LD H,(HL)
            h = ram.read(getHL());
            pc++;
            addCycles(8);
            break;
        case 0x67: // LD H,A
            h = a;
            pc++;
            addCycles(4);
            break;
        case 0x68: // LD L,B
            l = b;
            pc++;
            addCycles(4);
            break;
        case 0x69: // LD L,C
            l = c;
            pc++;
            addCycles(4);
            break;
        case 0x6a: // LD L,D
            l = d;
            pc++;
            addCycles(4);
            break;
        case 0x6b: // LD L,E
            l = e;
            pc++;
            addCycles(4);
            break;
        case 0x6c: // LD L,H
            l = h;
            pc++;
            addCycles(4);
            break;
        case 0x6d: // LD L,L
            // l=l;
            pc++;
            addCycles(4);
            break;
        case 0x6e: // LD L,(HL)
            l = ram.read(getHL());
            pc++;
            addCycles(4);
            break;
        case 0x6f: // LD L,A
            l = a;
            pc++;
            addCycles(4);
            break;

        case 0x70: // LD (HL),B
            ram.write(getHL(), b);
            pc++;
            addCycles(8);
            break;
        case 0x71: // LD (HL),C
            ram.write(getHL(), c);
            pc++;
            addCycles(8);
            break;
        case 0x72: // LD (HL),D
            ram.write(getHL(), d);
            pc++;
            addCycles(8);
            break;
        case 0x73: // LD (HL),E
            ram.write(getHL(), e);
            pc++;
            addCycles(8);
            break;
        case 0x74: // LD (HL),H
            ram.write(getHL(), h);
            pc++;
            addCycles(8);
            break;
        case 0x75: // LD (HL),L
            ram.write(getHL(), l);
            pc++;
            addCycles(8);
            break;
        // case 0x76: logln("HALT"); break;
        case 0x77: // LD (HL),A
            ram.write(getHL(), a);
            pc++;
            addCycles(8);
            break;
        case 0x78: // LD A,B
            a = b;
            pc++;
            addCycles(4);
            break;
        case 0x79: // LD A,C
            a = c;
            pc++;
            addCycles(4);
            break;
        case 0x7A: // LD A,D
            a = d;
            pc++;
            addCycles(4);
            break;
        case 0x7B: // LD A,E
            a = e;
            pc++;
            addCycles(4);
            break;
        case 0x7c: // LD A,H
            a = h;
            pc++;
            addCycles(4);
            break;
        case 0x7d: // LD A,L
            a = l;
            pc++;
            addCycles(4);
            break;
        case 0x7e: // LD A,(HL)
            a = ram.read(getHL());
            pc++;
            addCycles(8);
            break;
        case 0x7f: // LD A,A
            // a=a;
            pc++;
            addCycles(4);
            break;

        case 0x80: // ADD A,B
            add(b);
            pc++;
            addCycles(4);
            break;
        case 0x81: // ADD A,C
            add(c);
            pc++;
            addCycles(4);
            break;
        case 0x82: // ADD A,D
            add(d);
            pc++;
            addCycles(4);
            break;
        case 0x83: // ADD A,E
            add(e);
            pc++;
            addCycles(4);
            break;
        case 0x84: // ADD A,H
            add(h);
            pc++;
            addCycles(4);
            break;
        case 0x85: // ADD A,L
            add(l);
            pc++;
            addCycles(4);
            break;
        case 0x86: // ADD A,(HL)
            add(ram.read(getHL()));
            pc++;
            addCycles(8);
            break;
        case 0x87: // ADD A,A
            add(a);
            pc++;
            addCycles(4);
            break;

        case 0x88: // ADC A,B
            add(b + getC());
            pc++;
            addCycles(4);
            break;
        case 0x89: // ADC A,C
            add(c + getC());
            pc++;
            addCycles(4);
            break;
        case 0x8a: // ADC A,D
            add(d + getC());
            pc++;
            addCycles(4);
            break;
        case 0x8b: // ADC A,E
            add(e + getC());
            pc++;
            addCycles(4);
            break;
        case 0x8c: // ADC A,H
            add(h + getC());
            pc++;
            addCycles(4);
            break;
        case 0x8d: // ADC A,L
            add(l + getC());
            pc++;
            addCycles(4);
            break;
        case 0x8e: // ADC A,(HL)
            add(ram.read(getHL()) + getC());
            pc++;
            addCycles(8);
            break;
        case 0x8f: // ADC A,A
            add(a + getC());
            pc++;
            addCycles(4);
            break;

        case 0x90: // SUB B
            sub(b);
            pc++;
            addCycles(4);
            break;
        case 0x91: // SUB C
            sub(c);
            pc++;
            addCycles(4);
            break;
        case 0x92: // SUB D
            sub(d);
            pc++;
            addCycles(4);
            break;
        case 0x93: // SUB E
            sub(e);
            pc++;
            addCycles(4);
            break;
        case 0x94: // SUB H
            sub(h);
            pc++;
            addCycles(4);
            break;
        case 0x95: // SUB L
            sub(l);
            pc++;
            addCycles(4);
            break;
        case 0x96: // SUB (HL)
            sub(ram.read(getHL()));
            pc++;
            addCycles(8);
            break;
        case 0x97: // SUB A
            sub(a);
            pc++;
            addCycles(4);
            break;

        case 0x98: // SBC A,B
            sub(b + getC());
            pc++;
            addCycles(4);
            break;
        case 0x99: // SBC A,C
            sub(c + getC());
            pc++;
            addCycles(4);
            break;
        case 0x9A: // SBC A,D
            sub(d + getC());
            pc++;
            addCycles(4);
            break;
        case 0x9B: // SBC A,E
            sub(e + getC());
            pc++;
            addCycles(4);
            break;
        case 0x9C: // SBC A,H
            sub(h + getC());
            pc++;
            addCycles(4);
            break;
        case 0x9D: // SBC A,L
            sub(l + getC());
            pc++;
            addCycles(4);
            break;
        case 0x9e: // SBC A,(HL)
            sub(ram.read(getHL()) + getC());
            pc++;
            addCycles(8);
            break;
        case 0x9f: // SBC A,A
            sub(a + getC());
            pc++;
            addCycles(4);
            break;

        case 0xa0: // AND B
            and(b);
            pc++;
            addCycles(4);
            break;
        case 0xa1: // AND C
            and(c);
            pc++;
            addCycles(4);
            break;
        case 0xa2: // AND D
            and(d);
            pc++;
            addCycles(4);
            break;
        case 0xa3: // AND E
            and(e);
            pc++;
            addCycles(4);
            break;
        case 0xa4: // AND H
            and(h);
            pc++;
            addCycles(4);
            break;
        case 0xa5: // AND L
            and(l);
            pc++;
            addCycles(4);
            break;
        case 0xa6: // AND (HL)
            and(ram.read(getHL()));
            pc++;
            addCycles(8);
            break;
        case 0xa7: // AND A
            and(a);
            pc++;
            addCycles(4);
            break;

        case 0xa8: // XOR B
            xor(b);
            pc++;
            addCycles(4);
            break;
        case 0xa9: // XOR C
            xor(c);
            pc++;
            addCycles(4);
            break;
        case 0xaa: // XOR D
            xor(d);
            pc++;
            addCycles(4);
            break;
        case 0xab: // XOR E
            xor(e);
            pc++;
            addCycles(4);
            break;
        case 0xac: // XOR H
            xor(h);
            pc++;
            addCycles(4);
            break;
        case 0xad: // XOR L
            xor(l);
            pc++;
            addCycles(4);
            break;
        case 0xAE: // XOR (HL)
            xor(ram.read(getHL()));
            pc++;
            addCycles(8);
            break;
        case 0xAF: // XOR A
            xor(a);
            pc++;
            addCycles(4);
            break;

        case 0xB0: // OR B
            or(b);
            pc++;
            addCycles(4);
            break;
        case 0xB1: // OR C
            or(c);
            pc++;
            addCycles(4);
            break;
        case 0xB2: // OR D
            or(d);
            pc++;
            addCycles(4);
            break;
        case 0xb3: // OR E
            or(e);
            pc++;
            addCycles(4);
            break;
        case 0xb4: // OR H
            or(h);
            pc++;
            addCycles(4);
            break;
        case 0xb5: // OR L
            or(l);
            pc++;
            addCycles(4);
            break;
        case 0xb6: // OR (HL)
            or(ram.read(getHL()));
            pc++;
            addCycles(8);
            break;
        case 0xb7: // OR A
            or(a);
            pc++;
            addCycles(4);
            break;

        case 0xb8: // CP B
            cp(b);
            pc++;
            addCycles(4);
            break;
        case 0xb9: // CP C
            cp(c);
            pc++;
            addCycles(4);
            break;
        case 0xba: // CP D
            cp(d);
            pc++;
            addCycles(4);
            break;
        case 0xbb: // CP E
            cp(e);
            pc++;
            addCycles(4);
            break;
        case 0xbc: // CP H
            cp(h);
            pc++;
            addCycles(4);
            break;
        case 0xbd: // CP L
            cp(l);
            pc++;
            addCycles(4);
            break;
        case 0xbe: // CP (HL)
            cp(ram.read(getHL()));
            pc++;
            addCycles(8);
            break;
        case 0xbf: // CP A
            cp(a);
            pc++;
            addCycles(4);
            break;

        case 0xc0: // RET NZ
            if (getZ() == 0) {
                ret();
                addCycles(20);
            } else {
                pc++;
                addCycles(8);
            }
            break;
        case 0xc1: // POP BC
            setBC(pop());
            pc++;
            addCycles(12);
            break;
        case 0xc2: // JP NZ,nn
            if (getZ() == 0) {
                pc = readnn();
                addCycles(16);
            } else {
                pc += 3;
                addCycles(12);
            }
            break;
        case 0xc3: // JP nn
            pc = readnn();
            addCycles(16);
            break;
        case 0xc4: // CALL NZ,nn
            if (getZ() == 0) {
                call();
                addCycles(24);
            } else {
                pc += 3;
                addCycles(12);
            }
            break;
        case 0xc5: // PUSH BC
            push(getBC());
            pc++;
            addCycles(16);
            break;
        case 0xc6: // ADD A,n
            add(readn());
            pc += 2;
            addCycles(8);
            break;
        case 0xc7: // RST 0
            push(pc + 1);
            pc = 0;
            addCycles(16);
            break;
        case 0xc8: // RET Z
            if (getZ() == 1) {
                ret();
                addCycles(20);
            } else {
                pc++;
                addCycles(8);
            }
            break;
        case 0xc9: // RET
            ret();
            addCycles(16);
            break;
        case 0xca: // JP Z,nn
            if (getZ() == 1) {
                pc = readnn();
                addCycles(16);
            } else {
                pc += 3;
                addCycles(12);
            }
            break;
        case 0xcb: // CB Prefix
            int cbOp = readn();
            switch (cbOp & 0x07) {
            case 0: // B
                b = prefixCB(cbOp, b);
                addCycles(8);
                break;
            case 1: // C
                c = prefixCB(cbOp, c);
                addCycles(8);
                break;
            case 2: // D
                d = prefixCB(cbOp, d);
                addCycles(8);
                break;
            case 3: // E
                e = prefixCB(cbOp, e);
                addCycles(8);
                break;
            case 4: // H
                h = prefixCB(cbOp, h);
                addCycles(8);
                break;
            case 5: // L
                l = prefixCB(cbOp, l);
                addCycles(8);
                break;
            case 6: // (HL)
                ram.write(getHL(), prefixCB(cbOp, ram.read(getHL())));

                if (cbOp > 0x3f && cbOp < 0x80) {
                    // instruction is BIT
                    addCycles(12);
                } else {
                    // instruction is SET or RES
                    addCycles(16);
                }
                break;
            case 7: // A
                a = prefixCB(cbOp, a);
                addCycles(8);
                break;
            }
            pc += 2;
            break;
        case 0xcc: // CALL Z,nn
            if (getZ() == 1) {
                call();
                addCycles(24);
            } else {
                pc += 3;
                addCycles(12);
            }
            break;
        case 0xcd: // CALL nn
            call();
            addCycles(24);
            break;
        case 0xce: // ADC A,n
            add(readn() + getC());
            pc += 2;
            addCycles(8);
            break;
        case 0xcf: // RST 8
            push(pc + 1);
            pc = 8;
            addCycles(16);
            break;
        case 0xd0: // RET NC
            if (getC() == 0) {
                ret();
                addCycles(20);
            } else {
                pc++;
                addCycles(8);
            }
            break;
        case 0xd1: // POP DE
            setDE(pop());
            pc++;
            addCycles(12);
            break;
        case 0xd2: // JP NC,nn
            if (getC() == 0) {
                pc = readnn();
                addCycles(16);
            } else {
                pc += 3;
                addCycles(12);
            }
            break;
        case 0xd3: // NOP
            pc++;
            addCycles(4);
            break;
        case 0xd4: // CALL NC,nn
            if (getC() == 0) {
                call();
                addCycles(24);
            } else {
                pc += 3;
                addCycles(12);
            }
            break;
        case 0xd5: // PUSH DE
            push(getDE());
            pc++;
            addCycles(16);
            break;
        case 0xd6: // SUB n
            sub(readn());
            pc += 2;
            addCycles(8);
            break;
        case 0xd7: // RST 10H
            push(pc + 1);
            pc = 0x10;
            addCycles(16);
            break;
        case 0xd8: // RET C
            if (getC() == 1) {
                ret();
                addCycles(20);
            } else {
                pc++;
                addCycles(8);
            }
            break;
        case 0xd9: // RETI
            ret();
            ime = true;
            addCycles(16);
            break;
        case 0xda: // JP C,nn
            if (getC() == 1) {
                pc = readnn();
                addCycles(16);
            } else {
                pc += 3;
                addCycles(12);
            }
            break;
        case 0xdb: // NOP
            pc++;
            addCycles(4);
            break;
        case 0xdc: // CALL C,nn
            if (getC() == 1) {
                call();
                addCycles(24);
            } else {
                pc += 3;
                addCycles(12);
            }
            break;
        case 0xdd: // NOP
            pc++;
            addCycles(4);
            break;
        case 0xde: // SBC A,n
            sub(readn() + getC());
            pc += 2;
            addCycles(8);
            break;
        case 0xDF: // RST 18H
            push(pc + 1);
            pc = 0x18;
            addCycles(16);
            break;
        case 0xE0: // LD (FF00+n),A
            ram.write((0xFF00 + readn()), a);
            pc += 2;
            addCycles(12);
            break;
        case 0xE1: // POP HL
            setHL(pop());
            pc++;
            addCycles(12);
            break;
        case 0xe2: // LD (FF00+C),A
            ram.write((0xFF00 + c), a);
            pc++;
            addCycles(8);
            break;
        case 0xE3: // NOP
            pc++;
            addCycles(4);
            break;
        case 0xE4: // NOP
            pc++;
            addCycles(4);
            break;
        case 0xe5: // PUSH HL
            push(getHL());
            pc++;
            addCycles(16);
            break;
        case 0xE6: // AND n
            and(readn());
            pc += 2;
            addCycles(8);
            break;
        case 0xe7: // RST 20H
            push(pc + 1);
            pc = 0x20;
            addCycles(16);
            break;
        case 0xe8: // ADD SP,dd
            t1 = (byte) readn();
            setZ(0);
            setN(0);
            if (t1 < 0) { // Negative
                setC((sp + t1) < 0);
                calcHsub(sp, -t1);

            } else { // Positive
                setC((sp + t1) > 0xFFFF);
                calcH(sp, t1);
            }
            sp = sp + t1;
            pc += 2;
            addCycles(16);
            break;
        case 0xe9: // JP (HL)
            pc = ram.read(getHL());
            addCycles(4);
            break;
        case 0xEA: // LD (nn),A
            ram.write(readnn(), a);
            pc += 3;
            addCycles(16);
            break;
        case 0xEB: // NOP
            pc++;
            addCycles(4);
            break;
        case 0xEC: // NOP
            pc++;
            addCycles(4);
            break;
        case 0xed: // NOP
            pc++;
            addCycles(4);
            break;
        case 0xee: // XOR n
            xor(readn());
            pc += 2;
            addCycles(8);
            break;
        case 0xef: // RST 28H
            push(pc + 1);
            pc = 0x28;
            addCycles(16);
            break;

        case 0xF0: // LD A,(FF00+n)
            a = ram.read(0xFF00 + readn());
            pc += 2;
            addCycles(12);
            break;
        case 0xF1: // POP AF
            setAF(pop());
            pc++;
            addCycles(12);
            break;
        case 0xF2: // LD A,(FF00+C)
            a = ram.read(0xFF00 + c);
            pc++;
            addCycles(8);
            break;
        case 0xf3: // DI
            ime = false;
            pc++;
            addCycles(4);
            break;
        case 0xF4: // NOP
            pc++;
            addCycles(4);
            break;
        case 0xf5: // PUSH AF
            push(getAF());
            pc++;
            addCycles(16);
            break;
        case 0xf6: // OR n
            or(readn());
            pc += 2;
            addCycles(8);
            break;
        case 0xf7: // RST 30H
            push(pc + 1);
            pc = 0x30;
            addCycles(16);
            break;
        case 0xf8: // LD HL,SP+dd
            t1 = (byte) readn();
            setZ(0);
            setN(0);
            setHL(sp + t1);
            if (t1 < 0) { // Negative
                setC((sp + t1) < 0);
                calcHsub(sp, -t1);

            } else { // Positive
                setC((sp + t1) > 0xFFFF);
                calcH(sp, t1);
            }
            pc += 2;
            addCycles(12);
            break;
        case 0xf9: // LD SP,HL
            sp = getHL();
            pc++;
            addCycles(8);
            break;
        case 0xfa: // LD A,(nn)
            a = ram.read(readnn());
            pc += 3;
            addCycles(16);
            break;
        case 0xfb: // EI
            ime = true;
            pc++;
            addCycles(4);
            break;
        case 0xFC: // NOP
            pc++;
            addCycles(4);
            break;
        case 0xFD: // NOP
            pc++;
            addCycles(4);
            break;
        case 0xFE: // CP, n
            int cpa = a - readn();
            setN(1);
            setZ(cpa == 0);
            setC(cpa < 0);
            setH((cpa & 0xF0) != (a & 0xF0));
            pc += 2;
            addCycles(8);
            break;
        case 0xff: // RST 38h
            push(pc + 1);
            pc = 0x38;
            addCycles(16);
            break;

        default:
            System.out.println("implement step "
                    + Integer.toHexString(ram.read(pc)));
            while (true); // Debug
        }
    }

    /**
     * prefixCB handles the instructions with the prefix 0xCB.
     * @param op Opcode that is followed by the CB prefix.
     * @param r 8bit register value or memory space.
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
            int b = (op >>> 3) & 0x07;
            if (op < 0x80) { // BIT
                bit(b, r);
            } else if (op < 0xc0) { // RES
                r = res(b, r);
            } else { // SET
                r = set(b, r);
            }
        }
        return r;
    }

    /**
	 * Add Cycles after an instruction. 
	 * @param cycles Number of cycles.
	 */
	private void addCycles(int cycles) {
	    cycleTime += cycles;
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
     * @param i the register value to increment
     * @return new value of the register
     */
    private int inc(int i) {
        i = (i + 1) & 0xFF;
        setZ(i == 0);
        setN(0);
        setH((i & 0x0F) == 0x00);
        return i;
    }

    /**
     * The 8bit DEC operation (s <- s - 1).
     * @param i register value to decrement
     * @return new value of the register
     */
    private int dec(int i) {
        i = (i - 1) & 0xFF;
        setZ(i == 0);
        setN(1);
        setH((i & 0x0F) == 0x0F);
        return i;
    }

    /**
     * Operation ADD A,s (s is a 8bit value) (A <- A + s).
     * @param s The register value to add
     */
    private void add(int s) {
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
     * @param s The register value to subtract
     */
    private void sub(int s) {
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
     * @param s The register value.
     */
    private void and(int s) {
        setN(0);
        setH(1);
        setC(0);
        a = a & s;
        setZ(a == 0);
    }

    /**
     * 8bit XOR instruction (A <- A XOR s).
     * @param s Register value.
     */
    private void xor(int s) {
        a = (a ^ s) & 0xFF;
        setZ(a == 0);
        setN(0);
        setH(0);
        setC(0);
    }

    /**
     * 8bit OR instruction (A <- A | s).
     * @param s Register value.
     */
    private void or(int s) {
        a = a | s;
        setZ(a == 0);
        setN(0);
        setH(0);
        setC(0);
    }

    /**
     * Compares a value with register A (A - s).
     * @param s 
     */
    private void cp(int s) {
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
     * @param s Value to push to the stack.
     */
    private void push(int s) {
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
     * BIT instruction. The Z flag is set to the same 
     * value as bit at position b in s.  (Z <- /sb).
     * @param b bit number
     * @param s register value
     */
    private void bit(int b, int s) {
        setN(0);
        setH(1);
        setZ((s >>> b) & 0x01);
    }

    /**
     * RES instruction. Sets bit b in s to 0.
     * @param b bit number
     * @param s register value
     * @return new value of s.
     */
    private int res(int b, int s) {
        return s & (~(1 << b));
    }

    /**
     * SET instruction. Sets bit b in s to 1.
     * @param b bit number
     * @param s register value
     * @return new value of s.
     */
    private int set(int b, int s) {
        return s | (1 << b);
    }

    // Z
    /**
     * Changes the Z flag.
     * @param b If true the flag is set to 1, 
     *          else the flag is set to 0.
     */
    private void setZ(boolean b) {
        if (b)
            cc = cc | 0x80;
        else
            cc = cc & 0x7F;
    }

    /**
     * Changes the Z flag.
     * @param i If 0 the flag is set to 0, 
     *          else the flag is set to 1.
     */
    private void setZ(int i) {
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
     * @param b If true the flag is set to 1, 
     *          else the flag is set to 0.
     */
    private void setN(boolean b) {
        if (b)
            cc = cc | 0x40;
        else
            cc = cc & 0xBF;
    }
    
    /**
     * Changes the N flag.
     * @param i If 0 the flag is set to 0, 
     *          else the flag is set to 1.
     */
    private void setN(int i) {
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
     * @param b If true the flag is set to 1, 
     *          else the flag is set to 0.
     */
    private void setH(boolean b) {
        if (b)
            cc = cc | 0x20;
        else
            cc = cc & 0xDF;
    }

    /**
     * Changes the H flag.
     * @param i If 0 the flag is set to 0, 
     *          else the flag is set to 1.
     */
    private void setH(int i) {
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
     * @param v1 value
     * @param v2 another value
     */
    private void calcH(int v1, int v2) {
        setH(((v1 & 0x0F) + (v2 & 0x0F)) > 0x0F);
    }

    /**
     * Calculates the H flag for subtraction: v1 - v2. 
     * @param v1 value to subtract from
     * @param v2 value to subtract
     */
    private void calcHsub(int v1, int v2) {
        setH(((v1 & 0x0F) - (v2 & 0x0F)) < 0x00);
    }

    // C
    /**
     * Changes the C flag.
     * @param b If true the flag is set to 1, 
     *          else the flag is set to 0.
     */
    private void setC(boolean b) {
        if (b)
            cc = cc | 0x10;
        else
            cc = cc & 0xEF;
    }

    /**
     * Changes the C flag.
     * @param i If 0 the flag is set to 0, 
     *          else the flag is set to 1.
     */
    private void setC(int i) {
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
     * @param address Address to write to
     * @param data16bit 16bit value to be written.
     */
    private void dblwrite(int address, int data16bit) {
        ram.write(address, data16bit & 0xFF);
        ram.write(address + 1, (data16bit >>> 8) & 0xFF);
    }

    /**
     * Calculates the double register of two registers.
     * @param a the left part of the double register.
     * @param b the right part of the double register.
     * @return the value of the double register.
     */
    private int dblreg(int a, int b) {
        return a * 0x100 + b;
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
     * @param largeInt The new 16bit value of HL. 
     */
    public void setHL(int largeInt) {
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
     * @param largeInt The new 16bit value of BC. 
     */
    public void setBC(int largeInt) {
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
     * @param largeInt The new 16bit value of DE. 
     */
    public void setDE(int largeInt) {
        d = (largeInt >> 8) & 0xFF;
        e = largeInt & 0xFF;
    }


    /**
     * Change the value of the double register AF.
     * Note that F is the same as register cc.
     * @param largeInt The new 16bit value of AF. 
     */
    private void setAF(int largeInt) {
        a = (largeInt >> 8) & 0xFF;
        cc = largeInt & 0xFF;
    }

    /**
     * Returns the value of the 16bit AF register.
     * Note that F is the same as register cc.
     * @return The value of the AF register.
     */
    private int getAF() {
        return dblreg(a, cc);
    }

    /**
     * Sets the A register.
     * @param smallInt New 8bit value of A
     */
    public void setA(int smallInt) {
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
     * @param smallInt New 8bit value of F
     */
    public void setF(int smallInt) {
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
     * @param smallInt New 16bit value of SP.
     */
    public void setSP(int largeInt) {
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
     * @param smallInt New 16bit value of PC.
     */
    public void setPC(int smallInt) {
        pc = smallInt;
    }

    /**
     * Returns the state of the IME flag.
     * @return true if the IME flag is active,
     *         otherwise false.
     */
    public boolean getIME() {
        return ime;
    }

    /**
     * Changes the IME flag.
     * @param ime New value of the IME flag.
     */
    public void setIME(boolean ime) {
        this.ime = ime;
    }

    /**
     * Returns executeInterrupt.
     * @return executeInterrupt.
     */
    public boolean getExecuteInterrupt() {
        return executeInterrupt;
    }
}
