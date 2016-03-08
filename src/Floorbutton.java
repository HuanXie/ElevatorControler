/*
 * This class represent up and down button at elevator for each floor
 */

public class Floorbutton {
	public boolean up; // If up button at this floor is pressed
	public boolean down; // If down button at this floor is pressed
	
	// Constructor, initially all button are not pressed
	Floorbutton()
	{
		up = false;
		down = false;
	}
}
