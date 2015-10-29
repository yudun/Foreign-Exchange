This project is a modification of our previous Random Forest.
I modify it so as to read data from local Cassandra databases rather than raw file.
I created three tables: trainingData, testData and performanceMetrics, here are their schemes:

create table trainingData ( time timestamp,
						feature varchar,
						primary key (time));

create table testData ( time timestamp,
						feature varchar,
						primary key (time));

The 'feature' column in both tables actually includes both feature and label,
it is simple a row in our data matrix.

create table performanceMetrics ( name varchar,
						value double,
						primary key (name));

This table will store the result of my learning algorithm, and here is how does it look like:
cqlsh:financialdata> select * from performancemetrics ;

			 name          | value
			---------------+----------
			  testDataSize |     6050
				  accuracy | 0.847438
			  truePositive |     5127
			  trueNegative |        0
			 falsePositive |      923
			 falseNegative |        0




My source code including:

1. RandomForestBuilder.java
	This is the entry for our Random Forest Program.
	We build multiple trees, running test data on it and then persist it to disk.
	And finally we deserialize it from disk and test on it again to see if we dump it correctly.

2. PreDecisionTree.java
	This calss describe the data structure for each node in the tree.
	The function split() performs the branching process.

3. ConfigDataSingleton.java
	This is a singleton which contains all the configuration for building our tree.
	Like the path of source data file.

4. DataInstance.java
	This is a internal data structure for each data instance.

5. RandomForest.java
	This class describe how we can interact with this random forest, like adding a tree to it.
	We also implement method to serialize and deserialize a forest to and from disk files.