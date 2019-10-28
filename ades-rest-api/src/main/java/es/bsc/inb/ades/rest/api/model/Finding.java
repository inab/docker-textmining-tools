package es.bsc.inb.ades.rest.api.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Field;

/**
 * 
 * @author jcorvi
 *
 */
public class Finding {
	@Field("id")
	private Integer findingId;
	
	private Annotation finding_field;

	@Field("FINDING")
	private Annotation finding;
	
	@Field("SPECIMEN")
	private Annotation specimen;
	
	@Field("SEX")
	private Annotation sex;
	
	@Field("MANIFESTATION_FINDING")
	private Annotation manifestation_finding;

	@Field("GROUP")
	private Annotation group;
	
	@Field("DOSE")
	private Annotation dose;
	
	@Field("STUDY_TESTCD")
	private Annotation study_testcd;
	
	@Field("STUDY_DOMAIN")
	private Annotation study_domain;
	
	@Field("IS_TREATMENT_RELATED")
	private Annotation is_treatment_related;
	
	@Field("RISK_LEVEL")
	private Annotation risk_level;
	
	@Field("ROUTE_OF_AMINISTRATION")
	private Annotation route_of_administration;
	
	@Field("DOSE_QUANTITY")
	private Annotation dose_quantity;
	
	@Field("DOSE_FREQUENCY")
	private Annotation dose_frequency;
	
	@Field("DOSE_DURATION")
	private Annotation dose_duration;
	
	@Field("RELEVANT_TEXT")
	private Annotation relevant_text;
	
	public Finding() {}
	
	
	public Integer getFindingId() {
		return findingId;
	}

	public void setFindingId(Integer findingId) {
		this.findingId = findingId;
	}




	public Annotation getFinding_field() {
		return finding_field;
	}




	public void setFinding_field(Annotation finding_field) {
		this.finding_field = finding_field;
	}




	public Annotation getFinding() {
		return finding;
	}




	public void setFinding(Annotation finding) {
		this.finding = finding;
	}




	public Annotation getSpecimen() {
		return specimen;
	}




	public void setSpecimen(Annotation specimen) {
		this.specimen = specimen;
	}




	public Annotation getSex() {
		return sex;
	}




	public void setSex(Annotation sex) {
		this.sex = sex;
	}




	public Annotation getManifestation_finding() {
		return manifestation_finding;
	}




	public void setManifestation_finding(Annotation manifestation_finding) {
		this.manifestation_finding = manifestation_finding;
	}




	public Annotation getGroup() {
		return group;
	}




	public void setGroup(Annotation group) {
		this.group = group;
	}




	public Annotation getStudy_testcd() {
		return study_testcd;
	}




	public void setStudy_testcd(Annotation study_testcd) {
		this.study_testcd = study_testcd;
	}




	public Annotation getStudy_domain() {
		return study_domain;
	}




	public void setStudy_domain(Annotation study_domain) {
		this.study_domain = study_domain;
	}

	public Annotation getRelevant_text() {
		return relevant_text;
	}


	public void setRelevant_text(Annotation relevant_text) {
		this.relevant_text = relevant_text;
	}


	public Annotation getIs_treatment_related() {
		return is_treatment_related;
	}


	public void setIs_treatment_related(Annotation is_treatment_related) {
		this.is_treatment_related = is_treatment_related;
	}


	public Annotation getRisk_level() {
		return risk_level;
	}

	public void setRisk_level(Annotation risk_level) {
		this.risk_level = risk_level;
	}

	public Annotation getRoute_of_administration() {
		return route_of_administration;
	}

	public void setRoute_of_administration(Annotation route_of_administration) {
		this.route_of_administration = route_of_administration;
	}

	public Annotation getDose_quantity() {
		return dose_quantity;
	}

	public void setDose_quantity(Annotation dose_quantity) {
		this.dose_quantity = dose_quantity;
	}

	public Annotation getDose_frequency() {
		return dose_frequency;
	}


	public void setDose_frequency(Annotation dose_frequency) {
		this.dose_frequency = dose_frequency;
	}

	public Annotation getDose_duration() {
		return dose_duration;
	}

	public void setDose_duration(Annotation dose_duration) {
		this.dose_duration = dose_duration;
	}

	public Annotation getDose() {
		return dose;
	}

	public void setDose(Annotation dose) {
		this.dose = dose;
	}

	/**
	 * 
	 * @return
	 */
	public List<Annotation> generateSortedAnnotations() {
		List<Annotation> all = new ArrayList<Annotation>();
		if(this.getFinding()!=null) {
			all.add(this.getFinding());
		}
		if(this.getStudy_testcd()!=null) {
			all.add(this.getStudy_testcd());
		}
//		if(this.getSpecimen()!=null) {
//			all.add(this.getSpecimen());
//		}
//		if(this.getSex()!=null) {
//			all.add(this.getSex());
//		}
//		if(this.getRisk_level()!=null) {
//			all.add(this.getRisk_level());
//		}
//		if(this.getManifestation_finding()!=null) {
//			all.add(this.getManifestation_finding());
//		}
//		if(this.getRoute_of_administration()!=null) {
//			all.add(this.getRoute_of_administration());
//		}
//		if(this.getGroup()!=null) {
//			all.add(this.getGroup());
//		}
//		if(this.getDose_duration()!=null) {
//			all.add(this.getDose_duration());
//		}
//		if(this.getDose_quantity()!=null) {
//			all.add(this.getDose_quantity());
//		}
//		if(this.getDose_frequency()!=null) {
//			all.add(this.getDose_frequency());
//		}
//		if(this.getStudy_testcd()!=null) {
//			all.add(this.getStudy_testcd());
//		}
		return all;
	}
	
	
	
}
