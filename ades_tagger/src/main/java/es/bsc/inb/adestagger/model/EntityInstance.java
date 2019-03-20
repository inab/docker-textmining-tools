package es.bsc.inb.adestagger.model;

import java.util.List;

public class EntityInstance {

	private Integer interal_code;
	
	private List<ReferenceValue> referenceValues;

	public EntityInstance(Integer interal_code, List<ReferenceValue> referenceValues) {
		super();
		this.interal_code = interal_code;
		this.referenceValues = referenceValues;
	}

	public Integer getInteral_code() {
		return interal_code;
	}

	public void setInteral_code(Integer interal_code) {
		this.interal_code = interal_code;
	}

	public List<ReferenceValue> getReferenceValues() {
		return referenceValues;
	}

	public void setReferenceValues(List<ReferenceValue> referenceValues) {
		this.referenceValues = referenceValues;
	}

	
	
}
