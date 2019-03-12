#!/bin/sh

BASEDIR=/usr/local
METAMAP_HOME="${BASEDIR}/share/metamap/"

METAMAP_GATE_WRAPPER_VERSION=1.0

# Exit on error
set -e

if [ $# -ge 1 ] ; then
	METAMAP_GATE_WRAPPER_VERSION="$1"
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

mvn install:install-file -Dfile=libs/MetaMapApi.jar -DgroupId=gov.nih.nlm.nls -DartifactId=metamapapi -Dversion=1.0 -Dpackaging=jar
mvn install:install-file -Dfile=libs/prologbeans.jar -DgroupId=se.sics -DartifactId=prologbeans -Dversion=4.2.1 -Dpackaging=jar
#mvn install:install-file -Dfile=libs/annie-8.5.jar -DgroupId=uk.ac.gate.plugins -DartifactId=annie -Dversion=8.5 -Dpackaging=jar


mvn clean install -DskipTests

#rename jar
mv target/metamap-gate-wrapper-0.0.1-SNAPSHOT-jar-with-dependencies.jar metamap-gate-wrapper-${METAMAP_GATE_WRAPPER_VERSION}.jar

cat > /usr/local/bin/metamap-gate-wrapper <<EOF
#!/bin/sh
exec java \$JAVA_OPTS -jar "${METAMAP_HOME}/metamap-gate-wrapper-${METAMAP_GATE_WRAPPER_VERSION}.jar" -workdir "${METAMAP_HOME}" "\$@"
EOF
chmod +x /usr/local/bin/metamap-gate-wrapper


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

