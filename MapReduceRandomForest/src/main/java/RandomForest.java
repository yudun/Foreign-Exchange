import java.io.*;
import java.util.ArrayList;

/**
 * Created by Shimin Wang (andrewid: shiminw) on 15/10/6.
 */
public class RandomForest {
    // The number of trees in this forest
    private int numberOfTrees;

    // The accuracy of this random forest
    public double accuracy;

    // Trees in this forest
    private ArrayList<PreDecisionTree> trees = null;



    RandomForest(int numberOfTrees) {
        this.numberOfTrees = numberOfTrees;
        trees = new ArrayList<PreDecisionTree>();
    }

    public int getNumberOfTrees() {
        return numberOfTrees;
    }

    public void setNumberOfTrees(int numberOfTrees) {
        this.numberOfTrees = numberOfTrees;
    }

    public void addTree(PreDecisionTree t) {
        trees.add(t);
    }

    public PreDecisionTree getTree(int index){
        return trees.get(index);
    }

    public void clear(){
        numberOfTrees = 0;
        trees.clear();
    }

    // create a list of tree node for a single tree
    private ArrayList<PreDecisionTree> createListOfTreeNodes(PreDecisionTree root){
        ArrayList<PreDecisionTree> treeNodeList = new ArrayList<>();
        treeNodeList.add(root);

        int index = 0;
        while(index < treeNodeList.size()){
            PreDecisionTree T = treeNodeList.get(index);
            if(T.left != null)
                treeNodeList.add(T.left);
            if(T.right != null)
                treeNodeList.add(T.right);
            index ++;
        }

        return treeNodeList;
    }

    public void dump(String dir, String prefix){
        for (int i = 0; i < trees.size(); i++) {
            ArrayList<PreDecisionTree> treeNodeList = createListOfTreeNodes(trees.get(i));

            try {
                FileOutputStream fileOut = new FileOutputStream(dir + prefix + i);
                ObjectOutputStream out = new ObjectOutputStream(fileOut);
                out.writeObject(treeNodeList);
                out.close();
                fileOut.close();
            } catch (Exception e) {
                System.out.println("Problem serializing: " + e);
            }

        }
    }

    public void readFromFile(String dir){
        File [] filename = new File(dir).listFiles();
        int countFile = 0;
        for (int i = 0; i < filename.length; i++) {
            if(filename[i].toString().endsWith("Store"))
                continue;
            else
                System.out.println(filename[i]);
            ArrayList<PreDecisionTree> ar = null;

            try{
                FileInputStream in = new FileInputStream(filename[i]);
                ObjectInputStream ois = new ObjectInputStream(in);
                ar = (ArrayList<PreDecisionTree>) (ois.readObject());
                System.out.println("read success");
                trees.add(ar.get(0));

                countFile++;
            } catch (Exception e) {
                System.out.println("Problem serializing: " + e);
            }
        }

        this.numberOfTrees = countFile;
    }
}
