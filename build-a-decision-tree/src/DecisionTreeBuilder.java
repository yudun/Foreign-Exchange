

public class DecisionTreeBuilder {
    // the purity (0~1) when we decided to stop branching the decision tree
    private static double  PURITY_THREHOLD = 1;

    // We don't need features listed in this list
    //REMOVE_FEATURE = []
    // We choose the directionality of the bid of EURUSD as the LABEL
    private static String LABEL = "EURUSD-2015-01.csv";
    // define the location of our original data
    private static String INPUT_DATA_DIR = "../../lab1/alignedData/";
    // define the path of the training set
    private static String TRAINING_DATA_LOCATION = "../../lab2/trainingSet";
    // define the path of the test set
    private static String TEST_DATA_LOCATION = "../../lab2/testSet";

    public static void main(String [] args) {


        System.out.println("test");

    }

}