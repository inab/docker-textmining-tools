FROM alpine:3.9
WORKDIR /usr/local/share/adespostprocessing

ARG	ADES_POSTPROCESSING_VERSION=1.0
COPY	docker-build.sh /usr/local/bin/docker-build.sh
COPY	src src	
COPY	pom.xml .

RUN chmod u=rwx,g=rwx,o=r /usr/local/share/adespostprocessing -R

RUN	docker-build.sh ${ADES_POSTPROCESSING_VERSION}

