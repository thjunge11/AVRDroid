package de.thjunge11.avrremote;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class AVRRemoteService extends IntentService {

	private static final String TAG = AVRRemoteService.class.getSimpleName();
	
	public AVRRemoteService() {
		super("AVRRemoteService");
	}

	@Override
	protected void onHandleIntent(Intent arg0) {
		
		if (arg0 != null) {
			
			String command = arg0.getStringExtra(AVRRemoteBroadcastReceiver.EXTRA_COMMAND);
			
			if (command != null) {
		
				SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		        String strAddr = settings.getString("ipaddress", this.getString(R.string.pref_cat_connect_addr_default));
		        String strPort = settings.getString("port", this.getString(R.string.pref_cat_connect_addr_default));
		        int dstPort = 23;
		        try { 
		        	dstPort = Integer.parseInt(strPort);
		        } catch (NumberFormatException e) {
		        	dstPort = 23;
		        }
		        AVRConnection.setPort(dstPort);
		        AVRConnection.setAddr(strAddr);
		        AVRConnection.registerConnection();
		        
		        if (AVRConnection.isAVRconnected()) {
		        	AVRConnection.sendComplexCommand(command);
		        	// notification
					if (Constants.DEBUG) Log.d(TAG, "Send command = " + command);
					// Toast.makeText(this, R.string.toast_broadcast_command, Toast.LENGTH_SHORT).show();
		        }
		        
		        AVRConnection.unregisterConnection();
			}
		}
	}
}
