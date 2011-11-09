package application;

import java.util.Random;

public class RandomActor implements Runnable {
	public static final int MAX_SLEEP = 1000;
	private Random rnd;
	private TestApplication test;
	private int actions;
	public RandomActor(){
		actions = 0;
		test = null;
		rnd = TestApplication.initPRNG();
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
