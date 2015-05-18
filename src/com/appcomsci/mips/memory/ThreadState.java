package com.appcomsci.mips.memory;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

/**
 * Tracks the state of a execution thread.
 * 
 * Delayed instructions:  If we are supposed to
 * execute a delayed instruction, we push the address
 * on the stack and set the delayed flag.
 * 
 * @author Allen McIntosh
 */

public class ThreadState {
	/**
	 * counter so each thread can be given a unique ID
	 */
	private static int idCounter = 0;
	
	/**
	 * Unique thread id
	 */
	private int id;
	/**
	 * The thread call stack
	 */
	private Deque<Long> stack;
	/**
	 * Will we be executing a delayed instruction on the next cycle.
	 */
	private boolean delayed;
	
	/**
	 * Standard constructor.  Push the spin address and the address of the
	 * first instruction.
	 * @param programCounter
	 */
	public ThreadState(long programCounter) {
		stack = new ArrayDeque<Long>();
		stack.push(MipsInstructionSet.getSpinAddress());
		stack.push(programCounter);
		id = idCounter++;
	}
	
	/** Copy constructor
	 * 
	 * @param that
	 */
	public ThreadState(ThreadState that) {
		stack = new ArrayDeque<Long>(that.stack);
		delayed = that.isDelayed();
		id = idCounter++;
	}
	
	public Object clone() {
		return new ThreadState(this);
	}
	
	public boolean equals(Object o) {
		if(o == null || !(o instanceof ThreadState))
			return false;
		ThreadState that = (ThreadState) o;
		if(this.delayed != that.delayed)
			return false;
		if(this.stack.size() != that.stack.size())
			return false;
		
		// Default equals() method is by reference.
		Iterator<Long> thisI = this.stack.iterator();
		Iterator<Long> thatI = that.stack.iterator();
		while(thisI.hasNext()) {
			if(!thisI.next().equals(thatI.next())) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Push an address on the call stack
	 * @param calledAddress
	 */
	public void pushAddress(long calledAddress) {
		stack.push(calledAddress);
	}
	
	/**
	 * Pop an address off the call stack and return it.
	 * @return The address popped off the stack
	 */
	public long popAddress() {
		return stack.pop();
	}
	
	/**
	 * Get the current address from the call stack.
	 * @return The current thread address.
	 */
	public long getCurrentAddress() {
		return stack.peekFirst();
	}
	
	/** "Execute" one instruction
	 * 
	 */
	public void advance() {
		if(getCurrentAddress() == MipsInstructionSet.getSpinAddress())
			return;
		
		// Delayed execution pushes the addr of the delayed
		// instruction on the stack if control flow will not
		// be sequential.  Pop it off and continue.
		
		if(isDelayed()) {
			stack.removeFirst();
			delayed = false;
		} else {
			stack.push(stack.removeFirst()+4);
		}
	}

	/**
	 * @return Is this instruction delayed?
	 */
	public boolean isDelayed() {
		return delayed;
	}
	
	/** Schedule the next instruction as delayed,
	 * then execute a return (which will happen
	 * automagically when the delay address is
	 * popped)
	 * 
	 */
	public void doDelay() {
//System.err.println("doDelay() " + Long.toHexString(stack.peekFirst()));
		stack.push(stack.removeFirst()+4);
		this.delayed = true;
	}
	
	/** Schedule the next instruction as delayed, then
	 * continue on at the target address
	 * @param target
	 */
	public void doDelay(long target) {
		long dly = stack.removeFirst()+4;
		stack.push(target);
		stack.push(dly);
		this.delayed = true;
	}
	
	/** Do a call, including the delay slot
	 * 
	 * @param target
	 */
	public void doCall(long target) {
		long dly = stack.removeFirst();
		stack.push(dly+4);
		stack.push(target);
		stack.push(dly);
		this.delayed = true;
	}

	/**
	 * @return the stack
	 */
	public Deque<Long> getStack() {
		return stack;
	}
	
	/**
	 * Get the thread ID
	 * @return The ID
	 */
	public int getId() {
		return id;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder("Thread: " + id);
		sb.append(" Addr: " + Long.toHexString(getCurrentAddress()));
		if(delayed) sb.append(" delayed");
		return sb.toString();
	}
}
