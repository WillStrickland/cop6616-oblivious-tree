package oblivious.trees;

public class OTree_Leaf {
	
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
	public void calcDegree(){
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
		return 1; 
	}
}
