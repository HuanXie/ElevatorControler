/*Alarm thread is used to ask elevator to sleep 2 seconds when door is open and closed.
 *Otherwise the elevator will be wake up when button i elevator is pressed, then the simulation of open and close 
 *will be interrupted.*/
public class Alarm extends Thread{
	boolean sleep;
	int m;
	Elevator e;
	Alarm(Elevator e){
		this.e = e;
		sleep = false;
	}
	
	/*elevator thread call sleep function to change flag sleep*/
	public synchronized void sleep(int m)
	{
		this.m = m;
		sleep = true;
		notifyAll();
	}
	
	public void run()
	{
		try{
			while(true)
			{
				synchronized(this)
				{
					while(!sleep) //check if flag sleep is changed
					{
						wait();
					}
					Thread.sleep(m); //sleep
					sleep = false;  //reset sleep
					e.wakeUp(); //wake up elevator
				}
			}
		}catch(Exception e)
		{}
	}
}
