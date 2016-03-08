import java.util.LinkedList;

public class MainController extends Thread{
	Courier c;
	Elevator[] elevators;
	int numberOfElevators;
	int numberOfFloors;
	
	
	MainController(int numberOfElevators, int numberOfFloors, Elevator[] elevators)
	{
		this.numberOfElevators = numberOfElevators;
		this.numberOfFloors = numberOfFloors;
		this.elevators = elevators;
		c = new Courier();
	}
	
	public void run()
	{		
		String m;
		while(true)
		{
			m = c.receive();
			messageParser(m);
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
			case "v":
			{
			}
		}
	}
}

