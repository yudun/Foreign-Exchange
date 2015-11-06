/**
 * Created by yudun on 15/11/5.
 */

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.google.common.base.Joiner;
import com.google.gson.Gson;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.LineRecordReader;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;
import java.util.*;

public class MapReduceRandomForest {
    /** Helper Functions for mapper to generate a tree in the Random Forest **/

    // randomly select m (m<=n) distinct number from 0 ~ n-1;
    private static ArrayList<Integer> randomSelectNum(int n, int m){
        ArrayList<Integer> source = new ArrayList<>(n);

        for(int i=0; i<n; i++){
            source.add(i);
        }

        Collections.shuffle(source);

        return new ArrayList<Integer>(source.subList(0, m));
    }


    // get the prediction value from given features
    public static int getPredition(PreDecisionTree T, DataInstance dt) {
        while(true) {

            if(T.level == -1) {
                return T.label;
            } else {
                if (dt.feature[T.featureIdx] == 0){
                    T = T.left;
                }
                else {
                    T = T.right;
                }
            }
        }
    }


    /** The mapper -- each builds a decision tree from data sampled from Cassandra, and out put as json Object
     *  We ignore the key and value, and read the real input data using session to connect to Canssandra
     * **/

    public static class RandomForestBuilderMapper
            extends Mapper<Object, Text, Text, Text> {
        // a dummmy key for each out put json
        private Text word = new Text("key");

        public void map(Object key, Text value, Context context)
                throws IOException, InterruptedException {

            ConfigDataSingleton config = ConfigDataSingleton.getInstance();

            // This is the input for this mapper
            ArrayList<DataInstance> dataSet = new ArrayList<>();
            // create Cassandra cluster and session
            Cluster cluster;
            // Connect to the cluster and keyspace
            cluster = Cluster.builder().addContactPoint(config.CONTACT_POINT).build();
            config.session = cluster.connect(config.KEYSPACE);
            ArrayList<DataInstance> trainingSet = new ArrayList<>();
            ResultSet results = config.session.execute("SELECT * FROM " + config.TRAINING_DATA_TABLENAME);
            for (Row row : results) {
                String line = row.getDate("time") + "\t" + row.getString("feature");
                dataSet.add(new DataInstance(line));
            }

            // shuffle the dataset from Cassandra to make sure each mapper create different trees
            Collections.shuffle(dataSet);

            String resultJsonTree = null;
            String[] inputString = value.toString().split("\n");


            // This is the bootstrap dataset for this tree
            ArrayList<DataInstance> trainingSubSet = new ArrayList<>();
            // This is the rest 1/3 test data set for this tree
            ArrayList<DataInstance> testSubSet = new ArrayList<>();

            int trainingSize = dataSet.size();
            int subSetSize = (int)(trainingSize * ConfigDataSingleton.TRAINING_SUBSET_RATIO);

            for (int i = 0; i < subSetSize; i++) {
                trainingSubSet.add(dataSet.get(i));
            }

            for (int i = subSetSize; i < trainingSize; i++){
                testSubSet.add(dataSet.get(i));
            }


            // This is the random subspace for this tree
            ArrayList<Integer> featureSubspace =
                    randomSelectNum(ConfigDataSingleton.FEATURE_NUM, ConfigDataSingleton.SUBSPACE_DIMENSION);

            System.out.println(featureSubspace);


            // create a queue for training this tree from top down
            Queue<PreDecisionTree> queue = new LinkedList<>();
            PreDecisionTree T = new PreDecisionTree(0, -1, -1, featureSubspace);
            T.data = trainingSubSet;
            queue.add(T);

            // Training this tree from branching the root
            while(queue.size() > 0) {
                PreDecisionTree currentT = queue.remove();
                currentT.split(queue);
            }


            // Use the rest 1/3 data to get its accuracy
            int countCorrect = 0;
            for (DataInstance dt : testSubSet) {
                if (dt.label == getPredition(T, dt)) {
                    countCorrect++;
                }
            }

            T.accuracy = 1.0 * countCorrect /testSubSet.size();

            // Transfer the result tree to a json string
            Gson gson = new Gson();
            resultJsonTree = gson.toJson(T);

            Text outputValue = new Text();
            outputValue.set(resultJsonTree + "\n");
            context.write(word, outputValue);
        }
    }

    /** The reducer -- The one reducer that collect all trees into a JSON array **/

    public static class RandomForestBuilderReducer
            extends Reducer<Text,Text,Text,Text> {
        private IntWritable result = new IntWritable();

        public void reduce(Text key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {

            String resultRandomForest = "[ ";
            String[] inputString = values.toString().split("\n");

            resultRandomForest += Joiner.on(", ").join(inputString) + " ]";

            Text outputValue = new Text();
            outputValue.set(resultRandomForest);

            // Out put the random forest as a json array of json trees
            context.write(new Text(), outputValue);
        }
    }

    /** A customized inputFormat for us to create TREENUM mappers **/
    public class CanssandraInputFormat extends FileInputFormat<LongWritable, Text> {
        @Override
        public RecordReader<LongWritable, Text> createRecordReader(
                InputSplit split, TaskAttemptContext context)
                throws IOException {
            context.setStatus(split.toString());
            return new LineRecordReader();
        }

        @Override
        public List<InputSplit> getSplits(JobContext job) throws IOException {
            List<InputSplit> splits = new ArrayList<InputSplit>();

            // make a fake split so that we can read the true split in TREENUM mappers
            for (int i = 0; i < ConfigDataSingleton.TREENUM; i++) {
                splits.add(new InputSplit() {
                    @Override
                    public long getLength() throws IOException, InterruptedException {
                        return 0;
                    }

                    @Override
                    public String[] getLocations() throws IOException, InterruptedException {
                        return new String[0];
                    }
                });
            }
            return splits;
        }

    }


    /** The config for our MapReduce **/

    public static void main(String[] args) throws Exception {
        ConfigDataSingleton config  = ConfigDataSingleton.getInstance();


        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "MapReduceRandomForest");
        job.setJarByClass(MapReduceRandomForest.class);

        job.setMapperClass(RandomForestBuilderMapper.class);
        job.setCombinerClass(RandomForestBuilderReducer.class);
        job.setReducerClass(RandomForestBuilderReducer.class);

        // customized inputFormat
        job.setInputFormatClass(CanssandraInputFormat.class);

        // we only need 1 reducer to combine all the trees
        job.setNumReduceTasks(1);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        FileOutputFormat.setOutputPath(job, new Path(config.mapreduceOutputFile));

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
