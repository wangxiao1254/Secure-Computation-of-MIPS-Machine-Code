package mips;

import static com.appcomsci.mips.cpu.Utils.consistentHash;
import static com.appcomsci.mips.cpu.Utils.consistentHashString;
import static com.appcomsci.mips.cpu.Utils.makeInstructionSet;
import static com.appcomsci.mips.cpu.Utils.toStringSet;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
//import gc.Boolean;




import oram.Memory;
import oram.Register;
import oram.SecureMap;
import util.Utils;
import circuits.arithmetic.IntegerLib;

import com.appcomsci.mips.binary.DataSegment;
import com.appcomsci.mips.binary.Reader;
import com.appcomsci.mips.binary.SymbolTableEntry;
import com.appcomsci.mips.memory.MemSetBuilder;
import com.appcomsci.mips.memory.MemorySet;

import compiledlib.dov.CPU;
import compiledlib.dov.CpuImpl;
import compiledlib.dov.MEM;
import flexsc.CompEnv;
import flexsc.CpuFcn;
// NEW import flexsc.CpuFcn;
import flexsc.Mode;
import flexsc.Party;
import gc.BadLabelException;
import gc.GCSignal;

public class MipsEmulatorImpl<ET> implements MipsEmulator {
	static final boolean muteLoadInstructions = true;
	static final int WORD_SIZE = 32;
	static final int NUMBER_OF_STEPS = 1;
	static final int REGISTER_SIZE = 32;

	/*
	 * XXInputIsRef indicates whether that user's inputs will fit into the two registers allocated to them.  
	 * I suppose it is possible they have 3 and 1 input values: that case isn't currently handled.
	 * If a user's value does not fit into the register space, the address is placed there (loadInputToRegisters).
	 * If the value does fit, and they only have one value, the second input value must be < 0, or it will 
	 * also be loaded.  
	 */
	static int stackFrameSize; 
	static int stackSize;
	static int aliceFuncAddress;

	// Should we blither about missing CPUs?
	static final boolean blither = false;

	protected LocalConfiguration config;

	private MipsEmulatorImpl(LocalConfiguration config) throws Exception {
		this.config = config;
		stackFrameSize = config.stackFrameSize / 4;
		stackSize = stackFrameSize + config.aliceInputSize + config.bobInputSize + 8;
	}

	private class MipsParty<T> {
		List<MemorySet<T>> sets;
		DataSegment instData; 
		DataSegment memData;
		int pcOffset; 
		int dataOffset; 
		IntegerLib<T> lib;
		public MipsParty(List<MemorySet<T>> sets,	DataSegment instData, DataSegment memData, int pcOffset,int dataOffset ){
			this.sets = sets;
			this.instData = instData;
			this.memData = memData;
			this.pcOffset = pcOffset;
			this.dataOffset = dataOffset;
		}
		public Register<T> reg;
		public void mainloop(CompEnv<T> env) throws Exception {
			lib = new IntegerLib<T>(env);
			CpuFcn<T> defaultCpu = new CpuImpl<T>(env);
			MEM<T> mem = new MEM<T>(env);
			reg = loadInputsToRegister(env, this.dataOffset);
			
			loadCpus(sets, env);

			SecureMap<T> singleInstructionBank = null;

			if (!config.isMultipleBanks()){
				singleInstructionBank = loadInstructionsSingleBank(env, instData);				
			}

			double t1 = System.nanoTime();
			loadInstructionsMultiBanks(env, singleInstructionBank, sets);
			Memory<T> memBank = getMemory(env, memData);
			System.out.println( (System.nanoTime()- t1)/1000000000.0);
			
			T[] pc = lib.toSignals(pcOffset, WORD_SIZE);
			T[] newInst = lib.toSignals(0, WORD_SIZE);
			int count = 0;
			int numBanksWithMem=0;
			//if (!config.isMultipleBanks())
			//EmulatorUtils.printOramBank(singleInstructionBank, lib, 60);
			long startTime = System.nanoTime();
			long fetchTime = 0, fetchAnd =0;
			long fetchTimeStamp = 0, fetchAndStamp = 0;
			long loadStoreTime = 0, loadStoreAnd = 0;
			long loadStoreTimeStamp = 0, loadStoreAndStamp=0;
			long cpuTime = 0, cpuAnd = 0;
			long cpuTimeStamp = 0, cpuAndStamp = 0;
			MemorySet<T> currentSet = sets.get(0);
			SecureMap<T> currentBank;
			dataOffset -= (stackSize*4);
			T terminationBit = lib.SIGNAL_ONE;
			int[] masks = new int[2];
			T[][] hiLo = env.newTArray(2,0);
			hiLo[0] = lib.zeros(32);
			hiLo[1] = lib.zeros(32);
			while (true) {
				currentBank = currentSet.getOramBank().getMap();
				//				EmulatorUtils.print("count: " + count + "\nexecution step: " + currentSet.getExecutionStep(), lib, false);

				count++;
				//if (config.isMultipleBanks())
				//currentSet.getOramBank().getMap().print();
				if (config.isMultipleBanks())
					pcOffset = (int) currentSet.getOramBank().getMinAddress();


				fetchTimeStamp = System.nanoTime();
				fetchAndStamp = env.numOfAnds;
				newInst = mem.getInst(currentBank, pc, pcOffset);
				fetchTime += System.nanoTime() - fetchTimeStamp;
				fetchAnd += env.numOfAnds - fetchAndStamp;

				terminationBit = lib.and(terminationBit, testTerminate(reg, newInst, lib));
				if(count%10 == 0) {
					T[] res = lib.getEnv().newTArray(1);
					res[0] = terminationBit ;			
					boolean ret = lib.declassifyToBoth(res)[0];
					if(ret == false)break;
				}


				CpuFcn<T> cpu = currentSet.getCpu();
				cpuTimeStamp = System.nanoTime();
				cpuAndStamp = env.numOfAnds;
				if(cpu == null  || !config.isMultipleBanks()) {
					pc = defaultCpu.function(reg, newInst, pc, null, terminationBit,masks);
					if(config.isMultipleBanks())
						System.out.println("Multibank mode but lost CPU!");
				}
				else
					pc = cpu.function(reg, newInst, pc, hiLo, terminationBit,masks);
				cpuTime += System.nanoTime() - cpuTimeStamp;
				cpuAnd += env.numOfAnds - cpuAndStamp;


				if (currentSet.isUsesMemory()) {
					numBanksWithMem++;
					loadStoreTimeStamp = System.nanoTime();
					loadStoreAndStamp = env.numOfAnds;
					mem.func(reg, memBank, newInst, dataOffset, terminationBit, currentSet.readWrite, masks[0],masks[1]);
					loadStoreTime += System.nanoTime() - loadStoreTimeStamp;
					loadStoreAnd += env.numOfAnds - loadStoreAndStamp;
				}

				//EmulatorUtils.printRegisters(reg, lib);
				//								  EmulatorUtils.printBooleanArray("PC", pc, lib);
				//EmulatorUtils.print(pcOffset+"", lib);
				//EmulatorUtils.print(currentSet.getOramBank().getMinAddress()+"", lib);

				currentSet = currentSet.getNextMemorySet();	
			}

			float runTime =  ((float)(System.nanoTime() - startTime))/ 1000000000;
			float cpuTimeFl = ((float)cpuTime) / 1000000000;
			float fetchTimeFl = ((float)fetchTime) / 1000000000;
			float loadStoreTimeFl = ((float)loadStoreTime) / 1000000000;
			int res = Utils.toInt(env.outputToAlice(reg.read(2)));
			if (env.getParty() == Party.Alice) {
				System.out.println(env.getParty());

				System.out.println("Count:"  + count);
				System.out.println("Run time: " + runTime);
				System.out.println("Average time / instruction: " + runTime / count +"\n");
				
				
				System.out.println("Time in CPU: " + cpuTimeFl);
				System.out.println("Average CPU #ANDs: " + cpuAnd / count);
				System.out.println("Average CPU time: " + cpuTimeFl / count+"\n");
				
				System.out.println("Time in instruction fetch: " + fetchTimeFl);
				System.out.println("Average fetch #ANDS: " + fetchAnd / count);
				System.out.println("Average fetch time: " + fetchTimeFl / count);
				System.out.println("Size of Instruction bank:"+ instData.getDataLength()+"\n");
				
				System.out.println("Time in loadStore: " + loadStoreTimeFl);
				System.out.println("Average loadStore #ANDS: " + loadStoreAnd/count);
				System.out.println("Average loadStore time: " + loadStoreTimeFl / count);
				System.out.println("Size of Memory:"+ memBank.size);
				System.out.println("numBanks with memory: " + numBanksWithMem+"\n");
				System.out.println("Result:"+res);
			}
			
		}

		private void loadCpus(List<MemorySet<T>> sets, CompEnv<T>env) {
			if(!config.isMultipleBanks()) {
				System.out.println("Not loading CPUs for single bank execution");
				return;
			}
			//System.out.println("Entering loadCpus");
			// Uses arcane knowledge. FIXME
			String packageName = CPU.class.getPackage().getName();
			String classNameRoot = "Cpu";
			for(MemorySet<T>s:sets) {
				CpuFcn<T> cpu = s.findCpu(env, packageName, classNameRoot, true);
				if(cpu == null && blither) {
					System.err.println("Could not find cpu for: [" +
							consistentHash(toStringSet(makeInstructionSet(s))) +
							"] " + consistentHashString(toStringSet(makeInstructionSet(s)))
							);
				}
			}
			//System.out.println("Exiting loadCpus");
		}

		private T testTerminate(Register<T> reg, T[] ins, IntegerLib<T> lib) throws BadLabelException {
			//			System.out.println(lib.getEnv().getParty().toString()+System.currentTimeMillis()%100000/1000.0);

			// Look for branch to here.  There are several ways to code this.
			// Gcc and cousins use BEQ $0,$0,-1
			// 0x1000ffff = 0b000100 00000 00000 1111111111111111
			T eq = lib.eq(ins, lib.toSignals(0x1000ffff, 32));
			// Look for jr $31 where $31 contains zero
			// 0x03e00008 = 0b000000 11111 0000000000 00000 001000
			T eq1 = lib.eq(ins, lib.toSignals(0b00000011111000000000000000001000, 32));

			T eq2 = lib.eq(reg.read(31), lib.toSignals(0, 32));
			eq1 = lib.and(eq1, eq2);
			eq = lib.or(eq,  eq1);

			return lib.not(eq);
		}


		private Register<T> loadInputsToRegister(CompEnv<T> env, int dataOffset)
				throws Exception {
			// inital registers are all 0's. no need to set value.
			Register<T> oram = new Register<T>(env,REGISTER_SIZE, WORD_SIZE);
			for(int i = 0; i < REGISTER_SIZE; ++i)
				oram.write(i, env.inputOfAlice(Utils.fromInt(0, WORD_SIZE)));

			//REGISTER 4

			oram.write(4, env.inputOfAlice(Utils.fromInt(dataOffset - (4 * (config.aliceInputSize + config.bobInputSize)), WORD_SIZE)));
			oram.write(5, env.inputOfAlice(Utils.fromInt(dataOffset - (4*config.bobInputSize), WORD_SIZE)));
			oram.write(6, env.inputOfAlice(Utils.fromInt(config.aliceInputSize, WORD_SIZE)));
			oram.write(7, env.inputOfAlice(Utils.fromInt(config.bobInputSize, WORD_SIZE)));

			env.flush();

			//Xiao: not sure about following:
			int stackPointer;
			stackPointer = dataOffset - (4*(config.aliceInputSize + config.bobInputSize)) - 32;
			oram.write(env.inputOfAlice(Utils.fromInt(29, oram.lengthOfIden)),
					env.inputOfAlice(Utils.fromInt(stackPointer, WORD_SIZE)));
			oram.write(env.inputOfAlice(Utils.fromInt(30, oram.lengthOfIden)),
					env.inputOfAlice(Utils.fromInt(stackPointer, WORD_SIZE)));
			//global pointer? 
			oram.write(env.inputOfAlice(Utils.fromInt(28, oram.lengthOfIden)),
					env.inputOfAlice(Utils.fromInt(stackPointer, WORD_SIZE)));
			return oram;
		}

		private SecureMap<T> loadInstructionsSingleBank(CompEnv<T> env, DataSegment instData)
				throws Exception {
			TreeMap<Long, boolean[]> instructions = null; 

			int numInst = instData.getDataLength();
			System.out.println("entering getInstructions, SingleBank.  Size:" + numInst);
			instructions = instData.getDataAsBooleanMap(); 

			//once we split the instruction from memory, remove the + MEMORY_SIZE
			SecureMap<T> instBank = new SecureMap<T>(env, numInst, WORD_SIZE);
//			IntegerLib<T> lib = new IntegerLib<T>(env);
//			T[] data; 
//			T[] index;

			if (env.getParty() == Party.Alice)
				instBank.init(instructions, 32, 32);
			else
				instBank.init(numInst, 32, 32);
			return instBank;
		}			

		private void loadInstructionsMultiBanks(CompEnv<T> env, SecureMap<T> singleBank, List<MemorySet<T>> sets) throws Exception {
			//System.out.println("entering loadInstructions");
			IntegerLib<T> lib = new IntegerLib<T>(env);
//			T[] data; 
//			T[] index;
			SecureMap<T> instructionBank;

			for(MemorySet<T> s:sets) {
				int i = s.getExecutionStep();

				EmulatorUtils.print("step: " + i + " size: " + s.size(), lib);

				TreeMap<Long,boolean[]> m = s.getAddressMap();	  
				long maxAddr = m.lastEntry().getKey();
				if (maxAddr == 0)
					break;
				//long minAddr = m.firstEntry().getKey();
				// do we still need this?
				long minAddr;
				if (s.size() == 1)
					minAddr = maxAddr;
				else minAddr = m.ceilingKey((long)1);

				if (!config.isMultipleBanks())
					instructionBank = singleBank;
				else {
					instructionBank = new SecureMap<T>(env, s.size(), WORD_SIZE);
					int count = 0;
					if (config.mode == Mode.VERIFY) {
						for( Map.Entry<Long, boolean[]> entry : m.entrySet()) {
							if (env.getParty() == Party.Alice) {
								EmulatorUtils.print("count: " + count + " key: " + entry.getKey() +
										" (0x" + Long.toHexString(entry.getKey()) + ")" +
										" value: " , lib, muteLoadInstructions);
								String output = "";
								for (int j = 31 ; j >= 0;  j--){
									if (entry.getValue()[j])
										output += "1";
									else 
										output += "0";
								}
								EmulatorUtils.print(output, lib, muteLoadInstructions);
							}
							count++;
						}
					}

					if (env.getParty() == Party.Alice){
						instructionBank.init(m, 32, 32);
					}
					else 
						instructionBank.init(m.size(), 32, 32);

					if (!muteLoadInstructions)
						instructionBank.print();

				}
				OramBank<T> bank = new OramBank<T>(instructionBank);
				bank.setMaxAddress(maxAddr);
				bank.setMinAddress(minAddr);
				s.setOramBank(bank);
			}
		}


		// load Alices code into memory here. (should be setMemory)

		//Change API to remove memBank and numInst.  Instantiate  memBank inside instead. 
		public Memory<T> getMemory(CompEnv<T> env, DataSegment memData) throws Exception {
			boolean memory[][] = memData.getDataAsBoolean();	
			IntegerLib<T> lib = new IntegerLib<T>(env);
			int dataLen = memData.getDataLength();
			int memSize = stackSize + dataLen;
			Memory<T> memBank = new Memory<T>(env, memSize, WORD_SIZE);

			T[] index; 
			T[] data;
			T[][] data2D;
			for (int i = 0; i < dataLen; i++) {
				index = lib.toSignals(i + stackSize, memBank.lengthOfIden);
				if (env.getParty() == Party.Alice)
					data = env.inputOfAlice(memory[i]);
				else 
					data = env.inputOfAlice(new boolean[WORD_SIZE]);
				if(memBank.circuitOram == null)
					memBank.trivialOram.content[i+stackSize] = data;
				else
				memBank.write(index, data);	
			}

			if(config.is2Ddata) {
				for (int i = 0; i < config.aliceInput2D.length; i++){
					for (int j = 0; j < config.aliceInput2D[0].length; j++){
						index = lib.toSignals(stackSize - config.aliceInputSize + (i * config.aliceInput2D[0].length)+j, memBank.lengthOfIden);
						if (env.getParty() == Party.Alice)
							data = env.inputOfAlice(Utils.fromInt(config.aliceInput2D[i][j], WORD_SIZE));
						else 
							data = env.inputOfAlice(new boolean[WORD_SIZE]);
						memBank.write(index, data);						
					}
				}
			}
			else if (true) {
				for (int i = 0; i < config.aliceInputSize; i++) {
					index = lib.toSignals(stackSize - config.aliceInputSize - config.bobInputSize + i , memBank.lengthOfIden);
					if (env.getParty() == Party.Alice)
						data = env.inputOfAlice(Utils.fromInt(config.aliceInput[i], WORD_SIZE));
					else 
						data = env.inputOfAlice(new boolean[WORD_SIZE]);
					if(memBank.circuitOram == null)
						memBank.trivialOram.content[stackSize - config.aliceInputSize - config.bobInputSize + i] = data;
					else 
						memBank.write(index, data);
				}
				T[][] Bobdata = null;
 				if (env.getParty() == Party.Alice)
 					Bobdata = env.inputOfBob(new boolean[config.bobInputSize][WORD_SIZE]);
				else {
					boolean[][] bob_binary = new boolean[config.bobInputSize][];
					for (int i = 0; i < config.bobInputSize; i++) {
						bob_binary[i] = Utils.fromInt(config.bobInput[i], WORD_SIZE);
					}
					Bobdata = env.inputOfBob(bob_binary);
				}
				
				
				for (int i = 0; i < config.bobInputSize; i++) {
					index = lib.toSignals(stackSize - config.bobInputSize + i , memBank.lengthOfIden);
					if(memBank.circuitOram == null)
						memBank.trivialOram.content[stackSize - config.bobInputSize + i] = Bobdata[i];
					else 
						memBank.write(index, Bobdata[i]);					
				}
			}
			EmulatorUtils.printOramBank(memBank, lib, stackSize + dataLen);
			return memBank;
		}
	}

	static public void main(String[] args) throws Exception {
		LocalConfiguration config = new LocalConfiguration( (args[1].contains("gen") || args[1].contains("Gen") ) ? Party.Alice : Party.Bob);
		MipsEmulator emu = null;
		switch(config.mode) {
		case VERIFY:
			emu = new MipsEmulatorImpl<Boolean>(config);
			break;
		case REAL:
		case OPT:
			emu = new MipsEmulatorImpl<GCSignal>(config);
			break;
		default:
			System.err.println("Help!  What do I do about " +  config.mode + "?");
			System.exit(1);
		}
		emu.emulate(args);
	}

	public void emulate(String[] args) throws Exception {
		if(args[1].contains("gen") || args[1].contains("Gen")) {
			Reader rdr = new Reader(new File(args[0].trim()), config);
			System.err.println("Executing binary file: " + args[0].trim());
			SymbolTableEntry ent = rdr.getSymbolTableEntry(config.getEntryPoint());
			DataSegment instData = rdr.getInstructions(config.getFunctionLoadList());
			DataSegment memData = rdr.getData();
			int pcOffset = (int) ent.getAddress();
			int dataOffset = (int) rdr.getDataAddress();
			MemSetBuilder<ET> b = new MemSetBuilder<ET>(config, args[0].trim());
			new GenRunnable<ET>(b.build(), instData, memData, pcOffset, dataOffset).run();
		}
		else {
			Reader rdr = new Reader(new File(args[0].trim()), config);
			SymbolTableEntry ent = rdr.getSymbolTableEntry(config.getEntryPoint());
			DataSegment instData = rdr.getInstructions(config.getFunctionLoadList());
			DataSegment memData = rdr.getData();
			int pcOffset = (int) ent.getAddress();
			int dataOffset = (int) rdr.getDataAddress();
			MemSetBuilder<ET> b = new MemSetBuilder<ET>(config, args[0].trim());
			new EvaRunnable<ET>(b.build(), instData, memData, pcOffset, dataOffset).run();
		}
	}

	private class GenRunnable<T> extends network.Server implements Runnable {
		MipsParty<T> mips;
		public GenRunnable(List<MemorySet<T>> sets,	DataSegment instData, DataSegment memData, int pcOffset,int dataOffset ){
			mips = new MipsParty<T>(sets, instData, memData, pcOffset, dataOffset);
		}

		public void run() {
			try {
				listen(config.ServerPort);
				@SuppressWarnings("unchecked")
				CompEnv<T> env = CompEnv.getEnv(config.mode, Party.Alice, this);
				mips.mainloop(env);
				disconnect();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	private class EvaRunnable<T> extends network.Client implements Runnable {
		MipsParty<T> mips;
		public EvaRunnable(List<MemorySet<T>> sets,	DataSegment instData, DataSegment memData, int pcOffset,int dataOffset ){
			mips = new MipsParty<T>(sets, instData, memData, pcOffset, dataOffset);
		}

		public void run() {
			try {
				connect(config.ServerAddress, config.ServerPort);
				@SuppressWarnings("unchecked")
				CompEnv<T> env = CompEnv.getEnv(config.mode, Party.Bob, this);
				mips.mainloop(env);
				disconnect();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
}
