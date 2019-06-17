#!/bin/sh

BASEDIR=/usr/local
ADES_POSTPROCESSING_HOME="${BASEDIR}/share/adesnerpostprocessing/"

ADES_POSTPROCESSING_VERSION=1.0

# Exit on error
set -e

if [ $# -ge 1 ] ; then
	ADES_POSTPROCESSING_VERSION="$1"
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
mv target/ades-ner-postprocessing-0.0.1-SNAPSHOT-jar-with-dependencies.jar ades-ner-postprocessing-${ADES_POSTPROCESSING_VERSION}.jar

git clone --depth 1 https://github.com/inab/docker-textmining-tools.git nlp_gate_generic_component
cd nlp_gate_generic_component
git filter-branch --prune-empty --subdirectory-filter nlp-gate-generic-wrapper HEAD
mvn clean install -DskipTests
echo "pepe2"
cd ..
#rename jar
mv nlp_gate_generic_component/target/nlp-gate-generic-wrapper-0.0.1-SNAPSHOT-jar-with-dependencies.jar nlp-gate-generic-wrapper-${ADES_POSTPROCESSING_VERSION}.jar

cat > /usr/local/bin/ades-ner-postprocessing <<EOF
#!/bin/sh
exec java \$JAVA_OPTS -jar "${ADES_POSTPROCESSING_HOME}/nlp-gate-generic-wrapper-${ADES_POSTPROCESSING_VERSION}.jar" -workdir "${ADES_POSTPROCESSING_HOME}" -j jape_rules/main.jape "\$@"
exec java \$JAVA_OPTS -jar "${ADES_POSTPROCESSING_HOME}/ades-ner-postprocessing-${ADES_POSTPROCESSING_VERSION}.jar" -workdir "${ADES_POSTPROCESSING_HOME}" "\$@"
EOF
chmod +x /usr/local/bin/ades-ner-postprocessing

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

