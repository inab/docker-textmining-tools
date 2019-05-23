EVALUATION-NER
========================

<b>Generic component that evaluates NER annotations using the XML Gate-formatted documents </b>   

========================

The component validate the performance of NER annotations files.  

The performance metrics follows the GATE Annotation Diff tool concept:

The Annotation Diff tool enables two sets of annotations in one or two documents to be compared, in order either to compare a system-annotated text with a reference (hand-annotated) text, or to compare the output of two different versions of the system (or two different systems). For each annotation type, ﬁgures are generated for precision, recall, F-measure. Each of these can be calculated according to 3 different criteria - strict, lenient and average. The reason for this is to deal with partially correct responses in different ways.
	<p>The Strict measure considers all partially correct responses as incorrect (spurious).</p>
    <p>The Lenient measure considers all partially correct responses as correct.</p>
    <p>The Average measure allocates a half weight to partially correct responses (i.e. it takes the average of strict and lenient).</p>

All annotations from the key set are compared with the ones from the response set.  The output of the tool will be a document with the following data separated by tabs:

Name CorrectMatches Missing Spurious PartiallyCorrectMatches Precision - strict Recall - strict Fmesure - strict Precision - lanient Recall - lanient Fmesure - lanient Precision - average Recall - average Fmesure - average

This information will be provided specifically for each annotation type and globally.

========================

Build and run the docker individually

	# To build the docker, just go into the evaluation-ner folder and execute
	docker build -t evaluation-ner .
	#To run the docker, just set the input_folder and the output
	docker run --rm -u $UID -v ${PWD}/input_annotated_folder:/in:ro -v ${PWD}:/out:rw evaluation-ner:1.0 evaluation-ner -i /in -o /out/performance_docker.txt -k EVALUATION -e BSC -d false	
Parameters:
<p>
-i Input folder with the documents to annotated. The documents are in XML GATE-Formatted
</p>
<p>
-o Result file with the performance metrics
</p>
<p>
-k Key annotation set with the golden values 
</p>
<p>
-e  Annotation set to be evaluate
</p>
<p>
-d  Indicates if the output has to contain the result by each document.  Could be true or false (default) 
</p>
		
		
