package es.bsc.inb.ades.rest.api.model;

import java.util.List;

import es.bsc.inb.ades.rest.api.util.VisualizationHTMLUtil;

/**
 * 
 * @author jcorvi
 *
 */
public class Annotation implements Comparable<Annotation> {

	private String text;
	
	private String value;

	private Integer startOffset;
	
	private Integer endOffset;
	
	private List<Feature> features;
	
	private Boolean processed;
	
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Integer getStartOffset() {
		return startOffset;
	}

	public void setStartOffset(Integer startOffset) {
		this.startOffset = startOffset;
	}

	public Integer getEndOffset() {
		return endOffset;
	}

	public void setEndOffset(Integer endOffset) {
		this.endOffset = endOffset;
	}

	public List<Feature> getFeatures() {
		return features;
	}

	public void setFeatures(List<Feature> features) {
		this.features = features;
	}

	@Override
	public int compareTo(Annotation arg0) {
		return this.getStartOffset() > arg0.getStartOffset() ? 1 : this.getStartOffset() < arg0.getStartOffset() ? -1 : 0;
	}
	
	public String getValue() {
		return value;
	}
	
	
	
//	private String getSendCode() {
//		for (Feature feature : features) {
//			if(feature.getName().equals("CDISC_SEND_CODE")) {
//				return feature.getValue();
//			}
//		}
//		for (Feature feature : features) {
//			if(feature.getName().equals("ETOX_SEND_CODE")  || feature.getName().equals("ETOX_SEND_DOMAIN_CODE")) {
//				return feature.getValue();
//			}
//		}
//		for (Feature feature : features) {
//			if(feature.getName().equals("MANUAL_SEND_CODE")) {
//				return feature.getValue();
//			}
//		}
//		return text;
//	}

	public Boolean getProcessed() {
		return processed;
	}

	public void setProcessed(Boolean processed) {
		this.processed = processed;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	/**
	 * Return feature
	 * @param string
	 * @return
	 */
	public String getFeature(String featureName) {
		for (Feature feature : features) {
			if(feature.getName().equals(featureName)) {
				return feature.getValue();
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @return
	 */
	public String generateHTMLFeatures() {
		StringBuilder htmlFeatures = new StringBuilder();
		String sources = this.getFeature("SOURCES");
		if(sources!=null) {
			VisualizationHTMLUtil.generateHTMLLABEL(this, htmlFeatures);
			if(sources.contains("CDISC")) {
				VisualizationHTMLUtil.generateHTMLCDISC(this, htmlFeatures);
			}
			if(sources.contains("ETOX")) {
				VisualizationHTMLUtil.generateHTMLETOX(this, htmlFeatures);
			}
			if(sources.contains("UMLS")) {
				VisualizationHTMLUtil.generateHTMLUMLS(this, htmlFeatures);
			}
			if(sources.contains("DNORM")) {
				VisualizationHTMLUtil.generateHTMLDNORM(this, htmlFeatures);
			}
			if(sources.contains("LIMTOX")) {
				VisualizationHTMLUtil.generateHTMLLIMTOX(this, htmlFeatures);
			}
			if(sources.contains("MY_ONTOLOGY")) {
				VisualizationHTMLUtil.generateHTMLMYONTOLOGY(this, htmlFeatures);
			}
		}else {
			for (Feature feature : features) {
				htmlFeatures = htmlFeatures.append(VisualizationHTMLUtil.getFeature(feature));
			}	
		}
		return htmlFeatures.toString();
	}

	
	
	

}
