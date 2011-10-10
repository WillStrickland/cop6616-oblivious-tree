package oblivious.trees;

public class OTree_Leaf extends OTree_Elem{
	
	// Instance Variable
	byte[] data;
	
	// Constructors
	/**
	 * construct OTree_Leaf with no data or parent.
	 */
	public OTree_Leaf(){
		// no parent no data
		super();
		// set data to null
		data = null;
	}
	/**
	 * construct OTree_Leaf with parent.
	 * @param p parent OTree_Elem
	 */
	public OTree_Leaf(OTree_Elem p){
		// call super to set parent
		super(p);
		//set data to null
		data = null;
	}
	/**
	 * construct OTree_Leaf with initial data but no parent.
	 * @param d data to be stored by OTree_Leaf
	 */
	public OTree_Leaf(byte[] d){
		super();
		// copy data from given byte array
		data = new byte[d.length];
		for (int i=0; i<d.length; i++){
			data[i] = d[i];
		}
	}
	/**
	 * construct OTree_Leaf with initial data.
	 * @param p parent OTree_Elem
	 * @param d data to be stored by OTree_Leaf
	 */
	public OTree_Leaf(OTree_Elem p, byte[] d){
		// call super to set parent
		super(p);
		// copy data from given byte array
		data = new byte[d.length];
		for (int i=0; i<d.length; i++){
			data[i] = d[i];
		}
	}
	
	// Mutators
	/**
	 * set the data stored by OTree_Leaf
	 * @param d
	 */
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
	public void calcLeafCnt(boolean forceCalc){
		return;
	}
	
	// Inspectors
	/**
	 * 
	 * @return data stored by OTree_Leaf
	 */
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
