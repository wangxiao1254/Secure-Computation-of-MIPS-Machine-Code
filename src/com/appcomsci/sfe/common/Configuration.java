/**
 * 
 */
package com.appcomsci.sfe.common;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import com.appcomsci.mips.memory.MipsInstructionSet;

/**
 * @author Allen McIntosh
 *
 */
public class Configuration {
	
	public static final String PROPERTY_FILE = "emulator.properties";
	private static final String DEFAULT_PROPERTY_FILE = "emulator.properties";
	
	public static final String READER_PATH_PROPERTY = "binary.reader.path";
	public static final String DEFAULT_READER_PATH = "/opt/mipsel/usr/bin/mipsel-linux-gnu-llvm-objdump";
	
	public static final String ENTRY_POINT_PROPERTY = "entry.point";
	public static final String DEFAULT_ENTRY_POINT = "sfe_main";
	
	public static final String EMULATOR_CLIENT_DIR_PROPERTY = "emulator.client.dir";
	public static final String DEFAULT_EMULATOR_CLIENT_DIR = ".";
	
	public static final String EMULATOR_SERVER_DIR_PROPERTY = "emulator.server.dir";
	public static final String DEFAULT_EMULATOR_SERVER_DIR = ".";
	
	public static final String FUNCTION_LOAD_LIST_PROPERTY = "function.load.list";
	
	public static final String SERVER_NAME_PROPERTY = "server.name";
	public static final String DEFAULT_SERVER_NAME = "localhost";
	
	public static final String MAX_PROGRAM_STEPS_PROPERTY = "max.program.steps";
	public static final int DEFAULT_MAX_PROGRAM_STEPS = 1000;
	
	public static final String HONOR_DELAY_SLOTS_PROPERTY = "honor.delay.slots";
	public static final boolean DEFAULT_HONOR_DELAY_SLOTS = false;
	
	public static final String MULTIPLE_BANKS_PROPERTY = "multiple.banks";
	public static final boolean DEFAULT_MULTIPLE_BANKS = true;
	
	public static final String CLASS_NAME_ROOT_PROPERTY = "class";
	public static final String DEFAULT_CLASS_NAME_ROOT = "Cpu";
	
	public static final String PACKAGE_NAME_PROPERTY = "package";
	public static final String DEFAULT_PACKAGE_NAME = "compiledlib.dov";
	
	public static final String OUTPUT_DIRECTORY_PROPERTY = "output.directory";
	public static final String DEFAULT_OUTPUT_DIRECTORY = ".";
	
	public static final String MIPS_BINARY_PATH_PROPERTY = "mips.binary.path";

	private String entryPoint;
	private String emulatorClientDir;
	private String emulatorServerDir;
	private String serverName;
	private String binaryReaderPath;
	private int maxProgramSteps;
	private boolean honorDelaySlots;
	private boolean multipleBanks;
	private String classNameRoot;
	private String packageName;
	private String outputDirectory;
	private String mipsBinaryPath;
	
	private ArrayList<String> functionLoadList;	
	
	private SfeProperties properties = null;
	
	/** Standard constructor.
	 * Initializes from a property file specified via -D, or from the default
	 * property file if no property file was given.
	 * @throws IOException
	 */
	public Configuration() throws IOException {
		String props = System.getProperty(PROPERTY_FILE, DEFAULT_PROPERTY_FILE);
		InputStream resourceStream = null;
		try {
//			System.out.println("Working Directory = " +
//		              System.getProperty("user.dir"));
			resourceStream = new FileInputStream(props);			
		} catch(FileNotFoundException e) {
			resourceStream =  Configuration.class.getClassLoader().getResourceAsStream(props);
		}
		if(resourceStream == null) {
			properties = new SfeProperties();
		} else {
			properties = new SfeProperties(resourceStream);
			resourceStream.close();
		}
		
		entryPoint = properties.getProperty(ENTRY_POINT_PROPERTY, DEFAULT_ENTRY_POINT);
		emulatorClientDir = properties.getProperty(EMULATOR_CLIENT_DIR_PROPERTY, DEFAULT_EMULATOR_CLIENT_DIR);
		emulatorServerDir = properties.getProperty(EMULATOR_SERVER_DIR_PROPERTY, DEFAULT_EMULATOR_SERVER_DIR);
		serverName = properties.getProperty(SERVER_NAME_PROPERTY, DEFAULT_SERVER_NAME);
		binaryReaderPath = properties.getProperty(READER_PATH_PROPERTY, DEFAULT_READER_PATH);
		maxProgramSteps = properties.getProperty(MAX_PROGRAM_STEPS_PROPERTY, DEFAULT_MAX_PROGRAM_STEPS);
		honorDelaySlots = properties.getProperty(HONOR_DELAY_SLOTS_PROPERTY, DEFAULT_HONOR_DELAY_SLOTS);
		multipleBanks = properties.getProperty(MULTIPLE_BANKS_PROPERTY, DEFAULT_MULTIPLE_BANKS);
		classNameRoot = properties.getProperty(CLASS_NAME_ROOT_PROPERTY, DEFAULT_CLASS_NAME_ROOT);
		packageName = properties.getProperty(PACKAGE_NAME_PROPERTY, DEFAULT_PACKAGE_NAME);
		outputDirectory = properties.getProperty(OUTPUT_DIRECTORY_PROPERTY, DEFAULT_OUTPUT_DIRECTORY);
		mipsBinaryPath = properties.getProperty(MIPS_BINARY_PATH_PROPERTY);
		initFunctionLoadList(properties);	
	}
	
	public Configuration(Configuration that) {
		this.setEntryPoint(that.getEntryPoint());
		this.setEmulatorClientDir(that.getEmulatorClientDir());
		this.setEmulatorServerDir(that.getEmulatorServerDir());
		this.setServerName(that.getServerName());
		this.setBinaryReaderPath(that.getBinaryReaderPath());
		this.setMaxProgramSteps(that.getMaxProgramSteps());
		this.setHonorDelaySlots(that.isHonorDelaySlots());
		this.setClassNameRoot(that.getClassNameRoot());
		this.setPackageName(that.getPackageName());
		this.setOutputDirectory(that.getOutputDirectory());
		this.setMipsBinaryPath(that.getMipsBinaryPath());
		functionLoadList = new ArrayList<String>();
		for(String s:that.getFunctionLoadList()) {
			functionLoadList.add(s);
		}
	}
	
	private void initFunctionLoadList(SfeProperties properties) {
		String s = properties.getProperty(FUNCTION_LOAD_LIST_PROPERTY);
		if(s == null) {
			// Default to entry point if no load list given
			setFunctionLoadList(entryPoint);
		} else {
			setFunctionLoadList(s);
		}
	}
	
	public String getEntryPoint() {
		return entryPoint;
	}
	
	public void setEntryPoint(String entryPoint) {
		this.entryPoint = entryPoint;
	}
	
	public String getBinaryReaderPath() {
		return binaryReaderPath;
	}
	
	public void setBinaryReaderPath(String binaryReaderPath) {
		this.binaryReaderPath = binaryReaderPath;
	}
	
	
	public List<String> getFunctionLoadList() {
		return functionLoadList;
	}
	
	public void setFunctionLoadList(List<String>newlist) {
		this.functionLoadList = new ArrayList<String>(newlist);
		if(!functionLoadList.contains(MipsInstructionSet.SPIN_SYMBOL)) {
			functionLoadList.add(MipsInstructionSet.SPIN_SYMBOL);
		}
	}

	public void setFunctionLoadList(String list) {
		String fcns[] = list.split("[, ]+", 0);
		// This ugliness because the result of Arrays.asList() does not do add
		// (even though it says it is an ArrayList)
		functionLoadList = new ArrayList<String>(Arrays.asList(fcns));
		if(!functionLoadList.contains(MipsInstructionSet.SPIN_SYMBOL)) {
			functionLoadList.add(MipsInstructionSet.SPIN_SYMBOL);
		}
	}
	
	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public String getEmulatorClientDir() {
		return emulatorClientDir;
	}

	public void setEmulatorClientDir(String emulatorClientDir) {
		this.emulatorClientDir = emulatorClientDir;
	}

	public String getEmulatorServerDir() {
		return emulatorServerDir;
	}

	public void setEmulatorServerDir(String emulatorServerDir) {
		this.emulatorServerDir = emulatorServerDir;
	}

	/**
	 * @return the maxProgramSteps
	 */
	public int getMaxProgramSteps() {
		return maxProgramSteps;
	}

	/**
	 * @param maxProgramSteps the maxProgramSteps to set
	 */
	public void setMaxProgramSteps(int maxProgramSteps) {
		this.maxProgramSteps = maxProgramSteps;
	}

	/**
	 * @return the honorDelaySlots
	 */
	public boolean isHonorDelaySlots() {
		return honorDelaySlots;
	}

	/**
	 * @param honorDelaySlots the honorDelaySlots to set
	 */
	public void setHonorDelaySlots(boolean honorDelaySlots) {
		this.honorDelaySlots = honorDelaySlots;
	}

	/**
	 * @return the multipleBanks
	 */
	public boolean isMultipleBanks() {
		return multipleBanks;
	}

	/**
	 * @param multipleBanks the multipleBanks to set
	 */
	public void setMultipleBanks(boolean multipleBanks) {
		this.multipleBanks = multipleBanks;
	}

	/**
	 * @return the properties
	 */
	public SfeProperties getProperties() {
		return properties;
	}

	/**
	 * @return the className
	 */
	public String getClassNameRoot() {
		return classNameRoot;
	}

	/**
	 * @param classNameRoot the classNameRoot to set
	 */
	public void setClassNameRoot(String classNameRoot) {
		this.classNameRoot = classNameRoot;
	}

	/**
	 * @return the packageName
	 */
	public String getPackageName() {
		return packageName;
	}

	/**
	 * @param packageName the packageName to set
	 */
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	/**
	 * @return the outputDirectory
	 */
	public String getOutputDirectory() {
		return outputDirectory;
	}

	/**
	 * @param outputDirectory the outputDirectory to set
	 */
	public void setOutputDirectory(String outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

	/**
	 * @return the mipsBinaryPath
	 */
	public String getMipsBinaryPath() {
		return mipsBinaryPath;
	}

	/**
	 * @param mipsBinaryPath the mipsBinaryPath to set
	 */
	public void setMipsBinaryPath(String mipsBinaryPath) {
		this.mipsBinaryPath = mipsBinaryPath;
	}

}
