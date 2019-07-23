package es.bsc.inb.ades.rest.api.repository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import es.bsc.inb.ades.rest.api.model.Document;

/**
 * Custom implementation of DocumentRepository
 * @author jcorvi
 *
 */
public class DocumentRepositoryImpl implements DocumentRepositoryCustom{

	@Autowired
    MongoTemplate mongoTemplate;
	
	public List<Document> findByComplexCustom() {
		// TODO Auto-generated method stub
		return null;
	}
	
	

}
