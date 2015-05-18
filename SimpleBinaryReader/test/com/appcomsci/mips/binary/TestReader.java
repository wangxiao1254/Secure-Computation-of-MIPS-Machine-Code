package com.appcomsci.mips.binary;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;

import org.junit.Test;

import com.appcomsci.sfe.common.Configuration;

public class TestReader {

	@Test
	public void test() throws FileNotFoundException, IOException {
		Configuration cfg = new Configuration();
		Reader rdr = new Reader(new File("data/data2-clang"), cfg);
		DataSegment txt = rdr.getInstructions("sfe_main");
		long longInst[] = txt.getData();
		BigInteger bigInst[] = txt.getDataAsBigIntegers();
		
		// Feedback just for kicks
		System.out.println(Long.toString(longInst[0], 16) + " " + longInst[0]);
		System.out.println(bigInst[0].toString(16) + " " + bigInst[0].toString());
		
		assertEquals("Program array lengths", longInst.length, bigInst.length);
		for(int i = 0; i < longInst.length; i++) {
			assertEquals("Instruction " + i, longInst[i], bigInst[i].longValue());
		}

		DataSegment seg = rdr.getData();
		long longData[] = seg.getData();
		BigInteger bigData[] = seg.getDataAsBigIntegers();		
		System.out.println("Data length " + longData.length);
		assertEquals("Data array lengths", longInst.length, bigInst.length);
		for(int i = 0; i < longData.length; i++) {
			assertEquals("Data word " + i, longData[i], bigData[i].longValue());
		}
	}

}
