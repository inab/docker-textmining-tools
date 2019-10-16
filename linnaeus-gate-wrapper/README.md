# linnaeus-gate-wrapper

This component is a Gate wrapper of the Linnaeus application.  Could be easily downloaded and run as a docker container.
   

## Description

LINNAEUS: Species Tagger

Uses a dictionary-based approach (implemented as an efficient deterministic finite-state automaton) to identify species names and a set of heuristics to resolve ambiguous mentions.
LINNAEUS performs with 94% recall and 97% precision at the mention level, and 98% recall and 90% precision at the document level. Our system successfully solves the problem of disambiguating uncertain species mentions, with 97% of all mentions in PubMed Central full-text documents resolved to unambiguous NCBI taxonomy identifiers.


For more detailed information:
https://www.ncbi.nlm.nih.gov/pubmed/20149233/

http://linnaeus.sourceforge.net/


## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

## For clone this component

	git clone --depth 1 https://github.com/inab/docker-textmining-tools.git linnaeus-gate-wrapper
	cd linnaeus-gate-wrapper
	git filter-branch --prune-empty --subdirectory-filter nlp-standard-preprocessing HEAD
	
## Build and run the docker individually

	# To build the docker, just go into the linnaeus-gate-wrapper folder and execute
	docker build -t linnaeus-gate-wrapper .
	#To run the docker, just set the input_folder and the output
	mkdir ${PWD}/linnaeus_output; docker run --rm -u $UID -v ${PWD}/input_folder:/in:ro -v ${PWD}/linnaeus_output:/out:rw linnaeus-gate-wrapper linnaeus-gate-wrapper -i /in -o /out
		
To parameters:
<p>
-i input folder with the documents to annotated. The documents could be plain txt or xml gate documents.
</p>
<p>
-o output folder with the documents annotated in gate format.
</p>

<p>If you want, just replace "input_folder" for your directory that contains the files, and execute. You can also replace the name of the output folder "linnaeus_output"</p>		
		
## Built With

* [Docker](https://www.docker.com/) - Docker Containers
* [Maven](https://maven.apache.org/) - Dependency Management
* [StanfordCoreNLP](https://stanfordnlp.github.io/CoreNLP/) - Stanford CoreNLP – Natural language software
* [GATE](https://gate.ac.uk/overview.html) - GATE: a full-lifecycle open source solution for text processing

## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/inab/docker-textmining-tools/edit/master/nlp-standard-preprocessing/tags). 

## Authors

* **Javier Corvi** 


## License

This project is licensed under the GNU GENERAL PUBLIC LICENSE Version 3 - see the [LICENSE.md](LICENSE.md) file for details
