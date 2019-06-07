FROM alpine:3.9
WORKDIR /usr/local/share/hepatotoxicity_annotation

ARG	HEP_TAGGER_VERSION=1.0
COPY	docker-build.sh /usr/local/bin/docker-build.sh

COPY	dictionaries dictionaries

RUN chmod u=rwx,g=rwx,o=r /usr/local/share/hepatotoxicity_annotation -R

RUN	docker-build.sh ${HEP_TAGGER_VERSION}

