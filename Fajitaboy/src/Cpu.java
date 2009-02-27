import java.io.*; //For debug pause and stepping
import static constants.AddressConstants.*;

/**
 * CPU class for emulating the Game Boy CPU
 */
public class Cpu {

	AddressBus ram;

	//InterruptRegister ireg;

	int pc;

	int cc; //ZNHC0000	(cc is often called the F)

	int sp;

	int cycles; // Processor cycles since init

	boolean ime; // Interrupt Master Enable

	//8bit register
	int h = 0;

	int l = 0;

	int a = 0;

	int b = 0;

	int c = 0;

	int d = 0;

	int e = 0;

	// Internal variables
	int t1, t2; // temp variables to use in step().

	public Cpu(AddressBus ram) {
		this.ram = ram;
		reset();
	}
	
	/**
	 * Sets CPU to start state
	 */
	public void reset() {
	    pc = 0x100;
        cc = 0xB0;
        a = 0x01;
        b = 0x00;
        c = 0x13;
        d = 0x00;
        e = 0xD8;
        h = 0x01;
        l = 0x4D;
        sp = 0xfffe;
        cycles = 0;
        ime = false;
	}

	public void step() {
		// Interrupt handler
		boolean performInterrupt = false;
		int jumpAddress = 0x0000;
		if ( ime ) {
			// Look for interrupts
			int ie_reg = ram.read(ADDRESS_IE);
			int if_reg = ram.read(ADDRESS_IF);
			
			if ((ie_reg & 0x01) != 0 && (if_reg & 0x01) != 0)
			{
				// V-Blank interrupt
				performInterrupt = true;
				jumpAddress = ADDRESS_INT_VBLANK;
				ram.write(ADDRESS_IF, if_reg & 0xFE);
			}
			else if ((ie_reg & 0x02) != 0 && (if_reg & 0x02) != 0)
			{
				// LCD Status interrupt
				performInterrupt = true;
				jumpAddress = ADDRESS_INT_LCDSTAT;
				ram.write(ADDRESS_IF, if_reg & 0xFD);
			}
			else if ((ie_reg & 0x04) != 0 && (if_reg & 0x04) != 0)
			{
				// Timer interrupt
				performInterrupt = true;
				jumpAddress = ADDRESS_INT_TIMER;
				ram.write(ADDRESS_IF, if_reg & 0xFB);
			}
			else if ((ie_reg & 0x08) != 0 && (if_reg & 0x08) != 0)
			{
				// Serial interrupt
				performInterrupt = true;
				jumpAddress = ADDRESS_INT_SERIAL;
				ram.write(ADDRESS_IF, if_reg & 0xF7);
			}
			else if ((ie_reg & 0x10) != 0 && (if_reg & 0x10) != 0)
			{
				// Joypad interrupt
				performInterrupt = true;
				jumpAddress = ADDRESS_INT_JOYPAD;
				ram.write(ADDRESS_IF, if_reg & 0xEF);
			}
		}
		
		if ( performInterrupt ) {
			ime = false;
			sp -= 2;
			dblwrite(sp, pc);
			pc = jumpAddress;
		}
		
		// Perform processor operation
		int inst = ram.read(pc);
		System.out.print("OP: 0x" + Integer.toHexString(inst) + " ");
		runInstruction(inst);
	}

	private void runInstruction(int instruction) {
		switch (instruction) {
		case 0x00:
			System.out.println("NOP");
			pc++;
			addCycles(4);
			break;
		case 0x01:
			System.out.println("LD BC,nn");
			setBC(readnn());
			pc += 3;
			addCycles(12);
			break;
		case 0x02:
			System.out.println("LD (BC), A");
			ram.write(getBC(), a);
			pc++;
			addCycles(8);
			break;
		case 0x03:
			System.out.println("INC BC");
			setBC(getBC() + 1);
			pc++;
			addCycles(8);
			break;
		case 0x04:
			System.out.println("INC B");
			b = inc(b);
			pc++;
			addCycles(4);
			break;
		case 0x05:
			System.out.println("DEC B");
			b = dec(b);
			pc++;
			addCycles(4);
			break;

		case 0x06:
			System.out.println("LD B, n");
			b = readn();
			pc += 2;
			addCycles(8);
			break;
		case 0x07:
			System.out.println("RLCA");
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
		case 0x08:
			System.out.println("LD (nn),SP");
			dblwrite(readnn(), sp);
			pc += 3;
			addCycles(20);
			break;
		case 0x09:
			System.out.println("ADD HL,BC");
			t1 = getHL() + getBC();
			setH(((getHL() & 0x0F) + (getBC() & 0x0F)) > 0x0F);
			setHL(t1);
			setN(0);
			setC(t1 > 0xFFFF);
			pc++;
			addCycles(8);
			break;
		case 0x0a:
			System.out.println("LD A,(BC)");
			a = ram.read(getBC());
			pc++;
			addCycles(8);
			break;
		case 0x0b:
			System.out.println("DEC BC");
			setBC(getBC() - 1);
			pc++;
			addCycles(8);
			break;
		case 0x0c:
			System.out.println("INC C");
			c = inc(c);
			pc++;
			addCycles(4);
			break;
		case 0x0d:
			System.out.println("DEC C");
			c = dec(c);
			pc++;
			addCycles(4);
			break;
		case 0x0e:
			System.out.println("LD C,n");
			c = readn();
			pc += 2;
			addCycles(8);
			break;
		case 0x0f:
			System.out.println("RRCA");
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
		//case 0x10:System.out.println("STOP");break;
		case 0x11:
			System.out.println("LD DE,nn");
			setDE(readnn());
			pc += 3;
			addCycles(12);
			break;
		case 0x12:
			System.out.println("LD (DE),A");
			ram.write(getDE(), a);
			pc++;
			addCycles(8);
			break;
		case 0x13:
			System.out.println("INC DE");
			setDE(getDE() + 1);
			pc++;
			addCycles(8);
			break;
		case 0x14:
			System.out.println("INC D");
			d = inc(d);
			pc++;
			addCycles(4);
			break;
		case 0x15:
			System.out.println("DEC D");
			d = dec(d);
			pc++;
			addCycles(4);
			break;
		case 0x16:
			System.out.println("LD D,n");
			d = readn();
			pc += 2;
			addCycles(8);
			break;
		case 0x17:
			System.out.println("RLA");
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
		case 0x18:
			System.out.println("JR d");
			pc = pc + (byte) ram.read(pc + 1) + 2;
			addCycles(12);
			break;
		case 0x19:
			System.out.println("ADD HL,DE");
			t1 = getHL() + getDE();
			setH(((getHL() & 0x0F) + (getDE() & 0x0F)) > 0x0F);
			setHL(t1);
			setN(0);
			setC(t1 > 0xFFFF);
			pc++;
			addCycles(8);
			break;
		case 0x1a:
			System.out.println("LD A,(DE)");
			a = ram.read(getDE());
			pc++;
			addCycles(8);
			break;
		case 0x1b:
			System.out.println("DEC DE");
			setDE(getDE() - 1);
			pc++;
			addCycles(8);
			break;
		case 0x1c:
			System.out.println("INC e");
			e = inc(e);
			pc++;
			addCycles(4);
			break;
		case 0x1d:
			System.out.println("DEC e");
			e = dec(e);
			pc++;
			addCycles(4);
			break;
		case 0x1e:
			System.out.println("LD E,n");
			e = readn();
			pc += 2;
			addCycles(8);
			break;
		case 0x1f:
			System.out.println("RRA");
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

		case 0x20:
			System.out.println("JR NZ+e");
			if (getZ() == 0) {
				pc += (byte) readn() + 2;
				addCycles(12);
			} else {
				pc += 2;
				addCycles(8);
			}
			break;
		case 0x21:
			System.out.println("LD HL,nn");
			setHL(readnn());
			pc += 3;
			addCycles(12);
			break;
		case 0x22:
			System.out.println("LDI (HL),A");
			ram.write(getHL(), a);
			setHL(getHL() + 1);
			pc++;
			addCycles(8);
			break;
		case 0x23:
			System.out.println("INC HL");
			setHL(getHL() + 1);
			pc++;
			addCycles(8);
			break;
		case 0x24:
			System.out.println("INC H");
			h = inc(h);
			pc++;
			addCycles(4);
			break;
		case 0x25:
			System.out.println("DEC H");
			h = dec(h);
			pc++;
			addCycles(4);
			break;
		case 0x26:
			System.out.println("LD H,n");
			h = readn();
			pc += 2;
			addCycles(8);
			break;
		case 0x27: // algorithm found at http://www.worldofspectrum.org/faq/reference/z80reference.htm#DAA
			System.out.println("DAA");
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
		case 0x28:
			System.out.println("JR Z,d (Jump+n if Z=1)");
			if (getZ() == 1) {
				pc = pc + (byte) readn() + 2;
				addCycles(12);
			} else {
				pc += 2;
				addCycles(8);
			}
			break;

		case 0x29:
			System.out.println("ADD HL,HL");
			t1 = getHL() * 2;
			calcH(getHL(), getHL());
			setN(0);
			setC(t1 > 0xFFFF);
			setHL(t1);
			pc++;
			addCycles(8);
			break;
		case 0x2a:
			System.out.println("LDI A,(HL)");
			a = ram.read(getHL());
			setHL(getHL() + 1);
			pc++;
			addCycles(8);
			break;
		case 0x2b:
			System.out.println("DEC HL");
			setHL(getHL() - 1);
			pc++;
			addCycles(8);
			break;
		case 0x2c:
			System.out.println("INC L");
			l = inc(l);
			pc++;
			addCycles(4);
			break;
		case 0x2d:
			System.out.println("DEC L");
			l = dec(l);
			pc++;
			addCycles(4);
			break;
		case 0x2e:
			System.out.println("LD l,n");
			l = readn();
			pc += 2;
			addCycles(8);
			break;
		case 0x2f:
			System.out.println("CPL");
			a = ~a & 0xff;
			setN(1);
			setH(1);
			pc++;
			addCycles(4);
			break;

		case 0x30:
			System.out.println("JR NC,d");
			if (getC() == 0) {
				pc = pc + (byte) readn() + 2;
				addCycles(12);
			} else {
				pc += 2;
				addCycles(8);
			}
			break;
		case 0x31:
			System.out.println("LD SP,nn");
			sp = readnn();
			pc += 3;
			addCycles(12);
			break;
		case 0x32:
			System.out.println("LDD (HL),A; HL-");
			ram.write(getHL(), a);
			setHL(getHL() - 1);
			pc++;
			addCycles(8);
			break;
		case 0x33:
			System.out.println("INC SP");
			sp = (sp + 1) & 0xFFFF;
			pc++;
			addCycles(8);
			break;
		case 0x34:
			System.out.println("INC (HL)");
			ram.write(getHL(), inc(ram.read(getHL())));
			pc++;
			addCycles(12);
			break;
		case 0x35:
			System.out.println("DEC (HL)");
			ram.write(getHL(), dec(ram.read(getHL())));
			pc++;
			addCycles(12);
			break;
		case 0x36:
			System.out.println("LD (HL),n");
			ram.write(getHL(), readn());
			pc++;
			addCycles(12);
			break;
		case 0x37:
			System.out.println("SCF");
			setC(1);
			setN(0);
			setH(0);
			addCycles(4);
			break;
		case 0x38:
			System.out.println("JR C,d");
			if (getC() == 1) {
				pc = pc + (byte) readn() + 2;
				addCycles(12);
			} else {
				pc += 2;
				addCycles(8);
			}			
			break;
		case 0x39:
			System.out.println("ADD HL,SP");
			t1 = getHL() + sp;
			calcH(getHL(), sp);
			setHL(t1);
			setN(0);
			setC(t1 > 0xFFFF);
			pc++;
			addCycles(8);
			break;
		case 0x3a:
			System.out.println("LDD A,(HL)");
			a = ram.read(getHL());
			setHL(getHL() - 1);
			pc++;
			addCycles(8);
			break;
		case 0x3b:
			System.out.println("DEC SP");
			sp = (sp - 1) & 0xFFFF;
			pc++;
			addCycles(8);
			break;
		case 0x3c:
			System.out.println("INC A");
			a = inc(a);
			pc++;
			addCycles(4);
			break;
		case 0x3e:
			System.out.println("LD A, n");
			a = readn();
			pc += 2;
			addCycles(8);
			break;
		case 0x3f:
			System.out.println("CCF");
			setC(getC() == 0);
			setN(0);
			setH(0);
			pc++;
			addCycles(4);
			break;
		case 0x40:
			System.out.println("LD B,B");
			//b = b;
			pc++;
			addCycles(4);
			break;
		case 0x41:
			System.out.println("LD B,C");
			b = c;
			pc++;
			addCycles(4);
			break;
		case 0x42:
			System.out.println("LD B,D");
			b = d;
			pc++;
			addCycles(4);
			break;
		case 0x43:
			System.out.println("LD B,E");
			b = e;
			pc++;
			addCycles(4);
			break;
		case 0x44:
			System.out.println("LD B,H");
			b = h;
			pc++;
			addCycles(4);
			break;
		case 0x45:
			System.out.println("LD B,L");
			b = l;
			pc++;
			addCycles(4);
			break;
		case 0x46:
			System.out.println("LD B,(HL)");
			b = ram.read(getHL());
			pc++;
			addCycles(8);
			break;
		case 0x47:
			System.out.println("LD B,A");
			b = a;
			pc++;
			break;
		case 0x48:
			System.out.println("LD C,B");
			c = b;
			pc++;
			addCycles(4);
			break;
		case 0x49:
			System.out.println("LD C,C");
			//c=c;
			pc++;
			addCycles(4);
			break;
		case 0x4a:
			System.out.println("LD C,D");
			c = d;
			pc++;
			addCycles(4);
			break;
		case 0x4b:
			System.out.println("LD C,E");
			c = e;
			pc++;
			addCycles(4);
			break;
		case 0x4c:
			System.out.println("LD C,H");
			c = h;
			pc++;
			addCycles(4);
			break;
		case 0x4d:
			System.out.println("LD C,L");
			c = l;
			pc++;
			addCycles(4);
			break;
		case 0x4e:
			System.out.println("LD C,(HL)");
			c = ram.read(getHL());
			pc++;
			addCycles(8);
			break;
		case 0x4f:
			System.out.println("LD C,A");
			c = a;
			pc++;
			addCycles(4);
			break;
		case 0x50:
			System.out.println("LD D,B");
			d = b;
			pc++;
			addCycles(4);
			break;
		case 0x51:
			System.out.println("LD D,C");
			d = c;
			pc++;
			addCycles(4);
			break;
		case 0x52:
			System.out.println("LD D,D");
			//d=d;
			pc++;
			addCycles(4);
			break;
		case 0x53:
			System.out.println("LD D,E");
			d = e;
			pc++;
			addCycles(4);
			break;
		case 0x54:
			System.out.println("LD D,H");
			d = h;
			pc++;
			addCycles(4);
			break;
		case 0x55:
			System.out.println("LD D,L");
			d = l;
			pc++;
			addCycles(4);
			break;
		case 0x56:
			System.out.println("LD D,(HL)");
			d = ram.read(getHL());
			pc++;
			addCycles(8);
			break;
		case 0x57:
			System.out.println("LD D,A");
			d = a;
			pc++;
			break;
		case 0x58:
			System.out.println("LD E,B");
			e = b;
			pc++;
			addCycles(4);
			break;
		case 0x59:
			System.out.println("LD E,C");
			e = c;
			pc++;
			addCycles(4);
			break;
		case 0x5A:
			System.out.println("LD E,D");
			e = d;
			pc++;
			addCycles(4);
			break;
		case 0x5b:
			System.out.println("LD E,E");
			//e=e;
			pc++;
			addCycles(4);
			break;
		case 0x5c:
			System.out.println("LD E,H");
			e = h;
			pc++;
			addCycles(4);
			break;
		case 0x5d:
			System.out.println("LD E,L");
			e = l;
			pc++;
			addCycles(4);
			break;
		case 0x5e:
			System.out.println("LD E,(HL)");
			e = ram.read(getHL());
			pc++;
			addCycles(8);
			break;
		case 0x5f:
			System.out.println("LD E,A");
			e = a;
			pc++;
			addCycles(4);
			break;
		case 0x60:
			System.out.println("LD H,B");
			h = b;
			pc++;
			addCycles(4);
			break;
		case 0x61:
			System.out.println("LD H,C");
			h = c;
			pc++;
			addCycles(4);
			break;
		case 0x62:
			System.out.println("LD H,D");
			d = c;
			pc++;
			addCycles(4);
			break;
		case 0x63:
			System.out.println("LD H,E");
			h = e;
			pc++;
			addCycles(4);
			break;
		case 0x64:
			System.out.println("LD H,H");
			//h=h;
			pc++;
			addCycles(4);
			break;
		case 0x65:
			System.out.println("LD H,L");
			h = l;
			pc++;
			addCycles(4);
			break;
		case 0x66:
			System.out.println("LD H,(HL)");
			h = ram.read(getHL());
			pc++;
			addCycles(8);
			break;
		case 0x67:
			System.out.println("LD H,A");
			h = a;
			pc++;
			addCycles(4);
			break;
		case 0x68:
			System.out.println("LD L,B");
			l = b;
			pc++;
			addCycles(4);
			break;
		case 0x69:
			System.out.println("LD L,C");
			l = c;
			pc++;
			addCycles(4);
			break;
		case 0x6a:
			System.out.println("LD L,D");
			l = d;
			pc++;
			addCycles(4);
			break;
		case 0x6b:
			System.out.println("LD L,E");
			l = e;
			pc++;
			addCycles(4);
			break;
		case 0x6c:
			System.out.println("LD L,H");
			l = h;
			pc++;
			addCycles(4);
			break;
		case 0x6d:
			System.out.println("LD L,L");
			//l=l;
			pc++;
			addCycles(4);
			break;
		case 0x6e:
			System.out.println("LD L,(HL)");
			l = ram.read(getHL());
			pc++;
			addCycles(4);
			break;
		case 0x6f:
			System.out.println("LD L,A");
			l = a;
			pc++;
			addCycles(4);
			break;
			
		case 0x70:
			System.out.println("LD (HL),B");
			ram.write(getHL(), b);
			pc++;
			addCycles(8);
			break;
		case 0x71:
			System.out.println("LD (HL),C");
			ram.write(getHL(), c);
			pc++;
			addCycles(8);
			break;
		case 0x72:
			System.out.println("LD (HL),D");
			ram.write(getHL(), d);
			pc++;
			addCycles(8);
			break;
		case 0x73:
			System.out.println("LD (HL),E");
			ram.write(getHL(), e);
			pc++;
			addCycles(8);
			break;
		case 0x74:
			System.out.println("LD (HL),H");
			ram.write(getHL(), h);
			pc++;
			addCycles(8);
			break;
		case 0x75:
			System.out.println("LD (HL),L");
			ram.write(getHL(), l);
			pc++;
			addCycles(8);
			break;
		//case 0x76: System.out.println("HALT"); break;
		case 0x77:
			System.out.println("LD (HL),A");
			ram.write(getHL(), a);
			pc++;
			addCycles(8);
			break;
		case 0x78:
			System.out.println("LD A,B");
			a = b;
			pc++;
			addCycles(4);
			break;
		case 0x79:
			System.out.println("LD A,C");
			a = c;
			pc++;
			addCycles(4);
			break;
		case 0x7A:
			System.out.println("LD A,D");
			a = d;
			pc++;
			addCycles(4);
			break;
		case 0x7B:
			System.out.println("LD A,E");
			a = e;
			pc++;
			addCycles(4);
			break;
		case 0x7c:
			System.out.println("LD A,H");
			a = h;
			pc++;
			addCycles(4);
			break;
		case 0x7d:
			System.out.println("LD A,L");
			a = l;
			pc++;
			addCycles(4);
			break;
		case 0x7e:
			System.out.println("LD A,(HL)");
			a = ram.read(getHL());
			pc++;
			addCycles(8);
			break;
		case 0x7f:
			System.out.println("LD A,A");
			//a=a;
			pc++;
			addCycles(4);
			break;

		case 0x80:
			System.out.println("ADD A,B");
			add(b);
			pc++;
			addCycles(4);
			break;
		case 0x81:
			System.out.println("ADD A,C");
			add(c);
			pc++;
			addCycles(4);
			break;
		case 0x82:
			System.out.println("ADD A,D");
			add(d);
			pc++;
			addCycles(4);
			break;
		case 0x83:
			System.out.println("ADD A,E");
			add(e);
			pc++;
			addCycles(4);
			break;
		case 0x84:
			System.out.println("ADD A,H");
			add(h);
			pc++;
			addCycles(4);
			break;
		case 0x85:
			System.out.println("ADD A,L");
			add(l);
			pc++;
			addCycles(4);
			break;
		case 0x86:
			System.out.println("ADD A,(HL)");
			add(ram.read(getHL()));
			pc++;
			addCycles(8);
			break;
		case 0x87:
			System.out.println("ADD A,A");
			add(a);
			pc++;
			addCycles(4);
			break;

		case 0x88:
			System.out.println("ADC A,B");
			add(b + getC());
			pc++;
			addCycles(4);
			break;
		case 0x89:
			System.out.println("ADC A,C");
			add(c + getC());
			pc++;
			addCycles(4);
			break;
		case 0x8a:
			System.out.println("ADC A,D");
			add(d + getC());
			pc++;
			addCycles(4);
			break;
		case 0x8b:
			System.out.println("ADC A,E");
			add(e + getC());
			pc++;
			addCycles(4);
			break;
		case 0x8c:
			System.out.println("ADC A,H");
			add(h + getC());
			pc++;
			addCycles(4);
			break;
		case 0x8d:
			System.out.println("ADC A,L");
			add(l + getC());
			pc++;
			addCycles(4);
			break;
		case 0x8e:
			System.out.println("ADC A,(HL)");
			add(ram.read(getHL()) + getC());
			pc++;
			addCycles(8);
			break;
		case 0x8f:
			System.out.println("ADC A,A");
			add(a + getC());
			pc++;
			addCycles(4);
			break;

		case 0x90:
			System.out.println("SUB B");
			sub(b);
			pc++;
			addCycles(4);
			break;
		case 0x91:
			System.out.println("SUB C");
			sub(c);
			pc++;
			addCycles(4);
			break;
		case 0x92:
			System.out.println("SUB D");
			sub(d);
			pc++;
			addCycles(4);
			break;
		case 0x93:
			System.out.println("SUB E");
			sub(e);
			pc++;
			addCycles(4);
			break;
		case 0x94:
			System.out.println("SUB H");
			sub(h);
			pc++;
			addCycles(4);
			break;
		case 0x95:
			System.out.println("SUB L");
			sub(l);
			pc++;
			addCycles(4);
			break;
		case 0x96:
			System.out.println("SUB (HL)");
			sub(ram.read(getHL()));
			pc++;
			addCycles(8);
			break;
		case 0x97:
			System.out.println("SUB A");
			sub(a);
			pc++;
			addCycles(4);
			break;

		case 0x98:
			System.out.println("SBC A,B");
			sub(b + getC());
			pc++;
			addCycles(4);
			break;
		case 0x99:
			System.out.println("SBC A,C");
			sub(c + getC());
			pc++;
			addCycles(4);
			break;
		case 0x9A:
			System.out.println("SBC A,D");
			sub(d + getC());
			pc++;
			addCycles(4);
			break;
		case 0x9B:
			System.out.println("SBC A,E");
			sub(e + getC());
			pc++;
			addCycles(4);
			break;
		case 0x9C:
			System.out.println("SBC A,H");
			sub(h + getC());
			pc++;
			addCycles(4);
			break;
		case 0x9D:
			System.out.println("SBC A,L");
			sub(l + getC());
			pc++;
			addCycles(4);
			break;
		case 0x9e:
			System.out.println("SBC A,(HL)");
			sub(ram.read(getHL()) + getC());
			pc++;
			addCycles(8);
			break;
		case 0x9f:
			System.out.println("SBC A,A");
			sub(a + getC());
			pc++;
			addCycles(4);
			break;

		case 0xa0:
			System.out.println("AND B");
			and(b);
			pc++;
			addCycles(4);
			break;
		case 0xa1:
			System.out.println("AND C");
			and(c);
			pc++;
			addCycles(4);
			break;
		case 0xa2:
			System.out.println("AND D");
			and(d);
			pc++;
			addCycles(4);
			break;
		case 0xa3:
			System.out.println("AND E");
			and(e);
			pc++;
			addCycles(4);
			break;
		case 0xa4:
			System.out.println("AND H");
			and(h);
			pc++;
			addCycles(4);
			break;
		case 0xa5:
			System.out.println("AND L");
			and(l);
			pc++;
			addCycles(4);
			break;
		case 0xa6:
			System.out.println("AND (HL)");
			and(ram.read(getHL()));
			pc++;
			addCycles(8);
			break;
		case 0xa7:
			System.out.println("AND A");
			and(a);
			pc++;
			addCycles(4);
			break;

		case 0xa8:
			System.out.println("XOR B");
			xor(b);
			pc++;
			addCycles(4);
			break;
		case 0xa9:
			System.out.println("XOR C");
			xor(c);
			pc++;
			addCycles(4);
			break;
		case 0xaa:
			System.out.println("XOR D");
			xor(d);
			pc++;
			addCycles(4);
			break;
		case 0xab:
			System.out.println("XOR E");
			xor(e);
			pc++;
			addCycles(4);
			break;
		case 0xac:
			System.out.println("XOR H");
			xor(h);
			pc++;
			addCycles(4);
			break;
		case 0xad:
			System.out.println("XOR L");
			xor(l);
			pc++;
			addCycles(4);
			break;
		case 0xAE:
			System.out.println("XOR (HL)");
			xor(ram.read(getHL()));
			pc++;
			addCycles(8);
			break;
		case 0xAF:
			System.out.println("XOR A");
			xor(a);
			pc++;
			addCycles(4);
			break;

		case 0xB0:
			System.out.println("OR B");
			or(b);
			pc++;
			addCycles(4);
			break;
		case 0xB1:
			System.out.println("OR C");
			or(c);
			pc++;
			addCycles(4);
			break;
		case 0xB2:
			System.out.println("OR D");
			or(d);
			pc++;
			addCycles(4);
			break;
		case 0xb3:
			System.out.println("OR E");
			or(e);
			pc++;
			addCycles(4);
			break;
		case 0xb4:
			System.out.println("OR H");
			or(h);
			pc++;
			addCycles(4);
			break;
		case 0xb5:
			System.out.println("OR L");
			or(l);
			pc++;
			addCycles(4);
			break;
		case 0xb6:
			System.out.println("OR (HL)");
			or(ram.read(getHL()));
			pc++;
			addCycles(8);
			break;
		case 0xb7:
			System.out.println("OR A");
			or(a);
			pc++;
			addCycles(4);
			break;

		case 0xb8:
			System.out.println("CP B");
			cp(b);
			pc++;
			addCycles(4);
			break;
		case 0xb9:
			System.out.println("CP C");
			cp(c);
			pc++;
			addCycles(4);
			break;
		case 0xba:
			System.out.println("CP D");
			cp(d);
			pc++;
			addCycles(4);
			break;
		case 0xbb:
			System.out.println("CP E");
			cp(e);
			pc++;
			addCycles(4);
			break;
		case 0xbc:
			System.out.println("CP H");
			cp(h);
			pc++;
			addCycles(4);
			break;
		case 0xbd:
			System.out.println("CP L");
			cp(l);
			pc++;
			addCycles(4);
			break;
		case 0xbe:
			System.out.println("CP (HL)");
			cp(ram.read(getHL()));
			pc++;
			addCycles(8);
			break;
		case 0xbf:
			System.out.println("CP A");
			cp(a);
			pc++;
			addCycles(4);
			break;

		case 0xc0:
			System.out.println("RET NZ");
			if (getZ() == 0) {
				ret();
				addCycles(20);
			} else {
				pc++;
				addCycles(8);
			}
			break;
		case 0xc1:
			System.out.println("POP BC");
			setBC(pop());
			pc++;
			addCycles(12);
			break;
		case 0xc2:
			System.out.println("JP NZ,nn");
			if (getZ() == 0) {
				pc = readnn();
				addCycles(16);
			} else {
				pc += 3;
				addCycles(12);
			}
			break;
		case 0xc3:
			System.out.println("JP nn");
			pc = readnn();
			addCycles(16);
			break;
		case 0xc4:
			System.out.println("CALL NZ,nn");
			if (getZ() == 0) {
				call();
				addCycles(24);
			} else {
				pc += 3;
				addCycles(12);
			}
			break;
		case 0xc5:
			System.out.println("PUSH BC");
			push(getBC());
			pc++;
			addCycles(16);
			break;
		case 0xc6:
			System.out.println("ADD A,n");
			add(readn());
			pc += 2;
			addCycles(8);
			break;
		case 0xc7:
			System.out.println("RST 0");
			push(pc + 1);
			pc = 0;
			addCycles(16);
			break;
		case 0xc8:
			System.out.println("RET Z");
			if (getZ() == 1) {
				ret();
				addCycles(20);
			} else {
				pc++;
				addCycles(8);
			}
			break;
		case 0xc9:
			System.out.println("RET");
			ret();
			addCycles(16);
			break;
		case 0xca:
			System.out.println("JP Z,nn");
			if (getZ() == 1) {
				pc = readnn();
				addCycles(16);
			} else {
				pc += 3;
				addCycles(12);
			}
			break;
		case 0xcb:
			System.out.print("[CB Prefix]");
			int cbOp = readn();
			switch (cbOp & 0x07) {
			case 0:
				b = prefixCB(cbOp, b);
				addCycles(8);
				System.out.println(" B");
				break;
			case 1:
				c = prefixCB(cbOp, c);
				addCycles(8);
				System.out.println(" C");
				break;
			case 2:
				d = prefixCB(cbOp, d);
				addCycles(8);
				System.out.println(" D");
				break;
			case 3:
				e = prefixCB(cbOp, e);
				addCycles(8);
				System.out.println(" E");
				break;
			case 4:
				h = prefixCB(cbOp, h);
				addCycles(8);
				System.out.println(" H");
				break;
			case 5:
				l = prefixCB(cbOp, l);
				addCycles(8);
				System.out.println(" L");
				break;
			case 6:
				ram.write(getHL(), prefixCB(cbOp, ram.read(getHL())));
				
				if (cbOp > 0x3f && cbOp < 0x80) {
					// instruction is BIT
					addCycles(12);
				} else {
					//  instruction is SET or RES
					addCycles(16);
				}
				
				System.out.println(" (HL)");
				break;
			case 7:
				a = prefixCB(cbOp, a);
				addCycles(8);
				System.out.println(" A");
				break;
			}
			pc += 2;
			break;
		case 0xcc:
			System.out.println("CALL Z,nn");
			if (getZ() == 1) {
				call();
				addCycles(24);
			} else {
				pc += 3;
				addCycles(12);
			}
			break;
		case 0xcd:
			System.out.println("CALL nn");
			call();
			addCycles(24);
			break;
		case 0xce:
			System.out.println("ADC A,n");
			add(readn() + getC());
			pc += 2;
			addCycles(8);
			break;
		case 0xcf:
			System.out.println("RST 8");
			push(pc + 1);
			pc = 8;
			addCycles(16);
			break;
		case 0xd0:
			System.out.println("RET NC");
			if (getC() == 0) {
				ret();
				addCycles(20);
			} else {
				pc++;
				addCycles(8);
			}
			break;
		case 0xd1:
			System.out.println("POP DE");
			setDE(pop());
			pc++;
			addCycles(12);
			break;
		case 0xd2:
			System.out.println("JP NC,nn");
			if (getC() == 0) {
				pc = readnn();
				addCycles(16);
			} else {
				pc += 3;
				addCycles(12);
			}
			break;
		case 0xd3:
			System.out.println("NOP");
			pc++;
			addCycles(4);
			break;
		case 0xd4:
			System.out.println("CALL NC,nn");
			if (getC() == 0) {
				call();
				addCycles(24);
			} else {
				pc += 3;
				addCycles(12);
			}
			break;
		case 0xd5:
			System.out.println("PUSH DE");
			push(getDE());
			pc++;
			addCycles(16);
			break;
		case 0xd6:
			System.out.println("SUB n");
			sub(readn());
			pc += 2;
			addCycles(8);
			break;
		case 0xd7:
			System.out.println("RST 10H");
			push(pc + 1);
			pc = 0x10;
			addCycles(16);
			break;
		case 0xd8:
			System.out.println("RET C");
			if (getC() == 1) {
				ret();
				addCycles(20);
			} else {
				pc++;
				addCycles(8);
			}
			break;
		case 0xd9:
			System.out.println("RETI");
			ret();
			ime = true;
			addCycles(16);
			break;
		case 0xda:
			System.out.println("JP C,nn");
			if (getC() == 1) {
				pc = readnn();
				addCycles(16);
			} else {
				pc += 3;
				addCycles(12);
			}
			break;
		case 0xdb:
			System.out.println("NOP");
			pc++;
			addCycles(4);
			break;
		case 0xdc:
			System.out.println("CALL C,nn");
			if (getC() == 1) {
				call();
				addCycles(24);
			} else {
				pc += 3;
				addCycles(12);
			}
			break;
		case 0xdd:
			System.out.println("NOP");
			pc++;
			addCycles(4);
			break;
		case 0xde:
			System.out.println("SBC A,n");
			sub(readn() + getC());
			pc += 2;
			addCycles(8);
			break;
		case 0xDF:
			System.out.println("RST 18H");
			push(pc + 1);
			pc = 0x18;
			addCycles(16);
			break;
		case 0xE0:
			System.out.println("LD (FF00+n),A");
			ram.write((0xFF00 + readn()), a);
			pc += 2;
			addCycles(12);
			break;
		case 0xE1:
			System.out.println("POP HL");
			setHL(pop());
			pc++;
			addCycles(12);
			break;
		case 0xe2:
			System.out.println("LD (FF00+C),A");
			ram.write((0xFF00 + c), a);
			pc++;
			addCycles(8);
			break;
		case 0xE3:
			System.out.println("NOP");
			pc++;
			addCycles(4);
			break;
		case 0xE4:
			System.out.println("NOP");
			pc++;
			addCycles(4);
			break;
		case 0xe5:
			System.out.println("PUSH HL");
			push(getHL());
			pc++;
			addCycles(16);
			break;
		case 0xE6:
			System.out.println("AND n");
			and(readn());
			pc += 2;
			addCycles(8);
			break;
		case 0xe7:
			System.out.println("RST 20H");
			push(pc + 1);
			pc = 0x20;
			addCycles(16);
			break;
		case 0xe8:
			System.out.println("ADD SP,dd");
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
		case 0xe9:
			System.out.println("JP (HL)");
			pc = ram.read(getHL());
			addCycles(4);
			break;
		case 0xEA:
			System.out.println("LD (nn),A");
			ram.write(readnn(), a);
			pc += 3;
			addCycles(16);
			break;
		case 0xEB:
			System.out.println("NOP");
			pc++;
			addCycles(4);
			break;
		case 0xEC:
			System.out.println("NOP");
			pc++;
			addCycles(4);
			break;
		case 0xed:
			System.out.println("NOP");
			pc++;
			addCycles(4);
			break;
		case 0xee:
			System.out.println("XOR n");
			xor(readn());
			pc += 2;
			addCycles(8);
			break;
		case 0xef:
			System.out.println("RST 28H");
			push(pc + 1);
			pc = 0x28;
			addCycles(16);
			break;

		case 0xF0:
			System.out.println("LD A,(FF00+n)");
			a = ram.read(0xFF00 + readn());
			pc += 2;
			addCycles(12);
			break;
		case 0xF1:
			System.out.println("POP AF");
			setAF(pop());
			pc++;
			addCycles(12);
			break;
		case 0xF2:
			System.out.println("LD A,(FF00+C)");
			a = ram.read(0xFF00 + c);
			pc++;
			addCycles(8);
			break;
		case 0xf3:
			System.out.println("DI");
			ime = false;
			pc++;
			addCycles(4);
			break;
		case 0xF4:
			System.out.println("NOP");
			pc++;
			addCycles(4);
			break;
		case 0xf5:
			System.out.println("PUSH AF");
			push(getAF());
			pc++;
			addCycles(16);
			break;
		case 0xf6:
			System.out.println("OR n");
			or(readn());
			pc += 2;
			addCycles(8);
			break;
		case 0xf7:
			System.out.println("RST 30H");
			push(pc + 1);
			pc = 0x30;
			addCycles(16);
			break;
		case 0xf8:
			System.out.println("LD HL,SP+dd"); //Testat och ska fungera enligt dokumentation ;)
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
		case 0xf9:
			System.out.println("LD SP,HL");
			sp = getHL();
			pc++;
			addCycles(8);
			break;
		case 0xfa:
			System.out.println("LD A,(nn)");
			a = ram.read(readnn());
			pc += 3;
			addCycles(16);
			break;
		case 0xfb:
			System.out.println("EI");
			ime = true;
			pc++;
			addCycles(4);
			break;
		case 0xFC:
			System.out.println("NOP");
			pc++;
			addCycles(4);
			break;
		case 0xFD:
			System.out.println("NOP");
			pc++;
			addCycles(4);
			break;
		case 0xFE:
			System.out.println("CP, n");
			int cpa = a - readn();
			setN(1);
			setZ(cpa == 0);
			setC(cpa < 0);
			setH((cpa & 0xF0) != (a & 0xF0));
			pc += 2;
			addCycles(8);
			break;
		case 0xff:
			System.out.println("RST 38h");
			push(pc + 1);
			pc = 0x38;
			addCycles(16);
			break;

		default:
			System.out.println("implement step "
					+ Integer.toHexString(ram.read(pc)));
			while (true)
				; //Debug
		}
	}

	/*
	 * prefixCB handles the instructions with the prefix 0xCB.
	 */
	private int prefixCB(int op, int r) {
		if (op < 0x40) {
			switch (op >>> 3) {
			case 0:
				log(" RLC");
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
			case 1:
				log(" RRC");
				setN(0);
				setH(0);
				setC((r & 0x01) == 1);
				r = r >>> 1;
				if (getC() == 1) {
					r = r | 0x80;
				}
				setZ(r == 0);
				break;
			case 2:
				log(" RL");
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
			case 3:
				log(" RR");
				setN(0);
				setH(0);
				if (getC() == 1) {
					r = r | 0x100;
				}
				setC((r & 0x01) == 1);
				r = r >>> 1;
				setZ(r == 0);
				break;
			case 4:
				log(" SLA");
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
			case 5:
				log(" SRA");
				setN(0);
				setH(0);
				int b7 = r & 0x80;
				setC((r & 0x01) == 1);
				r = r >>> 1;
				r += b7;
				setZ(r == 0);
				break;
			case 6:
				log(" SWAP");
				setN(0);
				setH(0);
				setC(0);
				int highNibble = r >>> 4;
				int lowNibble = r & 0x0F;
				r = lowNibble * 0x10 + highNibble;
				setZ(r == 0);
				break;
			case 7:
				log(" SRL");
				setN(0);
				setH(0);
				setC((r & 0x01) == 1);
				r = r >>> 1;
				setZ(r == 0);
				break;
			}
		} else {
			int b = (op >>> 3) & 0x07;
			if (op < 0x80) {
				log(" BIT " + b + ",");
				bit(b, r);
			} else if (op < 0xc0) {
				log(" RES " + b + ",");
				r = res(b, r);
			} else {
				log(" SET " + b + ",");
				r = set(b, r);
			}
		}
		return r;
	}

	public void debugInfo() { //debug
		//System.out.println("PC: "+Integer.toHexString(pc)+" SP: "+Integer.toHexString(sp)+" Mem: "+Integer.toHexString(ram.read(dblreg(h,l))));
		//System.out.println("C: "+getC()+" Z: "+getZ());
		//System.out.println("A: "+a+" B: "+b+" C: "+c+" D: "+d+" H: "+h+" L: "+l);
		System.out.println("A = " + Integer.toHexString(a) + "    BC = "
				+ Integer.toHexString(b * 256 + c) + "    DE = "
				+ Integer.toHexString(d * 256 + e) + "    HL = "
				+ Integer.toHexString(h * 256 + l) + "    PC =  "
				+ Integer.toHexString(pc) + "    SP = "
				+ Integer.toHexString(sp));
		System.out.println("F = " + Integer.toHexString(cc));
	}

	private int readn() {
		return ram.read(pc + 1);
	}

	private int readnn() {
		return (ram.read(pc + 1) + ram.read(pc + 2) * 0x100);
	}

	private int readSP() {
		return (ram.read(sp) + ram.read(sp + 1) * 0x100);
	}

	// inc and dec should only be used on 8bit register
	private int inc(int i) {
		i = (i + 1) & 0xFF;
		setZ(i == 0);
		setN(0);
		setH((i & 0x0F) == 0x00);
		return i;
	}

	private int dec(int i) {
		i = (i - 1) & 0xFF;
		setZ(i == 0);
		setN(1);
		setH((i & 0x0F) == 0x0F);
		return i;
	}

	// add is for operation ADD A,s (s is a 8bit value)
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

	// sub is for operation SUB s (s is a 8bit value)
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

	// and is only for operation AND s (s is a 8bit value)
	private void and(int s) {
		setN(0);
		setH(1);
		setC(0);
		a = a & s;
		setZ(a == 0);
	}

	private void xor(int s) {
		a = (a ^ s) & 0xFF;
		setZ(a == 0);
		setN(0);
		setH(0);
		setC(0);
	}

	private void or(int s) {
		a = a | s;
		setZ(a == 0);
		setN(0);
		setH(0);
		setC(0);
	}

	private void cp(int s) {
		int t = a - s;
		setN(1);
		calcHsub(a, s);
		setC(t < 0);
		setZ(t == 0);
	}

	// RET instruction
	private void ret() {
		pc = readSP();
		sp += 2;
	}

	// push dblreg s on stack
	private void push(int s) {
		sp -= 2;
		dblwrite(sp, s);
	}

	private int pop() {
		int s = readSP();
		sp += 2;
		return s;
	}

	private void call() {
		push(pc + 3);
		pc = readnn();
	}

	private void bit(int b, int s) {
		setN(0);
		setH(1);
		setZ((s >>> b) & 0x01);
	}

	private int res(int b, int s) {
		return s & (~(1 << b));
	}

	private int set(int b, int s) {
		return s | (1 << b);
	}

	/*
	 * Set and get methods for the cc register (F)
	 */
	// Z
	private void setZ(boolean b) {
		if (b)
			cc = cc | 0x80;
		else
			cc = cc & 0x7F;
	}

	private void setZ(int i) {
		setZ(i != 0);
	}

	private int getZ() {
		return (cc >>> 7) & 0x01;
	}

	// N
	private void setN(boolean b) {
		if (b)
			cc = cc | 0x40;
		else
			cc = cc & 0xBF;
	}

	private void setN(int i) {
		setN(i != 0);
	}

	private int getN() {
		return (cc >>> 6) & 0x01;
	}

	// H
	private void setH(boolean b) {
		if (b)
			cc = cc | 0x20;
		else
			cc = cc & 0xDF;
	}

	private void setH(int i) {
		setH(i != 0);
	}

	private int getH() {
		return (cc >>> 5) & 0x01;
	}

	// calcH can be used to calculate the H flag for addition: v1 + v2
	private void calcH(int v1, int v2) {
		setH(((v1 & 0x0F) + (v2 & 0x0F)) > 0x0F);
	}

	// calcHsub is the same as calcH but for subtraction: v1 - v2
	private void calcHsub(int v1, int v2) {
		setH(((v1 & 0x0F) - (v2 & 0x0F)) < 0x00);
	}

	// C
	private void setC(boolean b) {
		if (b)
			cc = cc | 0x10;
		else
			cc = cc & 0xEF;
	}

	private void setC(int i) {
		setC(i != 0);
	}

	private int getC() {
		return (cc >>> 4) & 0x01;
	}

	// writes 16bit values to memory
	private void dblwrite(int address, int data16bit) {
		ram.write(address, data16bit & 0xFF);
		ram.write(address + 1, (data16bit >>> 8) & 0xFF);
	}

	private int dblreg(int a, int b) {
		return a * 0x100 + b;
	}

	/*
	 * Get and set methods for double registers
	 */
	public int getHL() {
		return dblreg(h, l);
	}

	public void setHL(int largeInt) {
		h = (largeInt >> 8) & 0xFF;
		l = largeInt & 0xFF;
	}

	public int getBC() {
		return dblreg(b, c);
	}

	public void setBC(int largeInt) {
		b = (largeInt >> 8) & 0xFF;
		c = largeInt & 0xFF;
	}

	public int getDE() {
		return dblreg(d, e);
	}

	public void setDE(int largeInt) {
		d = (largeInt >> 8) & 0xFF;
		e = largeInt & 0xFF;
	}

	// AF is the the register a and cc
	private void setAF(int largeInt) {
		a = (largeInt >> 8) & 0xFF;
		cc = largeInt & 0xFF;
	}

	private int getAF() {
		return dblreg(a, cc);
	}
	

    public void setA(int smallInt) {
            a=smallInt;
    }
    public int getA() {
        return a;
    }
    
    public void setF(int smallInt) {
        cc=smallInt;
    }
    public int getF() {
        return cc;
    }
        
    public void setSP(int largeInt) {
        sp=largeInt;
    }
    
    public int getSP() {
        return sp;
    }
    
    public int getPC() {
        return pc;
    }

    public void setPC(int smallInt) {
        pc = smallInt;
    }
    
    public boolean getIME() {
    	return ime;
    }

	public void debugpause() { //debug
		try {
			System.in.read();
		} catch (IOException e) {
			log("input fail");
		}
	}

	public static void main(String[] args) {
		//Cpu cpu = new Cpu(null);
		//cpu.ram = new AddressBus("tetris.gb",cpu);
		//cpu.mainLoop();
	}
	
	private void addCycles(int cycl) {
		cycles += cycl;
	}
	
	// Temporary logging functions
	boolean enableLogging = false;
	
	private void log(String s) {
		if ( enableLogging )
			System.out.print(s);
	}
	
	private void logln(String s) {
		if ( enableLogging )
			System.out.println(s);
	}
	
	public void logEnable(boolean enable) {
		enableLogging = enable;
	}

	/**
	 * Function used for testing purposes only.
	 */
	private void mainLoop() {
		int cnt = 1; //DEBUG räknare i while-loopen.
		int skip = 0;//0x3035;

		PrintStream stout = System.out; // Disable println
		System.setOut(new PrintStream(new OutputStream() {
			public void write(int b) {
			}
		}));

		for (int i = 1; i < skip; i++) {
			step();
			cnt++;
		}
		System.setOut(stout); // Enable println

		while (true) {
			//debugpause();
			System.out.println();
			System.out.println(pc);
			System.out.println("PC: " + Integer.toHexString(pc));
			step();
			debugInfo();
			System.out.println("Counter: " + (cnt) + " ("
					+ Integer.toHexString(cnt) + ")");
			cnt++; //debugr�knare

		}
	}
}