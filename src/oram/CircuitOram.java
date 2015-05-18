// Copyright (C) 2014 by Xiao Shaun Wang <wangxiao@cs.umd.edu>
package oram;

import java.util.Arrays;

import flexsc.CompEnv;
import flexsc.Party;

public class CircuitOram<T> extends TreeBasedOramParty<T> {
	public CircuitOramLib<T> lib;
	Block<T>[] scQueue;
	int cnt = 0;
	public int queueCapacity;

	boolean[] nextPath() {
		boolean[] res = new boolean[logN];
		int temp = cnt;
		for (int i = res.length - 1; i >= 0; --i) {
			res[i] = (temp & 1) == 1;
			temp >>= 1;
		}
		cnt = (cnt + 1) % N;
		return res;
	}

	public CircuitOram(CompEnv<T> env, int N, int dataSize, int cap, int sp) {
		super(env, N, dataSize, cap);
		lib = new CircuitOramLib<T>(lengthOfIden, lengthOfPos, lengthOfData,
				logN, capacity, env);
		queueCapacity = 30;
		PlainBlock[]queue = new PlainBlock[queueCapacity];

		for (int i = 0; i < queue.length; ++i)
			queue[i] = getDummyBlock(p == Party.Alice);

		scQueue = prepareBlocks(queue, queue);
	}

	public CircuitOram(CompEnv<T> env, int N, int dataSize) {
		super(env, N, dataSize, 3);
		lib = new CircuitOramLib<T>(lengthOfIden, lengthOfPos, lengthOfData,
				logN, capacity, env);
		queueCapacity = 30;
		PlainBlock[] queue = new PlainBlock[queueCapacity];

		for (int i = 0; i < queue.length; ++i)
			queue[i] = getDummyBlock(p == Party.Alice);

		scQueue = prepareBlocks(queue, queue);

	}

	protected void ControlEviction() {
		flushOneTime(nextPath());
		flushOneTime(nextPath());
	}

	public void flushOneTime(boolean[] pos) {
		Block<T>[][] scPath = getPath(pos);
		lib.flush(scPath, pos, scQueue);
		putPath(scPath, pos);
	}

	public T[] readAndRemove(T[] scIden, boolean[] pos,
			boolean RandomWhenNotFound) {
		Block<T>[][] scPath = getPath(pos);

		Block<T> res = lib.readAndRemove(scPath, scIden);
		Block<T> res2 = lib.readAndRemove(scQueue, scIden);
		res = lib.mux(res, res2, res.isDummy);

		putPath(scPath, pos);

		if (RandomWhenNotFound) {
			return lib.mux(res.data, lib.randBools(res.data.length), res.isDummy);
		} else {
			return lib.mux(res.data, lib.zeros(res.data.length), res.isDummy);
		}
	}

	public T[] readAndRemove(T[] scIden, T[] pos, T condition) {
		scIden = Arrays.copyOf(scIden, lengthOfIden);
		T[] scPos = Arrays.copyOf(pos, lengthOfPos);
		T[] randbools = lib.randBools(scPos.length);
		T[] posToUse = lib.mux(randbools, scPos, condition);

		boolean[] path = lib.declassifyToBoth(posToUse);
		Block<T>[][] scPath = getPath(path);

		Block<T> res = lib.conditionalReadAndRemove(scPath, scIden, condition);
		Block<T> res2 = lib.conditionalReadAndRemove(scQueue, scIden, condition);
		res = lib.mux(res, res2, res.isDummy);

		putPath(scPath, path);
		return lib.mux(res.data, lib.toSignals(0, res.data.length), res.isDummy);
	}

	
	public void putBack(T[] scIden, T[] scNewPos, T[] scData) {
		Block<T> b = new Block<T>(scIden, scNewPos, scData, lib.SIGNAL_ZERO);
		lib.add(scQueue, b);

		ControlEviction();
	}
	
	public void putBack(T[] scIden, T[] scNewPos, T[] scData, T dummy) {
		Block<T> b = new Block<T>(scIden, scNewPos, scData, lib.SIGNAL_ZERO);
		lib.conditionalAdd(scQueue, b, dummy);

		ControlEviction();
	}

	public T[] read(T[] scIden, boolean[] pos, T[] scNewPos) {
		scIden = Arrays.copyOf(scIden, lengthOfIden);
		T[] r = readAndRemove(scIden, pos, false);
		putBack(scIden, scNewPos, r);
		return r;
	}

	public void write(T[] scIden, boolean[] pos, T[] scNewPos, T[] scData) {
		scIden = Arrays.copyOf(scIden, lengthOfIden);
		readAndRemove(scIden, pos, false);
		putBack(scIden, scNewPos, scData);
	}
	
	public void write(T[] scIden, T[] pos, T[] scNewPos, T[] scData, T dummy) {
		scIden = Arrays.copyOf(scIden, lengthOfIden);
		readAndRemove(scIden, pos, dummy);
		putBack(scIden, scNewPos, scData, dummy);
	}
}
