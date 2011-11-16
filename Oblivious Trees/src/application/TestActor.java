package application;

import java.util.ArrayList;
import java.util.List;

public abstract class TestActor implements Runnable {
	
	// common instance properties
	protected TestApplication test;
	protected ArrayList<Act> actions;
	protected boolean useTiming;

	// abstract implementation of runnable
	public abstract void run();

	// Mutator
	/** Associate this actor to a test to perform
	 *  @param a TestApplication to call methods of
	 *  @return true if successful, else false
	 */
	public boolean setTest(TestApplication a){
		if (a!=null){
			this.test = a;
			return true;
		} else {
			return false;
		}
	}
	/** Sets list actions this RandomActor has performed
	 *  deep copies acts into instance act list
	 *  @param acts list of acts to copy 
	 */
	public void setActions(List<Act> acts){
		// make a new list of same size
		this.actions = new ArrayList<Act>(acts.size());
		// copy individual act records into new list
		for (int i=0; i<this.actions.size(); i++){
			this.actions.set(i, acts.get(i).clone());
		}
	}
	/** Sets list actions this RandomActor has performed
	 *  deep copies acts into instance act list
	 *  @param acts array of acts to copy 
	 */
	public void setActions(Act[] acts){
		// make a new list of same size
		this.actions = new ArrayList<Act>(acts.length);
		// copy individual act records into new list
		for (int i=0; i<this.actions.size(); i++){
			this.actions.set(i, acts[i].clone());
		}
	}
	/** set the useTiming flag to determine if Actor will generate/obey
	 *  timing delays. If true will introduce/obey delays, if false 
	 *  will ignore delays and execute as quickly as possible.
	 *  @param useTiming the flag value
	 */
	public void setUseTiming(boolean useTiming) {
		this.useTiming = useTiming;
	}
	
	// Inspector
	/** Returns the state of the useTiming flag If true will introduce/obey
	 *  delays, if false will ignore delays and execute as quickly as possible.
	 *  @return state of useTiming flag
	 */  
	public boolean isUseTiming() {
		return useTiming;
	}
	/** Get list actions this RandomActor has performed
	 *  @return ArrayList deep copy of RandomActors actions
	 */
	public ArrayList<Act> getActions(){
		// make a new list of same size
		ArrayList<Act> tmp = new ArrayList<Act>(this.actions.size());
		// copy individual act records into new list
		for (int i=0; i<this.actions.size(); i++){
			tmp.set(i, this.actions.get(i).clone());
		}
		// return deep cloned list
		return tmp;
	}

}
