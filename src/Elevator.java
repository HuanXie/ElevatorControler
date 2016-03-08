import java.util.Collections;
import java.util.concurrent.PriorityBlockingQueue;

public class Elevator extends Thread{
	int id; // ID of this elevator
	int floor; // Number of floors
	float position; // Current position of elevator
	boolean moving;  // Status of elevator: moving or not
	boolean up;  // Status of elevator: moving up or not
	boolean down; // Status of elevator: moving down or not
	boolean newEvent; // New event trigger
	boolean direction_current_path; // True when current == up_path, false when current == down_path
	boolean wakeUp; // Alarm object to control wake up time of elevator
	Alarm alarm;  //help object to ask thread sleep without holding lock in hand
	MainController controller; // Main controller of elevators
	PriorityBlockingQueue<Integer> up_path;  // When floor button with direction up is pressed, put it in queue up_path
	PriorityBlockingQueue<Integer> down_path; // When floor button with direction down is pressed, put it in queue down_path
	PriorityBlockingQueue<Integer> current_path; // Current path elevator is used
	Floorbutton[] floorButtons; // Button at each floor to simulate if any of them are pressed
	int current_max; // Maximum floor in current_path
	int current_min; // Minimum floor in current_path
	int down_max; // Maximum floor in down_path
	int down_min; // Minimum floor in down_path
	int up_max; // Maximum floor in up_path
	int up_min; // Minimum floor in up_path
	
	// Constructor, setting all to false and initialize everything
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
		floorButtons = new Floorbutton[floor+1];
		for(int i = 0; i < floorButtons.length; i++)
		{
			floorButtons[i] = new Floorbutton();
		}
	}
	
	// Stop the elevator and reinitialize everything
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
	
	// Returns if elevator is moving
	public synchronized boolean isMoving(){
		return moving;
	}

	// Update current_max or current_min if necessary
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
	
	// Update up_max or up_min if necessary
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
	
	// Update down_max or down_min if necessary
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
	
	// Score function returns how much floors elevator must go before going to this specific floor. We have all task in current-,up
	// and down-path. So based on current path, moving direction and maximum and minimum in each path we can calculate the difference.
	public synchronized int score(int floor, String direction)
	{
		int up_max = floor;
		int down_max = 0;
		if(direction.equals("Up"))  // Up button pressed
		{
			if(moving && up) // Elevator is moving up
			{
				if(position > floor) // The current position of elevator is higher than the floor which button is pressed
				{
					if(direction_current_path) // Direction_current_path = up, position > floor, event = up button pressed
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
					}else{ // Direction_current_path = down, position > floor, event = up button pressed
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
				}else{ // Positon < floor
					if(direction_current_path) // Direction_current_path = up, position < floor, up, "Up"
					{
						// The current position is lower than floor which button is pressed, the people is on the same direction with current path
						// no matter there is other tasks or not, only need count the distance between current position and people
						return(Math.abs(floor-Math.round(position)));
					}else{ // Direction_current_path = down, position < floor, up, "Up"
						if(up_path.isEmpty()) 
						{ 
							// No other task in the up direction, count distance on the way up to the top and down to the floor
							return(Math.abs(current_max-Math.round(position))+Math.abs(current_max-floor));
						}else{
							// There is other tasks in the up direction
							return(Math.abs(current_max-Math.round(position))+Math.abs(current_max-up_min)+Math.abs(floor-up_min));
							}
						}
					}
			}else if (moving && down){  
				if(position > floor)
				{
					if(direction_current_path) // Direction_current_path = up, position > floor, elevator is moving up
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
					}else{ // Direction_current_path = down, position > floor, elevator is moving up
						if(up_path.isEmpty())
						{
							//no task in up direction, event handle on the way down
							return(Math.abs(Math.round(position)-current_min)+Math.abs(floor-current_min));
						}else{
							//there is task in the up direction, handle them first
							return(Math.abs(Math.round(position)-current_min)+Math.abs(current_min-up_min)+Math.abs(floor-up_min));
							}
						}
				}else{
					if(direction_current_path) // Direction_current_path = up, position < floor,  elevator moving down, event = up button pressed
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
					}else{ // Direction_current_path = down, position < floor
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
			}else{ // Not moving
				return(Math.abs(Math.round(position)-floor));
				}
		}else{ // The floor button pressed, with direction down
			if(moving && down)
			{
				if(position < floor)
				{
					if(direction_current_path) // Direction_current_path = up, position < floor, down, "Down"
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
					}else{ // Direction_current_path = down, position < floor, down, "Down"
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
					if(direction_current_path) // Direction_current_path = up, position > floor, down, "Down"
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
					}else{ // Direction_current_path = down, position > floor, down, "Down"
						// Event handle on the way down
						return(Math.abs(Math.round(position)-floor));
					}
				}
			}else if(moving && up)  // Elevator elevator is moving up 
				{
				if(position < floor)
				{
					if(direction_current_path) // Direction_current_path = up, position < floor, up, "Down"
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
					}else{ // Direction_current_path = down, position < floor, up, "Down"
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
					if(direction_current_path) // Direction_current_path = up, position > floor, up, "Down"
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
					}else{  // Direction_current_path = down, position > floor, moving up, event= "Down"
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
			else{ // Not moving
				return(Math.abs(Math.round(position)-floor));
			}
		}
		return 0;
	}
	
	// Used in alarm to wake up this thread
	public synchronized void wakeUp(){
		wakeUp = true;
		notifyAll();
	}
	
	// Elevators position is updated all the time, but notify elevator when a certain floor is reached
	public synchronized void updatePosition(float position)
	{
		this.position = position;
		// If we are at certain floor, notify
		if(Math.abs(position-Math.round(position)) < 0.04)
		{
			System.out.println("Elevator " + id + " at " + Math.round(position) + " floor");
			notifyAll();
		}
	}
	
	// FloorButtonPressed function call by controller when new event assigned to this elevator
	public synchronized void floorButtonPressed(int floor, String direction)
	{
		if(direction.equals("Up"))  // Up floor button is pressed
		{
			floorButtons[floor].up = true; // Set this floorbutton to true
			if(moving)  
			{
				if(up)  // Elevator is moving up
				{
					// The current position of elevator is higher than the floor which button is pressed
					// We will ignore it this time, and put it in the next up path
					if(position > floor)  
					{
						add_up_path(floor);
					}else{  // The current position is lower than floor which button is pressed
						
						if(direction_current_path) // The current path is up_path, we will give them a ride, put event in current path
						{
							add_current_path(floor);
						}else{ // The current path is down_path, different direction we have, so ignore it this time
							add_up_path(floor);
						}
					}
				}else{  // Elevator is moving down
					if(position > floor) // The current position of elevator is higher than the floor which button is pressed
					{
						if(direction_current_path) // The current path is up_path, elevator will move up again, so give them a ride
						{
							add_current_path(floor);
						}else{ // Different direction, up next time
							add_up_path(floor);
						}
					}else{ // The current position is lower than floor which button is pressed, up next time
						add_up_path(floor);
					}
				}
			}else{ // Elevator is not moving
				add_up_path(floor);
			}
		}else{ //  The floor button pressed, with direction down
			floorButtons[floor].down = true; // Set this floorbutton to true
			if(moving)
			{
				if(down)  // The elevator is moving down
				{
					if(position < floor) // The current position is lower than floor which button is pressed, down next time 
					{
						add_down_path(floor);
					}else{ // The current position is higher than floor which button is pressed,
						if(!direction_current_path) // Current_path is down_path, give him a ride
						{
							add_current_path(floor);
						}else{ // Current path is up path, different direction, down next time
							add_down_path(floor);
						}
					}
				}else{  // The elevator is moving up
					if(position < floor) // The current position is lower than floor which button is pressed
					{
						if(!direction_current_path) // Current path is down path, elevator will go down, give him a ride
						{
							add_current_path(floor);
						}else{ // Current path is up path, down next time
							add_down_path(floor);
						}
					}else{  // The current position is higher than floor which button is pressed
						add_down_path(floor);
					}
				}
			}else{  // Elevator is not moving
				add_down_path(floor);
			}
		}
		newEvent = true; // Set flag this is a new event
		notifyAll();
	}
	
	
	// This function deal with the event which is triggered by button in elevator
	public synchronized void controlButtonPressed(int floor)
	{
		System.out.println("Someone at elevator " + id + " pressed " + floor + " floor");
		if(moving)  
		{
			if(up) // Elevator moving up
			{
				if(floor > position)  // Passenger wants to go up
				{
					if(direction_current_path)  // Current path is up_path, give him a ride
					{
						add_current_path(floor);
					}else{  // Current path is down path, wait in up_path
						add_up_path(floor);
					}
				}else{   // Passenger wants to go down
					if(direction_current_path)  // Current path is up path, wait in down path
					{
						add_down_path(floor);
					}else{ // Current path is down path, give him a ride
						add_current_path(floor);
					}
				}
			}else{  // Elevator moving down
				if(floor < position)  // Passenger wants go down
				{
					if(direction_current_path)  // Current path is up path, wait in down path
					{
						add_down_path(floor);
					}else{ // Current path is down path, give him a ride
						add_current_path(floor);
					}
				}else{ // Passenger wants to go up
					if(direction_current_path)  // Current path is up path, give him a ride
					{
						add_current_path(floor);
					}else{ // Current path is down path, wait in up path
						add_up_path(floor);
					}
				}
			}
		}else{ // Elevator is not moving
			if(position > floor)  // Passenger wants to go down
			{
				add_down_path(floor);
			}else{ // Passenger wants to go up
				add_up_path(floor);
			}
		}
		newEvent = true; // New event trigger
		notifyAll();
	}
	
	public void run()
	{
		try
		{
			while(true)
			{
				while(!newEvent && !moving) // There is no new event and elevator is not moving
				{
					synchronized(this)
					{
						wait(); // We wait for any notify
					}
			}
			synchronized(this)  // Wake up if new event trigger
			{
				newEvent = false; // Reset flag
				if(!moving) // Elevator is waiting
				{
					if(up_path.isEmpty())  // New down event
					{
						// Read new event and compare with current position of elevator
						if(down_path.peek() > position)  // Elevator position is lower than floor
						{
							controller.up(id); // Move elevator upwards
							moving = true;
							down = false;
							up = true;
						}else{ // Elevator position is higher than floor
							controller.down(id); // Move elevator downwards
							moving = true;
							down = true;
							up = false;
						}
						System.out.println("Elevator " + id + " current path is down");
						direction_current_path = false; //set flag of current path to down
						current_path = new PriorityBlockingQueue<Integer>(down_path); // Copy down_path to current_path
						current_max = down_max; // Update current_max
						current_min = down_min; // Update current_min
						down_path = new PriorityBlockingQueue<Integer>(11, Collections.reverseOrder()); // Reset down_path
						down_max = 0; // Reset down_max
						down_min = floor; // Reset down_min
					}else{ // New up event
						if(up_path.peek() > position) // Elevator position is lower than floor
						{
							controller.up(id); // Move elevator upwards
							moving = true;
							up = true;
							down = false;
						}else{ // Elevator position is higher than floor
							controller.down(id); // Move elevator downwards
							moving = true;
							up = false;
							down = true;
						}
						direction_current_path = true; // Set flag of current path to up
						System.out.println("Elevator " + id + " current path is up");
						current_path = new PriorityBlockingQueue<Integer>(up_path); // Copy up_path to current_path
						current_max = up_max; // Update current_max
						current_min = up_min; // Update up_min
						up_path = new PriorityBlockingQueue<Integer>(); // Reset up_path
						up_max = 0; // Reset up_max
						up_min = floor; // Reset up_min
					}
				}else{ // Else elevator is moving
					// We wait until we reach any floor
					while(Math.abs(position-Math.round(position)) > 0.04)
					{
						wait();
					}
					if(Math.abs(current_path.peek()-Math.round(position)) < 0.04) // Check if we have to stop at this floor
					{
						controller.stop(id); // Ask elevator to stop for a while
						controller.openDoor(id); // Open door
						alarm.sleep(2000); // Wait 2 seconds
						while(!wakeUp) 
						{
							wait(); 
						}
						wakeUp = false;
						controller.closeDoor(id); // Close door
						alarm.sleep(2000); // Wait 2 seconds
						while(!wakeUp)
						{
							wait();
						}
						wakeUp = false;
						int i = current_path.remove(); // Remove that floor and update floorbuttons for itself and at controller
						if(direction_current_path) // If we are using up_path
						{
							floorButtons[i].up = false; // Up button at this floor is turned off
							controller.done(i, true); // Up button at this floor in controller is turned off
						}else{
							floorButtons[i].down = false; // Down button at this floor is turned off
							controller.done(i, false); // Down button at this floor in controller is turned off
						}
						if(current_path.isEmpty())  // No more task in current_path
						{
							if(direction_current_path)  // Current path is up
							{
								if(!down_path.isEmpty()) // Switch path and check if down_path is empty
								{
									current_path = new PriorityBlockingQueue<Integer>(down_path); // Copy down_path to current_path
									current_max = down_max; // Update current_max
									current_min = down_min; // Update current_min
									down_path = new PriorityBlockingQueue<Integer>(11, Collections.reverseOrder()); // Reset down_path
									down_max = 0; // Reset down_max
									down_min = floor; // Reset down_min
									direction_current_path = false; // Current path is now down_path
									moveToNextFloor(current_path, position);
								}else{  // No task in down_path
									if(!up_path.isEmpty()) // Check for task in up_path
									{
										current_path = new PriorityBlockingQueue<Integer>(up_path); // Copy up_path to current_path
										current_max = up_max; // Update current_max
										current_min = up_min; // Update current_min
										up_path = new PriorityBlockingQueue<Integer>(); // Reset up_path
										up_max = 0; // Reset up_max
										up_min = floor; // Rest up_min
										direction_current_path = true; // Current path is up_path
										moveToNextFloor(current_path, position); // Move to next floor in current_path
									}else{ // No task anymore, elevator is not moving
										moving = false;
										up = false;
										down = false;
									}
								}
							}else{  // Else current_path is down_path
								if(!up_path.isEmpty()){   // Check for more task in up_path
									current_path = new PriorityBlockingQueue<Integer>(up_path); // Copy up_path to current_path
									current_max = up_max; // Update current_max
									current_min = up_min; // Update current_min
									up_path = new PriorityBlockingQueue<Integer>(); // Reset up_path
									up_max = 0; // Reset up_max
									up_min = floor; // Reset up_min
									direction_current_path = true; // Current_path is up_path
									moveToNextFloor(current_path, position); // Move to next floor in current_path
								}else{ // Else no task in up path, continue using up
									if(!down_path.isEmpty()) // Check if there are more task in down path
									{
										current_path = new PriorityBlockingQueue<Integer>(down_path); // Copy down_path to current_path
										current_max = down_max; // Update current_max
										current_min = down_min; // Update current_min
										down_path = new PriorityBlockingQueue<Integer>(11, Collections.reverseOrder()); // Reset down_path
										down_max = 0; // Reset down_max
										down_min = floor; // Reset down_min
										direction_current_path = false; // Current path is now down_path
										moveToNextFloor(current_path, position);
									}else{ // No more task to do, elevator is not moving
										moving = false;
										up = false;
										down = false;
									}
								}
							}
						}else{ // Else current_path is not empyt, more task
							moveToNextFloor(current_path, position);
						}
					}
				}
			}
		}
		}catch(InterruptedException e){}
	}
	
	// Add this floor to up_path
	private synchronized void add_up_path(int floor)
	{
		System.out.println("Elevator " + id + ". Add this request to up_path");
		if(!up_path.contains(floor))
		{
			up_path.add(floor);
			updateUp(floor);
		}
	}
	
	// Add this floor to down_path
	private synchronized void add_down_path(int floor)
	{
		System.out.println("Elevator " + id + ". Add this request to down_path");
		if(!down_path.contains(floor))
		{
			down_path.add(floor);
			updateDown(floor);
		}
	}
	
	// Add this floor to current_path
	private synchronized void add_current_path(int floor)
	{
		System.out.println("Elevator " + id + ". Add this request to current_path");
		if(!current_path.contains(floor))
		{
			current_path.add(floor);
			updateCurrent(floor);
		}
	}
	
	// Move to next floor at current_path
	private synchronized void moveToNextFloor(PriorityBlockingQueue<Integer> current_path, float position)
	{
		if(current_path.peek() > position)  // Position lower than calling floor
		{
			controller.up(id); // Move upwards
			moving = true;
			up = true;
			down = false;
		}else{  // Position higher than calling floor
			controller.down(id); // Move downwards
			moving = true;
			down = true;
			up = false;
		}
	}
	
}
