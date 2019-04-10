#!/bin/sh

BASEDIR=/usr/local
DNORM_HOME="${BASEDIR}/share/dnorm/"

DNORM_GATE_WRAPPER_VERSION=1.0

# Exit on error
set -e

if [ $# -ge 1 ] ; then
	DNORM_GATE_WRAPPER_VERSION="$1"
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

#The Dnorm project is not a maven project, some of the libraries that includes are not available in the mavens reporsitory or 
#the version is not clear.  Installation of the system path libs to maven repository m2

mvn install:install-file -Dfile=libs/dnorm.jar -DgroupId=dnorm.com -DartifactId=dnorm_thirdparty -Dversion=1.0 -Dpackaging=jar
mvn install:install-file -Dfile=libs/banner.jar -DgroupId=banner.com -DartifactId=banner_thirdparty -Dversion=1.0 -Dpackaging=jar
mvn install:install-file -Dfile=libs/bioc.jar -DgroupId=bioc.com -DartifactId=bioc_thirdparty -Dversion=1.0 -Dpackaging=jar
mvn install:install-file -Dfile=libs/heptag.jar -DgroupId=heptag.com -DartifactId=heptag_thirdparty -Dversion=1.0 -Dpackaging=jar
mvn install:install-file -Dfile=libs/mallet-deps.jar -DgroupId=malletdeps.com -DartifactId=malletdeeps_thirdparty -Dversion=1.0 -Dpackaging=jar
mvn install:install-file -Dfile=libs/colt.jar -DgroupId=colt.com -DartifactId=colt_thirdparty -Dversion=1.0 -Dpackaging=jar

mvn clean install -DskipTests

#rename jar
mv target/dnorm-gate-wrapper-0.0.1-SNAPSHOT-jar-with-dependencies.jar dnorm-gate-wrapper-${DNORM_GATE_WRAPPER_VERSION}.jar

cat > /usr/local/bin/dnorm-gate-wrapper <<EOF
#!/bin/sh
exec java \$JAVA_OPTS -jar "${DNORM_HOME}/dnorm-gate-wrapper-${DNORM_GATE_WRAPPER_VERSION}.jar" -workdir "${DNORM_HOME}" -configfile "${DNORM_HOME}/config/banner_NCBIDisease_TEST_PROD.xml" "\$@"
EOF
chmod +x /usr/local/bin/dnorm-gate-wrapper


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

