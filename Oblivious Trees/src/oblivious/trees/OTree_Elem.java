package oblivious.trees;

public abstract class OTree_Elem {
	final static int MAX_CHILDREN = 3;
	// Instance properties
	protected OTree_Elem parent;
	protected OTree_Elem[] children;
	
	// Constructors
	public OTree_Elem(){
		parent = null;
		children = new OTree_Elem[OTree_Elem.MAX_CHILDREN];
		for (int i=0; i<OTree_Elem.MAX_CHILDREN; i++){
			children[i] = null;
		}
	}
	public OTree_Elem(OTree_Elem p){
		parent = p;
		children = new OTree_Elem[OTree_Elem.MAX_CHILDREN];
		for (int i=0; i<OTree_Elem.MAX_CHILDREN; i++){
			children[i] = null;
		}
	}
	
	// Mutators
	public boolean addChild(OTree_Elem c){
		boolean success = false;
		if (c==null)
			return false;
		for (int i=0; i<OTree_Elem.MAX_CHILDREN; i++){
			if (this.children[i] == null){
				this.children[i] = c;
				c.parent = this;
				success = true;
				break;
			}
		}
		return success;
	}
	public boolean removeChild(int i){
		if (this.children[i] == null){
			this.children[i] = null;
			return true;
		} else {
			return false;
		}
	}
	protected boolean swapChildren(int i, int j){
		if (i>0 || j<0 || i> OTree_Elem.MAX_CHILDREN || j> OTree_Elem.MAX_CHILDREN)
			return false;
		OTree_Elem tmp = this.children[i];
		this.children[i] = this.children[j];
		this.children[j] = tmp;
		return true;	
	}
	abstract void calcDegree();
	protected void trickleDegree(){
		OTree_Elem tmp = this.parent;
		while (tmp != null){
			tmp.calcDegree();
			tmp = tmp.parent;
		}
	}
	
	// Inspectors
	abstract int getDegree();
	public OTree_Elem getParent(){
		return this.parent;
	}
	public OTree_Elem getChild(int i){
		return this.children[i];
	}
	public OTree_Elem[] getChildren(){
		OTree_Elem[] tmp = new OTree_Elem[OTree_Elem.MAX_CHILDREN];
		for (int i=0; i<OTree_Elem.MAX_CHILDREN; i++){
			 tmp[i] = children[i];
		}
		return tmp;
	}
}
