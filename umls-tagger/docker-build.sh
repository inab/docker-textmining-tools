#!/bin/sh

BASEDIR=/usr/local
UMLS_TAGGER_HOME="${BASEDIR}/share/umlstagger/"

UMLS_TAGGER_VERSION=1.0

# Exit on error
set -e

if [ $# -ge 1 ] ; then
	UMLS_TAGGER_VERSION="$1"
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
mv target/umls_tagger-0.0.1-SNAPSHOT-jar-with-dependencies.jar umls-tagger-${UMLS_TAGGER_VERSION}.jar

cat > /usr/local/bin/umlstagger <<EOF
#!/bin/sh
exec java \$JAVA_OPTS -jar "${ADES_TAGGER_HOME}/umls-tagger-${UMLS_TAGGER_VERSION}.jar" -workdir "${UMLS_TAGGER_HOME}" "\$@"
EOF
chmod +x /usr/local/bin/umlstagger

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

