package de.thjunge11.avrremote;

import android.app.Activity;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

public class SimpleGestureFilter extends SimpleOnGestureListener {

	public final static int SWIPE_UP = 1;
	public final static int SWIPE_DOWN = 2;
	public final static int SWIPE_LEFT = 3;
	public final static int SWIPE_RIGHT = 4;


	private int swipe_Min_Distance = 100;
	private int swipe_Max_Distance = 600;
	private int swipe_Min_Velocity = 100;

	private GestureDetector detector;
	private SimpleGestureListener listener;

	public SimpleGestureFilter(Activity context, SimpleGestureListener sgl) {

		this.detector = new GestureDetector(context, this);
		this.listener = sgl;
	}

	public void onTouchEvent(MotionEvent event) {
		this.detector.onTouchEvent(event);
	}

	public void setSwipeMaxDistance(int distance) {
		this.swipe_Max_Distance = distance;
	}

	public void setSwipeMinDistance(int distance) {
		this.swipe_Min_Distance = distance;
	}

	public void setSwipeMinVelocity(int distance) {
		this.swipe_Min_Velocity = distance;
	}

	public int getSwipeMaxDistance() {
		return this.swipe_Max_Distance;
	}

	public int getSwipeMinDistance() {
		return this.swipe_Min_Distance;
	}

	public int getSwipeMinVelocity() {
		return this.swipe_Min_Velocity;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {

		final float xDistance = Math.abs(e1.getX() - e2.getX());
		final float yDistance = Math.abs(e1.getY() - e2.getY());

		if (xDistance > this.swipe_Max_Distance
				|| yDistance > this.swipe_Max_Distance)
			return false;

		velocityX = Math.abs(velocityX);
		velocityY = Math.abs(velocityY);
		boolean result = false;

		if (velocityX > this.swipe_Min_Velocity
				&& xDistance > this.swipe_Min_Distance) {
			if (e1.getX() > e2.getX()) // right to left
				this.listener.onSwipe(SWIPE_LEFT);
			else
				this.listener.onSwipe(SWIPE_RIGHT);

			result = true;
		} else if (velocityY > this.swipe_Min_Velocity
				&& yDistance > this.swipe_Min_Distance) {
			if (e1.getY() > e2.getY()) // bottom to up
				this.listener.onSwipe(SWIPE_UP);
			else
				this.listener.onSwipe(SWIPE_DOWN);

			result = true;
		}

		return result;
	}

	static interface SimpleGestureListener {
		void onSwipe(int direction);
	}

}
