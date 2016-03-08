import java.util.Scanner;

public class Init {
	public static void main(String[] args)
	{
		Scanner s = new Scanner(System.in);
		System.out.println("Number of elevators? ");
		int numberOfElevators = s.nextInt();
		System.out.println("Number of floors? ");
		int numberOfFloors = s.nextInt();
		s.close();
		
		Elevator[] elevators = new Elevator[numberOfElevators];
		MainController controller = new MainController(numberOfElevators, numberOfFloors, elevators);
		for(int i = 0; i < elevators.length; i++)
		{
			elevators[i] = new Elevator(i+1,numberOfFloors,controller);
		}
		controller.start();
		for(int i = 0; i < elevators.length; i++)
		{
			elevators[i].start();
		}
	}
}
