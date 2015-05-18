package mips;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.appcomsci.sfe.common.Configuration;

import flexsc.Mode;
import flexsc.Party;

public class LocalConfiguration extends Configuration {
	public Mode mode = Mode.VERIFY;
	public int aliceInputSize = 0;
	public int bobInputSize = 0;
	public int[] aliceInput = null;
	public int[] bobInput = null;
	public int[][] aliceInput2D = null;
	public int[][] bobInput2D = null;
	public boolean is2Ddata;
	public int stackFrameSize = 0;

	public String ServerAddress;
	public int ServerPort;
	protected LocalConfiguration(Party party) throws IOException {
		super();
		is2Ddata = false;
		mode = Mode.valueOf(getProperties().getProperty("mode"));
		aliceInputSize = new Integer(getProperties().getProperty("alice_input_size"));
		bobInputSize = new Integer(getProperties().getProperty("bob_input_size"));
		ServerAddress = getProperties().getProperty("Server.address");
		ServerPort = new Integer(getProperties().getProperty("Server.port"));
		stackFrameSize = new Integer(getProperties().getProperty("stack_frame_size"));
		if(party == Party.Alice)
			aliceInput = readArrayFromFile(getProperties().getProperty("alice_input_file"), aliceInputSize);
		else 
			bobInput = readArrayFromFile(getProperties().getProperty("bob_input_file"), bobInputSize);
	}

	private int[] readArrayFromFile(String file, int length) throws IOException {
		List<String> lines = Files.readAllLines(Paths.get(file));
		int[] data = new int[length];
		for(int i = 0; i < data.length; ++i)
			data[i] = new Integer(lines.get(i));
		return data;
	}
	
	protected LocalConfiguration(LocalConfiguration that) throws IOException {
		super(that);
		this.mode = that.mode;
	}
}