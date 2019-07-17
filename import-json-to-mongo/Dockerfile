FROM alpine:3.9
WORKDIR /usr/local/share/import_json_to_mongo

ARG	IMPORT_JSON_TO_MONGO_VERSION=1.0
COPY	docker-build.sh /usr/local/bin/docker-build.sh
COPY	src src	
COPY	pom.xml .

RUN chmod u=rwx,g=rwx,o=r /usr/local/share/import_json_to_mongo -R

RUN	docker-build.sh ${IMPORT_JSON_TO_MONGO_VERSION}

