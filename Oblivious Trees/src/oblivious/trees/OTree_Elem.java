package oblivious.trees;

public abstract class OTree_Elem {
	final static int MAX_CHILDREN = 3;
	// Instance properties
	protected OTree_Elem parent;
	
	// Constructors
	/** OTree_Elem with without parent (root) */
	public OTree_Elem(){
		parent = null;
	}
	/** OTree_Elem as the child of given node.
	 *  @param p parent OTree_Elem 
	 */
	public OTree_Elem(OTree_Elem p){
		parent = p;
	}
	
	// Mutators
	/** Changes the parent node of this node such that it is a child of the given node.
	 *  @param p new parent OTree_Elem
	 *  @return true if successful, false if failure
	 */
	public boolean setParent(OTree_Elem p){
		if (p != null){
			this.parent = p;
			return true;
		} else {
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
	/** calculate the number of leaves below (or at) this node of the tree.
	 */
	abstract void calcLeafCnt();
	/** trickle updates in leaf count to top of tree. 
	 */
	protected void trickleLeafCnt(){
		OTree_Elem tmp = this.parent;
		while (tmp != null){
			tmp.calcLeafCnt();
			tmp = tmp.parent;
		}
	}
	
	// Inspectors
	/** @return degree of this node of tree
	 */
	abstract int getDegree();
	/** @return count of leaf nodes below (or at) this node of the tree
	 */
	abstract int getLeafCnt();
	/** @return parent node of this node, null if has no parent
	 */
	public OTree_Elem getParent(){
		return this.parent;
	}
	/** return the child at given position of child set.
	 *  @param i position of child
	 */
	public abstract OTree_Elem getChild(int i);
	/** @return copy of child tree 
	 */
	public abstract OTree_Elem[] getChildren();
	/** 
	 */
	public OTree_Elem getPrevSibling(){
		OTree_Elem rtn = null;
		if (this.parent != null){	
			OTree_Elem[] tmp = this.parent.getChildren();
			if(tmp!=null){
				for (int i=tmp.length-1; i>0; i--){
					if (tmp[i]==this){
						rtn = tmp[i-1];
						break;
					}
				}
			}
		}
		return rtn;
	}
	/** 
	 */
	public OTree_Elem getNextSibling(){
		OTree_Elem rtn = null;
		if (this.parent != null){	
			OTree_Elem[] tmp = this.parent.getChildren();
			if(tmp!=null){
				for (int i=0; i<tmp.length-1; i++){
					if (tmp[i]==this){
						rtn = tmp[i+1];
						break;
					}
				}
			}
		}
		return rtn;
	}
}
