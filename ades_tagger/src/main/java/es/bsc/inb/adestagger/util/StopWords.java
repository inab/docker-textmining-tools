package es.bsc.inb.adestagger.util;

import java.util.Arrays;
import java.util.Set;

import edu.stanford.nlp.util.Generics;

public final class StopWords {
	
	public static final Set<String> stopWordsEn = Generics.newHashSet(Arrays.asList(new String[]{"a", "an", "the", "of", "at",
		      "on", "upon", "in", "to", "from", "out", "as", "so", "such", "or", "and", "those", "this", "these", "that",
		      "for", ",", "is", "was", "am", "are", "'s", "been", "were","none","no", "all", "ca","appendix"}));


}
