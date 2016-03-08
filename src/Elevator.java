import java.util.Collections;
import java.util.concurrent.PriorityBlockingQueue;

public class Elevator extends Thread{
	int id;
	int floor;
	float position; //current position of elevator
	boolean moving;  //status of elevator: moving or not
	boolean up;  //status of elevator: moving up or not
	boolean down; //status of elevator: moving down or not
	boolean newEvent; //new event trigger
	boolean direction_current_path; // True when current == up_path, false when current == down_path
	boolean wakeUp;
	Alarm alarm;  //help object to ask thread sleep without holding lock in hand
	MainController controller;
	PriorityBlockingQueue<Integer> up_path;  //when floor button with direction up is pressed, put it in queue up_path
	PriorityBlockingQueue<Integer> down_path; //when floor button with direction down is pressed, put it in queue down_path
	PriorityBlockingQueue<Integer> current_path; //current path elevator is used
	int current_max;
	int current_min;
	int down_max;
	int down_min;
	int up_max;
	int up_min;
	
	// Constructor, setting all to false
	Elevator(int id, int floor, MainController controller)
	{ 
		this.id = id;
		this.floor = floor;
		this.controller = controller;
		position = 0f;
		moving = false;
		up = false;
		down = false;
		newEvent = false;
		wakeUp = false;
		alarm = new Alarm(this);
		alarm.start();
		up_path = new PriorityBlockingQueue<Integer>();
		down_path = new PriorityBlockingQueue<Integer>(11, Collections.reverseOrder());
		current_max = 0;
		current_min = floor;
		down_max = 0;
		down_min = floor;
		up_max = 0;
		up_min = floor;
	}
	
	public synchronized void stopElevator()
	{
		controller.stop(id);
		up_path = new PriorityBlockingQueue<Integer>();
		down_path = new PriorityBlockingQueue<Integer>(11, Collections.reverseOrder());
		current_max = 0;
		current_min = floor;
		down_max = 0;
		down_min = floor;
		up_max = 0;
		up_min = floor;
		moving = false;
		up = false;
		down = false;
		newEvent = false;
		wakeUp = false;
		alarm = new Alarm(this);
		alarm.start();
	}
	
	public synchronized boolean isMoving(){
		return moving;
	}

	private synchronized void updateCurrent(int floor)
	{
		if(floor > current_max)
		{
			current_max = floor;
		}
		if(floor < current_min)
		{
			current_min = floor;
		}
	}
	
	private synchronized void updateUp(int floor)
	{
		if(floor > up_max)
		{
			up_max = floor;
		}
		if(floor < up_min)
		{
			up_min = floor;
		}
	}
	
	private synchronized void updateDown(int floor)
	{
		if(floor > down_max)
		{
			down_max = floor;
		}
		if(floor < down_min)
		{
			down_min = floor;
		}
	}
	
	public synchronized int score(int floor, String direction)
	{
		int up_max = floor;
		int down_max = 0;
		if(direction.equals("Up"))
		{
			if(moving)
			{
				if(up)
				{
					if(position > floor)
					{
						if(direction_current_path) // NextPath = true, position > floor, up, "Up"
						{
							if(down_path.isEmpty())
							{
								if(up_path.isEmpty())
								{
									return(Math.abs(current_max-Math.round(position))+Math.abs(current_max-floor));
								}else{
									return(Math.abs(current_max-Math.round(position))+Math.abs(current_max-up_min)+Math.abs(floor-up_min));
								}
							}else{
								if(up_path.isEmpty())
								{
									return(Math.abs(current_max-Math.round(position))+Math.abs(down_max-current_max)+Math.abs(down_max-down_min)+Math.abs(floor-down_min));
								}
								else{
									return(Math.abs(current_max-Math.round(position))+Math.abs(down_max-current_max)+Math.abs(down_max-down_min)+Math.abs(down_min-up_min)+Math.abs(floor-up_min));
								}
							}
						}else{ // NextPath = false, position > floor, up, "Up"
							if(up_path.isEmpty())
							{
								if(down_path.isEmpty())
								{
									return(Math.abs(current_max-Math.round(position))+Math.abs(current_max-floor));
								}
								else{
									return(Math.abs(current_max-Math.round(position))+Math.abs(current_max-floor));
								}
							}else{
								if(down_path.isEmpty())
								{
									return(Math.abs(current_max-Math.round(position))+Math.abs(current_max-up_min)+Math.abs(floor-up_min));
								}else{
									return(Math.abs(current_max-Math.round(position))+Math.abs(current_max-up_min)+Math.abs(floor-up_min));
								}
							}
						}
					}else{ 
						if(direction_current_path) // NextPath = true, position < floor, up, "Up"
						{
							if(up_path.isEmpty())
							{
								if(down_path.isEmpty())
								{
									return(Math.abs(floor-Math.round(position)));
								}else{
									return(Math.abs(floor-Math.round(position)));
								}
							}else{
								if(down_path.isEmpty())
								{
									return(Math.abs(floor-Math.round(position)));
								}else{
									return(Math.abs(floor-Math.round(position)));
								}
							}
						}else{ // NextPath = false, position < floor, up, "Up"
							if(up_path.isEmpty())
							{
								if(down_path.isEmpty())
								{
									return(Math.abs(current_max-Math.round(position))+Math.abs(current_max-floor));
								}else{
									System.out.println("Current_max=" + current_max + ",position=" + Math.round(position) + ",floor="+floor);
									return(Math.abs(current_max-Math.round(position))+Math.abs(current_max-floor));
								}
							}else{
								if(down_path.isEmpty())
								{
									return(Math.abs(current_max-Math.round(position))+Math.abs(current_max-up_min)+Math.abs(floor-up_min));
								}else{
									return(Math.abs(current_max-Math.round(position))+Math.abs(current_max-up_min)+Math.abs(floor-up_min));
								}
							}
						}
					}
				}else{
					if(position > floor)
					{
						if(direction_current_path) // NextPath = true, position > floor, down, "Up"
						{
							if(up_path.isEmpty())
							{
								if(down_path.isEmpty())
								{
									return(Math.abs(Math.round(position)-current_min)+Math.abs(floor-current_min));
								}else{
									return(Math.abs(Math.round(position)-current_min)+Math.abs(down_max-current_min)+Math.abs(down_max-down_min)+Math.abs(floor-down_min));
								}
							}
						}else{ // nextPath = false, position > floor, down, "Up
							if(up_path.isEmpty())
							{
								if(down_path.isEmpty())
								{
									return(Math.abs(Math.round(position)-current_min)+Math.abs(floor-current_min));
								}else{
									return(Math.abs(Math.round(position)-current_min)+Math.abs(floor-current_min));
								}
							}else{
								if(down_path.isEmpty())
								{
									return(Math.abs(Math.round(position)-current_min)+Math.abs(current_min-up_min)+Math.abs(floor-up_min));
								}else{
									return(Math.abs(Math.round(position)-current_min)+Math.abs(current_min-up_min)+Math.abs(floor-up_min));
								}
							}
						}
					}else{
						if(direction_current_path) // nextPath = true, position < floor, down, "Up"
						{
							if(down_path.isEmpty())
							{
								if(up_path.isEmpty())
								{
									return(Math.abs(Math.round(position)-current_min)+Math.abs(floor-current_min));
								}else{
									return(Math.abs(Math.round(position)-current_min)+Math.abs(current_min-up_min)+Math.abs(floor-up_min));
								}
							}else{
								if(up_path.isEmpty())
								{
									return(Math.abs(Math.round(position)-current_min)+Math.abs(down_max-current_min)+Math.abs(down_max-down_min)+Math.abs(floor-down_min));
								}else{
									return(Math.abs(Math.round(position)-current_min)+Math.abs(down_max-current_min)+Math.abs(down_max-down_min)+Math.abs(down_min-up_min)+Math.abs(floor-up_min));
								}
							}
						}else{ // nextPath = false, position < floor, down, "Up"
							if(down_path.isEmpty())
							{
								if(up_path.isEmpty())
								{
									return(Math.abs(Math.round(position)-current_min)+Math.abs(floor-current_min));
								}else{
									return(Math.abs(Math.round(position)-current_min)+Math.abs(current_min-up_min)+Math.abs(floor-up_min));
								}
							}else{
								if(up_path.isEmpty())
								{
									return(Math.abs(Math.round(position)-current_min)+Math.abs(floor-current_min));
								}else{
									return(Math.abs(Math.round(position)-current_min)+Math.abs(current_min-up_min)+Math.abs(floor-up_min));
								}
							}
						}
					}
				}
			}else{ // Not moving
				return(Math.abs(Math.round(position)-floor));
			}
		}else{ // 
			if(moving)
			{
				if(down)
				{
					if(position < floor)
					{
						if(direction_current_path) // nextPath = true, position < floor, down, "Down"
						{
							if(up_path.isEmpty())
							{
								if(down_path.isEmpty())
								{
									return(Math.abs(Math.round(position)-current_min)+Math.abs(floor-current_min));
								}else{
									return(Math.abs(Math.round(position)-current_min)+Math.abs(down_max-current_min)+Math.abs(down_max-floor));
								}
							}else{
								if(down_path.isEmpty())
								{
									return(Math.abs(Math.round(position)-current_min)+Math.abs(floor-current_min));
								}else{
									return(Math.abs(Math.round(position)-current_min)+Math.abs(down_max-current_min)+Math.abs(down_max-floor));
								}
							}
						}else{ // nextPath = false, position < floor, down, "Down"
							if(up_path.isEmpty())
							{
								if(down_path.isEmpty())
								{
									return(Math.abs(Math.round(position)-current_min)+Math.abs(floor-current_min));
								}else{
									return(Math.abs(Math.round(position)-current_min)+Math.abs(down_max-current_min)+Math.abs(down_max-floor));
								}
							}else{
								if(down_path.isEmpty())
								{
									return(Math.abs(Math.round(position)-current_min)+Math.abs(current_min-up_min)+Math.abs(up_max-up_min)+Math.abs(up_max-floor));
								}else{
									return(Math.abs(Math.round(position)-current_min)+Math.abs(current_min-up_min)+Math.abs(up_max-up_min)+Math.abs(up_max-down_max)+Math.abs(down_max-floor));
								}
							}
						}
					}else{
						if(direction_current_path) // nextPath = true, position > floor, down, "Down"
						{
							if(up_path.isEmpty())
							{
								if(down_path.isEmpty())
								{
									return(Math.abs(Math.round(position)-current_min)+Math.abs(floor-current_min));
								}else{
									return(Math.abs(Math.round(position)-current_min)+Math.abs(down_max-current_min)+Math.abs(down_max-floor));
								}
							}else{
								if(down_path.isEmpty())
								{
									return(Math.abs(Math.round(position)-current_min)+Math.abs(floor-current_min));
								}else{
									return(Math.abs(Math.round(position)-current_min)+Math.abs(down_max-current_min)+Math.abs(down_max-floor));
								}
							}
						}else{ // nextPath = false, position > floor, down, "Down"
							if(up_path.isEmpty())
							{
								if(down_path.isEmpty())
								{
									return(Math.abs(Math.round(position)-floor));
								}else{
									return(Math.abs(Math.round(position)-floor));
								}
							}else{
								if(down_path.isEmpty())
								{
									return(Math.abs(Math.round(position)-floor));
								}else{
									return(Math.abs(Math.round(position)-floor));
								}
							}
						}
					}
				}else{
					if(position < floor)
					{
						if(direction_current_path) // nextPath = true, position < floor, up, "Down"
						{
							if(up_path.isEmpty())
							{
								if(down_path.isEmpty())
								{
									System.out.println("current_max="+current_max);
									return(Math.abs(current_max-Math.round(position))+Math.abs(current_max-floor));
								}else{
									return(Math.abs(current_max-Math.round(position))+Math.abs(current_max-down_max)+Math.abs(down_max-floor));
								}
							}else{
								if(down_path.isEmpty())
								{
									return(Math.abs(current_max-Math.round(position))+Math.abs(current_max-floor));
								}else{
									return(Math.abs(current_max-Math.round(position))+Math.abs(current_max-down_max)+Math.abs(down_max-floor));
								}
							}
						}else{ // nextPath = false, position < floor, up, "Down"
							if(up_path.isEmpty())
							{
								if(down_path.isEmpty())
								{
									return(Math.abs(current_max-Math.round(position))+Math.abs(current_max-floor));
								}else{
									return(Math.abs(current_max-Math.round(position))+Math.abs(current_max-down_max)+Math.abs(down_max-floor));
								}
							}else{
								if(down_path.isEmpty())
								{
									return(Math.abs(current_max-Math.round(position))+Math.abs(current_max-up_min)+Math.abs(up_max-up_min)+Math.abs(up_max-floor));
								}else{
									return(Math.abs(current_max-Math.round(position))+Math.abs(current_max-up_min)+Math.abs(up_max-up_min)+Math.abs(up_max-down_max)+Math.abs(down_max-floor));
								}
							}
						}
					}else{
						if(direction_current_path) // nextPath = true, position > floor, up, "Down"
						{
							if(up_path.isEmpty())
							{
								if(down_path.isEmpty())
								{
									return(Math.abs(current_max-Math.round(position))+Math.abs(current_max-floor));
								}else{
									return(Math.abs(current_max-Math.round(position))+Math.abs(current_max-down_max)+Math.abs(down_max-floor));
								}
							}else{
								if(down_path.isEmpty())
								{
									return(Math.abs(current_max-Math.round(position))+Math.abs(current_max-floor));
								}else{
									return(Math.abs(current_max-Math.round(position))+Math.abs(current_max-down_max)+Math.abs(down_max-floor));
								}
							}
						}else{
							if(up_path.isEmpty())
							{
								if(down_path.isEmpty())
								{
									return(Math.abs(current_max-Math.round(position))+Math.abs(current_max-floor));
								}else{
									return(Math.abs(current_max-Math.round(position))+Math.abs(current_max-down_max)+Math.abs(down_max-floor));
								}
							}else{
								if(down_path.isEmpty())
								{
									return(Math.abs(current_max-Math.round(position))+Math.abs(current_max-up_min)+Math.abs(up_max-up_min)+Math.abs(up_max-floor));
								}else{
									return(Math.abs(current_max-Math.round(position))+Math.abs(current_max-up_min)+Math.abs(up_max-up_min)+Math.abs(up_max-down_max) + Math.abs(down_max-floor));
								}
							}
						}
					}
				}
			}else{ // Not moving
				return(Math.abs(Math.round(position)-floor));
			}
		}
		return 0;
	}
	
	public synchronized void wakeUp(){
		wakeUp = true;
		notifyAll();
	}
	
	/*elevators position is updated all the time, but notify elevator when a certain floor is reached*/
	public synchronized void updatePosition(float position)
	{
		this.position = position;
		if(Math.abs(position-Math.round(position)) < 0.04)
		{
			System.out.println("Elevator " + id + " at " + Math.round(position) + " floor");
			notifyAll();
		}
	}
	
	/*floorButtonPressed function call by controller when new event is dispatched to a certain elevator thread*/
	public synchronized void floorButtonPressed(int floor, String direction)
	{
		if(direction.equals("Up"))  //up floor button is pressed
		{
			if(moving)  
			{
				if(up)  //elevator is moving up
				{
					//the current position of elevator is higher than the floor which button is pressed
					//we will ignore it this time, and put it in the next up path
					if(position > floor)  
					{
						add_up_path(floor);
					}else{  //the current position is lower than floor which button is pressed
						
						if(direction_current_path) //the current path is up_path, we will give them a ride, put event in current path
						{
							add_current_path(floor);
						}else{ // the current path is down_path, different direction we have, so ignore it this time
							add_up_path(floor);
						}
					}
				}else{  //elevator is moving down
					if(position > floor) //the current position of elevator is higher than the floor which button is pressed
					{
						if(direction_current_path) //the current path is up_path, elevator will move up again, so give them a ride
						{
							add_current_path(floor);
						}else{ //different direction, up next time
							add_up_path(floor);
						}
					}else{ //the current position is lower than floor which button is pressed, up next time
						add_up_path(floor);
					}
				}
			}else{ //elevator is not moving
				add_up_path(floor);
			}
		}else{ // the floor button pressed, with direction down
			if(moving)
			{
				if(down)  //the elevator is moving down
				{
					if(position < floor) //the current position is lower than floor which button is pressed, down next time 
					{
						add_down_path(floor);
					}else{ //the current position is higher than floor which button is pressed,
						if(!direction_current_path) //current path is down path, give him a ride
						{
							add_current_path(floor);
						}else{ //current path is up path, different direction, down next time
							add_down_path(floor);
						}
					}
				}else{  //the elevator is moving up
					if(position < floor) //the current position is lower than floor which button is pressed
					{
						if(!direction_current_path) //current path is down path, elevator will go down, give him a ride
						{
							add_current_path(floor);
						}else{ //current path is up path, down next time
							add_down_path(floor);
						}
					}else{  //the current position is higher than floor which button is pressed
						add_down_path(floor);
					}
				}
			}else{  //elevator is not moving
				add_down_path(floor);
			}
		}
		newEvent = true; //set flag this is a new event
		notifyAll();
	}
	
	
	/*This function deal with the event which is triggered by button in elevator*/
	public synchronized void controlButtonPressed(int floor)
	{
		System.out.println("Someone at elevator " + id + " pressed " + floor + " floor");
		if(moving)  
		{
			if(up) //elevator moving up
			{
				if(floor > position)  //passenger will go up
				{
					if(direction_current_path)  //current path is up path, give him a ride
					{
						add_current_path(floor);
					}else{  //current path is down path, wait in up path
						add_up_path(floor);
					}
				}else{   //passenger will do down
					if(direction_current_path)  //current path is up path, wait in down path
					{
						add_down_path(floor);
					}else{ //current path is down path, give him a ride
						add_current_path(floor);
					}
				}
			}else{  //elevator moving down
				if(floor < position)  //passenger will go down
				{
					if(direction_current_path)  //current path is up path, wait in down path
					{
						add_down_path(floor);
					}else{ //current path is down path, give him a ride
						add_current_path(floor);
					}
				}else{ //passenger will go up
					if(direction_current_path)  //current path is up path, give him a ride
					{
						add_current_path(floor);
					}else{ //current path is down path, wait in up path
						add_up_path(floor);
					}
				}
			}
		}else{ //elevator is not moving
			if(position > floor)  //passenger will go down
			{
				add_down_path(floor);
			}else{ //passenger will go up
				add_up_path(floor);
			}
		}
		newEvent = true; //new event trigger
		notifyAll();
	}
	
	public void run()
	{
		try
		{
			while(true)
			{
				while(!newEvent && !moving) //there is no new event and elevator is not moving
				{
					synchronized(this)
					{
						wait();
					}
			}
			synchronized(this)  //wake up if new event trigger
			{
				newEvent = false; //reset
				if(!moving) //elevator is waiting
				{
					if(up_path.isEmpty())  //new down event
					{
						//read new even and compare with current position of elevator
						if(down_path.peek() > position)  //elevator position is lower than floor
						{
							
							controller.up(id); //move N'th elevator upwards
							moving = true;
							down = false;
							up = true;
						}else{ //elevator position is higher than floor
							controller.down(id); //move N'th elevator downwards
							moving = true;
							down = true;
							up = false;
						}
						System.out.println("Elevator " + id + " current path is down");
						direction_current_path = false; //set flag of current path to down
						current_path = new PriorityBlockingQueue<Integer>(down_path);
						current_max = down_max;
						current_min = down_min;
						down_path = new PriorityBlockingQueue<Integer>(11, Collections.reverseOrder());
						down_max = 0;
						down_min = floor;
					}else{//new up event
						if(up_path.peek() > position) //elevator position is lower than floor
						{
							controller.up(id); //move N'th elevator upwards
							moving = true;
							up = true;
							down = false;
						}else{ //elevator position is higher than floor
							controller.down(id); //move N'th elevator downwards
							moving = true;
							up = false;
							down = true;
						}
						direction_current_path = true; //set flag of current path to up
						System.out.println("Elevator " + id + " current path is up");
						current_path = new PriorityBlockingQueue<Integer>(up_path);
						current_max = up_max;
						current_min = up_min;
						up_path = new PriorityBlockingQueue<Integer>();
						up_max = 0;
						up_min = floor;
					}
				}else{ //elevator is not moving
					while(Math.abs(position-Math.round(position)) > 0.04)
					{
						wait();
					}
					if(Math.abs(current_path.peek()-Math.round(position)) < 0.04) //position of elevator get close to a certain floor
					{
						controller.stop(id); //ask elevator to stop for a while
						controller.openDoor(id); //open door
						alarm.sleep(2000); //wait 2 seconds
						while(!wakeUp) 
						{
							wait(); 
						}
						wakeUp = false;
						controller.closeDoor(id); //close door
						alarm.sleep(2000);
						while(!wakeUp)
						{
							wait();
						}
						wakeUp = false;
						current_path.remove(); //finish task
						if(current_path.isEmpty())  //change direction of path
						{
							if(direction_current_path)  //current path is up
							{
								if(!down_path.isEmpty()) //waiting task in down path,set current path to down path
								{
									current_path = new PriorityBlockingQueue<Integer>(down_path);
									current_max = down_max;
									current_min = down_min;
									down_path = new PriorityBlockingQueue<Integer>(11, Collections.reverseOrder());
									down_max = 0;
									down_min = floor;
									direction_current_path = false;
									pick_up_passenger(current_path, position);
								}else{  //no task in down path
									if(!up_path.isEmpty()) //task in up path
									{
										current_path = new PriorityBlockingQueue<Integer>(up_path);
										current_max = up_max;
										current_min = up_min;
										up_path = new PriorityBlockingQueue<Integer>();
										up_max = 0;
										up_min = floor;
										direction_current_path = true;
										pick_up_passenger(current_path, position);
									}else{ // no task anymore
										moving = false;
										up = false;
										down = false;
									}
								}
							}else{  //current path is down
								if(!up_path.isEmpty()){   //more task in up path
									current_path = new PriorityBlockingQueue<Integer>(up_path);
									current_max = up_max;
									current_min = up_min;
									up_path = new PriorityBlockingQueue<Integer>();
									up_max = 0;
									up_min = floor;
									direction_current_path = true;
									pick_up_passenger(current_path, position);
								}else{ //no task in up path
									if(!down_path.isEmpty()) //more task in down path
									{
										current_path = new PriorityBlockingQueue<Integer>(up_path);
										current_max = up_max;
										current_min = up_min;
										up_path = new PriorityBlockingQueue<Integer>();
										up_max = 0;
										up_min = floor;
										direction_current_path = false;
										pick_up_passenger(current_path, position);
									}else{ //no more task to do
										moving = false;
										up = false;
										down = false;
									}
								}
							}
						}else{ //has other tasks to do
							pick_up_passenger(current_path, position);
						}
					}
				}
			}
		}
		}catch(InterruptedException e){}
	}
	
	private synchronized void add_up_path(int floor)
	{
		System.out.println("Add this request to up_path");
		if(!up_path.contains(floor))
		{
			up_path.add(floor);
			updateUp(floor);
		}
	}
	
	private synchronized void add_down_path(int floor)
	{
		System.out.println("Add this request to down_path");
		if(!down_path.contains(floor))
		{
			down_path.add(floor);
			updateDown(floor);
		}
	}
	
	private synchronized void add_current_path(int floor)
	{
		System.out.println("Add this request to current_path");
		if(!current_path.contains(floor))
		{
			current_path.add(floor);
			updateCurrent(floor);
		}
	}
	
	private synchronized void pick_up_passenger(PriorityBlockingQueue<Integer> current_path, float position)
	{
		if(current_path.peek() > position)  //position lower than calling floor
		{
			controller.up(id); //move upwards
			moving = true;
			up = true;
			down = false;
		}else{  //position higher than calling floor
			controller.down(id); //move downwards
			moving = true;
			down = true;
			up = false;
		}
	}
	
}
