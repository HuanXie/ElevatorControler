/*
 * Alarm thread is used as Thread.sleep(), but with the benefit that it will release the lock and waits until alarm wakes it up.
 * Any thread that call this function should have a wakeUp boolean to indicate if the thread have to wake up and wakeUp function
 * to let alarm set wakeUp boolean.
 */
public class Alarm extends Thread{
	boolean sleep; // If elevator called sleep function
	int m; // Number of millisecond to sleep
	Elevator e; // Reference to that elevator
	
	// Constructor
	Alarm(Elevator e){
		this.e = e;
		sleep = false;
	}
	
	// Elevator thread call sleep function to change flag sleep
	public synchronized void sleep(int m)
	{
		this.m = m;
		sleep = true;
		notifyAll();
	}
	
	// Overwrites run method in Thread class
	public void run()
	{
		try{
			while(true)
			{
				synchronized(this)
				{
					while(!sleep) // Check if flag sleep is changed
					{
						wait();
					}
					Thread.sleep(m); // Sleep
					sleep = false;  // Reset sleep
					e.wakeUp(); // Wake up elevator
				}
			}
		}catch(Exception e)
		{}
	}
}
