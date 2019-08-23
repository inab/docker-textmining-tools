package es.bsc.inb.ades.relation.extraction.model;

import java.util.ArrayList;
import java.util.List;

import gate.Annotation;

public class Finding implements Cloneable{  
	
	private Integer id;
	
	private List<Annotation> annotations;
	
	public Finding(Integer id) {
		this.id = id;
		this.annotations = new ArrayList<Annotation>();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public List<Annotation> getAnnotations() {
		return annotations;
	}

	public void setAnnotations(List<Annotation> annotations) {
		this.annotations = annotations;
	}

	public void addAnnotation(Annotation finding) {
		annotations.add(finding);
	}
	
	public Finding clone() {
		try {
			return (Finding)super.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
