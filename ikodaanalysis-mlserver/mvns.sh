#!/bin/bash

echo "Config copied?




read go1
yes="y"
if [ "$go1" != "$yes" ]; then

     exit 1
else
     echo "Building ikodaSparse..."
fi


echo mvn ikodaSparse
cd /home/jake/project/__workspace/scalaProjects/ikodaSparse/
echo $PWD
mvn clean install

echo did ikodaSparse build successfully?
read ikssuccess

if [ "$ikssuccess" != "$yes" ]; then

     exit 1
else
     echo "Building ikodaML..."
fi

#######################################
echo "Build ikodaML?"

echo $PWD


echo mvn ikodaML
cd /home/jake/project/__workspace/scalaProjects/ikodaML/
mvn clean install

echo did ikodaML build successfully?
read smlsuccess

if [ "$smlsuccess" != "$yes" ]; then

     exit 1
else
     echo "Building MLServer..."
fi

#######################################
cd /home/jake/project/__workspace/ikodaanalysis-mlserver/

echo mvn
mvn clean install

echo "Did build complete successfully?"
read doinstall

