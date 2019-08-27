#!/bin/bash

echo "Build, deploy and run MLServer +++JOB DESCRIPTIONS+++ on Spark?"
echo "1. ikodaSparse, 2. ikodaML, 3. MLServer"



read go
yes="y"
if [ "$go" != "$yes" ]; then

     exit 1
else
     echo "Building ikodaSparse..."
fi


echo setting up config files

rm /home/jake/project/__workspace/scalaProjects/ikodaML/src/main/resources/streaming.conf
rm /home/jake/project/__workspace/scalaProjects/ikodaML/src/main/resources/aml.properties
rm /home/jake/project/__workspace/ikodaanalysis-mlserver/src/main/resources/application.properties
rm /home/jake/project/__workspace/ikodaanalysis-mlserver/mlserverjd.sh
cp /home/jake/project/__workspace/scalaProjects/ikodaML/customResources/jd/streaming.conf /home/jake/project/__workspace/scalaProjects/ikodaML/src/main/resources/streaming.conf
cp /home/jake/project/__workspace/scalaProjects/ikodaML/customResources/jd/aml.properties /home/jake/project/__workspace/scalaProjects/ikodaML/src/main/resources/aml.properties
cp /home/jake/project/__workspace/ikodaanalysis-mlserver/customResources/jd/mlserverjd.sh /home/jake/project/__workspace/ikodaanalysis-mlserver/mlserverjd.sh
cp /home/jake/project/__workspace/ikodaanalysis-mlserver/customResources/jd/application.properties /home/jake/project/__workspace/ikodaanalysis-mlserver/src/main/resources/application.properties

./mvns.sh
echo "Deploy JD?"
read doinstall
if [ "$doinstall" != "$yes" ] 
then

     exit 1
else

     echo "Deploying...."
fi

echo copying assembly jar
rm -rf /home/jake/environment/spark-2.2.1-bin-hadoop2.7/ikoda/mlserver-jd-0.1.0.jar
rm -rf /home/jake/environment/spark-2.2.1-bin-hadoop2.7/mlserverjd.sh
cp   /home/jake/project/__workspace/ikodaanalysis-mlserver/target/ikodaanalysis-mlserver-0.1.0.jar /home/jake/environment/spark-2.2.1-bin-hadoop2.7/ikoda//mlserver-jd-0.1.0.jar
                                                                                                                                                            
cp   /home/jake/project/__workspace/ikodaanalysis-mlserver/mlserverjd.sh /home/jake/environment/spark-2.2.1-bin-hadoop2.7/mlserverjd.sh
chmod +x /home/jake/environment/spark-2.2.1-bin-hadoop2.7/mlserverjd.sh

echo "Run mlserver??"
read runpipe
if [ "$runpipe" != "$yes" ] 
then
     exit 1
else
     echo "Going to spark and calling mlserverjd"
fi


echo calling mlserverjd
cd /home/jake/environment/spark-2.2.1-bin-hadoop2.7

./mlserverjd.sh