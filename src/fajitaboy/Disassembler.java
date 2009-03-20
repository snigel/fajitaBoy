package fajitaboy;

import java.util.List;
import java.util.LinkedList;
import static fajitaboy.InstructionSet.*;

/**
 * A class for disassembling game boy cpu instructions. Since this class hold no
 * mutable state, it has been declared it static.
 * @author Arvid
 */
public final class Disassembler {

    /**
     * Hidden unused constructor.
     */
    private Disassembler() {
    }

    /**
     * Disassemble a list of bytes.
     * @param bus
     *            MemoryInterface object
     * @param addr
     *            Adress to start disassembling
     * @param len
     *            Number of instructions
     * @param javaboyComp
     *            If true, try to make output close to javaboys.
     * @return A list of strings, representing the disassembled code. Each
     *         string is one instruction.
     */
    public static List<DisassembledInstruction> disassemble(
            final MemoryInterface bus, final int addr, final int len,
            final boolean javaboyComp) {

        // Disassemble len instructions, starting at curAddr.

        List<DisassembledInstruction> out = new LinkedList<DisassembledInstruction>();
        int curAddr = addr;

        for (int i = 0; i < len; i++) {
            DisassembledInstruction di = dsmInstruction(bus, curAddr,
                    javaboyComp);
            out.add(di);
            curAddr += di.getData().size();
        }

        // len instructions has been disassembled, return.
        return out;
    }

    /**
     * Disassembles one instruction from bus, at address addr.
     * @param bus
     *            MemoryInterface to read data from
     * @param addr
     *            Address to read data from
     * @param javaboyComp
     *            If true, stay true to dah javaboy stylez.
     * @return Disassembled instruction.
     */
    private static DisassembledInstruction dsmInstruction(
            final MemoryInterface bus, final int addr, final boolean javaboyComp) {
        // this instruction starts at iAddr.
        int curAddr = addr;

        // the data this instruction is disassembled from, is contained in
        // iData.
        List<Integer> iData = new LinkedList<Integer>();

        int opcode = bus.read(curAddr++);
        if (opcode == 0xCB) {
            return dsmPrefix(bus, addr, javaboyComp);
        }

        InstructionSet.Instruction instr = InstructionSet
                .getInstruction(opcode);
        iData.add(opcode);

        String pn = new String(instr.getPrettyName()); // create a copy.

        InstructionSet.InstructionArgument arg;
        // keep reading arguments.
        while ((arg = getNextArgument(pn)) != null) {
            // reverse arguments & save the data for output
            int argValue = 0;
            for (int j = 0; j < arg.getSize(); j++) {
                int d = bus.read(curAddr + j);
                argValue += (d << 8 * j);
                iData.add(d);
            }

            // do some magic depending on argument type
            String formatString = "";
            switch (arg) {
            case SIGNED_OFFSET:
                int offs = (byte) argValue;
                argValue = 1 + curAddr + offs;

                formatString = hexFormat(4, javaboyComp);
                if (offs < 0) {
                    formatString += " : " + offs;
                }

                break;
            case FF_OFFSET:
                formatString = hexFormat(4, javaboyComp);
                argValue += 0xFF00;
                break;
            default:
                formatString = hexFormat(arg.getSize() * 2, javaboyComp);
                break;
            }

            // forward adress pointer.
            curAddr += arg.getSize();
            pn = pn.replaceFirst("\\" + Character.toString(arg.getFChar()),
                    String.format(formatString, argValue));
        }
        return new DisassembledInstruction(iData, addr, pn, javaboyComp);
    }

    /** Disassembles one prefix-instruction.
     * @param bus
     *            MemoryInterface to read data from
     * @param addr
     *            Address to read data from
     * @param javaboyComp
     *            If true, stay true to dah javaboy stylez.
     * @return Disassembled instruction.
     */
    private static DisassembledInstruction dsmPrefix(final MemoryInterface bus,
            final int addr, final boolean javaboyComp) {
        // the data this instruction is disassembled from, is contained in
        // iData.
        List<Integer> iData = new LinkedList<Integer>();
        iData.add(0xCB);

        int arg = bus.read(addr + 1);
        iData.add(arg);

        String instr = "";

        // which operation?
        if (arg < 0x40) {
            String[] instrs = {"RLC", "RRC", "RL", "RR", "SLA", "SRA", "SWAP",
                    "SRL"};
            instr = instrs[arg >>> 3] + " ";
        } else {
            // which bit?
            int bit = arg >>> 3 & 0x07;
            if (arg < 0x80) { // BIT
                instr = "BIT";
            } else if (arg < 0xc0) { // RES
                instr = "RES";
            } else { // SET
                instr = "SET";
            }
            instr = instr + " " + bit + ", ";
        }

        // what register?
        String[] regs = {"B", "C", "D", "E", "H", "L", "(HL)", "A"};
        String reg = regs[arg & 0x07];
        instr += reg;

        return new DisassembledInstruction(iData, addr, instr, javaboyComp);
    }

    /**
     * Get next argument.
     * @param str
     *            input string
     * @return Next instruction argument to disassemble.
     */
    private static InstructionArgument getNextArgument(final String str) {
        for (InstructionArgument ia : InstructionArgument.values()) {
            if (str.indexOf(ia.getFChar()) > -1) {
                return ia;
            }
        }
        return null;
    }

    /**
     * Returns a format string for outputing hex values.
     * @param w Width of number to output.
     * @param javaboyComp
     *            If true, stay true to dah javaboy stylez. Meaning,
     *            print in upper case, do not prefix with "0x".
     * @return Format string.
     */
    private static String hexFormat(final int w, final boolean javaboyComp) {
        // if javaboy, do not prefix with 0x, and upcase.
        String valPrefix = javaboyComp ? "" : "0x";
        String hexOut = javaboyComp ? "X" : "x";

        return valPrefix + "%0" + w + hexOut;
    }

    /**
     * Instructions.
     * @author Arvid
     */
    public static class DisassembledInstruction {
        /**
         * DATA.
         */
        private List<Integer> data;

        /**
         * Adress.
         */
        private int addr;

        /**
         * Instruction.
         */
        private String instructionString;

        /**
         * If true, stay true to dah javaboy stylez.
         */
        private boolean javaboyComp;

        /**
         * Constructor.
         * @param d
         *            Data
         * @param a
         *            Address
         * @param ist
         *            Input string
         * @param jbc
         *            Javaboy style.
         */
        public DisassembledInstruction(final List<Integer> d, final int a,
                final String ist, final boolean jbc) {
            this.data = d;
            this.addr = a;
            this.instructionString = ist;
            this.javaboyComp = jbc;
        }

        /**
         * @return the addr
         */
        public final int getAddr() {
            return addr;
        }

        /**
         * @return the data
         */
        public final List<Integer> getData() {
            return data;
        }

        /**
         * @return the instructionString
         */
        public final String getInstructionString() {
            return instructionString;
        }

        /**
         * Overloaded toString for instructions.
         * @return A string representation of this disassembled instruction.
         */
        public final String toString() {
            String out = String.format(hexFormat(4, javaboyComp) + ":", addr);
            for (int d : data) {
                out += " " + String.format(hexFormat(2, javaboyComp), d);
            }
            for (int i = 0; i < 10 - data.size() * 3; i++) {
                out += " ";
            }
            out += instructionString;
            return out;
        }
    }
}
