/**
 * 
 */
package com.appcomsci.mips.binary;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

import org.junit.Before;
import org.junit.Test;

/**
 * @author mcintosh
 *
 */
public class TestDataSegment {
	
	private static final long data[] = {
		0x00000000, 0xffffffff, 0x0f1e2d3c, 0x4b5a6978,
		0xdeadbeef
	};
	long startAddr = 0x40000000;
	
	DataSegment seg;

	@Before
	public void setUp() throws Exception {
		seg = new DataSegment(startAddr, data);
	}

	/**
	 * Test method for {@link com.appcomsci.mips.binary.DataSegment#getStartAddress()}.
	 */
	@Test
	public void testGetStartAddress() {
		assertEquals("Start address", seg.getStartAddress(), startAddr);
	}

	/**
	 * Test method for {@link com.appcomsci.mips.binary.DataSegment#getDataLength()}.
	 */
	@Test
	public void testGetDataLength() {
		assertEquals("Data Length", seg.getDataLength(), data.length);
	}

	/**
	 * Test method for {@link com.appcomsci.mips.binary.DataSegment#getData()}.
	 */
	@Test
	public void testGetData() {
		assertArrayEquals("Data", seg.getData(), data);
	}

	/**
	 * Test method for {@link com.appcomsci.mips.binary.DataSegment#getDatum(long)}.
	 */
	@Test
	public void testGetDatum() {
		assertEquals("Datum(40000000+4", seg.getDatum(startAddr+4*1), data[1]);
	}

	/**
	 * Test method for {@link com.appcomsci.mips.binary.DataSegment#getDataAsBigIntegers()}.
	 */
	/*
	@Test
	public void testGetDataAsBigIntegers() {
		fail("Not yet implemented");
	}
	*/

	/**
	 * Test method for {@link com.appcomsci.mips.binary.DataSegment#getDataAsBoolean()}.
	 */
	@Test
	public void testGetDataAsBoolean() {
		boolean x[][] = seg.getDataAsBoolean();
		boolean y[][] = getDataAsBooleanOld(data);
		assertEquals("Bool length", x.length, data.length);
		assertEquals("Bool lengths", x.length, y.length);
		for(int i = 0; i < x.length; i++) {
			assertEquals("x[i].length == 32", x[i].length, 32);
			assertEquals("y[i].length == 32", y[i].length, 32);
			// There is no assertArrayEquals for booleans!
			for(int j = 0; j < 32; j++)
				assertEquals("BoolData[" + i + "][" + j + "]", x[i][j], y[i][j]);
		}
	}
	
	// Old code.  Use it to test compatibility of new code.
	
	private boolean[][] getDataAsBooleanOld(long data[]) {
		if(data == null)
			return null;
		boolean rslt[][] = new boolean[data.length][32];
		for(int i = 0; i < data.length; i++) {
			long x = data[i];
			byte t[] = new byte[4];
			t[3] = (byte) (x & 0xff); x >>= 8;
			t[2] = (byte) (x & 0xff); x >>= 8;
			t[1] = (byte) (x & 0xff); x >>= 8;
			t[0] = (byte) x;
			
			System.out.println(t[0] + " : " + t[1] + " : " + t[2] + " : " + t[3] + " : ");
			
			rslt[i][7] = ((t[3] & 0x80) == 0) ? false : true;
			rslt[i][15] = ((t[2] & 0x80) == 0) ? false : true;
			rslt[i][23] = ((t[1] & 0x80) == 0) ? false : true;
			rslt[i][31] = ((t[0] & 0x80) == 0) ? false : true;
			
			rslt[i][6] = ((t[3] & 0x40) == 0) ? false : true;
			rslt[i][14] = ((t[2] & 0x40) == 0) ? false : true;
			rslt[i][22] = ((t[1] & 0x40) == 0) ? false : true;
			rslt[i][30] = ((t[0] & 0x40) == 0) ? false : true;
			
			rslt[i][5] = ((t[3] & 0x20) == 0) ? false : true;
			rslt[i][13] = ((t[2] & 0x20) == 0) ? false : true;
			rslt[i][21] = ((t[1] & 0x20) == 0) ? false : true;
			rslt[i][29] = ((t[0] & 0x20) == 0) ? false : true;
			
			rslt[i][4] = ((t[3] & 0x10) == 0) ? false : true;
			rslt[i][12] = ((t[2] & 0x10) == 0) ? false : true;
			rslt[i][20] = ((t[1] & 0x10) == 0) ? false : true;
			rslt[i][28] = ((t[0] & 0x10) == 0) ? false : true;
			
			rslt[i][3] = ((t[3] & 0x08) == 0) ? false : true;
			rslt[i][11] = ((t[2] & 0x08) == 0) ? false : true;
			rslt[i][19] = ((t[1] & 0x08) == 0) ? false : true;
			rslt[i][27] = ((t[0] & 0x08) == 0) ? false : true;
			
			rslt[i][2] = ((t[3] & 0x04) == 0) ? false : true;
			rslt[i][10] = ((t[2] & 0x04) == 0) ? false : true;
			rslt[i][18] = ((t[1] & 0x04) == 0) ? false : true;
			rslt[i][26] = ((t[0] & 0x04) == 0) ? false : true;
			
			rslt[i][1] = ((t[3] & 0x02) == 0) ? false : true;
			rslt[i][9] = ((t[2] & 0x02) == 0) ? false : true;
			rslt[i][17] = ((t[1] & 0x02) == 0) ? false : true;
			rslt[i][25] = ((t[0] & 0x02) == 0) ? false : true;
			
			rslt[i][0] = ((t[3] & 0x01) == 0) ? false : true;
			rslt[i][8] = ((t[2] & 0x01) == 0) ? false : true;
			rslt[i][16] = ((t[1] & 0x01) == 0) ? false : true;
			rslt[i][24] = ((t[0] & 0x01) == 0) ? false : true;
						
		}
		return rslt;
	
	}

}
