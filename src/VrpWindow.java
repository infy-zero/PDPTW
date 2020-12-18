import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @version 1.01 2020-11-22
 * @author infy-zero
 * 功能：绘制G(V,E)图，并将输入的可行解画出来
 */

public class VrpWindow {
    // 获得当前屏幕分辨率（不包含任务栏）
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    // Dimension dim=Toolkit.getDefaultToolkit().getScreenSize(); //整个屏幕大小
    Rectangle rect = ge.getMaximumWindowBounds();
    private final int DEFAULT_WIDTH = rect.width;
    private final int DEFAULT_HEIGHT = rect.height;
    private final String FilePath;
    private ArrayList<Node> NodeArrayList;
    private String FilePP = "D:\\JAVA_WORKPLACE\\vrp_data\\VRPTW_solomon\\solomon_100";
    private String FilePPP;
    private WindowFrame frame;
    private String originalSolution = "Insert";
    private String optimizationAlgorithm = "Tabu";
    private ArrayList<Vehicle> vehList;
    private double speed;
    private boolean testmode = false;
    public VrpWindow(String filePath, double SPEED) {
        vehList = new ArrayList<>();
        NodeArrayList = new ArrayList<>();
        speed = SPEED;
        FilePath = filePath;
        EventQueue.invokeLater(() ->
        {
            frame = new WindowFrame();
            frame.setTitle("For DVRPTW");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
    }

    public class WindowFrame extends JFrame {
        private DrawComponent NodeDraw;
        private ControlComponent ComponentControl;
        public WindowFrame() {
            setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
            setLayout(null);    //取消布局管理器
            NodeDraw = new DrawComponent();
            ComponentControl = new ControlComponent();
            NodeDraw.setBounds(0, 0, DEFAULT_WIDTH / 2, DEFAULT_HEIGHT);
            ComponentControl.setBounds(DEFAULT_WIDTH / 2, 0, DEFAULT_WIDTH / 2, DEFAULT_HEIGHT);
            add(NodeDraw);
            add(ComponentControl);
            /**设定图标
             * Image img = new ImageIcon("icon.gif").getImage();
             * setIconImage(img);
             * */
        }
    }

    public class DrawComponent extends JPanel {
        // 定义组件变量
        private double ComponentWidth, ComponentHeight, minX, minY, WidthRatio, HeightRatio;
        private Graphics2D g2;
        private ArrayList<Integer[]> nodeIndex = new ArrayList<>();
        private ArrayList<ArrayList<Integer[]>> vehIndex = new ArrayList<>();
        public void paintComponent(Graphics g) {
            g2 = (Graphics2D) g;
            drawContent();
        }

        public void drawContent()
        {
            setSize(new Dimension((int) (DEFAULT_WIDTH / 2), DEFAULT_HEIGHT));
            ComponentHeight = (double) (DEFAULT_HEIGHT) / 1.1;
            ComponentWidth = ComponentHeight;
            // draw all vehicles
            if (vehIndex.size()!=0) drawVeh();
            // draw all nodes
            if (nodeIndex.size()!=0) drawNode();
            else {
                nodeIndex = new ArrayList<>();
                File file = new File(FilePath);// 如果这个路径是文件夹
                File[] files = file.listFiles();
                String tmp = files[0].getPath();
                file = new File(tmp);
                files = file.listFiles();
                tmp = files[0].getPath();
                DataExtraction.readTxtFile(tmp, NodeArrayList,speed);
                setNodeIndex();
                drawNode();
            }
            // draw the rectangular DEFAULT_HEIGHT/1.2
            Rectangle2D r = new Rectangle2D.Double(8.0, 8.0, ComponentWidth + 8.0, ComponentHeight + 8.0);
            g2.draw(r);
        }

        public void drawVeh(){
            for (ArrayList<Integer[]> vehindex:vehIndex){
                if (vehindex.size()<=3)g2.setColor(Color.RED);
                else if(vehindex.size()<=8) g2.setColor(Color.GREEN);
                else if(vehindex.size()<=15) g2.setColor(Color.BLUE);
                else g2.setColor(Color.CYAN);
                for (Integer[] index:vehindex){
                    g2.drawLine(index[0],index[1],index[2],index[3]);
                }
            }
            g2.setColor(Color.BLACK);
        }

        public void setVehIndex(){
            vehIndex = new ArrayList<>();
            for (Vehicle veh:vehList){
                ArrayList<Integer[]> tmp = new ArrayList<>();
                for (int i=0;i<veh.getVehSize();i++){
                    Integer[] index = new Integer[4];
                    if (i!=0){
                        index[0] = (int)((veh.getNode(i-1).getX() - minX) * WidthRatio + 10);;
                        index[1] = (int)((veh.getNode(i-1).getY() - minY) * HeightRatio + 10);
                        index[2] = (int)((veh.getNode(i).getX() - minX) * WidthRatio + 10);
                        index[3] = (int)((veh.getNode(i).getY() - minY) * HeightRatio + 10);
                        tmp.add(index);
                    }
                }
                vehIndex.add(tmp);
            }
        }
        public void drawNode(){
            for (Integer[] index : nodeIndex) {
                Ellipse2D circle = new Ellipse2D.Double();
                circle.setFrameFromCenter(index[0],index[1],index[0]+3.0,index[1]+3.0);
                g2.fill(circle);
                g2.draw(circle);
                g2.drawString(Integer.toString(index[2]),index[0]+3,index[1]-3);
            }
        }

        public void setNodeIndex() {
            double maxX = NodeArrayList.get(0).getX();
            double maxY = NodeArrayList.get(0).getY();
            minX = NodeArrayList.get(0).getX();
            minY = NodeArrayList.get(0).getY();
            for (Node node : NodeArrayList) {
                if (maxX < node.getX()) maxX = node.getX();
                if (maxY < node.getY()) maxY = node.getY();
                if (minX > node.getX()) minX = node.getX();
                if (minY > node.getY()) minY = node.getY();
            }
            WidthRatio = ComponentWidth / (maxX - minX);
            HeightRatio = ComponentHeight / (maxY - minY);
            nodeIndex = new ArrayList<>();
            for (Node node : NodeArrayList) {
                Integer[] index = new Integer[3];
                index[0] = (int)((node.getX() - minX) * WidthRatio + 10);
                index[1] = (int)((node.getY() - minY) * HeightRatio + 10);
                index[2] = node.getNodeNum();
                nodeIndex.add(index);
            }

        }

        public void cleanNodeIndex(){
            nodeIndex = new ArrayList<>();
        }
        public void cleanVehIndex(){
            vehIndex = new ArrayList<>();
        }
    }

    public class ControlComponent extends JPanel {
        private JLabel SelectNodeNum;
        private JLabel SelectMap;
        private JLabel SelectMethod;
        private JLabel SelectGenerateOriginalMethod;
        private JLabel TotalTime;
        private JLabel TotalTimeResult;
        private JLabel IterationNum;
        private JLabel IterationNumResult;
        private JLabel OriginalGroup;
        private JLabel OriginalGroupResult;
        private JComboBox<String> SelectNodeCombox;
        private JComboBox<String> SelectMapCombox;
        private JButton SaveMethod;
        private JButton SaveproMethod;
        private JButton ACOMethod;
        private JButton Min2Method;
        private JButton Ga;
        private JButton opt2;
        private JButton Tabu;
        private JButton Vsn;
        private JButton Start;
        private JButton Log;
        private JButton RePaint;
        private JButton Analyse;
        private JButton ClearDrawMap;

        private JTable table;
        private JScrollPane scrollPane;
        // 列名
        private String[] columnName = new String[]{"客户数量", "网络结构", "总费用", "总路径长度", "迭代用时", "迭代次数"};
        // 表格具体数据
        public String[][] columnDate = new String[][]{
                {"100", "C101", "0", "0", "0", "0", "0"},
        };

        private int LocalWidth = DEFAULT_WIDTH / 10;
        private int LocalHeight = DEFAULT_HEIGHT / 10;

        OriginalSolutionMethod osm = null;

        public ControlComponent() {
            setLayout(null);
            Font font = new Font("宋体", Font.BOLD, 70);
            setFont(font);
            // 第一行
            SelectNodeNum = new JLabel("选择网络规模");
            SelectNodeCombox = new JComboBox<String>();
            SelectNodeCombox.setEditable(false);
            getFileContent(FilePath,SelectNodeCombox);
            SelectNodeCombox.addItemListener(new ItemListener()
            {
                public void itemStateChanged(ItemEvent event)
                {
                    switch (event.getStateChange())
                    {
                        case ItemEvent.SELECTED:
                            FilePP = (String) event.getItem();
                            FilePP = FilePath + "\\" + FilePP;
                            SelectMapCombox.removeAllItems();
                            getFileContent(FilePP,SelectMapCombox);
                            break;
                    }
                }
            });

            TotalTime = new JLabel("总用时");
            IterationNum = new JLabel("总迭代次数");
            OriginalGroup = new JLabel("初始种群规模");

            SelectNodeNum.setBounds(0, 0, LocalWidth, LocalHeight);
            SelectNodeCombox.setBounds(LocalWidth, 0, LocalWidth, LocalHeight);
            TotalTime.setBounds(LocalWidth * 2, 0, LocalWidth, LocalHeight);
            IterationNum.setBounds(LocalWidth * 3, 0, LocalWidth, LocalHeight);
            OriginalGroup.setBounds(LocalWidth * 4, 0, LocalWidth, LocalHeight);

            // 第二列
            SelectMap = new JLabel("选择网络结构");
            SelectMapCombox = new JComboBox<>();
            SelectMapCombox.setEditable(false);
            getFileContent(FilePP,SelectMapCombox);
            SelectMapCombox.addItemListener(new ItemListener()
            {
                public void itemStateChanged(ItemEvent event)
                {
                    switch (event.getStateChange())
                    {
                        case ItemEvent.SELECTED:
                            FilePPP = FilePP + "\\" + event.getItem();
                            // 重新绘制图像
                            DataExtraction.readTxtFile(FilePPP, NodeArrayList,speed);
                            frame.NodeDraw.cleanVehIndex();
                            frame.NodeDraw.cleanNodeIndex();
                            frame.NodeDraw.setNodeIndex();
                            frame.NodeDraw.removeAll();
                            frame.NodeDraw.repaint();
                            frame.NodeDraw.validate();
                            break;
                    }
                }
            });
            TotalTimeResult = new JLabel("0");
            IterationNumResult = new JLabel("0");
            OriginalGroupResult = new JLabel("0");

            SelectMap.setBounds(0, LocalHeight, LocalWidth, LocalHeight);
            SelectMapCombox.setBounds(LocalWidth, LocalHeight, LocalWidth, LocalHeight);
            TotalTimeResult.setBounds(LocalWidth * 2, LocalHeight, LocalWidth, LocalHeight);
            IterationNumResult.setBounds(LocalWidth * 3, LocalHeight, LocalWidth, LocalHeight);
            OriginalGroupResult.setBounds(LocalWidth * 4, LocalHeight, LocalWidth, LocalHeight);
            // 第三列
            SelectGenerateOriginalMethod = new JLabel("初始解算法");
            ArrayList<JButton> ChainOne = new ArrayList<>();
            SaveMethod = new JButton("节约值法");
            ChainOne.add(SaveMethod);
            SaveMethod.addActionListener(new ButionListener(ChainOne,SaveMethod,"Original"));

            SaveproMethod = new JButton("节约值法pro");
            ChainOne.add(SaveproMethod);
            SaveproMethod.addActionListener(new ButionListener(ChainOne,SaveproMethod,"Original"));

            ACOMethod = new JButton("蚁群算法");
            ACOMethod.addActionListener(new ButionListener(ChainOne,ACOMethod,"Original"));
            ChainOne.add(ACOMethod);

            Min2Method = new JButton("Insert");
            Min2Method.addActionListener(new ButionListener(ChainOne,Min2Method,"Original"));
            ChainOne.add(Min2Method);

            SelectGenerateOriginalMethod.setBounds(0, LocalHeight * 2, LocalWidth, LocalHeight);
            SaveMethod.setBounds(LocalWidth, LocalHeight * 2, LocalWidth, LocalHeight);
            SaveproMethod.setBounds(LocalWidth * 2, LocalHeight * 2, LocalWidth, LocalHeight);
            ACOMethod.setBounds(LocalWidth * 3, LocalHeight * 2, LocalWidth, LocalHeight);
            Min2Method.setBounds(LocalWidth * 4, LocalHeight * 2, LocalWidth, LocalHeight);

            // 第四列
            SelectMethod = new JLabel("优化算法");
            ArrayList<JButton> ChainTwo = new ArrayList<>();
            Ga = new JButton("GA");
            ChainTwo.add(Ga);
            Ga.addActionListener(new ButionListener(ChainTwo,Ga,"Optimization"));

            opt2 = new JButton("2-OPT*");
            ChainTwo.add(opt2);
            opt2.addActionListener(new ButionListener(ChainTwo,opt2,"Optimization"));

            Tabu = new JButton("Tabu");
            ChainTwo.add(Tabu);
            Tabu.addActionListener(new ButionListener(ChainTwo,Tabu,"Optimization"));

            Vsn = new JButton("VSN");
            ChainTwo.add(Vsn);
            Vsn.addActionListener(new ButionListener(ChainTwo,Vsn,"Optimization"));

            SelectMethod.setBounds(0, LocalHeight * 3, LocalWidth, LocalHeight);
            Ga.setBounds(LocalWidth, LocalHeight * 3, LocalWidth, LocalHeight);
            opt2.setBounds(LocalWidth * 2, LocalHeight * 3, LocalWidth, LocalHeight);
            Tabu.setBounds(LocalWidth * 3, LocalHeight * 3, LocalWidth, LocalHeight);
            Vsn.setBounds(LocalWidth * 4, LocalHeight * 3, LocalWidth, LocalHeight);
            // 第五列
            Start = new JButton("开始");
            Start.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    frame.NodeDraw.cleanVehIndex();
                    frame.NodeDraw.removeAll();
                    frame.NodeDraw.repaint();
                    frame.NodeDraw.validate();
                    try {
                        osm = new OriginalSolutionMethod(NodeArrayList, originalSolution, optimizationAlgorithm,speed);
                        if (testmode==true) osm.setTestmode();
                        osm.cal();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    } catch (ClassNotFoundException classNotFoundException) {
                        classNotFoundException.printStackTrace();
                    }
                    frame.NodeDraw.cleanVehIndex();
                    vehList = osm.getVeh();
                    frame.NodeDraw.setVehIndex();
                    frame.NodeDraw.removeAll();
                    frame.NodeDraw.repaint();
                    frame.NodeDraw.validate();
                }
            });
            Log = new JButton("记录");
            RePaint = new JButton("重绘");
            RePaint.addMouseListener(new MouseAdapter() {
                 public void mouseClicked(MouseEvent e) {
                     if (e.getClickCount() == 1) {
                         frame.NodeDraw.cleanVehIndex();
                         frame.NodeDraw.cleanNodeIndex();
                         frame.NodeDraw.setVehIndex();
                         frame.NodeDraw.removeAll();
                         frame.NodeDraw.repaint();
                         frame.NodeDraw.validate();
                     }
                 }
             });

            Analyse = new JButton("分析");
            ClearDrawMap = new JButton("清除");
            ClearDrawMap.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    //frame.NodeDraw.removeAll();
                }
            });


            Start.setBounds(0, LocalHeight * 4, LocalWidth, LocalHeight);
            Log.setBounds(LocalWidth, LocalHeight * 4, LocalWidth, LocalHeight);
            RePaint.setBounds(LocalWidth * 2, LocalHeight * 4, LocalWidth, LocalHeight);
            Analyse.setBounds(LocalWidth * 3, LocalHeight * 4, LocalWidth, LocalHeight);
            ClearDrawMap.setBounds(LocalWidth * 4, LocalHeight * 4, LocalWidth, LocalHeight);
            {
                // 1st row
                add(SelectNodeNum);
                add(SelectNodeCombox);
                add(TotalTime);
                add(IterationNum);
                add(OriginalGroup);
                //2nd row
                add(SelectMap);
                add(SelectMapCombox);
                add(TotalTimeResult);
                add(IterationNumResult);
                add(OriginalGroupResult);
                // 3rd row
                add(SelectGenerateOriginalMethod);
                add(SaveMethod);
                add(SaveproMethod);
                add(ACOMethod);
                add(Min2Method);
                // 4th row
                add(SelectMethod);
                add(Ga);
                add(opt2);
                add(Tabu);
                add(Vsn);
                // 5th row
                add(Start);
                add(Log);
                add(RePaint);
                add(Analyse);
                add(ClearDrawMap);
            }
            // 记录框
            table = new JTable(columnDate, columnName);
            scrollPane = new JScrollPane(table);
            scrollPane.setBounds(0, LocalHeight * 5, LocalWidth * 5, LocalHeight * 5);
            add(scrollPane);
        }
        public class ButionListener implements ActionListener {
            private Color FGC = Color.WHITE;
            private Color BGC = Color.BLUE;
            private ArrayList<JButton> Chain;
            private JButton choosen;
            private String mode;
            public ButionListener(ArrayList<JButton> chain, JButton selected,
                                  String setmode)
            {
                this.Chain = chain;
                this.choosen = selected;
                this.mode = setmode;
            }
            public void actionPerformed(ActionEvent e) {
                for(JButton button:Chain)
                {
                    button.setForeground(Color.BLACK);
                }
                choosen.setForeground(Color.RED);
                if(mode=="Original"){
                    originalSolution = choosen.getText();
                }else{
                    optimizationAlgorithm = choosen.getText();
                }
            }
        }
    }

    public void getFileContent(String filename, JComboBox<String> cb1) {
        File file = new File(filename);// 如果这个路径是文件夹
        // 获取路径下的所有文件
        File[] files = file.listFiles();
        String fname = filename;
        fname = fname.replaceAll("\\\\","");
        for (int i = 0; i < files.length; i++) {// 如果还是文件夹 递归获取里面的文件 文件夹
            String temp = files[i].getPath();
            temp = temp.replaceAll("\\\\","");
            temp = temp.replaceAll(fname,"");
            cb1.addItem(temp);
        }
    }
}
