import com.pi4j.io.serial.*;
import com.pi4j.util.CommandArgumentParser;
import com.pi4j.util.Console;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class stamat
{
	public stamat() {}

	 final static GpioController gpio = GpioFactory.getInstance();
     final static GpioPinDigitalOutput pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_04, "led", PinState.LOW);
     final static int servo5Up = 15,servo5Down=-90;
     
	final static Console console = new Console();
	public static void main(String[] args) throws Exception
	{
		console.println("Stamat server is running.");
		int clientNumber = 0;
		ServerSocket listener = new ServerSocket(2121);
		while(true) {
			try
			{
				new connectionHandler(listener.accept(), ++clientNumber).start();
			}
			finally {
				console.println("cant error");
			}
		}
	}


	private static boolean executeCommand(String s, Serial serial,PrintWriter out)
	{
		synchronized(console){
			try {
				if(s.contains("AR")) {
					out.println(s);
					console.println(s);
				}
				
				if (s.contains("RESET")) {
					serial.write("reset\r");
				}

				if (s.contains("LEDON")) {
					pin.high();
				}else if(s.contains("LEDOFF")) {
					pin.low();
				}
					
				if(s.contains("SENSORUP")){
					serial.write("servo 5:" + servo5Up + "\r");
					console.println(s);
				}else if(s.contains("SENSORDOWN")){
					serial.write("servo 5:" + servo5Down + "\r");
					console.println(s);
				}
				
				if(s.contains("S1")) {
					s = s.replace("S1", "");
					serial.write("servo 1:" + s + "\r");
					console.println(s);
				}
				
				if(s.contains("S2")) {
					s = s.replace("S2", "");
					serial.write("servo 2:" + s + "\r");
					console.println(s);
				}


				if (s.contains("S6")) {
					s = s.replace("S6", "");
					serial.write("servo 6:" + s + "\r");
					console.println(s);
				}



				if (s.contains("S3")) {
					s = s.replace("S3", "");
					serial.write("servo 3:" + s + "\r");
					console.println(s);
				}

				if (s.contains("S4")) {
					s = s.replace("S4", "");
					serial.write("servo 4:" + s + "\r");
					console.println(s);
				}

				if (s.contains("FB")) {
					s = s.replace("FB", "");
					if(s=="0")
					{
						serial.write("stop\r");
					}
					else{
						serial.write("mogo 1:"+s+" 2:"+s+"\r");
					}
				}

				if (s.contains("LR")) {
					s = s.replace("LR", "");
					if(Integer.parseInt(s)>0){
						serial.write("mogo 1:"+s+" 2:-"+s+"\r");
					}
					else if(Integer.parseInt(s)<0){
						serial.write("mogo 1:"+s);
						s=s.replace("-","");
						serial.write(" 2:"+s+"\r");
					}
					else {
						serial.write("stop\r");
					}
				}
			}
			catch(IOException ex){
			}

		}
		return true;
	}

	private static class connectionHandler
	extends Thread
	{
		private Socket socket;
		private int clientNumber;

		public connectionHandler(Socket socket, int clientNumber)
		{
			this.socket = socket;
			this.clientNumber = clientNumber;
		}

		public void run()
		{
			final Serial serial = SerialFactory.createInstance();
			try {

				// print program title/header
				console.title("Stamat Server v1");
				// allow for user to exit program using CTRL-C
				console.promptForExit();
				// create an instance of the serial communications class
				
				// create and register the serial data listener
				serial.addListener(new SerialDataEventListener() {
					@Override
					public void dataReceived(SerialDataEvent event) {

						// NOTE! - It is extremely important to read the data received from the
						// serial port.  If it does not get read from the receive buffer, the
						// buffer will continue to grow and consume memory.

						// print out the data received to the console
						try {
							console.println(event.getAsciiString());
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});

				// create serial config for stamat controller
				SerialConfig config = new SerialConfig();
				// set default serial settings (device, baud rate, flow control, etc)
				config.device("/dev/ttyUSB0")
				.baud(Baud._19200)
				.dataBits(DataBits._8)
				.parity(Parity.NONE)
				.stopBits(StopBits._1)
				.flowControl(FlowControl.NONE);

				// display connection details
				console.box(" Connecting to: " + config.toString(),
						" Data received on serial port will be displayed below.");
				console.println("New connection with client# " + clientNumber + " at " + socket);
				// open the default serial device/port with the configuration settings
				serial.open(config);
				
				
				final Serial arduino = SerialFactory.createInstance();
				arduino.addListener(new SerialDataEventListener() {
					@Override
					public void dataReceived(SerialDataEvent event) {

						// print out the data received to the console
						try {
							console.println(event.getAsciiString());
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
				
				try {
				config.device("/dev/ttyACM0")
				.baud(Baud._9600)
				.flowControl(FlowControl.NONE);

				console.box(" Connecting to: " + config.toString(),
						" Data from Arduino will be displayed below.");

				arduino.open(config);
				}
				catch(Exception e) {
					console.println("nema arduino bachi na");
				}
				
				
				int flag=0;
				BufferedReader in = new BufferedReader(new java.io.InputStreamReader(socket.getInputStream()));
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

				//out - is for clients 
				out.println("Hello, you are client #" + clientNumber + ".");
				out.println("Enter a line with only a period to quit\n");
				for (;;)
				{
					String input = in.readLine();
					if ((input == null) || (input.equals("."))) {
						break;
					}

						if (executeCommand(input, serial,out)) {
							out.println("OK" + input);
						} else {
							out.println("FAK");
						}
				}
			} catch (IOException e) {
				console.println("Error handling client# " + clientNumber + ": " + e);
			} finally {
				try {
					socket.close();
				} catch (IOException e) {
					console.println("Couldn't close a socket, what's going on?");
				}
				console.println("Connection with client# " + clientNumber + " closed"+"  "+Thread.activeCount());
				try{
				serial.close();
				}catch(Exception e) {
					
				}
				this.interrupt();
			}
		}




		private void log(String message)
		{
			System.out.println(message);
		}
	}
}
