#!/bin/sh

BASEDIR=/usr/local
LINNAEUS_HOME="${BASEDIR}/share/linnaeus"

LINNAEUS_GATE_WRAPPER_VERSION=1.0

# Exit on error
set -e

if [ $# -ge 1 ] ; then
	LINNAEUS_GATE_WRAPPER_VERSION="$1"
fi

if [ -f /etc/alpine-release ] ; then
	# Installing OpenJDK 8
	apk add --update openjdk8-jre
	
	# linneaus_wrapper's development dependencies
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
mv target/linnaeus-gate-wrapper-0.0.1-SNAPSHOT-jar-with-dependencies.jar linnaeus-gate-wrapper-${LINNAEUS_GATE_WRAPPER_VERSION}.jar

cat > /usr/local/bin/linnaeus-gate-wrapper <<EOF
#!/bin/sh
exec java \$JAVA_OPTS -jar "${LINNAEUS_HOME}/linnaeus-gate-wrapper-${LINNAEUS_GATE_WRAPPER_VERSION}.jar" "\$@"
EOF
chmod +x /usr/local/bin/linnaeus-gate-wrapper


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

