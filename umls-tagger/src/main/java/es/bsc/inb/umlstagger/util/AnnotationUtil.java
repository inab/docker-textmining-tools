package es.bsc.inb.umlstagger.util;

import java.util.Arrays;
import java.util.Set;

import edu.stanford.nlp.util.Generics;


public class AnnotationUtil {
	
	public static final Set<String> entityMentionsToDelete = Generics.newHashSet(Arrays.asList(new String[]{"NUMBER", "MONEY", "DATE","TIME","TITLE","CAUSE_OF_DEATH","PERSON"}));
	
	public static final Set<String> stopWordsEn = Generics.newHashSet(Arrays.asList(new String[]{"a", "an", "the", "of", "at",
		      "on", "upon", "in", "to", "from", "out", "as", "so", "such", "or", "and", "those", "this", "these", "that",
		      "for", ",", "is", "was", "am", "are", "'s", "been", "were","none","no", "all", "ca","appendix","per","but","page","nor"}));
	
}
