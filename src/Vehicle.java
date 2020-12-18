import java.io.Serializable;
import java.util.ArrayList;

public class Vehicle implements Serializable {
    private int VEH_NO;
    private Double CAPACITYDEFAULT = 200.0;
    private double SPEED;
    private double VEHCOST;
    private double COSTPERDIS;
    private boolean testmode = false;
    // 判断依据
    private Double CAPACITY;
    private ArrayList<Double> ARRIVALTIME = new ArrayList<>();
    private ArrayList<Double> DISTANCE = new ArrayList<>();
    private ArrayList<Double> BACKWARDDELTA = new ArrayList<>();

    // 更改对象
    private ArrayList<Node> NODELIST = new ArrayList<>();

    public Vehicle(int idnum, Node insertnode, double dis,double speed)
    {
        // 无需变更
        VEH_NO = idnum;
        CAPACITY = CAPACITYDEFAULT;
        // 判断是否可以合并依据
        CAPACITY -= insertnode.getDemond();
        ARRIVALTIME.add((double) insertnode.getReadyTime());
        DISTANCE.add(dis);
        BACKWARDDELTA.add((double)(insertnode.getDueTime()-insertnode.getReadyTime()));

        // 非判断依据
        NODELIST.add(insertnode);
        SPEED = speed;
        VEHCOST = 30;
        COSTPERDIS = 1.0;
    }

    public boolean isInnerSwap(int nodeloc,double acceptpro){
        Vehicle tmp1 = this;
        Node n1 = tmp1.getNode(nodeloc);
        Node n2 = tmp1.getNode(nodeloc+1);
        if (Math.random()<acceptpro){// 当节约值小于零，拒绝改变
            if (DISTANCE.get(nodeloc)+DISTANCE.get(nodeloc+1)+DISTANCE.get(nodeloc+2)// 原本距离
                <NODELIST.get(nodeloc-1).distoNode(NODELIST.get(nodeloc+1))+DISTANCE.get(nodeloc+1)+
                    NODELIST.get(nodeloc).distoNode(NODELIST.get(nodeloc+2))
            )return false;
        }
        // 先计算到n2的
        double at1 = Math.max(tmp1.getArrivalTime(nodeloc-1),tmp1.getNode(nodeloc-1).getReadyTime())+
                tmp1.getNode(nodeloc-1).getServiceTime()+
                tmp1.getNode(nodeloc-1).distoNode(n2)/SPEED;
        if ( at1 > n2.getDueTime())
            return false;
        // 在计算到n1的
        at1 = Math.max(at1,n2.getReadyTime())+n2.getServiceTime()+n2.distoNode(n1)/SPEED;
        if ( at1 > n1.getDueTime())
            return false;
        // 判断是否可达下一节点
        at1 = Math.max(at1,n1.getReadyTime())+n1.getServiceTime()+n1.distoNode(tmp1.getNode(nodeloc+1))/SPEED;
        if (at1-tmp1.getArrivalTime(nodeloc+1)>tmp1.getBackTime(nodeloc+1)) return false;
        return true;
    }

    public void innerSwap(int nodeloc){
        ArrayList<Node> tmp = new ArrayList<>();
        this.removeNode(nodeloc+1,tmp);
        this.addNode(nodeloc,tmp.get(0));

    }

    public void addNode(int loc, Node node){
        // 用于插入节点，当小于三个节点时只允许插入O/D
        CAPACITY -= node.getDemond();
        NODELIST.add(loc,node);
        double distmp = node.distoNode(NODELIST.get(loc-1));
        DISTANCE.add(loc,distmp);
        if (NODELIST.size()>=3) {
            distmp = node.distoNode(NODELIST.get(loc + 1));
            DISTANCE.set(loc + 1, distmp);
        }

        // at 为ArrivalTime
        double at = (Math.max(ARRIVALTIME.get(loc - 1),NODELIST.get(loc-1).getReadyTime()) + NODELIST.get(loc - 1).getServiceTime()) + DISTANCE.get(loc) / SPEED;
        ARRIVALTIME.add(loc, at);
        if (NODELIST.size()>=3) {
            /** 从前向后计算实际到达时间
             * 计算实际到达时间时，不采用max{arrivetime，readytime}进行取舍，在此使用实际到达时间，方便后续比较
             * */
            // 这里可以从loc+1开始，保险起见，从1开始
            for (int i = 1; i < ARRIVALTIME.size(); i++) {
                at = (Math.max(ARRIVALTIME.get(i - 1),NODELIST.get(i-1).getReadyTime()) + NODELIST.get(i - 1).getServiceTime()) + DISTANCE.get(i) / SPEED;
                ARRIVALTIME.set(i, at);
                //if (at<NODELIST.get(i).getDueTime())System.out.println("该点不能插入");
            }
            /** 从后向前更新每一节点允许后推值,这里可以从loc往后退，当一个节点的到达时间仍然在READY时即可停止
             * 后推值以真实出发时间为基准从后向前进行推算，之后再将这部分时间加回来——如果不以真是出发时间推算的话可能会降低时间容差
             * */
            BACKWARDDELTA.add(loc,(NODELIST.get(loc).getDueTime()-Math.max(NODELIST.get(loc).getReadyTime(),ARRIVALTIME.get(loc))));
            for (int i = ARRIVALTIME.size()-1; i>=0; i--){
                double tmp;
                if (i == ARRIVALTIME.size()-1){
                    tmp = (NODELIST.get(i).getDueTime()-ARRIVALTIME.get(i));
                }else {
                    // 一共包含三个部分，最大时间窗min(D-A/D(N+1))+max(0，R-A)
                    tmp = Math.min(BACKWARDDELTA.get(i+1),(NODELIST.get(i).getDueTime()-Math.max(ARRIVALTIME.get(i),NODELIST.get(i).getReadyTime())))// 这里是最晚时间-最早时间
                    + Math.max(0,NODELIST.get(i).getReadyTime()-ARRIVALTIME.get(i));
                }
                BACKWARDDELTA.set(i,tmp);
            }
        }else {
            double tmp = (NODELIST.get(1).getDueTime()-ARRIVALTIME.get(1));
            BACKWARDDELTA.add(tmp);
        }
        if (testmode) {
            int sss = 0;
            for (int i = 1; i < DISTANCE.size(); i++) {
                if (DISTANCE.get(i) - NODELIST.get(i - 1).distoNode(NODELIST.get(i)) > 1) {
                    sss++;
                    System.out.println("距离错误,本车序号=" + VEH_NO + "，本节点位置" + i + "，插入节点位置=" + loc + "，本车错误节点数" + sss);
                }
            }
        }
    }
    // 判断是否能插入节点于loc位置前一个空位
    public boolean isAddNode(int loc, Node node){
        // 插入位置限制
        if (testmode==true&&(loc<=0||loc>=NODELIST.size())){
            System.out.println("不允许插入该节点");
            return false;
        }
        // 容量限制
        if (CAPACITY-node.getDemond()<0){
            return false;
        }
        // 到达本节点时间
        double at = Math.max(ARRIVALTIME.get(loc-1),NODELIST.get(loc-1).getReadyTime()) +
                NODELIST.get(loc-1).getServiceTime()+node.distoNode(NODELIST.get(loc-1))/SPEED;
        // 本节点时间窗限制
        if (at>node.getDueTime())
            return false;
        // 后续节点时间窗限制
        // 下一节点实际到到达时间
        double atNext = Math.max(at,node.getReadyTime()) + node.getServiceTime() +
                node.distoNode(NODELIST.get(loc))/SPEED;
        if (atNext-ARRIVALTIME.get(loc)>BACKWARDDELTA.get(loc))
            return false;
        return true;
    }

    public void removeNode(int loc , ArrayList<Node> removedlist){
        // 不允许超出范围
        if ((loc<=0||loc>=NODELIST.size()-1))
        {
            System.out.println("本节点不允许删除，节点序号="+loc+"，共有节点="+NODELIST.size());
            return;
        }
        // 加回删除节点的容量
        CAPACITY += NODELIST.get(loc).getDemond();
        // 删除该节点
        removedlist.add(NODELIST.get(loc));
        NODELIST.remove(loc);
        // 删除距离并且更改后一个节点的距离
        DISTANCE.remove(loc);
        DISTANCE.set(loc,NODELIST.get(loc-1).distoNode(NODELIST.get(loc)));
        // 删除到达时间并且更改之后（loc）所有节点的到达时间
        ARRIVALTIME.remove(loc);

        double at;
        // 可以使用 i = loc
        for (int i = 1; i < ARRIVALTIME.size(); i++) {
            at = (Math.max(ARRIVALTIME.get(i - 1),NODELIST.get(i-1).getReadyTime()) + NODELIST.get(i - 1).getServiceTime()) + DISTANCE.get(i) / SPEED;
            if (testmode==true&&(DISTANCE.get(i)-NODELIST.get(i-1).distoNode(NODELIST.get(i))>1)){
                System.out.println("删除节点时，距离错误");
            }
            ARRIVALTIME.set(i, at);
        }
        // 倒推时间重新计算
        BACKWARDDELTA.remove(loc);
        for (int i = ARRIVALTIME.size()-1; i>=0; i--){
            double tmp;
            if (i == ARRIVALTIME.size()-1){
                tmp = (NODELIST.get(i).getDueTime()-ARRIVALTIME.get(i));
            }else {
                tmp = Math.min(BACKWARDDELTA.get(i+1),(NODELIST.get(i).getDueTime()-Math.max(ARRIVALTIME.get(i),NODELIST.get(i).getReadyTime())))+
                Math.max(0,NODELIST.get(i).getReadyTime()-ARRIVALTIME.get(i));
            }
            BACKWARDDELTA.set(i,tmp);
        }

    }

    public double getSaveValue(int loc, Node insertnode, Node orinode){
        double beforeLength = NODELIST.get(loc-1).distoNode(NODELIST.get(loc))
                + 2.0 * insertnode.distoNode(orinode);
        double afterLength = insertnode.distoNode(NODELIST.get(loc-1))+
        insertnode.distoNode(NODELIST.get(loc));
        return beforeLength-afterLength;
    }

    public double getArrivalTime(int loc){ return ARRIVALTIME.get(loc);}

    public int getVehNum(){
        return VEH_NO;
    }

    public Double getDis(int loc){
        return DISTANCE.get(loc);
    }

    public Node getNode(int loc){
        return NODELIST.get(loc);
    }

    public int getVehSize(){return NODELIST.size();}

    public double getCapacity(){ return CAPACITY;}

    public double getCapDefalut(){ return CAPACITYDEFAULT;}

    public double getBackTime(int loc){ return BACKWARDDELTA.get(loc);}

    public double getVehCost(){ return VEHCOST;}

    public double getCostPerDis(){ return COSTPERDIS;}

    public void setTestmode(){
        testmode = true;
    }
}
