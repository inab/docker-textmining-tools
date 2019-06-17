ades-relation-extraction
========================

<b>Component to extract relationships at the pre-clinical ADEs (Adverse Drug Effects) project.
</b>   

========================

The relationships between the diferents entities are develop in JAPE rules and the input and output of the component are GATE-formated documents.

========================

Build and run the docker individually
	
	# To build the docker, just go into the ades-relation-extraction folder and execute
	docker build -t ades-relation-extraction .
	#To run the docker, just set the input_folder and the output
	mkdir ${PWD}/ades_relation_output; docker run --rm -u $UID -v ${PWD}/input_folder:/in:ro -v ${PWD}/ades_relation_output:/out:rw ades-relation-extraction ades-relation-extraction -i /in -o /out -a "BSC"	
		
Parameters:
<p>
-i input folder with the documents to annotated. The documents could be plain txt or xml gate documents.
</p>
<p>
-o output folder with the documents annotated in gate xml format.
</p>
<p>
-a annotation set output
</p>

<p>If you want, just replace "input_folder" for your directory that contains the files, and execute. You can also replace the name of the output folder "ades_relation_output"</p>
		
		
