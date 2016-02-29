public class Test {
	public static void main(String[] args)
	{
		Courier c = new Courier();
		c.send("m 1 1");
		while(true){
			System.out.println(c.receive());
		}
	}
}
