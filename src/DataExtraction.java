import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 从给定格式文件中提取客户信息
 * @version 1.01 2020-11-22
 * @author infy-zero
 */
public class DataExtraction {
    public static void readTxtFile(String filePath , ArrayList<Node> nodeArrayList, double speed) {
        /**
         * 功能：Java读取txt文件的内容
         * 步骤：1：先获得文件句柄
         * 2：获得文件句柄当做是输入一个字节码流，需要对这个输入流进行读取
         * 3：读取到输入流后，需要读取生成字节流
         * 4：一行一行的输出。readline()。
         * 备注：需要考虑的是异常情况
         * @param filePath 文件路径
         */
        nodeArrayList.clear();
        try {
            String encoding="GBK";
            File file=new File(filePath);
            if(file.isFile() && file.exists()){ //判断文件是否存在
                InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file),encoding);//考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                int i = 0;
                while((lineTxt = bufferedReader.readLine()) != null){
                    i ++ ;
                    if(i>=10){// 从第11行开始记录
                        ArrayList<String> numbers = new ArrayList<String>();
                        // 正则表达式匹配数字字符
                        Pattern p = Pattern.compile("\\d+");
                        Matcher m = p.matcher(lineTxt);
                        while (m.find()) {
                            numbers.add(m.group());
                        }
                        // 创建Node并放入nodeArrayList
                        if(numbers.size()<5) continue;
                        Node newnode = new Node(numbers,speed);
                        nodeArrayList.add(newnode);
                    }
                }
                read.close();
            }else{
                System.out.println("找不到指定的文件");
            }
        } catch (Exception e) {
            System.out.println("读取文件内容出错");
            e.printStackTrace();
        }
    }

    public static void main(String argv[]){
        // String filePath = "D:\\JAVA_WORKPLACE\\vrp_data\\VRPTW_solomon\\solomon_100\\C101.txt";
        String RootFilePath = "D:\\JAVA_WORKPLACE\\vrp_data\\VRPTW_solomon";
        //ArrayList<Node> nodeArrayList = new ArrayList<Node>();//用来存放node
        double speed = 1.0;
        //readTxtFile(filePath, nodeArrayList,speed);
        VrpWindow window = new VrpWindow(RootFilePath,speed);
    }
}
