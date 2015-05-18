package com.appcomsci.mips.binary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.appcomsci.mips.memory.MipsInstructionSet;
import com.appcomsci.sfe.common.Configuration;
import com.appcomsci.sfe.common.SfeProperties;

/**
 * A class to read text and data from binary files.
 * Think of this class as the simulator loader.
 * 
 * @author Allen McIntosh
 *
 */
public class Reader {
	/** A logger for error messages */
	private static Logger logger = Logger.getLogger(Reader.class.getName());
	
	/** Properties */
	private SfeProperties properties;
	/** Configuration information */
	private final Configuration config;
	
	/** A table of symbols found in the binary */
	Map<String, SymbolTableEntry> symbolTable = new HashMap<String, SymbolTableEntry>();
	/** A table of sections found in the binary */
	Map<String, SectionData> sectionTable = new HashMap<String, SectionData>();
	
	/**
	 * Constructor for use by TextReader.  Do not use.
	 */
	protected Reader(final Configuration config) {
		this.config = config;
	}
	
	/**
	 * Standard constructor - runs llvm-objdump and parses the output.
	 * 
	 * @param f Load data from this file
	 * @throws FileNotFoundException If the file does not exist
	 * @throws IOException If the file cannot be read
	 */
	public Reader(File f, Configuration config) throws FileNotFoundException, IOException, IllegalArgumentException {
		this.config = config;
		if(!f.exists()) {
			String msg = "Binary file " + f.getPath() + " does not exist";
			logger.severe(msg);
			throw new FileNotFoundException(msg);
		}
		
		// Find the name of the reader program and run it
		
		String readerProgram = this.config.getBinaryReaderPath();
		
		ProcessBuilder pb = new ProcessBuilder(readerProgram,
				"-disassemble", "-t", "-r", "-s", "-section-headers", f.getPath()
				);
		pb.redirectInput(ProcessBuilder.Redirect.PIPE);
		pb.redirectError(ProcessBuilder.Redirect.INHERIT);
		
		Process proc = pb.start();
		// Avoid deadlocks by using pipe for stdin and closing it
		// immediately.
		proc.getOutputStream().close();
		// Now read the process output.
		init(new BufferedReader(new InputStreamReader(proc.getInputStream())));
	}
	
	/** Parser states
	 */
	private enum parseState {
		BEGIN,
		TEXT_SECTION,
		SECTION_HEADERS,
		SECTION,
		SYMBOL_TABLE
	}
	
	/** Real initialization.
	 * 
	 * @param rdr A file or the output of a process.
	 * @throws IOException
	 */
	protected void init(BufferedReader rdr) throws IOException {
		String line;
		SectionData currentBinarySection = null;
		
		// Basic patterns
		String nameEx = "([-\\.A-Za-z0-9_\\*]+)";
		String hex = "([0-9a-f]+)";
		String hexWs = hex + "\\s+";
		
		// Start of text disassembly
		Pattern textSectionPattern = Pattern.compile("Disassembly of section " + nameEx + ":");
		// The parts of an instruction that we used to care about
		Pattern instructionPattern = Pattern.compile(hex + ":\\s*+" + hexWs + hexWs + hexWs + hex);
		
		// Start of binary section contents
		Pattern sectionPattern = Pattern.compile("Contents of section " + nameEx + ":");	
		// One line of binary contents
		Pattern dataPattern = Pattern.compile(" " + hex + "(.{36}?)");
		
		// For picking apart a symbol table entry
		Pattern symbolTablePattern = Pattern.compile("([0-9a-f]{8}?)\\s+(.{7}?)\\s+" + nameEx + "\\s+"
					+ hex + "\\s+(\\S*)");
		
		// For picking apart "skipping contents" line
		Pattern skippingPattern = Pattern.compile("\\[" + hex + ",\\s*" + hex + "\\)");
		
		parseState state = parseState.BEGIN;
		while((line = rdr.readLine()) != null) {
//			System.err.println(line);
			
			// First, look for major state transitions
			
			Matcher m;
			m = textSectionPattern.matcher(line);
			if(m.find()) {
				// currentSection = m.group(1);
				state = parseState.TEXT_SECTION;
				// System.err.println(">>>>>Text Section " + m.group(1));
				continue;
			}
			if(line.startsWith("Sections:")) {
				state = parseState.SECTION_HEADERS;
				continue;
			}
			m = sectionPattern.matcher(line);
			if(m.find()) {
				currentBinarySection = new SectionData(m.group(1));
				sectionTable.put(currentBinarySection.getName(), currentBinarySection);
				state = parseState.SECTION;
				continue;
			}
			if(line.startsWith("SYMBOL TABLE:")) {
				state = parseState.SYMBOL_TABLE;
				continue;
			}
			
			// No changes.  Parse data as appropriate
			switch(state) {
			case TEXT_SECTION:
				/*
				// No longer need any of this
				// Probably only pick these off for the addresses
				m = instructionPattern.matcher(line);
				if(m.find()) {
					String addr = m.group(1);
					String instr = m.group(5) + m.group(4) + m.group(3) + m.group(2);
					System.err.println(">>Instruction " + addr + " " + instr);
				}
				*/
				break;
			case SYMBOL_TABLE:
				// Match a symbol table line
				m = symbolTablePattern.matcher(line);
				if(m.find()) {
					SymbolTableEntry ent = new SymbolTableEntry(m.group(5));
					ent.setAddress(m.group(1));
					ent.setFlags(m.group(2));
					ent.setSectionName(m.group(3));
					ent.setNumBytes(m.group(4));
					symbolTable.put(ent.getName(), ent);
				} else {
					logger.severe(">>>>>Fall through: Symbol table: " + line);
				}
				break;
			case SECTION:
				// Section data
				if(line.contains("skipping contents")) {
					m = skippingPattern.matcher(line);
					if(m.find()) {
						long secStart = Long.parseLong(m.group(1), 16);
						long secEnd =  Long.parseLong(m.group(2), 16);
						currentBinarySection.setLocation(secStart, secEnd);
					}
				} else {
					m = dataPattern.matcher(line);
					if(m.find()) {
						String addr = m.group(1);
						String data = m.group(2);
						currentBinarySection.addDataSegment(addr, data);
					}
				}
				break;
			case SECTION_HEADERS:
				break;
			default:
				break;
			}
		}
		
		// Clean up section table entries.
		// These are associated with symbols, but are never defined.
		
		sectionTable.put("*ABS*", new SectionData("*ABS*"));
		sectionTable.put("*UND*", new SectionData("*UND*"));
		
		for(SectionData sec:sectionTable.values()) {
			SymbolTableEntry ent = symbolTable.get(sec.getName());
			if(ent == null) {
				// Sometimes there are no symbol table entries for sections
				ent = new SymbolTableEntry(sec.getName());
				ent.setSectionData(sec);
				ent.setAddress(sec.getStartAddress());
				// logger.warning("No symbol table entry for section " + sec.getName());
			} else {
				sec.setSymbolTableEntry(ent);
			}
			
			// Symbol table entries for sections often do not have num bytes set.
			// Fix this in the interest of paranoia
			if(ent.getNumBytes() == 0)
				ent.setNumBytes(sec.getNumBytes());
		}
		for(SymbolTableEntry ent:symbolTable.values()) {
			SectionData sec = sectionTable.get(ent.getSectionName());
			if(sec == null) {
				logger.warning("No section table entry for symbol " + ent.getName()
						+ " section was " + ent.getSectionName());
			} else {
				ent.setSectionData(sec);
			}
		}
		
		// This is wrong.  The address of the spin symbol should not be static.  FIXME
		// Second, the length of the halt symbol should not be zero.
		
		SymbolTableEntry halt = getSymbolTableEntry(MipsInstructionSet.SPIN_SYMBOL);
		if(halt != null) {
			if(halt.getNumBytes() == 0) {
				System.err.println("Warning: Halt length listed as 0");
				halt.setNumBytes(8);
			}
			MipsInstructionSet.setSpinAddress(halt.getAddress());
		}
	}
	
	/**
	 * Look a name up in the symbol table.  Check the sanity of
	 * the symbol table entry and return it.
	 * 
	 * @param name Name to be looked up in the symbol table.
	 * @return The symbol table entry
	 */
	private SymbolTableEntry checkSymbolNameSanity(String name) {
		SymbolTableEntry ent = symbolTable.get(name);
		if(ent == null) {
			logger.info("No symbol table entry for " + name);
			return null;
		}
		if(ent.getSectionData() == null) {
			logger.info("No section data associated with " + name);
			return null;
		}
		return ent;
	}
	
	/**
	 * Get the instructions for a routine as longs
	 * @param name The routine name
	 * @return The instructions
	 */
	public DataSegment getInstructions(String name) {
		ArrayList<String> tmp = new ArrayList<String>();
		tmp.add(name);
		return extractByFunctionName(tmp);
	}
	
	public DataSegment getInstructions(Collection<String>names) {
		return extractByFunctionName(names);
	}
	
	/**
	 * Extract a symbol table entry for a symbol.
	 * The getSectionData method of that entry will give access
	 * to the data (if any) associated with the object.
	 * 
	 * @param name The name of the symbol
	 * @return The symbol table entry
	 */
	public SymbolTableEntry getSymbolTableEntry(String name) {
		return symbolTable.get(name);
	}
	
	private static String dataSectionNames[] = {
		".data", ".rld_map", ".got", ".sdata", ".bss"
	};
	
	/** Compute address of data returned by getData()
	 * 
	 * @return
	 */
	public long getDataAddress() {
		long baseAddress = Long.MAX_VALUE;
		for(String name : dataSectionNames) {
			SectionData sec = sectionTable.get(name);
			if(sec == null) {
				logger.warning("Could not find section data for " + name);
				continue;
			}
			if(sec.getStartAddress() < baseAddress)
				baseAddress = sec.getStartAddress();
		}
		return baseAddress;
	}
	
	/** Get contents of all data segments
	 * 
	 * @return The (merged) contents of all data segments.
	 * 		This expands the .bss segment into the appropriate
	 * 		number of 0 words
	 */
	
	public DataSegment getData() {
		
		// Sample data layout
		// 20 .data         00000010 0000000000411000 DATA 
		// 21 .rld_map      00000004 0000000000411010 DATA 
		// 22 .got          00000040 0000000000411020 DATA 
		// 23 .sdata        00000004 0000000000411060 DATA 
		// 24 .bss          00000410 0000000000411070 BSS

		return extractBySectionName(Arrays.asList(dataSectionNames));
	}
	
	/** Get the contents of one or more named sections, consolidated into a
	 * single DataSegment object
	 * @param sectionNames
	 * @return
	 */
	private DataSegment extractBySectionName(Collection<String> sectionNames) {
		
		// Extract sections

		ArrayList<DataSegment> dataSections = new ArrayList<DataSegment>();
		for(String name : sectionNames) {
			SectionData sec = sectionTable.get(name);
			if(sec != null) dataSections.add(sec.getData());
		}
		return extract(dataSections);
		
	}
	
	/** Get the contents of one or more functions, returning a single
	 * DataSegment
	 * @param functionNames
	 * @return
	 */
	private DataSegment extractByFunctionName(Collection<String> functionNames) {
		ArrayList<DataSegment> dataSections = new ArrayList<DataSegment>();
		for(String name : functionNames) {
			SymbolTableEntry ent = checkSymbolNameSanity(name);
			if(ent != null) {
				dataSections.add(ent.getSectionData().getData(ent));
			}
			
		}
		return extract(dataSections);
	}
	
	/** Get the contents of one or more data segments, consolidated into a
	 * single DataSegment object
	 * @param sectionNames
	 * @return
	 */
	private DataSegment extract(ArrayList<DataSegment> segments) {
		
		// Get addresses

		long baseAddress = segments.get(0).getStartAddress();
		long endAddress = segments.get(0).getStartAddress();
		for(DataSegment seg:segments) {
			if(seg.getStartAddress() < baseAddress)
				baseAddress = seg.getStartAddress();
			long addr = seg.getStartAddress() + (seg.getDataLength()<<2);
			if(addr > endAddress)
				endAddress = addr;
		}
		
		// Allocate and build data

		int numInstructions = (int) (endAddress- baseAddress) >> 2;
		long rData[] = new long[numInstructions];
		
		for(DataSegment seg:segments) {
			long secData[] = seg.getData();
			int delta = (int)(seg.getStartAddress() - baseAddress) >> 2;
			for(int i = 0; i < secData.length; i++) {
				rData[delta + i] = secData[i];
			}
		}
		return new DataSegment(baseAddress, rData);
	}
}
