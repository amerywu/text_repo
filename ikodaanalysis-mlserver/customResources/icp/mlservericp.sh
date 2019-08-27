nohup ./bin/spark-submit   --jars ./ikoda/extrajars/ikoda_assembled_ml_nlp.jar,./ikoda/extrajars/stanford-corenlp-3.8.0.jar,./ikoda/extrajars/stanford-parser-3.8.0.jar \
--packages com.datastax.spark:spark-cassandra-connector_2.11:2.0.9 \
--class ikoda.mlserver.Application \
--driver-java-options "-Dlog4j.debug=true -Dlog4j.configuration=file:///home/jake/environment/spark-2.2.1-bin-hadoop2.7/ikoda/icplog4j.properties" \
--conf spark.cassandra.connection.host=192.168.0.34 \
--conf spark.cores.max=6 \
--master spark://192.168.0.141:7077  ./ikoda/mlserver-icp-0.1.0.jar   1000  > ./logs/icpnohup.out &