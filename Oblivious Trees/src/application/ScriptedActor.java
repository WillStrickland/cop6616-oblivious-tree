package application;

import java.util.Random;
import java.util.concurrent.DelayQueue;

/** Runnable class that performs actions according to provided 
 * script of actions. Script consists of delay queue with actions
 * to perform for the test class.
 */
public class ScriptedActor implements Runnable {
	public static final int MAX_SLEEP = 1000;
	private Random rnd;
	private TestApplication test;
	private int actions;
	private DelayQueue queue;
	public ScriptedActor(){
		actions = 0;
		test = null;
		rnd = TestApplication.initPRNG();
	}
	public ScriptedActor(DelayQueue dq){
		actions = 0;
		test = null;
		rnd = TestApplication.initPRNG();
		queue = dq;
	}
	public boolean setActions(int a){
		if (a>0){
			this.actions = a;
			return true;
		} else {
			return false;
		}
	}
	public boolean setTest(TestApplication a){
		if (a!=null){
			this.test = a;
			return true;
		} else {
			return false;
		}
	}
	public void run(){
		if (this.test!=null){
			while (this.actions>0){
				try{
					Thread.sleep(rnd.nextInt(RandomActor.MAX_SLEEP));
				} catch (InterruptedException e){
					
				}
				this.test.buttonMash();
				this.actions--;

			}
		}
	}

}
