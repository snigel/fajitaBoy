package fajitaboy;

import java.util.List;
import java.util.LinkedList;

/**
 * Holds a static list of the Game Boy instruction set.
 * @author arvid
 */
public class InstructionSet {
    /**
     * Enum for types of instructions.
     * Used for parsing.
     */
    public static enum InstructionArgument {
        /** Types of instructions. */
        VALUE ('%', 1), DOUBLE_VALUE ('#', 2), ADDRESS ('&', 2), SIGNED_OFFSET ('$', 2);

        /** Representation. */
        private final char fChar;
        /** Size. */
        private final int size;

        /**
         * Constructor.
         * @param c Representation
         * @param s Size
         */
        InstructionArgument(final char c, final int s) {
            this.fChar = c;
            this.size  = s;
        }

        /**
         * Get representation.
         * @return representation
         */
        public char getFChar() {
            return this.fChar;
        }

        /**
         * Get size.
         * @return size
         */
        public int getSize() {
            return this.size;
        }
    }
    /**
     * Returns the instruction with given opcode.
     * @param opcode opcode-number
     * @return instruction
     */
    public static Instruction getInstruction(final int opcode) {
        //return instructions[opcode];
        return instructions[opcode];
    }

    /**
     * @author arvid
     */
    static public class Instruction {
        /** Name. */
        private String instructionName;
        /** Operation code. */
        private int opCode;
        /** Pretty output name. */
        private String prettyName;
        /** List of arguemnts taken by this instruction. */
        private List<InstructionArgument> arguments;

        // public void Instruction(final String name, final String pretty, final
        // String args) {
        /**
         * Constructor.
         * @param opCodez opCode
         * @param name Instruction name
         * @param pretty Instruction pretty name
         */
        private Instruction(final int opCodez, final String name,
                final String pretty) {
            this.instructionName = name;
            this.prettyName = pretty;
            //this.arguments = args;
        }

        /**
         * Returns the pretty name of an instruction.
         * @return pretty name
         */
        public String getPrettyName() {
            return prettyName;
        }
        /**
         * Get the arguements to the instruction.
         * @return arguments
         */
        public List<InstructionArgument> getArguments() {
            return arguments;
        }
    }

    /**
     * THE list of instructions. Ugh. :)
     */
    private static Instruction[] instructions = {
        new Instruction(0x0, "NOP", "NOP"),
        new Instruction(0x1, "LD   BC,nn", "LD   BC,#"),
        new Instruction(0x2, "LD   (BC),A", "LD   (BC),A"),
        new Instruction(0x3, "INC  BC", "INC  BC"),
        new Instruction(0x4, "INC  B", "INC  B"),
        new Instruction(0x5, "DEC  B", "DEC  B"),
        new Instruction(0x6, "LD   B,n", "LD   B,%"),
        new Instruction(0x7, "RLCA", "RLCA"),
        new Instruction(0x8, "LD (nn),SP", "LD (#),SP"),
        new Instruction(0x9, "ADD  HL,BC", "ADD  HL,BC"),
        new Instruction(0xa, "LD   A,(BC)", "LD   A,(BC)"),
        new Instruction(0xb, "DEC  BC", "DEC  BC"),
        new Instruction(0xc, "INC  C", "INC  C"),
        new Instruction(0xd, "DEC  C", "DEC  C"),
        new Instruction(0xe, "LD   C,n", "LD   C,%"),
        new Instruction(0xf, "RRCA", "RRCA"),
        new Instruction(0x10, "STOP", "STOP"),
        new Instruction(0x11, "LD   DE,nn", "LD   DE,#"),
        new Instruction(0x12, "LD   (DE),A", "LD   (DE),A"),
        new Instruction(0x13, "INC  DE", "INC  DE"),
        new Instruction(0x14, "INC  D", "INC  D"),
        new Instruction(0x15, "DEC  D", "DEC  D"),
        new Instruction(0x16, "LD   D,n", "LD   D,%"),
        new Instruction(0x17, "RLA", "RLA"),
        new Instruction(0x18, "JR   d", "JR   d"),
        new Instruction(0x19, "ADD  HL,DE", "ADD  HL,DE"),
        new Instruction(0x1a, "LD   A,(DE)", "LD   A,(DE)"),
        new Instruction(0x1b, "DEC  DE", "DEC  DE"),
        new Instruction(0x1c, "INC  E", "INC  E"),
        new Instruction(0x1d, "DEC  E", "DEC  E"),
        new Instruction(0x1e, "LD   E,n", "LD   E,%"),
        new Instruction(0x1f, "RRA", "RRA"),
        new Instruction(0x20, "JR   NZ,d", "JR   NZ,d"),
        new Instruction(0x21, "LD   HL,nn", "LD   HL,#"),
        new Instruction(0x22, "LD (HLI),A", "LD (HLI),A"),
        new Instruction(0x23, "INC  HL", "INC  HL"),
        new Instruction(0x24, "INC  H", "INC  H"),
        new Instruction(0x25, "DEC  H", "DEC  H"),
        new Instruction(0x26, "LD   H,n", "LD   H,%"),
        new Instruction(0x27, "DAA", "DAA"),
        new Instruction(0x28, "JR   Z,d", "JR   Z,d"),
        new Instruction(0x29, "ADD  HL,HL", "ADD  HL,HL"),
        new Instruction(0x2a, "LD A,(HLI)", "LD A,(HLI)"),
        new Instruction(0x2b, "DEC  HL", "DEC  HL"),
        new Instruction(0x2c, "INC  L", "INC  L"),
        new Instruction(0x2d, "DEC  L", "DEC  L"),
        new Instruction(0x2e, "LD   L,n", "LD   L,%"),
        new Instruction(0x2f, "CPL", "CPL"),
        new Instruction(0x30, "JR   NC,d", "JR   NC,d"),
        new Instruction(0x31, "LD   SP,nn", "LD   SP,#"),
        new Instruction(0x32, "LD (HLD),A", "LD (HLD),A"),
        new Instruction(0x33, "INC  SP", "INC  SP"),
        new Instruction(0x34, "INC  (HL)", "INC  (HL)"),
        new Instruction(0x35, "DEC  (HL)", "DEC  (HL)"),
        new Instruction(0x36, "LD   (HL),n", "LD   (HL),%"),
        new Instruction(0x37, "SCF", "SCF"),
        new Instruction(0x38, "JR   C,d", "JR   C,d"),
        new Instruction(0x39, "ADD  HL,SP", "ADD  HL,SP"),
        new Instruction(0x3a, "LD A,(HLD)", "LD A,(HLD)"),
        new Instruction(0x3b, "DEC  SP", "DEC  SP"),
        new Instruction(0x3c, "INC  A", "INC  A"),
        new Instruction(0x3d, "DEC  A", "DEC  A"),
        new Instruction(0x3e, "LD   A,n", "LD   A,%"),
        new Instruction(0x3f, "CCF", "CCF"),
        new Instruction(0x40, "LD   B,B", "LD   B,B"),
        new Instruction(0x41, "LD   B,C", "LD   B,C"),
        new Instruction(0x42, "LD   B,D", "LD   B,D"),
        new Instruction(0x43, "LD   B,E", "LD   B,E"),
        new Instruction(0x44, "LD   B,H", "LD   B,H"),
        new Instruction(0x45, "LD   B,L", "LD   B,L"),
        new Instruction(0x46, "LD   B,(HL)", "LD   B,(HL)"),
        new Instruction(0x47, "LD   B,A", "LD   B,A"),
        new Instruction(0x48, "LD   C,B", "LD   C,B"),
        new Instruction(0x49, "LD   C,C", "LD   C,C"),
        new Instruction(0x4a, "LD   C,D", "LD   C,D"),
        new Instruction(0x4b, "LD   C,E", "LD   C,E"),
        new Instruction(0x4c, "LD   C,H", "LD   C,H"),
        new Instruction(0x4d, "LD   C,L", "LD   C,L"),
        new Instruction(0x4e, "LD   C,(HL)", "LD   C,(HL)"),
        new Instruction(0x4f, "LD   C,A", "LD   C,A"),
        new Instruction(0x50, "LD   D,B", "LD   D,B"),
        new Instruction(0x51, "LD   D,C", "LD   D,C"),
        new Instruction(0x52, "LD   D,D", "LD   D,D"),
        new Instruction(0x53, "LD   D,E", "LD   D,E"),
        new Instruction(0x54, "LD   D,H", "LD   D,H"),
        new Instruction(0x55, "LD   D,L", "LD   D,L"),
        new Instruction(0x56, "LD   D,(HL)", "LD   D,(HL)"),
        new Instruction(0x57, "LD   D,A", "LD   D,A"),
        new Instruction(0x58, "LD   E,B", "LD   E,B"),
        new Instruction(0x59, "LD   E,C", "LD   E,C"),
        new Instruction(0x5a, "LD   E,D", "LD   E,D"),
        new Instruction(0x5b, "LD   E,E", "LD   E,E"),
        new Instruction(0x5c, "LD   E,H", "LD   E,H"),
        new Instruction(0x5d, "LD   E,L", "LD   E,L"),
        new Instruction(0x5e, "LD   E,(HL)", "LD   E,(HL)"),
        new Instruction(0x5f, "LD   E,A", "LD   E,A"),
        new Instruction(0x60, "LD   H,B", "LD   H,B"),
        new Instruction(0x61, "LD   H,C", "LD   H,C"),
        new Instruction(0x62, "LD   H,D", "LD   H,D"),
        new Instruction(0x63, "LD   H,E", "LD   H,E"),
        new Instruction(0x64, "LD   H,H", "LD   H,H"),
        new Instruction(0x65, "LD   H,L", "LD   H,L"),
        new Instruction(0x66, "LD   H,(HL)", "LD   H,(HL)"),
        new Instruction(0x67, "LD   H,A", "LD   H,A"),
        new Instruction(0x68, "LD   L,B", "LD   L,B"),
        new Instruction(0x69, "LD   L,C", "LD   L,C"),
        new Instruction(0x6a, "LD   L,D", "LD   L,D"),
        new Instruction(0x6b, "LD   L,E", "LD   L,E"),
        new Instruction(0x6c, "LD   L,H", "LD   L,H"),
        new Instruction(0x6d, "LD   L,L", "LD   L,L"),
        new Instruction(0x6e, "LD   L,(HL)", "LD   L,(HL)"),
        new Instruction(0x6f, "LD   L,A", "LD   L,A"),
        new Instruction(0x70, "LD   (HL),B", "LD   (HL),B"),
        new Instruction(0x71, "LD   (HL),C", "LD   (HL),C"),
        new Instruction(0x72, "LD   (HL),D", "LD   (HL),D"),
        new Instruction(0x73, "LD   (HL),E", "LD   (HL),E"),
        new Instruction(0x74, "LD   (HL),H", "LD   (HL),H"),
        new Instruction(0x75, "LD   (HL),L", "LD   (HL),L"),
        new Instruction(0x76, "HALT", "HALT"),
        new Instruction(0x77, "LD   (HL),A", "LD   (HL),A"),
        new Instruction(0x78, "LD   A,B", "LD   A,B"),
        new Instruction(0x79, "LD   A,C", "LD   A,C"),
        new Instruction(0x7a, "LD   A,D", "LD   A,D"),
        new Instruction(0x7b, "LD   A,E", "LD   A,E"),
        new Instruction(0x7c, "LD   A,H", "LD   A,H"),
        new Instruction(0x7d, "LD   A,L", "LD   A,L"),
        new Instruction(0x7e, "LD   A,(HL)", "LD   A,(HL)"),
        new Instruction(0x7f, "LD   A,A", "LD   A,A"),
        new Instruction(0x80, "ADD  A,B", "ADD  A,B"),
        new Instruction(0x81, "ADD  A,C", "ADD  A,C"),
        new Instruction(0x82, "ADD  A,D", "ADD  A,D"),
        new Instruction(0x83, "ADD  A,E", "ADD  A,E"),
        new Instruction(0x84, "ADD  A,H", "ADD  A,H"),
        new Instruction(0x85, "ADD  A,L", "ADD  A,L"),
        new Instruction(0x86, "ADD  A,(HL)", "ADD  A,(HL)"),
        new Instruction(0x87, "ADD  A,A", "ADD  A,A"),
        new Instruction(0x88, "ADC  A,B", "ADC  A,B"),
        new Instruction(0x89, "ADC  A,C", "ADC  A,C"),
        new Instruction(0x8a, "ADC  A,D", "ADC  A,D"),
        new Instruction(0x8b, "ADC  A,E", "ADC  A,E"),
        new Instruction(0x8c, "ADC  A,H", "ADC  A,H"),
        new Instruction(0x8d, "ADC  A,L", "ADC  A,L"),
        new Instruction(0x8e, "ADC  A,(HL)", "ADC  A,(HL)"),
        new Instruction(0x8f, "ADC  A,A", "ADC  A,A"),
        new Instruction(0x90, "SUB  B", "SUB  B"),
        new Instruction(0x91, "SUB  C", "SUB  C"),
        new Instruction(0x92, "SUB  D", "SUB  D"),
        new Instruction(0x93, "SUB  E", "SUB  E"),
        new Instruction(0x94, "SUB  H", "SUB  H"),
        new Instruction(0x95, "SUB  L", "SUB  L"),
        new Instruction(0x96, "SUB  (HL)", "SUB  (HL)"),
        new Instruction(0x97, "SUB  A", "SUB  A"),
        new Instruction(0x98, "SBC  A,B", "SBC  A,B"),
        new Instruction(0x99, "SBC  A,C", "SBC  A,C"),
        new Instruction(0x9a, "SBC  A,D", "SBC  A,D"),
        new Instruction(0x9b, "SBC  A,E", "SBC  A,E"),
        new Instruction(0x9c, "SBC  A,H", "SBC  A,H"),
        new Instruction(0x9d, "SBC  A,L", "SBC  A,L"),
        new Instruction(0x9e, "SBC  A,(HL)", "SBC  A,(HL)"),
        new Instruction(0x9f, "SBC  A,A", "SBC  A,A"),
        new Instruction(0xa0, "AND  B", "AND  B"),
        new Instruction(0xa1, "AND  C", "AND  C"),
        new Instruction(0xa2, "AND  D", "AND  D"),
        new Instruction(0xa3, "AND  E", "AND  E"),
        new Instruction(0xa4, "AND  H", "AND  H"),
        new Instruction(0xa5, "AND  L", "AND  L"),
        new Instruction(0xa6, "AND  (HL)", "AND  (HL)"),
        new Instruction(0xa7, "AND  A", "AND  A"),
        new Instruction(0xa8, "XOR  B", "XOR  B"),
        new Instruction(0xa9, "XOR  C", "XOR  C"),
        new Instruction(0xaa, "XOR  D", "XOR  D"),
        new Instruction(0xab, "XOR  E", "XOR  E"),
        new Instruction(0xac, "XOR  H", "XOR  H"),
        new Instruction(0xad, "XOR  L", "XOR  L"),
        new Instruction(0xae, "XOR  (HL)", "XOR  (HL)"),
        new Instruction(0xaf, "XOR  A", "XOR  A"),
        new Instruction(0xb0, "OR   B", "OR   B"),
        new Instruction(0xb1, "OR   C", "OR   C"),
        new Instruction(0xb2, "OR   D", "OR   D"),
        new Instruction(0xb3, "OR   E", "OR   E"),
        new Instruction(0xb4, "OR   H", "OR   H"),
        new Instruction(0xb5, "OR   L", "OR   L"),
        new Instruction(0xb6, "OR   (HL)", "OR   (HL)"),
        new Instruction(0xb7, "OR   A", "OR   A"),
        new Instruction(0xb8, "CP   B", "CP   B"),
        new Instruction(0xb9, "CP   C", "CP   C"),
        new Instruction(0xba, "CP   D", "CP   D"),
        new Instruction(0xbb, "CP   E", "CP   E"),
        new Instruction(0xbc, "CP   H", "CP   H"),
        new Instruction(0xbd, "CP   L", "CP   L"),
        new Instruction(0xbe, "CP   (HL)", "CP   (HL)"),
        new Instruction(0xbf, "CP   A", "CP   A"),
        new Instruction(0xc0, "RET  NZ", "RET  NZ"),
        new Instruction(0xc1, "POP  BC", "POP  BC"),
        new Instruction(0xc2, "JP   NZ,nn", "JP   NZ,#"),
        new Instruction(0xc3, "JP   nn", "JP   #"),
        new Instruction(0xc4, "CALL NZ,nn", "CALL NZ,#"),
        new Instruction(0xc5, "PUSH BC", "PUSH BC"),
        new Instruction(0xc6, "ADD  A,n", "ADD  A,%"),
        new Instruction(0xc7, "RST  0", "RST  0"),
        new Instruction(0xc8, "RET  Z", "RET  Z"),
        new Instruction(0xc9, "RET", "RET"),
        new Instruction(0xca, "JP   Z,nn", "JP   Z,#"),
        new Instruction(0xcb, "Prefix", "Prefix"),
        new Instruction(0xcc, "CALL Z,nn", "CALL Z,#"),
        new Instruction(0xcd, "CALL nn", "CALL #"),
        new Instruction(0xce, "ADC  A,n", "ADC  A,%"),
        new Instruction(0xcf, "RST  8", "RST  8"),
        new Instruction(0xd0, "RET  NC", "RET  NC"),
        new Instruction(0xd1, "POP  DE", "POP  DE"),
        new Instruction(0xd2, "JP   NC,nn", "JP   NC,#"),
        new Instruction(0xd3, "NOP", "NOP"),
        new Instruction(0xd4, "CALL NC,nn", "CALL NC,#"),
        new Instruction(0xd5, "PUSH DE", "PUSH DE"),
        new Instruction(0xd6, "SUB  n", "SUB  %"),
        new Instruction(0xd7, "RST  10H", "RST  10H"),
        new Instruction(0xd8, "RET  C", "RET  C"),
        new Instruction(0xd9, "RETI", "RETI"),
        new Instruction(0xda, "JP   C,nn", "JP   C,#"),
        new Instruction(0xdb, "NOP", "NOP"),
        new Instruction(0xdc, "CALL C,nn", "CALL C,#"),
        new Instruction(0xdd, "NOP", "NOP"),
        new Instruction(0xde, "SBC  A,n", "SBC  A,%"),
        new Instruction(0xdf, "RST  18H", "RST  18H"),
        new Instruction(0xe0, "LD (n),A", "LD (%),A"),
        new Instruction(0xe1, "POP  HL", "POP  HL"),
        new Instruction(0xe2, "LD (C),A", "LD (C),A"),
        new Instruction(0xe3, "NOP", "NOP"),
        new Instruction(0xe4, "NOP", "NOP"),
        new Instruction(0xe5, "PUSH HL", "PUSH HL"),
        new Instruction(0xe6, "AND  n", "AND  %"),
        new Instruction(0xe7, "RST  20H", "RST  20H"),
        new Instruction(0xe8, "ADD SP,s", "ADD SP,s"),
        new Instruction(0xe9, "JP   (HL)", "JP   (HL)"),
        new Instruction(0xea, "LD (nn),A", "LD (#),A"),
        new Instruction(0xeb, "NOP", "NOP"),
        new Instruction(0xec, "NOP", "NOP"),
        new Instruction(0xed, "Prefix", "Prefix"),
        new Instruction(0xee, "XOR  n", "XOR  %"),
        new Instruction(0xef, "RST  28H", "RST  28H"),
        new Instruction(0xf0, "LD A,(n)", "LD A,(%)"),
        new Instruction(0xf1, "POP  AF", "POP  AF"),
        new Instruction(0xf2, "NOP", "NOP"),
        new Instruction(0xf3, "DI", "DI"),
        new Instruction(0xf4, "NOP", "NOP"),
        new Instruction(0xf5, "PUSH AF", "PUSH AF"),
        new Instruction(0xf6, "OR   n", "OR   %"),
        new Instruction(0xf7, "RST  30H", "RST  30H"),
        new Instruction(0xf8, "LDHL SP,s", "LDHL SP,s"),
        new Instruction(0xf9, "LD   SP,HL", "LD   SP,HL"),
        new Instruction(0xfa, "LD A,(nn)", "LD A,(#)"),
        new Instruction(0xfb, "EI", "EI"),
        new Instruction(0xfc, "NOP", "NOP"),
        new Instruction(0xfd, "NOP", "NOP"),
        new Instruction(0xfe, "CP   n", "CP   %"),
        new Instruction(0xff, "RST  38H", "RST  38H")
    };
}