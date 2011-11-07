package oblivious.concurrent;

import java.util.LinkedList;

class DescStatus {
	protected static enum StatusType { 
		NEW, 	// the current step is newly started
		OPEN,	// the current step is open for all to offer
		LINK,	// the current step needs to be linked
		DONE,	// the current step has been completed
		VOID	// there is no status
	} 

	// Instance variables
	protected StatusType stage;
	protected OTree_Elem currentNode;
	protected LinkedList<OTree_Elem> unassigned;
	
	/** DescStatus default constructor
	 */
	public DescStatus(){
		this.stage = DescStatus.StatusType.VOID;
	}
	/** DescStatus constructor
	 * @param s current stage of this status
	 */
	public DescStatus(StatusType s){
		this.stage = s;
	}
	
}
