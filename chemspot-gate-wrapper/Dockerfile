FROM alpine:3.9
WORKDIR /usr/local/share/chemspot_tagger

ARG	CHEMSPOT_GATE_WRAPPER_VERSION=1.0

COPY	docker-build.sh /usr/local/bin/docker-build.sh
COPY	src src	
COPY	pom.xml .

RUN mkdir logs
RUN chmod u=rx,o=r /usr/local/share/chemspot_tagger -R
RUN chmod u=rwx,g=rwx,o=rwx logs -R

RUN	docker-build.sh ${CHEMSPOT_GATE_WRAPPER_VERSION}
