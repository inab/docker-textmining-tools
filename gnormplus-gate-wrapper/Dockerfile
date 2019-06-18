FROM ubuntu:18.04
WORKDIR /usr/local/share/gnormplus/

ARG	GNORMPLUS_GATE_WRAPPER_VERSION=1.0

COPY	docker-build.sh /usr/local/bin/docker-build.sh
COPY	src src	
COPY	tmp tmp
COPY	pom.xml .


RUN mkdir logs
RUN chmod u=rwx,g=rwx,o=rwx /usr/local/share/gnormplus -R
RUN chmod u=rwx,g=rwx,o=rwx tmp -R

RUN	docker-build.sh ${GNORMPLUS_GATE_WRAPPER_VERSION}
