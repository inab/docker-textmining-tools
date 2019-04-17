package es.bsc.inb.umlstagger.model;

import java.util.List;

public class EntityInstance {

	private String interal_code;
	
	private List<ReferenceValue> referenceValues;

	public EntityInstance(String interal_code, List<ReferenceValue> referenceValues) {
		super();
		this.interal_code = interal_code;
		this.referenceValues = referenceValues;
	}

	public String getInteral_code() {
		return interal_code;
	}

	public void setInteral_code(String interal_code) {
		this.interal_code = interal_code;
	}

	public List<ReferenceValue> getReferenceValues() {
		return referenceValues;
	}

	public void setReferenceValues(List<ReferenceValue> referenceValues) {
		this.referenceValues = referenceValues;
	}

	
	
}
