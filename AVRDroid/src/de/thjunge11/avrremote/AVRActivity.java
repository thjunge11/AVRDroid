package de.thjunge11.avrremote;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class AVRActivity extends FragmentActivity {
	
	private static final String TAG = AVRActivity.class.getSimpleName();
	private RegisterAVRConnection taskHandlerAVRConnection;
	private static final int DIALOG_ABOUT = 0x112;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
	
	@Override
	protected void onStart() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
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
        if (Constants.DEBUG) Log.d(TAG, "ONSTART!");
        if (AVRConnection.isAVRconnected()) {
        	AVRConnection.registerConnection(false);
        	updateConnectionStatus(true);
        }
        else {
	        // connect in worker thread
        	taskHandlerAVRConnection = new RegisterAVRConnection();
        	taskHandlerAVRConnection.execute();
        }
        super.onStart();
	}
	
	@Override
	protected void onStop() {
		if (Constants.DEBUG) Log.d(TAG, "ONSTOP!");
		/* When activity B is launched this callback will be launched after activitys B 
		 * onStart() callback is called. 
		 * So UnregisterAVRConnection will not close connection if Activity B is derived from this class 
		 * and calls RegisterAVRConnection() in onStart().
		 */
		if (taskHandlerAVRConnection != null) {
			if (taskHandlerAVRConnection.getStatus() == AsyncTask.Status.RUNNING) {
				taskHandlerAVRConnection.cancel(true);
			}
		}
		new UnegisterAVRConnection().execute();
		super.onStop();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = new MenuInflater(this);
		menuInflater.inflate(R.menu.options_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.op_menu_about :
			AVRActivity.this.showDialog(DIALOG_ABOUT);
			return true;
		case R.id.op_menu_settings :
			// show preferences activity
			Intent i = new Intent(this, Settings.class);
			this.startActivity(i);
			return true;
		default :
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		Dialog dialog;
		
		switch (id) {
		
			case DIALOG_ABOUT:
				LayoutInflater factory = LayoutInflater.from(this);
			    final View aboutView = factory.inflate(R.layout.about, null);
			    TextView versionLabel = (TextView)aboutView.findViewById(R.id.version_label);
			    versionLabel.setText(getString(R.string.version) + " " + getString(R.string.versionName));
			    dialog = new AlertDialog.Builder(this)
			        .setIcon(R.drawable.avrdroid)
			        .setTitle(R.string.app_name)
			        .setView(aboutView)
			        .setPositiveButton("OK", null)
			        .create();
			    return dialog; 
			    
			default:
				return null;
		}
	}
	
	
	protected class RegisterAVRConnection extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI); 
			if (networkInfo.isConnected()) {
				return AVRConnection.registerConnection(true);
			}
			else {
				return AVRConnection.registerConnection(false);
			}
		}
		protected void onPostExecute(Boolean connected) {
			updateConnectionStatus(connected);
		}
	}
	
	protected class UnegisterAVRConnection extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			AVRConnection.unregisterConnection();
			return null;
		}
	}
	

	// override in child activity classes
	protected void updateConnectionStatus(boolean status) {
		if (Constants.DEBUG) Log.d(TAG, "updateConnectionStatus():" + status);
	}
}
