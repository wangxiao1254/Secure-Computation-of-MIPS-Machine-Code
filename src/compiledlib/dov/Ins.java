package compiledlib.dov;
import java.security.SecureRandom;
import oram.SecureArray;
import oram.CircuitOram;
import flexsc.Mode;
import flexsc.Party;
import flexsc.CompEnv;
import java.util.BitSet;
import circuits.arithmetic.IntegerLib;
import circuits.arithmetic.FloatLib;
import util.Utils;
import gc.regular.GCEva;
import gc.regular.GCGen;
import gc.GCSignal;
import java.util.Arrays;
import java.util.Random;
import flexsc.IWritable;
import flexsc.Comparator;
import java.lang.reflect.Array;
public class Ins implements IWritable<Ins, Boolean> {

	public CompEnv<Boolean> env;
	public IntegerLib<Boolean> intLib;
	public FloatLib<Boolean> floatLib;

	public Ins(CompEnv<Boolean> env) throws Exception {
		this.env = env;
		this.intLib = new IntegerLib<Boolean>(env);
		this.floatLib = new FloatLib<Boolean>(env, 24, 8);
	}

	public int numBits() {
		return 0;
	}
	public Boolean[] getBits() {
		Boolean[] ret = new Boolean[this.numBits()];
		Boolean[] tmp_b;
		Boolean tmp;
		int now = 0;
		return ret;
}

	public Ins newObj(Boolean[] data) throws Exception {
		if(data == null) {
			data = new Boolean[this.numBits()];
			for(int i=0; i<this.numBits(); ++i) { data[i] = intLib.SIGNAL_ZERO; }
		}
		if(data.length != this.numBits()) return null;
		Ins ret = new Ins(env);
		Boolean[] tmp;
		int now = 0;
		return ret;
}

	public Boolean[] aaa(Boolean[] m, int n) throws Exception {
		Boolean[] ret = env.inputOfAlice(Utils.fromInt(0, 32));
		Boolean[] f_tmp_0 = intLib.leftPublicShift(m, n);
		Boolean[] __tmp0 = f_tmp_0;
		ret = __tmp0;
		return ret;
	}
}
