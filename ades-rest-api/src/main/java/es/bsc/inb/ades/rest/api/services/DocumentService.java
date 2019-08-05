package es.bsc.inb.ades.rest.api.services;

import java.util.List;

import es.bsc.inb.ades.rest.api.model.Document;


public interface DocumentService  {

	List<Document> findAll();

	Document findByDocumentId(Long id);

	String findTextSnippetByDocumentIdAndFindingId(Long id, Integer findingId);
	
}
