This is the Spark  version of my previous Random Forest.
Using the random forest estimator from Spark MLLib, I trained my previous data

The the Performance metrics of this trainer on my test data is

accuracy = 0.847438


My source code including:

1. RandomForestBuilder.scala
	This is the where our Spark work flow happens
	I build it based on the example from http://spark.apache.org/docs/latest/mllib-ensembles.html
	First I read data from Cassandra and than use the RandomForest API from MLLib to train a random forest.

3. ConfigDataSingleton.java
	This is a singleton which contains all the configuration for building our tree.
	Like the path of source data file and all the parameters required for Spark and Cassandra.
