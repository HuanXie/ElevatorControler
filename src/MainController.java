import java.util.LinkedList;

public class MainController extends Thread{
	Courier c;
	Elevator[] elevators;
	int numberOfElevators;
	int numberOfFloors;
	Floorbutton[] floorButtons;
	
	
	MainController(int numberOfElevators, int numberOfFloors, Elevator[] elevators)
	{
		this.numberOfElevators = numberOfElevators;
		this.numberOfFloors = numberOfFloors;
		this.elevators = elevators;
		floorButtons = new Floorbutton[numberOfFloors+1];
		for(int i = 0; i < floorButtons.length; i++)
		{
			floorButtons[i] = new Floorbutton();
		}
		c = new Courier();
	}
	
	public void run()
	{		
		while(true)
		{
			messageParser(c.receive());
		}
	}
	
	public synchronized void up(int ID)
	{
		c.send("m" + " " + ID + " " + "1");
	}
	
	public synchronized void stop(int ID)
	{
		c.send("m" + " " + ID + " " + "0");
	}
	
	public synchronized void down(int ID)
	{
		c.send("m" + " " + ID + " " + "-1");
	}
	
	public synchronized void openDoor(int ID)
	{
		c.send("d" + " " + ID + " " + "1");
	}
	
	public synchronized void closeDoor(int ID)
	{
		c.send("d" + " " + ID + " " + "-1");
	}
	
	public synchronized void done(int floor, boolean up)
	{
		if(up)
		{
			floorButtons[floor].up = false;
		}else{
			floorButtons[floor].down = false;
		}
	}
	
	private void messageParser(String message)
	{
		String[] parts = message.split(" ");
		int i;
		switch(parts[0])
		{
			case "f":
			{
				i = Integer.parseInt(parts[1]);
				elevators[i-1].updatePosition(Float.parseFloat(parts[2]));
				break;
			}
			case "b":
			{
				i = Integer.parseInt(parts[1]);
				if(Integer.parseInt(parts[2]) == 1)
				{
					if(floorButtons[i].up)
					{
						break;
					}else{
						floorButtons[i].up = true;
					}
				}else{
					if(floorButtons[i].down)
					{
						break;
					}else{
						floorButtons[i].down = true;
					}
				}
				LinkedList<Integer> allPosition = new LinkedList<Integer>();
				int current;
				int minimum = Integer.MAX_VALUE;
				for(int j = 0; j < elevators.length; j++)
				{
					current = elevators[j].score(i, ((parts[2].equals("-1"))? "Down":"Up"));
					if(current <= minimum)
					{
						if(current < minimum)
						{
							allPosition.clear();
							allPosition.add(j);
						}else{
							allPosition.add(j);
						}
						minimum = current;
					}
				}
				for(int j:allPosition)
				{
					if(!elevators[j].isMoving())
					{
						allPosition.clear();
						allPosition.add(j);
						break;
					}
				}
				elevators[allPosition.getFirst()].floorButtonPressed(i, (parts[2].equals("-1"))? "Down":"Up");
				break;
			}
			case "p":
			{
				i = Integer.parseInt(parts[1]);
				if(Integer.parseInt(parts[2]) == 32000)
				{
					elevators[i-1].stopElevator();
				}else{
					elevators[i-1].controlButtonPressed(Integer.parseInt(parts[2]));
				}
				break;
			}
		}
	}
}

