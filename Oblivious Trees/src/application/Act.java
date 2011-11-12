package application;

import java.util.Arrays;

public class Act implements Cloneable{
	
	// Instance variables
	private Runnable caller;
	private OpType operation;
	private long time;
	private int location;
	private byte[] data;
	
	public static enum OpType { 
		INSERT,	// chunk insert operation
		DELETE,	// chunk delete operation
		GENSIG,	// output current signature operation
		VOID	// no operation (used to indicate that current task is clear)
	}
	
	
	// Constructors
	
	/** Act default constructor
	 */
	public Act(){
		caller = null;
		operation = null;
		time = -1;
		location = -1;
		data = null;
	}
	/** Act constructor
	 * 	@param o operation being performed
	 *  @param t time of this action
	 */
	public Act(OpType o, long t){
		caller = null;
		operation = o;
		time = t;
		location = -1;
		data = null;
	}
	/** Act constructor
	 * 	@param o operation being performed
	 *  @param t time of this action
	 *  @param l location action is being performed
	 */
	public Act(OpType o, long t, int l){
		caller = null;
		operation = o;
		time = t;
		location = l;
		data = null;
	}
	/** Act Constructor
	 * 	@param o operation being performed
	 *  @param t time of this action
	 *  @param l location action is being performed
	 *  @param d data being sent into this action
	 */
	 public Act(OpType o, long t, int l, byte[] d){
		caller = null;
		operation = o;
		time = t;
		location = l;
		data = Arrays.copyOf(d, d.length);
	}
	/** Act constructor
	 * 	@param o operation being performed
	 *  @param t time of this action
	 */
	public Act(String o, long t){
		caller = null;
		operation = OpType.valueOf(o);
		time = t;
		location = -1;
		data = null;
	}
	/** Act constructor
	 * 	@param o operation being performed
	 *  @param t time of this action
	 *  @param l location action is being performed
	 */
	public Act(String o, long t, int l){
		caller = null;
		operation = OpType.valueOf(o);
		time = t;
		location = l;
		data = null;
	}
	/** Act Constructor
	 * 	@param o operation being performed
	 *  @param t time of this action
	 *  @param l location action is being performed
	 *  @param d data being sent into this action
	 */
	public Act(String o, long t, int l, byte[] d){
		caller = null;
		operation = OpType.valueOf(o);
		time = t;
		location = l;
		data = Arrays.copyOf(d, d.length);
	}
	
	// Mutators
	/** set Act caller
	 *  @param caller the caller to set
	 */
	public void setCaller(Runnable caller) {
		this.caller = caller;
	}
	/** set Act operation
	 * @param operation the operation to set
	 */
	public void setOperation(OpType operation) {
		this.operation = operation;
	}
	/** set Act operation
	 * @param operation the operation to set
	 */
	public void setOperation(String operation) {
		this.operation = OpType.valueOf(operation);
	}
	/** set Act time
	 * @param time the time to set
	 */
	public void setTime(long time) {
		this.time = time;
	}
	/** set Act location
	 * @param location the location to set
	 */
	public void setLocation(int location) {
		this.location = location;
	}
	/** set Act data
	 * @param data the data to set
	 */
	public void setData(byte[] data) {
		this.data = data;
	}
	// Inspectors
	/** get Act caller
	 *  @return the Act caller
	 */
	public Runnable getCaller() {
		return caller;
	}
	/** get Act operation
	 * @return the Act operation
	 */
	public OpType getOperation() {
		return operation;
	}
	/** get Act 
	 * @return the Act time
	 */
	public long getTime() {
		return time;
	}
	/** get Act location
	 * @return the Act location
	 */
	public int getLocation() {
		return location;
	}
	/** get Act data
	 * @return the Act data
	 */
	public byte[] getData() {
		return data;
	}
	/** toString method for string representation of Act
	 * @return String representation
	 */
	public String toString(){
		return this.caller.toString()+"\t"+this.time+"\t"+this.operation.name()+"\t"+this.location+"\t"+this.data.toString();
	}
	// clone method
	public Act clone(){
		Act tmp = new Act();
		tmp.caller = this.caller;
		tmp.operation = this.operation;
		tmp.time = this.time;
		tmp.location = this.location;
		tmp.data = Arrays.copyOf(this.data, this.data.length);
		return tmp;
	}
}
