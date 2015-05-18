package com.appcomsci.mips.binary;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Data for a single section
 * @author Allen McIntosh
 *
 */

public class SectionData {
	/** The name of the section */
	private final String name;
	/** The symbol table entry (if any) for this section.
	 * Note that there may be other symbol table entries describing
	 * parts of the section.
	 */
	private SymbolTableEntry symbolTableEntry;
	/** The data contained in the section */
	private byte bytes[];
	/** The number of bytes in the section */
	private int numBytes;
	/** The start address of the section */
	private long startAddress;
	/** Have we consolidated all the lines of section yet? */
	private boolean consolidated = false;
	
	private static Logger logger = Logger.getLogger(SectionData.class.getName());
	
	/** Holds one line of section data collected from
	 * objdump output.
	 *
	 */
	private class DumpedDataSegment {
		final long address;
		/** Size in *bytes* */
		int numBytes;
		// Should this be bytes?
		byte data[] = new byte[16];
		DumpedDataSegment(long address) {
			this.address = address;
		}
	}
	
	/** Data from objdump output get stashed here until we need to consolidate
	 * them in order to extract a chunk.
	 */
	private ArrayList<DumpedDataSegment> inputData = new ArrayList<DumpedDataSegment>();
	
	public SectionData(String name) {
		this.name = name;
	}
	
	/** Convenience function to extract the data for the entire segment */
	public DataSegment getData() {
		return getData(startAddress, numBytes>>2);
	}
	
	/** Convenience function to extract the data associated with a symbol
	 * (a function, usually).
	 * @param ent
	 * @return
	 */
	public DataSegment getData(SymbolTableEntry ent) {
		return getData(ent.getAddress(), ent.getNumBytes()>>2);
	}
	
	private DataSegment getData(long addr, int numInstructions) {
		// Must do this first
		if(!consolidated)
			consolidate();
		checkFetchSanity(addr, numInstructions);
		long rData[] = new long[numInstructions];
		if(bytes != null) {
			int j = (int)(addr - startAddress);
			for(int i = 0; i < numInstructions; i+=1, j+=4) {
				long w = (bytes[j]&0xff)
						|((bytes[j+1]&0xff)<<8)
						|((bytes[j+2]&0xff)<<16)
						|((bytes[j+3]&0xff)<<24);
				rData[i] = w & 0xffffffffL;
			}
		}
		return new DataSegment(addr, rData);
	}
	
	private void checkFetchSanity(long addr, int numInstructions) {
		if((numBytes&0x3) != 0) {
			String msg = "Size of segment " + name + " not a multiple of 4";
			logger.severe(msg);
			throw new RuntimeException(msg);
		}
		if(addr < startAddress) {
			String msg = "Fetch at address " + addr + " < start address " + startAddress;
			logger.severe(msg);
			throw new RuntimeException(msg);
		}
		if(addr+(numInstructions<<2) > startAddress+numBytes) {
			String msg = "Fetch at address " + addr + " exceeds segment size";
			logger.severe(msg);
			throw new RuntimeException(msg);
		}
	}
	
	/** Set the start address and length of this segment.
	 * Complain if there are conflicts.
	 * @param startAddress
	 * @param endAddress
	 */
	public void setLocation(long startAddress, long endAddress) {
		if(!consolidated)
			consolidate();
		int numBytes = (int) (endAddress - startAddress);
		if(inputData.size() > 0) {
			if(startAddress != inputData.get(0).address)
				logger.warning("Setting conflicting start address");
			if(numBytes != this.numBytes) {
				logger.warning("Setting conflicting numBytes");
			}
		}
		this.startAddress = startAddress;
		this.numBytes = numBytes;
	}
	
	/** Consolidate all the single lines of data found for this section */
	private void consolidate() {
		// Just return if consolidation done already
		if(consolidated)
			return;
		if(inputData.size() == 0) {
			startAddress = 0;
			consolidated = true;
			return;
		}
		startAddress = inputData.get(0).address;
		// The end address plus one
		long endAddress = startAddress;
		for(DumpedDataSegment seg:inputData) {
			if(seg.address < startAddress) {
				throw new RuntimeException("Addresses in segment " + name + " run backwards at " + seg.address);
			}
			long a = seg.address+seg.numBytes;
			if(endAddress < a)
				endAddress = a;
		}
		numBytes = (int)(endAddress - startAddress);
		bytes = new byte[(int)numBytes];
		for(DumpedDataSegment seg:inputData) {
			int a = (int)(seg.address - startAddress);
			for(int i = 0; i < seg.numBytes; i++) {
				bytes[a+i] = seg.data[i];
			}
		}
		consolidated = true;
	}
	
	/** Add another line of data from objdump output */
	public void addDataSegment(String address, String data) {
		if(consolidated) {
			String msg = "Attempting to add data after consolidation: segment " + name;
			logger.severe(msg);
			throw new RuntimeException(msg);
		}
		DumpedDataSegment seg = new DumpedDataSegment(Long.parseLong(address, 16));
		inputData.add(seg);
		Pattern dataPattern = Pattern.compile("[0-9a-f]+");
		Matcher m = dataPattern.matcher(data);
		int n = 0;
		while(m.find()) {
			if(n >= 16) {
				String msg = "Too many hex digit groups in data: " + data;
				logger.severe(msg);
				throw new RuntimeException(msg);
			}
			String digits = m.group(0);
			for(int i = 0; i < digits.length(); i += 2) {
				String s = digits.substring(i,i+2);
				seg.data[n++] = (byte) Integer.parseInt(s, 16);
			}
		}
		seg.numBytes = n;
	}

	public String getName() {
		return name;
	}

	public SymbolTableEntry getSymbolTableEntry() {
		return symbolTableEntry;
	}

	public void setSymbolTableEntry(SymbolTableEntry symbolTableEntry) {
		this.symbolTableEntry = symbolTableEntry;
	}

	/** Get the section data */
	public byte[] getBytes() {
		if(!consolidated)
			consolidate();
		return bytes;
	}

	/** Get the section size */
	public int getNumBytes() {
		if(!consolidated)
			consolidate();
		return numBytes;
	}

	/** Get the section start address */
	public long getStartAddress() {
		if(!consolidated)
			consolidate();
		return startAddress;
	}
	
	protected void foo() {
		if(bytes == null) System.err.println("Bytes null");
		else System.err.println("Bytes not null");
	}
}
