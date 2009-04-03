package fajitaboy.debugger;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import fajitaboy.Cpu;
import fajitaboy.DrawsGameboyScreen;
import fajitaboy.GamePanel;
import fajitaboy.Oscillator;
import fajitaboy.lcd.LCD;
import fajitaboy.memory.IO;
import fajitaboy.memory.RomWriteException;
import static java.lang.Math.*;

import static fajitaboy.constants.LCDConstants.*;
import static fajitaboy.constants.AddressConstants.*;

/**
 * Debugger is a class that creates an CPU object and an AdressBus object and
 * lets the user input debugging commands to the emulator.
 * @author Arvid Jakobsson, Marcus Johansson
 */
public final class Debugger implements DrawsGameboyScreen {

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
     * List of logged pc-values. This is used to provide a backwards trace.
     */
    private LinkedList<Integer> pcLog;

    /**
     * Stops debugging on interrupts.
     */
    private boolean interruptEnabled = false;

    /**
     * Creates a debugger, and loads a ROM-file.
     * @param path
     *            ROM-file to load into the CPU.
     */

    private PrintWriter traceWriter;

    private boolean tracingToFile = false;

    private JFrame jfr;

    // private boolean showFrame;
    private GamePanel panelScreen;

    private Debugger(final String path) {
        pcLog = new LinkedList<Integer>();
        addressBus = new DebuggerMemoryInterface(path);
        breakPoints = new HashSet<Integer>();
        cpu = new Cpu(addressBus);
        osc = new Oscillator(cpu, addressBus, this, false);
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
            // Dump sprite table
        } else if (scLine.equals("sprites")) {
            spriteTable();
            
            // Step and disassemble
        } else if (scLine.equals("td")) {
            if (in.hasNextInt(argRadix)) {
                debugNSteps(false, in.nextInt(argRadix));
            } else {
                debugNSteps(false, 1);
            }
            disassemble(cpu.getPC(), 1);

            // Display tile
        } else if (scLine.equals("tile")) {
            if (in.hasNextInt(argRadix)) {
                showTile(in.nextInt(argRadix));
            } else {
                showDebugError("tile takes an adress to tile to display");
            }

            // Display background tile numbers
        } else if (scLine.equals("bg")) {
            String err = "bg takes an address to map to display and scroll x, scroll y";
            if (in.hasNextInt(argRadix)) {
                int addr = in.nextInt(argRadix);
                if (in.hasNextInt(argRadix)) {
                    int scx = in.nextInt(argRadix);
                    if (in.hasNextInt(argRadix)) {
                        int scy = in.nextInt(argRadix);
                        showBackgroundNumbers(addr, scx, scy);
                    } else {
                        showDebugError(err);
                    }
                } else {
                    showDebugError(err);
                }
            } else {
                showDebugError(err);
            }

            // Show output window
        } else if (scLine.equals("show")) {
            if (jfr == null) {
                jfr = new JFrame("FajitaBoy Screen");
                jfr.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

                int zoom = 2;
                panelScreen = new GamePanel(zoom);
                panelScreen.setPreferredSize(new Dimension(zoom * GB_LCD_W,
                        zoom * GB_LCD_H));
                jfr.setContentPane(panelScreen);
                jfr.addKeyListener(new SimpleKeyInputController(addressBus.getJoyPad()));
                jfr.pack();
            }

            jfr.setVisible(!jfr.isVisible());

            // Draw to output window
        } else if (scLine.equals("draw")) {
            drawScreen();

            // Start tracing to file.
        } else if (scLine.equals("trf")) {
            tracingToFile = !tracingToFile;
            if (tracingToFile) {
                try {
                    System.out.println("Writing trace to file.");
                    traceWriter = new PrintWriter(new BufferedWriter(
                            new FileWriter("trace.log")));
                } catch (IOException e) {
                    System.out.println("Could not open output-file for trace!");
                    tracingToFile = false;
                    return;
                }

            } else {
                if (traceWriter != null) {
                    System.out.println("Stopped trace.");
                    traceWriter.flush();
                    traceWriter.close();
                    traceWriter = null;
                }
            }

            // step over
        } else if (scLine.equals("to")) {
            stepOver();

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
                System.out.println("Interrupt: " + cpu.getIME()
                        + "\tPause on interrupts: " + interruptEnabled);
            }
            // Toggle gameboy key
        } else if (scLine.equals("k")) {
            if (in.hasNext()) {
                String key = in.next();
                toggleKey(key);
            } else {
                showDebugError("k requires a key argument");
            }

        } else if (scLine.equals("tr")) {
            if (in.hasNextInt()) {
                int nr = in.nextInt();
                showTrace(nr);
            } else {
                showTrace(20);
            }
            // Dump screen
        } else if (scLine.equals("screen")) {
            dumpScreen();

            // Run forever
        } else if (scLine.equals("g")) {
            if (in.hasNextInt(argRadix)) {
                int stop = in.nextInt(argRadix);
                runForever(stop);
            } else {
                runForever();
            }

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

    private void spriteTable() {
    	for (int i = 0; i < GB_SPRITE_ATTRIBUTES; i++) {
    		int addr = SPRITE_ATTRIBUTE_TABLE + i * 4;
    		int y = addressBus.read(addr);
    		int x = addressBus.read(addr + 1);
    		int id = addressBus.read(addr + 2);
    		int fl = addressBus.read(addr + 3);
    		
    		System.out.println(String.format("%03d : %03d, id: %02x, flags: %02x", x, y, id, fl));
    	}
    }
    
    private void showBackgroundNumbers(int addr, int scx, int scy) {
        for (int y = 0; y < GB_LCD_H / GB_TILE_H; y++) {
            for (int x = 0; x < GB_LCD_W / GB_TILE_W; x++) {
                int ay = (scy + y) % 32;
                int ax = (scx + x) % 32;
                int nr = addressBus.read(addr + ay * 32 + ax);
                System.out.print(String.format("%02x ", nr));
            }
            System.out.print("\n");
        }
    }

    private void showTile(int addr) {
        for (int i = 0; i < 16; i += 2) {
            int hi = addressBus.read(addr + i);
            int lo = addressBus.read(addr + i + 1);

            int pxl[] = LCD.convertToPixels(hi, lo);
            for (int j : pxl) {
                if (j != 0) {
                    System.out.print(j);
                } else {
                    System.out.print(' ');
                }
            }
            System.out.print("\n");
        }
    }

    private void toggleKey(String key) {
        IO.JoyPad jp = addressBus.getJoyPad();
        if (key.equals("a")) {
            jp.setA(!jp.isA());
        } else if (key.equals("b")) {
            jp.setB(!jp.isB());
        } else if (key.equals("start")) {
            jp.setStart(!jp.isStart());
        } else if (key.equals("select")) {
            jp.setSelect(!jp.isSelect());
        } else if (key.equals("up")) {
            jp.setUp(!jp.isUp());
        } else if (key.equals("down")) {
            jp.setDown(!jp.isDown());
        } else if (key.equals("left")) {
            jp.setLeft(!jp.isLeft());
        } else if (key.equals("right")) {
            jp.setRight(!jp.isRight());
        } else {
            System.out.println("Unknown key " + key);
        }
    }

    private void drawScreen() {
        panelScreen.drawGameboyScreen(osc.getLCD().getScreen());
    }

    /**
     * Reads pixeldata from lcd, and outputs them in the console.
     */
    private void dumpScreen() {
        LCD lcd = osc.getLCD();
        int[][] scr = lcd.getScreen();
        for (int i = 0; i < scr.length; i++) {
            for (int j = 0; j < scr[i].length; j++) {
                int pxl = scr[i][j];
                if (pxl > 0) {
                    System.out.print(pxl);
                } else {
                    System.out.print(' ');
                }
            }
            System.out.print("\n");
        }
    }

    /**
     * showTrace nr shows a disassemble of the latest nr of instructions.
     * @param nr
     *            number of instructions to show.
     */
    private void showTrace(final int nr) {
        int sz = pcLog.size();
        System.out.println("Showing latest " + min(sz, nr) + " PC-values.");
        for (int i = max(0, sz - nr); i < sz; i++) {
            disassemble(pcLog.get(i), 1);
        }
        System.out.print("\n");
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
            addressBus.forceWrite(curAddr & 0xFFFF, d & 0xFF);
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
                + "No input repeats the last command\n"
                + "[Unimplemented] c [script]\t\t"
                + "Execute _c_ommands from script file [default.scp]\n"
                + "s\t\tRe_s_et CPU\n"
                + "r\t\tShow current register values\n"
                + "r reg val\t"
                + "Set value of register reg to value val\n"
                + "e addr val [val]"
                + "Write values to RAM / ROM starting at address addr\n"
                + "d addr len\tHex _D_ump len bytes starting at addr\n"
                + "i addr len\t"
                + "D_i_sassemble len instructions starting at addr\n"
                + "p len\t\t"
                + "Disassemble len instructions starting at current PC\n"
                + "n\t\t"
                + "Show interrupt state\n"
                + "n 1|0\t\t"
                + "Enable/disable interrupts\n"
                + "t [len]\t\t"
                + "Execute len instructions starting at current PC [1]\n"
                + "g\t\t"
                + "Execute forever\n"
                + "show\t\t"
                + "Output Gameboy screen to applet window\n"
                + "b addr\t\t"
                + "Set breakpoint at addr\n"
                + "key [keyname]\t\t"
                + "Toggle Gameboy key\n"
                + "[Unimplemented] m bank\t\t"
                + "_M_ap to ROM bank\n"
                + "[Unimplemented] m\t\t"
                + "Display current ROM mapping\n"
                + "q"
                + "\t\tQuit debugger interface\n"
                + "ENTER"
                + "\t\tRepeats last command\n"
                + "hex"
                + "\t\tSwitches between decimal / hexadecimal input. "
                + "Default is hex.\n"
                + "<CTRL> + C\t\t"
                + "Quit JavaBoy\n"
                + "mb \t\t View list of current memory breakpoints\n"
                + "mb type addr\t\tAdds a memory breakpoint at addr, of type. "
                + "Type can be r, w, or rw.\n"
                + "mb type lower upper\tAdds a memory breakpoint over the "
                + "address space from lower to upper of type\n"
                + "rmb idx \t Removes the memory breakpoint with given index.\n"
                + "tr \t\t Shows a disassemble of the latest 20 instructions.\n"
                + "tr nr \t\tShows a disassemble of the latest nr of instructions.\n"
                + "td\t\tStep and disassemble. Works as t, but shows a "
                + "disassemble of comming instruction.\n"
                + "screen\t\t Outputs screen to console\n";

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
                .disassemble(addressBus, addr, len, true);
        for (Disassembler.DisassembledInstruction di : dInstructs) {
            if (getBreakpoint(di.getAddr())) {
                System.out.println("* " + di);
            } else {
                System.out.println("  " + di);
            }
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
     * Logs a pc value in the pc log, pruning the log if necessary.
     * @param pc
     *            pc value to log
     */
    private void logPC(final int pc) {
        if (pcLog.add(pc)) {
            if (pcLog.size() > 1024) {
                pcLog.removeFirst();
            }
        }
    }

    /**
     * Steps the oscillator one step, throws on three different cases: 1.
     * Read/Write errors. The cpu tried to write or read somewhere it's not
     * allowed. 2. Breakpoint reached. The cpu reached a user specified
     * breakpoint. 3. Memory breakpoint triggered. The cpu wrote or read
     * somewhere where the user have setup a memory breakpoint.
     * @return Number of cycles this step took.
     * @throws InterruptedStepException
     *             if the interruption was interrupted, this is thrown.
     */
    private int debugStep() throws InterruptedStepException {
        int c = 0;
        try {
            int pc = cpu.getPC();
            if (tracingToFile) {
                traceWriter.println(Disassembler.dsmInstruction(addressBus, pc,
                        true));
            }
            logPC(pc);
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
     * Steps the oscillator forever or a specified number of steps, and stops on
     * breakpoints.
     * @param forever
     *            If set to true, steps is ignored and the oscillator is stepped
     *            forever.
     * @param steps
     *            If forever is set to fals, debugNSteps will step steps steps.
     */
    private void debugNSteps(final boolean forever, final int steps) {
        int c = 0;
        final boolean pointCounter = true;
        int lastPoint = 0;
        int points = 0;

        for (int i = 0; i < steps || forever; i++) {
            try {
                c += debugStep();
            } catch (InterruptedStepException e) {
                if (pointCounter && points > 0) {
                    System.out.print("\n");
                }
                System.out.println("Stopped after " + (i + 1) + " steps.");
                System.out.println(e);
                printDirtyActions();
                addressBus.clearDirtyActions();
                c += e.getCycles();
                break;
            }

            if (pointCounter) {
                if (c - lastPoint > 100000) {
                    points++;
                    lastPoint = c;
                    if (points > 80) {
                        System.out.println(".");
                        points = 0;
                    } else {
                        System.out.print(".");
                    }

                }
            }
        }
        System.out.print("\nCycles run: " + c + "\n");

        if (tracingToFile) {
            traceWriter.flush();
        }
    }

    public void drawGameboyScreen(int[][] data) {
        if (jfr != null && jfr.isVisible()) {
            panelScreen.drawGameboyScreen(data);
        }
    }

    /**
     * Prints the list of dirty actions that the addressbus has accumulated.
     */
    private void printDirtyActions() {
        for (DebuggerMemoryInterface.MemoryAction ma : addressBus
                .getDirtyActions()) {
            System.out.println(ma);
        }
    }

    private void stepOver() {
        int pc = cpu.getPC();
        Disassembler.DisassembledInstruction instr = Disassembler
                .dsmInstruction(addressBus, pc, false);
        runForever(pc + instr.getData().size());
    }

    /**
     * Steps the program forever, or until some kind of breakpoint is reached.
     */
    private void runForever() {
        debugNSteps(true, -1);
    }

    /**
     * Steps the program until a breakpoint is reached.
     */
    private void runForever(int stop) {
        // if there already is an breakpoint at stop, we wont remove it.
        if (getBreakpoint(stop)) {
            debugNSteps(true, -1);
        } else {
            toggleBreakpoint(stop);
            debugNSteps(true, -1);
            toggleBreakpoint(stop);
        }
    }

    /**
     * Starts the debugger with a specified ROM-filepath.
     * @param args
     *            Standard input arguments.
     */
    public static void main(final String[] args) {
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
