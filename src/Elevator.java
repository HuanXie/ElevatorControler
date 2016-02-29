import java.util.Collections;
import java.util.concurrent.PriorityBlockingQueue;

public class Elevator extends Thread{
	int id;
	Courier courier;
	float position;
	boolean moving;
	boolean up;
	boolean down;
	boolean newEvent;
	PriorityBlockingQueue<Integer> up_path;
	PriorityBlockingQueue<Integer> down_path;
	PriorityBlockingQueue<Integer> current_path;
	
	// Constructor, setting all to false
	Elevator(int id, Courier courier)
	{ 
		this.id = id;
		this.courier = courier;
		position = 0f;
		moving = false;
		up = false;
		down = false;
		newEvent = false;
		up_path = new PriorityBlockingQueue<Integer>();
		down_path = new PriorityBlockingQueue<Integer>(11, Collections.reverseOrder());
	}
	
	public synchronized void updatePosition(float position)
	{
		this.position = position;
		if(Math.abs(position-Math.round(position)) < 0.04)
		{
			System.out.println("At " + Math.round(position) + " floor");
			notifyAll();
		}
	}
	
	public synchronized void floorButtonPressed(int floor, String direction)
	{
		System.out.println("At " + floor + " floor someone pressed " + direction);
		if(direction.equals("Up"))
		{
			if(moving)
			{
				if(up)
				{
					if(position > floor)
					{
						System.out.println("Add this request to down_path");
						if(!down_path.contains(floor))
						{
							down_path.add(floor);
						}
					}else{
						System.out.println("Add this request to current_path");
						if(!current_path.contains(floor))
						{
							current_path.add(floor);
						}
					}
				}else{
					System.out.println("Add this request to up_path");
					if(!up_path.contains(floor))
					{
						up_path.add(floor);
					}
				}
			}else{
				if(position > floor)
				{
					System.out.println("Add this request to down_path");
					if(!down_path.contains(floor))
					{
						down_path.add(floor);
					}
				}else{
					System.out.println("Add this request to up_path");
					if(!up_path.contains(floor))
					{
						up_path.add(floor);
					}
				}
			}
		}else{
			if(moving)
			{
				if(down)
				{
					if(position < floor)
					{
						System.out.println("Add this request to up_path");
						if(!up_path.contains(floor))
						{
							up_path.add(floor);
						}
					}else{
						System.out.println("Add this request to current_path");
						if(!current_path.contains(floor))
						{
							current_path.add(floor);
						}
					}
				}else{
					System.out.println("Add this request to down_path");
					if(!down_path.contains(floor))
					{
						down_path.add(floor);
					}
				}
			}else{
				if(position > floor)
				{
					System.out.println("Add this request to down_path");
					if(!down_path.contains(floor))
					{
						down_path.add(floor);
					}
				}else{
					System.out.println("Add this request to up_path");
					if(!up_path.contains(floor))
					{
						up_path.add(floor);
					}
				}
			}
		}
		newEvent = true;
		notifyAll();
	}
	
	public synchronized void controlButtonPressed(int floor)
	{
		System.out.println("Someone at elevator pressed " + floor + " floor");
		if(moving)
		{
			if(up)
			{
				if(floor > position)
				{
					System.out.println("Add this request to current_path");
					if(!current_path.contains(floor))
					{
						current_path.add(floor);
					}
				}else{
					System.out.println("Add this request to down_path");
					if(!down_path.contains(floor))
					{
						down_path.add(floor);
					}
				}
			}else{
				if(floor < position)
				{
					System.out.println("Add this request to current_path");
					if(!current_path.contains(floor))
					{
						current_path.add(floor);
					}
				}else{
					System.out.println("Add this request to up_path");
					if(!up_path.contains(floor))
					{
						up_path.add(floor);
					}
				}
			}
		}else{
			if(position > floor)
			{
				System.out.println("Add this request to down_path");
				if(!down_path.contains(floor))
				{
					down_path.add(floor);
				}
			}else{
				System.out.println("Add this request to up_path");
				if(!up_path.contains(floor))
				{
					up_path.add(floor);
				}
			}
		}
		newEvent = true;
		notifyAll();
	}
	
	public void run()
	{
		try
		{
			while(true)
			{
				while(!newEvent && !moving)
				{
					synchronized(this)
					{
						wait();
					}
			}
			synchronized(this)
			{
				newEvent = false;
				if(!moving)
				{
					if(up_path.isEmpty())
					{
						courier.send("m" + " " + id + " " + "-1");
						moving = true;
						down = true;
						up = false;
						System.out.println("Current path is down");
						current_path = new PriorityBlockingQueue<Integer>(down_path);
						down_path = new PriorityBlockingQueue<Integer>(11, Collections.reverseOrder());
					}else{
						courier.send("m" + " " + id + " " + "1");
						moving = true;
						up = true;
						down = false;
						System.out.println("Current path is up");
						current_path = new PriorityBlockingQueue<Integer>(up_path);
						up_path = new PriorityBlockingQueue<Integer>();
					}
				}else{
					while(Math.abs(position-Math.round(position)) > 0.04)
					{
						wait();
					}
					if(Math.abs(current_path.peek()-Math.round(position)) < 0.04)
					{
						courier.send("m" + " " + id + " " + "0");
						courier.send("d" + " " + id + " " + "1");
						Thread.sleep(2000);
						courier.send("d" + " " + id + " " + "-1");
						Thread.sleep(2000);
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
										System.out.println("Current path is up");
										current_path = new PriorityBlockingQueue<Integer>(up_path);
										up_path = new PriorityBlockingQueue<Integer>();
									}
								}else{
									courier.send("m" + " " + id + " " + "-1");
									moving = true;
									up = false;
									down = true;
									System.out.println("Current path is down");
									current_path = new PriorityBlockingQueue<Integer>(down_path);
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
										System.out.println("Current path is down");
										current_path = new PriorityBlockingQueue<Integer>(down_path);
										down_path = new PriorityBlockingQueue<Integer>(11, Collections.reverseOrder());
									}
								}else{
									courier.send("m" + " " + id + " " + "1");
									moving = true;
									up = true;
									down = false;
									System.out.println("Current path is up");
									current_path = new PriorityBlockingQueue<Integer>(up_path);
									up_path = new PriorityBlockingQueue<Integer>();
								}
							}
						}else{
							if(current_path.peek() > position)
							{
								courier.send("m" + " " + id + " " + "1");
								moving = true;
								up = true;
								down = false;
							}else{
								courier.send("m" + " " + id + " " + "-1");
								moving = true;
								down = true;
								up = false;
							}
						}
					}
				}
			}
		}
		}catch(InterruptedException e){}
	}
	
}
