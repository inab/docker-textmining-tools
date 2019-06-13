FROM alpine:3.9
WORKDIR /usr/local/share/umlstagger

ARG	UMLS_TAGGER_VERSION=1.0
COPY	docker-build.sh /usr/local/bin/docker-build.sh
COPY	src src
COPY	jape_rules jape_rules
COPY	config.properties config.properties
COPY	pom.xml .


RUN mkdir logs
RUN chmod u=rwx,g=rwx,o=r /usr/local/share/umlstagger -R
RUN chmod u=rwx,g=rwx,o=rwx logs -R

RUN	docker-build.sh ${UMLS_TAGGER_VERSION}

