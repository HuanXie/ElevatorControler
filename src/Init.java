/*
 * This class initialize all elevators and main controller and starts them
 */
import java.util.Scanner;

public class Init {
	public static void main(String[] args)
	{
		Scanner s = new Scanner(System.in); // Scanner to read input
		System.out.println("Number of elevators? "); // Ask user
		int numberOfElevators = s.nextInt(); // Read input
		System.out.println("Number of floors? "); // Ask user
		int numberOfFloors = s.nextInt(); // Read input
		s.close(); // Close scanner
		
		Elevator[] elevators = new Elevator[numberOfElevators]; // Create elevators
		MainController controller = new MainController(numberOfElevators, numberOfFloors, elevators); // Create controller
		// Initiallize all elevators
		for(int i = 0; i < elevators.length; i++)
		{
			elevators[i] = new Elevator(i+1,numberOfFloors,controller);
		}
		controller.start(); // Start controller
		// Start all elevators
		for(int i = 0; i < elevators.length; i++)
		{
			elevators[i].start();
		}
	}
}
