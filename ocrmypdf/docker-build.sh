#!/bin/sh

POPPLER_VERSION=0.73.0
POPPLER_DATA_VERSION=0.4.9

# Exit on error
set -e

if [ $# -ge 2 ] ; then
	POPPLER_VERSION="$1"
	POPPLER_DATA_VERSION="$2"
fi

apk update

# Installing tesseract 4.0
apk add --update tesseract-ocr 

# Needed packages for building poppler-utils
apk add cmake make gcc g++ freetype freetype-dev \
	fontconfig fontconfig-dev jpeg jpeg-dev openjpeg openjpeg-dev \
	openjpeg-tools tiff tiff-dev lcms2 lcms2-dev \
	cairo cairo-dev libxml2 libxml2-dev pkgconf py-lxml qpdf qpdf-libs qpdf-dev ghostscript
#	ocrmypdf

#apk add py-pybind11 py3-pybind11

#apk --no-cache add --repository https://github.com/pybind/pybind11 py-pybind11

# As poppler-utils are too old, install them from source code
mkdir /tmp/poppler_install
cd /tmp/poppler_install
wget -q https://poppler.freedesktop.org/poppler-${POPPLER_VERSION}.tar.xz \
	https://poppler.freedesktop.org/poppler-data-${POPPLER_DATA_VERSION}.tar.gz

# Fix to locate in the proper place the installed libraries
ln -s lib /usr/local/lib64

# Fix to OpenJPEG cmake declaration, so its include dir is properly located
sed -i 's#}/../../include#}/../../../include#' /usr/lib/cmake/openjpeg-2.3/OpenJPEGConfig.cmake

# Installing poppler-data, needed by poppler
tar xf poppler-data-${POPPLER_DATA_VERSION}.tar.gz
cd poppler-data-${POPPLER_DATA_VERSION}

mkdir build
cd build
cmake .. -DCMAKE_BUILD_TYPE=release
make
make install

# Installing poppler
cd /tmp/poppler_install
tar xf poppler-${POPPLER_VERSION}.tar.xz
cd poppler-${POPPLER_VERSION}

mkdir build
cd build
cmake .. -DCMAKE_BUILD_TYPE=release -DWITH_NSS3=no -DENABLE_QT5=no
make
make install

# Install ocrmypdf
apk add python3
apk add python3-dev
cd /app
pip3 install --trusted-host pypi.python.org -r requirements.txt
pip3 install --upgrade pip
# Removing not needed tools
apk del cmake make gcc g++ freetype-dev fontconfig-dev jpeg-dev \
	openjpeg-dev openjpeg-tools tiff-dev lcms2-dev cairo-dev libxml2-dev \
	pkgconf

# And the sources
rm -rf /tmp/poppler_install /var/cache/apk/*