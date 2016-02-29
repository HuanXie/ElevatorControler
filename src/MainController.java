public class MainController {
	
	public static void main(String[] args)
	{
		Courier c = new Courier();
		
		Elevator e = new Elevator(1,c);
		e.start();
		
		String m;
		while(true)
		{
			m = c.receive();
			//System.out.println(m);
			messageParser(m, e);
		}
	}

	private static void messageParser(String message, Elevator e)
	{
		String[] parts = message.split(" ");
		int i = Integer.parseInt(parts[1]);
		switch(parts[0])
		{
			case "f":
			{
				e.updatePosition(Float.parseFloat(parts[2]));
				break;
			}
			case "b":
			{
				e.floorButtonPressed(i, (parts[2].equals("-1"))? "Down":"Up");
				break;
			}
			case "p":
			{
				e.controlButtonPressed(Integer.parseInt(parts[2]));
				break;
			}
		}
	}
}

