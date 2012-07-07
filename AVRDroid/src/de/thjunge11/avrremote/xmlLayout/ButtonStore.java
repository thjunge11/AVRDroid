package de.thjunge11.avrremote.xmlLayout;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.util.Log;
import de.thjunge11.avrremote.BuildConfig;
import de.thjunge11.avrremote.Constants;
import de.thjunge11.avrremote.xmlModel.Button;
import de.thjunge11.avrremote.xmlModel.Buttonslayout;
import de.thjunge11.avrremote.xmlModel.Page;

// wrapper class for deserialized ButtonLayout object 
public class ButtonStore {
	
	private static final String TAG = ButtonStore.class.getSimpleName();
	public final static int BUTTON_BASE_ID = 1000;
	public final static int STREAM_NOT_INITIALIZED = -1;
	public final static int MODIFY_LABEL = 1;
	public final static int MODIFY_ICONID = 2;
	public final static int MODIFY_COMMAND = 3;
	public final static int MODIFY_COMMENT = 4;
	public final static int MODIFY_STYLE = 5;
	public final static int MODIFY_STATES = 101;
	public final static int MODIFY_STATE_QUERY = 102;
	public final static int MODIFY_STATE_LABELS = 103;
	public final static int MODIFY_STATE_ICONS = 104;
	public final static int MODIFY_STATE_STYLES = 105;
	public final static int MODIFY_STATE_COMMANDS = 106;
	public final static int MODIFY_STATE_LIST = 107;
	
	
	
	// fields
	static private int intButtonWidth = 0;
	static private int intButtonHeight = 0;
	static private Buttonslayout xmlButtonslayout;
	static private Vector<Integer> indexPagesVertical = new Vector<Integer>();
	static private Vector<Integer> indexPagesHorizontal = new Vector<Integer>();
	static private HashMap<Integer,ButtonAttributes> mapButtonAttributes = new HashMap<Integer,ButtonAttributes>();
	static private ConcurrentHashMap<Integer,StateButtonAttributes> mapStateButtonAttributes = new ConcurrentHashMap<Integer,StateButtonAttributes>();
	static private int buttonStreamPageindex = STREAM_NOT_INITIALIZED;
	static private int buttonStreamButtonindex = 0;
	static private boolean buttonStreamNewRow = false;
	static private boolean buttonStreamSeperator = false;
	static private int buttonStreamSkip = 0;
	static private boolean buttonStreamWasView = false;
	
	// methods
	static public void clear() {
		indexPagesHorizontal.clear();
		indexPagesVertical.clear();
		buttonStreamPageindex = STREAM_NOT_INITIALIZED;
		buttonStreamNewRow = false;
		buttonStreamSeperator = false;
		buttonStreamSkip = 0;
		buttonStreamWasView = false;
		mapButtonAttributes.clear();
		mapStateButtonAttributes.clear();
	}
		
	// getters
	static public int getButtonwidth() {
		return intButtonWidth;
	}
	static public int getButtonHeight() {
		return intButtonHeight;
	}
	static public int getNoOfPages(int orientation) {
		switch (orientation) {
		case Page.ORIENTATION_HORIZONTAL : 
			return indexPagesHorizontal.size();
		case Page.ORIENTATION_VERTICAL : 
			return indexPagesVertical.size();
		}
		return 0;
	}
	static public int getNoOfButtons(int pageNo, int orientation) {
		switch (orientation) {
		case Page.ORIENTATION_HORIZONTAL : 
			if ((pageNo > 0) && (pageNo <= indexPagesHorizontal.size())) {
				int pageIndex = indexPagesHorizontal.get(pageNo-1);
				return xmlButtonslayout.getPages().get(pageIndex).getButtons().size();
			}
			break;
		case Page.ORIENTATION_VERTICAL : 
			if ((pageNo > 0) && (pageNo <= indexPagesVertical.size())) {
				int pageIndex = indexPagesVertical.get(pageNo-1);
				return xmlButtonslayout.getPages().get(pageIndex).getButtons().size();
			}
			break;
		}
		
		return 0;
	}
	static public String getPageName(int pageNo, int orientation) {
		switch (orientation) {
		case Page.ORIENTATION_HORIZONTAL : 
			if ((pageNo > 0) && (pageNo <= indexPagesHorizontal.size())) {
				int pageIndex = indexPagesHorizontal.get(pageNo-1);
				return xmlButtonslayout.getPages().get(pageIndex).getName();
			}
			break;
		case Page.ORIENTATION_VERTICAL : 
			if ((pageNo > 0) && (pageNo <= indexPagesVertical.size())) {
				int pageIndex = indexPagesVertical.get(pageNo-1);
				return xmlButtonslayout.getPages().get(pageIndex).getName();
			}
			break;
		}
		
		return "";
	}
	static public String getButtonCommand (int id) {
		if (mapButtonAttributes.containsKey(id)) {
			return mapButtonAttributes.get(id).getCommand();
		}
		else {
			return "";
		}
	}
	static public String getButtonComment (int id) {
		if (mapButtonAttributes.containsKey(id)) {
			return mapButtonAttributes.get(id).getComment();
		}
		else {
			return "";
		}
	}
	static public String getButtonLabel (int id) {
		if (mapButtonAttributes.containsKey(id)) {
			return mapButtonAttributes.get(id).getLabel();
		}
		else {
			return "";
		}
	}
	static public String getButtonStyle (int id) {
		if (mapButtonAttributes.containsKey(id)) {
			return mapButtonAttributes.get(id).getStyle();
		}
		else {
			return "";
		}
	}
	static public int getStateType (int id) {
		if (mapButtonAttributes.containsKey(id)) {
			return mapButtonAttributes.get(id).getStateType();
		}
		else {
			return Button.STATETYPE_NONE;
		}
	}	
	static public int getButtonIconId (int id) {
		if (mapButtonAttributes.containsKey(id)) {
			return mapButtonAttributes.get(id).getIconId();
		}
		else {
			return ButtonIcons.NO_ICON;
		}
	}
	static public String getButtonStates (int id) {
		if (mapButtonAttributes.containsKey(id)) {
			return xmlButtonslayout.getPages().get(mapButtonAttributes.get(id).getPageId()).getButtons().get(mapButtonAttributes.get(id).getButtonId()).getStates();
		}
		else {
			return "";
		}
	}
	static public String getButtonStateQuery (int id) {
		if (mapButtonAttributes.containsKey(id)) {
			return xmlButtonslayout.getPages().get(mapButtonAttributes.get(id).getPageId()).getButtons().get(mapButtonAttributes.get(id).getButtonId()).getStateQuery();
		}
		else {
			return "";
		}
	}
	static public String getButtonIconIds (int id) {
		if (mapButtonAttributes.containsKey(id)) {
			return xmlButtonslayout.getPages().get(mapButtonAttributes.get(id).getPageId()).getButtons().get(mapButtonAttributes.get(id).getButtonId()).getIconIds();
		}
		else {
			return "";
		}
	}
	static public String getButtonStateLabels (int id) {
		if (mapButtonAttributes.containsKey(id)) {
			return xmlButtonslayout.getPages().get(mapButtonAttributes.get(id).getPageId()).getButtons().get(mapButtonAttributes.get(id).getButtonId()).getLabel();
		}
		else {
			return "";
		}
	}
	static public String[] getButtonChooserList (int id) {
		if (mapStateButtonAttributes.containsKey(id)) {
			return mapStateButtonAttributes.get(id).getList();
		}
		else {
			return null;
		}
	}
	static public String getButtonStateStyles (int id) {
		if (mapButtonAttributes.containsKey(id)) {
			return xmlButtonslayout.getPages().get(mapButtonAttributes.get(id).getPageId()).getButtons().get(mapButtonAttributes.get(id).getButtonId()).getStyle();
		}
		else {
			return "";
		}
	}
	static public String getButtonStateCommands (int id) {
		if (mapButtonAttributes.containsKey(id)) {
			return xmlButtonslayout.getPages().get(mapButtonAttributes.get(id).getPageId()).getButtons().get(mapButtonAttributes.get(id).getButtonId()).getValue();
		}
		else {
			return "";
		}
	}
	static public String getButtonStateList (int id) {
		if (mapButtonAttributes.containsKey(id)) {
			return xmlButtonslayout.getPages().get(mapButtonAttributes.get(id).getPageId()).getButtons().get(mapButtonAttributes.get(id).getButtonId()).getList();
		}
		else {
			return "";
		}
	}
	static public String getButtonStateCommand(int id, int stateId) {
		if (mapStateButtonAttributes.containsKey(id)) {
			return mapStateButtonAttributes.get(id).getCommand(stateId);
		}
		else {
			return null;
		}
	}
	static public String getButtonState (int id) {
		if (mapStateButtonAttributes.containsKey(id)) {
			return mapStateButtonAttributes.get(id).getState(mapStateButtonAttributes.get(id).getStoredState());
		}
		else {
			return null;
		}
	}
	static public String getInterpretedButtonState (int id) {
		if (mapStateButtonAttributes.containsKey(id)) {
			return mapStateButtonAttributes.get(id).getInterpretedState(mapStateButtonAttributes.get(id).getStoredState());
		}
		else {
			return null;
		}
	}
	
	// states
	static public boolean isButtonStateDefined(int id) {
		if (mapStateButtonAttributes.containsKey(id)) {
			if (mapStateButtonAttributes.get(id).getStoredState() == StateButtonAttributes.STATE_UNDEFINED) {
				return false;
			}
		}
		return true;
	}
	
	static public Vector<String> getStates() {
		Vector<String> states = new Vector<String>();
		for (StateButtonAttributes stateButtonAttributes : mapStateButtonAttributes.values()) {
			if (stateButtonAttributes.getStates() != null) {
				states.addAll(Arrays.asList(stateButtonAttributes.getStates()));
			}
		}
		// hack to remove duplicates
		return new Vector<String>(new LinkedHashSet<String>(states));
	}
	
	static public Vector<StateQueryAttributes> getStateQueries() {
		Vector<StateQueryAttributes> stateQueries = new Vector<StateQueryAttributes>();
		Vector<String> stateQueryStrings = new Vector<String>();
		for (Map.Entry<Integer, StateButtonAttributes> entry : mapStateButtonAttributes.entrySet()) {
			if (entry.getValue().getStateQuery() != null) {
				if (!stateQueryStrings.contains(entry.getValue().getStateQuery())) {
					stateQueryStrings.add(entry.getValue().getStateQuery());
					if (BuildConfig.DEBUG) Log.d(TAG, "getStateQueries(): added statequery=" + entry.getValue().getStateQuery());
					stateQueries.add(new StateQueryAttributes(entry.getValue().getStateQuery(), entry.getKey()));
				}
			}
		}
		return stateQueries;
	}
	
	static public void resetStatesReceived() {
		for (Map.Entry<Integer, StateButtonAttributes> entry : mapStateButtonAttributes.entrySet()){
			entry.getValue().resetStateReceived();
		}
	}
	
	static public boolean areStatesReceived(String stateQuery) {
		if (BuildConfig.DEBUG) Log.d(TAG, "areStatesReceived(): called with statequery=:" + stateQuery);
		// return false if at least on button with corresponding statequery has not received a state in processState yet
		for (Map.Entry<Integer, StateButtonAttributes> entry : mapStateButtonAttributes.entrySet()){
			if (entry.getValue().getStateQuery() != null) {
				if (entry.getValue().getStateQuery().equals(stateQuery)) {
					if (entry.getValue().getStateReceived() == false) {
						if (BuildConfig.DEBUG) Log.d(TAG, "areStatesReceived(): found:" + entry.getValue().toString());
						return false;
					}
				}
			}
		}
		return true;
	}
	
	static public Vector<Integer> processState(String receivedState) {
		Vector<Integer> matchedKeys = new Vector<Integer>();
		Vector<Integer> matchedKeysStateIds = new Vector<Integer>();
		
		// mark matched buttons
		// #1: for (Map.Entry<Integer, StateButtonAttributes> entry : mapStateButtonAttributes.entrySet())
		// 
		// #2: Iterator<Map.Entry<Integer, StateButtonAttributes>> entries = mapStateButtonAttributes.entrySet().iterator(); {
		// Map.Entry<Integer, StateButtonAttributes> entry = entries.next(); ... }
		
		for (Map.Entry<Integer, StateButtonAttributes> entry : mapStateButtonAttributes.entrySet()){
		    
		    if (entry.getValue().getParamType() == StateButtonAttributes.PARAM_TYPE_NONE) {
		    	if (entry.getValue().getStates() != null) {
			    	String[] states = entry.getValue().getStates();
			    	for (int i=0; i < states.length; i++) {
			    		if (states[i].equals(receivedState)) {
				    		if (BuildConfig.DEBUG) Log.d(TAG, "processState(): found exact match:" + states[i]);
				    		matchedKeys.add(entry.getKey());
				    		matchedKeysStateIds.add(i);
				    		break;
				    	}		    	
				    }
		    	}
		    }
		    else {
		    	String state = entry.getValue().getState(0);
		    	try {
		    		state = state.substring(0, state.length() -1);
		    	} catch (IndexOutOfBoundsException e) {
		    	}
		    	if (receivedState.startsWith(state)) {
		    		if (BuildConfig.DEBUG) Log.d(TAG, "processState(): found param match:" + state);
		    		matchedKeys.add(entry.getKey());
		    		matchedKeysStateIds.add(0);
		    	}
		    }
		}
		
		// change states
		if (matchedKeys.size() > 0) {
			for (int i=0; i < matchedKeys.size(); i++) {
				StateButtonAttributes storeAttr = mapStateButtonAttributes.get(matchedKeys.get(i));
				mapStateButtonAttributes.put(matchedKeys.get(i), new StateButtonAttributes(storeAttr, matchedKeysStateIds.get(i), receivedState, true));	
			}
		}
		return matchedKeys;
	}
	
	
	// Modify
	static public boolean modify(int element, int id, String newValue) {
		
		int pageid;
		int buttonid;
		
		if (!mapButtonAttributes.containsKey(id)) {
			return false;
		}
		if ((element > MODIFY_STATES) && !mapStateButtonAttributes.containsKey(id)) {
			return false;
		}
			
		else {
			pageid = mapButtonAttributes.get(id).getPageId();
			buttonid = mapButtonAttributes.get(id).getButtonId();
			
			// check if ids exists in xmlButtonslayout object
			if (pageid < 0 || pageid >= xmlButtonslayout.getPages().size()) {
				return false;
			}
			if (buttonid <0 || buttonid >= xmlButtonslayout.getPages().get(pageid).getButtons().size()) {
				return false;
			}
		}
		
		// access all safe
		switch (element) {
			case MODIFY_LABEL :
				xmlButtonslayout.getPages().get(pageid).getButtons().get(buttonid).setLabel(newValue);
				break;
			case MODIFY_COMMAND :
				xmlButtonslayout.getPages().get(pageid).getButtons().get(buttonid).setCommand(newValue);
				break;
			case MODIFY_COMMENT :
				xmlButtonslayout.getPages().get(pageid).getButtons().get(buttonid).setComment(newValue);
				break;
			case MODIFY_STYLE :
				xmlButtonslayout.getPages().get(pageid).getButtons().get(buttonid).setStyle(newValue);
				break;
			case MODIFY_ICONID :
				int newIconId = 0;
				try {
					newIconId = Integer.parseInt(newValue);
				} catch (NumberFormatException e) {
					Log.e(TAG, "modify():" + e.getMessage());
					return false;
				}
				xmlButtonslayout.getPages().get(pageid).getButtons().get(buttonid).setIconId(newIconId);
				break;
				
			// modifieing a state needs it removed from mapStateButtonIds
			case MODIFY_STATES :
				xmlButtonslayout.getPages().get(pageid).getButtons().get(buttonid).setStates(newValue);
				mapStateButtonAttributes.remove(id);
				break;
			case MODIFY_STATE_QUERY :
				xmlButtonslayout.getPages().get(pageid).getButtons().get(buttonid).setStateQuery(newValue);
				mapStateButtonAttributes.remove(id);
				break;
			case MODIFY_STATE_LABELS :
				xmlButtonslayout.getPages().get(pageid).getButtons().get(buttonid).setLabel(newValue);
				mapStateButtonAttributes.remove(id);
				break;
			case MODIFY_STATE_STYLES :
				xmlButtonslayout.getPages().get(pageid).getButtons().get(buttonid).setStyle(newValue);
				mapStateButtonAttributes.remove(id);
				break;
			case MODIFY_STATE_ICONS :
				xmlButtonslayout.getPages().get(pageid).getButtons().get(buttonid).setIconIds(newValue);
				mapStateButtonAttributes.remove(id);
				break;
			case MODIFY_STATE_COMMANDS :
				xmlButtonslayout.getPages().get(pageid).getButtons().get(buttonid).setCommand(newValue);
				mapStateButtonAttributes.remove(id);
				break;
			case MODIFY_STATE_LIST :
				xmlButtonslayout.getPages().get(pageid).getButtons().get(buttonid).setList(newValue);
				mapStateButtonAttributes.remove(id);
				break;
				
				
			default:
				return false;
		}
		return true;
	}
	
	// read xml object
	static public boolean readButtonsFromXmlInputStream(InputStream inputStream) {
        
		// clear store
		clear();
		
		// try to deserialize xml file
		try {
			Serializer serializer = new Persister();
			xmlButtonslayout = serializer.read(Buttonslayout.class, inputStream);
		} catch (InvocationTargetException ite) {
			if (ite.getCause() != null) {
				Log.e(TAG,"readButtonsFromXmlInputStream(): " + ite.getCause().getMessage());
			}
			else {
				Log.e(TAG,"readButtonsFromXmlInputStream(): " + ite.getMessage());
			}
			return false;
		} catch (Exception e) {
			Log.e(TAG,"readButtonsFromXmlInputStream(): " + e.getMessage());
			return false;
		}
		
		if (Constants.DEBUG) Log.d(TAG, xmlButtonslayout.toString());
		
		// index pages
		for (int i = 0 ; i < xmlButtonslayout.getPages().size(); i++) {
			switch (xmlButtonslayout.getPages().get(i).getOrientation()) {
			case Page.ORIENTATION_HORIZONTAL : 
				indexPagesHorizontal.add(i);
				break;
			case Page.ORIENTATION_VERTICAL :
				indexPagesVertical.add(i);
				break;
			}
		}
		
		return true;
	}
	static public XmlValidationResults validateXmlInputStream(InputStream inputStream) {
        
		// try to deserialize xml file
		Buttonslayout validateButtonslayout;
		String errorMessage = "";
		
		try {
			Serializer serializer = new Persister();
			validateButtonslayout = serializer.read(Buttonslayout.class, inputStream);
		
		} catch (InvocationTargetException ite) {	
			if (ite.getCause() != null) {
				errorMessage += ite.getCause().getMessage();
				Log.e(TAG,"validateXmlInputStream(): " + errorMessage);				
			}
			else {
				errorMessage += ite.getMessage();
				Log.e(TAG,"validateXmlInputStream(): " + errorMessage);
			}
			return new XmlValidationResults(false, 0, 0, null, null, errorMessage);
		} catch (Exception e) {
			errorMessage += e.getMessage();
			Log.e(TAG, "validateXmlInputStream(): " + errorMessage);
			return new XmlValidationResults(false, 0, 0, null, null, errorMessage);
		}
		if (Constants.DEBUG) Log.d(TAG, validateButtonslayout.toString());
		
		// calc layout statistics
		int pagesV = 0;
		int pagesH = 0;
		int pages = validateButtonslayout.getPages().size();
		int[] buttonsV = new int[pages];
		int[] buttonsH = new int[pages];
		
		for (int i = 0; i < pages; i++) {
			switch (validateButtonslayout.getPages().get(i).getOrientation()) {
			case Page.ORIENTATION_HORIZONTAL : 
				buttonsH[pagesH] = validateButtonslayout.getPages().get(i).getButtons().size();
				pagesH++;
				break;
			case Page.ORIENTATION_VERTICAL :
				buttonsV[pagesV] = validateButtonslayout.getPages().get(i).getButtons().size();
				pagesV++;
				break;
			}
		}

		return new XmlValidationResults(true, pagesV, pagesH, buttonsV, buttonsH, "no error");
	}
	
	// write xml object
	static public boolean writeButtonsToOutputStream(OutputStream os) {
		// try to serialize xml file
		try {
			Serializer serializer = new Persister();
			serializer.write(xmlButtonslayout, os);	
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			return false;
		}
		return true;
	}
	
	// streamlined button output
	static public boolean initializeButtonStream(int pageNo, 
			int orientation, int displayWidth, int marginWidthPerButton, int marginWidthLayout) {
		switch (orientation) {
		case Page.ORIENTATION_HORIZONTAL : 
			if ((pageNo > 0) && (pageNo <= indexPagesHorizontal.size())) {
				buttonStreamPageindex = indexPagesHorizontal.get(pageNo-1);
				buttonStreamButtonindex = 0;
				initializePageAttributes(displayWidth, marginWidthPerButton, marginWidthLayout);
				return true;
			}
			break;
		case Page.ORIENTATION_VERTICAL : 
			if ((pageNo > 0) && (pageNo <= indexPagesVertical.size())) {
				buttonStreamPageindex = indexPagesVertical.get(pageNo-1);
				buttonStreamButtonindex = 0;
				initializePageAttributes(displayWidth, marginWidthPerButton, marginWidthLayout);
				return true;
			}
			break;
		}
		return false;
	}
	static public int streamGetCurrentXmlPage() {
		return buttonStreamPageindex;
	}
	static public boolean streamIsInitialized() {
		if (buttonStreamPageindex != STREAM_NOT_INITIALIZED) return true;
		else return false; 
	}
	static public boolean streamHasButton() {
		return (buttonStreamPageindex != STREAM_NOT_INITIALIZED) &&
				(buttonStreamButtonindex < xmlButtonslayout.getPages().get(buttonStreamPageindex).getButtons().size());
	}
	static public boolean streamIsNewRow() {
		return buttonStreamNewRow;
	}
	static public boolean streamIsSeperator() {
		return buttonStreamSeperator;
	}
	static public int streamSkipButton () {
		return buttonStreamSkip;
	}
	static public XmlButton getButtonFromStream() {
		
		// test if stream is initialized and is not at eof
		if ((buttonStreamPageindex != STREAM_NOT_INITIALIZED) &&
				(buttonStreamButtonindex < xmlButtonslayout.getPages().get(buttonStreamPageindex).getButtons().size())) {
			
			int stateType = xmlButtonslayout.getPages().get(buttonStreamPageindex).getButtons().get(buttonStreamButtonindex).getStateType();
			int id = BUTTON_BASE_ID * (buttonStreamPageindex + 1) + buttonStreamButtonindex;
			int span = xmlButtonslayout.getPages().get(buttonStreamPageindex).getButtons().get(buttonStreamButtonindex).getSpan();
			String comment = xmlButtonslayout.getPages().get(buttonStreamPageindex).getButtons().get(buttonStreamButtonindex).getComment();
			String label = xmlButtonslayout.getPages().get(buttonStreamPageindex).getButtons().get(buttonStreamButtonindex).getLabel();
			int iconid = xmlButtonslayout.getPages().get(buttonStreamPageindex).getButtons().get(buttonStreamButtonindex).getIconId();
			String style = xmlButtonslayout.getPages().get(buttonStreamPageindex).getButtons().get(buttonStreamButtonindex).getStyle();
			String command = xmlButtonslayout.getPages().get(buttonStreamPageindex).getButtons().get(buttonStreamButtonindex).getValue();
			boolean enabled = true;
			boolean viewonly = false;
			
			if (stateType != Button.STATETYPE_NONE) {
				
				if (!mapStateButtonAttributes.containsKey(id)) {
					String statequery = xmlButtonslayout.getPages().get(buttonStreamPageindex).getButtons().get(buttonStreamButtonindex).getStateQuery();
					String states = xmlButtonslayout.getPages().get(buttonStreamPageindex).getButtons().get(buttonStreamButtonindex).getStates();
					String iconIds = xmlButtonslayout.getPages().get(buttonStreamPageindex).getButtons().get(buttonStreamButtonindex).getIconIds();
					String list = xmlButtonslayout.getPages().get(buttonStreamPageindex).getButtons().get(buttonStreamButtonindex).getList();
					StateButtonAttributes stateButtonAttributes = new StateButtonAttributes(stateType, statequery, states, list, command, label, style, iconIds, 
							StateButtonAttributes.STATE_UNDEFINED);
					
					mapStateButtonAttributes.put(id, stateButtonAttributes);
					// if (BuildConfig.DEBUG) Log.d(TAG, stateButtonAttributes.toString());
				}
				
				int storedState = mapStateButtonAttributes.get(id).getStoredState();
				if (storedState == StateButtonAttributes.STATE_UNDEFINED) {
					enabled = false;
					// set storedState to 0 to display first state
					storedState = 0;
				}
				label = mapStateButtonAttributes.get(id).getLabel(storedState);
				iconid = mapStateButtonAttributes.get(id).getIconId(storedState);
				style = mapStateButtonAttributes.get(id).getStyle(storedState);
				command = mapStateButtonAttributes.get(id).getCommand(storedState);
				
				if (stateType == Button.STATETYPE_VIEW) {
					viewonly = true;
				}
			}
			
			buttonStreamNewRow = xmlButtonslayout.getPages().get(buttonStreamPageindex).getButtons().get(buttonStreamButtonindex).getNewRow();
			// ensure that viewonly state buttons are not in row with normal buttons 
			if ((viewonly && !buttonStreamWasView) || (!viewonly && buttonStreamWasView)) {
				buttonStreamNewRow = true;
			}
			buttonStreamWasView = viewonly;
			
			buttonStreamSeperator = xmlButtonslayout.getPages().get(buttonStreamPageindex).getButtons().get(buttonStreamButtonindex).getSeperator();
			buttonStreamSkip = xmlButtonslayout.getPages().get(buttonStreamPageindex).getButtons().get(buttonStreamButtonindex).getSkip();
			
			ButtonAttributes  buttonAttributes = new ButtonAttributes(comment, command, label, style, iconid, 
					buttonStreamPageindex, buttonStreamButtonindex, stateType, span);
			mapButtonAttributes.put(id, buttonAttributes);
			XmlButton xmlButton = new XmlButton(id, label, span, iconid, style, enabled, viewonly);
			
			// increment index for next call
			buttonStreamButtonindex++;
			
			return xmlButton;
		}
		
		return null;
	}
	
	static public XmlButton updateXmlStateButton(int id) {
		boolean enabled = true;
		boolean viewonly = false;
		int span = mapButtonAttributes.get(id).getSpan();
		int stateType = mapStateButtonAttributes.get(id).getStateType();
		int storedState = mapStateButtonAttributes.get(id).getStoredState();
		if (storedState == StateButtonAttributes.STATE_UNDEFINED) {
			enabled = false;
			// set storedState to 0 to display first state
			storedState = 0;
		}
		String label = mapStateButtonAttributes.get(id).getLabel(storedState);
		int iconid = mapStateButtonAttributes.get(id).getIconId(storedState);
		String style = mapStateButtonAttributes.get(id).getStyle(storedState);
		String command = mapStateButtonAttributes.get(id).getCommand(storedState);
		
		if (stateType == Button.STATETYPE_VIEW) {
			viewonly = true;
		}
		
		ButtonAttributes  buttonAttributes = new ButtonAttributes(mapButtonAttributes.get(id).getComment(), command, label, style, iconid, 
				mapButtonAttributes.get(id).getPageId(), mapButtonAttributes.get(id).getButtonId(), stateType, span);
		mapButtonAttributes.put(id, buttonAttributes);
		return new XmlButton(id, label, span, iconid, style, enabled, viewonly);
	}
	
		
	// utils
	static private void initializePageAttributes(int displayWidth, int marginWidthPerButton, int marginWidthLayout) {
		int intButtonsPerRow = xmlButtonslayout.getPages().get(buttonStreamPageindex).getButtonsPerRow();
		double dButtonHeightScale = xmlButtonslayout.getPages().get(buttonStreamPageindex).getScaleHeight();
		
		intButtonWidth = (int) Math.floor( (double) displayWidth / (double) intButtonsPerRow);
		intButtonWidth -= marginWidthPerButton;
		intButtonWidth -= (int) Math.floor( (double) marginWidthLayout / (double)intButtonsPerRow);
		
		intButtonHeight = (int) Math.floor( dButtonHeightScale * (double) intButtonWidth);
	}
}