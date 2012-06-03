package de.thjunge11.avrremote.xmlLayout;

public class ButtonAttributes {
	private String comment;
	private String command;
	private String label;
	private int iconid;
	private int pageid;
	private int buttonid;
	private String style;
	
	public ButtonAttributes(String comment, String command, String label,  String style, int iconid, int pageid, int buttonid) {
		this.comment = comment;
		this.command = command;
		this.iconid = iconid;
		this.pageid = pageid;
		this.buttonid = buttonid;
		this.label = label;
		this.style = style;
	}
	public String getCommand() { return command; }
	public String getComment() { return comment; }
	public int getIconId() { return iconid; }
	public int getPageId() {return pageid; }
	public int getButtonId() { return buttonid; }
	public String getLabel() { return label; }
	public String getStyle() { return style; }
}
