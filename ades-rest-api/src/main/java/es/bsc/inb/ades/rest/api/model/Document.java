package es.bsc.inb.ades.rest.api.model;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;

@org.springframework.data.mongodb.core.mapping.Document(collection="reports")
public class Document{
	@Field("_id")
	private ObjectId _id;
	
	@Field("id")
	private Long documentId;
	
	private String name;
	
	private String text;
	
	//private String textWithAnnotations;
	
	private List<Finding> findings;
	
	public Document() {
		super();
	}

	

	public ObjectId get_id() {
		return _id;
	}



	public void set_id(ObjectId _id) {
		this._id = _id;
	}
	
	

	public Long getDocumentId() {
		return documentId;
	}



	public void setDocumentId(Long documentId) {
		this.documentId = documentId;
	}

	

	



	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}



	public List<Finding> getFindings() {
		return findings;
	}



	public void setFindings(List<Finding> findings) {
		this.findings = findings;
	}

	

}
