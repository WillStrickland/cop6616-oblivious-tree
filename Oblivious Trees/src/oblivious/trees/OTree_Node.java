package oblivious.trees;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class OTree_Node extends OTree_Elem {

	// Instance Variable
	private OTree_Elem[] children;
	private int degree;
	private int leafCnt;
	
	// Constructors
	/** Construct OTree_Node with no parent and no children.
	 */
	public OTree_Node(){
		super();
		// initialize children array
		children = new OTree_Elem[OTree_Elem.MAX_CHILDREN];
		// initialize all children to null
		Arrays.fill(children, null);
		// set degree and leaf count to zero
		degree = 0;
		leafCnt = 0;
	}
	/** Construct OTree_Node with with parent but no children.
	 *  @param p parent OTree_Elem
	 */
	public OTree_Node(OTree_Elem p){
		// call super to set parent
		super(p);
		// initialize children array
		children = new OTree_Elem[OTree_Elem.MAX_CHILDREN];
		// initialize all children to null
		Arrays.fill(children, null);
		// set degree and leaf count to zero
		degree = 0;
		leafCnt = 0;
	}
	
	// Mutators
	public void calcLeafCnt(){
		// reset leaf count
		this.leafCnt = 0;
		// for each child
		for (int i=0; i<this.degree; i++){
			// sum leaf count
			leafCnt += children[i].getLeafCnt();
		}
	}
	public void calcLeafCnt(boolean forceCalc){
		// reset leaf count
		this.leafCnt = 0;
		// for each child
		for (int i=0; i<this.degree; i++){
			// if force calc set
			if (forceCalc){ 
				// recalculate leaf count for each child's subtree
				children[i].calcLeafCnt(forceCalc);
			}
			// sum leaf count
			leafCnt += children[i].getLeafCnt();
		}
	}
	public boolean setChild(int i, OTree_Elem c){
		// check index and c are valid
		if (i>=0 && i<this.degree && c!=null){
			// set c into child set and return true
			this.children[i] = c;
			return true;
		} else {
			// else return false
			return false;
		}
	}
	public boolean addChild(OTree_Elem c){
		// check child and degree not already maximum 
		if (c!=null && this.degree<OTree_Elem.MAX_CHILDREN){
			// add this as next child and increment degree
			this.children[this.degree++] = c;
			return true;
		} else {
			// return failure
			return false;
		}
	}
	public boolean removeChild(int i){
		// check if valid index
		if (i>=0 && i<this.degree){
			// decrement degree
			this.degree--;
			// shift all nodes to right one 
			for (int j=i+1; j<=this.degree; j++){
				this.children[j-1] = this.children[j];
			}
			// clean up the last index of child set
			this.children[this.degree]=null;
			return true;
		} else {
			// else failed so return false
			return false;
		}
	}
	public boolean swapChildren(int i, int j){
		// catch invalid indices
		if (i==j || i<0 || j<0 || i>=this.degree || j>=this.degree){
			return false;
		}
		// perform simple swap and return true
		OTree_Elem tmp = this.children[i];
		this.children[i] = this.children[j];
		this.children[j] = tmp;
		return true;
	}
	
	// Inspectors
	public int getDegree(){
		return this.degree;
	}
	public int getLeafCnt(){
		return this.leafCnt;
	}
	public OTree_Elem getChild(int i){
		if (i>=0 && i<this.degree){
			// if valid index return the child
			return this.children[i];
		} else {
			// else return null
			return null;
		}
	}
	public OTree_Elem[] getChildren(){
		// check that children is initialized and has children
		if (children!=null && this.degree>0){
			try { 
				// return copy of 
				return Arrays.copyOf(this.children, this.degree);
			} catch (Exception e){
				// return null due to exception
				return null;
			}
		} else {
			// return null due to missing children
			return null;
		}
	}
	
	// Representation
	/** reconstruct an individual OTree_Node from byte array
	 *  checks size parameter against actual remaining bytes
	 *  similar to de-serialization, but we have other aims in mind
	 *  @param b byte array to read in
	 *  @return reconstructed OTree_Node
	 */
	public static OTree_Node fromBytes(byte[] b){
		// initialize new OTree_Node
		OTree_Node tmp = new OTree_Node();
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
