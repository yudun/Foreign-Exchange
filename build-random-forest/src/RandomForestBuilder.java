import java.io.*;
import java.util.*;

/**
 * Created by Shimin Wang (andrewid: shiminw) on 15/9/27.
 */

public class RandomForestBuilder {


    // randomly select m (m<=n) distinct number from 0 ~ n-1;
    private static ArrayList<Integer> randomSelectNum(int n, int m){
        ArrayList<Integer> source = new ArrayList<>(n);

        for(int i=0; i<n; i++){
            source.add(i);
        }

        Collections.shuffle(source);

        return new ArrayList<Integer>(source.subList(0, m));
    }


    // split the training set into training subset and testSubset
    private static void splitTrainingSet(ArrayList<DataInstance> trainingSet,
                           ArrayList<DataInstance> trainingSubSet,
                           ArrayList<DataInstance> testSubSet){

        int trainingSize = trainingSet.size();
        int subSetSize = (int)(trainingSize * ConfigDataSingleton.TRAINING_SUBSET_RATIO);

        Collections.shuffle(trainingSet);

        for (int i = 0; i < subSetSize; i++) {
            trainingSubSet.add(trainingSet.get(i));
        }


        for (int i = subSetSize; i < trainingSize; i++){
            testSubSet.add(trainingSet.get(i));
        }

    }

    private static int getMode(ArrayList<Integer> votes){
        votes.sort((x,y) -> x-y);
        int result = 0;
        int maxcount = 0;
        int count = 0;
        int last = votes.get(0);

        for (Integer i : votes){
            if (i != last) {
                if (count > maxcount) {
                    result = last;
                    maxcount = count;
                }
                count = 1;
            }
            else {
                count ++;
            }
            last = i;
        }

        if (count > maxcount)
            result = last;

        return result;
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

    public static void main(String [] args) {
        ConfigDataSingleton config  = ConfigDataSingleton.getInstance();

        /** Get Training data **/
        ArrayList<DataInstance> trainingSet = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(ConfigDataSingleton.TRAINING_DATA_LOCATION))) {
            String line;
            while ((line = br.readLine()) != null) {
                trainingSet.add(new DataInstance(line));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Totally " + trainingSet.size() + " training instances read.");



        /** Begin Training **/
        RandomForest forest = new RandomForest(ConfigDataSingleton.TREENUM);
        // train N trees one by one
        for (int i = 0; i < forest.getNumberOfTrees(); i++) {
            // This is the bootstrap dataset for this tree
            ArrayList<DataInstance> trainingSubSet = new ArrayList<>();
            // This is the rest 1/3 test data set for this tree
            ArrayList<DataInstance> testSubSet = new ArrayList<>();
            // create the bootstrap dataset and the rest 1/3 test data set
            splitTrainingSet(trainingSet, trainingSubSet, testSubSet);

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

            forest.addTree(T);

            System.out.println("Training for the " + (i+1) + "th tree is done. Its accuracy is "
                    + (T.accuracy)*100 + "%");
        }


        /** Get Test data **/
        ArrayList<DataInstance> testSet = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(ConfigDataSingleton.TEST_DATA_LOCATION))) {
            String line;
            while ((line = br.readLine()) != null) {
                testSet.add(new DataInstance(line));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Totally " + testSet.size() + " test instances read.");



        /** Begin Test **/
        int countCorrect = 0;
        ArrayList<Integer> votes = new ArrayList<>();
        // For each test instance, get a list of votes form our trees
        for (DataInstance dt : testSet) {
            votes.clear();
            for (int i = 0; i < ConfigDataSingleton.TREENUM; i++) {
                PreDecisionTree T = forest.getTree(i);
                votes.add(getPredition(T, dt));
            }

            if (dt.label == getMode(votes)) {
                countCorrect++;
            }
        }

        System.out.println("The accuracy of our forest is  "
                + (1.0 * countCorrect / testSet.size()) * 100 + "%");

        int count1 = 0;
        for (DataInstance dt : testSet) {
            if (dt.label == 1) {
                count1++;
            }
        }
        System.out.println("The percentage of label value 1 in training set is "
                + (1.0 * count1 / testSet.size()) * 100 + "%");

        System.out.println();
        System.out.println();
        System.out.println();


        /** Persist this forest for later use **/
        // We delete all files in target directory first
        File[] files = new File(ConfigDataSingleton.SERILIZED_DIR_LOCATION).listFiles();
        for (File f: files)
            f.delete();
        // dump the forest to disk
        forest.dump(ConfigDataSingleton.SERILIZED_DIR_LOCATION, ConfigDataSingleton.SERILIZED_FILE_NAME_PREFIX);



        /** Deserialize the forest from files **/
        forest.clear();
        forest.readFromFile(ConfigDataSingleton.SERILIZED_DIR_LOCATION);



        /** Begin Test On Deserialized Forest**/
        countCorrect = 0;
        // For each test instance, get a list of votes form our trees
        for (DataInstance dt : testSet) {
            votes.clear();
            for (int i = 0; i < ConfigDataSingleton.TREENUM; i++) {
                PreDecisionTree T = forest.getTree(i);
                votes.add(getPredition(T, dt));
            }
            if (dt.label == getMode(votes)) {
                countCorrect++;
            }
        }

        System.out.println("The accuracy of our forest (Deserialized) is  "
                + (1.0 * countCorrect / testSet.size()) * 100 + "%");
    }

}