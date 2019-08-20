FROM alpine:3.9
WORKDIR /usr/local/share/ades_relation

ARG	ADES_RELATION_VERSION=1.0
COPY	docker-build.sh /usr/local/bin/docker-build.sh
COPY	src src
COPY	pom.xml .

RUN mkdir logs
RUN chmod u=rwx,g=rwx,o=r /usr/local/share/ades_relation -R

RUN	docker-build.sh ${ADES_RELATION_VERSION}

