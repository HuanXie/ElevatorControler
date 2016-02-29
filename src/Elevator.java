import java.util.Collections;
import java.util.concurrent.PriorityBlockingQueue;

public class Elevator extends Thread{
	int id;
	Courier courier;
	float position;
	boolean moving;
	boolean up;
	boolean down;
	//FloorButton[] floors;
	//Boolean[] controlPanel;
	PriorityBlockingQueue<Integer> up_path;
	PriorityBlockingQueue<Integer> down_path;
	PriorityBlockingQueue<Integer> current_path;
	
	// Constructor, setting all to false
	Elevator(int id, int floor, Courier courier)
	{ 
		this.id = id;
		this.courier = courier;
		position = 0f;
		moving = false;
		up = false;
		down = false;
		/*floors = new FloorButton[floor+1];
		controlPanel = new Boolean[floor+1];
		for(int i = 0; i < floors.length; i++)
		{
			floors[i] = new FloorButton();
			controlPanel[i] = false;
		}*/
		up_path = new PriorityBlockingQueue<Integer>();
		down_path = new PriorityBlockingQueue<Integer>(11, Collections.reverseOrder());
	}
	
	public synchronized void updatePosition(float position)
	{
		this.position = position;
		if(Math.abs(position-Math.round(position)) < 0.1)
		{
			notifyAll();
		}
	}
	
	public synchronized void floorButtonPressed(int floor, String direction)
	{
		if(direction.equals("Up"))
		{
			if(moving)
			{
				if(up)
				{
					if(position > floor)
					{
						up_path.add(floor);
					}else{
						current_path.add(floor);
					}
				}else{
					up_path.add(floor);
				}
			}else{
				if(position > floor)
				{
					down_path.add(floor);
				}else{
					up_path.add(floor);
				}
			}
		}else{
			if(moving)
			{
				if(down)
				{
					if(position < floor)
					{
						down_path.add(floor);
					}else{
						current_path.add(floor);
					}
				}else{
					down_path.add(floor);
				}
			}else{
				if(position < floor)
				{
					up_path.add(floor);
				}else{
					down_path.add(floor);
				}
			}
		}
		notifyAll();
	}
	
	public synchronized void controlButtonPressed(int floor)
	{
		if(up)
		{
			if(floor > position)
			{
				current_path.add(floor);
			}else{
				down_path.add(floor);
			}
		}else{
			if(floor < position)
			{
				current_path.add(floor);
			}else{
				up_path.add(floor);
			}
		}
		notifyAll();
	}
	
	public void run()
	{
		while(true)
		{
			if(!moving)
			{
				try {
					synchronized(this)
					{
						wait();
					}
				} catch (InterruptedException e) {}
			}
			synchronized(this)
			{
				if(!moving)
				{
					if(up_path.isEmpty())
					{
						courier.send("m" + " " + id + " " + "-1");
						moving = true;
						down = true;
						current_path = down_path;
						down_path = new PriorityBlockingQueue<Integer>(11, Collections.reverseOrder());
					}else{
						courier.send("m" + " " + id + " " + "1");
						moving = true;
						up = true;
						current_path = up_path;
						up_path = new PriorityBlockingQueue<Integer>();
					}
				}else{
					while(Math.abs(position-Math.round(position)) > 0.1)
					{
						try {
							wait();
						} catch (InterruptedException e) {}
					}
					if(Math.abs(current_path.peek()-Math.round(position)) < 0.1)
					{
						try {
							courier.send("m" + " " + id + " " + "0");
							courier.send("d" + " " + id + " " + "1");
							Thread.sleep(1000);
							courier.send("d" + " " + id + " " + "-1");
							Thread.sleep(1000);
							current_path.remove();
							if(current_path.isEmpty())
							{
								if(up)
								{
									if(down_path.isEmpty())
									{
										if(up_path.isEmpty())
										{
											moving = false;
											up = false;
											down = false;
										}else{
											courier.send("m" + " " + id + " " + "1");
											moving = true;
											up = true;
											down = false;
											current_path = up_path;
											up_path = new PriorityBlockingQueue<Integer>();
										}
									}else{
										courier.send("m" + " " + id + " " + "-1");
										moving = true;
										up = false;
										down = true;
										current_path = down_path;
										down_path = new PriorityBlockingQueue<Integer>(11, Collections.reverseOrder());
									}
								}else{
									if(up_path.isEmpty())
									{
										if(down_path.isEmpty())
										{
											moving = false;
											up = false;
											down = false;
										}else{
											courier.send("m" + " " + id + " " + "-1");
											moving = true;
											up = false;
											down = true;
											current_path = down_path;
											down_path = new PriorityBlockingQueue<Integer>(11, Collections.reverseOrder());
										}
									}else{
										courier.send("m" + " " + id + " " + "1");
										moving = true;
										up = true;
										down = false;
										current_path = up_path;
										up_path = new PriorityBlockingQueue<Integer>();
									}
								}
							}
							if(up)
							{
								courier.send("m" + " " + id + " " + "1");
							}else{
								courier.send("m" + " " + id + " " + "-1");
							}
						} catch (InterruptedException e) {}
					}
				}
			}
		}
	}
	
}
