/**
 * Created by Shimin Wang (andrewid: shiminw) on 15/9/27.
 *
 * A internal data structure for each data instance
 */
public class DataInstance {
    String time;
    int[] feature = new int[ConfigDataSingleton.FEATURE_NUM];
    int label;

    DataInstance(String s) {
        String [] record = s.split("\t");
        time  = record[0];
        for (int i = 1; i <= ConfigDataSingleton.FEATURE_NUM; i++) {
            feature[i-1] = Integer.parseInt(record[i]);
        }

        label = Integer.parseInt(record[ConfigDataSingleton.FEATURE_NUM + 1]);
    }

    public String toString(){
        String featureString = "";
        for (int i = 0; i < feature.length; i++ ) {
            featureString += " " + feature[i];
        }
        return time + " " + featureString + " Label:" + label;
    }
}
