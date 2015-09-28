import java.util.ArrayList;
import java.util.Queue;

/**
 * Created by Shimin Wang (andrewid: shiminw) on 15/9/27.
 *
 * Each node of our decision tree. It also includes our training data.
 * So we name it as "pre".
 * To store this decision tree into disk, we have to convert it into
 * class DecisionTree and serialize it.
 *
 * We always filter data those indicated by value 0 to the left and 1 to the right
 */

public class PreDecisionTree {
    // accuracy of this model (gained after tested)
    static double accuracy;

    // indicate which level this node is in. start from 0
    int level;

    // the index of feature that we use at this node
    // **** if it equals to -1, it means that we reach the leave ****
    int featureIdx = -1;

    // the index of feature that we use at its parent node
    int parentFeatureIdx = -1;

    // filtered data that belongs to this node
    ArrayList<DataInstance> data = null;

    // child of this node
    PreDecisionTree left ,right;

    //label for this node (0 default)
    int label = 0;

    //purity for data in this node
    double purity = 0;

    PreDecisionTree(int l, int fidx, int pfidx) {
        level = l;
        featureIdx = fidx;
        parentFeatureIdx = pfidx;
        left = right = null;
    }

    // split and push children nodes into q
    public void split(Queue<PreDecisionTree> q) {

        // we stop splitting on bottomest level and empty node
        if (data.size() == 0 || level == ConfigDataSingleton.FEATURE_NUM - 1 || level == -1) {
            this.level = -1;
            left = right = null;
            return;
        }

        this.featureIdx = evaluateIG();

        // no more purity can be improved, we stop here
        if(featureIdx == this.parentFeatureIdx) {
            this.level = -1;
            left = right = null;
            return;
        }

        // split new children nodes
        left = new PreDecisionTree(level + 1, -1, featureIdx);
        right = new PreDecisionTree(level + 1, -1, featureIdx);
        // statistics we need for update children's label and purity
        int cLeft = 0, cRight = 0;
        int cLeft0 = 0, cLeft1 = 0;
        int cRight0 = 0, cRight1 = 0;
        left.data = new ArrayList<>();
        right.data = new ArrayList<>();

        for(DataInstance dt: this.data){
            // filter to the left child
            if(dt.feature[featureIdx] == 0) {
                left.data.add(dt);
                cLeft ++;
                if(dt.label == 0)
                    cLeft0 ++;
                else
                    cLeft1 ++;
            }
            // filter to the right child
            else {
                right.data.add(dt);
                cRight ++;
                if(dt.label == 0)
                    cRight0 ++;
                else
                    cRight1 ++;
            }
        }


        // if one of the child node has empty data set, we
        // set its label as the opposite of the other node
        // We revised the broken node by adding 1 to achieve this
        if (cLeft == 0) {
            cLeft = 1;
            if (cRight1 > cRight0)
                cLeft0 = 1;
            else
                cRight0 = 1;
        } else if (cRight == 0) {
            cRight = 1;
            if (cLeft1 > cLeft0)
                cRight0 = 1;
            else
                cRight1 = 1;
        }


        left.label = (cLeft1 > cLeft0 ? 1 :0);
        left.purity = (left.label == 1 ? cLeft1 * 1.0 / cLeft : cLeft0 * 1.0 / cLeft);

        right.label = (cRight1 > cRight0 ? 1 :0);
        right.purity = (right.label == 1 ? cRight1 * 1.0 / cRight : cRight0 * 1.0 / cRight);

        q.add(left);
        q.add(right);
    }


    private int evaluateIG(){
        int resultIdx = 0;
        double minIG = getIG(0);
        for(int testIdx = 1; testIdx < ConfigDataSingleton.FEATURE_NUM; testIdx++) {
            double tmpIG = getIG(testIdx);
            if(minIG > tmpIG) {
                minIG = tmpIG;
                resultIdx = testIdx;
            }
        }

        return resultIdx;
    }

    // calculate the entropy for a list of data
    private double H(ArrayList<DataInstance> data){
        int size = data.size();

        if(size == 0)
            return 0;

        int zeroNum = 0;
        for(DataInstance dt: data){
            if(dt.label == 0)
                zeroNum ++;
        }

        if(zeroNum == 0 || zeroNum == size) {
            return 0;
        }

        double pZero = zeroNum * 1.0 / size;
        double pOne = 1 - pZero;

        return -(pZero * Math.log(pZero) + pOne * Math.log(pOne));
    }


    private double getIG(int testIdx) {
        ArrayList<DataInstance> data0 = new ArrayList<>();
        ArrayList<DataInstance> data1 = new ArrayList<>();
        int count0 = 0, count1 = 0;
        double size = this.data.size();

        for (DataInstance dt : this.data) {
            if(dt.feature[testIdx] == 0) {
                count0++;
                data0.add(dt);
            }
            else {
                count1++;
                data1.add(dt);
            }
        }

        return (count0 / size) * H(data0) + (count1 / size) * H(data1);
    }
}
