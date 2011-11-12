package application;

import java.util.ArrayList;
import java.util.Random;

public class RandomActor implements Runnable {
	public static final int MAX_SLEEP = 1000;
	private Random rnd;
	private TestApplication test;
	private int actCnt;
	private ArrayList<Act> actions;
	public RandomActor(){
		actCnt = 0;
		test = null;
		actions = null;
		rnd = TestApplication.initPRNG();
	}
	public boolean setActCnt(int a){
		if (a>0){
			this.actCnt = a;
			return true;
		} else {
			return false;
		}
	}
	/** 
	 *  @param a
	 *  @return
	 */
	public boolean setTest(TestApplication a){
		if (a!=null){
			this.test = a;
			return true;
		} else {
			return false;
		}
	}
	/** run method to perform random actions
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
	/** Get list actions this RandomActor has performed
	 * @return ArrayList deep copy of RandomActors actions
	 */
	public ArrayList<Act> getActions(){
		ArrayList<Act> tmp = new ArrayList<Act>(this.actions.size());
		for (int i=0; i<this.actions.size(); i++){
			tmp.set(i, this.actions.get(i).clone());
		}
		return tmp;
	}
	
}
