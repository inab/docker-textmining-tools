FROM alpine:3.9
WORKDIR /usr/local/share/ades_relation_extraction

ARG	ADES_RELATION_EXTRACTION_VERSION=1.0

COPY	docker-build.sh /usr/local/bin/docker-build.sh
COPY	jape_rules jape_rules

RUN chmod u=rx,o=r /usr/local/share/ades_relation_extraction -R

RUN	docker-build.sh ${ADES_RELATION_EXTRACTION_VERSION}
