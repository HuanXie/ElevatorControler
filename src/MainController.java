import java.util.LinkedList;
import java.util.Scanner;

public class MainController{
	static Courier c;
	static Elevator[] elevators;
	
	public static void main(String[] args)
	{
		c = new Courier();
		
		Scanner s = new Scanner(System.in);
		System.out.println("Number of elevators? ");
		int total = s.nextInt();
		System.out.println("Floor? ");
		int floor = s.nextInt();
		s.close();
		
		elevators = new Elevator[total];
		for(int i = 0; i < total; i++)
		{
			elevators[i] = new Elevator(i+1,floor);
			elevators[i].start();
		}
		
		String m;
		while(true)
		{
			m = c.receive();
			messageParser(m);
		}
	}
	
	public synchronized static void up(int ID)
	{
		c.send("m" + " " + ID + " " + "1");
	}
	
	public synchronized static void stop(int ID)
	{
		c.send("m" + " " + ID + " " + "0");
	}
	
	public synchronized static void down(int ID)
	{
		c.send("m" + " " + ID + " " + "-1");
	}
	
	public synchronized static void openDoor(int ID)
	{
		c.send("d" + " " + ID + " " + "1");
	}
	
	public synchronized static void closeDoor(int ID)
	{
		c.send("d" + " " + ID + " " + "-1");
	}
	
	private static void messageParser(String message)
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
				elevators[i-1].controlButtonPressed(Integer.parseInt(parts[2]));
				break;
			}
			case "v":
			{
			}
		}
	}
}

