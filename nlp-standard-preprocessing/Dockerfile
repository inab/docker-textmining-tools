FROM alpine:3.9
WORKDIR /usr/local/share/nlp_preprocessing

ARG	NLP_STANDARD_PREPROCESSING_VERSION=1.0
COPY	docker-build.sh /usr/local/bin/docker-build.sh
COPY	src src	
COPY	pom.xml .

RUN chmod u=rwx,g=rwx,o=r /usr/local/share/nlp_preprocessing -R

RUN	docker-build.sh ${NLP_STANDARD_PREPROCESSING_VERSION}

