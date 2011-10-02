package oblivious.trees;

public class OTree_Elem {
	final static int MAX_CHILDREN = 3;
	// Instance properties
	protected OTree_Elem parent;
	protected OTree_Elem[] children;
	private int degree;
	
	// Constructors
	public OTree_Elem(){
		parent = null;
		children = new OTree_Elem[OTree_Elem.MAX_CHILDREN];
		for (int i=0; i<OTree_Elem.MAX_CHILDREN; i++){
			children[i] = null;
		}
		degree = 0;
	}
	
	// Mutators
	public boolean addChild(OTree_Elem c){
		boolean success = false;
		for (int i=0; i<OTree_Elem.MAX_CHILDREN; i++){
			if (this.children[i] == null){
				this.children[i] = c;
				c.parent = this;
				success = true;
			}
		}
		return success;
	}
	
	// Inspectors
	public int getDegree(){
		return this.degree;
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
