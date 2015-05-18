package com.appcomsci.mips.binary;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;

import org.junit.Test;

import com.appcomsci.sfe.common.Configuration;
import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

public class TestTextReader {

	// Minimal sanity check - extract main twice and check for equality
	
	@Test
	public void test1() throws IOException {
		Configuration cfg = new Configuration();
		TextReader rdr = new TextReader(new File("data/data1.txt"), cfg);
		DataSegment mseg = rdr.getInstructions("main");
		long longInst[] = mseg.getData();
		BigInteger bigInst[] = mseg.getDataAsBigIntegers();
		
		// Feedback just for kicks
		System.out.println(Long.toString(longInst[0], 16) + " " + longInst[0]);
		System.out.println(bigInst[0].toString(16) + " " + bigInst[0].toString());		
		assertEquals("Program array lengths", longInst.length, bigInst.length);
		for(int i = 0; i < longInst.length; i++) {
			assertEquals("Instruction " + i, longInst[i], bigInst[i].longValue());
		}
		DataSegment dataSeg = rdr.getData();
		long longData[] = dataSeg.getData();
		BigInteger bigData[] = dataSeg.getDataAsBigIntegers();	
		long addr = rdr.getDataAddress();
		System.out.println("Data address " + Long.toHexString(addr));	
		System.out.println("Data length (words) " + longData.length);
		assertEquals("Data array lengths", longInst.length, bigInst.length);
		for(int i = 0; i < longData.length; i++) {
			assertEquals("Data word " + i, longData[i], bigData[i].longValue());
		}
	}
	
	@Test
	public void test2() throws IOException {
		// Just test that we can parse this one
		Configuration cfg = new Configuration();
		TextReader rdr = new TextReader(new File("data/data2.txt"), cfg);
		DataSegment seg = rdr.getInstructions("sfe_main");
		long longInst[] = seg.getData();
		assertTrue("non-zero instruction length", longInst.length > 0);
		SymbolTableEntry ent = rdr.getSymbolTableEntry("sfe_main");
		System.out.println("Address of sfe_main " + Long.toHexString(ent.getAddress()));
	}
	
	private static final String functionNames[] = { "sfe_main", "foo", "bar" };
	
	@Test
	public void test3() throws IOException {
		Configuration cfg = new Configuration();
		TextReader rdr = new TextReader(new File("data/data3.txt"), cfg);
		DataSegment seg = rdr.getInstructions(Arrays.asList(functionNames));
		System.out.println("Text address " + Long.toHexString(seg.getStartAddress()));
		System.out.println("Start address " + Long.toHexString(rdr.getSymbolTableEntry("sfe_main").getAddress()));
	}

}
