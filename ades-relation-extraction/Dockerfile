FROM alpine:3.9
WORKDIR /usr/local/share/dnorm

ARG	DNORM_GATE_WRAPPER_VERSION=1.0

COPY	docker-build.sh /usr/local/bin/docker-build.sh
COPY	src src	
COPY	libs libs	
COPY	config config
COPY	data data
COPY	nlpdata nlpdata
COPY	output output
COPY	pom.xml .

RUN mkdir logs
RUN chmod u=rx,o=r /usr/local/share/dnorm -R
RUN chmod u=rx,o=r output -R
RUN chmod u=rwx,g=rwx,o=rwx logs -R

RUN	docker-build.sh ${DNORM_GATE_WRAPPER_VERSION}
