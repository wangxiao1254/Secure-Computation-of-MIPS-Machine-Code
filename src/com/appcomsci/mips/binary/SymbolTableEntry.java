package com.appcomsci.mips.binary;

import java.util.logging.Logger;

public class SymbolTableEntry {
	private final String name;
	private long address;
	private int numBytes;
	private String flags;
	private String sectionName;
	private SectionData sectionData;
	
	private static Logger logger = Logger.getLogger(SymbolTableEntry.class.getName());
	
	public SymbolTableEntry(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public long getAddress() {
		return address;
	}
	public void setAddress(long address) {
		this.address = address;
	}
	public void setAddress(String address) {
		setAddress(Long.parseLong(address, 16));
	}
	public int getNumBytes() {
		return numBytes;
	}
	public void setNumBytes(int numBytes) {
		this.numBytes = numBytes;
	}
	public void setNumBytes(String numBytes) {
		setNumBytes((int)Long.parseLong(numBytes, 16));
	}
	public String getFlags() {
		return flags;
	}
	public void setFlags(String flags) {
		this.flags = flags;
	}

	public String getSectionName() {
		return sectionName;
	}

	public void setSectionName(String sectionName) {
		this.sectionName = sectionName;
	}

	public SectionData getSectionData() {
		return sectionData;
	}

	public void setSectionData(SectionData sectionData) {
		this.sectionData = sectionData;
	}
}
