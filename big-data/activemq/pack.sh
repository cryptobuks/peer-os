#!/bin/sh
set -e
# Check if any deb file exists!!!

BASE=$(pwd)
sh -c 'cd $BASE'
echo $BASE
cd ../workspace
SOURCE=$(pwd)"/big-data/activemq/activemq"
TARGET="/var/lib/jenkins/Automation/Bigdata/activemq"
echo $SOURCE
echo $TARGET
cd $BASE

mv activemq.xml SSL/

if ls *.deb ; then
	rm  *.deb
fi



fileName=`ls | grep ksks | awk '{print $1}' | head -1`
echo "FILENAME: " $fileName

cp -a $SOURCE/DEBIAN/ $BASE/$fileName/
cp -a $SOURCE/etc/ $BASE/$fileName/ 
cp -a $SOURCE/opt $BASE/$fileName/

wget http://apache.bilkent.edu.tr/activemq/5.9.1/apache-activemq-5.9.1-bin.tar.gz -P $fileName/opt/
cd $fileName/opt
tar xzvf apache-activemq-5.9.1-bin.tar.gz
rm apache-activemq-5.9.1-bin.tar.gz
rm README
cd $BASE

#Read SSL Passwords for Activemq broker
cd $BASE/SSL
passwd=$(cat password.dat)
echo "SSL Password is: " $passwd
sleep 2
#Change activemq.xml
sed -i '/keyStore=.*/c\          keyStore="/opt/apache-activemq-5.9.1/conf/broker.ks" keyStorePassword= "'$passwd'"\' activemq.xml
sed -i '/trustStore=.*/c\          trustStore="/opt/apache-activemq-5.9.1/conf/broker.ts" trustStorePassword= "'$passwd'"/>\' activemq.xml

#Deleting and copied files
mv $BASE/SSL/activemq.xml $BASE/$fileName/opt/apache-activemq-5.9.1/conf/
cd $BASE/$fileName/opt/apache-activemq-5.9.1/conf/
rm broker*
rm client*
cd $BASE

lineNumberVersion=$(sed -n '/Version:/=' $fileName/DEBIAN/control)
lineNumberPackage=$(sed -n '/Package:/=' $fileName/DEBIAN/control)
lineVersion=$(sed d$lineNumberVersion!d $fileName/DEBIAN/control)
linePackage=$(sed $lineNumberPackage!d $fileName/DEBIAN/control)

version=$(echo $lineVersion | awk -F":" '{split($2,a," ");print a[1]}')
package=$(echo $linePackage | awk -F":" '{split($2,a," ");print a[1]}')
echo $version
echo $package

versionFirst=$(echo $version | awk -F"." '{print $1}')
versionSecond=$(echo $version | awk -F"." '{print $2}')
versionThird=$(echo $version | awk -F"." '{print $3}')

#updatedVersion=$(echo `expr $versionThird + 1`)
updatedRelease=$versionFirst.$versionSecond.$versionThird
replaceVersion="Version: $updatedRelease"
sed -i $fileName/DEBIAN/control -e $lineNumberVersion's!.*!'"$replaceVersion"'!'
packageName=$package-$updatedRelease"-amd64"

if [ "$fileName" != "$packageName" ] ;then
echo "different!!"
mv $fileName $packageName
fi

find ./$packageName -name "*~" -print0 | xargs -0 rm -rf
rm $packageName/DEBIAN/md5sums
md5sum `find ./$packageName -type f | awk '/.\//{ print substr($0, 3) }'` >> $packageName/DEBIAN/md5sums
dpkg-deb -z8 -Zgzip --build $packageName/
cp $packageName".deb" $TARGET/
