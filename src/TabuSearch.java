import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;

public class TabuSearch {
    /**
     * 当前未对每辆车尾端加入的0进行优化（每n次结束后将连续的零删除，每次保证车后两个零）
     * 多场站可以尝试两个零为一对，在两零中间插入
     * */
    private VehicleList vehList;
    private ArrayList<Node> nodeList;
    private int MAXITERATION;
    private double SPEED;
    private TreeMap<Double,VehicleList> ansList = new TreeMap<>();// 最好的十个解
    private TreeMap<Double,VehicleList> notBestList = new TreeMap<>();// 次之十个解
    private TreeMap<Double,VehicleList> notList = new TreeMap<>();// 不考虑可行性的十个解
    private double acceptPro;
    private boolean testmode = false;
    public TabuSearch(ArrayList<Node> node,VehicleList vehicles,double speed)
    {
        vehList = vehicles;
        SPEED = speed;
        MAXITERATION = 9000;
        nodeList = node;
        acceptPro = 0.95;
    }

    public void cal() throws IOException, ClassNotFoundException {
        int iteration = 0 ;
        int iter;
        double delta;
        double fit;
        double min=Double.MAX_VALUE;
        int count=0;
        if (testmode==true){
            vehList.setTestmode();
        }
        while (iteration<=MAXITERATION){
            /**目前需要考虑的问题有：
             * 1、每一步是否更好再执行操作——通过deepcopy返回
             *      （1）对于swap来说，可以先进行计算save再选择是否进行操作
             *      （2）对于deletenode，则是是否每次取最好的位置插入，还是随机插入
             * 2、保留三组不同性质的解，以及如何对三组解进行操作
             * 3、计算优化
             *      （1）计算fitness时，可以标记每次发生了什么变化，再从变化的值入手进行计算
             * 4、每次执行各种操作的依据——是否应该将删除车辆作为最终手段
             * 5、其实有些地方判断约束比实际情况更加小，值得优化
             * 6、未加入车辆使用成本
             * 7、InnerSwap应该放到vehicle
             * */
            iter = Math.max(1,(int)(2*Math.random()));
            double pro = Math.random();
            //System.out.println(pro);
            // 交换操作，分为两种，一种是相邻节点交换，一种是相邻三节点旋转，其实没啥区别
            // 相邻交换--遍历所有可以交换的点，然后计算节约值，概率p取最大值执行交换，概率1-p不进行交换
            if (pro<0.3){
                innerSwapNode(iter);// 输入迭代次数、每次删除节点数
            }
            else if (pro<0.6){
                // 非相邻交换，随机选择两个节点，可以交换且节约值变大者进行交换，概率p取最大值执行交换，概率1-p不进行交换
                outerSwapNode(iter);// 输入迭代次数、每次删除节点数
            }else if (pro<1){
                // 删除节点操作，概率p取最大值执行交换，概率1-p不进行交换
                int nodenum = (int)(6*Math.random());
                deleteNode(iter,nodenum);// 输入迭代次数、每次删除节点数
            }else{
                // 删除车辆操作，概率p取最大值执行交换，概率1-p不进行交换，不计入迭代次数
                int vehnum = Math.max(1, (int) (3 * Math.random()));
                deleteVeh(1, vehnum);// 输入迭代次数、每次删除车辆数
            }

            fit = vehList.calFitness();
            insertMap(fit);
            iteration += iter;
            if (min>fit) {
                count = 0;
                min = fit;
            }
            else count++;

            if (count>200) {
                // 取最优解，然后删除车辆操作，概率p取最大值执行交换，概率1-p不进行交换，不计入迭代次数
                vehList = ansList.firstEntry().getValue().deepCopy();
                int vehnum = Math.max(1, (int) (3 * Math.random()));
                deleteVeh(1, vehnum);// 输入迭代次数、每次删除车辆数
                fit = vehList.calFitness();
                insertMap(fit);
                count = 0;
            }
        }
        // 获取最优解
        vehList = ansList.firstEntry().getValue().deepCopy();
        System.out.println("****禁忌搜索结束****");
        System.out.println("迭代次数="+MAXITERATION);
        System.out.println("共使用车辆="+vehList.getVehList().size());
        fit = vehList.calFitness();
        System.out.println("总费用="+ fit);
        if (testmode)vehList.finalCheck();
    }

    public void innerSwapNode(int iteration) {
        for (int iter = 0; iter < iteration; iter++) {
            // 随机生成第一个点的坐标
            int v = (int)(Math.random()*vehList.getVehSize());
            int loc = (int)(Math.random()*(vehList.getVeh(v).getVehSize()-2));
            // 当两坐标相同或者相邻时拒绝操作或者取到了〇节点，重新生成坐标
            if (loc==0){
                iter--;
                continue;
            }
            if (vehList.isInnerSwap(v,loc,acceptPro))
                vehList.innerSwap(v,loc);
        }
    }
    public void outerSwapNode(int iteration){
        for (int iter = 0;iter<iteration;iter++){
            // 随机生成两组坐标，不能是头和尾max（1，size-1）
            int v1 = (int)(Math.random()*vehList.getVehSize());
            int v2 = (int)(Math.random()*vehList.getVehSize());
            int loc1 = (int)(Math.random()*(vehList.getVeh(v1).getVehSize()-1));
            int loc2 = (int)(Math.random()*(vehList.getVeh(v2).getVehSize()-1));
            // 当两坐标相同或者相邻时拒绝操作或者取到了〇节点，重新生成坐标
            if (v1==v2||Math.abs(loc1-loc2)<=1||loc1==0||loc2==0){
                iter--;
                continue;
            }
            // 先判断是否可以交换，然后在进行交换。交换原则，删除两个节点后，再插入相应位置
            // 在此应该考虑交换后的结果是否更优然后选择性保留——尚未实现
            if (vehList.isOuterSwap(v1,v2,loc1,loc2,acceptPro))
                vehList.outerSwap(v1,v2,loc1,loc2);
        }

    }

    public void insertMap(double fit) throws IOException, ClassNotFoundException {
        if (ansList.size()<10) {
            VehicleList copy = vehList.deepCopy();
            ansList.put(fit, copy);
        }else{
            if (fit<ansList.lastEntry().getKey()){
                VehicleList copy = vehList.deepCopy();
                ansList.put(fit,copy);
                ansList.pollLastEntry();
            }
        }
    }
    public void deleteNode(int iteration,int nodenum){
        // 进行iteration次操作：随机删除指定个数个节点，然后重新插入
        for (int iter = 0;iter<iteration;iter++){
            // 随机选择nodenum个节点进行删除
            // System.out.println("当前迭代次数"+iter+"，删除节点总数="+nodenum);
            for (int i=0;i<nodenum;i++) {
                // 随机生成车辆位置序号和节点位置序号
                int veh = (int) (vehList.getVehList().size() * Math.random());
                int loc = 1+ (int) ((vehList.getVehList().get(veh).getVehSize()-2) * Math.random());
                if (vehList.getVehList().size()<=2){
                    vehList.getVehList().remove(veh);
                    i--;
                    continue;
                }
                vehList.removeNode(veh,loc);
            }
            vehList.recover();
        }
    }

    public void deleteVeh(int iteration,int vehnum){
        // 进行iteration次操作：随机删除指定个数辆车，然后重新插入
        if (testmode&&(vehnum<=0||vehnum>vehList.getVehList().size())){
            System.out.println("拒绝删除车辆，已超过车辆上限或小于零");
            return;
        }
        for (int iter = 0;iter<iteration;iter++){
            // 对于乘客数量小于3的车辆，直接删除
            if (Math.random()<acceptPro) {
                for (int i = 0; i < vehList.getVehList().size(); i++) {
                    if (vehList.getVehList().get(i).getVehSize() <= 3) {
                        int vehid = vehList.getVehList().get(i).getVehNum();
                        vehList.removeVeh(vehid);
                    }
                }
            }
            // 随机选择vehnum辆车进行删除，将不可以删除的车辆直接删除。
            for (int count =0;count<vehnum;count++) {
                // hashset用来记录每次删除的车辆id
                HashSet<Integer> deletedveh = new HashSet<>();
                // 每次删除一辆车，共删除vehnum次
                int num = (int) ((vehList.getVehSize()-1) * Math.random());
                int vehid = vehList.getVehList().get(num).getVehNum();
                if (deletedveh.contains(vehid)){
                    count--;
                    continue;
                }
                vehList.removeVeh(vehid);
                if (vehList.getVeh(num).getVehSize()<=2){
                    vehList.removeVeh(vehid);
                    count--;
                }
                deletedveh.add(vehid);
            }
            vehList.recover();
        }
    }

    public void setFactors(int iter){
        MAXITERATION = iter;
    }
    public VehicleList getVeh(){
        return vehList;
    }

    public void setTestmode(){
        testmode = true;
    }

}
