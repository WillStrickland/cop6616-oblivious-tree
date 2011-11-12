package oblivious.concurrent;

import java.util.Arrays;

/** Wrapper class for byte array so that we may use AtomicReference and CompareAndSet.
 *  Class is immutable for safety.
 */
final public class ByteArrayWrapper {

	// Instance variable
	private byte[] data;
	
	// Constructors
	/** Default constructor, leaves data null
	 */
	public ByteArrayWrapper(){
		data = null;
	}
	/** Primary constructor, makes data a copy of input.
	 *  @param d byte[] to be set into this wrapper
	 */
	public ByteArrayWrapper(byte[] d){
		// make copy of array and store
		data = Arrays.copyOf(d, d.length);
	}
	// Mutator - leaving this here for whatevers
	
	/** set the value of this wrapper to a copy of input.
	 *  @param d byte[] to be set into this wrapper
	 */
	@SuppressWarnings("unused")
	private void set(byte[] d){
		data = Arrays.copyOf(d, d.length);
	}
	// Inspector
	/** get the wrapped byte array.
	 *  @return byte[] copy of wrapped array
	 */
	public byte[] get(){
		return Arrays.copyOf(data, data.length);
	}
	
	
}