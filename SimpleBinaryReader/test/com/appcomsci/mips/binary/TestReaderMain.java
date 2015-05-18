package com.appcomsci.mips.binary;

import java.io.File;
import java.io.IOException;

import com.appcomsci.sfe.common.Configuration;

public class TestReaderMain {

	public static void main(String[] args) {
		if(args.length != 2) {
			System.err.println("Usage: " +  TestReaderMain.class.getName() + " file function_name");
			return;
		}
		Configuration cfg = null;
		try {
				cfg = new Configuration();
		} catch(IOException e) {
			System.err.println("Exception setting up configuration: " + e);
			System.exit(1);
		}
		cfg.setEntryPoint(args[1]);
		try {
			Reader rdr = new Reader(new File(args[0]), cfg);
			DataSegment seg = rdr.getData();
		} catch(Exception e) {
			System.err.println("Exception reading file " + args[0]);
		}
	}

}
