package oblivious.concurrent;

import java.nio.ByteBuffer;
import java.util.Arrays;

class OTree_Leaf extends OTree_Elem{
	
	// Instance Variable
	/* NONE */
	
	// Constructors
	/** construct OTree_Leaf with no data or parent.
	 */
	public OTree_Leaf(){
		// no parent no data
		super();
		// set data to null
		//data = null;
	}
	/** construct OTree_Leaf with parent.
	 *  @param p parent OTree_Elem
	 */
	public OTree_Leaf(OTree_Elem p){
		// call super to set parent
		super(p);
		//set data to null
		//data = null;
	}
	/* construct OTree_Leaf with initial data but no parent.
	 *  @param d data to be stored by OTree_Leaf
	 */ /*
	public OTree_Leaf(byte[] d){
		super();
		// copy data from given byte array
		data = new byte[d.length];
		for (int i=0; i<d.length; i++){
			data[i] = d[i];
		}
	} //*/
	/* construct OTree_Leaf with initial data.
	 *  @param p parent OTree_Elem
	 *  @param d data to be stored by OTree_Leaf
	 */ /*
	public OTree_Leaf(OTree_Elem p, byte[] d){
		// call super to set parent
		super(p);
		// copy data from given byte array
		data = new byte[d.length];
		for (int i=0; i<d.length; i++){
			data[i] = d[i];
		}
	} //*/
	
	// Mutators
	/* set the data stored by OTree_Leaf
	 *  @param d data to be copied into OTree_Leaf
	 *  @return true if successful, false if failure
	 */ /*
	public boolean setData(byte[] d){
		if (d!=null && d.length>0){
			this.data = new byte[d.length];
			for (int i=0; i<this.data.length; i++){
				this.data[i] = d[i];
			}
			return true;
		} else {
			return false;
		}
	} //*/
	public boolean setChild(int i, OTree_Elem c){
		return false;
	}
	public boolean addChild(OTree_Elem c){
		return false;
	}
	public boolean removeChild(int i){
		return false;
	}
	public boolean swapChildren(int i, int j){
		return false;
	}
	public void calcLeafCnt(){
		return;
	}
	public void calcLeafCnt(boolean forceCalc){
		return;
	}
	
	// Inspectors
	/* @return data stored by OTree_Leaf
	 */ /*
	public byte[] getData(){
		byte[] tmp = new byte[this.data.length];
		for (int i=0; i<this.data.length; i++){
			tmp[i] = this.data[i];
		}
		return tmp;
	} //*/
	public int getDegree(){
		return 0; 
	}
	public int getLeafCnt(){
		return 1; 
	}
	public OTree_Elem getChild(int i){
		return null;
	}
	public OTree_Elem[] getChildren(){
		return null;
	}
	
	// Representation
	/** reconstruct an individual OTree_Leaf from byte array
	 *  checks size parameter against actual remaining bytes
	 *  similar to de-serialization, but we have other aims in mind
	 *  @param b byte array to read in
	 *  @return reconstructed OTree_Leaf
	 */
	public static OTree_Leaf fromBytes(byte[] b){
		// initialize new OTree_Node
		OTree_Leaf tmp = new OTree_Leaf();
		// create byte buffer just for size integer
		ByteBuffer buf = ByteBuffer.wrap(b, 0, 4);
		// compare recorded size to actual
		if (buf.getInt()==(b.length-4)){
			try { 
				tmp.setSig(Arrays.copyOfRange(b, 4, b.length-1));
			} catch (Exception e){
				// return null due to exception
				return null;
			}
			// return reconstructed OTree_Leaf
			return tmp;
		} else {
			// return null due to size mismatch
			return null;
		}
	}
}
