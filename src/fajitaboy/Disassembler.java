package fajitaboy;

import java.util.List;
import java.util.LinkedList;


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
     * @return A list of strings, representing the disassembled code. Each
     *         string is one instruction.
     */
    public static List<DisassembledInstruction> disassemble(
            final MemoryInterface bus, final int addr, final int len, final boolean javaboyComp) {

        // Disassemble len instructions, starting at curAddr.

        List<DisassembledInstruction> out;
        out = new LinkedList<DisassembledInstruction>();
        int curAddr = addr;

        for (int i = 0; i < len; i++) {
            // this instruction starts at iAddr.
            int iAddr = curAddr;
            // the data this instruction is disassembled from, is contained in iData.
            List<Integer> iData = new LinkedList<Integer>();
            int opcode = bus.read(curAddr++);
            InstructionSet.Instruction instr;

            instr = InstructionSet.getInstruction(opcode);
            iData.add(opcode);

            String pn = new String(instr.getPrettyName()); //create a copy.
            String formatString = "";

            InstructionSet.InstructionArgument arg;
            //keep reading arguments.
            while ((arg = getNextArgument(pn)) != null) {
                // we could switch over argument type here, format different arguments accordingly.

            	if (javaboyComp) {
            		// if javaboy, do not prefix with 0x, and upcase. 
            		formatString = "%0" + arg.getSize() * 2 + "X";
            	} else {
            		formatString = "0x%0" + arg.getSize() * 2 + "x";
            	}
                // convert arguments
                int argValue = 0;
                for (int j = arg.getSize() - 1; j >= 0; j--) {
                    argValue = (argValue << 8) + bus.read(curAddr + j);
                }
                // save the data for output
                for (int j = 0; j < arg.getSize(); j++) {
                    iData.add(bus.read(curAddr + j));
                }

                // forward adress pointer.
                curAddr += arg.getSize();

                pn = pn.replaceFirst(Character.toString(arg.getFChar()), String
                        .format(formatString, argValue));
            }
            out
                    .add(new Disassembler.DisassembledInstruction(iData, iAddr,
                            pn, javaboyComp));
        }

        // len instructions has been disassembled, return.
        return out;
    }

    /**
     * Get next argument.
     * @param str
     *            input string
     * @return Next instruction argument to disassemble.
     */
    private static InstructionSet.InstructionArgument getNextArgument(
            final String str) {
        for (InstructionSet.InstructionArgument ia : InstructionSet.InstructionArgument
                .values()) {
            if (str.indexOf(ia.getFChar()) > -1) {
                return ia;
            }
        }
        return null;
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
         * 
         */
        private boolean javaboyComp;

        /**
         * Constructor.
         * @param d
         *            data
         * @param a
         *            address
         * @param ist
         *            input string
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
    		// if javaboy, do not prefix with 0x, and upcase.
        	//change here for javaboy
        	String out;
        	if (javaboyComp) {
        		out = String.format("%04X: ", addr);
        	} else {
        		out = String.format("%04x: ", addr);
        	}
        	
            for (int d : data) {
            	if (javaboyComp) {
            		out += String.format("%02X ", d);
            	} else {
            		out += String.format("%02x ", d);
            	}
            }
            for (int i = 0; i < 10 - data.size() * 3; i++) {
                out += " ";
            }
            out += instructionString;
            return out;
        }
    }
}
