# own-ades-terminology-annotation

treatment-related findings related facets annotator component: manifestations of findings, 

## Description

Internally, the own-ades-terminology-annotation library uses the generic nlp-generic-dictionary-annotation https://github.com/inab/docker-textmining-tools/tree/
master/nlp-generic-dictionary-annotation. This library is a generic component that annotate text with parametrices GATE-formatted gazetters/dictionaries. In other words, the hepatotoxicity-annotation library is an instance of the nlp-generic-dictionary-annotation with the hepatotoxicity dictionaries.

## For clone this component

	git clone --depth 1 https://github.com/inab/docker-textmining-tools.git own-ades-terminology-annotation
	cd own-ades-terminology-annotation
	git filter-branch --prune-empty --subdirectory-filter own-ades-terminology-annotation HEAD

## Build and Run the Docker 

	# To build the docker, just go into the cdisc-etox-annotation folder and execute
	docker build -t own-ades-terminology-annotation .
	#To run the docker, just set the input_folder and the output
	mkdir ${PWD}/output_annotation; docker run --rm -u $UID -v ${PWD}/input_folder:/in:ro -v ${PWD}/output_annoation:/out:rw cdisc-etox-annotation own-ades-terminology-annotation -i /in -o /out -a MY_SET_NAME	
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
	
