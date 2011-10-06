package oblivious.trees;

public class OTree_Node extends OTree_Elem {

	// Instance Variable
	protected OTree_Elem[] children;
	private int degree;
	
	// Constructors
	public OTree_Node(){
		super();
		children = new OTree_Elem[OTree_Elem.MAX_CHILDREN];
		for (int i=0; i<OTree_Elem.MAX_CHILDREN; i++){
			children[i] = null;
		}
		degree = 0;
	}
	public OTree_Node(OTree_Elem p){
		super(p);
		children = new OTree_Elem[OTree_Elem.MAX_CHILDREN];
		for (int i=0; i<OTree_Elem.MAX_CHILDREN; i++){
			children[i] = null;
		}
		degree = 0;
	}
	
	// Mutators
	protected void calcDegree(){
		this.degree = 0;
		for (int i=0; i<OTree_Elem.MAX_CHILDREN; i++){
			if (children[i] != null){
				children[i].calcDegree();
				degree += children[i].getDegree();
			}
		}
	}
	public boolean setChild(int i, OTree_Elem c){
		if (i>=0 || i<OTree_Elem.MAX_CHILDREN || c!=null)
			return false;
		this.children[i] = c;
		return true;
	}
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
		if (i>=0 && i<OTree_Elem.MAX_CHILDREN && this.children[i] != null){
			this.children[i] = null;
			return true;
		} else {
			return false;
		}
	}
	protected boolean swapChildren(int i, int j){
		if (i>=0 || j>=0 || i<OTree_Elem.MAX_CHILDREN || j<OTree_Elem.MAX_CHILDREN)
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
	public OTree_Elem getChild(int i){
		if (i>=0 && i<OTree_Elem.MAX_CHILDREN){
		return this.children[i];
		} else {
			return null;
		}
	}
	public OTree_Elem[] getChildren(){
		if (this.children == null){
			return null;
		}
		OTree_Elem[] tmp = new OTree_Elem[OTree_Elem.MAX_CHILDREN];
		for (int i=0; i<OTree_Elem.MAX_CHILDREN; i++){
			 tmp[i] = children[i];
		}
		return tmp;
	}
}
