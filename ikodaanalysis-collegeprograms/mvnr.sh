#!/bin/bash

echo "Build, deploy icp?"




read go
yes="y"
if [ "$go" != "$yes" ]; then

     exit 1
else
     echo "Building ..."
fi

cd ../ikoda-utils

echo $PWD

echo mvn
mvn clean install

echo Did utils build successfully?
read go1
yes="y"
if [ "$go1" != "$yes" ]; then

     exit 1
else
     echo "Next ..."
fi

cd ../text

echo $PWD

echo mvn
mvn clean install



echo "Did text complete successfully?"
read doinstall

if [ "$doinstall" != "$yes" ] 
then

     exit 1
else

     echo "Next...."
fi

cd ../ikodaanalysis-collegeprograms
echo $PWD

echo mvn
mvn clean install

echo "Did build complete successfully?"
read doinstall1

if [ "$doinstall1" != "$yes" ] 
then

     exit 1
else

     echo "Deploying...."
fi



echo copying assembly jar
cp   /home/jake/project/__workspace/ikodaanalysis-collegeprograms/ikodaanalysis-collegeprograms-manager/target/ikodaanalysis-collegeprograms-manager-0.0.1-SNAPSHOT.jar /home/jake/ikodaApps/icpapp/target/ikodaanalysis-collegeprograms-manager-0.0.1-SNAPSHOT.jar


echo "Run icp"
read runpipe
if [ "$runpipe" != "$yes" ] 
then

     exit 1
else

     echo "Starting jd"
fi



cd  /home/jake/ikodaApps/icpapp
echo date +"%T"
./launchicp.sh
