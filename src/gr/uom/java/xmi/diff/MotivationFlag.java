package gr.uom.java.xmi.diff;

public enum MotivationFlag {
		
	EM_HAS_ADDED_PARAMETERS("Extracted Operation has more parameters than the Source Operation");
	
	private String description;
	
    private MotivationFlag(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
	

}
