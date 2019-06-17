#!/bin/sh

BASEDIR=/usr/local
DICT_TAGGER_HOME="${BASEDIR}/share/dictionary_annotation/"

DICT_TAGGER_VERSION=1.0

# Exit on error
set -e

if [ $# -ge 1 ] ; then
	DICT_TAGGER_VERSION="$1"
fi

if [ -f /etc/alpine-release ] ; then
	# Installing OpenJDK 8
	apk add --update openjdk8-jre
	
	# dict tagger development dependencies
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
mv target/nlp-generic-dictionary-annotation-0.0.1-SNAPSHOT-jar-with-dependencies.jar nlp-generic-dictionary-annotation-${DICT_TAGGER_VERSION}.jar

cat > /usr/local/bin/nlp-generic-dictionary-annotation <<EOF
#!/bin/sh
exec java \$JAVA_OPTS -jar "${DICT_TAGGER_HOME}/nlp-generic-dictionary-annotation-${DICT_TAGGER_VERSION}.jar" -workdir "${DICT_TAGGER_HOME}" "\$@"
EOF
chmod +x /usr/local/bin/nlp-generic-dictionary-annotation

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

