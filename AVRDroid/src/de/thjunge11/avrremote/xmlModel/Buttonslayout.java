package de.thjunge11.avrremote.xmlModel;

import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root
public class Buttonslayout {
	
	@ElementList
	private List<Page> pages;
	
	// getters
	public List<Page> getPages() {
		return pages;
	}
	
	// toString()
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("[Buttonslayout:");
		str.append("NumberOfElements=");
		str.append(pages.size());
		str.append(";Elements=");
		str.append(pages);
		str.append("]");
		return str.toString();
	}
}
