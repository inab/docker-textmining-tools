#!/bin/sh

BASEDIR=/usr/local
GNORMPLUS_HOME="${BASEDIR}/share/gnormplus/"

GNORMPLUS_GATE_WRAPPER_VERSION=1.0

# Exit on error
set -e

if [ $# -ge 1 ] ; then
	GNORMPLUS_GATE_WRAPPER_VERSION="$1"
fi

apt-get update

apt-get install -y openjdk-8-jre 
	
# The development dependencies
apt-get install -y openjdk-8-jdk git maven make g++

apt-get install -y wget

apt-get install -y unzip

#Download GNormPlus
echo "Download GNormPlus ..."
wget https://www.ncbi.nlm.nih.gov/CBBresearch/Lu/Demo/tmTools/download/GNormPlus/GNormPlusJava.zip

echo "Unzip GNormPlus ..."
unzip GNormPlusJava.zip
echo "Copy to home directory..."
cp -r ${GNORMPLUS_HOME}GNormPlusJava/. ${GNORMPLUS_HOME}
echo "Delete Temporal ..."
rm -r ${GNORMPLUS_HOME}GNormPlusJava

echo "Download CRF++-0.58.tar.gz  ..."
wget -O CRF++-0.58.tar.gz "https://drive.google.com/uc?export=download&id=0B4y35FiV1wh7QVR6VXJ5dWExSTQ"
tar -zxvf CRF++-0.58.tar.gz
echo "Copy from 0.58 to CRF ..."
cp CRF++-0.58/. CRF/ -R
rm CRF++-0.58 -R
cd CRF
echo "Configure CRF ..."
sh ./configure
echo "Install ...."
make
su
make install
echo "Install CRF END"

chmod u=rwx,g=rwx,o=rwx ${GNORMPLUS_HOME} -R

#The GnormPlus project is not a maven project, some of the libraries that includes are not available in the mavens reporsitory or 
#the version is not clear.  Installation of the system path libs to maven repository m2
cd ..
mvn install:install-file -Dfile=GNormPlus.jar -DgroupId=gnormplus.com -DartifactId=gnormplus_thirdparty -Dversion=1.0 -Dpackaging=jar

mvn clean install -DskipTests

#rename jar
mv target/gnormplus-gate-wrapper-0.0.1-SNAPSHOT-jar-with-dependencies.jar gnormplus-gate-wrapper-${GNORMPLUS_GATE_WRAPPER_VERSION}.jar

cat > /usr/local/bin/gnormplus-gate-wrapper <<EOF
#!/bin/sh
exec java \$JAVA_OPTS -jar "${GNORMPLUS_HOME}/gnormplus-gate-wrapper-${GNORMPLUS_GATE_WRAPPER_VERSION}.jar" -workdir "${GNORMPLUS_HOME}" "\$@"
EOF
chmod +x /usr/local/bin/gnormplus-gate-wrapper

#delete unnecesary files
rm -R target src pom.xml

apt-get remove -y openjdk-8-jdk git maven wget unzip
rm -rf /var/cache/dpkg
echo "FINISH"

