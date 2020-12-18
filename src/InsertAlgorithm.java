import java.util.*;

public class InsertAlgorithm {
    private ArrayList<Node> nodeList;
    private ArrayList<Node> unReachedList= new ArrayList<>();
    private HashMap<Node, Integer> markedPoint = new HashMap();
    private VehicleList vehList;
    private double SPEED;
    private double acceptpro = 1;// 插入法，每次选取最好的结果插入
    private boolean testmode = false;
    public InsertAlgorithm(ArrayList<Node> node, ArrayList<ArrayList<Double>> imput, double speed)
    {
        /** 选取最远节点作为种子节点
         * 然后按照最近邻原则选取下一节点，按照取节约值最大的位置插入
         * 在此有两种方案1.每下一个节点均为当前节点的最近邻
         *             2.每下一个节点均为初始点的最近邻
         * */
        SPEED = speed;
        nodeList = node;
        vehList = new VehicleList(nodeList,SPEED);
        for (int j=1;j<imput.size();j++) {
            nodeList.get(0).addNearPoint(nodeList.get(j),unReachedList);
        }
        nodeList.get(0).sortNearPoint();
    }

    public void cal(){
        /** 不断更新当前节点来实现最近邻搜索
         * 搜索开始加入一个数组，表示某个点是否已经加入结果
         * 每次搜索加入一个数组，表示本次搜索已经搜索的范围，避免重复搜索
         * 这一部分使用DFS
         * */
        int[] searched = new int[nodeList.size()-1];
        int[] inserted = new int[nodeList.size()-1];
        ArrayList<Node> oriPoint = nodeList.get(0).getNearPoint();
        if (testmode){
            vehList.setTestmode();
        }
        // 默认DEFAULT由远到近分配，modeSearch="最近邻搜索"时，每次搜索当前节点最近邻。
        // 默认DEFAULT优先插入车辆，modeAddVeh="onbyOne"时，仅当当前车辆用完后才插入车辆。
        String modeSearch = "DEFAULT";
        String modeAddVeh = "onbyOne";
        if (modeAddVeh == "DEFAULT"){
            for (int i=oriPoint.size()-1;i>=0;i--){
                Node node = oriPoint.get(i);
                vehList.insertBestNode(node);
            }
        }else if (modeAddVeh == "onbyOne"){
            if (vehList.getVehList() == null || vehList.getVehList().isEmpty())
                vehList.addVeh(5);
            while(Arrays.stream(inserted).sum()<inserted.length) {
                int insertNum = Arrays.stream(inserted).sum();
                if (insertNum == inserted.length) break;
                for (int i = oriPoint.size() - 1; i >= 0; i--) {
                    Node node = oriPoint.get(i);
                    if (searched[node.getNodeNum() - 1] == 1) continue;
                    if (inserted[node.getNodeNum() - 1] == 1) continue;
                    vehList.insertBestNode(node);
                    if(vehList.insertState()) {
                        inserted[node.getNodeNum() - 1] = 1;
                        // 已经标记过的节点直接跳过
                        searched = Arrays.copyOf(inserted, inserted.length);
                        vehList.cleanInsertState();
                        break;
                    }
                }
                if (insertNum==Arrays.stream(inserted).sum()) {
                    vehList.addVeh(5);
                }
            }
        }
        if (testmode){
            double dis = 0;
            for (Vehicle veh: vehList.getVehList()){
                for (int i=0;i<veh.getVehSize();i++){
                    dis += veh.getDis(i)*veh.getCostPerDis();
                }
                dis += veh.getVehCost();
            }
            System.out.println("****插入法****");
            System.out.println("共使用车辆="+vehList.getVehList().size());
            System.out.println("共插入节点="+ Arrays.stream(inserted).sum());
            System.out.println("总距离="+ dis);
            System.out.println("****插入法结束****");
        }
    }

    public VehicleList getVeh(){
        return vehList;
    }

    public void setTestmode(){
        testmode = true;
    }
}
