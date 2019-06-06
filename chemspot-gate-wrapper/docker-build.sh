#!/bin/sh

BASEDIR=/usr/local
CHEMSPOT_HOME="${BASEDIR}/share/chemspot_tagger/"

CHEMSPOT_GATE_WRAPPER_VERSION=1.0

# Exit on error
set -e

if [ $# -ge 1 ] ; then
	CHEMSPOT_GATE_WRAPPER_VERSION="$1"
fi

if [ -f /etc/alpine-release ] ; then
	# Installing OpenJDK 8
	apk add --update openjdk8-jre
	#chemspot_wrapper's development dependencies
	apk add openjdk8 git maven
else
	# Runtime dependencies
	apt-get update
	apt-get install openjdk-8-jre
	# The development dependencies
	apt-get install openjdk-8-jdk git maven
fi

#add this library to maven repository

mvn install:install-file -Dfile=lib/chemspot2.0.jar -DgroupId=com.chemspot -DartifactId=chemspot -Dversion=2.0 -Dpackaging=jar

mvn clean install -DskipTests

#rename jar
mv target/chemspot-tagger-gate-wrapper-0.0.1-SNAPSHOT-jar-with-dependencies.jar chemspot-tagger-gate-wrapper-${CHEMSPOT_GATE_WRAPPER_VERSION}.jar

cat > /usr/local/bin/chemspot-tagger-gate-wrapper <<EOF
#!/bin/sh
exec java \$JAVA_OPTS -jar "${DNORM_HOME}/chemspot-tagger-gate-wrapper-${DNORM_GATE_WRAPPER_VERSION}.jar" -workdir "${CHEMSPOT_HOME}" -configfile "${DNORM_HOME}/config/banner_NCBIDisease_TEST_PROD.xml" "\$@"
EOF
chmod +x /usr/local/bin/chemspot-tagger-gate-wrapper

#delete unnecesary files
rm -R target src libs pom.xml

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

