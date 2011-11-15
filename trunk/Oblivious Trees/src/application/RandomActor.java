package application;

import java.util.ArrayList;
import java.util.Random;

public class RandomActor implements Runnable {
	
	// Instance variables
	public static final int MAX_SLEEP = 1000;
	private Random rnd;
	private TestApplication test;
	private int actCnt;
	private ArrayList<Act> actions;
	
	/** RandomActor default constructor
	 */
	public RandomActor(){
		actCnt = 0;
		test = null;
		actions = new ArrayList<Act>();
		rnd = TestApplication.initPRNG();
	}
	
	// Mutators
	/** Set the number of Actions this actor should take
	 *  @param a count of actions
	 *  @return true if successful, else false
	 */
	public boolean setActCnt(int a){
		if (a>0){
			// set number of actions
			this.actCnt = a;
			// make actions array large enough to hold
			// act records
			actions.ensureCapacity(this.actCnt);
			return true;
		} else {
			return false;
		}
	}
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
	
	// Runnable
	/** run method to perform random actions
	 *  calls back to methods in test class to 
	 *  execute appropriate actions.
	 */
	public void run(){
		if (this.test!=null){
			while (this.actCnt>0){
				try{
					Thread.sleep(rnd.nextInt(RandomActor.MAX_SLEEP));
				} catch (InterruptedException e){
					
				}
				this.actions.add(this.test.buttonMash());
				this.actCnt--;
			}
		}
	}
	
	// Inspector
	/** Get list actions this RandomActor has performed
	 * @return ArrayList deep copy of RandomActors actions
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
