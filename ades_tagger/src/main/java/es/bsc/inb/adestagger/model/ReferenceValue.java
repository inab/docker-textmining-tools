package es.bsc.inb.adestagger.model;

public class ReferenceValue {
	
	private String name;
	
	private String value="";

	
	
	public ReferenceValue(String name, String value) {
		super();
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	
	
	
}
