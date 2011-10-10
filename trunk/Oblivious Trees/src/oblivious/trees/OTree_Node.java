package oblivious.trees;

public class OTree_Node extends OTree_Elem {

	// Instance Variable
	protected OTree_Elem[] children;
	private int degree;
	private int leafCnt;
	
	// Constructors
	/**
	 * Construct OTree_Node with no parent and no children.
	 */
	public OTree_Node(){
		super();
		// initialize children array
		children = new OTree_Elem[OTree_Elem.MAX_CHILDREN];
		// initialize all children to null
		for (int i=0; i<OTree_Elem.MAX_CHILDREN; i++){
			children[i] = null;
		}
		// set degree and leaf count to zero
		degree = 0;
		leafCnt = 0;
	}
	/**
	 * Construct OTree_Node with with parent but no children.
	 * @param p parent OTree_Elem
	 */
	public OTree_Node(OTree_Elem p){
		// call super to set parent
		super(p);
		// initialize children array
		children = new OTree_Elem[OTree_Elem.MAX_CHILDREN];
		// initialize all children to null
		for (int i=0; i<OTree_Elem.MAX_CHILDREN; i++){
			children[i] = null;
		}
		// set degree and leaf count to zero
		degree = 0;
		leafCnt = 0;
	}
	
	// Mutators
	protected void calcLeafCnt(){
		// reset leaf count
		this.leafCnt = 0;
		// for each child
		for (int i=0; i<this.degree; i++){
			// sum leaf count
			leafCnt += children[i].getLeafCnt();
		}
	}
	protected void calcLeafCnt(boolean forceCalc){
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
	protected boolean swapChildren(int i, int j){
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
		// make temporary array with size of this nodes degree
		// if degree is zero return null
		OTree_Elem[] tmp = (this.degree>0) ? new OTree_Elem[this.degree] : null;
		// iterate for degree and copy child set
		for (int i=0; i<this.degree; i++){
			 tmp[i] = children[i];
		}
		return tmp;
	}
}
