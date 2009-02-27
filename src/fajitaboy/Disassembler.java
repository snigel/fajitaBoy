package fajitaboy;

import java.util.List;
import java.util.LinkedList;

public class Disassembler {
    /** Disassemble a list of bytes.
     * @param input A list of bytes to disassemble.
     * @return A list of strings, representing the disassembled code. Each string is one instruction.
     */
    public static List<DisassembledInstruction> disassemble(final AddressBus bus, final int addr, int len) {
        List<DisassembledInstruction> out = new LinkedList<DisassembledInstruction>();
        int curAddr = addr;
        
        for (int i = 0; i < len; i++) {
            int iAddr = curAddr;
            List iData = new LinkedList<Integer>();
            int opcode = bus.read(curAddr++);
            InstructionSet.Instruction instr = InstructionSet.getInstruction(opcode);
            iData.add(opcode);
            
            String pn = instr.getPrettyName();
            String intrString = pn;
            String formatString = "";

            InstructionSet.InstructionArgument a;
            while ((a = getNextArgument(pn)) != null) {
                formatString = "0x%0" + a.getSize()*2 + "x";
                int argValue = 0;
                for (int j = a.getSize() - 1; j >= 0; j--) {
                    argValue = (argValue << 8) + bus.read(curAddr + j);
                }
                for (int j = 0; j < a.getSize(); j++) {
                    iData.add(bus.read(curAddr + j));
                }
                
                curAddr += a.getSize();

                pn = pn.replaceFirst(Character.toString(a.getFChar()),
                        String.format(formatString, argValue));
            }
            String iStr = String.format("%04x: %s", iAddr, pn); 
            out.add(new Disassembler.DisassembledInstruction(iData, iAddr, iStr));
        }
        
        // all data inläst, returnera.
        return out;
    }
    
    private static InstructionSet.InstructionArgument getNextArgument(final String str) {
        for (InstructionSet.InstructionArgument ia : InstructionSet.InstructionArgument.values()) {
            if (str.indexOf(ia.getFChar()) > -1) {
                return ia;
            }
        }
        // TODO bättre lösning, för att returnera null är satan.
        return null;
    }
    
    public static class DisassembledInstruction {
        private List<Integer> data;
        private int addr;
        String instructionString;
        
        public DisassembledInstruction(final List<Integer> d, final int a, String ist) {
            this.data = d;
            this.addr = a;
            this.instructionString = ist;
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
        
        public String toString() {
            String out = String.format("%04x: ", addr);
            for (int d : data) {
                out += String.format("%02x ", d);
            }
            for (int i = 0; i < 10 - data.size()*3; i++) {
                out += " ";
            }
            out += instructionString;
            return out;
        }
    }
    
}
