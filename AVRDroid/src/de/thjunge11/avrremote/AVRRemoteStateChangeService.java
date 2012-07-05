package de.thjunge11.avrremote;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class AVRRemoteStateChangeService extends Service {

	private static final String TAG = AVRRemoteStateChangeService.class.getSimpleName();
		
	private final IBinder mBinder = new LocalBinder();
	private Thread handlerReceivingThread;
	private AtomicBoolean stateReceiving = new AtomicBoolean();
	private AVRRemoteStateChangeListener callback;
	private StateVector stateVector;
	private Object lock = new Object();
	
	public class LocalBinder extends Binder {
		AVRRemoteStateChangeService getService() {
            // Return this instance of LocalService so clients can call public methods
            return AVRRemoteStateChangeService.this;
        }
    }
	
	private class StateVector extends Vector<String> {
		private static final long serialVersionUID = -3769538380581428269L;
		// public StateVector() { super(); }
		public boolean contains(String value) {
			for (String match : this) {
				if (match.endsWith("$")) {
					try {
						if (value.startsWith(match.substring(0, match.length()-1))) {
							return true;
						}
					} catch (IndexOutOfBoundsException e)
					{
						Log.e(TAG, e.getMessage());
					}	
				}
				else if (match.equals(value)) {
					return true;
				}
			}
			return false;
		}
	}
	
	@Override
	public void onCreate() {
		stateVector = new StateVector();
		if (BuildConfig.DEBUG) Log.d(TAG, "StateChangeReceiverService created");
		super.onCreate();
	}
	
	@Override
    public IBinder onBind(Intent intent) {
		if (BuildConfig.DEBUG) Log.d(TAG, "StateChangeReceiverService bound");
        return mBinder;
    }
	
	@Override
	public boolean onUnbind(Intent intent) {
		if (BuildConfig.DEBUG) Log.d(TAG, "StateChangeReceiverService unbound");
		return super.onUnbind(intent);
	}
	
	@Override
	public void onDestroy() {
		if (BuildConfig.DEBUG) Log.d(TAG, "StateChangeReceiverService destroyed");
		stateReceiving.set(false);
		super.onDestroy();
	}
	
	public void registerListener(AVRRemoteStateChangeListener callback) {
		if (BuildConfig.DEBUG) Log.d(TAG, "StateChangeReceiverService: listener registered");
		this.callback = callback;
	}
	
	public void registerStates(Vector<String> states) {
		synchronized(lock) {
			stateVector.clear();
			stateVector.addAll(states);
		}
		if (BuildConfig.DEBUG) Log.d(TAG, "StateChangeReceiverService: states registered:" + states.toString());
	}
	
	public void startReceiving() {
		stateReceiving.set(true);
		handlerReceivingThread = new Thread(bodyReceivingThread);
		handlerReceivingThread.setDaemon(true);
		handlerReceivingThread.start();
	}
		
	public boolean isReceivingThreadRunning () {
		if (handlerReceivingThread != null) {
			return handlerReceivingThread.isAlive();
		}
		return false;
	}
	
	private Runnable bodyReceivingThread = new Runnable() {
		
		@Override
		public void run() {
		
			if (BuildConfig.DEBUG) Log.d(TAG, "StateChangeReceiverService.ReceivingThread started");
			
			try {
				// clear buffer
				AVRConnection.emptyReadBuffer();
				InputStreamReader socketInputStream = new InputStreamReader(AVRConnection.getInputStream());
				
				String receiveEvent = "";
				int intChar;
				
				while(stateReceiving.get()) {
					
					try { 
						intChar = socketInputStream.read();
					
						if (intChar == 0x0D) {
							// if (BuildConfig.DEBUG) Log.d(TAG, "received: " + receiveEvent);
							
							// check if in state vector
							synchronized(lock) {
								if (stateVector.contains(receiveEvent)) {
									// if (BuildConfig.DEBUG) Log.d(TAG, "StateChangeReceiverService.ReceivingThread callback: " + receiveEvent);
									callback.onStateChange(receiveEvent);
								}
							}
							
							receiveEvent = "";
						}
						else {
							receiveEvent += (char) intChar;
						}
					} catch (SocketTimeoutException ste) {
						// bad style, thread is stopped by stateReceiving variable
						// better way would be to use socketChannel with thread.interrupt
					}
				}
			} catch (SocketException se) {
				// bad style, thread may be cancelled due to socket closed exception before read() returns after timeout
				if ("Socket closed".equals(se.getMessage())) {
					if (BuildConfig.DEBUG) Log.d(TAG, "StateChangeReceiverService.ReceivingThread " + se.getMessage());
				}
				else {
					Log.e(TAG, se.getMessage());
				}
			} catch (IOException e) {				
				Log.e(TAG, e.getMessage());
			}
			
			if (BuildConfig.DEBUG) Log.d(TAG, "StateChangeReceiverService.ReceivingThread stopped");
		}
	};
}
