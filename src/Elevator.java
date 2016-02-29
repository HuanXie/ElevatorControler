public class Elevator extends Thread{
	float position;
	boolean moving;
	boolean up;
	boolean down;
	FloorButton[] floors;
	Boolean[] controlPanel;
	Object update_position = new Object();
	Object update_floorButton = new Object();
	
	// Constructor, setting all to false
	Elevator(int floor)
	{
		position = 0f;
		moving = false;
		up = false;
		down = false;
		floors = new FloorButton[floor+1];
		controlPanel = new Boolean[floor+1];
		for(int i = 0; i < floors.length; i++)
		{
			floors[i] = new FloorButton();
			controlPanel[i] = false;
		}
	}
	
	public synchronized void updatePosition(float position)
	{
		this.position = position;
	}
	
	public synchronized void floorButtonPressed(int floor, String direction)
	{
		if(direction.equals("Up"))
		{
			floors[floor].up = true;
		}else{
			floors[floor].down = true;
		}
	}
	
	public synchronized void controlButtonPressed(int floor)
	{
		controlPanel[floor] = true;
	}
	
	public void run()
	{
		while(true)
		{
			
		}
	}
	
}
