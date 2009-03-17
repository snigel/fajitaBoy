package fajitaboy;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Debugger is a class that creates an CPU object and an AdressBus object and
 * lets the user input debugging commands to the emulator.
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
    private DebuggerMemoryInterface addressBus;

    /**
     * CPU to emulate.
     */
    private Cpu cpu;

    /**
     * Oscillator to emulate.
     */
    private Oscillator osc;

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
     * @param path
     *            ROM-file to load into the CPU.
     */
    private Debugger(final String path) {
        addressBus = new DebuggerMemoryInterface(new AddressBus(path));
        breakPoints = new HashSet<Integer>();
        cpu = new Cpu(addressBus);
        osc = new Oscillator(cpu, addressBus);
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
        String prevLine = "";
        Scanner inputScanner = new Scanner(System.in);
        boolean running = true;

        // Commands: reset, step n, regs x, mem adr adr, bp, bp n, exit
        while (running) {
            System.out.print(String.format("%04x", cpu.getPC()) + " :> ");
            String line = inputScanner.nextLine();
            Scanner in = new Scanner(line);

            // No input
            if (!in.hasNext()) {
                if (prevLine.equals("")) {
                    continue; // No previous command, skip
                }
                in = new Scanner(prevLine); // Else: Read prev line again
            } else {
                prevLine = line;
            }

            scanCommand(in);
        }
    }

    /**
     * Scans input from the given Scanner, and performs a debugger command,
     * depending on the input.
     * @param in
     *            Scanner to read input from.
     */
    private void scanCommand(final Scanner in) {
        String scLine = in.next();
        scLine.trim();
        scLine.toLowerCase();

        // Step
        if (scLine.equals("t")) {
            if (in.hasNextInt(argRadix)) {
                debugNSteps(false, in.nextInt(argRadix));
            } else {
                debugNSteps(false, 1);
            }

            // Switch hex/dec input
        } else if (scLine.equals("hex")) {
            switchRadix();

            // Reset
        } else if (scLine.equals("s")) {
            cpu.reset();
            addressBus.reset();

            // Show/Set registers
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

            // Write to memory
        } else if (scLine.equals("e")) {
            if (in.hasNextInt(argRadix)) {

                int addr = in.nextInt(argRadix);
                List<Integer> toWrite = new LinkedList<Integer>();
                while (in.hasNextInt(argRadix)) {
                    int data = in.nextInt(argRadix);
                    toWrite.add(data);
                }

                if (toWrite.size() == 0) {
                    System.out
                            .println("No data was inputted, performing hex dump instead.");
                    hexDump(addr);
                } else {
                    writeData(addr, toWrite);
                }

            } else {
                showDebugError("e expects two or more arguments");
            }

            // Hexdump
        } else if (scLine.equals("d")) {
            if (in.hasNextInt(argRadix)) {
                int addr = in.nextInt(argRadix);
                if (in.hasNextInt(argRadix)) {
                    int len = in.nextInt(argRadix);
                    hexDump(addr, len);
                } else {
                    hexDump(addr);
                }
            } else {
                showDebugError("d expects one or two arguments");
            }

            // Show/modify breakpoints
        } else if (scLine.equals("b")) {
            if (in.hasNextInt(argRadix)) {
                int addr = in.nextInt(argRadix);
                toggleBreakpoint(addr);
            } else {
                System.out.println("Current breakpoints: " + breakPoints);
            }

            // Show/modify memory breakpoints
        } else if (scLine.equals("mb")) {
            if (in.hasNext()) {
                String typeStr = in.next();
                DebuggerMemoryInterface.ActionType type = DebuggerMemoryInterface.ActionType
                        .fromTinyString(typeStr);

                if (type != null) {
                    if (in.hasNextInt(argRadix)) {
                        int lower = in.nextInt(argRadix);
                        if (in.hasNextInt(argRadix)) {
                            int upper = in.nextInt(argRadix);
                            addressBus
                                    .addBreakpoint(addressBus.new MemoryBreakpoint(
                                            lower, upper, type));

                        } else {
                            addressBus
                                    .addBreakpoint(addressBus.new MemoryBreakpoint(
                                            lower, type));
                        }
                    }
                } else {
                    showDebugError("First argument to mb must be one of \"r\", \"w\" or \"rw\".");
                }
            } else {
                System.out.println("Current memory breakpoints: ");
                List<DebuggerMemoryInterface.MemoryBreakpoint> mbs = addressBus
                        .getMBreakPoints();
                for (int i = 0; i < mbs.size(); i++) {
                    DebuggerMemoryInterface.MemoryBreakpoint mb = mbs.get(i);
                    System.out.println(i + ". " + mb);
                }
            }

            // Remove a memory breakpoint
        } else if (scLine.equals("rmb")) {
            if (in.hasNextInt()) {
                int idx = in.nextInt();
                try {
                    addressBus.removeBreakpoint(idx);
                } catch (IndexOutOfBoundsException e) {
                    showDebugError("Invalid memory break point index.");
                }
            } else {
                showDebugError("rmb must be given exactly one argument, index"
                        + " of memory breakpoint to remove."
                        + "Try \"mb\" to view a list of memory breakpoints.");
            }

            // Disassemble instructions
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

            // Pause on interrupts on/off
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
                System.out.println("Interrupt: " + cpu.getExecuteInterrupt()
                        + "\tPause on interrupts: " + interruptEnabled);
            }

            // Run forever
        } else if (scLine.equals("g")) {
            runForever();

            // Show help
        } else if (scLine.equals("?")) {
            showHelp();

            // Quit
        } else if (scLine.equals("q")) {
            System.exit(0);

            // No valid parse
        } else {
            showDebugError("Invalid command!");
        }
    }

    /**
     * Toggles between hexadecimal and decimal arguments.
     */
    private void switchRadix() {
        if (argRadix == 16) {
            argRadix = 10;
            System.out
                    .println("Arguments will now be read as decimal numbers.");
        } else {
            argRadix = 16;
            System.out
                    .println("Arguments will now be read as hexadecimal numbers.");
        }
    }

    /**
     * Dumps one line of memory, at specified address.
     * @param addr
     *            Where to dump memory.
     */
    private void hexDump(final int addr) {
        hexDump(addr, 1);
    }

    /**
     * Prints a hexdump starting at specified adress, rounded down to closest
     * 0xFFF0, writing for len bytes, rounded up in a similar way.
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
            System.out
                    .println("Address overflow detected! Wrapping around! Silly you!");
            addrE &= 0xFFFF;
            addrS &= 0xFFFF;
        }
        if (addrE > 0xFFFF) {
            size = 0xFFFF - addrS + 1;
            for (int i = 0; i < (addrE & 0xFFFF); i++) {
                mem[size + i] = addressBus.read(i);
            }
        }

        for (int i = 0; i < size; i++) {
            mem[i] = addressBus.read((i + addrS) & 0xFFFF);
        }

        for (int i = 0; i < (addrE - addrS) / 16; i++) {
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

        addressBus.clearDirtyActions();
    }

    /**
     * Writes a list of data to an address on the memorybus.
     * @param addr
     *            Address where data should be written.
     * @param toWrite
     *            A list of bytes to write.
     */
    private void writeData(final int addr, final List<Integer> toWrite) {
        int curAddr = addr;
        for (Integer d : toWrite) {
            if (d > MAXBYTE) {
                System.out
                        .println("Byte overflow detected! Ignoring! Silly you!");
            }
            if (curAddr > 0xFFFF) {
                System.out
                        .println("Address overflow detected! Wrapping! Silly you!");
            }
            addressBus.write(curAddr & 0xFFFF, d & 0xFF);
            curAddr++;
        }
        addressBus.clearDirtyActions();
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
                + "n\t\t" + "Show interrupt state\n" + "n 1|0\t\t"
                + "Enable/disable interrupts\n" + "t [len]\t\t"
                + "Execute len instructions starting at current PC [1]\n"
                + "g\t\t" + "Execute forever\n" + "[Unimplemented] o\t\t"
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
     * Disassembles a series of instructions from PC.
     * @param len
     *            number of instructions
     */
    private void disassemble(final int len) {
        disassemble(cpu.getPC(), len);
    }

    /**
     * Disassembles a series of instructions from a given address.
     * @param addr
     *            startaddress
     * @param len
     *            number of instructions
     */
    private void disassemble(final int addr, final int len) {
        List<Disassembler.DisassembledInstruction> dInstructs = Disassembler
                .disassemble(addressBus, addr, len);
        for (Disassembler.DisassembledInstruction di : dInstructs) {
            System.out.println(di);
        }

        addressBus.clearDirtyActions();
    }

    /**
     * Show an error message followed by a help message.
     * @param str
     *            Error message to show.
     */
    private void showDebugError(final String str) {
        System.out.println(str);
        System.out.println("Write ? for help");
    }

    /**
     * Steps the oscillator one step, throws on three different cases:
     *  1. Read/Write errors. The cpu tried to write or read somewhere it's
     *   not allowed.
     *  2. Breakpoint reached. The cpu reached a user specified breakpoint.
     *  3. Memory breakpoint triggered. The cpu wrote or read somewhere where
     *   the user have setup a memory breakpoint.
     * @return Number of cycles this step took.
     * @throws InterruptedStepException if the interruption was interrupted, this
     *  is thrown.
     */
    private int debugStep() throws InterruptedStepException {
        int c = 0;
        try {
            c += osc.step();
        } catch (RomWriteException e) {
            throw new InterruptedStepException(
                    "ROM tried to write to read only memory. Execution stopped: "
                            + e, 0);
        }

        if (getBreakpoint(cpu.getPC())) {
            throw new InterruptedStepException("Breakpoint at " + cpu.getPC()
                    + " reached.", c);
        }
        if (interruptEnabled && cpu.getExecuteInterrupt()) {
            throw new InterruptedStepException("CPU interrupt.", c);
        }
        if (addressBus.getDirtyActions().size() > 0) {
            throw new InterruptedStepException("Memory breakpoint reached.", c);
        }

        return c;
    }

    /**
     * Steps the oscillator forever or a specified number of steps, and stops
     * on breakpoints.
     * @param forever If set to true, steps is ignored and the oscillator is
     * stepped forever.
     * @param steps If forever is set to fals, debugNSteps will step steps steps.
     */
    private void debugNSteps(final boolean forever, final int steps) {
        int c = 0;
        int lenLast = 0;

        System.out.print("Cycles run: ");
        for (int i = 0; i < steps || forever; i++) {
            try {
                c += debugStep();
            } catch (InterruptedStepException e) {
                System.out.println(e.getCycles());
                System.out.println(e);
                printDirtyActions();
                addressBus.clearDirtyActions();
                break;
            }

            // TODO saker händer i lite konstig ordning här
            String outStr = Integer.toString(c);

            // Remove previous output
            for (int j = 0; j < lenLast; j++) {
                outStr = '\b' + outStr;
            }
            lenLast = Integer.toString(c).length();
            System.out.print(outStr);
        }
        System.out.print("\n");
    }

    /**
     * Prints the list of dirty actions that the addressbus has accumulated.
     */
    private void printDirtyActions() {
        for (DebuggerMemoryInterface.MemoryAction ma : addressBus.getDirtyActions()) {
            System.out.println(ma);
        }
    }

    /**
     * Steps the program until a breakpoint is reached.
     */
    private void runForever() {
        debugNSteps(true, -1);
    }

    /**
     * Starts the debugger with a specified ROM-filepath.
     * @param args
     *            Standard input arguments.
     */
    public static void main(final String[] args) {
        Map<String, String> env = System.getenv();
        for (String envName : env.keySet()) {
            System.out.format("%s=%s%n", envName, env.get(envName));
        }

        if (args.length == 1) {
            String path = args[0];
            if ((new File(path)).exists()) {
                new Debugger(path);
            } else {
                System.out.println(path + " is not a valid rom. Quitting.");
            }
        } else {
            System.out.println("Usage:\n\t"
                    + "java Debugger.java /path/to/tetris.gb");
        }
    }
}
