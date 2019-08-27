#!/bin/bash

echo "Build, deploy and run MLServer **COLLEGE** on Spark?"
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
rm /home/jake/project/__workspace/ikodaanalysis-mlserver/mlservericp.sh
rm /home/jake/project/__workspace/ikodaanalysis-mlserver/src/main/resources/application.properties

cp /home/jake/project/__workspace/scalaProjects/ikodaML/customResources/icp/streaming.conf /home/jake/project/__workspace/scalaProjects/ikodaML/src/main/resources/streaming.conf
cp /home/jake/project/__workspace/scalaProjects/ikodaML/customResources/icp/aml.properties /home/jake/project/__workspace/scalaProjects/ikodaML/src/main/resources/aml.properties
cp /home/jake/project/__workspace/ikodaanalysis-mlserver/customResources/mlservericp.sh /home/jake/project/__workspace/ikodaanalysis-mlserver/mlservericp.sh
cp /home/jake/project/__workspace/ikodaanalysis-mlserver/customResources/application.properties /home/jake/project/__workspace/ikodaanalysis-mlserver/src/main/resources/application.properties

./mvns.sh
echo "Deploy COLLEGE?"
read doinstall
if [ "$doinstall" != "$yes" ] 
then
     exit 1
else
     echo "Deploying...."
fi

echo copying assembly jar
rm -rf /home/jake/environment/spark-2.2.1-bin-hadoop2.7/ikoda/mlserver-icp-0.1.0.jar
rm -rf /home/jake/environment/spark-2.2.1-bin-hadoop2.7/mlservericp.sh
cp   /home/jake/project/__workspace/ikodaanalysis-mlserver/target/ikodaanalysis-mlserver-0.1.0.jar /home/jake/environment/spark-2.2.1-bin-hadoop2.7/ikoda//mlserver-icp-0.1.0.jar
                                                                                                                                                            
cp   /home/jake/project/__workspace/ikodaanalysis-mlserver/mlservericp.sh /home/jake/environment/spark-2.2.1-bin-hadoop2.7/mlservericp.sh
chmod +x /home/jake/environment/spark-2.2.1-bin-hadoop2.7/mlservericp.sh

echo "Run mlserver??"
read runpipe
if [ "$runpipe" != "$yes" ] 
then
     exit 1
else
     echo "Going to spark and calling mlservericp"
fi


echo calling mlservericp
cd /home/jake/environment/spark-2.2.1-bin-hadoop2.7

./mlservericp.sh