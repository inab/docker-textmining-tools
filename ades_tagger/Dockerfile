FROM alpine:3.9
WORKDIR /usr/local/share/ades

ARG	ADES_TAGGER_VERSION=1.0
COPY	docker-build.sh /usr/local/bin/docker-build.sh
COPY	src src
COPY	dict dict
COPY	ner_list ner_list
COPY	rules rules	
COPY	pom.xml .


RUN mkdir logs
RUN chmod u=rwx,g=rwx,o=r /usr/local/share/ades -R
RUN chmod u=rwx,g=rwx,o=rwx logs -R

RUN	docker-build.sh ${ADES_TAGGER_VERSION}

