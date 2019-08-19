package es.bsc.inb.ades.rest.api.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Field;

/**
 * 
 * @author jcorvi
 *
 */
public class Finding2 {
	@Field("id")
	private Integer findingId;
	
	private List<Annotation> finding_fields;

	@Field("FINDING")
	private List<Annotation> findings;
	
	@Field("SPECIMEN")
	private List<Annotation> specimens;
	
	@Field("SEX")
	private List<Annotation> sexs;
	
	@Field("MANIFESTATION_FINDING")
	private List<Annotation> manifestations_finding;

	@Field("GROUP")
	private List<Annotation> groups;
	
	@Field("STUDY_TESTCD")
	private List<Annotation> study_testcds;
	
	@Field("STUDY_DOMAIN")
	private List<Annotation> study_domains;
	
	@Field("TREATMENT_RELATED_TRIGGER")
	private List<Annotation> treatment_related_triggers;
	
	@Field("NO_TREATMENT_RELATED_TRIGGER")
	private List<Annotation> no_treatment_related_triggers;
	
	@Field("RISK_LEVEL")
	private List<Annotation> risk_levels;
	
	@Field("ROUTE_OF_AMINISTRATION")
	private List<Annotation> routes_of_administration;
	
	@Field("DOSE_QUANTITY")
	private List<Annotation> dose_quantitys;
	
	@Field("DOSE_FREQUENCY")
	private List<Annotation> dose_frequencys;
	
	@Field("DOSE_DURATION")
	private List<Annotation> dose_durations;
	
	public Finding2() {}
	
	public Integer getFindingId() {
		return findingId;
	}

	public void setFindingId(Integer findingId) {
		this.findingId = findingId;
	}

	public List<Annotation> getFinding_fields() {
		return finding_fields;
	}

	public void setFinding_fields(List<Annotation> finding_fields) {
		this.finding_fields = finding_fields;
	}

	public List<Annotation> getFindings() {
		return findings;
	}

	public void setFindings(List<Annotation> findings) {
		this.findings = findings;
	}

	public List<Annotation> getSpecimens() {
		return specimens;
	}

	public void setSpecimens(List<Annotation> specimens) {
		this.specimens = specimens;
	}

	public List<Annotation> getSexs() {
		return sexs;
	}

	public void setSexs(List<Annotation> sexs) {
		this.sexs = sexs;
	}

	public List<Annotation> getManifestations_finding() {
		return manifestations_finding;
	}

	public void setManifestations_finding(List<Annotation> manifestations_finding) {
		this.manifestations_finding = manifestations_finding;
	}

	public List<Annotation> getGroups() {
		return groups;
	}

	public void setGroups(List<Annotation> groups) {
		this.groups = groups;
	}

	public List<Annotation> getStudy_testcds() {
		return study_testcds;
	}

	public void setStudy_testcds(List<Annotation> study_testcds) {
		this.study_testcds = study_testcds;
	}

	public List<Annotation> getStudy_domains() {
		return study_domains;
	}

	public void setStudy_domains(List<Annotation> study_domains) {
		this.study_domains = study_domains;
	}

	public List<Annotation> getTreatment_related_triggers() {
		return treatment_related_triggers;
	}

	public void setTreatment_related_triggers(List<Annotation> treatment_related_triggers) {
		this.treatment_related_triggers = treatment_related_triggers;
	}

	public List<Annotation> getNo_treatment_related_triggers() {
		return no_treatment_related_triggers;
	}

	public void setNo_treatment_related_triggers(List<Annotation> no_treatment_related_triggers) {
		this.no_treatment_related_triggers = no_treatment_related_triggers;
	}

	public List<Annotation> getRisk_levels() {
		return risk_levels;
	}

	public void setRisk_levels(List<Annotation> risk_levels) {
		this.risk_levels = risk_levels;
	}

	public List<Annotation> getRoutes_of_administration() {
		return routes_of_administration;
	}

	public void setRoutes_of_administration(List<Annotation> routes_of_administration) {
		this.routes_of_administration = routes_of_administration;
	}

	public List<Annotation> getDose_quantitys() {
		return dose_quantitys;
	}

	public void setDose_quantitys(List<Annotation> dose_quantitys) {
		this.dose_quantitys = dose_quantitys;
	}

	public List<Annotation> getDose_frequencys() {
		return dose_frequencys;
	}

	public void setDose_frequencys(List<Annotation> dose_frequencys) {
		this.dose_frequencys = dose_frequencys;
	}

	public List<Annotation> getDose_durations() {
		return dose_durations;
	}

	public void setDose_durations(List<Annotation> dose_durations) {
		this.dose_durations = dose_durations;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public List<Annotation> generateSortedAnnotations() {
		List<Annotation> all = new ArrayList<Annotation>();
		all.addAll(this.getFindings());
		if(this.getSpecimens()!=null) {
			all.addAll(this.getSpecimens());
		}
		if(this.getSexs()!=null) {
			all.addAll(this.getSexs());
		}
		if(this.getRisk_levels()!=null) {
			all.addAll(this.getRisk_levels());
		}
		if(this.getManifestations_finding()!=null) {
			all.addAll(this.getManifestations_finding());
		}
		if(this.getRoutes_of_administration()!=null) {
			all.addAll(this.getRoutes_of_administration());
		}
		if(this.getGroups()!=null) {
			all.addAll(this.getGroups());
		}
		if(this.getDose_durations()!=null) {
			all.addAll(this.getDose_durations());
		}
		if(this.getDose_quantitys()!=null) {
			all.addAll(this.getDose_quantitys());
		}
		if(this.getDose_frequencys()!=null) {
			all.addAll(this.getDose_frequencys());
		}
		if(this.getTreatment_related_triggers()!=null) {
			all.addAll(this.getTreatment_related_triggers());
		}
		if(this.getStudy_testcds()!=null) {
			all.addAll(this.getStudy_testcds());
		}
		
		
//		if(this.getStudy_domains()!=null) {
//			//all.addAll(this.getStudy_domains());
//		}




		
		
		return all;
	}
	
	
	
}
