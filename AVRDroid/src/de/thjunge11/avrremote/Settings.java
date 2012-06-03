package de.thjunge11.avrremote;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

public class Settings extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	
	private static final String TAG = Settings.class.getSimpleName();
	SharedPreferences prefs; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.addPreferencesFromResource(R.xml.settings);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
        String strVolUpAction = prefs.getString("volupkey_action", 
        		getResources().getStringArray(R.array.entries_volume_up_keys)[0]);
		if (strVolUpAction.equals(getResources().getStringArray(R.array.entries_volume_up_keys)[2])) {
			this.findPreference("volupkey_custom").setEnabled(true);			
		}
		else {
			this.findPreference("volupkey_custom").setEnabled(false);
		}
		
		String strVolDownAction = prefs.getString("voldownkey_action", 
        		getResources().getStringArray(R.array.entries_volume_down_keys)[0]);
		if (strVolDownAction.equals(getResources().getStringArray(R.array.entries_volume_down_keys)[2])) {
			this.findPreference("voldownkey_custom").setEnabled(true);			
		}
		else {
			this.findPreference("voldownkey_custom").setEnabled(false);
		}
		
		prefs.registerOnSharedPreferenceChangeListener(this);
	}
				
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		
		if (Constants.DEBUG) Log.d(TAG, "onSharedPreferenceChanged(): key=" + key);
		
		if (key.equals("volupkey_action")) {
			String strVolUpAction = sharedPreferences.getString("volupkey_action", 
	        		getResources().getStringArray(R.array.entries_volume_up_keys)[0]);
			if (strVolUpAction.equals(getResources().getStringArray(R.array.entries_volume_up_keys)[2])) {
				this.findPreference("volupkey_custom").setEnabled(true);			
			}
			else {
				this.findPreference("volupkey_custom").setEnabled(false);
			}
		}
		
		else if (key.equals("voldownkey_action")) {
			String strVolDownAction = sharedPreferences.getString("voldownkey_action", 
	        		getResources().getStringArray(R.array.entries_volume_down_keys)[0]);
			if (strVolDownAction.equals(getResources().getStringArray(R.array.entries_volume_down_keys)[2])) {
				this.findPreference("voldownkey_custom").setEnabled(true);			
			}
			else {
				this.findPreference("voldownkey_custom").setEnabled(false);
			}
		}
	}
}
