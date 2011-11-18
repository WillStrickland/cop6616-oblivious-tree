package application;

import java.util.ArrayList;
import java.util.Random;

public class RandomActor extends TestActor {
	
	// Instance variables
	public static final int MAX_SLEEP = 1000;
	private Random rnd;
	private int actCnt;
	
	/** RandomActor default constructor
	 */
	public RandomActor(){
		test = null;
		actions = new ArrayList<Act>();
		actCnt = 0;
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

	// Runnable
	/** run method to perform random actions
	 *  calls back to methods in test class to 
	 *  execute appropriate actions.
	 */
	public void run(){
		// make sure test is valid
		if (this.test!=null){
			// while there are still actions left to do
			while (this.actCnt>0){
				// if use timing set then make a delay queue to orchestrate actions
				if(this.useTiming){
					try{
						// sleep for a randomized amount of time
						Thread.sleep(rnd.nextInt(RandomActor.MAX_SLEEP));
					} catch (InterruptedException e){ }
				}
				// execute a random actionand record the result
				Act a = this.test.buttonMash();
				a.setCaller(this);
				// add this action to the list
				this.actions.add(a);
				// decrement action remaining count
				this.actCnt--;
			}
			// notify the main thread waiting on the
			// test object that this actor has completed
			test.notifyActorComplete();
		}
	}
	
}
