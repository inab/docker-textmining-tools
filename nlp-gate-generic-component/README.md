# nlp-gate-generic-component

Text mining GATE generic component for run in Batch/Pipeline mode.

## Description

This component is a docker wrapper that execute the GATE ANNIE DefaultGazeteer and JAPE rules in batch mode. 

The tool execute the Default Gazeteer Lookup given the dictionaries passed as parameters and, in a second stage, execute JAPE rules given a main.jape file.

The list of the dictionaries/gazeteers entries has to be provided as in the GATE DefaultGazzeteer format. 

More information about ANNIE DefaultGazeteer: 
https://gate.ac.uk/sale/tao/splitch13.html#x18-32200013.2

More information about JAPE rules:  
https://gate.ac.uk/sale/thakker-jape-tutorial/GATE%20JAPE%20manual.pdf

This library is very useful if you need to run gazeteer lookup and JAPE rules in batch mode, inside a Nextflow pipeline for example.  

## For clone this component

	git clone --depth 1 https://github.com/inab/docker-textmining-tools.git nlp-gate-generic-component
	cd nlp-gate-generic-component
	git filter-branch --prune-empty --subdirectory-filter nlp-gate-generic-component HEAD

## Build and Run the Docker 

	# To build the docker, just go into the ades-tagger folder and execute
	docker build -t nlp-gate-generic-component .
	#To run the docker, just set the input_folder and the output
	mkdir ${PWD}/output_folder; docker run --rm -u $UID -v ${PWD}/input_folder:/in:ro -v ${PWD}/output_folder:/out:rw nlp-gate-generic-component nlp-gate-generic-component -i /in -o /out	-a ANNOTATION_SET -l in/dictionaries/lists.def -j in/jape_rules/main.jape
Parameters:
<p>
-i input folder with the documents to annotated. The documents could be plain txt or xml gate documents.
</p>
<p>
-o output folder with the documents annotated in gate format.
</p>
<p>
-a Output Annotation Set. Annotation set where the annotation will be included for the gazetter lookup and for the Jape Rules
</p>
<p>
-ia Input Annotation Set. If you want to provided different input annotation set this parameter.  By default the -a output annotation set is used as input.  
</p>
<p>
-l list definition of the dictionary in GATE format.
</p>
<p>
-j main.jape path with the JAPE rules to be executed.
</p>	
		
In this example the dictionaries/gazeteers and the jape rules are in the input folder.

## Built With

* [Docker](https://www.docker.com/) - Docker Containers
* [Maven](https://maven.apache.org/) - Dependency Management
* [GATE](https://gate.ac.uk/overview.html) - GATE: a full-lifecycle open source solution for text processing

## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/inab/docker-textmining-tools/edit/master/nlp-standard-preprocessing/tags). 

## Authors

* **Javier Corvi** 


## License

This project is licensed under the GNU GENERAL PUBLIC LICENSE Version 3 - see the [LICENSE.md](LICENSE.md) file for details

