FROM alpine:3.9
WORKDIR /usr/local/share/adesnerpostprocessing

ARG	ADES_POSTPROCESSING_VERSION=1.0
COPY	docker-build.sh /usr/local/bin/docker-build.sh
COPY	jape_rules jape_rules
COPY	dictionaries dictionaries

RUN chmod u=rwx,g=rwx,o=r /usr/local/share/adesnerpostprocessing -R

RUN	docker-build.sh ${ADES_POSTPROCESSING_VERSION}

