package oblivious.trees;

public abstract class OTree_Elem {
	final static int MAX_CHILDREN = 3;
	// Instance properties
	protected OTree_Elem parent;
	
	// Constructors
	public OTree_Elem(){
		parent = null;
	}
	public OTree_Elem(OTree_Elem p){
		parent = p;
	}
	
	// Mutators
	public boolean setParent(OTree_Elem p){
		if (p != null){
			this.parent = p;
			return true;
		} else {
			return false;
		}
	}
	public abstract boolean setChild(int i, OTree_Elem c);
	public abstract boolean addChild(OTree_Elem c);
	public abstract boolean removeChild(int i);
	protected abstract boolean swapChildren(int i, int j);
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
	public abstract OTree_Elem getChild(int i);
	public abstract OTree_Elem[] getChildren();
}
