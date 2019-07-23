package es.bsc.inb.ades.rest.api.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import es.bsc.inb.ades.rest.api.model.Document;
import es.bsc.inb.ades.rest.api.services.DocumentService;


@RestController
@CrossOrigin
public class DocumentController {
	
	@Autowired
	public DocumentService documentService;
	
	@RequestMapping("/documents/")
    public List<Document> findAll() {
        return documentService.findAll();
    }
	@RequestMapping("/documents/find/{id}")
	public List<Document> find(@PathVariable(value="id") String id) {
		return null;
	}
	
}