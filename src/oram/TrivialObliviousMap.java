// Copyright (C) 2014 by Xiao Shaun Wang <wangxiao@cs.umd.edu>
package oram;

import java.util.Map.Entry;
import java.util.TreeMap;

import util.Utils;
import circuits.arithmetic.IntegerLib;
import flexsc.CompEnv;
import flexsc.Mode;
import flexsc.Party;

public class TrivialObliviousMap<T> {
	int capacity, dataSize, indexSize;
	CompEnv<T>  env;
	T[][] key; T[][] value;
	public IntegerLib<T> lib;
	
	public TrivialObliviousMap(CompEnv<T> env){
		this.env = env;
		lib = new IntegerLib<T>(env);
	}
	
	public void init(TreeMap<Long,boolean[]> m, int indexLength, int dataLength) {
		this.capacity = m.size();
		this.indexSize = indexLength;
		this.dataSize = dataLength;
		
		boolean[][] ckey = new boolean[capacity][0];
		boolean[][] cval = new boolean[capacity][0];
		int i = 0;
		for(Entry<Long, boolean[]>e: m.entrySet()) {
			ckey[i] = Utils.fromInt(e.getKey().intValue(), indexLength);
			cval[i] = e.getValue();
			i++;
		}
		if(env.getParty() == Party.Alice) {
			key = env.inputOfAlice(ckey);
			value = env.inputOfAlice(cval);
		}
		else {
			key = env.inputOfBob(ckey);
			value = env.inputOfBob(cval);
		}
	}
	
	public void init(int cap, int indexLength, int dataLength) {
		this.capacity = cap;
		this.indexSize = indexLength;
		this.dataSize = dataLength;
		if(env.getParty() == Party.Bob) {
			key = env.inputOfAlice(new boolean[capacity][indexLength]);
			value = env.inputOfAlice(new boolean[capacity][dataLength]);
		}
		else {
			key = env.inputOfBob(new boolean[capacity][indexLength]);
			value = env.inputOfBob(new boolean[capacity][dataLength]);
		}
	}

	public T[] read(T[] scIden) {
		scIden = lib.padSignedSignal(scIden, indexSize);
		T[] res = lib.zeros(dataSize);
		for(int i = 0; i < capacity; ++i) {
			T match = lib.eq(scIden, key[i]);
			res = lib.mux(res, value[i], match);
		}
		return res;
	}
	
	public void print(){
		if(lib.getEnv().mode == Mode.REAL || lib.getEnv().mode == Mode.OPT)
			return;
		String output = "";
		
		for (int i = 0; i < capacity; i++){
			boolean[] tmp = lib.getEnv().outputToAlice(value[i]); 
			output = "";
			output += "item number " + String.valueOf(i) +": ";
			for (int j = tmp.length-1 ; j >= 0 ; j--){
				output += (tmp[j] ? "1" : "0");
			}	
			//output += "\n";
			if (lib.getEnv().getParty() == Party.Alice)
				System.out.println(output);
		}
		
	}
}

