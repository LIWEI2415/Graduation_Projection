package com.lw.work;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class show_door {
    final String fileURL="LWTest1\\src\\com\\lw\\material\\医院CT室2018.ifc";//文件所在相对位置
    final String usefulStart="DATA;";//有用的开始
    final int lengthOfStart=7;//indexOf只能取到“D”的位置，要往后补位置
    final String usefulEnd="ENDSEC;";//有用的结束
    final String jsonPath="D:\\a学习资料\\毕设\\demo\\html_json\\door.json";//存放json文件的绝对位置
    public static void main(String[] args){
        show_door project=new show_door();
        String string=project.showParameters();
        System.out.println(string);
        project.createJSON();
    }
    /*
    读取wallRevit.ifc文件，加"\r\n"是为了每段自己换行
     */
    private static String readFileContent(String fileName) {
        File file = new File(fileName);
        BufferedReader reader = null;
        StringBuilder sbf = new StringBuilder();
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempStr;
            while ((tempStr = reader.readLine()) != null) {
                sbf.append(tempStr).append("\r\n");
            }
            reader.close();
            return sbf.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return sbf.toString();
    }
    /*
    截取ifc文件中DATA到ENDSEC中的关键内容
     */
    private String getUsefulFilePart(){

        String iniFile=readFileContent(fileURL);
        String ultimaFile;
        int startNum=iniFile.indexOf(usefulStart)+lengthOfStart;
        int endNum=iniFile.lastIndexOf(usefulEnd);
        ultimaFile=iniFile.substring(startNum,endNum);
        return ultimaFile;
    }
    /*
    为了得到想要的参数(json文件的内容）
     */
    private String showParameters(){
        String jsonText = null;
        ArrayList<String> points3D=new ArrayList<>();//一切坐标
        ArrayList<String> vectors=new ArrayList<>();//一切向量
        String ultimaFile=getUsefulFilePart();
        //使用HashMap存储，k为实体加编号，v为后面的参数
        Map<String, ArrayList<String>> map=new HashMap<>();
        String k;
        ArrayList<String> v;
        //crudeV为还没剪切的”参数群“
        String crudeV;
        //将全文分行
        String[] lines=ultimaFile.split("\r\n");
        //for循环为了获得k,v
        for (String line : lines) {
            //从每一行第一个”（“进行k,crudeV的划分
            int i = line.indexOf("(");
            StringBuilder group = new StringBuilder();
            k = line.substring(0, i);
            v =new ArrayList<>();
            crudeV = line.substring(i + 1, line.length() - 2);
            //再在crudeV中根据”，“划分，得到crudeV1
            String[] crudeV1 = crudeV.split(",");
            //for循环就是为了整理crudeV1中的参数
            for (int p = 0; p < crudeV1.length; p++) {

                //if语句是为了将例如（0，1，2）这样的参数进行合并（crudeV1中会被分为三组）
                if (crudeV1[p].contains("(")) {
                    while (!crudeV1[p].contains(")")) {
                        group.append(crudeV1[p]);
                        group.append(",");
                        p++;
                    }
                    group.append(crudeV1[p]);
                    v.add(group.toString());
                } else {
                    v.add(crudeV1[p]);
                }
            }
            map.put(k, v);
        }

        //将V中的#+数字替换成对应的参数
        for(String s:map.keySet()){
            if(s.contains("#268622")){//！！！这个判断方法在ifc文件中实体超过1000000时可能会出错，建议提取出数字部分用equals
                //从key中得到value
                ArrayList<String> list=map.get(s);
                for(int n=0;n<list.size();){
                    //str表示V中的每个参数
                    String str=list.get(n);
                    //正则表达式寻找#+数字
                    Pattern pattern = Pattern.compile("#\\d+");
                    Matcher matcher = pattern.matcher(str);
                    StringBuilder all = new StringBuilder();
                    while(matcher.find()){
                        all.append(matcher.group());
                    }
                    //提取出数字
                    String[] strs= all.toString().split("#");
                    //就算V中没有#，也会分成一个字符串数组，所以此处就直接下一个
                    if(strs.length==1){
                        n++;
                    }
                    //else是为了将#+数字替换，不要n++是为了重新再次检查替换的参数中是否还需要替换
                    else {
                        for(int i=1;i<strs.length;i++){
                            String number="#"+strs[i];
                            if(number.equals("#92463")){
                                list.set(n,list.get(n).replace(number,""));
                                continue;
                            }
                            for(String key:map.keySet()){
                                Matcher matcher1=Pattern.compile("#\\d+").matcher(key);
                                String key1= "";
                                while (matcher1.find()){
                                    key1=matcher1.group();
                                }
                                if(key1.equals(number)){
                                    ArrayList<String> value=map.get(key);
                                    String val=String.join(",",value);
                                    //在原来的list中修改
                                    list.set(n,list.get(n).replace(number,val));
                                    //先得到所有的三维坐标
                                    if(key.contains("IFCCARTESIANPOINT")){//！！！这个方法在有共同前缀的时候会出错，最好能使用equals
                                        points3D.add(map.get(key).get(0));
                                    }
                                    if(key.contains("IFCDIRECTION")){//！！！这个方法在有共同前缀的时候会出错，最好能使用equals
                                        vectors.add(map.get(key).get(0));
                                    }
                                    break;
                                }
                            }

                        }
                    }
                }
                map.put(s,list);
            }
        }
//        for(Map.Entry<String,ArrayList<String>> entry:map.entrySet()){
//            System.out.println(entry.getKey());
//            System.out.println(entry.getValue());
//        }
        jsonText="{" + "\n" +
                "\t" + "\"" + "参考坐标系原点位置" + "\"" + ":" + "\"" + points3D.get(0) + "\"" + "," + "\n" +
                "\t" + "\"" + "参考坐标系Z轴方向" + "\"" + ":" + "\"" + vectors.get(0) + "\"" + "," + "\n" +
                "\t" + "\"" + "参考坐标系X轴方向" + "\"" + ":" + "\"" + vectors.get(1) + "\"" + "," + "\n" +
                "\t" + "\"" + "局部坐标系原点坐标" + "\"" + ":" + "\"" + points3D.get(1) + "\"" + "," + "\n" +
                "\t" + "\"" + "门原点坐标" + "\"" + ":" + "\"" + points3D.get(2) + "\"" + "," + "\n" +
                "\t" + "\"" + "门Z轴方向" + "\"" + ":" + "\"" + vectors.get(2) + "\"" + "," + "\n" +
                "\t" + "\"" + "门X轴方向" + "\"" + ":" + "\"" + vectors.get(3) + "\"" + "," + "\n" +
                "\t" + "\"" + "面一" + "\"" + ":" + "["+"\"" + points3D.get(7) + "\"" + ","+
                                                        "\"" + points3D.get(8) + "\"" + "," +
                                                        "\"" + points3D.get(9) + "\"" + ","+
                                                        "\"" + points3D.get(10) + "\"" + "]"+","+"\n" +
                "\t" + "\"" + "面二" + "\"" + ":" + "["+"\"" + points3D.get(11) + "\"" + ","+
                                                        "\"" + points3D.get(12) + "\"" + "," +
                                                        "\"" + points3D.get(13) + "\"" + ","+
                                                        "\"" + points3D.get(14) + "\"" + "]"+","+"\n" +
                "\t" + "\"" + "面三" + "\"" + ":" + "["+"\"" + points3D.get(15) + "\"" + ","+
                                                        "\"" + points3D.get(16) + "\"" + "," +
                                                        "\"" + points3D.get(17) + "\"" + ","+
                                                        "\"" + points3D.get(18) + "\"" + "]"+","+"\n" +
                "\t" + "\"" + "面四" + "\"" + ":" + "["+"\"" + points3D.get(19) + "\"" + ","+
                                                        "\"" + points3D.get(20) + "\"" + "," +
                                                        "\"" + points3D.get(21) + "\"" + ","+
                                                        "\"" + points3D.get(22) + "\"" + "]"+","+"\n" +
                "\t" + "\"" + "面五" + "\"" + ":" + "["+"\"" + points3D.get(23) + "\"" + ","+
                                                        "\"" + points3D.get(24) + "\"" + "," +
                                                        "\"" + points3D.get(25) + "\"" + ","+
                                                        "\"" + points3D.get(26) + "\"" + "]"+","+"\n" +
                "\t" + "\"" + "面六" + "\"" + ":" + "["+"\"" + points3D.get(27) + "\"" + ","+
                                                        "\"" + points3D.get(28) + "\"" + "," +
                                                        "\"" + points3D.get(29) + "\"" + ","+
                                                        "\"" + points3D.get(30) + "\"" + "]"+"\n" +
                "}";
        return jsonText;
    }
    /*
    创建json文件
    */
    private void createJSON(){
        String content=showParameters();
        try {
            File file=new File(jsonPath);
            if (!file.getParentFile().exists())
            { // 如果父目录不存在，创建父目录
                file.getParentFile().mkdirs();
            }
            if (file.exists()) { // 如果已存在,删除旧文件
                file.delete();
            }
            file.createNewFile();
            Writer writer=new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
            writer.write(content);
            writer.flush();
            writer.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

}
