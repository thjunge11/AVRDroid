package de.thjunge11.avrremote;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import android.util.Log;

public class AVRConnection {

	private static final String TAG = AVRConnection.class.getSimpleName();
	
	private final static int MAX_STACKED_COMMANDS = 20;
	private final static long waitForNextCommandTimeMs = 250;
	private static final int bufferSize = 4096;
	private static final long responseTimeMs = 300;
	private static final long waitCloseTimeMs = 500;
	private static final int timeoutConnectMs = 2000;
	
	private static Socket socket = null;
	private static String dstAddr = null;
	private static int dstPort = 0;
	private static int connectionCounter = 0;
	
	
	public static void setAddr(String addr) {
		dstAddr = addr;
	}
	
	public static void setPort(int port) {
		dstPort = port;
	}
	
	public static boolean isAVRconnected () {
		if (socket == null) 
			return false;
		else
			return ((!socket.isClosed()) && socket.isConnected());
	}
	
	public static void unregisterConnection() {
		connectionCounter--;
		if (connectionCounter < 1) {
			close();
			connectionCounter = 0;
		}
		if (Constants.DEBUG) Log.d(TAG,"unregisterConnection, Counter: " + connectionCounter);
	}
	
	public static boolean registerConnection () {
		if (connectionCounter < 1) {
			open();
			connect();
		}
		connectionCounter++;
		if (Constants.DEBUG) Log.d(TAG,"registerConnection, Counter: " + connectionCounter);
		return isAVRconnected();
	}
	
	private static void close() {
		if (socket != null) {
			if (!socket.isClosed()) {
				try {
					socket.close();
				} catch (IOException e) {
					Log.e(TAG, e.getMessage());
				}
			}
		}
		socket = null;
		if (Constants.DEBUG) Log.d(TAG, "called .close()");
	}
	
	private static void open() {
		socket = new Socket();
		if (Constants.DEBUG) Log.d(TAG, "called .open()");
	}
	
	private static void connect() {
		
		logStatus(".connect()@begin");
		
		// wait for possible previous close
		try {
			Thread.sleep(waitCloseTimeMs);
		} catch (InterruptedException e1) {
			Log.e(TAG, e1.getMessage());
		}
		
		if (socket == null) {
			open();
		}
		else if (socket.isClosed()) {
			open();
		}
		
		if (dstAddr == null || dstPort == 0) {
			Log.e(TAG, ".connect(): SocketAdress not inizialized");
			return;
		}
		
        try {
        	InetAddress addr = InetAddress.getByName(dstAddr);
        	SocketAddress sockaddr = new InetSocketAddress(addr, dstPort);
        	// This method will block no more than timeout
        	// If the timeout occurs, SocketTimeoutException is thrown.
        	socket.connect(sockaddr, timeoutConnectMs);
        } catch (UnknownHostException e) {
        	Log.e(TAG, e.getMessage());
        } catch (SocketTimeoutException e) {
        	Log.e(TAG, e.getMessage());
        } catch (IOException e) {
        	Log.e(TAG, e.getMessage());
        }
	}
	
	public static boolean reconnect() {
		logStatus("reconnect()@begin");
		
		close();
		
		try {
			Thread.sleep(waitCloseTimeMs);
		} catch (InterruptedException e1) {
			Log.e(TAG, e1.getMessage());
		}
		
		open();
		
        try {
        	InetAddress addr = InetAddress.getByName(dstAddr);
        	SocketAddress sockaddr = new InetSocketAddress(addr, dstPort);
        	// This method will block no more than timeoutMs.
        	// If the timeout occurs, SocketTimeoutException is thrown.
        	int timeoutMs = 2000;   // 2 seconds
        	socket.connect(sockaddr, timeoutMs);
        } catch (UnknownHostException e) {
        	Log.e(TAG, e.getMessage());
        } catch (SocketTimeoutException e) {
        	Log.e(TAG, e.getMessage());
        } catch (IOException e) {
        	Log.e(TAG, e.getMessage());
        }

        return isAVRconnected(); 
	}
	
	public static boolean sendCommand(String strCommand) {
		logStatus("sendCommand()@begin");
		
		if (!isAVRconnected() || (strCommand == null)) {
			return false;
		}
		
		// build command structure from string
		byte[] command = strCommand.getBytes();
		byte[] command_struct = new byte [command.length+1];
		System.arraycopy(command, 0, command_struct, 0, command.length);
		command_struct[command.length] = 0x0D;
		
		try {
			socket.getOutputStream().write(command_struct);
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
			return false;
		}
		
		if (Constants.DEBUG) Log.d(TAG, "sendCommand(): sent " + strCommand);
		return true;
	}
	
	public static String getResponse() {
		logStatus("getResponse()@begin");
		
		if (!isAVRconnected()) {
			return null;
		}
		
		byte[] response_buff = new byte[bufferSize];
		int byteRead = 0;
		
		try  {
			if (socket.getInputStream().available() > 0) {
				byteRead = socket.getInputStream().read(response_buff, 0, response_buff.length);
			}
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
			return null;
		}
		if (Constants.DEBUG) Log.d(TAG, " getResponse(): read " + byteRead + " bytes from socket stream");
		
		byte[] response = new byte [byteRead];
		System.arraycopy(response_buff, 0, response, 0, byteRead);
		
		
		// parse for linefeeds
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < response.length; i++) {
			if (response[i] == 0x0D) {
				result.append("\n");
			}
			else {
				result.append((char) response[i]);
			}
		}
		return result.toString(); 
	}
		
	public static String setget(String str) {
		if(!sendComplexCommand(str))
			return null;
		// wait for response
		try {
			Thread.sleep(responseTimeMs);
		} catch (InterruptedException e1) {
			Log.e(TAG, e1.getMessage());
		}
		return getResponse();
	}
	
	private static void logStatus(String str) {
		if  (socket != null && dstAddr != null) {
			if (Constants.DEBUG) {
				Log.d(TAG, str + ": socket dstAddr: " + dstAddr + ", dstPort: " + dstPort);
				Log.d(TAG, str + ": socket status isConnected(): " + socket.isConnected());
				Log.d(TAG, str + ": socket status isClosed(): " + socket.isClosed());
			}
		}		
	}
	
	public static boolean sendComplexCommand (String strCommand) {
		
		if (strCommand == null)
			return false;
		
		// diessect string for concatenate operator &
		String sep = "_";
		String[] commandArray = strCommand.split(sep, 0);
		
		// process array  
		// pause if String is "" as result of consecutive "__" in String.split()
		int commandCounter = 0;
		long pauseTimeInc = 1;
		for (int i = 0; ((i < commandArray.length) && (commandCounter < MAX_STACKED_COMMANDS)); i++ ) {
			// check if there is apause before first command and consecutive ones after a command
			if (commandArray[i].equals("")) {
				//pause
				try {
					Thread.sleep(pauseTimeInc * waitForNextCommandTimeMs);
					pauseTimeInc++;
				} catch (InterruptedException e1) {
					Log.e(TAG, e1.getMessage());
				}				
			}
			else {
				// send command (return if not successfull)
				if (!sendCommand(commandArray[i])) {
					return false;
				}
				// increase command counter and reset pauseTimer increase
				commandCounter++;
				pauseTimeInc = 1;
				
				// check if command (or pause follows) and then pause (because one "_" is eaten by .split())
				if ((i+1) < commandArray.length) {
					try {
						Thread.sleep(pauseTimeInc * waitForNextCommandTimeMs);
						pauseTimeInc++;
					} catch (InterruptedException e1) {
						Log.e(TAG, e1.getMessage());
					}
				}
			}
		}
		
		return true;
	}
}


