# ades-export-to-json

This component export the treatment-related findings annotated in XML GATE format to JSON format.

## Description 

This component is only used inside the treatment-related findings pipeline because is tired up to that specific domain.

## For clone this component

	git clone --depth 1 https://github.com/inab/docker-textmining-tools.git ades-export-to-json
	cd ades-export-to-json
	git filter-branch --prune-empty --subdirectory-filter ades-export-to-json HEAD

## Build and Run the Docker 

	# To build the docker, just go into the ades-export-to-json folder and execute
	docker build -t ades-export-to-json .
	#To run the docker, just set the input_folder and the output
	mkdir ${PWD}/output_annotation; docker run --rm -u $UID -v ${PWD}/input_folder:/in:ro -v ${PWD}/output_annoation:/out:rw ades-export-to-json ades-export-to-json -i /in -o /out -a MY_SET_NAME	
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
	
		
