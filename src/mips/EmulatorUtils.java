package mips;

import oram.Memory;
import oram.Register;
import oram.SecureArray;
import circuits.arithmetic.IntegerLib;
import flexsc.Mode;
import flexsc.Party;
import gc.BadLabelException;

public class EmulatorUtils {
	
	public static<T> boolean checkMatchBooleanArray(T[] array, IntegerLib<T> lib, int matchVal) throws Exception{
		if(lib.getEnv().mode == Mode.REAL || lib.getEnv().mode == Mode.OPT)
			return true;
		boolean[] temp = lib.getEnv().outputToAlice(array);
		boolean match = true;
		if (lib.getEnv().getParty() == Party.Alice){
			for (int i = 31; i >=0; i--){
				if (!temp[i] && ((matchVal & (1 << i)) != 0))
					match = false;
				else if (temp[i] && ((matchVal & (1 << i)) == 0))
					match = false;
			}
			//System.out.println("Alice Match = " + match);
			lib.getEnv().channel.writeInt(match ? 1 : 0);
		}
		else{
			match = (lib.getEnv().channel.readInt() == 1);
			//System.out.println("Bob Match: " + match);
		}
		return match;
	}
	
	public static<T> void print(String s, IntegerLib<T> lib, boolean smart) {
		if((lib.getEnv().mode == Mode.REAL || lib.getEnv().mode == Mode.OPT ) || smart)
			return;
		if(lib.getEnv().getParty() == Party.Alice)
			System.out.println(s);
	}

	public static<T> void print(String s, IntegerLib<T> lib) {
		print(s, lib, true);
	}

	
	public static<T> void printBooleanArray(String s, T[] array, IntegerLib<T> lib){
		printBooleanArray(s, array, lib, true);
	}
	public static<T> void printBooleanArray(String s, T[] array, IntegerLib<T> lib, boolean smart){
		if((lib.getEnv().mode == Mode.REAL || lib.getEnv().mode == Mode.OPT ) && smart)
			return;
		String output = s+":";
		boolean[] temp = lib.getEnv().outputToAlice(array);

		for (int i = array.length -1 ; i >= 0;  i--){
					output += temp[i] ? "1" : "0"; 
		}
output+=" ";for(int i=array.length;i>0;i-=4)output+=String.format("%x", (temp[i-1]?8:0)|(temp[i-2]?4:0)|(temp[i-3]?2:0)|(temp[i-4]?1:0));
		if(lib.getEnv().getParty() == Party.Alice)
			System.out.println(output);
		
	}
	public static<T> void printRegisters(Register<T> reg, IntegerLib<T> lib) throws BadLabelException{
		if(lib.getEnv().mode == Mode.REAL || lib.getEnv().mode == Mode.OPT)
			return;
		String output = "";
		T[] temp; 

		for (int i = 0 ; i < 32; i++){
			output += "|reg" + i + ": ";
			temp = reg.read(lib.toSignals(i, reg.lengthOfIden));
			boolean[] tmp = lib.getEnv().outputToAlice(temp);
			//if (lib.getEnv().getParty() == Party.Alice)
				//System.out.println(Utils.toInt(tmp));
			for (int j = 31 ; j >= 0 ; j--){
				output += (tmp[j] ? "1" : "0");
			}	
			if (i % 3 == 0)
				output += "\n";
		}
		if(lib.getEnv().getParty() == Party.Alice)
			System.out.println(output);
	}
	
	public static<T> void printOramBank(Memory<T> oramBank, IntegerLib<T> lib, int numItems){
		if(lib.getEnv().mode == Mode.REAL || lib.getEnv().mode == Mode.OPT)
			return;
		String output = "";
		T[] temp; 
		
		for (int i = 0 ; i < numItems; i++){
			output += "item number " + String.valueOf(i) +": ";
			temp = oramBank.read(lib.toSignals(i, oramBank.lengthOfIden));
			boolean[] tmp = lib.getEnv().outputToAlice(temp);
			//if (lib.getEnv().getParty() == Party.Alice)
				//System.out.println(Utils.toInt(tmp));
			for (int j = tmp.length-1 ; j >= 0 ; j--){
				output += (tmp[j] ? "1" : "0");
			}	
			output += "\n";
		}
		if(lib.getEnv().getParty() == Party.Alice)
			System.out.println(output);
	}
	
	public static<T> void printOramBank(SecureArray<T> oramBank, IntegerLib<T> lib, int numItems) throws BadLabelException{
		if(lib.getEnv().mode == Mode.REAL || lib.getEnv().mode == Mode.OPT)
			return;
		String output = "";
		T[] temp; 
		
		for (int i = 0 ; i < numItems; i++) {
			output += "item number " + String.valueOf(i) +": ";
			temp = oramBank.read(lib.toSignals(i, oramBank.lengthOfIden));
			boolean[] tmp = lib.getEnv().outputToAlice(temp);
			//if (lib.getEnv().getParty() == Party.Alice)
				//System.out.println(Utils.toInt(tmp));
			for (int j = tmp.length-1 ; j >= 0 ; j--){
				output += (tmp[j] ? "1" : "0");
			}	
			output += "\n";
		}
		if(lib.getEnv().getParty() == Party.Alice)
			System.out.println(output);
	}
	
	public static int[] castStringToIntArray(String str) {
		int arrLen = str.length()/4+1;
		int dangle = str.length() - 4*(str.length()/4);
		int[] ret = new int[arrLen];
		byte[] byteArr = str.getBytes();
		
		for (int i = 0; i < arrLen-1; i++){
			ret[i] = ((byteArr[i] << 24) | (byteArr[i+1] << 16) | (byteArr[i+3] << 8) | byteArr[i+3]);
		}
		for (int i = 0 ; i < dangle; i++)
			ret[arrLen-1] = ret[arrLen-1] | (byteArr[str.length() - dangle + i] << ((3-i)*8));  
		
		return ret;
	}
	
}
