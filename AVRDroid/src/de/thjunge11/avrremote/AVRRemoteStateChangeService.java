package de.thjunge11.avrremote;

import java.io.IOException;
import java.io.InputStreamReader;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class AVRRemoteStateChangeService extends Service {

	private static final String TAG = AVRRemoteStateChangeService.class.getSimpleName();
	
	private final IBinder mBinder = new LocalBinder();
	private Thread handlerReceivingThread;
	
	public class LocalBinder extends Binder {
		AVRRemoteStateChangeService getService() {
            // Return this instance of LocalService so clients can call public methods
            return AVRRemoteStateChangeService.this;
        }
    }
	
	@Override
	public void onCreate() {
		if (BuildConfig.DEBUG) Log.d(TAG, "StateChangeReceiverService created");
		super.onCreate();
	}
	
	@Override
    public IBinder onBind(Intent intent) {
		if (BuildConfig.DEBUG) Log.d(TAG, "StateChangeReceiverService bound");
        return mBinder;
    }
	
	@Override
	public void onDestroy() {
		if (BuildConfig.DEBUG) Log.d(TAG, "StateChangeReceiverService destroyed");
		if (handlerReceivingThread != null) {
			handlerReceivingThread.interrupt();
		}
		super.onDestroy();
	}
	
	public void startReceiving() {
		handlerReceivingThread = new Thread(bodyReceivingThread);
		handlerReceivingThread.start();
	}
	
	private Runnable bodyReceivingThread = new Runnable() {
		
		@Override
		public void run() {
		
			if (BuildConfig.DEBUG) Log.d(TAG, "StateChangeReceiverService.ReceivingThread started");
			
			try {
				InputStreamReader socketInputStream = new InputStreamReader(AVRConnection.getInputStream());
				
				String receiveEvent = "";
				int intChar;
				
				
				// socketInputStream.read() blocks until characters are available
				while((intChar = socketInputStream.read()) != -1) {
					
					if (intChar == 0x0D) {
						if (BuildConfig.DEBUG) Log.d(TAG, "received: " + receiveEvent);
						receiveEvent = "";
					}
					else {
						receiveEvent += (char) intChar;
					}
				}
				
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
			}
			
			if (BuildConfig.DEBUG) Log.d(TAG, "StateChangeReceiverService.ReceivingThread stopped");
		}
	};
}
