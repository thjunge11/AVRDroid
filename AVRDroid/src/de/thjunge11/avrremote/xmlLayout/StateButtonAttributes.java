package de.thjunge11.avrremote.xmlLayout;

import de.thjunge11.avrremote.xmlModel.Button;

public class StateButtonAttributes {
	
	
	public final static int STATE_UNDEFINED = -1;
	public final static int PARAM_TYPE_NUMBER = 10;
	public final static int PARAM_TYPE_TEXT = 11;
	public final static int PARAM_TYPE_NONE = 12;
	
	private int storeState;
	private String storedParam;
	
	private int noOfStates;
	private int stateType;
	private String statequery;
	private String[] states;
	private String[] labels;
	private int[] iconIds;
	private String[] styles;
	private String[] commands;
	private int paramType;
	private String[] list;
	private int noOfChooseItems;
	
	// getters
	public int getNoOfStates() { return noOfStates; }
	public String getStateQuery() { return statequery; }
	public int getStateType() { return stateType; }
	public String[] getStates() { return states; }
	public String[] getLabels() { return labels; }
	public String[] getStyles() { return styles; }
	public String[] getCommands() { return commands; }
	public int[] geticonIds() { return iconIds; }
	public int getParamType() { return paramType; }
	public String[] getList() { return list; }
	public int getNoOfChooseItems()  { return this.noOfChooseItems; }
	// single state access
	public int getStoredState() { return storeState; }
	public String getStoredParam () { return storedParam; }
	public String getLabel(int stateId) { 
		if (stateId >= 0 && stateId < noOfStates) {
			String label = labels[stateId].replace("$", storedParam);
			return label;
		}
		else return ""; 
	}
	public String getCommand(int stateId) { 
		if (this.stateType == Button.STATETYPE_SELECT) {
			if (stateId >= 0 && stateId < noOfChooseItems) return commands[stateId]; else return "";
		}
		else {
			if (stateId >= 0 && stateId < noOfStates) return commands[stateId]; else return ""; }
	}
	public String getStyle(int stateId) { if (stateId >= 0 && stateId < noOfStates) return styles[stateId]; else return ""; }
	public int getIconId(int stateId) { if (stateId >= 0 && stateId < noOfStates) return iconIds[stateId]; else return ButtonIcons.NO_ICON; }
	public String getState(int stateId) { if ((stateId >= 0 && stateId < noOfStates) && (states != null)) return states[stateId]; else return ""; }
	
	// copy constructor
	public StateButtonAttributes(StateButtonAttributes stateButtonAttributes, int storeState, String receivedState) {
		this.storeState = storeState;
		this.noOfStates = stateButtonAttributes.getNoOfStates();
		this.statequery = stateButtonAttributes.getStateQuery();
		this.stateType = stateButtonAttributes.getStateType();
		this.states = stateButtonAttributes.getStates();
		this.labels = stateButtonAttributes.getLabels();
		this.commands = stateButtonAttributes.getCommands();
		this.styles = stateButtonAttributes.getStyles();
		this.iconIds = stateButtonAttributes.geticonIds();
		this.paramType = stateButtonAttributes.getParamType();
		this.storedParam = stateButtonAttributes.getStoredParam();
		this.list = stateButtonAttributes.getList();
		this.noOfChooseItems = stateButtonAttributes.getNoOfChooseItems();
		
		if (this.paramType == PARAM_TYPE_NUMBER) {
			String value = receivedState.substring(states[0].length()-1);
			try {
				int intVal = Integer.parseInt(value);
				storedParam = Integer.toString(intVal);
			} catch (NumberFormatException e) {}
		}
		else if (paramType == PARAM_TYPE_TEXT) {
			this.storedParam = receivedState.substring(states[0].length()-1);
		}
		else {
			this.storedParam = "";
		}
	}
	
	
	// constructor
	public StateButtonAttributes(int stateType, String statequery, String states, String list, String command,
			String label, String style, String iconid, int storeState) {
		
		this.storeState =  storeState;
		this.stateType = stateType;
		this.statequery = statequery;
		
		// process commands/list
		if (stateType == de.thjunge11.avrremote.xmlModel.Button.STATETYPE_SELECT) {
			// process states
			if (states != null) {
				if (states.endsWith("$text")) {
					this.paramType = PARAM_TYPE_TEXT;
					this.storedParam = "";
					this.noOfStates = 1;
					this.states = new String[this.noOfStates];
					this.states[0] = states.replace("$text","$");
				}
				else if (states.endsWith("$number")) {
					this.paramType = PARAM_TYPE_NUMBER;
					this.storedParam = "";
					this.noOfStates = 1;
					this.states = new String[this.noOfStates];
					this.states[0] = states.replace("$number","$");
				}
				else {
					this.states = states.split(Button.STATE_SEP, 0);
					this.noOfStates = this.states.length;
					storedParam = "";
					paramType = PARAM_TYPE_NONE;
				}
			}
			else {
				this.states = null;
				this.noOfStates = 1;
				this.storeState = 0;
				storedParam = "";
				paramType = PARAM_TYPE_NONE;
			}
			
			String providedCommands[] = command.split(Button.STATE_SEP, 0);
			int noOfCommands = providedCommands.length;
			String providedList[] = list.split(Button.STATE_SEP, 0);
			int noOfItems = providedList.length;
			if (noOfItems < noOfCommands) {
				this.noOfChooseItems = noOfItems;
			}
			else {
				this.noOfChooseItems = noOfCommands;
			}
			this.commands = new String[this.noOfChooseItems];
			this.list = new String[this.noOfChooseItems];
			for (int i=0; i < this.noOfChooseItems; i++) {
				this.commands[i] = providedCommands[i];
				this.list[i] = providedList[i];
			}			
		}
		else {
			// process states
			if (states.endsWith("$text")) {
				this.paramType = PARAM_TYPE_TEXT;
				this.storedParam = "";
				this.noOfStates = 1;
				this.states = new String[this.noOfStates];
				this.states[0] = states.replace("$text","$");
			}
			else if (states.endsWith("$number")) {
				this.paramType = PARAM_TYPE_NUMBER;
				this.storedParam = "";
				this.noOfStates = 1;
				this.states = new String[this.noOfStates];
				this.states[0] = states.replace("$number","$");
			}
			else {
				this.states = states.split(Button.STATE_SEP, 0);
				this.noOfStates = this.states.length;
				storedParam = "";
				paramType = PARAM_TYPE_NONE;
			}
			
			String[] providedCommands = command.split(Button.STATE_SEP, 0);
			String storeCommand = "";
			this.commands = new String[this.noOfStates];
			for (int i=0; i < this.noOfStates; i++) {
				if (i < providedCommands.length) { storeCommand = this.commands[i] = providedCommands[i]; }
				else { this.commands[i] = storeCommand; }
			}
			this.list = null;
		}
			
		
		// process labels
		String[] providedLabels = label.split(Button.STATE_SEP, 0);
		String storeLabel = "";
		this.labels = new String[this.noOfStates];
		for (int i=0; i < this.noOfStates; i++) {
			if (i < providedLabels.length) { storeLabel = this.labels[i] = providedLabels[i]; }
			else { this.labels[i] = storeLabel; }
		}
		// process styles
		String[] providedStyles = style.split(Button.STATE_SEP, 0);
		String storeStyle = "";
		this.styles = new String[this.noOfStates];
		for (int i=0; i < this.noOfStates; i++) {
			if (i < providedStyles.length) { storeStyle = this.styles[i] = providedStyles[i]; }
			else { this.styles[i] = storeStyle; }
		}
		// process iconids
		String[] providedIconIds = iconid.split(Button.STATE_SEP, 0);
		int storeIconId = ButtonIcons.NO_ICON;
		this.iconIds = new int[this.noOfStates];
		for (int i=0; i < this.noOfStates; i++) {
			if (i < providedIconIds.length) {
				try {
					storeIconId = this.iconIds[i] = Integer.parseInt(providedIconIds[i]);
				} catch (NumberFormatException e) {
					storeIconId = this.iconIds[i] = ButtonIcons.NO_ICON;
				}
			}
			else { this.iconIds[i] = storeIconId; }
		}		
	}
	
	// toString()
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("StateButtonAttributes:[");
		str.append("stateType=");
		str.append(stateType);
		str.append(";statequery=");
		str.append(statequery);
		str.append(";noOfStates=");
		str.append(noOfStates);
		str.append(";states=");
		for (int i=0; i < noOfStates; i++) { str.append(states[i]); str.append("|"); }
		str.append(";labels=");
		for (int i=0; i < noOfStates; i++) { str.append(labels[i]); str.append("|"); }
		str.append(";styles=");
		for (int i=0; i < noOfStates; i++) { str.append(styles[i]); str.append("|"); }
		str.append(";iconIds=");
		for (int i=0; i < noOfStates; i++) { str.append(iconIds[i]); str.append("|"); }
		if (stateType == de.thjunge11.avrremote.xmlModel.Button.STATETYPE_SELECT) {
			str.append(";commands=");
			for (int i=0; i < noOfChooseItems; i++) { str.append(commands[i]); str.append("|"); }
			str.append(";list=");
			for (int i=0; i < noOfChooseItems; i++) { str.append(list[i]); str.append("|"); }
		}
		else {
			str.append(";commands=");
			for (int i=0; i < noOfStates; i++) { str.append(commands[i]); str.append("|"); }
		}
		
		str.append("]");
		return str.toString();
	}
}
