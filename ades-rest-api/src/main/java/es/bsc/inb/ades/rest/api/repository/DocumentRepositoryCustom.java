package es.bsc.inb.ades.rest.api.repository;

import es.bsc.inb.ades.rest.api.model.Finding;

/**
 * Custom Interface for Document
 * @author jcorvi
 *
 */
public interface DocumentRepositoryCustom {
	/**
	 * 
	 * @return
	 */
	Finding findByDocumentIdAndFindingId(Long id);
 	
}
