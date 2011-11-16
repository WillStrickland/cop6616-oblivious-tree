package application;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class Act implements Cloneable, Delayed{
	
	// Instance variables
	private Runnable caller;	// calling object
	private String callerNm;	// string name of calling thread/object
	private OpType operation;	// operation performed
	private long time;			// time action took place
	private int location;		// location of insert/delete
	private byte[] data;		// data that was or will be inserted
	private long delayEnd;		// time that delay should expire

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
		this.callerNm = caller.toString();
	}
	/** set Act caller Name
	 *  does allow whitespace, will remove whitespace
	 *  @param callerNm the caller name to set
	 */
	public void setCallerNm(String callerNm) {
		callerNm = callerNm.replaceAll("\\Qs\\E", "");
		this.callerNm = (!callerNm.isEmpty())? callerNm : "null";
	}
	/** set Act operation
	 *  @param operation the operation to set
	 */
	public void setOperation(OpType operation) {
		this.operation = operation;
	}
	/** set Act operation
	 *  @param operation the operation to set
	 */
	public void setOperation(String operation) {
		this.operation = OpType.valueOf(operation);
	}
	/** set Act time
	 *  @param time the time to set
	 */
	public void setTime(long time) {
		this.time = time;
	}
	/** set Act location
	 *  @param location the location to set
	 */
	public void setLocation(int location) {
		this.location = location;
	}
	/** set Act data
	 *  @param data the data to set
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
	/** get Act caller name
	 *  @return the Act caller name
	 */
	public String getCallerNm() {
		return callerNm;
	}
	/** get Act operation
	 *  @return the Act operation
	 */
	public OpType getOperation() {
		return operation;
	}
	/** get Act 
	 *  @return the Act time
	 */
	public long getTime() {
		return time;
	}
	/** get Act location
	 *  @return the Act location
	 */
	public int getLocation() {
		return location;
	}
	/** get Act data
	 *  @return the Act data
	 */
	public byte[] getData() {
		return data;
	}

	// Psuedo-serialization methods
	/** toString method for string representation of Act
	 *  @return String representation
	 */
	public String toString(){
		return ((caller!=null)?this.caller.toString():this.callerNm)+"\t"+this.time+"\t"+((this.operation!=null)?this.operation.name():"NOOP")+"\t"+this.location+"\t"+Arrays.toString(this.data);
	}
	/** Method for constructing a new Act 
	 *  object from the string representation Compatible
	 *  with the Act.toString() output, except cannot set 
	 *  the caller property. Sets callerNm instead.
	 *  @param txt Scanner input
	 *  @return Act object with same values, null if failure
	 */
	public static Act scanAct(Scanner txt){
		Act tmp = new Act();
		tmp.callerNm = txt.next();
		tmp.time = txt.nextLong();
		tmp.operation = Act.OpType.valueOf(txt.next());
		tmp.location = txt.nextInt();
		tmp.data = scanByteArray(txt);
		return tmp;
	}
	/** Reads byte array from scanner input. Compatible with
	 *  standard output format for Arrays.toString(byte[]).
	 *  @param txt Scanner input
	 *  @return byte[] constructed from input, null if failure
	 */
	public static byte[] scanByteArray(Scanner txt){
		boolean started = false;
		boolean finished = false;
		ArrayList<Byte> tmpL = new ArrayList<Byte>();
		while (txt.hasNext() && !finished){
			// get next token in list
			String tmpS = txt.next();
			//check empty
			if (!started && !finished && tmpS.startsWith("[") && tmpS.endsWith("]") && tmpS.length()==2){
				started = true;
				finished = true;
				break;
			}
			// check if this starts the array or array already started
			else if (tmpS.startsWith("[") || started){
				started = true;
			} else {
				break;	// else invalid
			}
			// check if end of list and already started
			if (tmpS.endsWith("]") && started){
				finished = true;
			} 
			// else if invalid
			else if (tmpS.endsWith("]") && !started) {
				break;	
			}
			// remove characters
			tmpS = tmpS.replaceAll("[\\Q[,]\\E]", "");
			// parse Byte and add to list
			tmpL.add(Byte.parseByte(tmpS));
		}
		// convert ArrayList<Byte> to byte[]
		byte[] tmpA = new byte[tmpL.size()];
		for (int i=0; i<tmpL.size(); i++){
			tmpA[i]=tmpL.get(i).byteValue();
		}
		// return temporary array unless error
		return (started && finished) ? tmpA: null;
	}
	/** Clone method, creates new Act object with identical values
	 */
	public Act clone(){
		Act tmp = new Act();
		
		tmp.caller = this.caller;
		tmp.callerNm = this.callerNm;
		tmp.operation = this.operation;
		tmp.time = this.time;
		tmp.location = this.location;
		tmp.data = Arrays.copyOf(this.data, this.data.length);
		return tmp;
	}
	// Delayed methods
	/** Start the delay count down, can be called multiple times
	 *  uses time variable as the amount of delay in milliseconds
	 */
	public void setDelayStart(){
		this.delayEnd = System.currentTimeMillis() + this.time;
	}
	/** Start the delay count down, can be called multiple times
	 *  accepts delay in any TimeUnit
	 *  @param delay amount of time to delay as long
	 *  @param unit TimeUnit of given delay
	 */
	public void setDelayStart(long delay, TimeUnit unit){
		this.delayEnd = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(delay, unit);
	}
	/** Set exact delay end for a specific system time
	 * @param exact desired system time in milliseconds (as in System.currentTimeMillis())
	 */
	public void setDelayEnd(long timeEnd){
		this.delayEnd = timeEnd;
	}
	
	/** Comparable compareTo function, 
	 *  used to implement the Delayed interface 
	 *  and only the Delayed interface
	 *  @param o object to be compared, MUST be Act
	 *  @return int specifying ordering of this and o
	 */
	public int compareTo(Delayed o) {
		long tmp = this.delayEnd - ((Act) o).delayEnd;
		return (tmp>0) ? 1 : (tmp<0) ? -1: 0;
	}
	/** Delayed interface required method
	 * @param unit TimeUnit to return time in
	 * @return long in specified TimeUnit till delay expires, negative if already expired
	 */
	public long getDelay(TimeUnit unit) {
		return unit.convert(this.delayEnd-System.currentTimeMillis(), TimeUnit.MILLISECONDS);
	}
	
}
