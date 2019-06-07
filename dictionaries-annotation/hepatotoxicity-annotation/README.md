nlp-generic-dictionary-annotation
========================

<b>Generic library for dictionary mapping in text</b>   

========================

This library annotated text with terms present in a dictionary using the GATE format.  Is a wrapper that execute the ANNIE DefaultGazeteer and basics JAPE rules that annotated text with given dictionaries. 

One of the input is the list of the dictionaries/gazeteers entries, has to be provided as in the GATE DefaultGazzeteer format. More information here:
https://gate.ac.uk/sale/tao/splitch13.html#x18-32200013.2

After the execution of the DefaultGazetter, the tool executes a JAPE rule to detect the Lookup annotations and generate the corresponding annotation taking into account the minorType of the configuration, and copy all the features present in the Lookup annotation. 
For example:

hepatotoxicity.lst:TOXICOLOGY:HEPATOTOXICITY  

The name of the annotation will be HEPATOTOXICITY and is going to be set into the annotationSet given as parameter. 
This tool remove the Basic Lookup annotation from the DefaultGazeteer.

This library is very use full if we need to run batch mode, generating the software container.

========================

Build and run the docker individually

	# To build the docker, just go into the ades-tagger folder and execute
	docker build -t nlp-generic-dictionary-annotation .
	#To run the docker, just set the input_folder and the output
	mkdir ${PWD}/output_annoation; docker run --rm -u $UID -v ${PWD}/input_folder:/in:ro -v ${PWD}/output_annoation:/out:rw nlp-generic-dictionary-annotation nlp-generic-dictionary-annotation -i /in -o /out	
Parameters:
<p>
-i input folder with the documents to annotated. The documents could be plain txt or xml gate documents.
</p>
<p>
-o output folder with the documents annotated in gate format.
</p>
<p>
-a annotation set output
</p>
<p>
-l list definition of the dictionary, with the GATE format.
</p>
		
		
