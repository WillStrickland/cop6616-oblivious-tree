package oblivious.trees;

public class OTree_Node extends OTree_Elem {

	// Instance Variable
	private int degree;
	
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
	
	// Inspectors
	public int getDegree(){
		return this.degree;
	}
}
