// Copyright (C) 2014 by Xiao Shaun Wang <wangxiao@cs.umd.edu>
package oram;

import flexsc.CompEnv;
import flexsc.Mode;
import flexsc.Party;

public abstract class TreeBasedOramParty<T> extends OramParty<T> {
	public Block<T>[][] tree;
	protected int capacity;
	Block<T> blo;
	public TreeBasedOramParty(CompEnv<T> env, int N, int dataSize, int capacity) {
		super(env, N, dataSize);
		this.capacity = capacity;

		if (env.getMode() != Mode.COUNT) {
			tree = new Block[this.N][capacity];

			PlainBlock b = getDummyBlock(p == Party.Alice);
			blo = prepareBlock(b, b);

			for (int i = 0; i < this.N; ++i)
				for (int j = 0; j < capacity; ++j)
					tree[i][j] = blo;
		}
	}

	protected Block<T>[][] getPath(boolean[] path) {
		Block<T>[][] result = new Block[logN][];
		if (env.getMode() == Mode.COUNT) {
			for (int i = 0; i < logN; ++i) {
				result[i] = new Block[capacity];
				for (int j = 0; j < capacity; ++j)
					result[i][j] = blo;
			}
			return result;
		}
		int index = 1;
		result[0] = tree[index];
		for (int i = 1; i < logN; ++i) {
			index *= 2;
			if (path[lengthOfPos - i])
				++index;
			result[i] = tree[index];
		}
		return result;
	}

	protected void putPath(Block<T>[][] blocks, boolean[] path) {
		if (env.getMode() == Mode.COUNT)
			return;
		int index = 1;
		tree[index] = blocks[0];
		for (int i = 1; i < logN; ++i) {
			index *= 2;
			if (path[lengthOfPos - i])
				++index;
			tree[index] = blocks[i];
		}
	}
}
