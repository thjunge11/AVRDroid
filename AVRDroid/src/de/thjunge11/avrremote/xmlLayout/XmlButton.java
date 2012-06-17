package de.thjunge11.avrremote.xmlLayout;

public class XmlButton {
	
	public int id;
	public String label;
	public int span;
	public int iconid;
	public String style;
	public boolean enabled;
	public boolean viewonly;
	
	XmlButton (int id, String label, int span, int iconid, String style, boolean enabled, boolean viewonly) {
		this.id = id;
		this.span = span;
		this.label = label;
		this.iconid = iconid;
		this.style = style;
		this.enabled = enabled;
		this.viewonly = viewonly;
	}
	
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("[Button:");
		str.append("id=");
		str.append(id);
		str.append(";label=");
		str.append(label);
		str.append(";span=");
		str.append(span);
		str.append(";iconid=");
		str.append(iconid);
		str.append(";style=");
		str.append(style);
		str.append("]");
		return str.toString();
	}
}


