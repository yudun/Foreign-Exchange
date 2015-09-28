import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by Shimin Wang (andrewid: shiminw) on 15/9/27.
 */

public class DecisionTreeBuilder {
    static int maxDepth = 0;

    public static int getPredition(PreDecisionTree T, DataInstance dt) {
        while(true) {
            if(maxDepth < T.level)
                maxDepth = T.level;

            if(T.level == -1) {
                return T.label;
            } else {
                if (dt.feature[T.featureIdx] == 0)
                    T = T.left;
                else
                    T = T.right;
            }
        }
    }

    public static void dumpModel(PreDecisionTree T, String filename){

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
        // create a queue for training this tree from top down
        Queue<PreDecisionTree> queue = new LinkedList<>();

        PreDecisionTree T = new PreDecisionTree(0, -1, -1);
        T.data = trainingSet;
        queue.add(T);

        while(queue.size() > 0) {
            PreDecisionTree currentT = queue.remove();
            currentT.split(queue);
        }
        System.out.println("Training is done. The max depth is: " + maxDepth);

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
        double size = testSet.size();
        for(DataInstance dt : testSet) {
            if(dt.label == getPredition(T, dt)) {
                countCorrect ++;
            }
        }

        T.accuracy = countCorrect / size;
        System.out.println("The accuracy of our decision tree is " + T.accuracy * 100 + "%");

        dumpModel(T, "decisionTree");

    }

}