#!/bin/sh

BASEDIR=/usr/local
HEP_TAGGER_HOME="${BASEDIR}/share/hepatotoxicity_annotation/"

HEP_TAGGER_VERSION=1.0

# Exit on error
set -e

if [ $# -ge 1 ] ; then
	HEP_TAGGER_VERSION="$1"
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

git clone --depth 1 https://github.com/inab/docker-textmining-tools.git nlp_generic_annotation
cd nlp_generic_annotation
git filter-branch --prune-empty --subdirectory-filter nlp-generic-dictionary-annotation HEAD
mvn clean install -DskipTests
mv jape_rules ${HEP_TAGGER_HOME}
cd ..
#rename jar
mv nlp_generic_annotation/target/nlp-generic-dictionary-annotation-0.0.1-SNAPSHOT-jar-with-dependencies.jar nlp-generic-dictionary-annotation-${HEP_TAGGER_VERSION}.jar

cat > /usr/local/bin/hepatotoxicity-annotation <<EOF
#!/bin/sh
exec java \$JAVA_OPTS -jar "${HEP_TAGGER_HOME}/nlp-generic-dictionary-annotation-${HEP_TAGGER_VERSION}.jar" -workdir "${HEP_TAGGER_HOME}" -l dictionaries/hepatotoxicity.zip "\$@" 
EOF
chmod +x /usr/local/bin/hepatotoxicity-annotation

#delete target, do not delete for now because it has the jape rules inside
#rm -R nlp_generic_annotation

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

