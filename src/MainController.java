import javax.xml.ws.handler.MessageContext;

public class MainController {
	static int floor = 0;
	final static int max_number_elevator = 10;
	static int default_number_elevator = 3;
	static Elevator[] elevators;
	
	public static void main(String[] args)
	{
		Courier c = new Courier();
		
		/*initialize number of elevator to default*/
		int numberOfElevator = default_number_elevator;
		/*command line check*/
		if(args.length > 1) 
		{
			int input_number_elevator = Integer.parseInt(args[1]);
			if( input_number_elevator < max_number_elevator && input_number_elevator > 0 ) /*input number of worker should be bigger than 0, and smaller than max worker number*/
			{
				numberOfElevator = input_number_elevator;
			}
		}
		
		/*create elevator thread*/
		for(int i = 0; i < numberOfElevator; i ++)
		{
			elevators[i] = new Elevator(i+1,floor, c);
			elevators[i].start();
		}
		
		while(true)
		{
			messageParser(c.receive());
		}
	}

	private static void messageParser(String message)
	{
		String[] parts = message.split(" ");
		int i = Integer.parseInt(parts[1]);
		switch(parts[0])
		{
			case "f":
			{
				elevators[i].updatePosition(Float.parseFloat(parts[2]));
			}
			case "b":
			{
				elevators[i].floorButtonPressed(2, (parts[2].equals("-1"))? "Down":"Up");
			}
			case "p":
			{
				elevators[i].controlButtonPressed(Integer.parseInt(parts[2]));
			}
		}
	}
}

