FROM alpine:3.9
WORKDIR /usr/local/bin

ARG	CERMINE_VERSION=6a253f28fec8ddc5f6eb98d514951c55954f5878
COPY	docker-build.sh .
RUN	docker-build.sh ${CERMINE_VERSION}

