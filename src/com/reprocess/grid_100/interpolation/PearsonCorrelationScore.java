package com.reprocess.grid_100.interpolation;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import utils.FileTool;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;
/**
 * Created by ZhouXiang on 2016/9/5.
 */
public class PearsonCorrelationScore {
    public static Map<String, Map<String, Double>> dataset = null;

    /**
     * 初始化数据集
     */
    public static void initDataSet(String selfcode,String adjacent_code,JSONObject self_timeseries,JSONObject adjacent_codedata) {
        dataset = new HashMap<String, Map<String, Double>>();

        // 初始化self_timeseries数据集
        Map<String, Double> selfMap = new HashMap<String, Double>();
        Iterator it = self_timeseries.keys();
        String key="";
        double value=0;
        if(it.hasNext()){
            while (it.hasNext()){
                key=(String) it.next();
                value=self_timeseries.getDouble(key);
                selfMap.put(key, value);
            }

        }
        dataset.put(selfcode, selfMap);

        // 初始化adjacent_codedata数据集
        Map<String, Double> compareMap = new HashMap<String, Double>();
        it = adjacent_codedata.keys();
        if(it.hasNext()){
            while (it.hasNext()){
                key=(String) it.next();
                //System.out.println(key);
                value=adjacent_codedata.getDouble(key);
                compareMap.put(key, value);
            }

        }
        dataset.put(adjacent_code, compareMap);
    }

    public Map<String, Map<String, Double>> getDataSet() {
        return dataset;
    }

    /**
     * @param person1
     *            name
     * @param person2
     *            name
     * @return 皮尔逊相关度值
     */
    public static double sim_pearson(String person1, String person2) {
        // 找出双方都有的数据,（皮尔逊算法要求）
        List<String> list = new ArrayList<String>();
        for (Entry<String, Double> p1 : dataset.get(person1).entrySet()) {
            if (dataset.get(person2).containsKey(p1.getKey())) {
                list.add(p1.getKey());
            }
        }

        double sumX = 0.0;
        double sumY = 0.0;
        double sumX_Sq = 0.0;
        double sumY_Sq = 0.0;
        double sumXY = 0.0;
        int N = list.size();

        for (String name : list) {
            Map<String, Double> p1Map = dataset.get(person1);
            Map<String, Double> p2Map = dataset.get(person2);

            sumX += p1Map.get(name);
            sumY += p2Map.get(name);
            sumX_Sq += Math.pow(p1Map.get(name), 2);
            sumY_Sq += Math.pow(p2Map.get(name), 2);
            sumXY += p1Map.get(name) * p2Map.get(name);
        }

        double numerator = sumXY - sumX * sumY / N;
        double denominator = Math.sqrt((sumX_Sq - sumX * sumX / N)
                * (sumY_Sq - sumY * sumY / N));

        // 分母不能为0
        if (denominator == 0) {
            return 0;
        }

        return numerator / denominator;
    }

    public static void main(String[] args) {
        /*PearsonCorrelationScore pearsonCorrelationScore = new PearsonCorrelationScore();
        System.out.println(pearsonCorrelationScore.sim_pearson("Lisa Rose",
                "Gene Seymour"));*/

        /*String testpath=PearsonCorrelationScore.class.getResource("").toString().replace("file:/","");
        System.out.println(testpath);*/
        String testpath="D:\\github.com\\bigdataXiang\\HousePriceServer\\src\\com\\reprocess\\grid_100\\interpolation\\";
        Vector<String> testfile= FileTool.Load(testpath+"testfile.txt","utf-8");
        String result=testfile.elementAt(0);
       // System.out.println(result);
        JSONObject single_code=JSONObject.fromObject(result);
        String interest_code="";
        JSONObject single_code_value=new JSONObject();
        JSONObject self_timeseries=new JSONObject();
        JSONArray adjacent_array=new JSONArray();
        JSONObject adjacent_codedata=new JSONObject();
        String adjacent_code="";
        JSONObject adjacent_code_timeseries=new JSONObject();

        Iterator iterator=single_code.keys();
        Iterator adjacent_codes;
        if(iterator.hasNext()){

            while (iterator.hasNext()){
                interest_code=(String) iterator.next();
                single_code_value=single_code.getJSONObject(interest_code);
                self_timeseries=single_code_value.getJSONObject("self_timeseries");
                adjacent_array=single_code_value.getJSONArray("adjacent_array");

                for(int i=0;i<adjacent_array.size();i++){
                    adjacent_codedata=adjacent_array.getJSONObject(i);
                    adjacent_codes=adjacent_codedata.keys();
                    while (adjacent_codes.hasNext()){
                        adjacent_code=(String) adjacent_codes.next();//"44153"
                        //System.out.println(adjacent_code);
                    }

                    //求解邻接code对应的时间序列
                    adjacent_code_timeseries=adjacent_codedata.getJSONObject(adjacent_code);

                    //初始化两个数据序列的值
                    initDataSet(interest_code,adjacent_code,self_timeseries,adjacent_code_timeseries);
                    //计算两者的皮尔逊相关系数
                    double r=sim_pearson(interest_code,adjacent_code);
                    System.out.println(interest_code+"与"+adjacent_code+"的相关系数是："+r);

                }

            }
        }
    }

}
