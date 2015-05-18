// Copyright (C) 2014 by Xiao Shaun Wang <wangxiao@cs.umd.edu>
package oram;

import java.util.Arrays;

import circuits.arithmetic.IntegerLib;
import flexsc.CompEnv;

public class Memory<T> {
	static final int threshold = 65536;
	boolean useTrivialOram = false;
	public LinearScanOram<T> trivialOram = null;
	public RecursiveCircuitOram<T> circuitOram = null;
	public int lengthOfIden;
	public int dataSize;
	public IntegerLib<T> lib;
	public int size = 0;
	public Memory(CompEnv<T> env, int N, int dataSize) {
		size = N;
		useTrivialOram = N <= threshold;
		this.dataSize = dataSize;
		if (useTrivialOram) {
			trivialOram = new LinearScanOram<T>(env, N, dataSize);
			lengthOfIden = trivialOram.lengthOfIden;
		} else {
			circuitOram = new RecursiveCircuitOram<T>(env, N, dataSize);
			lengthOfIden = circuitOram.lengthOfIden;
		}
		lib = new IntegerLib<T>(env);
	}

	public T[] readAndRemove(T[] iden)  {
		return circuitOram.clients.get(0).readAndRemove(iden, 
				Arrays.copyOfRange(circuitOram.clients.get(0).lib.declassifyToBoth(iden), 0, circuitOram.clients.get(0).lengthOfPos), false);
	}

	public T[] read(T[] iden, int operationMask)  {
		if ((operationMask&1)==0)
			return lib.zeros(dataSize);
		if (useTrivialOram)
			return trivialOram.read(iden);
		else
			return circuitOram.read(iden);
	}
	public T[] read(T[] iden)  {
		if (useTrivialOram)
			return trivialOram.read(iden);
		else
			return circuitOram.read(iden);
	}

	public void write(T[] iden, T[] data, int operationMask) {
		if (((operationMask>>1)&1)==0)return;
		if (useTrivialOram)
			trivialOram.write(iden, data);
		else
			circuitOram.write(iden, data);
	}

	public void write(T[] iden, T[] data) {
		if (useTrivialOram)
			trivialOram.write(iden, data);
		else
			circuitOram.write(iden, data);
	}

	public void conditionalWrite(T[] iden, T[]data, T condition, int operationMask)  {
		if (((operationMask>>1)&1)==0)return;
		if(useTrivialOram) {
			trivialOram.write(iden, data, condition);
		}
		else {
			circuitOram.write(iden, data, condition);
		}
	}
}
