package oblivious.trees;

public abstract class OTree_Elem {
	final static int MAX_CHILDREN = 3;
	// Instance properties
	private OTree_Elem parent;	// parent node in tree
	private byte[] sig;			// signature of this node
	
	// Constructors
	/** OTree_Elem with without parent (root) */
	public OTree_Elem(){
		parent = null;
		sig = null;
	}
	/** OTree_Elem as the child of given node.
	 *  @param p parent OTree_Elem
	 */
	public OTree_Elem(OTree_Elem p){
		parent = p;
		sig = null;
	}
	
	// Mutators
	/** set the the signature for this OTree_Elem
	 *  @param s signature to be set
	 *  @return true if successful, false if failure
	 */
	public boolean setSig(byte[] s){
		if (s!=null && s.length>0){
			this.sig = new byte[s.length];
			for (int i=0; i<this.sig.length; i++){
				this.sig[i] = s[i];
			}
			return true;
		} else {
			return false;
		}
	}
	/** Changes the parent node of this node such that it is a child of the given node.
	 *  @param p new parent OTree_Elem
	 *  @return true if successful, false if failure
	 */
	public boolean setParent(OTree_Elem p){
		// check if parent value
		if (p != null){
			// set parent and return true
			this.parent = p;
			return true;
		} else {
			// else return false
			return false;
		}
	}
	/** replaces the pointer to one child with another. Cannot increase degree of node.
	 *  @param i index of child to be updated
	 *  @param c child to be set at location
	 *  @return true if successful, false if failure
	 */
	public abstract boolean setChild(int i, OTree_Elem c);
	/** adds a child to end of the child list.
	 *  @param c child to be added
	 *  @return true if successful, false if failure
	 */
	public abstract boolean addChild(OTree_Elem c);
	/** remove a child at given position from child set. 
	 *  @param i index of child to be removed
	 *  @return true if successful, false if failure
	 */
	public abstract boolean removeChild(int i);
	/** swap position of two children in child set.
	 *  @param i index of first child
	 *  @param j index of second child
	 *  @return true if successful, false if failure
	 */
	protected abstract boolean swapChildren(int i, int j);
	/** calculate the number of leaves below (or at) this node of the tree. Does not calculate re-calculate child subtrees.
	 */
	public abstract void calcLeafCnt();
	/** calculate the number of leaves below (or at) this node of the tree.
	 *  @param forceCalc if true re-calculates whole subtree. if false behaves like calcLeafCnt().
	 */
	public abstract void calcLeafCnt(boolean forceCalc);
	/** trickle updates in leaf count to top of tree. 
	 */
	protected void trickleLeafCnt(){
		// starting at the parent of this node
		OTree_Elem tmp = this.parent;
		// iterate to root
		while (tmp != null){
			// calculate the leaf count for this node
			tmp.calcLeafCnt();
			// repeat with next parent
			tmp = tmp.parent;
		}
	}
	
	// Inspectors
	/** @return signature of this OTree_Elem
	 */
	public byte[] getSig(){
		byte[] tmp = new byte[this.sig.length];
		for (int i=0; i<this.sig.length; i++){
			tmp[i] = this.sig[i];
		}
		return tmp;
	}
	/** @return degree of this node of tree
	 */
	public abstract int getDegree();
	/** @return count of leaf nodes below (or at) this node of the tree
	 */
	public abstract int getLeafCnt();
	/** @return parent node of this node, null if has no parent
	 */
	public OTree_Elem getParent(){
		return this.parent;
	}
	/** return the child at given position of child set.
	 *  @param i position of child
	 *  @return child OTree_Elem if child exists else null
	 */
	public abstract OTree_Elem getChild(int i);
	/** @return copy of child tree 
	 */
	public abstract OTree_Elem[] getChildren();
	/** @return the previous child on in parent child set, null if no parent or no previous sibling
	 */
	public OTree_Elem getPrevSibling(){
		OTree_Elem rtn = null;	// return value
		if (this.parent != null){
			OTree_Elem[] tmp = this.parent.getChildren();
			if(tmp!=null){
				// if has parent and parent has children
				// starting at end and search backwards till second element
				for (int i=tmp.length-1; i>0; i--){
					// if match this element and set element 
					if (tmp[i]==this){
						// set return and break
						rtn = tmp[i-1];
						break;
					}
				}
			}
		}
		// previous element if found or return null
		return rtn;
	}
	/** @return the next child on in parent child set, null if no parent or no next sibling
	 */
	public OTree_Elem getNextSibling(){
		OTree_Elem rtn = null;	// return value
		if (this.parent != null){
			OTree_Elem[] tmp = this.parent.getChildren();
			if(tmp!=null){
				// if has parent and parent has children
				// starting at beginning and search till second to last element
				for (int i=0; i<tmp.length-1; i++){
					// if match this element and set element 
					if (tmp[i]==this){
						// set return and break
						rtn = tmp[i+1];
						break;
					}
				}
			}
		}
		// next element if found or return null
		return rtn;
	}
}
