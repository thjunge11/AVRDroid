package de.thjunge11.avrremote.xmlModel;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Text;
import org.simpleframework.xml.core.Validate;

@Element
public class Button{
	
	public static final String PAUSE_SEP = "_";
	public static final String STATE_SEP = ";";
	
	public static final int STATETYPE_NONE = 0;
	public static final int STATETYPE_TOGGLE = 1;
	public static final int STATETYPE_TOGGLE_VIEW = 2;
	public static final int STATETYPE_VALUE = 3;
	public static final int STATETYPE_VALUE_VIEW = 4;
	
	@Attribute(required=false)
	private int span;
	
	@Attribute(required=false)
	private int skip;
	
	@Attribute(required=false)
	private boolean newrow;
	
	@Attribute(required=false)
	private boolean seperator;	
	
	@Attribute(required=false)
	private String comment;

	@Attribute(required=false)
	private String iconid;
	private int intIconId;
	
	@Attribute(required=false)
	private String style;
	
	@Attribute
	private String label;
	
	@Text
	private String value;
	
	// state buttons
	@Attribute(required=false)
	private String statetype;
	private int intStatetype;
	
	@Attribute(required=false)
	private String statequery;
	
	@Attribute(required=false)
	private String states;
	
	// getters
	public String getLabel() { return label; }
	public boolean getNewRow() { return newrow;	}
	public int getSpan() { return span; }
	public String getComment() { return comment; }
	public String getValue() { return value; }
	public int getSkip() { return skip; }
	public boolean getSeperator() { return seperator; }
	public int getIconId() { return intIconId; }
	public String getIconIds() { return iconid; }
	public String getStyle() { return style; }
	public int getStateType() { return intStatetype; }
	public String getStateQuery() { return statequery; }
	public String getStates() { return states; }
	
	// modify
	public void setLabel(String newLabel) { if (newLabel != null) this.label = newLabel; else this.label = ""; }
	public void setCommand (String newCommand) { if (newCommand != null ) this.value = newCommand; else this.value = "";}
	public void setComment (String newComment) { if (newComment != null) this.comment = newComment; else this.comment = ""; }
	public void setIconId (int newId) { 
		this.intIconId = newId;
		this.iconid = Integer.toString(newId); }
	public void setStyle (String newStyle) { if (newStyle != null) this.style = newStyle; else this.style = ""; }
		
	@Validate
	public void validate() throws XmlFileLayoutException {
		if (span < 1) span = 1;
		if (comment == null) comment = "";
		if (skip < 0) skip = 0;
		if (style == null) style="";
		if (iconid == null) {
			iconid="0";
			intIconId = 0;
		}
		
		// states dependent validation
		if (statetype == null) {
			intStatetype = STATETYPE_NONE;
			// transform String iconid into integer
			try {
				intIconId = Integer.parseInt(iconid);
			} catch (NumberFormatException nfe) {
				throw new XmlFileLayoutException("iconid=" + nfe.getMessage());
			}
		}
		else if (statetype.equals("toggle")) {
			intStatetype = STATETYPE_TOGGLE;
			// validate state elements
			if ((statequery == null) || (states == null)) {
				throw new XmlFileLayoutException("missing state attributes \"statequery\",  \"states\" for statetype=" + statetype);
			}
			if (statequery.equals("")) {
				throw new XmlFileLayoutException("statequery attribute must not be empty");
			}
			if (states.equals("")) {
				throw new XmlFileLayoutException("states attribute must not be empty");
			}
		}
		else {
			throw new XmlFileLayoutException("invalid statetype=" + statetype);
		}
	}
	
	// toString()
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("[Button:");
		str.append("label=");
		str.append(label);
		str.append(";comment=");
		str.append(comment);
		str.append(";iconid=");
		str.append(iconid);
		str.append(";intIconId=");
		str.append(intIconId);
		str.append(";span=");
		str.append(span);
		str.append(";skip=");
		str.append(skip);
		str.append(";newrow=");
		str.append(newrow);
		str.append(";seperator=");
		str.append(seperator);
		str.append(";style=");
		str.append(style);
		str.append(";value=");
		str.append(value);
		str.append(";statetype=");
		str.append(statetype);
		str.append(";intStatetype=");
		str.append(intStatetype);
		str.append(";statequery=");
		str.append(statequery);
		str.append(";states=");
		str.append(states);
		str.append("]");
		return str.toString();
	}
}
