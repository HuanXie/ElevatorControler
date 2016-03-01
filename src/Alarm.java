public class Alarm extends Thread{
	boolean sleep;
	int m;
	Elevator e;
	Alarm(Elevator e){
		this.e = e;
		sleep = false;
	}
	
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
					while(!sleep)
					{
						wait();
					}
					Thread.sleep(m);
					sleep = false;
					e.wakeUp();
				}
			}
		}catch(Exception e)
		{}
	}
}
