/**
 * Created by Shimin Wang (andrewid: shiminw) on 15/11/19.
 */

import com.datastax.driver.core.{Cluster, ResultSet, Row, Session}
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.tree.RandomForest
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}


/** Set up the context and configuration for Spark and Cassandra **/
var config: ConfigDataSingleton  = ConfigDataSingleton.getInstance()
val conf = new SparkConf()
  .setMaster(config.masterURL)
  .setAppName(config.appName)

val sc = new SparkContext(conf)

// create Cassandra cluster and session
var cluster : Cluster = null
var session : Session = null

// Connect to the cluster and keyspace
cluster = Cluster.builder().addContactPoint(config.CONTACT_POINT).build()
session = cluster.connect(config.KEYSPACE)


/** The RDD data points we are going to populate from Cassandra **/
var results : ResultSet = session.execute("SELECT * FROM " + config.TRAINING_DATA_TABLENAME)
var rawdata: List[LabeledPoint] = List()

for ( row : Row <- results) {
  var line : Array[Double] = row.getString("feature").split(" ").map((s: String) => s.toDouble)
  var label : Double = line(line.length-1)

  var feature : Array[Double] = line.take(line.length - 1)

  val point = LabeledPoint(label, Vectors.dense(feature))

  // append the rawdata
  rawdata = rawdata :+ point
}

// cast the raw data to RDD[LabeledPoint]
var data : RDD[LabeledPoint] = sc.parallelize(rawdata)

// Split the data into training and test sets (30% held out for testing)
val splits = data.randomSplit(Array(config.TRAINING_SUBSET_RATIO, 1-config.TRAINING_SUBSET_RATIO))

val (trainingData, testData) = (splits(0), splits(1))


/** Train a RandomForest model. **/
//  Empty categoricalFeaturesInfo indicates all features are continuous.
val numClasses = 2
val categoricalFeaturesInfo = Map[Int, Int]()
val numTrees = config.TREENUM // Use more in practice.
val featureSubsetStrategy = "auto" // Let the algorithm choose.
val impurity = "gini"
val maxDepth = 4
val maxBins = 32

val model = RandomForest.trainClassifier(trainingData, numClasses, categoricalFeaturesInfo,
  numTrees, featureSubsetStrategy, impurity, maxDepth, maxBins)

// Evaluate model on test instances and compute test error
val labelAndPreds = testData.map { point =>
  val prediction = model.predict(point.features)
  (point.label, prediction)
}


/** Print out the Performance metrics  **/
val accuracy = labelAndPreds.filter(r => r._1 == r._2 ).count.toDouble / testData.count()
println("Test accuracy = " + accuracy)

