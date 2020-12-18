import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class VehicleList implements Serializable{
    private int idnum;
    private ArrayList<Vehicle> vehList;
    private ArrayList<Node> nodeList;
    private ArrayList<Node> removedList;
    private double SPEED;
    private boolean insertFlag;
    private boolean testmode = false;
    public VehicleList(ArrayList<Node> node,double speed) {
        nodeList = node;
        SPEED = speed;
        vehList = new ArrayList<>();
        removedList = new ArrayList<>();
        idnum = 0;
        insertFlag = false;
        if (testmode){
            for (int i=0;i<vehList.size();i++) getVeh(i).setTestmode();
        }
    }
    // 相邻节点交换
    public boolean isInnerSwap(int vehloc,int nodeloc,double acceptpro){
        // 没有容量约束，只需要求at和bfd
        return vehList.get(vehloc).isInnerSwap(nodeloc,acceptpro);
    }
    public void innerSwap(int vehloc,int nodeloc){
        vehList.get(vehloc).innerSwap(nodeloc);
    }
    // 路径之间节点交换
    public boolean isOuterSwap(int vehloc1,int vehloc2,int nodeloc1,int nodeloc2,double acceptpro){
        // 判断两个因素三种情况：容量和时间窗（前一位置节点和后一位置节点）
        // 判断容量——这里的n1和n2其实反了
        Vehicle tmp1 = vehList.get(vehloc1);
        Vehicle tmp2 = vehList.get(vehloc2);
        Node n1 = tmp2.getNode(nodeloc2);
        Node n2 = tmp1.getNode(nodeloc1);
        // 容量约束
        if (tmp1.getCapacity()+n1.getDemond()-n2.getDemond()>tmp1.getCapDefalut())
            return false;
        if (Math.random()<acceptpro) {
            if (tmp1.getDis(nodeloc1) + tmp1.getDis(nodeloc1 + 1) +
                    tmp2.getDis(nodeloc2) + tmp2.getDis(nodeloc2 + 1) < // 当前距离之和
                    tmp1.getNode(nodeloc1 - 1).distoNode(n2) + tmp1.getNode(nodeloc1 + 1).distoNode(n2) +
                            tmp2.getNode(nodeloc2 - 1).distoNode(n1) + tmp2.getNode(nodeloc2 + 1).distoNode(n1)
            ) return false;
        }
        // 判断是否可达
        // 判断是否可达当前节点
        double at1 = Math.max(tmp1.getArrivalTime(nodeloc1-1),tmp1.getNode(nodeloc1-1).getReadyTime())+
                tmp1.getNode(nodeloc1-1).getServiceTime()+
                tmp1.getNode(nodeloc1-1).distoNode(n1)/SPEED;
        if ( at1 > n1.getDueTime())
            return false;
        // 判断是否可达下一节点
        at1 = Math.max(at1,n1.getReadyTime())+n1.getServiceTime()+tmp1.getNode(nodeloc1+1).distoNode(n1)/SPEED;
        if (at1 - tmp1.getArrivalTime(nodeloc1+1)>tmp1.getBackTime(nodeloc1+1)) return false;
        // 容量约束
        if (tmp2.getCapacity()+n2.getDemond()-n1.getDemond()>tmp2.getCapDefalut())
            return false;
        // 判断是否可达当前节点
        double at2 = Math.max(tmp2.getArrivalTime(nodeloc2-1),tmp2.getNode(nodeloc2-1).getReadyTime())+
                tmp2.getNode(nodeloc2-1).getServiceTime()
                + tmp2.getNode(nodeloc2-1).distoNode(n2)/SPEED;
        if (at2 > n2.getDueTime()) return false;
        // 判断是否可达下一节点
        at2 = Math.max(at2,n2.getReadyTime())+n2.getServiceTime()+tmp2.getNode(nodeloc2+1).distoNode(n2)/SPEED;
        if (at2 - tmp2.getArrivalTime(nodeloc2+1)>tmp2.getBackTime(nodeloc2+1)) return false;
        return true;
    }
    public void outerSwap(int vehloc1,int vehloc2,int nodeloc1,int nodeloc2){
        // 先删除两个节点，在插入相应位置
        ArrayList<Node> tmp = new ArrayList<>();
        vehList.get(vehloc1).removeNode(nodeloc1,tmp);
        vehList.get(vehloc2).removeNode(nodeloc2,tmp);
        vehList.get(vehloc1).addNode(nodeloc1,tmp.get(1));
        vehList.get(vehloc1).addNode(nodeloc1,tmp.get(0));
    }
    // 删除节点，放入removedList
    public void removeNode(int vehloc,int nodeloc){
        vehList.get(vehloc).removeNode(nodeloc,removedList);
    }
    // 恢复removedList中的节点
    public void recover(){
        // 对被删除节点进行标记
        // 当随机数大于acceppro时，寻找最佳位置插入，否则随意位置插入
        int[] inserted = new int[getRemovedList().size()];
        while (Arrays.stream(inserted).sum() < getRemovedList().size()) {
            int count = Arrays.stream(inserted).sum();
            for (int i = 0; i < getRemovedList().size(); i++) {
                if (inserted[i] == 0)
                    insertBestNode(getRemovedList().get(i));
                if (insertState()) {
                    inserted[i] = 1;
                    cleanInsertState();
                    }

            }
            if (count == Arrays.stream(inserted).sum()) {
                addVeh(5);
            }
        }


        removedList = new ArrayList<>();
        // System.out.println("实际恢复节点总数="+ Arrays.stream(inserted).sum());
        // System.out.println("********");
        checkZero();
    }
    // 检验是否可行
    public void checkZero(){
        for (int i=0;i<vehList.size();i++){
            if (vehList.get(i).getVehSize()<=2)
            {
                vehList.remove(i);
                // System.out.println("删除空车，序号="+i);
            }
        }
    }
    // 最终检验
    public void finalCheck(){
        boolean capanswer = true;
        boolean fitanswer = true;
        boolean timeanswer = true;
        // 容量
        for (Vehicle veh:vehList){
            double tmpc = veh.getCapDefalut();
            for (int i=0;i<veh.getVehSize();i++){
                tmpc -= veh.getNode(i).getDemond();
                // 时间窗
                if (veh.getArrivalTime(i)>veh.getNode(i).getDueTime())
                    timeanswer = false;
            }
            if (tmpc<0) capanswer=false;
        }
        // 适应度
        double tmp = 0;
        for (Vehicle veh:vehList) {
            if (veh.getDis(0)!=0){
                System.out.println("出发节点距离错误");
            }
            for (int i = 1; i < veh.getVehSize(); i++) {
                Node n1 = veh.getNode(i-1);
                Node n2 = veh.getNode(i);
                tmp += n1.distoNode(n2)*veh.getCostPerDis();
                if (n1.distoNode(n2)*veh.getCostPerDis()-veh.getDis(i)>1)
                    System.out.println("当前车辆="+veh+",位置="+i+"存储结果="+veh.getDis(i)+
                            "实际结果="+n1.distoNode(n2)*veh.getCostPerDis());


            }
            tmp += veh.getVehCost();
        }
        if (tmp-calFitness()>10) fitanswer=false;
        System.out.println("进行最终结果检查，容量="+capanswer+"适应度="+fitanswer+"时间窗="+timeanswer);
        System.out.println("适应度"+tmp+",fit="+calFitness());
    }
    // 加入空车
    public void addVeh(int addnum){
        // 创建一个只有两个0节点的节点链
        Vehicle vehTmp = new Vehicle(idnum,nodeList.get(0),0,SPEED);
        for (int i=0;i<addnum;i++){
            vehTmp.addNode(1,nodeList.get(0));
        }
        idnum++;
        vehList.add(vehTmp);
    }
    // 删除车辆
    public void removeVeh(int vehnum){
        for(int i=0;i<vehList.size();i++){
            if (vehList.get(i).getVehNum()==vehnum){
                for (int j=1;j<vehList.get(i).getVehSize()-1;j++){
                    // 获取所有节点
                    removedList.add(vehList.get(i).getNode(j));
                }
                vehList.remove(i);
            }
        }
    }
    // 车辆交换位置
    public void swapVeh(int pos1, int pos2){
        Vehicle tmp = vehList.get(pos1);
        vehList.set(pos1,vehList.get(pos2));
        vehList.set(pos2,tmp);
    }
    // 以下三个是一组，用来插入节点
    public void insertBestNode(Node node) {
        // 判断插入位置
        double maxValue = -Double.MAX_VALUE;
        int[] maxIndex = new int[]{-1, -1};
        for (int k = 0; k < vehList.size(); k++) {
            for (int j = 1; j <= vehList.get(k).getVehSize() - 1; j++) {
                double value;
                if (vehList.get(k).isAddNode(j, node)) {
                    value = vehList.get(k).getSaveValue(j, node, nodeList.get(0));
                    if (maxValue < value) {
                        maxValue = value;
                        maxIndex[0] = k;
                        maxIndex[1] = j;
                    }
                }

            }
        }
        // 执行插入操作
        if (maxIndex[0] != -1) {
            vehList.get(maxIndex[0]).addNode(maxIndex[1], node);
            insertFlag = true;
        }

    }
    public boolean insertState(){
        return insertFlag;
    }
    public void cleanInsertState(){
        insertFlag = false;
    }
    // 计算适应度值
    public double calFitness() {
        double fit = 0;
        for (int i = 0; i < vehList.size(); i++) {
            for (int j = 0; j < vehList.get(i).getVehSize(); j++) {
                fit += vehList.get(i).getDis(j) * vehList.get(i).getCostPerDis();
            }
            fit += vehList.get(i).getVehCost();
        }
        return fit;
    }
    // 获得最终结果
    public ArrayList<Node> getRemovedList(){
        return removedList;
    }

    public ArrayList<Vehicle> getVehList(){ return vehList; }
    public int getVehSize(){ return vehList.size(); }
    public Vehicle getVeh(int loc){ return vehList.get(loc);}
    public VehicleList deepCopy() throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bos=new ByteArrayOutputStream();
        ObjectOutputStream oos=new ObjectOutputStream(bos);
        oos.writeObject(this);
        oos.flush();
        ObjectInputStream ois=new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        VehicleList copy = (VehicleList) ois.readObject();
        return copy;
    }
    public void setTestmode(){
        testmode = true;
    }
}
