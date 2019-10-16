# nlp-standard-preprocessing

This component runs a standard preprocessing nlp process. 
Sentence Splitting, Tokenization, Part of Speech (POS), other features: word types and formats.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### For clone this component

	git clone --depth 1 https://github.com/inab/docker-textmining-tools.git nlp-standard-preprocessing
	cd nlp-standard-preprocessing
	git filter-branch --prune-empty --subdirectory-filter nlp-standard-preprocessing HEAD

### Build and Run the Docker 

	#To build the docker, just go into the nlp-standard-preprocessing folder and execute
	docker build -t nlp-standard-preprocessing .
	#To run the docker, just set the input_folder and the output
	mkdir ${PWD}/nlp_preprocessing_output; docker run --rm -u $UID -v ${PWD}/input_output:/in:ro -v ${PWD}/nlp_preprocessing_output:/out:rw nlp-standard-preprocessing nlp-standard-preprocessing -i /in -o /out	

Parameters:
<p>
-i input folder with the documents to annotated. The documents could be plain txt or xml gate documents.
</p>
<p>
-o output folder with the documents annotated in gate format.
</p>

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







		
		
