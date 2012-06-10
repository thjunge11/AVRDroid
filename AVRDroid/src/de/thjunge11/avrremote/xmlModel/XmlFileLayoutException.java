package de.thjunge11.avrremote.xmlModel;

public class XmlFileLayoutException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public XmlFileLayoutException() {
		super("no error message");
	}
	
	public XmlFileLayoutException(String message) {
		super(message);
	}
}
