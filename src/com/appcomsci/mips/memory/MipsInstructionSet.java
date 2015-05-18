/**
 * 
 */
package com.appcomsci.mips.memory;

import java.util.HashMap;
import java.util.Map;

/**
 * Constants and bit picking for the MIPS instruction set
 * 
 * Reference: MIPS32(TM) Architecture for Programmers
 *            Volume II: The MIPS32(TM) Instruction Set
 *            Document Number: MD00086
 *            Revision 2.00
 *            June 9, 2003
 *            
 * MIPS32 is a trademark of MIPS Technologies, Inc
 * 
 * @author Allen McIntosh
 *
 */
public class MipsInstructionSet {
	// Some opcodes, expressed as small integers
	
	public static final int OP_FUNCT  = 0x00;
	public static final int OP_REGIMM = 0x01;
	public static final int OP_J      = 0x02;
	public static final int OP_JAL    = 0x03; // Link
	public static final int OP_BEQ    = 0x04;
	public static final int OP_BNE    = 0x05;
	public static final int OP_BLEZ   = 0x06;
	public static final int OP_BGTZ   = 0x07;
	
	// Function codes
	public static final int OP_JR    = 0x08;
	public static final int OP_JALR  = 0x09;
	
	// IMM codes
	public static final int OP_BLTZ   = 0x00;
	public static final int OP_BGEZ   = 0x01;
	public static final int OP_BLTZAL = 0x10; // Link.
	public static final int OP_BGEZAL = 0x11; // Link.
	
	// Constants for bit picking functions
	
	public static final int OP_SHIFT = 26;
	public static final int OP_MASK = 0x3f;
	
	public static final int OP_REGIMM_CODE_SHIFT = 16;
	public static final int OP_REGIMM_CODE_MASK = 0x1f;
	
	public static final int SRC_REG_SHIFT = 21;
	public static final int SRC_REG_MASK  = 0x1f;
	
	public static final int SRC2_REG_SHIFT = 16;
	public static final int SRC2_REG_MASK = 0x1f;
	
	public static final int DEST_REG_SHIFT = 11;
	public static final int DEST_REG_MASK = 0x1f;
	
	public static final int FUNCT_SHIFT = 0;
	public static final int FUNCT_MASK = 0x3f;
	
	public static final int OFFSET_SHIFT = 0;
	public static final int OFFSET_MASK = 0xffff;
	public static final int OFFSET_SXT_SHIFT = 16;
	
	public static final int INSTR_INDEX_SHIFT = 0;
	public static final int INSTR_INDEX_MASK = 0x03ffffff;
	
	public static final int RETURN_REG = 0x1f;
	
	// This really doesn't belong in this file
	/** Address presumed to be a branch to itself */
	public static final String SPIN_SYMBOL = "hlt";
	public static final long DEFAULT_SPIN_ADDRESS = -1L;
	private static long spinAddress = DEFAULT_SPIN_ADDRESS;
	public static final long SPIN_INSTRUCTION = 0xffff | (OP_BEQ<<OP_SHIFT);
	public static final long NOP = 0x0;
	
	/**
	 * Pull an op out of instruction as a small integer between 0 and 127 inclusive
	 * @param instruction The original instruction
	 * @return The op
	 */
	public static int getOp(long instruction) {
		return (int)((instruction>>OP_SHIFT)&OP_MASK);
	}
	
	/**
	 * Pull an op out of instruction as a small integer between 0 and 127 inclusive
	 * @param instruction The original instruction
	 * @return The op
	 */
	public static int getOp(int instruction) {
		return (instruction>>OP_SHIFT)&OP_MASK;
	}
	
	/**
	 * Pull the IMM code out of an instruction as a small integer between 0 and 31 inclusive
	 * @param instruction The original instruction
	 * @return The IMM code
	 */
	public static int getRegImmCode(long instruction) {
		return (int)((instruction>>OP_REGIMM_CODE_SHIFT)&OP_REGIMM_CODE_MASK);
	}
	
	/**
	 * Pull the IMM code out of an instruction as a small integer between 0 and 31 inclusive
	 * @param instruction The original instruction
	 * @return The IMM code
	 */
	public static int getRegImmCode(int instruction) {
		return (instruction>>OP_REGIMM_CODE_SHIFT)&OP_REGIMM_CODE_MASK;
	}
	
	/** Pull the function code out of an instruction as a small integer between 0 and 63 inclusive
	 * @param instruction The original instruction
	 * @return The function code
	 */
	public static int getFunct(long instruction) {
		return (int)((instruction>>FUNCT_SHIFT)&FUNCT_MASK);
	}
	
	/** Pull the function code out of an instruction as a small integer between 0 and 63 inclusive
	 * @param instruction The original instruction
	 * @return The function code
	 */
	public static int getFunct(int instruction) {
		return (int)((instruction>>FUNCT_SHIFT)&FUNCT_MASK);
	}
	
	public static int getSrcReg(long instruction) {
		return (int)((instruction>>SRC_REG_SHIFT)&SRC_REG_MASK);
	}
	
	public static int getSrcReg(int instruction) {
		return (instruction>>SRC_REG_SHIFT)&SRC_REG_MASK;
	}

	public static int getDestReg(long instruction) {
		return (int)((instruction>>DEST_REG_SHIFT)&DEST_REG_MASK);
	}
	
	public static int getDestReg(int instruction) {
		return (instruction>>DEST_REG_SHIFT)&DEST_REG_MASK;
	}
	

	
	public static int getSrc2Reg(long instruction) {
		return (int)((instruction>>SRC2_REG_SHIFT)&SRC2_REG_MASK);
	}
	
	public static int getSrc2Reg(int instruction) {
		return (instruction>>SRC2_REG_SHIFT)&SRC2_REG_MASK;
	}

	
	public static int getOffset(long instruction) {
		int r = (int)((instruction>>OFFSET_SHIFT)&OFFSET_MASK);
		return (r<<OFFSET_SXT_SHIFT)>>OFFSET_SXT_SHIFT;
	}
	
	public static int getOffset(int instruction) {
		int r = (instruction>>OFFSET_SHIFT)&OFFSET_MASK;
		return (r<<OFFSET_SXT_SHIFT)>>OFFSET_SXT_SHIFT;
	}
	
	public static int getInstrIndex(long instruction) {
		return (int)((instruction>>INSTR_INDEX_SHIFT)&INSTR_INDEX_MASK);
	}
	
	public static int getInstrIndex(int instruction) {
		return (instruction>>INSTR_INDEX_SHIFT)&INSTR_INDEX_MASK;
	}
	
	public static long getOpBits(long instr) {
		long op = getOp(instr);
		long bits = instr & (OP_MASK << OP_SHIFT);
		if(op == OP_FUNCT) {
			bits |= instr & (FUNCT_MASK << FUNCT_SHIFT);
		} else if(op == OP_REGIMM) {
			bits |= instr & (OP_REGIMM_CODE_MASK << OP_REGIMM_CODE_SHIFT);
		}
		return bits;
	}
	
	public enum OperationType {
		FUNCT,
		I,
		J,		// like I, but the B/J instructions in that set
		MW,		// I, Memory, Write
		MR,		// I, Memory, Read 
		REGIMM
	}
	
	// An enum that covers the interesting MIPS instructions
	
	public enum Operation {
		// FUNCT(0),
		// REGIMM(1L << OP_SHIFT),
		J(2L << OP_SHIFT,				OperationType.J),
		JAL(3L << OP_SHIFT,				OperationType.J),
		BEQ(4L << OP_SHIFT,				OperationType.J),
		BNE(5L << OP_SHIFT,				OperationType.J),
		BLEZ(6L << OP_SHIFT,			OperationType.J),
		BGTZ(7L << OP_SHIFT,			OperationType.J),
		ADDI(8L << OP_SHIFT,			OperationType.I),
		ADDIU(9L << OP_SHIFT,			OperationType.I),
		SLTI(10L << OP_SHIFT,			OperationType.I),
		SLTIU(11L << OP_SHIFT,			OperationType.I),
		ANDI(12L << OP_SHIFT,			OperationType.I),
		ORI(13L << OP_SHIFT,			OperationType.I),
		XORI(14L << OP_SHIFT,			OperationType.I),
		LUI(15L << OP_SHIFT,			OperationType.I),
		LB(32L << OP_SHIFT,				OperationType.MR),
		LH(33L << OP_SHIFT,				OperationType.MR),
		LWL(34L << OP_SHIFT,			OperationType.MR),
		LW(35L << OP_SHIFT,				OperationType.MR),
		LBU(36L << OP_SHIFT,			OperationType.MR),
		LHU(37L << OP_SHIFT,			OperationType.MR),
		LWR(38L << OP_SHIFT,			OperationType.MR),
		SB(40L << OP_SHIFT,				OperationType.MW),
		SH(41L << OP_SHIFT,				OperationType.MW),
		SWL(42L << OP_SHIFT,			OperationType.MW),
		SW(43L << OP_SHIFT,				OperationType.MW),
		SWR(46L << OP_SHIFT,			OperationType.MW),
		SLL(0 | (OP_FUNCT<<OP_SHIFT),	OperationType.FUNCT),
		SRL(2 | (OP_FUNCT<<OP_SHIFT),	OperationType.FUNCT),
		SRA(3 | (OP_FUNCT<<OP_SHIFT),	OperationType.FUNCT),
		SLLV(4 | (OP_FUNCT<<OP_SHIFT),	OperationType.FUNCT),
		SRLV(6 | (OP_FUNCT<<OP_SHIFT),	OperationType.FUNCT),
		SRAV(7 | (OP_FUNCT<<OP_SHIFT),	OperationType.FUNCT),
		JR(8 | (OP_FUNCT<<OP_SHIFT),	OperationType.FUNCT),
		JALR(9 | (OP_FUNCT<<OP_SHIFT),	OperationType.FUNCT),
		MOVZ(10 | (OP_FUNCT<<OP_SHIFT),	OperationType.FUNCT),
		MOVN(11 | (OP_FUNCT<<OP_SHIFT),	OperationType.FUNCT),
		MFHI(16 | (OP_FUNCT<<OP_SHIFT),	OperationType.FUNCT),
		MTHI(17 | (OP_FUNCT<<OP_SHIFT),	OperationType.FUNCT),
		MFLO(18 | (OP_FUNCT<<OP_SHIFT),	OperationType.FUNCT),
		MTLO(16 | (OP_FUNCT<<OP_SHIFT),	OperationType.FUNCT),
		MULT(24 | (OP_FUNCT<<OP_SHIFT),	OperationType.FUNCT),
		MULTU(25 | (OP_FUNCT<<OP_SHIFT),	OperationType.FUNCT),
		DIV(26 | (OP_FUNCT<<OP_SHIFT),	OperationType.FUNCT),
		DIVU(27 | (OP_FUNCT<<OP_SHIFT),	OperationType.FUNCT),
		ADD(32 | (OP_FUNCT<<OP_SHIFT),	OperationType.FUNCT),
		ADDU(33 | (OP_FUNCT<<OP_SHIFT),	OperationType.FUNCT),
		SUB(34 | (OP_FUNCT<<OP_SHIFT),	OperationType.FUNCT),
		SUBU(35 | (OP_FUNCT<<OP_SHIFT),	OperationType.FUNCT),
		AND(36 | (OP_FUNCT<<OP_SHIFT),	OperationType.FUNCT),
		OR(37 | (OP_FUNCT<<OP_SHIFT),	OperationType.FUNCT),
		XOR(38 | (OP_FUNCT<<OP_SHIFT),	OperationType.FUNCT),
		NOR(39 | (OP_FUNCT<<OP_SHIFT),	OperationType.FUNCT),
		SLT(42 | (OP_FUNCT<<OP_SHIFT),	OperationType.FUNCT),
		SLTU(43 | (OP_FUNCT<<OP_SHIFT),	OperationType.FUNCT),
		BLTZ((0<<OP_REGIMM_CODE_SHIFT) | (OP_REGIMM<<OP_SHIFT), OperationType.REGIMM),
		BGEZ((1<<OP_REGIMM_CODE_SHIFT) | (OP_REGIMM<<OP_SHIFT),	OperationType.REGIMM),
		BLTZAL((16<<OP_REGIMM_CODE_SHIFT) | (OP_REGIMM<<OP_SHIFT), OperationType.REGIMM),
		BGEZAL((17<<OP_REGIMM_CODE_SHIFT) | (OP_REGIMM<<OP_SHIFT),	OperationType.REGIMM)
		;
		
		private static Map<Long, Operation> opMap;
		static {
			opMap = new HashMap<Long, Operation>();
			for(Operation o: values()) {
				opMap.put(o.value, o);
			};
		}
		
		public static Operation valueOf(long instr) {
			return opMap.get(getOpBits(instr));
		}
		
		private final long value;
		private final OperationType type;
		private Operation(long value, OperationType type) {
			this.value = value;
			this.type = type;
		}

		public long getValue() {
			return value;
		}

		public OperationType getType() {
			return type;
		}
	}

	/**
	 * @return the spinAddress
	 */
	public static long getSpinAddress() {
		return spinAddress;
	}

	/**
	 * @param spinAddress the spinAddress to set
	 */
	public static void setSpinAddress(long spinAddress) {
		MipsInstructionSet.spinAddress = spinAddress;
	}
}
