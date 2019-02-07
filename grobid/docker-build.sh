#!/bin/sh

GROBID_VERSION=0.5.3
BASEDIR=/usr/local
JARSDIR="${BASEDIR}/share/grobid"
GROBID_HOME="${BASEDIR}/share/grobid-home"

# Exit on error
set -e

if [ $# -ge 1 ] ; then
	GROBID_VERSION="$1"
fi

# Installing OpenJDK 8
apk add --update openjdk8-jre openjdk8 git

# Getting grobid sources and its configuration directory
mkdir /tmp/grobid_install
cd /tmp/grobid_install
git clone -b "$GROBID_VERSION" --depth 1 https://github.com/kermitt2/grobid.git
cd grobid
./gradlew --no-daemon shadowJar

# Copy the grobid-home dir
cp -dpTr grobid-home "${GROBID_HOME}"
sed -i 's#^grobid.temp.path=.*#grobid.temp.path=/tmp#' "${GROBID_HOME}/config/grobid.properties"

# Last, copy JARs to destination, while creating their invocation wrappers
mkdir -p "$JARSDIR"
for targ in grobid-core grobid-trainer ; do
	cp -p "${targ}/build/libs/${targ}-${GROBID_VERSION}-onejar.jar" "$JARSDIR"
	cat > /usr/local/bin/"${targ}" <<EOF
#!/bin/sh
exec java -jar "${JARSDIR}/${targ}-${GROBID_VERSION}-onejar.jar" -gH "${GROBID_HOME}"
EOF
	chmod +x /usr/local/bin/"${targ}"
done

# Removing not needed tools
apk del openjdk8 git

# And the sources
rm -rf /root/.gradle /tmp/grobid_install /tmp/hsperfdata_root /var/cache/apk/*
