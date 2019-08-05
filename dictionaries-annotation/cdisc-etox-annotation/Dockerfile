FROM alpine:3.9
WORKDIR /usr/local/share/cdisc_etox_annotation
 
ARG	HEP_TAGGER_VERSION=1.0
COPY	docker-build.sh /usr/local/bin/docker-build.sh
COPY	dictionaries dictionaries
COPY	jape_rules jape_rules

RUN chmod u=rwx,g=rwx,o=r /usr/local/share/cdisc_etox_annotation -R

RUN	docker-build.sh ${CDISC_ETOX_TAGGER_VERSION}

