FROM alpine:3.9
WORKDIR /usr/local/share/linnaeus

ARG	LINNAEUS_GATE_WRAPPER_VERSION=1.0
COPY	docker-build.sh /usr/local/bin/docker-build.sh
COPY	src src	
COPY	pom.xml .

RUN mkdir logs
RUN chmod u=rx,o=r /usr/local/share/linnaeus -R
RUN chmod u=rwx,g=rwx,o=rwx logs -R

RUN	docker-build.sh ${LINNAEUS_GATE_WRAPPER_VERSION}

