package de.thjunge11.avrremote;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

public class AVRRemoteBroadcastReceiver extends BroadcastReceiver {

	private static final String TAG = AVRRemoteBroadcastReceiver.class.getSimpleName();
	
	public static final String EXTRA_COMMAND = "command";
	private static final String ACTION_SEND_COMMAND = "de.thjunge11.avrdroid.action.SEND_COMMAND";

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		
		// check if enabled in app settings
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(arg0);
        if (settings.getBoolean("broadcast", false)) {
		
			// process ACTION_SEND intent by intent extra data
			if (ACTION_SEND_COMMAND.equals(arg1.getAction())) {
				
				if (Constants.DEBUG) Log.d(TAG,"Received broadcast intent: " + arg1.toString());
				
				if (arg1.getStringExtra(EXTRA_COMMAND) != null) {
					
					if (Constants.DEBUG) Log.d(TAG,"has EXTRA_COMMAND: " + arg1.getStringExtra(EXTRA_COMMAND));
					
					ConnectivityManager connMgr = (ConnectivityManager) arg0.getSystemService(Context.CONNECTIVITY_SERVICE);
					NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI); 
						
					if (networkInfo.isConnected()) {
						
						Intent intent = new Intent(arg0, AVRRemoteService.class);
						intent.putExtra(EXTRA_COMMAND, arg1.getStringExtra(EXTRA_COMMAND));
						arg0.startService(intent);
					}
				}
			}
			
			// process component intent
			// Component will be me only, so its not null
			if (arg1.getComponent() != null)
			{	
				// currently not supported
			}
		}
	}
}
