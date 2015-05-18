/**
 * This class reads a MIPS binary and builds the sets of
 * instructions that might be executed at each program step.
 * 
 * @author Allen McIntosh
 */
package com.appcomsci.mips.memory;

import static com.appcomsci.mips.memory.MipsInstructionSet.OP_BEQ;
import static com.appcomsci.mips.memory.MipsInstructionSet.OP_BGEZ;
import static com.appcomsci.mips.memory.MipsInstructionSet.OP_BGEZAL;
import static com.appcomsci.mips.memory.MipsInstructionSet.OP_BGTZ;
import static com.appcomsci.mips.memory.MipsInstructionSet.OP_BLEZ;
import static com.appcomsci.mips.memory.MipsInstructionSet.OP_BLTZ;
import static com.appcomsci.mips.memory.MipsInstructionSet.OP_BLTZAL;
import static com.appcomsci.mips.memory.MipsInstructionSet.OP_BNE;
import static com.appcomsci.mips.memory.MipsInstructionSet.OP_FUNCT;
import static com.appcomsci.mips.memory.MipsInstructionSet.OP_J;
import static com.appcomsci.mips.memory.MipsInstructionSet.OP_JAL;
import static com.appcomsci.mips.memory.MipsInstructionSet.OP_JALR;
import static com.appcomsci.mips.memory.MipsInstructionSet.OP_JR;
import static com.appcomsci.mips.memory.MipsInstructionSet.OP_REGIMM;
import static com.appcomsci.mips.memory.MipsInstructionSet.RETURN_REG;
import static com.appcomsci.mips.memory.MipsInstructionSet.DEFAULT_SPIN_ADDRESS;
import static com.appcomsci.mips.memory.MipsInstructionSet.NOP;
import static com.appcomsci.mips.memory.MipsInstructionSet.getFunct;
import static com.appcomsci.mips.memory.MipsInstructionSet.getInstrIndex;
import static com.appcomsci.mips.memory.MipsInstructionSet.getOffset;
import static com.appcomsci.mips.memory.MipsInstructionSet.getOp;
import static com.appcomsci.mips.memory.MipsInstructionSet.getRegImmCode;
import static com.appcomsci.mips.memory.MipsInstructionSet.getSrcReg;
import static com.appcomsci.mips.memory.MipsInstructionSet.getSrc2Reg;
import jargs.gnu.CmdLineParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.appcomsci.mips.binary.DataSegment;
import com.appcomsci.mips.binary.Reader;
import com.appcomsci.mips.binary.SymbolTableEntry;
import com.appcomsci.sfe.common.Configuration;

import static com.appcomsci.mips.cpu.Utils.makeInstructionSet;
import static com.appcomsci.mips.cpu.Utils.toStringSet;
import static com.appcomsci.mips.cpu.Utils.consistentHashString;

/**
 * Create sets of memory locations that can be executed at each
 * program step
 * 
 * @author Allen McIntosh
 *
 */
public class MemSetBuilder<T> extends MipsProgram {
	
	/**
	 * Contructor: Pick up configuration from a user-supplied config object
	 * @param config The configuration object used to initialize this builder
	 */
	public MemSetBuilder(Configuration config, String binaryFileName) {
		super(config, binaryFileName);
	}
	
	/**
	 * Constructor: Pick up configuration from command line arguments, with
	 * a properties file as backup.
	 * @param args
	 * @throws IOException
	 */
	public MemSetBuilder(String args[]) throws IOException, CmdLineParser.OptionException {
		super(args);
	}
	
	protected void printUsage() {
		printUsageStatic();
	}
	
	private static void printUsageStatic() {
		System.err.println("Usage!");
	}
	
	/**
	 * Generate a list of instructions that might be executed in any program step.
	 * The program is supplied by the caller on the command line, or from a config object
	 * passed to the constructor.
	 * 
	 * @return A list of MemorySet objects.  The first element of this list [accessible
	 * via get(0)] is the address of the first instruction at the entry point.  Subsequent
	 * elements are the addresses of subsequent instructions in the program.  They may be accessed by
	 * following the NextMemorySet properties.
	 * 
	 * The analysis attempts to trace possible execution paths.  Each time a conditional branch
	 * is encountered, a new thread representing a possible execution path is started.  If a
	 * thread successfully reaches the end of the routine being analyzed, it is assumed to return to
	 * the address MipsInstructionSet.DEFAULT_SPIN_ADDRESS (currently 0) where it is assumed to spin
	 * forever.
	 * 
	 * Eventually, one of the following happens:
	 * 1) The analysis terminates.  (This will only happen in simple programs with no
	 * loops).  The final MemorySet object will contain only DEFAULT_SPIN_ADDRESS and will be
	 * self-referential.
	 * 2) The analysis hits the maximum number of program steps.  The NextMemorySet pointer
	 * of the final object in the list will be null.
	 * 3) The analysis hits a JR or JALR instruction where the target cannot be determined.  The
	 * final MemorySet object in the list will be a MemorySet object containing all possible
	 * addresses, and the NextMemorySet pointer will be self-referential.
	 * 4) The analysis hits a state that it has already encountered.  The NextMemorySet pointer
	 * of the final state will point back to this previous state.
	 * 
	 * 
	 * @throws FileNotFoundException If the binary doesn't exist
	 * @throws IllegalArgumentException ??
	 * @throws IOException If the binary can't be read.
	 * @throws MemSetBuilderException For some impossible conditions in the set builder
	 */
	public List<MemorySet<T>> build()
			throws FileNotFoundException, IllegalArgumentException, IOException, MemSetBuilderException {
		Reader rdr = new Reader(new File(getMipsBinaryPath()), getConfiguration());
		SymbolTableEntry ent = rdr.getSymbolTableEntry(getEntryPoint());	
		DataSegment inst = rdr.getInstructions(getFunctionLoadList());
		
		List<MemorySet<T>> sets = build(inst, ent);
		
//		for(MemorySet s:sets) {
//			s.getAddressMap(inst);
//		}
		return sets;
	}
	
	/**
	 * Run the builder with user-supplied instructions and entry point.  The configuration
	 * is only consulted for (a) the max number of program steps and (b) to determine if
	 * delay slots are to be honored.
	 * @param instructions The program instructions
	 * @param entryPoint The program entry point
	 * @return A list of MemorySet objects.  See the description for build().
	 * @throws MemSetBuilderException For some impossible conditions in the set builder
	 */
	public List<MemorySet<T>> build(DataSegment instructions, SymbolTableEntry entryPoint) throws  MemSetBuilderException{
		
		// This is the array to be returned.
		List<MemorySet<T>> execSets = new ArrayList<MemorySet<T>>();
		
		// The previous set, for forward chaining
		MemorySet<T> prevSet = null;
		
		// A hash map, for detecting recurring states.
		// Each value in the hash map is a bucket of MemorySets
		Map<MemorySet<T>, ArrayList<MemorySet<T>>> memSetMap = new HashMap<MemorySet<T>, ArrayList<MemorySet<T>>>();
		
	    int maxSteps = getMaxProgramSteps();
		
		// The list of currently "executing" threads
		
		LinkedList<ThreadState> threads = new LinkedList<ThreadState>();
		
		// Initially, one thread starting at the entry address
		ThreadState initial = new ThreadState(entryPoint.getAddress());
		threads.add(initial);
		
		executionLoop: for(int executionStep = 0; executionStep < maxSteps; executionStep++) {
			if(threads.size() == 0) {
				throw new MemSetBuilderException("No execution threads after " + executionStep + " iterations");
			}
			
			// Create a memory set that contains the current step number, and
			// the address of all currently running threads.
			
			MemorySet<T> currentSet = new MemorySet<T>(executionStep, threads, instructions);
			
			// Have we seen this set before?
			
			ArrayList<MemorySet<T>> bucket = memSetMap.get(currentSet);
			if(bucket == null) { // Definitely not
				bucket = new ArrayList<MemorySet<T>>();
				memSetMap.put(currentSet, bucket);
			} else {
				// Maybe.  Does bucket contain a set equal to currentSet?
				for(MemorySet<T> s:bucket) {
					if(currentSet.equals(s)) {
						// Found an equivalent set.
						if(prevSet == null) {
							throw new MemSetBuilderException("Found an equivalent set with no previous set");
						} else {
							// So stop tracing here by pointing to the previous set.
							prevSet.setNextMemorySet(s);
						}
						break executionLoop;
					}
				}
			}
			bucket.add(currentSet);
			execSets.add(executionStep, currentSet);
			if(prevSet != null)
				prevSet.setNextMemorySet(currentSet);
			
			// Quit if the set of possible addresses is the universe.  (Should probably make
			// this half the universe or something and make the next set equal to the universe)
			if(currentSet.size() >= instructions.getDataLength()) {
				currentSet.setNextMemorySet(currentSet);
				break;
			}
			// Quit if everything is spinning
			if(currentSet.isAllSpinning()) {
				currentSet.setNextMemorySet(currentSet);
				break;
			}
			
			prevSet = currentSet;
			
			LinkedList<ThreadState> newThreads = new LinkedList<ThreadState>();
			ListIterator<ThreadState> thI = threads.listIterator(0);
			
			// Advance each thread one instruction.
			// Invariant:  At bottom of loop body the thread is ready to execute the next
			// instruction
			
//System.err.println("Step: " + executionStep);
			while(thI.hasNext()) {
				ThreadState th = thI.next();
//System.err.println("  Thread " + th.getId() + " A: " + Long.toHexString(th.getCurrentAddress()) + " D: " +
//Long.toHexString(th.getCurrentAddress() == DEFAULT_SPIN_ADDRESS ? 0 : instructions.getDatum(th.getCurrentAddress())));

				if(th.isDelayed()) {
					// If a delay slot, just pop it off and continue
					th.advance();
				} else {
					// Get the current address, and the instr if it's not the spin.
					long addr = th.getCurrentAddress();
					long instr = NOP;
					if(addr != DEFAULT_SPIN_ADDRESS)
						instr = instructions.getDatum(th.getCurrentAddress());
					
					// Now we get down to the tedious work of simulating individual
					// instructions
					
					switch(getOp(instr)) {
					case OP_FUNCT:
						switch(getFunct(instr)) {
							// Flying leap, or maybe return
						case OP_JR:
							if(getSrcReg(instr) == RETURN_REG) {
								// Assume this is a return
								if(isHonorDelaySlots()) {
									th.doDelay();
								} else {
									th.popAddress();
								}
							} else {
								// Flying leap
								currentSet = new MemorySet<T>(executionStep+1, instructions);
								execSets.add(executionStep+1, currentSet);
								prevSet.setNextMemorySet(currentSet);
								currentSet.setNextMemorySet(currentSet);
								break executionLoop;
							}
							break;
							// Flying leap with link
						case OP_JALR:
							/*
							currentSet = new MemorySet<T>(executionStep+1, instructions);
							execSets.add(executionStep+1, currentSet);
							prevSet.setNextMemorySet(currentSet);
							currentSet.setNextMemorySet(currentSet);
							break executionLoop;
							*/
							// IGNORE JALR FOR NOW
							th.advance();
							break;
						default:
							th.advance();
							break;
						}
						break;
					case OP_REGIMM:
						switch(getRegImmCode(instr)) {
						// Conditional branches
						case OP_BLTZ:
						case OP_BGEZ:
							{
								long targetAddress = th.getCurrentAddress() + (getOffset(instr)<<2) + 4;
								ThreadState newThread = new ThreadState(th);
								newThreads.add(newThread);
								if(isHonorDelaySlots()) {
									newThread.doDelay(targetAddress);
								} else {
									// Replace current address with branch target
									newThread.popAddress();
									newThread.pushAddress(targetAddress);
								}
								th.advance();
							}
							break;
						// Branches with link
						case OP_BGEZAL:
							{
								// Look for unconditional call
								if(getSrcReg(instr) == 0) {
									long targetAddress = th.getCurrentAddress() + (getOffset(instr)<<2) + 4;
									th.advance();
									if(isHonorDelaySlots()) {
										th.doCall(targetAddress);
									} else {
										// Advance past delay slot, ignoring it
										th.advance();
										// Push branch target
										th.pushAddress(targetAddress);
									}
									break;
								}
							}
							// Fall through
						case OP_BLTZAL:
							{
								long targetAddress = th.getCurrentAddress() + (getOffset(instr)<<2) + 4;
								ThreadState newThread = new ThreadState(th);
								newThreads.add(newThread);
								if(isHonorDelaySlots()) {
									newThread.doCall(targetAddress);
								} else {
									// Push branch target
									newThread.pushAddress(targetAddress);
								}
								th.advance();
							}
							break;
						default:
							th.advance();
							break;
						}
						break;
					case OP_J:
						{
							// Jump away, no link.  The target address can be computed.
							long targetAddress = th.getCurrentAddress()+4;
							targetAddress &= (long)(~MipsInstructionSet.INSTR_INDEX_MASK)<<2;
							targetAddress |= getInstrIndex(instr)<<2;
							if(isHonorDelaySlots()) {
								th.doDelay(targetAddress);
							} else {
								// Replace current address with branch target
								th.popAddress();
								th.pushAddress(targetAddress);
							}
						}
						break;
					case OP_JAL:
						{
							// Jump with link.  The target address can be computed.
							long targetAddress = th.getCurrentAddress()+4;
							targetAddress &= (long)(~MipsInstructionSet.INSTR_INDEX_MASK)<<2;
							targetAddress |= getInstrIndex(instr)<<2;
							th.advance();
							if(isHonorDelaySlots()) {
								th.doCall(targetAddress);
							} else {
								// Advance past delay slot, ignoring it
								th.advance();
								// Push branch target
								th.pushAddress(targetAddress);
							}
						}
						break;
						// Conditional branches.
					case OP_BEQ:
						{
							if(getSrcReg(instr) == getSrc2Reg(instr)) {
								long targetAddress = th.getCurrentAddress() + (getOffset(instr)<<2) + 4;
								// An unconditional branch, since the two registers are equal.
								if(isHonorDelaySlots()) {
									th.doDelay();
								} else {
									th.popAddress();
									th.pushAddress(targetAddress);
								}
								break;
							}
						}
						/* FALL THROUGH */
					case OP_BNE:
					case OP_BLEZ:
					case OP_BGTZ:
						{
							long targetAddress = th.getCurrentAddress() + (getOffset(instr)<<2) + 4;
							ThreadState newThread = new ThreadState(th);
							newThreads.add(newThread);
							if(isHonorDelaySlots()) {
								newThread.doDelay(targetAddress);
							} else {
								newThread.popAddress();
								newThread.pushAddress(targetAddress);
							}
							th.advance();
						}
						break;
					default:
						th.advance();
					}
				}
			}
			if(newThreads.size() > 0)
				threads.addAll(newThreads);
			
			// Prune duplicate threads.  Note that we can't do this by maintaining
			// "threads" as a Set:  Part way through the above Thread loop the
			// threads are in an inconsistent state.  A pair might appear to be
			// executing at the same address when in fact one has been advanced and the
			// other has not.
			Map<Long, ArrayList<ThreadState>> pruneMap = new HashMap<Long, ArrayList<ThreadState>>();
			thI = threads.listIterator(0);
			pruneLoop: while(thI.hasNext()) {
				ThreadState th = thI.next();
				// Catch delay slots on the next instruction cycle
				if(th.isDelayed())
					continue;
				
				// Is there another thread at this address, and does
				// it have the same call state?
				ArrayList<ThreadState> pruneBucket = pruneMap.get(th.getCurrentAddress());
				if(pruneBucket == null) {
					// Empty bucket.  Create one.
					pruneBucket = new ArrayList<ThreadState>();
					pruneMap.put(th.getCurrentAddress(), pruneBucket);
				} else {
					// Search bucket
					for(ThreadState tMap:pruneBucket) {
						if(th.equals(tMap)) {
							// Same address, same state.
							// Remove thread from linked list and do not
							// add to hash bucket
							thI.remove();
							continue pruneLoop;
						}
					}
				}
				pruneBucket.add(th);
			}
			
		}
		
		// Record which sets use memory
		for(MemorySet<T> s:execSets) {
			s.setUsesMemory(instructions);
		}
		return execSets;
		
	}
	public static void main(String args[]) throws IOException, MemSetBuilderException {
		MemSetBuilder<Boolean> b = null;
		try {
			b = new MemSetBuilder<Boolean>(args);
		} catch(CmdLineParser.OptionException e) {
			System.err.println(e.getMessage());
			printUsageStatic();
			System.exit(2);
		}
		List<MemorySet<Boolean>> sets = b.build();
		for(MemorySet<Boolean> m:sets) {
			System.err.println(m.toString());
			System.err.println(consistentHashString(toStringSet(makeInstructionSet(m))));
		}
	}
}
