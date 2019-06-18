FROM ubuntu:18.04
WORKDIR /usr/local/share/gnormplus/

ARG	GNORMPLUS_GATE_WRAPPER_VERSION=1.0

COPY	docker-build.sh /usr/local/bin/docker-build.sh
COPY	src src	
#COPY	libs libs	
#COPY	Corpus Corpus
#COPY	CRF CRF
#COPY	Dictionary Dictionary
#COPY	Dictionary.zip Dictionary.zip
COPY	tmp tmp
COPY	pom.xml .
#COPY	setup.txt .
#COPY	BioC.dtd .
#COPY	Ab3P .
#COPY	Installation.sh .

RUN mkdir logs
RUN chmod u=rx,o=r /usr/local/share/gnormplus -R
RUN chmod u=rwx,g=rwx,o=rwx tmp -R
#RUN chmod u=rwx,g=rwx,o=rwx Ab3P
RUN	docker-build.sh ${GNORMPLUS_GATE_WRAPPER_VERSION}
