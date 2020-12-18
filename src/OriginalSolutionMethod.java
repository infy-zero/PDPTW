import java.io.IOException;
import java.util.*;

public class OriginalSolutionMethod {
    private ArrayList<Node> node;
    private String originalmode;
    private ArrayList<ArrayList<Node>> solution;
    private String optimizationmode;
    private int lenth;
    private VehicleList vehicleList;
    private ArrayList<ArrayList<Double>> nodeMatrix;
    private double speed;
    private boolean testmode = false;
    public OriginalSolutionMethod(ArrayList<Node> nodeimput, String originalimput, String optimizationalimput
            , double SPEED) throws IOException, ClassNotFoundException {
        node = nodeimput;
        originalmode = originalimput;
        optimizationmode = optimizationalimput;
        lenth = node.size();
        nodeMatrix = getDistanceMatrix(node);
        speed = SPEED;

    }
    public void cal() throws IOException, ClassNotFoundException {
        double t =System.currentTimeMillis();
        switch(originalmode)
        {
            case "Insert":
                InsertAlgorithm sa = new InsertAlgorithm(node,nodeMatrix,speed);
                if (testmode==true) sa.setTestmode();
                sa.cal();
                vehicleList = sa.getVeh();
                break;
        }
        System.out.println("用时总计=" + (System.currentTimeMillis() - t) / 1000);
        t =System.currentTimeMillis();
        switch (optimizationmode)
        {
            case "Tabu":
                TabuSearch ts = new TabuSearch(node,vehicleList,speed);
                ts.cal();
                vehicleList = ts.getVeh();
        }
        System.out.println("用时总计=" + (System.currentTimeMillis() - t) / 1000);
    }

    public ArrayList<Vehicle> getVeh(){
        return vehicleList.getVehList();
    }

    public ArrayList<ArrayList<Double>> getDistanceMatrix(ArrayList<Node> imput)
    {
        ArrayList<ArrayList<Double>> output = new ArrayList<>();
        for(int i=0;i<lenth;i++){
            ArrayList<Double> temp = new ArrayList<>();
            for(int j=0;j<lenth;j++){
                temp.add(Math.sqrt(Math.pow((imput.get(i).getX()-
                        imput.get(j).getX()),2)+Math.pow((imput.get(i).getY()-
                        imput.get(j).getY()),2)));
            }
            output.add(temp);
        }
        return output;
    }
    public void setTestmode(){
        testmode = true;
    }
}
