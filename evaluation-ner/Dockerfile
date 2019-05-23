FROM alpine:3.9
WORKDIR /usr/local/share/evaluation_ner

ARG EVALUATION_NER_VERSION=1.0
COPY	docker-build.sh /usr/local/bin/docker-build.sh
COPY	src src	
COPY	pom.xml .

RUN chmod u=rwx,g=rwx,o=r /usr/local/share/evaluation_ner -R

RUN	docker-build.sh ${EVALUATION_NER_VERSION}

