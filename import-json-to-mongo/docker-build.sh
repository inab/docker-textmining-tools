#!/bin/sh

BASEDIR=/usr/local
IMPORT_JSON_TO_MONGO_HOME="${BASEDIR}/share/import_json_to_mongo"

IMPORT_JSON_TO_MONGO_VERSION=1.0

# Exit on error
set -e
 
if [ $# -ge 1 ] ; then
	IMPORT_JSON_TO_MONGO_VERSION="$1"
fi

if [ -f /etc/alpine-release ] ; then
	# Installing OpenJDK 8
	apk add --update openjdk8-jre
	
	# ades development dependencies 
	apk add openjdk8 git maven
else
	# Runtime dependencies
	apt-get update
	apt-get install openjdk-8-jre
	
	# The development dependencies
	apt-get install openjdk-8-jdk git maven
fi

mvn clean install -DskipTests

#rename jar
mv target/import-json-to-mongo-0.0.1-SNAPSHOT-jar-with-dependencies.jar import-json-to-mongo-${IMPORT_JSON_TO_MONGO_VERSION}.jar

cat > /usr/local/bin/import-json-to-mongo <<EOF
#!/bin/sh
exec java \$JAVA_OPTS -jar "${IMPORT_JSON_TO_MONGO_HOME}/import-json-to-mongo-${IMPORT_JSON_TO_MONGO_VERSION}.jar" -workdir "${IMPORT_JSON_TO_MONGO_HOME}" "\$@"
EOF
chmod +x /usr/local/bin/import-json-to-mongo

#delete target
rm -R target src pom.xml

#add bash for nextflow
apk add bash

if [ -f /etc/alpine-release ] ; then
	# Removing not needed tools
	apk del openjdk8 git maven
	rm -rf /var/cache/apk/*
else
	apt-get remove openjdk-8-jdk git maven
	rm -rf /var/cache/dpkg
fi

