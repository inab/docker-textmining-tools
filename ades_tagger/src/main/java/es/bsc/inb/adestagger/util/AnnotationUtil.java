package es.bsc.inb.adestagger.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.stanford.nlp.util.Generics;
import es.bsc.inb.adestagger.model.Domain;


public class AnnotationUtil {
	
	//SOURCES
	//SUFFIX
	public static final String SOURCE_ETOX_SUFFIX = "_ETOX_SOURCE";
	public static final String SOURCE_MANUAL_SUFFIX = "_MANUAL_SOURCE";
	public static final String SOURCE_CDISC_SUFFIX = "_CDISC_SOURCE";
	
	
	
	
	//SOURCE
	public static final String SOURCE_ETOX = "ETOX";
	public static final String SOURCE_MANUAL = "MANUAL";
	public static final String SOURCE_CDISC = "CDISC";
	public static final String STANDFORD_CORE_NLP_SOURCE = "STANDFORD_CORE_NLP";
	
	
	//GENERAL DATA
	public static final String SENTENCE_SUFFIX = "_SENTENCE";
	
	public static final String SENTENCES = "SENTENCES_QUANTITY";
	public static final String TOKENS = "TOKENS_QUANTITY";
	public static final String SENTENCES_TEXT = "SENTENCES_TEXT";
	
	//FEATURE LABEL
	public static final String FEATURE_SUFFIX = "_FEATURE_";
	
	//ANNOTATIONS
	public static final String TREATMENT_RELATED_EFFECT_DETECTED = "TREATMENT_RELATED_EFFECT_DETECTED";
	public static final String NO_TREATMENT_RELATED_EFFECT_DETECTED = "NO_TREATMENT_RELATED_EFFECT_DETECTED";
	public static final String STATISTICAL_SIGNIFICANCE = "STATISTICAL_SIGNIFICANCE";
	public static final String SEVERITY_FINDING = "SEVERITY_FINDING";
	public static final String DOSE = "DOSE";
	public static final String MANIFESTATION_OF_FINDING_SUFFIX = "_MANIFESTATION_FINDING";
	public static final String MANIFESTATION_OF_FINDING = "MANIFESTATION_FINDING";
	public static final String ROUTE_OF_ADMINISTRATION = "ROUTE_OF_ADMINISTRATION";
	public static final String GROUP = "GROUP";
	public static final String STUDY_DOMAIN_SUFFIX = "_DOMAIN";
	public static final String STUDY_DOMAIN_TESTCD_SUFFIX = " TEST CODE";
	public static final String STUDY_DOMAIN_TESTCD = "STUDY_TESTCD";
	public static final String SEX = "SEX";
	public static final String STUDY_DOMAIN = "STUDY_DOMAIN";
	public static final String ANATOMY = "ANATOMY";
	public static final String SPECIES = "SPECIES";
	public static final String SPECIMEN = "SPECIMEN";
	public static final String RISK_LEVEL = "RISK_LEVEL";
	public static final String HEAD_SECTION = "head";
	public static final String ORIGINAL_MARKUPS = "Original markups";
	//TEST NAME BY DOMAIN
	public static Map<String, String> SEND_DOMAIN_TO_DEFAULT_TESTCD = new HashMap<String, String>(){{
	    //DOMAIN THAT HAS TO BE CONSIDERED IN ETRANSAFE
		put("BODY_WEIGHT_DOMAIN", "BWTESTCD");
	    put("BODY_WEIGHT_GAIN_DOMAIN", "BGTESTCD");
	    put("CLINICAL_DOMAIN", "NOTEST");
	    put("DEATH_DIAGNOSIS_DOMAIN", "DDTESTCD");
	    put("FOOD_WATER_CONSUMPTION_DOMAIN", "FWTESTCD");
	    put("LABORATORY_FINDINGS_DOMAIN", "LBTESTCD");
	    put("MACROSCOPIC_FINDINGS_DOMAIN", "MATESTCD");
	    put("MICROSCOPIC_FINDINGS_DOMAIN", "MITESTCD");
	    put("ORGAN_MEASUREMENT_DOMAIN", "OMTESTCD");
	    put("PHARMACOKINETICS_PARAMETERS_DOMAIN", "PKPARMCD");
	    put("TUMOR_FINDINGS_DOMAIN", "TFTESTCD");
	    put("VITAL_SIGNS_DOMAIN", "VSTESTCD");
	    put("ECG_DOMAIN", "EGTESTCD");
	    put("CARDIOVASCULAR_DOMAIN", "SCVTSTCD");
	    put("RESPIRATORY_FINDINGS_DOMAIN", "SRETSTCD");
	    
	    //OTHER DOMAINS
	    put("BEHAVIORAL_DOMAIN", "NOTEST");
	    put("COMMENTS_DOMAIN", "NOTEST");
	    put("DEMOGRAPHICS_DOMAIN", "NOTEST");
	    put("DISPOSITION_DOMAIN", "NOTEST");
	    put("EXPOSURE_DOMAIN", "NOTEST");
	    put("FERTILITY_DOMAIN", "NOTEST");
	    put("FETAL_DOMAIN", "FMTESTCD");
	    put("FETAL_PATOLOGY_FINDINGS_DOMAIN", "FXTESTCD");
	    put("IMPLANTATION_CLASSIFICATION_DOMAIN", "ICTESTCD");
	    put("CESARIAN_SECTION_DELIVERY_LITTER_DOMAIN", "NOTEST");
	    put("NERVOUS_SYSTEM_DOMAIN", "NOTEST");
	    put("PARING_EVENTS_DOMAIN", "NOTEST");
	    put("PHARMACOKINETIC_CONCENTRATION_DOMAIN", "NOTEST");
	    put("PALPABLE_MASSES_DOMAIN", "PALPABLE_MASSES_DOMAIN");
	    put("NONCLINICAL_PREGNANCY_DOMAIN", "PYTESTCD");
	    put("SUBJECT_CHARACTERISTICS_DOMAIN", "SBCCDSND");
	    put("SUBJECT_ELEMENTS_DOMAIN", "NOTEST");
	    put("SUBJECT_STAGES_DOMAIN", "NOTEST");
	    put("TRIAL_ARMS_DOMAIN", "NOTEST");
	    put("TRIAL_ELEMENTS_DOMAIN", "NOTEST");
	    put("TRIAL_PATHS_DOMAIN", "NOTEST");
	    put("TRIAL_SUMMARY_DOMAIN", "STSPRMCD");
	    put("TRIAL_STAGES_DOMAIN", "NOTEST");
	    put("TRIAL_SETS_DOMAIN", "NOTEST");
	 }};
	
	//TEST NAME BY DOMAIN
		public static Map<String, String> SEND_DOMAIN_TO_DEFAULT_TESTCDVALUE = new HashMap<String, String>(){{
		    put("BODY_WEIGHT_DOMAIN", "BW(DEFAULT TEST)");
		    put("BODY_WEIGHT_GAIN_DOMAIN", "BWGAIN(DEFAULT TEST)");
		    
		    put("BEHAVIORAL_DOMAIN", "NOTEST");
		    put("CLINICAL_DOMAIN", "NOTEST");
		    put("COMMENTS_DOMAIN", "NOTEST");
		    put("CARDIOVASCULAR_DOMAIN", "SCVTSTCD(DEFAULT TEST)");
		    put("DEATH_DIAGNOSIS_DOMAIN", "DEATHD(DEFAULT TEST)");
		    put("DEMOGRAPHICS_DOMAIN", "NOTEST");
		    put("DISPOSITION_DOMAIN", "NOTEST");
		    put("ECG_DOMAIN", "ECG(DEFAULT TEST)");
		    put("EXPOSURE_DOMAIN", "NOTEST");
		    put("FERTILITY_DOMAIN", "NOTEST");
		    put("FETAL_DOMAIN", "FMTESTCD(DEFAULT TEST)");
		    put("FOOD_WATER_CONSUMPTION_DOMAIN", "FWTESTCD(DEFAULT TEST)");
		    put("FETAL_PATOLOGY_FINDINGS_DOMAIN", "FXTESTCD(DEFAULT TEST)");
		    put("IMPLANTATION_CLASSIFICATION_DOMAIN", "IMPSCHCTD(DEFAULT TEST)");
		    put("LABORATORY_FINDINGS_DOMAIN", "LBTESTCD(DEFAULT TEST)");
		    put("CESARIAN_SECTION_DELIVERY_LITTER_DOMAIN", "NOTEST");
		    put("MACROSCOPIC_FINDINGS_DOMAIN", "CLSFUP(DEFAULT TEST)");
		    put("MICROSCOPIC_FINDINGS_DOMAIN", "GHISTXQL(DEFAULT TEST)");
		    put("NERVOUS_SYSTEM_DOMAIN", "NOTEST");
		    put("ORGAN_MEASUREMENT_DOMAIN", "OMTESTCD(DEFAULT TEST)");
		    put("PARING_EVENTS_DOMAIN", "NOTEST");
		    put("PHARMACOKINETIC_CONCENTRATION_DOMAIN", "NOTEST");
		    put("PALPABLE_MASSES_DOMAIN", "NOTEST");
		    put("PHARMACOKINETICS_PARAMETERS_DOMAIN", "NOTEST");
		    put("NONCLINICAL_PREGNANCY_DOMAIN", "PYTESTCD(DEFAULT TEST)");
		    put("RESPIRATORY_FINDINGS_DOMAIN", "SRETSTCD(DEFAULT TEST)");
		    put("SUBJECT_CHARACTERISTICS_DOMAIN", "SBCCDSND(DEFAULT TEST)");
		    put("SUBJECT_ELEMENTS_DOMAIN", "NOTEST");
		    put("SUBJECT_STAGES_DOMAIN", "NOTEST");
		    put("TRIAL_ARMS_DOMAIN", "NOTEST");
		    put("TRIAL_ELEMENTS_DOMAIN", "NOTEST");
		    put("TUMOR_FINDINGS_DOMAIN", "TUMEX(DEFAULT TEST)");
		    put("TRIAL_PATHS_DOMAIN", "NOTEST");
		    put("TRIAL_SUMMARY_DOMAIN", "STSPRMCD(DEFAULT TEST)");
		    put("TRIAL_STAGES_DOMAIN", "NOTEST");
		    put("TRIAL_SETS_DOMAIN", "NOTEST");
		    put("VITAL_SIGNS_DOMAIN", "VSTESTCD(DEFAULT TEST)");
		}};
	
	//TEST DEFAULT BY DOMAIN
	public static	Map<String, Domain> SEND_DOMAIN_DESC_TO_SEND_DOMAIN_CODE = new HashMap<String, Domain>(){{
		    put("BODY_WEIGHT_DOMAIN", Domain.BW);
		    put("BEHAVIORAL_DOMAIN", Domain.BEHAVIORAL_DOMAIN);
		    put("BODY_WEIGHT_GAIN_DOMAIN", Domain.BG);
		    put("CLINICAL_DOMAIN", Domain.CL);
		    put("COMMENTS_DOMAIN", Domain.COMMENTS_DOMAIN);
		    put("CARDIOVASCULAR_DOMAIN", Domain.CV);
		    put("DEATH_DIAGNOSIS_DOMAIN", Domain.DD);
		    put("DEMOGRAPHICS_DOMAIN", Domain.DEMOGRAPHICS_DOMAIN);
		    put("DISPOSITION_DOMAIN", Domain.DISPOSITION_DOMAIN);
		    put("ECG_DOMAIN", Domain.EG);
		    put("EXPOSURE_DOMAIN", Domain.EXPOSURE_DOMAIN);
		    put("FERTILITY_DOMAIN", Domain.FERTILITY_DOMAIN);
		    put("BODY_WEIGHT_DOMAIN", Domain.BODY_WEIGHT_DOMAIN);
		    put("FETAL_DOMAIN", Domain.FM);
		    put("FOOD_WATER_CONSUMPTION_DOMAIN", Domain.FW);
		    put("FETAL_PATOLOGY_FINDINGS_DOMAIN", Domain.FX);
		    put("IMPLANTATION_CLASSIFICATION_DOMAIN", Domain.IC);
		    put("LABORATORY_FINDINGS_DOMAIN", Domain.LB);
		    put("CESARIAN_SECTION_DELIVERY_LITTER_DOMAIN", Domain.CESARIAN_SECTION_DELIVERY_LITTER_DOMAIN);
		    put("MACROSCOPIC_FINDINGS_DOMAIN", Domain.MA);
		    put("MICROSCOPIC_FINDINGS_DOMAIN", Domain.MI);
		    put("NERVOUS_SYSTEM_DOMAIN", Domain.NERVOUS_SYSTEM_DOMAIN);
		    put("ORGAN_MEASUREMENT_DOMAIN", Domain.OM);
		    put("PARING_EVENTS_DOMAIN", Domain.PARING_EVENTS_DOMAIN);
		    put("PHARMACOKINETIC_CONCENTRATION_DOMAIN", Domain.PHARMACOKINETIC_CONCENTRATION_DOMAIN);
		    put("PALPABLE_MASSES_DOMAIN", Domain.PALPABLE_MASSES_DOMAIN);
		    put("PHARMACOKINETICS_PARAMETERS_DOMAIN", Domain.PHARMACOKINETICS_PARAMETERS_DOMAIN);
		    put("NONCLINICAL_PREGNANCY_DOMAIN", Domain.NONCLINICAL_PREGNANCY_DOMAIN);
		    put("RESPIRATORY_FINDINGS_DOMAIN", Domain.RE);
		    put("SUBJECT_CHARACTERISTICS_DOMAIN", Domain.SC);
		    put("SUBJECT_ELEMENTS_DOMAIN", Domain.SUBJECT_ELEMENTS_DOMAIN);
		    put("SUBJECT_STAGES_DOMAIN", Domain.SUBJECT_STAGES_DOMAIN);
		    put("TRIAL_ARMS_DOMAIN", Domain.TRIAL_ARMS_DOMAIN);
		    put("TRIAL_ELEMENTS_DOMAIN", Domain.TRIAL_ELEMENTS_DOMAIN);
		    put("TUMOR_FINDINGS_DOMAIN", Domain.TF);
		    put("TRIAL_PATHS_DOMAIN", Domain.TRIAL_PATHS_DOMAIN);
		    put("TRIAL_SUMMARY_DOMAIN", Domain.TRIAL_SUMMARY_DOMAIN);
		    put("TRIAL_STAGES_DOMAIN", Domain.TRIAL_STAGES_DOMAIN);
		    put("TRIAL_SETS_DOMAIN", Domain.TRIAL_SETS_DOMAIN);
		    put("VITAL_SIGNS_DOMAIN", Domain.VS);
		}};
	
		
	public static final Set<String> entityMentionsToDelete = Generics.newHashSet(Arrays.asList(new String[]{"DOSE_UNIT","NUMBER", "MONEY", "DATE","TIME","TITLE","CAUSE_OF_DEATH","PERSON", 
			 "TREATMENT_RELATED_EFFECT_DETECTED","NO_TREATMENT_RELATED_EFFECT_DETECTED", "INCREASE_MANIFESTATION_FINDING", "DECREASE_MANIFESTATION_FINDING","JUSTPRESENT_MANIFESTATION_FINDING"}));
	

	
}