FROM alpine:3.9
WORKDIR /usr/local/bin

ARG	LINNEAUS_GATE_WRAPPER_VERSION=1.0
COPY	docker-build.sh .
COPY	src src	
COPY	pom.xml .
RUN	docker-build.sh ${LINNEAUS_GATE_WRAPPER_VERSION}

