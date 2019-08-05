package es.bsc.inb.ades.rest.api.util;

import es.bsc.inb.ades.rest.api.model.Annotation;
import es.bsc.inb.ades.rest.api.model.Feature;

public class VisualizationHTMLUtil {

	public static String getFeature(Feature feature) {
		return "<p>" + feature.getName() + ":" + feature.getValue() + "</p>";
	}

	public static void generateHTMLSOURCES(Annotation annotation, StringBuilder html) {
		String sources = annotation.getFeature("SOURCES");
		if(sources!=null) {
			html.append("<div class=\"sourcesClass\"><p> SOURCES: " + sources + "</p></div>");
		}else {
			html.append("<div class=\"sourcesClass\"><p> SOURCES: EMPTY FIX THIS </p></div>");
		}
	}
	
	public static void generateHTMLLABEL(Annotation annotation, StringBuilder html) {
		String label = annotation.getFeature("ANNOTATION_TYPE");
		if(label!=null) {
			html.append("<div class=\"annotationTypeClass\"><p>  " + label + "</p></div>");
		}else {
			html.append("<div class=\"annotationTypeClass\"><p> NO TIENE ANNOTATION LABEL </p></div>");
		}
	}
	
	public static void generateHTMLCDISC(Annotation annotation, StringBuilder html) {
		String oid = annotation.getFeature("CDISC_OID");
		String send_code = annotation.getFeature("CDISC_SEND_CODE");
		String codelist = annotation.getFeature("CDISC_CODELIST");
		String codelist_id = annotation.getFeature("CDISC_CODELIST_ID");
		String codelist_desc = annotation.getFeature("CDISC_CODELIST_NAME");
		String codelist_link = annotation.getFeature("CDISC_CODELIST_LINK");
		String ktype = annotation.getFeature("CDISC_KEYWORD_TYPE");
		String code_id = annotation.getFeature("CDISC_EXT_CODE_ID");
		html.append("<div class=\"cdiscClass\">");
		html.append("<table><tr><td>CDISC</td><td><table>");
		if(oid!=null) {
			html.append("<tr><td> SEND_CODE_LIST: " + oid + "</td></tr>");
		}
		if(codelist!=null) {
			html.append("<tr><td> CODELIST: " + codelist + "</td></tr>");
		}
		if(codelist_id!=null) {
			html.append("<tr><td> CODELIST_ID: " + codelist_id + "</td></tr>");
		}
		if(codelist_desc!=null) {
			html.append("<tr><td> CODELIST_NAME: " + codelist_desc + "</td></tr>");
		}
		if(codelist_link!=null) {
			html.append("<tr><td> <a target=\"_blank\" href=\"https://evs.nci.nih.gov/ftp1/CDISC/SEND/SEND%20Terminology.html#"+codelist_link+"\">LINK CODE_LIST</td></tr>");
		}
		if(send_code!=null) {
			html.append("<tr><td> SEND_CODE: " + send_code + "</td></tr>");
		}
		if(code_id!=null) {
			html.append("<tr><td> CODE_ID: " + code_id + "</td></tr>");
		}
		if(ktype!=null) {
			html.append("<tr><td> TYPE : " + ktype + "</td></tr>");
		}
		html.append(" </table></td>");
		html.append("</tr></table>");
		html.append("</div>");
	}
	
	public static void generateHTMLETOX(Annotation annotation, StringBuilder html) {
		String id = annotation.getFeature("ETOX_TERM_ID");
		String send_code = annotation.getFeature("ETOX_SEND_DOMAIN_CODE");
		String send_code_desc = annotation.getFeature("ETOX_SEND_DOMAIN_DESC");
		String ktype = annotation.getFeature("ETOX_KEYWORD_TYPE");
		
		String code_list = annotation.getFeature("ETOX_CODELIST");
		String code_list_id = annotation.getFeature("ETOX_CODELIST_ID");
		
		String send_code_bis = annotation.getFeature("ETOX_SEND_CODE");
		String send_code_id = annotation.getFeature("ETOX_SEND_CODE_ID");
		
		String link = "http://www.etoxproject.eu/project.html";
		
		html.append("<div class=\"etoxClass\">");
		html.append("<table><tr><td>ETOX</td><td><table>");
		if(id!=null) {
			html.append("<tr><td> ETOX_ID: " + id + "</td></tr>");
		}
		
		if(link!=null) {
			html.append("<tr><td> <a target=\"_blank\" href=\""+link+"\">LINK TO TERM</a></td></tr>");
		}
		
		if(code_list!=null) {
			html.append("<tr><td> ETOX_CODELIST: " + code_list + "</td></tr>");
		}
		if(code_list_id!=null) {
			html.append("<tr><td> ETOX_CODELIST_ID: " + code_list_id + "</td></tr>");
		}
		if(send_code!=null) {
			html.append("<tr><td> ETOX_SEND_CODE: " + send_code + "</td></tr>");
		}
		if(send_code_desc!=null) {
			html.append("<tr><td> ETOX_SEND_CODE_DESC: " + send_code_desc + "</td></tr>");
		}
		if(send_code_bis!=null) {
			html.append("<tr><td> ETOX_SEND_CODE: " + send_code_bis + "</td></tr>");
		}
		if(send_code_id!=null) {
			html.append("<tr><td> ETOX_SEND_CODE_ID: " + send_code_id + "</td></tr>");
		}
		if(ktype!=null) {
			html.append("<tr><td> TYPE: " + ktype + "</td></tr>");
		}
		html.append(" </table></td>");
		html.append("</tr></table>");
		html.append("</div>");
	}
	
	public static void generateHTMLUMLS(Annotation annotation, StringBuilder html) {
		String source_code = annotation.getFeature("UMLS_SOURCE_CODE");
		String sem_type_str = annotation.getFeature("UMLS_SEM_TYPE_STR");
		String cui = annotation.getFeature("UMLS_CUI");
		String source = annotation.getFeature("UMLS_SOURCE");
		String sem_type = annotation.getFeature("UMLS_SEM_TYPE");
		html.append("<div class=\"umlsClass\">");
		html.append("<table><tr><td>UMLS</td><td><table>");
		if(cui!=null) {
			html.append("<tr><td> UMLS_CUI: " + cui + "</td></tr>");
		}
		if(source!=null) {
			html.append("<tr><td> UMLS_SOURCE: " + source + "</td></tr>");
		}
		if(source_code!=null) {
			html.append("<tr><td> UMLS_SOURCE_CODE: " + source_code + "</td></tr>");
		}
		if(sem_type!=null) {
			html.append("<tr><td> TYPE CODE: " + sem_type + "</td></tr>");
		}
		if(sem_type_str!=null) {
			html.append("<tr><td> TYPE : " + sem_type_str + "</td></tr>");
		}
		html.append(" </table></td>");
		html.append("</tr></table>");
		html.append("</div>");
	}
	
	public static void generateHTMLDNORM(Annotation annotation, StringBuilder html) {
		String dnorm_mesh = annotation.getFeature("DNORM_MESH");
		String dnorm_original_label = annotation.getFeature("DNORM_ORIGINAL_LABEL");
		html.append("<div class=\"dnormClass\">");
		html.append("<table><tr><td>DNORM</td><td><table>");
		if(dnorm_original_label!=null) {
			html.append("<tr><td> DNORM_LABEL: " + dnorm_original_label + "</td></tr>");
		}
		if(dnorm_mesh!=null) {//https://www.ncbi.nlm.nih.gov/mesh/D006965
			html.append("<tr><td> DNORM_MESH: <a target=\"_blank\" href=\"https://www.ncbi.nlm.nih.gov/mesh/"+ dnorm_mesh.replace("MESH:","") +"\">"+dnorm_mesh.replace("MESH:","")+"</a></td></tr>");
		}
		html.append(" </table></td>");
		html.append("</tr></table>");
		html.append("</div>");
	}
	
	public static void generateHTMLLIMTOX(Annotation annotation, StringBuilder html) {
		String limtox = annotation.getFeature("LIMTOX_HEPATOTOXICITY");
		html.append("<div class=\"limtoxClass\">");
		if(limtox!=null) {
			html.append("<p> LIMTOX_HEPATOTOXICITY:" + limtox + "</p>");
		}
		html.append("</div>");
	}
	
	public static void generateHTMLMYONTOLOGY(Annotation annotation, StringBuilder html) {
		String code = annotation.getFeature("MANUAL_CODE");
		String send_code = annotation.getFeature("MANUAL_SEND_CODE");
		html.append("<div class=\"myontologyClass\">");
		html.append("<table><tr><td>MY ONTOLOGY</td><td><table>");
		if(code!=null) {
			html.append("<tr><td> MANUAL_CODE: " + code + "</td></tr>");
		}
		if(send_code!=null) {
			html.append("<tr><td> MANUAL_SEND_CODE: " + send_code + "</td></tr>");
		}
		html.append(" </table></td>");
		html.append("</tr></table>");
		html.append("</div>");
	}
	
}
