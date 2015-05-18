/**
 * 
 */
package com.appcomsci.mips.cpu;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import com.appcomsci.mips.memory.MemorySet;
import com.appcomsci.mips.memory.MipsInstructionSet;

import flexsc.CompEnv;
import flexsc.CpuFcn;

/**
 * @author Allen McIntosh
 *
 */
public class Utils {
	/**
	 * Build the set of instructions possibly executed by a MemorySet
	 * @param ms The memory set
	 * @return The set of instructions possibly executed by the MemorySet
	 */
	public static <T> Set<MipsInstructionSet.Operation>makeInstructionSet(MemorySet<T>ms) {
		HashSet<MipsInstructionSet.Operation> rslt = new HashSet<MipsInstructionSet.Operation>();
		for(long instr:ms.getInstructions()) {
			rslt.add(MipsInstructionSet.Operation.valueOf(instr));
		}
		return rslt;
	}
	
	public static <T> Set<Long>makeInstructionLongSet(MemorySet<T>ms) {
		HashSet<Long> rslt = new HashSet<Long>();
		for(long instr:ms.getInstructions()) {
			rslt.add(instr);
		}
		return rslt;
	}
	
	public static Set<MipsInstructionSet.Operation> makeOperationSet(Set<Long> ins) {
		HashSet<MipsInstructionSet.Operation> rslt = new HashSet<MipsInstructionSet.Operation>();
		for(long instr:ins) {
			rslt.add(MipsInstructionSet.Operation.valueOf(instr));
		}
		return rslt;
	}
	
	/**
	 * Convert a set of Operations to a set of their names.  Explicitly return a TreeSet to
	 * enforce the contract that the set of names will always be in alphabetical order.
	 * (This guarantee is required by consistentHash() below.
	 * @param operations The set of operations
	 * @return The set of Strings
	 */
	public static  TreeSet<String>toStringSet(Set<MipsInstructionSet.Operation>operations) {
		TreeSet<String>opNames = new TreeSet<String>();
		for(MipsInstructionSet.Operation op:operations) {
			opNames.add(op.toString());
		}
		return opNames;
	}
	/**
	 * Build a consistent hash for a set of instructions.  The default hash algorithm
	 * isn't consistent from one invocation to another when applied to sets of Operations.
	 * This relies on getting the instructions in a consistent order, which also isn't
	 * guaranteed in the original operation set.
	 * @param instructions
	 * @return The hash code.
	 */
	public static int consistentHash(Set<String>instructions) {
		return consistentHashString(instructions).hashCode();
	}
	
	/**
	 * This code does the heavy lifting for consistentHash() by building a string
	 * of instructions in alphabetical order.
	 * @param instructions
	 * @return
	 */
	public static String consistentHashString(Set<String>instructions) {
		TreeSet<String>opNames = new TreeSet<String>(instructions);
		StringBuilder sb = new StringBuilder();
		for(String name:opNames) {
			sb.append(" ");
			sb.append(name);
		}
		return sb.toString();
	}

	public static <T> CpuFcn<T> findCpu(MemorySet<T> ms, CompEnv<T> env, String packageName, String classNameRoot, boolean check) {
		Set<MipsInstructionSet.Operation>instructions = Utils.makeInstructionSet(ms);
		return findCpu(instructions, env, packageName, classNameRoot, check);
	}
	
	public static <T> CpuFcn<T> findCpu(Set<MipsInstructionSet.Operation>instructions, CompEnv<T> env, String packageName, String classNameRoot, boolean check) {
		// This is messy.  First, figure out a class name to search for.
		// This should have the form foo.bar.Cpu_HHHHImpl, where foo.bar
		// is the same package name as possessed by CPU, and HHHH is a hash
		// code made up from the list of operations contained in this MemorySet.
		CpuFcn<T> cpu = null;
		TreeSet<String>opNames = Utils.toStringSet(instructions);
		int hash = Utils.consistentHash(opNames);
		String hashPart = "_" + String.format("%08x", hash);
		String className = classNameRoot + hashPart;
		String wrapperClassName = className + "Impl";
		String fullWrapperClassName = packageName + "." + wrapperClassName;
		// Look up the class
		try {
			Class<?> c = Class.forName(fullWrapperClassName);

			// Got it.  Look for a suitable constructor.

			Constructor<?>[] ctors = c.getDeclaredConstructors();
			Constructor<?> ctor = null;
			int numParameters = (env == null) ? 0 : 1;
			for (int i = 0; i < ctors.length; i++) {
			    ctor = ctors[i];
			    if (ctor.getGenericParameterTypes().length == numParameters)
				break;
			}
			if(ctor == null) {
				System.err.println("Class " + fullWrapperClassName + " has no useable constructor");
				return null;
			}
			// Invoke the constructor
			if(env == null)
				cpu = (CpuFcn<T>) ctor.newInstance();
			else
				cpu = (CpuFcn<T>) ctor.newInstance(env);
		} catch(ClassNotFoundException e) {
			return null;
		} catch (InstantiationException e) {
			System.err.println("Could not instantiate " + fullWrapperClassName + ":" + e);
			return null;
		} catch (IllegalAccessException e) {
			System.err.println("Could not access constructor for " + fullWrapperClassName + ":" + e);
			return null;
		} catch (IllegalArgumentException e) {
			System.err.println("Illegal argument for constructor for " + fullWrapperClassName + ":" + e);
			return null;
		} catch (InvocationTargetException e) {
			System.err.println("Invocation target exception for " + fullWrapperClassName + ":" + e);
			return null;
		}
		
		// If asked, check that the object we just grabbed fits our needs.
		if(check) {
			Set<String> implemented = cpu.getOpcodesImplemented();
			if(!opNames.equals(implemented)) {
				System.err.println("Help! Hash code jackpot for " + hash);
				System.err.println("Class claims to implement " + Utils.consistentHashString(implemented));
				System.err.println("Trying to generate CPU for " + Utils.consistentHashString(opNames));
				return null;
			}
		}
		return cpu;
	}
}
