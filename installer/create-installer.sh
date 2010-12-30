#!/bin/bash
IZPACK_COMPILE_HOME=../IzPack/bin
#1. clean and rebuild project
if [ -e staging ]
then
   echo "delete old staging area"
   rm -r staging
fi

mkdir staging

cd ..
mvn clean install

cp target/partyPlayer.jar installer/staging
cp -r target/lib installer/staging/
cp -r target/bin installer/staging/
cp -r licenses installer/staging/

cd installer
cp -r config/* staging/

cd staging

$IZPACK_COMPILE_HOME/compile install.xml -b . -o ../partyplayer-installer.jar -k standard
chmod u+x ../partyplayer-installer.jar


