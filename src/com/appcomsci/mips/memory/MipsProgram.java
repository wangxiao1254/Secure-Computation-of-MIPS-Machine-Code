/**
 * 
 */
package com.appcomsci.mips.memory;

import jargs.gnu.CmdLineParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.appcomsci.sfe.common.Configuration;

/**
 * @author Allen McIntosh
 *
 */
public abstract class MipsProgram {
	
	/** This is the name of the program to run to dissect binaries */
	private String binaryReaderPath;
	/** This is the name of the mips binary */
	private String mipsBinaryPath;
//	private String entryPoint;
//	List<String>functionLoadList;
//	private int maxProgramSteps;
//	private boolean honorDelaySlots;
	
	private Configuration config;
	
	public MipsProgram(Configuration config, String mipsBinaryPath) {
		this.config = config;
		this.mipsBinaryPath = mipsBinaryPath;
	}
	
	public MipsProgram(String args[]) throws IOException, CmdLineParser.OptionException {
		// Create configuration object.  This reads the properties file.
		config = new Configuration();
		
		// Now parse command line arguments
		
		CmdLineParser parser = new CmdLineParser();
		CmdLineParser.Option.StringOption oE = new CmdLineParser.Option.StringOption('e', Configuration.ENTRY_POINT_PROPERTY);
		CmdLineParser.Option.StringOption oL = new CmdLineParser.Option.StringOption('l', Configuration.FUNCTION_LOAD_LIST_PROPERTY);
		CmdLineParser.Option.StringOption oB = new CmdLineParser.Option.StringOption('b', Configuration.READER_PATH_PROPERTY);
		CmdLineParser.Option.IntegerOption oM = new CmdLineParser.Option.IntegerOption('m', Configuration.MAX_PROGRAM_STEPS_PROPERTY);
		CmdLineParser.Option.BooleanOption oD = new CmdLineParser.Option.BooleanOption('d', Configuration.HONOR_DELAY_SLOTS_PROPERTY);
		CmdLineParser.Option.StringOption oC = new CmdLineParser.Option.StringOption('c', Configuration.CLASS_NAME_ROOT_PROPERTY);
		CmdLineParser.Option.StringOption oP = new CmdLineParser.Option.StringOption('p', Configuration.PACKAGE_NAME_PROPERTY);
		CmdLineParser.Option.StringOption oO = new CmdLineParser.Option.StringOption('o', Configuration.OUTPUT_DIRECTORY_PROPERTY);
		
		parser.addOption(oE);
		parser.addOption(oL);
		parser.addOption(oB);
		parser.addOption(oM);
		parser.addOption(oD);
		parser.addOption(oC);
		parser.addOption(oP);
		parser.addOption(oO);

		parser.parse(args);
		
		// Pick off file name, which should be remaining arg
		// (and currently only arg)
		// If no file name, will get from properties file.
		// This is probably an error.

		String rest[] = parser.getRemainingArgs();
		switch(rest.length) {
		case 1:
			mipsBinaryPath = rest[0];
			break;
		case 0:
			break;
		default:
			printUsage();
			System.exit(2);
		}
		
		// Finally, pick off options
		
		Object o;
		if((o = parser.getOptionValue(oE)) != null) {
			config.setEntryPoint((String)o);
			if((o = parser.getOptionValue(oL)) != null)
				config.setFunctionLoadList((String) o);
			else // Load list defaults to entry point, NOT something in
				// config file
				config.setFunctionLoadList(config.getEntryPoint());
		} else {
			// Set load list if given, otherwise use config file
			if((o = parser.getOptionValue(oL)) != null)
				config.setFunctionLoadList((String) o);
		}
		if((o = parser.getOptionValue(oM)) != null)
			config.setMaxProgramSteps((Integer)o);
		if((o = parser.getOptionValue(oD)) != null)
			config.setHonorDelaySlots((Boolean) o);
		if((o = parser.getOptionValue(oC)) != null)
			config.setClassNameRoot((String) o);
		if((o = parser.getOptionValue(oP)) != null)
			config.setPackageName((String) o);
		if((o = parser.getOptionValue(oO)) != null)
			config.setOutputDirectory((String) o);
		if((o = parser.getOptionValue(oB)) != null)
			setBinaryReaderPath((String)o);
	}
	
	protected abstract void printUsage();

	/**
	 * @return the entryPoint
	 */
	public String getEntryPoint() {
		return config.getEntryPoint();
	}

	/**
	 * @param entryPoint the entryPoint to set
	 */
	public void setEntryPoint(String entryPoint) {
		config.setEntryPoint(entryPoint);
	}
	
	public List<String> getFunctionLoadList() {
		if(config.getFunctionLoadList() == null) {
			ArrayList<String> functionLoadList = new ArrayList<String>();
			if(config.getEntryPoint() != null)
				functionLoadList.add(config.getEntryPoint());
			config.setFunctionLoadList(functionLoadList);
		}
		return config.getFunctionLoadList();
	}
	
	public void setFunctionLoadList(List<String>newlist) {
		config.setFunctionLoadList(newlist);
	}
	
	public void setFunctionLoadList(String list) {
		config.setFunctionLoadList(list);
	}

	/**
	 * @return the maxProgramSteps
	 */
	public int getMaxProgramSteps() {
		return config.getMaxProgramSteps();
	}

	/**
	 * @param maxProgramSteps the maxProgramSteps to set
	 */
	public void setMaxProgramSteps(int maxProgramSteps) {
		config.setMaxProgramSteps(maxProgramSteps);
	}

	/**
	 * @return the honorDelaySlots
	 */
	public boolean isHonorDelaySlots() {
		return config.isHonorDelaySlots();
	}

	/**
	 * @param honorDelaySlots the honorDelaySlots to set
	 */
	public void setHonorDelaySlots(boolean honorDelaySlots) {
		config.setHonorDelaySlots(honorDelaySlots);
	}
	
	protected Configuration getConfiguration() {
		return config;
	}

	/**
	 * @return the binaryReaderPath
	 */
	public String getBinaryReaderPath() {
		return binaryReaderPath;
	}

	/**
	 * @param binaryReaderProgramName the binaryReaderProgramName to set
	 */
	public void setBinaryReaderPath(String binaryReaderPath) {
		this.binaryReaderPath = binaryReaderPath;
	}

	/**
	 * @return the mipsBinaryFileName
	 */
	public String getMipsBinaryPath() {
		return mipsBinaryPath;
	}

	/**
	 * @param mipsBinaryFileName the mipsBinaryFileName to set
	 */
	public void setMipsBinaryPath(String mipsBinaryPath) {
		this.mipsBinaryPath = mipsBinaryPath;
	}
}
