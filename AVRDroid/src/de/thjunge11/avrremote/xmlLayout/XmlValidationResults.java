package de.thjunge11.avrremote.xmlLayout;

public class XmlValidationResults {
	
	private int noPagesVertical = 0;
	private int noPagesHorizontal = 0;
	private int[] noButtonsVertical = null;
	private int[] noButtonsHorizontal = null;
	private boolean valid = false;
	private String errorlog = "";
	
	public XmlValidationResults(boolean valid, int v, int h, int[] vb, int[] hb, String errorlog) {
		this.noPagesHorizontal = h;
		this.noPagesVertical = v;
		this.noButtonsHorizontal = hb;
		this.noButtonsVertical = vb;
		this.valid = valid; 
		this.errorlog = errorlog;
	}
	public boolean isValid() {
		return valid;
	}
	public int getPagesVertical() {
		return noPagesVertical;
	}
	public int getPagesHorizontal() {
		return noPagesHorizontal;
	}
	public int getButtonsVertical(int page) {
		if (page > 0 && page <= noButtonsVertical.length && noButtonsVertical != null)
			return noButtonsVertical[page-1];
		else return 0;
	}
	public int getButtonsHorizontal(int page) {
		if (page > 0 && page <= noButtonsHorizontal.length && noButtonsHorizontal != null)
			return noButtonsHorizontal[page-1];
		else return 0;
	}
	public String toString() {
		if (valid) {
			StringBuilder builder = new StringBuilder();
			for (int i=1; i <= noPagesVertical; i++) {
				builder.append("- vertical page with " + this.getButtonsVertical(i) + " buttons.\n");
			}
			for (int i=1; i <= noPagesHorizontal; i++) {
				builder.append("- horizontal page with " + this.getButtonsHorizontal(i) + " buttons.\n");
			}
			return builder.toString();
		}
		else {
			return errorlog;
		}
	}
}
