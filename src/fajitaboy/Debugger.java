package fajitaboy;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * Debugger is a class that creates an CPU object and an AdressBus object and
 * lets the user input debugging commands to the emulator.
 * 
 * @author Arvid Jakobsson, Marcus Johansson
 */
public final class Debugger {

    /**
     * Maximum value for a byte.
     */
    private static final int MAXBYTE = 255;

    /**
     * AdressBus to emulate.
     */
    private AddressBus addressBus;

    /**
     * CPU to emulate.
     */
    private Cpu cpu;

    /**
     * A set of breakpoints. Each breakpoint is represented as an adress, where
     * to break execution.
     */
    private HashSet<Integer> breakPoints;

    /**
     * Stops debugging on interrupts.
     */
    private boolean interruptEnabled = false;

    /**
     * Creates a debugger, and loads a ROM-file.
     * 
     * @param path
     *            ROM-file to load into the CPU.
     */
    private Debugger(final String path) {
        addressBus = new AddressBus(path);
        breakPoints = new HashSet<Integer>();

        cpu = new Cpu(addressBus);

        prompt();
    }

    /**
     * What radix to read arguments in.
     */
    private int argRadix = 16;

    /**
     * Reads an line from inputs, parses a debugger command from that line, and
     * executes it.
     */
    private void prompt() {
        String scLine;
        String prevLine = "";
        Scanner inputScanner = new Scanner(System.in);
        boolean running = true;

        // Commands: reset, step n, regs x, mem adr adr, bp, bp n, exit
        while (running) {
            System.out.print(String.format("%04x", cpu.getPC()) + " :> ");

            String line = inputScanner.nextLine();
            Scanner in = new Scanner(line);
            if (!in.hasNext()) {
                if (prevLine.equals("")) {
                    continue;
                }
                in = new Scanner(prevLine);
            } else {
                prevLine = line;
            }

            scLine = in.next();
            scLine.trim();
            scLine.toLowerCase();

            if (scLine.equals("t")) {
                if (in.hasNextInt(argRadix)) {
                    debugStep(in.nextInt(argRadix));
                } else {
                    debugStep(1);
                }
            } else if (scLine.equals("hex")) {
                switchRadix();
            } else if (scLine.equals("s")) {
                cpu.reset();

                /*
                 * Please note that GameBoy internal RAM on power up contains
                 * random data. All of the GameBoy emulators tend to set all RAM
                 * to value $00 on entry.
                 */
                // addressBus.reset();
            } else if (scLine.equals("r")) {
                if (in.hasNext()) {
                    String reg = in.next();
                    if (in.hasNextInt(argRadix)) {
                        int value = in.nextInt(argRadix);
                        setReg(reg, value);
                    } else {
                        showDebugError("r expects zero or two arguments");
                    }
                } else {
                    showRegs();
                }
            } else if (scLine.equals("e")) {
                if (in.hasNextInt(argRadix)) {

                    int addr = in.nextInt(argRadix);
                    List<Integer> toWrite = new LinkedList<Integer>();
                    while (in.hasNextInt(argRadix)) {
                        int data = in.nextInt(argRadix);
                        toWrite.add(data);
                    }
                    writeData(addr, toWrite);
                } else {
                    showDebugError("e expects two or more arguments");
                }
            } else if (scLine.equals("d")) {
                if (in.hasNextInt(argRadix)) {
                    int addr = in.nextInt(argRadix);
                    if (in.hasNextInt(argRadix)) {
                        int len = in.nextInt(argRadix);
                        hexDump(addr, len);
                    } else {
                        hexDump(addr);
                        /*
                         * Could have been: hexDump (cpu.getPC, addr) instead.
                         */
                    }
                } else {
                    showDebugError("d expects one or two arguments");
                }
            } else if (scLine.equals("b")) {
                if (in.hasNextInt(argRadix)) {
                    int addr = in.nextInt(argRadix);
                    toggleBreakpoint(addr);
                } else {
                    System.out.println("Current breakpoints: " + breakPoints);
                }
            } else if (scLine.equals("p") || scLine.equals("i")) {
                if (in.hasNextInt(argRadix)) {
                    int lenOrAddr = in.nextInt(argRadix);
                    if (in.hasNextInt(argRadix)) {
                        int len = in.nextInt(argRadix);
                        disassemble(lenOrAddr, len);
                    } else {
                        disassemble(lenOrAddr);
                    }

                } else {
                    showDebugError("p expects one or two arguments");
                }
            } else if (scLine.equals("n")) {
                if (in.hasNextInt(argRadix)) {
                    int i = in.nextInt(argRadix);
                    if (i == 0) {
                        interruptEnabled = false;
                        System.out.println("Not pausing on interrupts");
                    } else if (i == 1) {
                        interruptEnabled = true;
                        System.out.println("Pausing on interrupts");
                    } else {
                        showDebugError("n expects either no arguments or 0/1");
                    }

                } else {
                    System.out.println("Interrupt: "
                            + cpu.getExecuteInterrupt()
                            + "\tPause on interrupts: "
                            + interruptEnabled);
                }
            } else if (scLine.equals("g")) {
                runForever();
            } else if (scLine.equals("?")) {
                showHelp();
            } else if (scLine.equals("q")) {
                System.exit(0);
            } else {
                showDebugError("Invalid command!");
            }
        }
    }

    /**
     * Toggles between hexadecimal and decimal arguments.
     */
    private void switchRadix() {
        if (argRadix == 16) {
            argRadix = 10;
            System.out.println(
                    "Arguments will now be read as decimal numbers.");
        } else {
            argRadix = 16;
            System.out.println(
                    "Arguments will now be read as hexadecimal numbers.");
        }
    }

    /**
     * Dumps one line of memory, at specified address.
     * 
     * @param addr
     *            Where to dump memory.
     */
    private void hexDump(final int addr) {
        hexDump(addr, 1);
    }

    /**
     * Prints a hexdump starting at specified adress, rounded down to closest
     * 0xFFF0, writing for len bytes, rounded up in a similar way.
     * 
     * @param addr
     *            Address to start writing out, rounded down.
     * @param len
     *            Number of bytes to dump, rounded up.
     */
    private void hexDump(final int addr, final int len) {
        int addrS = addr & (~0xF);
        int addrE = (addr + len + 15) & (~0xF);
        int size = addrE - addrS;
        int[] mem = new int[size];

        if (addrS > 0xFFFF) {
            System.out.println(
                    "Address overflow detected! Wrapping around! Silly you!");
            addrE &= 0xFFFF;
            addrS &= 0xFFFF;
        } if (addrE > 0xFFFF) {
            size = 0xFFFF - addrS +1;
            for (int i = 0; i < (addrE & 0xFFFF); i++) {
                mem[size +i] = addressBus.read(i);
            }
        }

        for (int i = 0; i < size; i++) {
            mem[i] = addressBus.read((i + addrS) & 0xFFFF);
        }

        for (int i = 0; i < (addrE-addrS) / 16; i++) {
            String out = String.format("%04x ", (addrS + i * 16) & 0xFFFF);
            for (int j = 0; j < 16; j++) {
                int inArr = i * 16 + j;
                String before = "", after = " ";
                if (inArr == addr - addrS) {
                    before = "[";
                }
                if (inArr == addr - addrS + (len - 1)) {
                    after = "] ";
                }
                out += String.format(before + "%02x" + after, mem[inArr]);
            }
            out += "\t";
            for (int j = 0; j < 16; j++) {
                if (Character.isLetterOrDigit((char) mem[i * 16 + j])) {
                    out += String.format("%c", mem[i * 16 + j]);
                } else {
                    out += ".";
                }
            }
            System.out.println(out);
        }
    }

    /**
     * Writes a list of data to an address on the memorybus.
     * 
     * @param addr
     *            Address where data should be written.
     * @param toWrite
     *            A list of bytes to write.
     */
    private void writeData(final int addr, final List<Integer> toWrite) {
        int curAddr = addr;
        for (Integer d : toWrite) {
            if (d > MAXBYTE) {
                System.out.println(
                        "Byte overflow detected! Ignoring! Silly you!");
            }
            if (curAddr > 0xFFFF) {
                System.out.println(
                        "Address overflow detected! Wrapping! Silly you!");
            }
            addressBus.write(curAddr & 0xFFFF, d & 0xFF);
            curAddr++;
        }
    }

    /**
     * Prints the values contained in the CPU registers.
     */
    private void showRegs() {
        System.out.printf(
                "A = %02x\tBC = %04x\tDE = %04x\tHL = %04x\tPC = %04x\t "
                        + "SP = %04x\t\nF = %02x\n", cpu.getA(), cpu.getBC(),
                cpu.getDE(), cpu.getHL(), cpu.getPC(), cpu.getSP(), cpu.getF());
    }

    /**
     * Sets a register to a value.
     * 
     * @param reg
     *            Register to write set value on.
     * @param value
     *            Value to set register to.
     */
    private void setReg(final String reg, final int value) {
        if (reg.equals("A")) {
            cpu.setA(value);
        } else if (reg.equals("BC")) {
            cpu.setBC(value);
        } else if (reg.equals("DE")) {
            cpu.setDE(value);
        } else if (reg.equals("HL")) {
            cpu.setHL(value);
        } else if (reg.equals("PC")) {
            cpu.setPC(value);
        } else if (reg.equals("SP")) {
            cpu.setSP(value);
        } else if (reg.equals("F")) {
            cpu.setF(value);
        }
    }

    /**
     * Toggles a breakpoint at the specified address.
     * 
     * @param addr
     *            Address to toggle breakpoint at.
     */
    private void toggleBreakpoint(final int addr) {
        if (breakPoints.contains(addr)) {
            breakPoints.remove(addr);
        } else {
            breakPoints.add(addr);
        }
    }

    /**
     * Checks whether a breakpoint is set on a specific address.
     * 
     * @param addr
     *            Address to check breakpoint on.
     * @return Returns true if a breakpoint is set on that adress, false
     *         otherwise.
     */
    private boolean getBreakpoint(final int addr) {
        return breakPoints.contains(addr);
    }

    /**
     * Displays a listing of available commands in the debugger.
     */
    private void showHelp() {
        String helpStr = "?\t\tDisplay this help screen\n"
                + "No imput repeats the last command"
                + "[Unimplemented] c [script]\t\t"
                + "Execute _c_ommands from script file [default.scp]\n"
                + "s\t\tRe_s_et CPU\n" + "r\t\tShow current register values\n"
                + "r reg val\t" + "Set value of register reg to value val\n"
                + "e addr val [val]"
                + "Write values to RAM / ROM starting at address addr\n"
                + "d addr len\tHex _D_ump len bytes starting at addr\n"
                + "i addr len\t"
                + "D_i_sassemble len instructions starting at addr\n"
                + "p len\t\t"
                + "Disassemble len instructions starting at current PC\n"
                + "n\t\t" + "Show interrupt state\n"
                + "n 1|0\t\t" + "Enable/disable interrupts\n"
                + "t [len]\t\t"
                + "Execute len instructions starting at current PC [1]\n"
                + "g\t\t" + "Execute forever\n"
                + "[Unimplemented] o\t\t"
                + "Output Gameboy screen to applet window\n" + "b addr\t\t"
                + "Set breakpoint at addr\n"
                + "[Unimplemented] k [keyname]\t\t" + "Toggle Gameboy key\n"
                + "[Unimplemented] m bank\t\t" + "_M_ap to ROM bank\n"
                + "[Unimplemented] m\t\t" + "Display current ROM mapping\n"
                + "q" + "\t\tQuit debugger interface\n" + "ENTER"
                + "\t\tRepeats last command\n" + "hex"
                + "\t\tSwitches between decimal / hexadecimal input. "
                + "Default is hex.\n" + "<CTRL> + C\t\t" + "Quit JavaBoy\n";

        System.out.println(helpStr);
    }

    /**
     * Steps the program until a breakpoint is reached.
     */
    private void runForever() {
        int c = 0;
        int lenLast = 0;
        while (true) {
            c += cpu.step();
            
            String outStr = Integer.toString(c);
            for (int i = 0; i < lenLast; i++) {
                outStr = '\b' + outStr;
            }
            lenLast = Integer.toString(c).length();
            System.out.print(outStr);
            
            if (getBreakpoint(cpu.getPC())) {
                System.out.println(
                        "Breakpoint at " + cpu.getPC() + " reached.");
                break;
            } else if (interruptEnabled && cpu.getExecuteInterrupt()) {
                System.out.println("Cpu interrupt");
                break;
            }
        }
    }

    private void disassemble(final int len) {
        disassemble(cpu.getPC(), len);
    }

    private void disassemble(final int addr, final int len) {
        List<Disassembler.DisassembledInstruction> dInstructs = Disassembler
                .disassemble(addressBus, addr, len);
        for (Disassembler.DisassembledInstruction di : dInstructs) {
            System.out.println(di);
        }
    }

    /**
     * Show an error message followed by a help message.
     * 
     * @param str
     *            Error message to show.
     */
    private void showDebugError(final String str) {
        System.out.println(str);
        System.out.println("Write ? for help");
    }

    /**
     * Steps the program a specified number of steps, and stops on breakpoints.
     * 
     * @param steps
     *            Number of steps.
     */
    private void debugStep(final int steps) {
        int c = 0;
        for (int i = 0; i < steps; i++) {
            c += cpu.step();
            if (getBreakpoint(cpu.getPC())) {
                System.out
                        .println("Breakpoint at " + cpu.getPC() + " reached.");
                break;
            }
            if (interruptEnabled && cpu.getExecuteInterrupt()) {
                System.out.println("Cpu interrupt");
                break;
            }
        }
        System.out.println("Cycles run: " + c);
    }

    /**
     * Starts the debugger with a specified ROM-filepath.
     * 
     * @param args
     *            Standard input arguments.
     */
    public static void main(final String[] args) {
        if (args.length == 1) {
            new Debugger(args[0]);
        } else {
            System.out.println("Usage:\n\t"
                    + "java Debugger.java /path/to/tetris.gb");
        }

    }
}
