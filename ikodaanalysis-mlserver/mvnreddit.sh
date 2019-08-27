#!/bin/bash

echo "Build, deploy and run MLServer ##REDDIT## on Spark?"
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
rm /home/jake/project/__workspace/ikodaanalysis-mlserver/mlserverreddit.sh
rm /home/jake/project/__workspace/ikodaanalysis-mlserver/src/main/resources/application.properties


cp /home/jake/project/__workspace/scalaProjects/ikodaML/customResources/reddit/streaming.conf /home/jake/project/__workspace/scalaProjects/ikodaML/src/main/resources/streaming.conf
cp /home/jake/project/__workspace/scalaProjects/ikodaML/customResources/reddit/aml.properties /home/jake/project/__workspace/scalaProjects/ikodaML/src/main/resources/aml.properties
cp /home/jake/project/__workspace/ikodaanalysis-mlserver/customResources/reddit/mlserverreddit.sh /home/jake/project/__workspace/ikodaanalysis-mlserver/mlserverreddit.sh
cp /home/jake/project/__workspace/ikodaanalysis-mlserver/customResources/reddit/application.properties /home/jake/project/__workspace/ikodaanalysis-mlserver/src/main/resources/application.properties

./mvns.sh
echo "Deploy REDDIT?"
read doinstall
if [ "$doinstall" != "$yes" ] 
then

     exit 1
else

     echo "Deploying...."
fi

echo copying assembly jar
rm -rf /home/jake/environment/spark-2.2.1-bin-hadoop2.7/ikoda/mlserver-reddit-0.1.0.jar
rm -rf /home/jake/environment/spark-2.2.1-bin-hadoop2.7/ikoda/redditlog4j.properties
rm -rf /home/jake/environment/spark-2.2.1-bin-hadoop2.7/mlserverreddit.sh
cp   /home/jake/project/__workspace/ikodaanalysis-mlserver/target/ikodaanalysis-mlserver-0.1.0.jar /home/jake/environment/spark-2.2.1-bin-hadoop2.7/ikoda//mlserver-reddit-0.1.0.jar
cp   /home/jake/project/__workspace/ikodaanalysis-mlserver/mlserverreddit.sh /home/jake/environment/spark-2.2.1-bin-hadoop2.7/mlserverreddit.sh                                                                                                                                                            
cp   /home/jake/project/__workspace/ikodaanalysis-mlserver/customResources/reddit/redditlog4j.properties /home/jake/environment/spark-2.2.1-bin-hadoop2.7/ikoda/redditlog4j.properties
chmod +x /home/jake/environment/spark-2.2.1-bin-hadoop2.7/mlserverreddit.sh

echo "Run mlserver??"
read runpipe
if [ "$runpipe" != "$yes" ] 
then
     exit 1
else
     echo "Going to spark and calling mlserverreddit"
fi


echo calling mlserverreddit
cd /home/jake/environment/spark-2.2.1-bin-hadoop2.7

./mlserverreddit.sh