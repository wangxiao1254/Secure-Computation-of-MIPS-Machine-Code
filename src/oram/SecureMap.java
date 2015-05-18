// Copyright (C) 2014 by Xiao Shaun Wang <wangxiao@cs.umd.edu>
package oram;

import java.util.TreeMap;

import flexsc.CompEnv;

public class SecureMap<T> {
	int threshold = 256;
	boolean useTrivialOMap = false;
	public TrivialObliviousMap<T> tmap = null;
	public RecursiveCircuitOram<T> circuitOram = null;
	public int lengthOfIden;
	CompEnv<T> env;


	//keysize = valuesize = 32;
	public SecureMap(CompEnv<T> env, int N, int U) throws Exception {
		useTrivialOMap = true;//for now;
		if (useTrivialOMap) {
			tmap = new TrivialObliviousMap<T>(env);
		} else {
			circuitOram = new RecursiveCircuitOram<T>(env, N, 32);
		}
		this.env = env;
	}
	public SecureMap(CompEnv<T> env, int N, int U, int thresh) throws Exception {
		this.threshold = thresh;
		useTrivialOMap = true;//for now;
		if (useTrivialOMap) {
			tmap = new TrivialObliviousMap<T>(env);
		} else {
			circuitOram = new RecursiveCircuitOram<T>(env, N, 32);
		}
		this.env = env;
	}
	public void init(TreeMap<Long,boolean[]> m, int indexLength, int dataLength) {
		if(useTrivialOMap)
			tmap.init(m, indexLength, dataLength);
		else{
			//use circuit oram
		}
	}
	public void init(int cap, int indexLength, int dataLength) {
		if(useTrivialOMap)
			tmap.init(cap, indexLength, dataLength);
		else{
			//use circuit oram
		}
	}
	
	public T[] read(T[] scIden) {
		if(useTrivialOMap)
			return tmap.read(scIden);
		else{
			//use circuit oram
			return null;
		}
	}
	public void print(){
		if (useTrivialOMap)
			tmap.print();
	}
	
}