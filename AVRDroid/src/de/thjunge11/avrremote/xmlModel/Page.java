package de.thjunge11.avrremote.xmlModel;

import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.core.Validate;

@Element
public class Page {
	
	public static final int ORIENTATION_VERTICAL = 0;
	public static final int ORIENTATION_HORIZONTAL = 1;
	public static final int ORIENTATION_UNDEF = -1;
	
	public static double DEFAULT_SCALE_HEIGHT = 0.7;
	public static double MIN_SCALE_HEIGHT = 0.1;
	public static double MAX_SCALE_HEIGHT = 10.0;
	
	public static int MAX_BUTTONS_PER_ROW = 50;
	
	@Attribute
	private String orientation;
	// for integer representation of orientation
	private int intOrientation;
	
	@Attribute
	private int buttons_per_row;
	
	@Attribute(required=false)
	private String name;
	
	@Attribute(required=false)
	private double buttonheight_scale;
		
	@ElementList
	private List<Button> buttons;
	
	// process orientation string
	@Validate
	public void validateOrientation() throws Exception {
		if (orientation.equals("portrait")) { intOrientation = ORIENTATION_VERTICAL; }
		else if (orientation.equals("landscape")) { intOrientation = ORIENTATION_HORIZONTAL; }
		else throw new XmlFileLayoutException("invalid orientation=" + orientation);
		if (name == null) { name = ""; }
		if (buttons_per_row < 1) { buttons_per_row = 1; }
		else if (buttons_per_row > MAX_BUTTONS_PER_ROW) { buttons_per_row = MAX_BUTTONS_PER_ROW; }
		if (buttonheight_scale == 0) { buttonheight_scale = DEFAULT_SCALE_HEIGHT; }
		else if (buttonheight_scale < MIN_SCALE_HEIGHT) { buttonheight_scale = MIN_SCALE_HEIGHT; }
		else if (buttonheight_scale > MAX_SCALE_HEIGHT) { buttonheight_scale = MAX_SCALE_HEIGHT; }
	}
	
	// getters
	public int getOrientation() { return intOrientation; }
	public List<Button> getButtons() { return buttons; }
	public String getName() { return name; }
	public double getScaleHeight() { return buttonheight_scale; }
	public int getButtonsPerRow() { return buttons_per_row; }
	
	// toString()
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("Page:");
		str.append("orientation=");
		switch (intOrientation) {
		case ORIENTATION_HORIZONTAL: str.append("horizontal"); break;
		case ORIENTATION_VERTICAL: str.append("vertical"); break;
		case ORIENTATION_UNDEF: str.append("undefined"); break;	}
		str.append(";name=");
		str.append(name);
		str.append(";buttonsperrow=");
		str.append(buttons_per_row);
		str.append(";scaleheight=");
		str.append(buttonheight_scale);
		str.append(";NumberOfElements=");
		str.append(buttons.size());
		str.append(";Elements=");
		str.append(buttons);
		str.append("]");
		return str.toString();
	}
}
