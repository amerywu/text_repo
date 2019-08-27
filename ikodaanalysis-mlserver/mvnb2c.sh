#!/bin/bash

echo "Build, deploy and run MLServer ##b2c## on Spark?"
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
rm /home/jake/project/__workspace/ikodaanalysis-mlserver/mlserverb2c.sh
rm /home/jake/project/__workspace/ikodaanalysis-mlserver/src/main/resources/application.properties


cp /home/jake/project/__workspace/scalaProjects/ikodaML/customResources/b2c/streaming.conf /home/jake/project/__workspace/scalaProjects/ikodaML/src/main/resources/streaming.conf
cp /home/jake/project/__workspace/scalaProjects/ikodaML/customResources/b2c/aml.properties /home/jake/project/__workspace/scalaProjects/ikodaML/src/main/resources/aml.properties
cp /home/jake/project/__workspace/ikodaanalysis-mlserver/customResources/b2c/mlserverb2c.sh /home/jake/project/__workspace/ikodaanalysis-mlserver/mlserverb2c.sh
cp /home/jake/project/__workspace/ikodaanalysis-mlserver/customResources/b2c/application.properties /home/jake/project/__workspace/ikodaanalysis-mlserver/src/main/resources/application.properties

./mvns.sh
echo "Deploy b2c?"
read doinstall
if [ "$doinstall" != "$yes" ] 
then

     exit 1
else

     echo "Deploying...."
fi

echo copying assembly jar
rm -rf /home/jake/environment/spark-2.2.1-bin-hadoop2.7/ikoda/mlserver-b2c-0.1.0.jar
rm -rf /home/jake/environment/spark-2.2.1-bin-hadoop2.7/ikoda/b2clog4j.properties
rm -rf /home/jake/environment/spark-2.2.1-bin-hadoop2.7/mlserverb2c.sh
rm -rf /home/jake/environment/spark-2.2.1-bin-hadoop2.7/ikoda/b2crun.conf

cp   /home/jake/project/__workspace/ikodaanalysis-mlserver/target/ikodaanalysis-mlserver-0.1.0.jar /home/jake/environment/spark-2.2.1-bin-hadoop2.7/ikoda//mlserver-b2c-0.1.0.jar
cp   /home/jake/project/__workspace/ikodaanalysis-mlserver/mlserverb2c.sh /home/jake/environment/spark-2.2.1-bin-hadoop2.7/mlserverb2c.sh                                                                                                                                                            
cp   /home/jake/project/__workspace/ikodaanalysis-mlserver/customResources/b2c/b2clog4j.properties /home/jake/environment/spark-2.2.1-bin-hadoop2.7/ikoda/b2clog4j.properties
cp   /home/jake/project/__workspace/ikodaanalysis-mlserver/customResources/b2c/b2crun.conf /home/jake/environment/spark-2.2.1-bin-hadoop2.7/ikoda/b2crun.conf
chmod +x /home/jake/environment/spark-2.2.1-bin-hadoop2.7/mlserverb2c.sh

echo "Run mlserver??"
read runpipe
if [ "$runpipe" != "$yes" ] 
then
     exit 1
else
     echo "Going to spark and calling mlserverb2c"
fi


echo calling mlserverb2c
cd /home/jake/environment/spark-2.2.1-bin-hadoop2.7

./mlserverb2c.sh