package de.thjunge11.avrremote;

import java.io.IOException;
import java.io.InputStreamReader;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class AVRRemoteStateChangeService extends IntentService {

	private static final String TAG = AVRRemoteStateChangeService.class.getSimpleName();
	
	public AVRRemoteStateChangeService() {
		super("AVRRemoteStateChangeService");
	}

	@Override
	protected void onHandleIntent(Intent arg0) {
		
		if (BuildConfig.DEBUG) Log.d(TAG, "StateChangeReceiver started");
		
		try {
			InputStreamReader socketInputStream = new InputStreamReader(AVRConnection.getInputStream());
			String receiveEvent = "";
			int intChar;
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
		
		if (BuildConfig.DEBUG) Log.d(TAG, "StateChangeReceiver stopped");
	}

}
