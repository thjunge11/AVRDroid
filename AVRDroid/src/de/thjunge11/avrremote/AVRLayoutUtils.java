package de.thjunge11.avrremote;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ScrollView;
import android.widget.Toast;
import de.thjunge11.avrremote.xmlModel.Page;

public class AVRLayoutUtils {
	
	private static final String TAG = AVRLayoutUtils.class.getSimpleName();
	
	public static int resIdBtColor;
	public static int resIdBtStateList;
	public static int resIdBgColor;
	public static int resIdBtColorTrans;
	public static int resIdTabColor;
	public static int resIdTextAppearence;
	public static boolean UseIcons;
	public static boolean bScreenLock;

	
	public static int determinOrientation(Activity activity) {
		Display display = activity.getWindowManager().getDefaultDisplay();
		
		// determine orientation by aspect ratio
		if (display.getWidth() < display.getHeight()) {
			// if (Constants.DEBUG) Log.d(TAG, "Display orientation: vertical");
			return Page.ORIENTATION_VERTICAL;
		}
		else {
			// if (Constants.DEBUG) Log.d(TAG, "Display orientation: horizontal");
			return Page.ORIENTATION_HORIZONTAL;
		}
	}
	
	public static void setLayoutPreferences(Context context, Activity activity, View v) {
		
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        
		String btcolor = settings.getString("buttoncolor", context.getString(R.string.pref_cat_gui_buttoncolor_default));
        if (btcolor.equals("Blue")) { 
        	resIdBtColor = R.color.blue_light;
        }
        else if (btcolor.equals("Orange")) { 
        	resIdBtColor = R.color.orange;
        }
        else { // default
        	resIdBtColor = R.color.white;
        }
        
        String tabcolor = settings.getString("tabcolor", context.getString(R.string.pref_cat_gui_tabcolor_default));
        if (tabcolor.equals("White")) { 
           	resIdTabColor = R.color.white;
            resIdBtStateList = R.drawable.avrbutton_statelist_white;
        	resIdBtColorTrans = R.color.white_trans;
        }
        else if (tabcolor.equals("Orange")) { 
           	resIdTabColor = R.color.orange;
        	resIdBtStateList = R.drawable.avrbutton_statelist_orange;
        	resIdBtColorTrans = R.color.orange_trans;
        }
        else { // default
           	resIdTabColor = R.color.blue_light;
        	resIdBtStateList = R.drawable.avrbutton_statelist_blue;
        	resIdBtColorTrans = R.color.blue_light_trans;
        }
                
        resIdBgColor = R.color.bg_black;

        ScrollView scrollview = (ScrollView) activity.findViewById(R.id.scrollview);
        scrollview.setBackgroundResource(resIdBgColor);
        v.setBackgroundResource(resIdBgColor);
        
        UseIcons = settings.getBoolean("useicons", true);
        
        String strTextSize = settings.getString("textsize","Small");
        if (strTextSize.equals("Medium")) {
        	resIdTextAppearence = android.R.style.TextAppearance_Medium;
        }
        if (strTextSize.equals("Large")) {
        	resIdTextAppearence = android.R.style.TextAppearance_Large;
        }
        if (strTextSize.equals("Small")) {
        	resIdTextAppearence = android.R.style.TextAppearance_Small;	
        }
	}
	
	public static int getButtonStyleResId(String style) {
		if (style != null) {
			if (style.equals("dark")) return R.drawable.btn_dark;
			else if (style.equals("light")) return R.drawable.btn_light; 
			else if (style.equals("red")) return R.drawable.btn_red;
		}
		return R.drawable.btn_dark; // default
	}
	
	public static int getViewStyleResId(String style) {
		if (style != null) {
			if (style.equals("dark")) return R.drawable.view_dark;
			else if (style.equals("light")) return R.drawable.view_light; 
			else if (style.equals("red")) return R.drawable.view_red;
		}
		return R.drawable.view_dark; // default
	}
	
	public static void lockScreen(boolean keepScreenOn, boolean toast, Activity activity) {

		if(keepScreenOn) {
			
			if (Constants.DEBUG) Log.d(TAG, "utilLockScreen();" + activity.getRequestedOrientation());
	    	activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	    	activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
	    	if (determinOrientation(activity) == Page.ORIENTATION_HORIZONTAL) {
	    		activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	    	}
	    	else {
	    		activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	    	}
	    	
	    	if (toast && !bScreenLock) Toast.makeText(activity, R.string.keepscreenon_true, Toast.LENGTH_SHORT).show();
	    	bScreenLock = true;
		} 
		else {
	    	activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	    	activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
	    	activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
	    	if (toast && bScreenLock) Toast.makeText(activity, R.string.keepscreenon_false, Toast.LENGTH_SHORT).show();
	    	bScreenLock = false;
	    }
	}
}
