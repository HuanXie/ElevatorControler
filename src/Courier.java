/*
 * This class send and receive message to and from elevator UI by TCP connection
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Courier {
	Socket socket; // Socket to connect to UI
	PrintWriter out; // Outputstream
	BufferedReader in; // Inputstream
	
	
	Courier()
	{
		try {
			socket = new Socket("localhost", 4711); // Connect to UI
			socket.setTcpNoDelay(false); // Disable Nagle's algorithm
			out = new PrintWriter(socket.getOutputStream()); // Set outputstream
			in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Set inputstream
		} catch (Exception e) {
			System.out.println("Can not connect to host");
		}	
	}
	
	// Send message
	public void send(String str)
	{
		out.println(str);
		out.flush();
	}
	
	// Receive message
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
