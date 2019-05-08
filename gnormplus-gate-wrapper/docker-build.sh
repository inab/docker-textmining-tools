#!/bin/sh

BASEDIR=/usr/local
GNORMPLUS_HOME="${BASEDIR}/share/gnormplus/"

GNORMPLUS_GATE_WRAPPER_VERSION=1.0

# Exit on error
set -e

if [ $# -ge 1 ] ; then
	GNORMPLUS_GATE_WRAPPER_VERSION="$1"
fi

# Runtime dependencies
apt-get update
apt-get install -y openjdk-8-jre 
	
# The development dependencies
apt-get install -y openjdk-8-jdk git maven make g++


#The GnormPlus project is not a maven project, some of the libraries that includes are not available in the mavens reporsitory or 
#the version is not clear.  Installation of the system path libs to maven repository m2
mvn install:install-file -Dfile=libs/GNormPlus.jar -DgroupId=gnormplus.com -DartifactId=gnormplus_thirdparty -Dversion=1.0 -Dpackaging=jar


mvn clean install -DskipTests

#rename jar
mv target/gnormplus-gate-wrapper-0.0.1-SNAPSHOT-jar-with-dependencies.jar gnormplus-gate-wrapper-${GNORMPLUS_GATE_WRAPPER_VERSION}.jar

#Installation of CRF
./Installation.sh

cat > /usr/local/bin/gnormplus-gate-wrapper <<EOF
#!/bin/sh
exec java \$JAVA_OPTS -jar "${GNORMPLUS_HOME}/gnormplus-gate-wrapper-${GNORMPLUS_GATE_WRAPPER_VERSION}.jar" -workdir "${GNORMPLUS_HOME}" "\$@"
EOF
chmod +x /usr/local/bin/gnormplus-gate-wrapper


#delete unnecesary files
rm -R target src libs pom.xml


apt-get remove -y openjdk-8-jdk git maven
rm -rf /var/cache/dpkg


