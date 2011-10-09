package oblivious.trees;

public class OTree_Leaf extends OTree_Elem{
	
	// Instance Variable
	byte[] data;
	
	// Constructors
	public OTree_Leaf(){
		super();
		data = null;
	}
	public OTree_Leaf(byte[] d){
		super();
		data = new byte[d.length];
		for (int i=0; i<d.length; i++){
			data[i] = d[i];
		}
	}
	
	// Mutators
	public void setData(byte[] d){
		this.data = new byte[d.length];
		for (int i=0; i<this.data.length; i++){
			this.data[i] = d[i];
		}
	}
	public boolean setChild(int i, OTree_Elem c){
		return false;
	}
	public boolean addChild(OTree_Elem c){
		return false;
	}
	public boolean removeChild(int i){
		return false;
	}
	protected boolean swapChildren(int i, int j){
		return false;
	}
	public void calcLeafCnt(){
		return;
	}
	
	// Inspectors
	public byte[] getData(){
		byte[] tmp = new byte[this.data.length];
		for (int i=0; i<this.data.length; i++){
			tmp[i] = this.data[i];
		}
		return tmp;
	}
	public int getDegree(){
		return 0; 
	}
	public int getLeafCnt(){
		return 1; 
	}
	public OTree_Elem getChild(int i){
		return null;
	}
	public OTree_Elem[] getChildren(){
		return null;
	}
}
