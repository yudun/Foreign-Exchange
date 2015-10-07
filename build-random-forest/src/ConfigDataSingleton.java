import java.io.File;
import java.util.ArrayList;

/**
 * Created by Shimin Wang (andrewid: shiminw) on 15/9/27.
 *
 * This is a Singleton records configuration for building our decision tree
 */
public class ConfigDataSingleton {

    private static volatile ConfigDataSingleton instance = null;

    // the purity (0~1) when we decided to stop branching the decision tree
    // set to 1 to indicate that we want it to be as pure as possible
    public static double  PURITY_THREHOLD = 1;

    // We don't need features listed in this list
    public String[] REMOVE_FEATURES = {};

    // We choose the directionality of the bid of EURUSD 10 minutes later as the LABEL
    public static String LABEL = "EURUSD-2015-01.csv";

    // define the location of our original data
    public static String INPUT_DATA_DIR = "../../lab1/alignedData";

    // define the path of the training set
    public static String TRAINING_DATA_LOCATION = "../../lab2/trainingSet";

    // define the path of the test set
    public static String TEST_DATA_LOCATION = "../../lab2/testSet";

    // define the dir path where we will save our trees to
    public static String SERILIZED_DIR_LOCATION = "../../lab3/forestFiles/";

    // define the prefix of name of our tree files
    public static String SERILIZED_FILE_NAME_PREFIX = "tree";

    // the number of our feature
    public static int FEATURE_NUM;

    // column name of our data matrix
    public static ArrayList<String> columnName = new ArrayList<>();



    // The number of trees in this forest
    public static final int TREENUM = 100;

    // The ration of training subSet
    public static final double TRAINING_SUBSET_RATIO = 2.0/3;

    // The dimension of our subspace
    public static int SUBSPACE_DIMENSION;

    ConfigDataSingleton() {
        File folder = new File(INPUT_DATA_DIR);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            String filename =  listOfFiles[i].getName();
            if (listOfFiles[i].isFile() && filename.endsWith("csv")) {
                columnName.add(filename);
            }
        }

        removeString(columnName, LABEL);
        columnName.add(LABEL);

        for(String s: REMOVE_FEATURES) {
            removeString(columnName, s);
        }

        System.out.println("These are our features and label: " + columnName.toString());

        FEATURE_NUM = columnName.size() - 1;

        SUBSPACE_DIMENSION = (int)Math.sqrt(FEATURE_NUM);
        System.out.println("SUBSPACE_DIMENSION = " + SUBSPACE_DIMENSION);
    }


    private void removeString (ArrayList<String> al, String s){
        int size = al.size();
        int i;
        for(i =0; i < size; i++) {
            if(al.get(i).equals(s)) {
                break;
            }
        }
        if (i < size)
            al.remove(i);
    }

    public static ConfigDataSingleton getInstance() {
        if (instance == null) {
            synchronized (ConfigDataSingleton.class) {
                // Double check to avid thread race
                if (instance == null) {
                    instance = new ConfigDataSingleton();
                }
            }
        }
        return instance;
    }
}
