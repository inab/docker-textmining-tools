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


mkdir /tmp/grobid_install

if [ -f /etc/alpine-release ] ; then
	# Installing OpenJDK 8 + bash (hardcoded grobid dep)
	apk add --update openjdk8-jre bash
	
	# grobid's development dependencies
	apk add openjdk8 git
else
	# Runtime dependencies
	apt-get update
	apt-get install openjdk-8-jre
	
	# The development dependencies
	apt-get install openjdk-8-jdk git
fi

# Getting grobid sources and its configuration directory
cd /tmp/grobid_install
git clone -b "$GROBID_VERSION" --depth 1 https://github.com/kermitt2/grobid.git

# We need to recompile pdf2xml in order to get it working in Alpine
if [ -f /etc/alpine-release ] ; then
	PDF2XML_VERSION=856723a8da2f9b9513f715d78a67e06f6e171b79
	cd /tmp/grobid_install/grobid
	
	# Runtime deps of pdf2xml
	apk add libxml2 libpng
	
	# Deps to rebuild pdf2xml
	apk add cmake make gcc g++ freetype-dev \
		fontconfig-dev jpeg-dev openjpeg-dev \
		openjpeg-tools tiff-dev lcms2-dev \
		cairo-dev motif-dev libxml2-dev libpng-dev zlib-dev pkgconf
	
	cd /tmp/grobid_install
	git clone --recurse-submodules https://github.com/kermitt2/pdf2xml.git
	cd pdf2xml
	git checkout --recurse-submodules "$PDF2XML_VERSION"
	
	# Fix to link against local libraries
	sed -i 's/^add_subdirectory(image/#add_subdirectory(image/;s# zlib # z #;' CMakeLists.txt
	cmake . -DCMAKE_BUILD_TYPE=release
	make
	
	# Replacing grobid's copy of pdf2xml
	cp -p pdf2xml /tmp/grobid_install/grobid/grobid-home/pdf2xml/lin-64/pdftoxml

	# Removing pdf2xml development dependencies
	apk del cmake make gcc g++ freetype-dev \
		fontconfig-dev jpeg-dev openjpeg-dev \
		openjpeg-tools tiff-dev lcms2-dev \
		cairo-dev motif-dev libxml2-dev libpng-dev zlib-dev pkgconf
fi

# Getting grobid sources and its configuration directory
cd /tmp/grobid_install/grobid
./gradlew --no-daemon shadowJar

# Copy the grobid-home dir
cp -dpTr grobid-home "${GROBID_HOME}"
sed -i 's#^grobid.temp.path=.*#grobid.temp.path=/tmp/grobid-tmp#' "${GROBID_HOME}/config/grobid.properties"

# Last, copy JARs to destination, while creating their invocation wrappers
mkdir -p "$JARSDIR"
for targ in grobid-core grobid-trainer ; do
	cp -p "${targ}/build/libs/${targ}-${GROBID_VERSION}-onejar.jar" "$JARSDIR"
	cat > /usr/local/bin/"${targ}" <<EOF
#!/bin/sh
exec java -jar "${JARSDIR}/${targ}-${GROBID_VERSION}-onejar.jar" -gH "${GROBID_HOME}" "\$@"
EOF
	chmod +x /usr/local/bin/"${targ}"
done

if [ -f /etc/alpine-release ] ; then
	# Removing not needed tools
	apk del openjdk8 git
	rm -rf /var/cache/apk/*
else
	apt-get remove openjdk-8-jdk git
	rm -rf /var/cache/dpkg
fi

# And the sources
rm -rf /root/.gradle /tmp/grobid_install /tmp/hsperfdata_root
