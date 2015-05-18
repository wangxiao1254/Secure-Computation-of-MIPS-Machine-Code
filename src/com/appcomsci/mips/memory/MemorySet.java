/**
 * 
 */
package com.appcomsci.mips.memory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import mips.OramBank;

import com.appcomsci.mips.binary.DataSegment;
import com.appcomsci.mips.cpu.Utils;

import compiledlib.dov.CPU;
import flexsc.CompEnv;
import flexsc.CpuFcn;

/**
 * A set of memory addresses that might be executed at a particular
 * execution step.
 * @author Allen McIntosh
 *
 */
public class MemorySet<T> {
	/**
	 * The addresses
	 */
	private TreeSet<Long> addresses;
	/**
	 * The number of the execution step
	 */
	private final int executionStep;
	/**
	 * The next execution step, or null if we ran off the end of the world
	 */
	private MemorySet<T> nextMemorySet;
	/** Does this use memory?
	 */
	private boolean usesMemory;
	public int readWrite;
	/**
	 ** Oram Bank for storing the instructions securely
	 */
	private OramBank<T> oramBank = null;
	
	/** The data segment containing the memory addresses.
	 * This is not externally visible.
	 */
	private DataSegment dataSegment;
	
	private CpuFcn<T> cpu;
	/**
	 * Build a memory set consisting of the current addresses of a list of threads.
	 * @param executionStep The number of the execution step
	 * @param threads The list of threads.
	 */
	public MemorySet(final int executionStep, List<ThreadState>threads, DataSegment dataSegment) {
		this.dataSegment = dataSegment;
		this.executionStep = executionStep;
		addresses = new TreeSet<Long>();
		for(ThreadState t:threads) {
			addresses.add(t.getCurrentAddress());
		}
	}
	/**
	 * Initialize memory set to be every address in the data segment
	 * @param executionStep The number of the execution step
	 * @param dataSegment The data segment
	 */
	public MemorySet(final int executionStep, DataSegment dataSegment) {
		this.dataSegment = dataSegment;
		this.executionStep = executionStep;
		addresses = new TreeSet<Long>();
		for(int i = 0; i < dataSegment.getDataLength(); i++)
			addresses.add(dataSegment.getStartAddress() + 4*i);
	}
	/**
	 * Get the set of addresses associated with this step
	 * @return The set of addresses.
	 */
	public TreeSet<Long> getAddresses() {
		return addresses;
	}
	
	public List<Long> getInstructions() {
		ArrayList<Long> rslt = new ArrayList<Long>();
		for(Long addr:getAddresses()) {
			rslt.add(dataSegment.getDatum(addr));
		}
		return rslt;
	}
	
	/**
	 * Get a map from addresses in this set to the data at those addresses
	 * @param dseg A DataSegment containing the data
	 * @return The map.
	 */
	public TreeMap<Long,boolean[]> getAddressMap() {
		TreeMap<Long, boolean[]> rslt = new TreeMap<Long, boolean[]>();
		for(Long addr:addresses) {
			rslt.put(addr, dataSegment.getDatumAsBoolean(addr));
		}
		return rslt;
	}
	
	/**
	 * Determine if any instruction at this step uses memory.
	 * Cache the value for later use.
	 * @param dseg The program instructions.
	 */
	public void setUsesMemory(DataSegment dseg) {
		readWrite = 0;
		usesMemory = false;
		for(Long addr:addresses) {
			long instr = dseg.getDatum(addr);
			MipsInstructionSet.Operation op = MipsInstructionSet.Operation.valueOf(instr);
			switch(op.getType()) {
			case MW:
				readWrite = readWrite | (1<<1);
				usesMemory = true;
				break;
			case MR:
				readWrite = readWrite | (1);
				usesMemory = true;
				break;
			default:
				break;
			}
		}
//		usesMemory = false;
	}
	
	/**
	 * Do the instructions in this set reference memory?
	 * Note:  The value of this method is cached.  It must be set initially
	 * by calling setUsesMemory().
	 * @return True if memory is read or written, false otherwise.
	 */
	public boolean isUsesMemory() {
		return usesMemory;
	}
	/**
	 * Get the number of possible addresses in this execution step
	 * @return The number of possible addresses in this execution step
	 */
	public int size() {
		return addresses.size();
	}
	
	/**
	 * Is this memory set spinning at the spin address and nothing more?
	 * @return True if all threads are spinning, false otherwise.
	 */
	public boolean isAllSpinning() {
		if(addresses.size() != 1) return false;
		return addresses.first() == MipsInstructionSet.getSpinAddress();
	}
	
	public String toString() {
		return toString(false);
	}
	
	public String toString(boolean includeOpcode) {
		StringBuilder sb = new StringBuilder("Memory Set " + executionStep + ":");
		if(nextMemorySet != null) {
			sb.append(" Next: " + nextMemorySet.getExecutionStep());
		}
		sb.append(" [" + addresses.size() + "] ");
		for(Long l:addresses) {
			sb.append(" " + Long.toHexString(l));
			if(includeOpcode) {
				sb.append(" ");
				MipsInstructionSet.Operation op = MipsInstructionSet.Operation.valueOf(dataSegment.getDatum(l));
				if(op == null) {
					sb.append(String.format("0x%08x", l));
				} else {
					sb.append(op.toString());
				}
			}
		}
		return sb.toString();
	}
	
	public boolean equals(Object o) {
		if(o == null)
			return false;
		if(!(o instanceof MemorySet<?>))
			return false;
		MemorySet<T> that = (MemorySet<T>) o;
		return this.getAddresses().equals(that.getAddresses());
	}
	
	/**
	 * Compute a hash code for this MemorySet, using only the addresses
	 */
	public int hashCode() {
		// Use dumb GCC LCRNG to smash bits
		int rslt = 1103515245+12345;
		for(Long a:addresses) {
			rslt = rslt ^ (int)(a*1103515245+12345);
		}
		return rslt;
	}
	/**
	 * @return the executionStep
	 */
	public int getExecutionStep() {
		return executionStep;
	}
	/**
	 * @return the nextMemorySet
	 */
	public MemorySet<T> getNextMemorySet() {
		return nextMemorySet;
	}
	/**
	 * @param nextMemorySet the nextMemorySet to set
	 */
	public void setNextMemorySet(MemorySet<T> nextMemorySet) {
		this.nextMemorySet = nextMemorySet;
	}
	/**
	 * @return the oramBank
	 */
	public OramBank<T> getOramBank() {
		return oramBank;
	}
	/**
	 * @param oramBank the oramBank to set
	 */
	public void setOramBank(OramBank<T> oramBank) {
		this.oramBank = oramBank;
	}
	
	/**
	 * @return the cpu
	 */
	public CpuFcn<T> getCpu() {
		return cpu;
	}
	
	public CpuFcn<T> findCpu(CompEnv<T> env, String packageName, String classNameRoot, boolean check) {
		CpuFcn<T> cpu = Utils.findCpu(this, env, packageName, classNameRoot, check);
		if(cpu != null)
			this.cpu = cpu;
		return cpu;
	}

}
