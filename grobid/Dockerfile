FROM alpine:3.9
WORKDIR /usr/local/bin

ARG	GROBID_VERSION=0.5.4
COPY	docker-build.sh .
RUN	docker-build.sh ${GROBID_VERSION}

