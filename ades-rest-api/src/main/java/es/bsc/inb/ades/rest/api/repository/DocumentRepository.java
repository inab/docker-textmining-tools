package es.bsc.inb.ades.rest.api.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import es.bsc.inb.ades.rest.api.model.Document;

@Repository
public interface DocumentRepository extends DocumentRepositoryCustom, MongoRepository<Document, String> {
	
	Document findBy_id(ObjectId _id);
	
	Document findByName(String name);
	
	Document findByDocumentId(Long id);

	
	
}
