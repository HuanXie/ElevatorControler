/*
 * This class is handling communication between elevators and the UI. When message comes from UI, it parse the message
 * and call respective function. All sending function are synchronized so at most one elevator can send instruction to
 * UI. The MainController class has Floorbutton array to simulate floor buttons at each floor, when someone pressed
 * that button, corresponding floorbutton and its direction will be set to true, and if one button is pressed. Pressing
 * it again do not have any effect, you can pressed the button again after elevator arrived and leaves that floor. And
 * when floorbutton are pressed, it calculates score for every elevators and choose the one with least score. If all
 * score are same, we choose the one elevator that are not moving, else, we choose the first one.
 */
import java.util.LinkedList;

public class MainController extends Thread{
	Courier c; // Courier between UI and controller
	Elevator[] elevators; // All elevators
	int numberOfElevators; // Number of elevators to be created
	int numberOfFloors; // Number of floors at building
	Floorbutton[] floorButtons; // Floorbutton at each floor
	
	// Constructor, setting all necessary parameters
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
	
	// Overwrites run method in Thread class
	public void run()
	{		
		// Parse every message received and makes some action
		while(true)
		{
			messageParser(c.receive());
		}
	}
	
	// Function to move elevator up
	public synchronized void up(int ID)
	{
		c.send("m" + " " + ID + " " + "1");
	}
	
	// Function to stop elevator
	public synchronized void stop(int ID)
	{
		c.send("m" + " " + ID + " " + "0");
	}
	
	// Function to move elevator down
	public synchronized void down(int ID)
	{
		c.send("m" + " " + ID + " " + "-1");
	}
	
	// Function to open door
	public synchronized void openDoor(int ID)
	{
		c.send("d" + " " + ID + " " + "1");
	}
	
	// Function to close door
	public synchronized void closeDoor(int ID)
	{
		c.send("d" + " " + ID + " " + "-1");
	}
	
	// Function to tell controller that a elevator has arrived and set corresponding floorbutton to false
	public synchronized void done(int floor, boolean up)
	{
		if(up)
		{
			floorButtons[floor].up = false;
		}else{
			floorButtons[floor].down = false;
		}
	}
	
	// Parse the received message and persome some actions
	private void messageParser(String message)
	{
		String[] parts = message.split(" "); // Split the message by blank space
		int i; 
		switch(parts[0]) // Based on first character
		{
			case "f": // Position changed for certain elevator
			{
				i = Integer.parseInt(parts[1]); // ID of elevator
				elevators[i-1].updatePosition(Float.parseFloat(parts[2])); // Upgrade that elevator's position
				break;
			}
			case "b": // Someone pressed floorbutton
			{
				i = Integer.parseInt(parts[1]); // ID of elevator
				if(Integer.parseInt(parts[2]) == 1) // If up pressed
				{
					if(floorButtons[i].up) // If already pressed
					{
						System.out.println(message + " already processing by another elevator");
						break;
					}else{ // Else we mark it as pressed
						floorButtons[i].up = true;
					}
				}else{ // Else down is pressed
					if(floorButtons[i].down) // If already pressed
					{
						System.out.println(message + " already processing by another elevator");
						break;
					}else{ // Else we mark it as pressed
						floorButtons[i].down = true;
					}
				}
				LinkedList<Integer> allPosition = new LinkedList<Integer>(); // Linkedlist to contain all suitable elevators
				int current; // Current score of elevator
				int minimum = Integer.MAX_VALUE; // Set minimum to max_value of integer
				// Loop through all elevators and get score each one of them. All lowest score saves to linkedlist
				for(int j = 0; j < elevators.length; j++)
				{
					current = elevators[j].score(i, ((parts[2].equals("-1"))? "Down":"Up")); // Score of current elevator
					if(current <= minimum) // If that is less or equal than minimum
					{
						if(current < minimum) // If that is less than minimum
						{
							allPosition.clear(); // Remove all old elevators
							allPosition.add(j); // Add the new elevator
						}else{ // Else that is equals than minimum
							allPosition.add(j); // This elevator have same lowest score
						}
						minimum = current; // Update minimum
					}
				}
				// Prioritize elevator that are not moving
				for(int j:allPosition)
				{
					if(!elevators[j].isMoving()) // If this elevator not moving
					{
						allPosition.clear(); // Remove all other elevators
						allPosition.add(j); // Add this one
						break;
					}
				}
				elevators[allPosition.getFirst()].floorButtonPressed(i, (parts[2].equals("-1"))? "Down":"Up"); // This elevator gets the mission
				System.out.println("Elevator " + (allPosition.getFirst()+1) + " takes this request"); 
				break;
			}
			case "p": // Someone pressed button inside elevator
			{
				i = Integer.parseInt(parts[1]); // Get ID of elevator
				if(Integer.parseInt(parts[2]) == 32000) // If 32000, that means stop
				{
					elevators[i-1].stopElevator(); // Stop the elevator
					System.out.println("Stopped elevator " + i);
				}else{ // Else we have to move to that floor
					elevators[i-1].controlButtonPressed(Integer.parseInt(parts[2])); // Send command to this elevator
				}
				break;
			}
		}
	}
}

