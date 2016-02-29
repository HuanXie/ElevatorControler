import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Courier {
	Socket socket;
	PrintWriter out;
	BufferedReader in;
	
	
	Courier()
	{
		try {
			socket = new Socket("localhost", 4711);
			socket.setTcpNoDelay(false);
			out = new PrintWriter(socket.getOutputStream());
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (Exception e) {
			System.out.println("Can not connect to host");
		}	
	}
	
	
	public void send(String str)
	{
		out.println(str);
		out.flush();
	}
	
	public String receive()
	{
		try {
			String message;
			message = in.readLine();
			return message;
		} catch (IOException e) {}
		return null;
	}
}
