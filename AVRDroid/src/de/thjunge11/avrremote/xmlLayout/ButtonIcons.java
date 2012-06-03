package de.thjunge11.avrremote.xmlLayout;

import de.thjunge11.avrremote.R;

public class ButtonIcons {
	
	public static final int NO_ICON = 0;
	
	private static final int[] resIdMap = {
		0, // no icon
		R.drawable.av_forward, // 1
		R.drawable.av_next, // 2
		R.drawable.av_pause, // 3
		R.drawable.av_play, // 4
		R.drawable.av_previous, // 5
		R.drawable.av_repeat, // 6
		R.drawable.av_rewind, // 7
		R.drawable.av_shuffle, // 8
		R.drawable.av_stop, // 9
		R.drawable.favorite, // 10
		R.drawable.nav_accept, // 11
		R.drawable.nav_back, // 12
		R.drawable.nav_cancel, // 13
		R.drawable.nav_down, // 14
		R.drawable.nav_forward, // 15
		R.drawable.nav_left, // 16
		R.drawable.nav_rewind, // 17
		R.drawable.nav_right, // 18
		R.drawable.nav_up, // 19
		R.drawable.volume_on, // 20
		R.drawable.volume_muted // 21
		};
	
	static final public int getResId (int iconid) {
		if ((iconid > 0) && (iconid < 22)) {
			return resIdMap[iconid]; 
		}
		return NO_ICON;
	}	
}