// Copyright (C) 2014 by Xiao Shaun Wang <wangxiao@cs.umd.edu>
package oram;

import util.Utils;
import circuits.arithmetic.IntegerLib;
import flexsc.CompEnv;

public class Register<T> {
	public T[][] content;
	CompEnv<T> env;
	public int lengthOfIden;
	public IntegerLib<T> lib;
	public int dataSize;
	public Register(CompEnv<T> env, int N, int dataSize) {
		this.env = env;
		this.dataSize = dataSize;
		lib = new IntegerLib<T>(env);
		content = env.newTArray(N, 0);
		lengthOfIden = Utils.log2(N);
		for(int i = 0; i < N; ++i)
			content[i] = lib.zeros(dataSize);
	}

	public T[] read(int index) {
		return content[index];
	}

	public void write(int index, T[] d) {
		content[index] = d;
	}

	public T[] read(T[] iden) {
		return read(iden, 0xFFFFFFFF);
	}
	
	public T[] read(T[] iden, int mask) {
		boolean[] BoolMask = Utils.fromInt(mask, 32);
		T[] iden1 = lib.padSignal(iden, lengthOfIden);
		T[] res = lib.zeros(content[0].length);
		for(int i = 0; i < content.length; ++i) {
			if(false || BoolMask[i]) {
				T eq = lib.eq(iden1, lib.toSignals(i, lengthOfIden));
				res = lib.mux(res, content[i],  eq);
			}
		}
		return res;
	}

	public void write(T[] iden, T[] data, int mask) {
		boolean[] BoolMask = Utils.fromInt(mask, 32);
		T[] iden1 = lib.padSignal(iden, lengthOfIden);
		for(int i = 0; i < content.length; ++i) {
			if( false || BoolMask[i]) {
				T eq = lib.eq(iden1, lib.toSignals(i, lengthOfIden));
				content[i] = lib.mux(content[i], data, eq);
			}
		}
	}
	
	public void write(T[] iden, T[] data) {
		write(iden, data, 0xFFFFFFFF);
	}
	public void write(T[] iden, T[] data, T dummy) {
		write(iden, data, 0xFFFFFFFF, dummy);
	}
	
	public void write(T[] iden, T[] data, int mask, T dummy) {
		boolean[] BoolMask = Utils.fromInt(mask, 32);
		T[] iden1 = lib.padSignal(iden, lengthOfIden);
		for(int i = 0; i < content.length; ++i) {
			if(false || BoolMask[i]) {
				T eq = lib.eq(iden1, lib.toSignals(i, lengthOfIden));
				eq = lib.and(eq, dummy);
				content[i] = lib.mux(content[i], data, eq);
			}
		}
	}
}
