package es.bsc.inb.ades.rest.api.repository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import es.bsc.inb.ades.rest.api.model.Document;
import es.bsc.inb.ades.rest.api.model.Finding;

/**
 * Custom implementation of DocumentRepository
 * @author jcorvi
 *
 */
public class DocumentRepositoryImpl implements DocumentRepositoryCustom{

	@Autowired
    MongoTemplate mongoTemplate;

	@Override
	public Finding findByDocumentIdAndFindingId(Long id) {
		//mongoTemplate.find({ status: "D" }); 
		return null;
	}
	
	

}
