FROM alpine:3.9
WORKDIR /usr/local/share/adesexporttojson

ARG	ADES_EXPORT_JSON_VERSION=1.0
COPY	docker-build.sh /usr/local/bin/docker-build.sh
COPY	src src	
COPY	pom.xml .

RUN chmod u=rwx,g=rwx,o=r /usr/local/share/adesexporttojson -R

RUN	docker-build.sh ${ADES_EXPORT_JSON_VERSION}

