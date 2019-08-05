package es.bsc.inb.ades.rest.api.services;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.bsc.inb.ades.rest.api.model.Annotation;
import es.bsc.inb.ades.rest.api.model.Document;
import es.bsc.inb.ades.rest.api.model.Finding;
import es.bsc.inb.ades.rest.api.repository.DocumentRepository;


@Service
public class DocumentServiceImpl implements DocumentService {
	
	@Autowired
	public DocumentRepository documentRepository;
	
	public List<Document> findAll() {
		return documentRepository.findAll();
	}
	
	public Document findByDocumentId(Long id) {
		return documentRepository.findByDocumentId(id);
	}

	static final Map<String, String> ANNOTATION_CLASS_STYLE = createMap();
	
	
	@Override
	public String findTextSnippetByDocumentIdAndFindingId(Long id, Integer findingId) {
		Document document = this.findByDocumentId(id);
		Finding findingSelected = null;
		for (Finding finding : document.getFindings()) {
			if(finding.getFindingId().equals(findingId)) {
				findingSelected = finding;
				break;
			}
		}
		if(findingSelected!=null) {
			return this.generateFindingSnippet(document.getText(), findingSelected);
		}
		return "";
	}
	
	/**
	 * 
	 * @param text
	 * @param findingSelected
	 * @return
	 */
	private String generateFindingSnippet(String text, Finding findingSelected) {
		System.out.println(text);
		Integer offsetSlicing = 0;
		List<Annotation> all = findingSelected.generateSortedAnnotations();
		Collections.sort(all);
		Object[] data = {text, offsetSlicing};
		for (Annotation annotation : all) {
			if(!annotationSuperposition(annotation, all)) {
				data = addColorToAnnotation(data, this.getAnnotationStyleClass(annotation), annotation);
			}
			System.out.println(data[0]);
		}
		return data[0].toString();
	}
	/**
	 * 
	 * @param annotation
	 * @param all
	 * @return
	 */
	private boolean annotationSuperposition(Annotation annotation, List<Annotation> all) {
		return false;
	}

	/**
	 * 
	 * @param annotation
	 * @return
	 */
	private String getAnnotationStyleClass(Annotation annotation) {
		String annotation_type = ANNOTATION_CLASS_STYLE.get(annotation.getFeature("ANNOTATION_TYPE"));
		if(annotation_type!=null) {
			return annotation_type;
		}else {
			return "class_z";
		}
	}

	/**
	 * 
	 * @param string
	 * @param startOffset
	 * @param endOffset
	 * @return
	 */
	private Object[] addColorToAnnotation(Object[] data, String className, Annotation annotation) {
		/*String init_ = "<div onmouseover=\"document.getElementById('hoverShow2').style.display = 'inline'\"> <span title=\"pepe\" class="+"\""+className+"\">";
		String end_ = " <div style=\"display:none\" id='hoverShow2' onmouseout=\"this.style.display='none'\"><a href='http://google.com'>Google</a></div>\n" + 
				"</div></span>";*/
		String init_ = "<span onmouseout=\"document.getElementById('hoverShow"+annotation.getStartOffset()+annotation.getEndOffset()+"').style.display = 'none'\" onmouseover=\"document.getElementById('hoverShow"+annotation.getStartOffset()+annotation.getEndOffset()+"').style.display = 'inline-block'\" class="+"\""+className+"\">";
		String end_ = "<div class=\"tooltip_finding\"  id=\"hoverShow"+annotation.getStartOffset()+annotation.getEndOffset()+"\" onmouseout=\"this.style.display='none'\">"+annotation.generateHTMLFeatures()+"</div>" + 
				"</span>";
		//init_ ="<span class="+"\""+className+"\">";
		//end_ = 	"</span>";
		data[0] = data[0].toString().substring(0, annotation.getStartOffset() + new Integer(data[1].toString())) + init_ + annotation.getText() +  end_ + data[0].toString().substring(annotation.getEndOffset() + new Integer(data[1].toString()));
		data[1] = new Integer(data[1].toString()) + (init_ +  end_).length();
		annotation.setProcessed(true);
		return data;
	}
	
	private static Map<String, String> createMap() {
        Map<String, String> result = new HashMap<String, String>();
        result.put("FINDING", "class_a");
        result.put("SPECIMEN", "class_b");
        result.put("SEX", "class_c");
        result.put("GROUP", "class_d");
        result.put("STUDY_TESTCD", "class_e");
        result.put("STUDY_DOMAIN", "class_f");
        result.put("RISK_LEVEL", "class_g");
        result.put("MANIFESTATION_FINDING", "class_h");
        result.put("DOSE_QUANTITY", "class_i");
        result.put("DOSE_FREQUENCY", "class_j");
        result.put("DOSE_DURATION", "class_k");
        result.put("TREATMENT_RELATED_TRIGGER", "class_l");
        result.put("ROUTE_OF_ADMINISTRATION", "class_h");
        return Collections.unmodifiableMap(result);
    }
	
}
