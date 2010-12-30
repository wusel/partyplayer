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

cp picotask-gui/target/picotask.jar installer/staging
cp -r picotask-gui/target/lib installer/staging/
cd installer
cp -r config/* staging/

cd staging

$IZPACK_COMPILE_HOME/compile install.xml -b . -o ../picotask-installer.jar -k standard
chmod u+x ../picotask-installer.jar


