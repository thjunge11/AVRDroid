package de.thjunge11.avrremote.xmlModel;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Text;
import org.simpleframework.xml.core.Validate;

@Element
public class Button{
	
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
	private int iconid;
	
	@Attribute(required=false)
	private String style;
	
	@Attribute
	private String label;
	
	@Text
	private String value;
	
	// getters
	public String getLabel() { return label; }
	public boolean getNewRow() { return newrow;	}
	public int getSpan() { return span; }
	public String getComment() { return comment; }
	public String getValue() { return value; }
	public int getSkip() { return skip; }
	public boolean getSeperator() { return seperator; }
	public int getIconId() { return iconid; }
	public String getStyle() { return style; }
	// modify
	public void setLabel(String newLabel) { if (newLabel != null) this.label = newLabel; else this.label = ""; }
	public void setCommand (String newCommand) { if (newCommand != null ) this.value = newCommand; else this.value = "";}
	public void setComment (String newComment) { if (newComment != null) this.comment = newComment; else this.comment = ""; }
	public void setIconId (int newId) { this.iconid = newId; }
	public void setStyle (String newStyle) { if (newStyle != null) this.style = newStyle; else this.style = ""; }
		
	@Validate
	public void validate() {
		if (span < 1) span = 1;
		if (comment == null) comment = "";
		if (skip < 0) skip = 0;
		if (iconid < 0) iconid = 0;
		if (style == null) style=""; 
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
		str.append("]");
		return str.toString();
	}
}
