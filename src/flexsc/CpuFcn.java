/**
 * 
 */
package flexsc;

import java.util.Set;

import oram.Register;
import oram.SecureArray;

/**
 * @author Allen McIntosh
 *
 */
public interface CpuFcn<T> {
	public T[] function(Register<T> reg, T[] inst, T[]pc, T[][] hiLo, T terminationBit, int[] masks) throws Exception;
	public Set<String> getOpcodesImplemented();
}
