#!/bin/sh

CERMINE_VERSION=6a253f28fec8ddc5f6eb98d514951c55954f5878
BASEDIR=/usr/local
JARSDIR="${BASEDIR}/share/cermine"

# Exit on error
set -e

if [ $# -ge 1 ] ; then
	CERMINE_VERSION="$1"
fi


mkdir /tmp/cermine_install

if [ -f /etc/alpine-release ] ; then
	# Installing OpenJDK 8
	apk add --update openjdk8-jre
	
	# cermine's development dependencies
	apk add openjdk8 git maven
else
	# Runtime dependencies
	apt-get update
	apt-get install openjdk-8-jre
	
	# The development dependencies
	apt-get install openjdk-8-jdk git maven
fi

# Getting CERMINE sources and its configuration directory
cd /tmp/cermine_install
git clone https://github.com/CeON/CERMINE.git

cd /tmp/cermine_install/CERMINE
git checkout "$CERMINE_VERSION"

mvn install -DskipTests

# Last, copy JARs to destination, while creating their invocation wrappers
mkdir -p "$JARSDIR"
for targ in cermine-tools ; do
	cd "$targ"
	mvn assembly:single
	cd ..
	
	cp -p "${targ}/target/${targ}"-*-"with-dependencies.jar" "$JARSDIR"
	jarfile=${JARSDIR}/${targ}-*-with-dependencies.jar
	cat > /usr/local/bin/"${targ}" <<EOF
#!/bin/sh
exec java \$JAVA_OPTS -cp ${jarfile} pl.edu.icm.cermine.ContentExtractor "\$@"
EOF
	chmod +x /usr/local/bin/"${targ}"
done

if [ -f /etc/alpine-release ] ; then
	# Removing not needed tools
	apk del openjdk8 git maven
	rm -rf /var/cache/apk/*
else
	apt-get remove openjdk-8-jdk git maven
	rm -rf /var/cache/dpkg
fi

# And the sources
rm -rf /root/.m2 /tmp/cermine_install /tmp/hsperfdata_root
