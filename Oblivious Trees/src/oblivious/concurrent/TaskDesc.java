package oblivious.concurrent;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

class TaskDesc {
	protected static enum OpType { 
		INSERT,	// chunk insert operation
		DELETE,	// chunk delete operation
		GENSIG,	// output current signature operation
		VOID	// no operation (used to indicate that current task is clear)
	} 
	
	// Instance variables
	protected OpType operation;	// the operation to be performed
	protected int index;	//
	protected byte[] data;	//
	protected AtomicReference<DescStatus> status;	//
	
	// Constructors
	/** construct new Task Descriptor, default constructor
	 */
	public TaskDesc(){
		this.operation = TaskDesc.OpType.VOID;
	}
	/** construct new Task Descriptor
	 *  @param o type of operation
	 */
	public TaskDesc(OpType o){
		this.operation = o;
	}
	/** construct new Task Descriptor
	 *  @param o type of operation
	 *  @param i location in leaf set to perform
	 */
	public TaskDesc(OpType o, int i){
		this.operation = o;
		this.index = i;
	}
	/** construct new Task Descriptor
	 *  @param o type of operation
	 *  @param i location in leaf set to perform
	 *  @param d data to be inserted
	 */
	public TaskDesc(OpType o, int i, byte[] d){
		this.operation = o;
		this.index = i;
		this.data = Arrays.copyOf(d, d.length);
	}
	
}
