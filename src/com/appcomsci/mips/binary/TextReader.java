package com.appcomsci.mips.binary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import com.appcomsci.sfe.common.Configuration;

/**
 * Interface to Reader that feeds it an existing output file.
 * @author Allen McIntosh
 *
 */
public class TextReader extends Reader {
	public TextReader(File f, final Configuration config) throws FileNotFoundException, IOException {
		super(config);
		BufferedReader rdr = new BufferedReader(new FileReader(f));
		init(rdr);
	}
}
