package es.bsc.inb.ades.rest.api.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.bsc.inb.ades.rest.api.model.Document;
import es.bsc.inb.ades.rest.api.repository.DocumentRepository;


@Service
public class DocumentServiceImpl implements DocumentService {
	
	@Autowired
	public DocumentRepository documentRepository;
	
	public List<Document> findAll() {
		return documentRepository.findAll();
	}
	
	public Document find() {
		return null;
	}
	
	
}
