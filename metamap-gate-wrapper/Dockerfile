FROM alpine:3.9
WORKDIR /usr/local/share/metamap

ARG	METAMAP_GATE_WRAPPER_VERSION=1.0
COPY	docker-build.sh /usr/local/bin/docker-build.sh
COPY	src src
COPY	libs libs
COPY	plugins plugins	
COPY	pom.xml .
COPY	semantic_types_mapping.txt .

RUN mkdir logs
RUN chmod u=rx,o=r /usr/local/share/metamap -R
RUN chmod u=rwx,g=rwx,o=rwx logs -R

RUN	docker-build.sh ${METAMAP_GATE_WRAPPER_VERSION}

