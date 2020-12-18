import java.io.Serializable;
import java.util.*;

public class Node implements Serializable {
    /**
     * 功能：存储从文件中读取的节点（顾客）信息
     * 备注：需要考虑的是异常情况
     * @param CUST_NO      客序号
     * @param XCOORD        x坐标
     * @param YCOORD        y坐标
     * @param DEMAND        需求
     * @param READY_TIME    最早开始时间
     * @param DUE_DATE      最晚开始时间
     * @param SERVICE_TIME  服务时间
     */
    private int CUST_NO;
    private int XCOORD;
    private int YCOORD;
    private int DEMAND;
    private int READY_TIME;
    private int DUE_DATE;
    private int SERVICE_TIME;
    private ArrayList<Node> NEARPOINT;
    private double SPEED;
    private ArrayList<String> content;
    private boolean testmode = false;
    public Node(ArrayList<String> arrayList,double speed)
    {
        this.CUST_NO = Integer.parseInt(arrayList.get(0));
        this.XCOORD = Integer.parseInt(arrayList.get(1));
        this.YCOORD = Integer.parseInt(arrayList.get(2));
        this.DEMAND = Integer.parseInt(arrayList.get(3));
        this.READY_TIME = Integer.parseInt(arrayList.get(4));
        this.DUE_DATE = Integer.parseInt(arrayList.get(5));
        this.SERVICE_TIME = Integer.parseInt(arrayList.get(6));
        this.SPEED = speed;
        this.content = arrayList;
    }

    public ArrayList<String> getContent(){
        return content;
    }
    public double getSpeed(){
        return SPEED;
    }

    public void addNearPoint(Node node,ArrayList<Node> unreach){
        if (NEARPOINT==null) NEARPOINT = new ArrayList<>();
        double time = node.distoNode(node)/SPEED;
        if ( READY_TIME+SERVICE_TIME+time<=node.DUE_DATE)
            NEARPOINT.add(node);
        else unreach.add(node);
    }
    public void sortNearPoint(){
        Collections.sort(NEARPOINT,new Comparator<Node>() {
            @Override
            public int compare(Node s1, Node s2) {
                double flag;
                // 首选按年龄升序排序
                double s1dis = Math.pow(s1.XCOORD-XCOORD,2) + Math.pow(s1.YCOORD-YCOORD,2);
                double s2dis = Math.pow(s2.XCOORD-XCOORD,2) + Math.pow(s2.YCOORD-YCOORD,2);
                flag = s1dis-s2dis;
                /**if(flag==0){
                *    // 再按学号升序排序
                *    flag = s1.getNum()-s2.getNum();
                *}
                 * */
                return (int)flag;
            }
        });
    }
    public double distoNode(Node n1){
        return Math.sqrt(Math.pow((n1.getX()-XCOORD),2)+
                Math.pow((n1.getY()-YCOORD),2));
    }

    public int getNodeNum(){
        return CUST_NO;
    }
    public int getX(){ return XCOORD; }
    public int getY(){ return YCOORD; }
    public int getDemond(){
        return DEMAND;
    }
    public int getReadyTime(){
        return READY_TIME;
    }
    public int getDueTime()
    {
        return DUE_DATE;
    }
    public int getServiceTime()
    {
        return SERVICE_TIME;
    }
    public ArrayList<Node> getNearPoint() {
        return NEARPOINT;
    }
    public void setTestmode(){
        testmode = true;
    }
}
