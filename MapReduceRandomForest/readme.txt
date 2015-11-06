This is the map reduce version of my previous Random Forest.
I create a customized inputformat class to assign the number of mapper as TREENUM.
And within each mapper, we connect to the Cassandra Database to get the data set and make a random sample.

The reducer will simply combines all the stringify json decision trees together into a json array.
I save this result json array in file "randomForest.json"


My source code including:

1. MapReduceRandomForestBuilder.java
	This is the where our map reduce happens.
	We build a decision tree using sampled data from the Cansandra database in each mapper.
	And we combine all the stringify json decision tree together to a json array in the reducer.

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