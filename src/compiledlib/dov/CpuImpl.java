/**
 * 
 */
package compiledlib.dov;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import oram.Register;
import oram.SecureArray;
import flexsc.CompEnv;
import flexsc.CpuFcn;

/**
 * @author mcintosh
 *
 */
public class CpuImpl<T> implements CpuFcn<T> {
	private CPU<T>cpu = null;
	
	public CpuImpl(CompEnv<T> env) throws Exception {
		this.cpu = new CPU<T>(env);
	}
	public CpuImpl() {
	}

	public static String opcodes[] = {
		"ADDIU",
		"ANDI",
		"LUI",
		"SLTI",
		"ADDU",
		"XOR",
		"SLT",
		"SUBU",
		"SRL",
		"SLL",
		"OR",
		"JAL",
		"BGEZAL",	// called BAL, value 1 which is all of REGIMM.
					// This is wrong.  FIXME
		"JAL",
		"JR",
		"BNE",
		"BEQ"
	};
	
	static Set<String> opcodeSet = new HashSet<String>(Arrays.asList(opcodes));

	public Set<String> getOpcodesImplemented() {
		return opcodeSet;
	}
	public T[] function(Register<T> reg, T[] inst, T[] pc, T[][] hiLo, T terminationBit, int[] masks) throws Exception {
		if(cpu == null) return null;
		return cpu.function(reg,  inst,  pc, terminationBit, masks);
	}
}
