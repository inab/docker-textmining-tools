package es.bsc.inb.evaluation.ner.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.util.AnnotationDiffer;
import gate.util.ClassificationMeasures;

/**
 * Support class for document set evaluation
 * 
 * https://gate.ac.uk/sale/tao/splitch10.html
 * 
 * @author Francesco Ronzano
 *
 */
public class AnnotationEvaluator {

	private static Random rnd = new Random();

	private String goldStandardAnnotationSetName = null;
	private String generatedAnnotationSetName = null;

	private String classificationString = null;

	private int measuresType = 0; // 0: measuresDiff / 1: measuresClassification

	private ArrayList<String> documentNames = new ArrayList<String>();

	// "Observed agreement" "Cohen's Kappa" "Pi's Kappa"
	private static final Object[] measuresDiff = new Object[]{"f1-strict", "f1-lenient", "f1-average"};
	private ArrayList<AnnotationDiffer> documentDiffer = new ArrayList<AnnotationDiffer>();
	private ArrayList<Map<String, AnnotationDiffer>> documentDifferByType = new ArrayList<Map<String, AnnotationDiffer>>();
	
	// "Observed agreement" "Cohen's Kappa" "Pi's Kappa"
	static final Object[] measuresClassification = new Object[]{"Observed agreement", "Cohen's Kappa", "Pi's Kappa"};
	private ArrayList<ClassificationMeasures> classificationMeasures = new ArrayList<ClassificationMeasures>();
	private ArrayList<Map<String, ClassificationMeasures>> classificationMeasuresByType = new ArrayList<Map<String, ClassificationMeasures>>();
	private ClassificationMeasures multiDocClassificationMeasures = new ClassificationMeasures();
	private Map<String, ClassificationMeasures> multiDocClassificationMeasuresByType = new HashMap<String, ClassificationMeasures>();
	
	/**
	 * Build a document set evaluator
	 * 
	 * @param goldStandardAnnotationSetName
	 * @param generatedAnnotationSetName
	 * @param classificationString
	 * @param measuresType
	 */
	public AnnotationEvaluator(String goldStandardAnnotationSetName, String generatedAnnotationSetName,
			String classificationString, int measuresType) {
		super();
		this.goldStandardAnnotationSetName = goldStandardAnnotationSetName;
		this.generatedAnnotationSetName = generatedAnnotationSetName;
		this.classificationString = classificationString;
		this.measuresType = measuresType;
	}

	/**
	 * Add a document to the measure set
	 * 
	 * @param gateDoc
	 */
	public void processDocument(Document gateDoc) {
		if(gateDoc != null) {
			documentNames.add((gateDoc.getName() != null && gateDoc.getName().trim().length() > 0) ? gateDoc.getName() : "DOC_" + rnd.nextInt(1000000)); 
			Set<Annotation> goldStandardAnnotationSet = new HashSet<Annotation>(); 
			Set<Annotation> generatedAnnotationSet = new HashSet<Annotation>(); 

			// Get annotations from gold standard and generated annotationsets 
			goldStandardAnnotationSet = gateDoc.getAnnotations(goldStandardAnnotationSetName); 
			generatedAnnotationSet = gateDoc.getAnnotations(generatedAnnotationSetName); 

			if (measuresType == 0) {
				// Fscore document table 
				HashMap<String, AnnotationDiffer> differsByTypeInDocument = new HashMap<String, AnnotationDiffer>(); 
				AnnotationDiffer differOfAType; 
				Set<Annotation> keysIter = new HashSet<Annotation>(); 
				Set<Annotation> responsesIter = new HashSet<Annotation>(); 
				Set<String> goldStandatdAnnTypes = gateDoc.getAnnotations(goldStandardAnnotationSetName).getAllTypes();
				for (String goldStandardAnnotationType : goldStandatdAnnTypes) { 
					if (!goldStandardAnnotationSet.isEmpty() && !goldStandatdAnnTypes.isEmpty()) { 
						keysIter = ((AnnotationSet) goldStandardAnnotationSet).get(goldStandardAnnotationType); 
					} 
					if (!generatedAnnotationSet.isEmpty() && !goldStandatdAnnTypes.isEmpty()) { 
						responsesIter = ((AnnotationSet) generatedAnnotationSet).get(goldStandardAnnotationType); 
					} 
					differOfAType = new AnnotationDiffer(); 
					differOfAType.setSignificantFeaturesSet(new HashSet<String>()); // No feature level comparison is enabled
					differOfAType.calculateDiff(keysIter, responsesIter); // compare 
					differsByTypeInDocument.put(goldStandardAnnotationType, differOfAType); 
				} 

				documentDifferByType.add(differsByTypeInDocument);

				AnnotationDiffer differOfWholeDoc = new AnnotationDiffer(differsByTypeInDocument.values()); 
				documentDiffer.add(differOfWholeDoc);

			} else if (measuresType == 1 && classificationString != null && !classificationString.trim().equals("") && 
					!goldStandardAnnotationSet.isEmpty() && !generatedAnnotationSet.isEmpty()) {
				// Classification document table for each type with the specified feature

				HashMap<String, ClassificationMeasures> classificationMeasuresByTypeInDocument = new HashMap<String, ClassificationMeasures>(); 

				Set<String> goldStandatdAnnTypes = gateDoc.getAnnotations(goldStandardAnnotationSetName).getAllTypes();
				for (String goldStandardAnnotationType : goldStandatdAnnTypes) { 

					ClassificationMeasures classificationMeasures = new ClassificationMeasures(); 
					classificationMeasures.calculateConfusionMatrix((AnnotationSet) goldStandardAnnotationSet, (AnnotationSet) generatedAnnotationSet, 
							goldStandardAnnotationType, classificationString, false);

					classificationMeasuresByTypeInDocument.put(goldStandardAnnotationType, classificationMeasures);
				} 

				classificationMeasuresByType.add(classificationMeasuresByTypeInDocument);
			}
		}
	}

	/**
	 * Retrieve annotation diff results
	 * 
	 * @param printByDocument
	 * @return
	 */
	public String getFscoreMeasures(boolean printByDocument) {
		StringBuffer retStr = new StringBuffer("");
		
		List<String> headerStrList = new ArrayList<String>();
		headerStrList.add("DOC NAME");
		headerStrList.add("CorrectMatches");
		headerStrList.add("Missing");
		headerStrList.add("Spurious");
		headerStrList.add("PartiallyCorrectMatches");
		headerStrList.add("Precision - strict");
		headerStrList.add("Recall - strict");
		headerStrList.add("Fmesure - strict");
		headerStrList.add("Precision - lanient");
		headerStrList.add("Recall - lanient");
		headerStrList.add("Fmesure - lanient");
		headerStrList.add("Precision - average");
		headerStrList.add("Recall - average");
		headerStrList.add("Fmesure - average");
		
		if(printByDocument) {
			for(int i = 0; i < documentNames.size(); i++) {
				List<String> measuresRow = documentDiffer.get(i).getMeasuresRow(measuresDiff, documentNames.get(i));
				retStr.append("\n" + formattedStr(measuresRow.toArray()) + "\n");
				retStr.append("\n\n" + formattedStr(headerStrList.toArray()) + "\n");
				
				for(Entry<String, AnnotationDiffer> documentDifferByTypeElem : documentDifferByType.get(i).entrySet()) {
					retStr.append("   > " + documentDifferByTypeElem.getKey() + "\n" + formattedStr(documentDifferByTypeElem.getValue().getMeasuresRow(measuresDiff, documentNames.get(i)).toArray()) + "\n");
				}
			}
		}

		retStr.append("\n\n GLOBAL DIFF: \n");
		retStr.append("\n\n" + formattedStr(headerStrList.toArray()) + "\n");
		
		AnnotationDiffer globalDiff = new AnnotationDiffer(documentDiffer);
		List<String> measuresRow = globalDiff.getMeasuresRow(measuresDiff, "GLOBAL");
		retStr.append("\n" + formattedStr(measuresRow.toArray()) + "\n");
		
		Map<String, List<AnnotationDiffer>> multiDocDifferByType = new HashMap<String, List<AnnotationDiffer>>();
		for(int i = 0; i < documentNames.size(); i++) {
			for(Entry<String, AnnotationDiffer> documentDifferByTypeElem : documentDifferByType.get(i).entrySet()) {
				if(!multiDocDifferByType.containsKey(documentDifferByTypeElem.getKey())) multiDocDifferByType.put(documentDifferByTypeElem.getKey(), new ArrayList<AnnotationDiffer>());
				multiDocDifferByType.get(documentDifferByTypeElem.getKey()).add(documentDifferByTypeElem.getValue());
			}
		}
		
		for(Entry<String, List<AnnotationDiffer>> multiDocDifferByTypeEntry : multiDocDifferByType.entrySet()) {
			retStr.append("   > " + multiDocDifferByTypeEntry.getKey() + "\n" + formattedStr((new AnnotationDiffer(multiDocDifferByTypeEntry.getValue())).getMeasuresRow(measuresDiff, "ALL_DOCS_TOGETHER").toArray()) + "\n");
		}
		
		return retStr.toString();
	}

	/**
	 * Retrieve annotation diff results
	 * 
	 * @param printByDocument
	 * @return
	 */
	public String getFscoreMeasuresCSV(boolean printByDocument) {
		StringBuffer retStr = new StringBuffer("");
		List<String> headerStrList = new ArrayList<String>();
		headerStrList.add("DOC NAME");
		headerStrList.add("CorrectMatches");
		headerStrList.add("Missing");
		headerStrList.add("Spurious");
		headerStrList.add("PartiallyCorrectMatches");
		headerStrList.add("Precision - strict");
		headerStrList.add("Recall - strict");
		headerStrList.add("Fmesure - strict");
		headerStrList.add("Precision - lanient");
		headerStrList.add("Recall - lanient");
		headerStrList.add("Fmesure - lanient");
		headerStrList.add("Precision - average");
		headerStrList.add("Recall - average");
		headerStrList.add("Fmesure - average");
		retStr.append(String.join("\t", headerStrList) + "\n");
		if(printByDocument) {
			//retStr.append("###DOCUMENTS_INFORMATION");
			for(int i = 0; i < documentNames.size(); i++) {
				List<String> measuresRow = documentDiffer.get(i).getMeasuresRow(measuresDiff, documentNames.get(i));
				retStr.append(String.join("\t",  measuresRow)  + "\n");
				//retStr.append("\n\n" + formattedStr(headerStrList.toArray()) + "\n");
				
				for(Entry<String, AnnotationDiffer> documentDifferByTypeElem : documentDifferByType.get(i).entrySet()) {
					List<String> results = documentDifferByTypeElem.getValue().getMeasuresRow(measuresDiff, documentNames.get(i));
					retStr.append(String.join("\t",  results)  + "\n");
					//retStr.append("   > " + documentDifferByTypeElem.getKey() + "\n" + formattedStr() + "\n");
				}
			}
		}
		//retStr.append("###GLOBAL_INFORMATION\n");
		AnnotationDiffer globalDiff = new AnnotationDiffer(documentDiffer);
		List<String> measuresRow = globalDiff.getMeasuresRow(measuresDiff, "GLOBAL");
		retStr.append(String.join("\t", measuresRow)+ "\n");
		
		Map<String, List<AnnotationDiffer>> multiDocDifferByType = new HashMap<String, List<AnnotationDiffer>>();
		for(int i = 0; i < documentNames.size(); i++) {
			for(Entry<String, AnnotationDiffer> documentDifferByTypeElem : documentDifferByType.get(i).entrySet()) {
				if(!multiDocDifferByType.containsKey(documentDifferByTypeElem.getKey())) multiDocDifferByType.put(documentDifferByTypeElem.getKey(), new ArrayList<AnnotationDiffer>());
				multiDocDifferByType.get(documentDifferByTypeElem.getKey()).add(documentDifferByTypeElem.getValue());
			}
		}
		for(Entry<String, List<AnnotationDiffer>> multiDocDifferByTypeEntry : multiDocDifferByType.entrySet()) {
			List<String> result = (new AnnotationDiffer(multiDocDifferByTypeEntry.getValue()).getMeasuresRow(measuresDiff, "ALL_DOCS_TOGETHER"));
			result.remove(0);
			retStr.append(multiDocDifferByTypeEntry.getKey() + "\t" + 
					String.join("\t",  result)  + "\n");
		}
		
		return retStr.toString();
	}
	
	/**
	 * Retrieve classification measures results
	 * 
	 * @param printByDocument
	 * @return
	 */
	public String getClassificationMeasures(boolean printByDocument) {
		StringBuffer retStr = new StringBuffer("");
		if(printByDocument) {
			for(int i = 0; i < documentNames.size(); i++) {
				List<String> measuresRow = classificationMeasures.get(i).getMeasuresRow(measuresClassification, documentNames.get(i)); 
				retStr.append("\n" + Arrays.deepToString(measuresRow.toArray()) + "\n"); 
				List<List<String>> matrix = classificationMeasures.get(i).getConfusionMatrix(documentNames.get(i)); 
				for (List<String> matrixRow : matrix) { 
					retStr.append("\n" + Arrays.deepToString(matrixRow.toArray())); 
				} 
			}
		}
		retStr.append("\n\n GLOBAL CLASSIFICAITON MEASURES: \n");
		ClassificationMeasures globalClassificationMeasures = new ClassificationMeasures(classificationMeasures);
		List<String> measuresRow = globalClassificationMeasures.getMeasuresRow(measuresClassification, "GLOBAL"); 
		retStr.append("\n" + Arrays.deepToString(measuresRow.toArray()) + "\n"); 
		List<List<String>> matrix = globalClassificationMeasures.getConfusionMatrix("GLOBAL"); 
		for (List<String> matrixRow : matrix) { 
			retStr.append("\n" + Arrays.deepToString(matrixRow.toArray())); 
		} 
		return retStr.toString();
	}
	
	private static String formattedStr(Object[] objArray) {
		StringBuffer strBuffer = new StringBuffer("");
		int maxLen = 0;
		if(objArray != null && objArray.length > 1) {
			for(int k = 1; k < objArray.length; k++) {
				Object obj = objArray[k];
				if(obj != null && obj instanceof String) {
					String objStr = (String) obj;
					if(objStr.trim().length() > maxLen) maxLen = objStr.length(); 
				}
			}
		}
		if(objArray != null && objArray.length > 0) {
			for(int k = 0; k < objArray.length; k++) {
				Object obj = objArray[k];
				if(obj != null && obj instanceof String) {
					String objStr = (String) obj;
					strBuffer.append(objStr);
					if(k == 0) {
						int objStrLen = objStr.length();
						while(objStrLen < 50) {
							strBuffer.append(" ");
							objStrLen++;
						}
					}
					else {
						int objStrLen = objStr.length();
						while(objStrLen < maxLen + 1) {
							strBuffer.append(" ");
							objStrLen++;
						}
					}
				}
			}
		}
		return strBuffer.toString();
	}
	
	
	/**
	 * Reset document set
	 * 
	 */
	public void reset() {
		documentNames = new ArrayList<String>();
		documentDiffer = new ArrayList<AnnotationDiffer>();
		documentDifferByType = new ArrayList<Map<String, AnnotationDiffer>>();
		classificationMeasures = new ArrayList<ClassificationMeasures>();
		classificationMeasuresByType = new ArrayList<Map<String, ClassificationMeasures>>();
	}
	
	/**
	 * 
	 * @param printByDoc
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String getFscoreMeasuresJSON() {
		JSONObject json = new JSONObject();
		JSONArray json_fields = new JSONArray();
		StringBuffer retStr = new StringBuffer("");
		AnnotationDiffer globalDiff = new AnnotationDiffer(documentDiffer);
		List<String> measuresRow = globalDiff.getMeasuresRow(measuresDiff, "GLOBAL");
		retStr.append(String.join("\t", measuresRow)+ "\n");
		Map<String, List<AnnotationDiffer>> multiDocDifferByType = new HashMap<String, List<AnnotationDiffer>>();
		for(int i = 0; i < documentNames.size(); i++) {
			for(Entry<String, AnnotationDiffer> documentDifferByTypeElem : documentDifferByType.get(i).entrySet()) {
				if(!multiDocDifferByType.containsKey(documentDifferByTypeElem.getKey())) multiDocDifferByType.put(documentDifferByTypeElem.getKey(), new ArrayList<AnnotationDiffer>());
				multiDocDifferByType.get(documentDifferByTypeElem.getKey()).add(documentDifferByTypeElem.getValue());
			}
		}
		
		for(Entry<String, List<AnnotationDiffer>> multiDocDifferByTypeEntry : multiDocDifferByType.entrySet()) {
			List<String> result = (new AnnotationDiffer(multiDocDifferByTypeEntry.getValue()).getMeasuresRow(measuresDiff, "ALL_DOCS_TOGETHER"));
			JSONObject field_json = new JSONObject();
			field_json.put("Field", multiDocDifferByTypeEntry.getKey());
			
			JSONObject q = new JSONObject();
			q.put("CorrectMatches", result.get(1));
			q.put("Missing", result.get(2));
			q.put("Spurious", result.get(3));
			q.put("PartiallyCorrectMatches", result.get(4));
			field_json.put("quantity", q);
			
			JSONObject s = new JSONObject();
			s.put("Precision", result.get(5));
			s.put("Recall", result.get(6));
			s.put("Fmesure", result.get(7));
			field_json.put("strict", s);
			
			JSONObject l = new JSONObject();
			l.put("Precision", result.get(8));
			l.put("Recall", result.get(9));
			l.put("Fmesure", result.get(10));
			field_json.put("lanient", l);
			
			JSONObject a = new JSONObject();
			a.put("Precision", result.get(11));
			a.put("Recall", result.get(12));
			a.put("Fmesure", result.get(13));
			field_json.put("average", a);
			
			json_fields.add(field_json);
		}
		
		json.put("fields", json_fields);
		JSONObject field_json = new JSONObject();
		
		JSONObject q = new JSONObject();
		q.put("CorrectMatches", measuresRow.get(1));
		q.put("Missing", measuresRow.get(2));
		q.put("Spurious", measuresRow.get(3));
		q.put("PartiallyCorrectMatches", measuresRow.get(4));
		field_json.put("quantity", q);
		
		JSONObject s = new JSONObject();
		s.put("Precision", measuresRow.get(5));
		s.put("Recall", measuresRow.get(6));
		s.put("Fmesure", measuresRow.get(7));
		field_json.put("strict", s);
		
		JSONObject l = new JSONObject();
		l.put("Precision", measuresRow.get(8));
		l.put("Recall", measuresRow.get(9));
		l.put("Fmesure", measuresRow.get(10));
		field_json.put("lanient", l);
		
		JSONObject a = new JSONObject();
		a.put("Precision", measuresRow.get(11));
		a.put("Recall", measuresRow.get(12));
		a.put("Fmesure", measuresRow.get(13));
		field_json.put("average", a);
		
		json.put("global", field_json);
		return json.toJSONString();
	}

	
}