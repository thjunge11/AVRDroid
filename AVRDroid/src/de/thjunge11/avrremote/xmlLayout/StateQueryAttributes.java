package de.thjunge11.avrremote.xmlLayout;

public class StateQueryAttributes {
	private String mStateQuery;
	private int mButtonId;
	
	public String getStateQuery() { return this.mStateQuery; }
	public int getButtonId() { return this.mButtonId; }
	
	public StateQueryAttributes (String statequery, int buttonid) {
		this.mStateQuery = statequery;
		this.mButtonId = buttonid;
	}
}
