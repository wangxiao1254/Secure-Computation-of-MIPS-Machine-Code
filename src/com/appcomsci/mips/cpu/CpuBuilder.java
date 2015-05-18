package com.appcomsci.mips.cpu;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import backend.flexsc.Config;
import backend.flexsc.FlexSCCodeGenerator;

import com.appcomsci.mips.binary.DataSegment;
import com.appcomsci.mips.binary.SymbolTableEntry;
import com.appcomsci.mips.memory.MipsInstructionSet;
import com.appcomsci.mips.memory.MipsProgram;
import com.appcomsci.sfe.common.Configuration;

public class CpuBuilder extends MipsProgram {
	private static final String CPU_FILE_NAME = "cpu.txt";

	private static final String lineSeparator = System.getProperty("line.separator");
	private static final String fileSeparator = System.getProperty("file.separator");
	
	/** The main text of the CPU program */
	private List<String> text;
	/** The main text of the wrapper */
	private List<String> wrapper;
	private List<Map.Entry<String, List<String>>> actions;
	
	/** This constructor takes the CPU template from the classpath
	 * @throws IOException
	 */
	public CpuBuilder(String args[]) throws Exception {
		super(args);
		init();
	}
	
	public CpuBuilder(Configuration config, String binaryFileName) throws Exception {
		super(config, binaryFileName);
		init();
	}
	
	private void init() throws Exception {
		// Look for cpu.txt inside jar file (we hope)
		InputStream is = getClass().getResourceAsStream(CPU_FILE_NAME);
		if(is == null) // Did it get slightly mislaid?
			is = getClass().getResourceAsStream(fileSeparator+CPU_FILE_NAME);
		if(is == null) // Really mislaid.  Oops.
			throw new FileNotFoundException("Could not find " + CPU_FILE_NAME + " in classpath");
		setup(new InputStreamReader(is));
	}
	
	/**
	 * This constructor reads the CPU template from a Reader.
	 * Use InputStreamReader to bridge the gap from InputStreams
	 * @param rdr The reader containing the CPU template
	 * @throws IOException

	public CpuBuilder(Reader rdr) throws IOException {
		setup(rdr);
	}
	*/
	
	/**
	 * This constructor reads the CPU template from a File.
	 * @param f The file containing the CPU template.
	 * @throws IOException
	public CpuBuilder(File f) throws IOException {
		setup(Files.readAllLines(f.toPath(),StandardCharsets.US_ASCII));
	}
	*/
	
	/**
	 * Constructor setup
	 * @param rdr A reader 
	 * @throws IOException
	 */
	private void setup(java.io.Reader rdr) throws IOException {
		BufferedReader br = new BufferedReader(rdr);
		List<String> rawLines = new ArrayList<String>();
		String s;
		while((s=br.readLine()) != null)
			rawLines.add(s);
		setup(rawLines);
	}
	
	/**
	 * More onstructor setup.  This organizes the template file.
	 * @param rawLines A list of lines from the CPU template file
	 */
	private void setup(List<String>rawLines) {
		text = new ArrayList<String>();
		actions = new ArrayList<Map.Entry<String, List<String>>>();
		Set<String>mnemonicSet = new HashSet<String>();
		List<String> current = text;
		for(String s:rawLines) {
			if(s.startsWith("%OP_")) {
				// Need to check for duplicates!
				String mnemonic = s.substring(4);
				if(mnemonicSet.contains(mnemonic)) {
					System.err.println("Warning: duplicate actions for " + mnemonic);
				}
				current = new ArrayList<String>();
				actions.add(new AbstractMap.SimpleImmutableEntry<String, List<String>>(mnemonic, current));
			} else if(s.startsWith("%WRAPPER")) {
				wrapper = new ArrayList<String>();
				current = wrapper;
			} else {
				current.add(s);
			}
		}
	}
	
	/**
	 * Generate actions for operations of type "type".  The actions are a large if statement of the form
	 * <br>
	 * if(op_type == some_type) {
	 * &nbsp;&nbsp;// Actions for type some_type
	 * <br>
	 * &nbsp;&nbsp;if(appropriate_var == something) {
	 * <br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;actions
	 * <br>
	 * &nbsp;&nbsp;} else if(var == something_else) {
	 * <br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;other actions
	 * <br>
	 * &nbsp;&nbsp;}
	 * <br>
	 * } else if(op_type == some_other_type) {
	 * &nbsp;&nbsp;// Actions for some_other_type
	 * <br>
	 * }
	 * <br>
	 * @param sb Emit code here
	 * @param codeWritten Has any code been written to the outer "if" yet?  Used to decide
	 * 				whether or not to add an "else"
	 * @param operations The set of operations to implement
	 * @param type The general class of operations (REGIMM, FUNCT, regular, memory read or write)
	 * @return False if no code has been written to the outer "if" yet.  True otherwise.
	 * 				This constitutes an updated value for codeWritten.
	 */
	private boolean emitActions(StringBuilder sb, boolean codeWritten, Set<String>operations, MipsInstructionSet.OperationType type) {
		if(operations.size() == 0)
			return codeWritten;
		
		String varName = null;
		// Decide which variable to check for the operation
		switch(type) {
		case I:
		case J:
		case MR:
		case MW:
			varName = "op";
			break;
		case FUNCT:
			varName = "funct";
			break;
		case REGIMM:
			varName = "rt";	// The regimm bits live here
			break;
		default:
			varName = "?";
			break;
		}

		sb.append("\t");
		if(codeWritten)
			sb.append("else ");
		sb.append("if(op_type == ");
		switch(type) {
		case I:
			sb.append("OP_CODE_I");
			break;
		case FUNCT:
			sb.append("OP_CODE_R");
			break;
		case REGIMM:
			sb.append("OP_CODE_REGIMM");
			break;
		case J:
			sb.append("OP_CODE_J");
			break;
		default:
			sb.append("?");
		}
		sb.append(") {");
		sb.append(lineSeparator);

		boolean actionsWritten = false;

		// Output actions in order give in input file
		for(Map.Entry<String, List<String>>e: actions) {
			String op = e.getKey();
			if(operations.contains(op)) {
				sb.append("\t\t");
				if(actionsWritten)
					sb.append("else ");
				actionsWritten = true;
				sb.append("if(");
				sb.append(varName);
				sb.append(" == OP_");
				sb.append(op);
				sb.append(") {");
				sb.append(lineSeparator);
				for(String x:e.getValue()) {
					sb.append("\t\t");
					sb.append(x);
					sb.append(lineSeparator);
				}
				sb.append("\t\t}");
				sb.append(lineSeparator);
			}
		}
		sb.append("\t}");
		sb.append(lineSeparator);
		return true;
	}
	
	public void buildWrapper(Set<Long>operations, String packageName, String className, File f) throws FileNotFoundException {
		PrintStream w = new PrintStream(f);
		buildWrapper(operations, packageName, className, w);
	}	
	
	public void buildWrapper(Set<Long>operations, String packageName, String className, PrintStream w) {
		StringBuilder sb = new StringBuilder();
		buildWrapper(operations, packageName, className, sb);
		w.print(sb.toString());
	}
	
	public void buildWrapper(Set<Long>InstructionSet, String packageName, String className, StringBuilder sb) {
		Set<MipsInstructionSet.Operation> operations = Utils.makeOperationSet(InstructionSet);
		setNeedMult(operations);
		for(String s:wrapper) {
			if(s.startsWith("%OPCODES")) {
				// Write out list of operations
				for(MipsInstructionSet.Operation o : operations) {
					sb.append("\t\t\"");
					sb.append(o.toString());
					sb.append("\",");
					sb.append(lineSeparator);
				}
			} else if(s.startsWith("%HILO_REG")) {
				// Arcane knowledge of the name of the mult regs.
				if(needMult)
					sb.append("\t, hiLo");
			} else if(s.startsWith("%PACKAGE")) {
				sb.append("package ");
				sb.append(packageName);
				sb.append(";");
				sb.append(lineSeparator);
			} else {
				if(s.contains("%CLASS")) {
					s = s.replace("%CLASS", className);
				}
				sb.append(s);
				sb.append(lineSeparator);
			}
		}
	}
	
	/** Build a CPU
	 * 
	 * @param operations The set of operations to be implemented
	 * @param f Write the CPU program to this file
	 * @throws FileNotFoundException
	 */
	
	public void buildCpu(Set<Long>operations, String packageName, String className, File f) throws FileNotFoundException {
		PrintStream w = new PrintStream(f);
		buildCpu(operations, packageName, className, w);
		w.close();
	}
	
	/** Build a CPU
	 * 
	 * @param operations The set of operations to be implemented
	 * @param w Write the CPU program here.  Note that w is <b>not</b> closed on exit.
	 */
	
	public void buildCpu(Set<Long>operations, String packageName, String className, PrintStream w) {
		StringBuilder sb = new StringBuilder();
		buildCpu(operations, packageName, className, sb);
		w.print(sb.toString());
	}

	int countone(boolean[] a) {
		int res = 0;
		for(boolean b : a)res += (b ? 1 : 0);
		return res;
	}
	
	
	/**
	 * Build a CPU
	 * @param operations The set of operations to be implemented
	 * @return The text of the CPU
	 */
	public void buildCpu(Set<Long>InstructionSet, String packageName, String className, StringBuilder sb) {
		Set<MipsInstructionSet.Operation> operations = Utils.makeOperationSet(InstructionSet);

		// Build sets of ops by type.
		// Also keep track of whether there were any multiplies or divides
		setNeedMult(operations);
		
		Set<String> I_ops = new HashSet<String>();
		Set<String> R_ops = new HashSet<String>();
		Set<String> REGIMM_ops = new HashSet<String>();
		Set<String> J_ops = new HashSet<String>();
		boolean[] touchedRs = new boolean[32];
		boolean[] touchedRt = new boolean[32];
		boolean[] touchedRd = new boolean[32];
		
		boolean[] MEMRS = new boolean[32];
		boolean[] MEMRT = new boolean[32];
		for(Long l : InstructionSet) {
			MipsInstructionSet.Operation o = MipsInstructionSet.Operation.valueOf(l);
//			System.out.println(o.toString()+" "+o.getType().toString());
			switch(o.getType()) {
			case I:
				I_ops.add(o.toString());
				break;
			case MR:	// Ignore these
			case MW:
				break;
			case FUNCT:
				R_ops.add(o.toString());
				break;
			case REGIMM:
				REGIMM_ops.add(o.toString());
				break;
			case J:
				J_ops.add(o.toString());
			}
			//this part is imcomplete... ad hoc impl for now.
			if(o.getType().equals(MipsInstructionSet.OperationType.MR) ||
					o.getType().equals(MipsInstructionSet.OperationType.MW)) {
				MEMRS[MipsInstructionSet.getSrcReg(l)] = true;
				MEMRT[MipsInstructionSet.getSrc2Reg(l)] = true;
			}
			else if(o.getType().equals(MipsInstructionSet.OperationType.FUNCT)) {
				touchedRs[MipsInstructionSet.getSrcReg(l)] = true;
				touchedRt[MipsInstructionSet.getSrc2Reg(l)] = true;
				touchedRd[MipsInstructionSet.getDestReg(l)] = true;
			}
			else if(o.getType().equals(MipsInstructionSet.OperationType.I)){
				touchedRs[MipsInstructionSet.getSrcReg(l)] = true;
				touchedRt[MipsInstructionSet.getSrc2Reg(l)] = true;				
			}
			else if(o.equals(MipsInstructionSet.Operation.BEQ) ||
					o.equals(MipsInstructionSet.Operation.BNE)
					){
				touchedRs[MipsInstructionSet.getSrcReg(l)] = true;
				touchedRt[MipsInstructionSet.getSrc2Reg(l)] = true;
			}
			else if(o.equals(MipsInstructionSet.Operation.BLEZ) ||
					o.equals(MipsInstructionSet.Operation.BLTZ)) {
				touchedRs[MipsInstructionSet.getSrcReg(l)] = true;
			}
			else 
				System.out.println(o.toString()+" "+o.getType().toString());
		}
		
		System.out.println(countone(touchedRs) + " "+util.Utils.toInt(touchedRs));
		System.out.println(countone(touchedRt) + " "+util.Utils.toInt(touchedRt));
		System.out.println(countone(touchedRd) + " "+util.Utils.toInt(touchedRd));
		for(String s:text) {
			if(s.startsWith("%CHECK_TYPE")) {
				if(J_ops.size() > 0) {
					sb.append("\t if(");
					sb.append(lineSeparator);
					sb.append("\t\t");
					boolean codeWritten = false;
					for(String o: J_ops) {
						if(codeWritten)
							sb.append(" || ");
						codeWritten = true;
						sb.append("op == OP_");
						sb.append(o.toString());
					}
					sb.append(lineSeparator);
					sb.append("\t)");
					sb.append(lineSeparator);
					sb.append("\t\tret = OP_CODE_J;");
					sb.append(lineSeparator);	
				}
			} else if(s.startsWith("%HILO_REG")) {
				if(needMult)
					sb.append("\t, secure int32[public 2] hiLo");
			} else if(s.startsWith("%ACTIONS")) {
				boolean codeWritten = false;
				codeWritten = emitActions(sb, codeWritten, I_ops, MipsInstructionSet.OperationType.I);
				codeWritten = emitActions(sb, codeWritten, J_ops, MipsInstructionSet.OperationType.J);
				codeWritten = emitActions(sb, codeWritten, R_ops, MipsInstructionSet.OperationType.FUNCT);
				emitActions(sb, codeWritten, REGIMM_ops, MipsInstructionSet.OperationType.REGIMM);
			} else if(s.startsWith("%TOUCHED")) {
				sb.append("\t public int32 TOUCHED_RS = 0x"+Integer.toHexString(util.Utils.toInt(touchedRs))+";\n");
				sb.append("\t public int32 TOUCHED_RT = 0x"+Integer.toHexString(util.Utils.toInt(touchedRt))+";\n");
				sb.append("\t public int32 TOUCHED_RD = 0x"+Integer.toHexString(util.Utils.toInt(touchedRd))+";\n");
				sb.append("\t public int32 MEMRS = 0x"+Integer.toHexString(util.Utils.toInt(MEMRS))+";\n");
				sb.append("\t public int32 MEMRT = 0x"+Integer.toHexString(util.Utils.toInt(MEMRT))+";\n");
			}
			else {
				if(s.contains("%CLASS")) {
					s = s.replace("%CLASS", className);
				}
				sb.append(s);
				sb.append(lineSeparator);
			}
		}
	}
	
	private boolean needMult = false;
	
	private void setNeedMult(Set<MipsInstructionSet.Operation>operations) {
		for(MipsInstructionSet.Operation o : operations) {
			switch(o) {
			case MFHI:
			case MTHI:
			case MFLO:
			case MTLO:
			case MULT:
			case MULTU:
			case DIV:
			case DIVU:
				needMult = true;
				break;
			default:		// Shut compiler up
				break;
			}
		}
	}

	/** Main program for testing
	 * 
	 * @param args A list of operations to be implemented.  If empty, the complete set of operations
	 *		defined by MipsInstructionSet.Operation.values() will be implemented.
	 */
	public static void main(String args[]) throws Exception {
		Set<MipsInstructionSet.Operation> operations = new HashSet<MipsInstructionSet.Operation>();
		CpuBuilder bldr = null;
		try {
			bldr = new CpuBuilder(args);
		} catch(FileNotFoundException e) {
			System.err.println("No " + CPU_FILE_NAME + " despite existence check");
		} catch(IOException e) {
			System.err.println("Error reading " + CPU_FILE_NAME + ": " + e);
		}
		bldr.build();
	}
	
	/** This default method uses the root as the full class name.
	 * 
	 */
	// c.f. similar code in CpuFactory.  Should be refactored.
	public void build() throws Exception {
		Configuration config = getConfiguration();
		Set<Long>instructions = new HashSet<Long>();
		
		/*if(getMipsBinaryPath() == null) {
			// No args means do them all
			for(MipsInstructionSet.Operation op: MipsInstructionSet.Operation.values())
				instructions.add(op);

		} else*/ {
			com.appcomsci.mips.binary.Reader rdr = new com.appcomsci.mips.binary.Reader(new File(getMipsBinaryPath()), config);
			SymbolTableEntry ent = rdr.getSymbolTableEntry(getEntryPoint());	
			DataSegment inst = rdr.getInstructions(getFunctionLoadList());
			for(long l:inst.getData()) {
				instructions.add(l);
			}
		}

		String packageName = getConfiguration().getPackageName();
		String classDirectory = getConfiguration().getOutputDirectory();
		if(classDirectory == null)
			throw new Exception("No output directory given");
		classDirectory += fileSeparator + packageName.replace(".", fileSeparator);
		Files.createDirectories(FileSystems.getDefault().getPath(classDirectory));
		
		String className = config.getClassNameRoot();
		String wrapperClassName = className + "Impl";
		
		File cpuFile = new File(classDirectory + fileSeparator + className + ".cpp");		
		/*if(cpuFile.exists()) {
			System.err.println("Will not overwrite " + cpuFile.getPath());
			// throw new Exception("File " + cpuFIle.getPath() + " already exists");
			return;
		}*/
		
		File wrapperFile = new File(classDirectory + fileSeparator + wrapperClassName + ".java");
		/*if(wrapperFile.exists()) {
			System.err.println("Will not overwrite " + wrapperFile.getPath());
			// throw new Exception("File " + wrapperFile.getPath() + " already exists");
			return;
		}*/
		
		buildCpu(instructions, packageName, className, cpuFile);
		buildWrapper(instructions, packageName, className, wrapperFile);
		
		FlexSCCodeGenerator compiler = new FlexSCCodeGenerator(cpuFile.getAbsolutePath());
		Config cfg = new Config();
		cfg.path = classDirectory;
		cfg.packageName = packageName;
		System.out.println(cfg.path +" "+cfg.packageName);
		compiler.FlexSCCodeGen(cfg, true, false);
	}

	@Override
	protected void printUsage() {
		System.err.println("Usage!");
	}
}
