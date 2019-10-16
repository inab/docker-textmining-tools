# cdisc-etox-annotation

This component annotated text using CDISC SEND and eTOX (OntoBrowser) terminologies. These terminologies are oriented to the preclinical study reports.  

## Description 

This component annotated several entities related to the treatment-related findings:
FINDINGS.
STUDY_TESTCDS.
SPECIMEN.

CDISC SEND controlled terminology:  CDISC maintain and develop the official SEND terminology, available at: https://evs.nci.nih.gov/ftp1/CDISC/SEND/.

ETOX terminology:  Information available from the Ontobrowser system was used to increase the terminology. The primary objective of these system was to provide an online collaborative solution for expert curators to map report terms (from the eTOX database) to preferred ontology (or controlled terminology) terms.

The cdisc-etox-annotation component uses the generic nlp-generic-dictionary-annotation https://github.com/inab/docker-textmining-tools/tree/master/nlp-generic-dictionary-annotation. This library is a generic component that annotate text with parametrices GATE-formatted gazetters/dictionaries. In other words, the hepatotoxicity-annotation library is an instance of the nlp-generic-dictionary-annotation with the hepatotoxicity dictionaries.


## For clone this component

	git clone --depth 1 https://github.com/inab/docker-textmining-tools.git cdisc-etox-annotation
	cd cdisc-etox-annotation
	git filter-branch --prune-empty --subdirectory-filter cdisc-etox-annotation HEAD

## Build and Run the Docker 

	# To build the docker, just go into the cdisc-etox-annotation folder and execute
	docker build -t cdisc-etox-annotation .
	#To run the docker, just set the input_folder and the output
	mkdir ${PWD}/output_annotation; docker run --rm -u $UID -v ${PWD}/input_folder:/in:ro -v ${PWD}/output_annoation:/out:rw cdisc-etox-annotation cdisc-etox-annotation -i /in -o /out -a MY_SET_NAME	
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
	
		
