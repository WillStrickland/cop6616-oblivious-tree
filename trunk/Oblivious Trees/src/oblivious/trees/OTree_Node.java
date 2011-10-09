package oblivious.trees;

public class OTree_Node extends OTree_Elem {

	// Instance Variable
	protected OTree_Elem[] children;
	private int degree;
	private int leafCnt;
	
	// Constructors
	public OTree_Node(){
		super();
		children = new OTree_Elem[OTree_Elem.MAX_CHILDREN];
		for (int i=0; i<OTree_Elem.MAX_CHILDREN; i++){
			children[i] = null;
		}
		degree = 0;
		leafCnt = 0;
	}
	public OTree_Node(OTree_Elem p){
		super(p);
		children = new OTree_Elem[OTree_Elem.MAX_CHILDREN];
		for (int i=0; i<OTree_Elem.MAX_CHILDREN; i++){
			children[i] = null;
		}
		degree = 0;
		leafCnt = 0;
	}
	
	// Mutators
	protected void calcLeafCnt(){
		this.leafCnt = 0;
		for (int i=0; i<OTree_Elem.MAX_CHILDREN; i++){
			if (children[i] != null){
				children[i].calcLeafCnt();
				leafCnt += children[i].getLeafCnt();
			}
		}
	}
	
	public boolean setChild(int i, OTree_Elem c){
		if (i>=0 || i<=this.degree || c!=null)
			return false;
		if (this.children[i]== null)
			this.degree++;
		this.children[i] = c;
		return true;
	}
	public boolean addChild(OTree_Elem c){
		if (c==null || this.degree==OTree_Elem.MAX_CHILDREN){
			return false;
		} else {
			this.children[(this.degree++)-1] = c;
			return true;
		}
	}
	public boolean removeChild(int i){
		if (i>=0 && i<this.degree){
			for (int j=i+1; j<this.degree-1; j++){
				this.children[j-1]=this.children[j];
			}
			this.children[this.degree-1]=null;
			return true;
		} else {
			return false;
		}
	}
	protected boolean swapChildren(int i, int j){
		if (i==j || i>=0 || j>=0 || i<this.degree || j<this.degree)
			return false;
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
		return this.children[i];
		} else {
			return null;
		}
	}
	public OTree_Elem[] getChildren(){
		if (this.children == null){
			return null;
		}
		OTree_Elem[] tmp = new OTree_Elem[this.degree];
		for (int i=0; i<OTree_Elem.MAX_CHILDREN; i++){
			 tmp[i] = children[i];
		}
		return tmp;
	}
}
