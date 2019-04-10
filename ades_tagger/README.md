ADES-TAGGER
========================

<b>Tagger of adverse effects (ades)</b>   

========================

ADES NER: Adverse drugs effects name entity recognition

The ADES tagger is a text mining component that retrieves treatment-related effect or adverse effects at pre-clinical level.
To that aim the NER Tagger detect:





========================

Build and run the docker individually

	# To build the docker, just go into the ades-tagger folder and execute
	docker build -t addes-tagger .
	#To run the docker, just set the input_folder and the output
	mkdir ${PWD}/ades_output; docker run --rm -u $UID -v ${PWD}/input_output:/in:ro -v ${PWD}/ades_output:/out:rw ades-tagger ades -i /in -o /out	
Parameters:
<p>
-i input folder with the documents to annotated. The documents could be plain txt or xml gate documents.
</p>
<p>
-o output folder with the documents annotated in gate format.
</p>

		
		
