package com.reprocess.grid_50;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.reprocess.grid_100.PoiCode;
import com.reprocess.grid_100.util.Color;
import com.reprocess.grid_100.util.NumJudge;
import com.reprocess.grid_100.util.RowColCalculation;
import com.svail.db.db;
import net.sf.json.JSONObject;
import utils.FileTool;
import utils.UtilFile;

import java.util.*;

/**
 * 生成特征统计像元的程序.
 */
public class GridElementInvestment {
    public static void main(String[] args){
        JSONObject condition=new JSONObject();
        condition.put("N",1);
        condition.put("year","2016");
        condition.put("month","05");
        condition.put("source","woaiwojia");
        condition.put("export_collName","BasicData_Resold_100");
        condition.put("import_collName","GridData_Resold_Investment_50");

        getInvestment(condition);
    }
    public static void getInvestment(JSONObject condition){

        callDataFromMongo(condition);

        //statisticCode();

        ergodicStatistics(condition);

        System.out.println("ok!");
    }

    //注意，统计不同月份的数据的时候，全局变量要清空！！！
    public static Map<Integer,Map<String,Integer>> code_houseType_map=new HashMap<>();
    public static Map<Integer,Map<String,Integer>> code_direction_map=new HashMap<>();
    public static Map<Integer,Map<String,Integer>> code_floors_map=new HashMap<>();
    public static Map<Integer,Map<String,Integer>> code_area_map=new HashMap<>();
    public static Map<Integer,Map<String,Integer>> code_price_map=new HashMap<>();
    public static Map<Integer,Map<String,Integer>> code_unitprice_map=new HashMap<>();
    public static Map<Integer,Map<String,Integer>> code_flooron_map=new HashMap<>();
    public static TreeSet<Integer> codesSet= new TreeSet<>();
    public static JSONObject total=new JSONObject();

    //1：将每个格网的数据（obj）存储在codelists_map中，其中key是格网code，value是装了所有房源数据的list
    public static void callDataFromMongo(JSONObject condition){
        int N=condition.getInt("N");

        String collName_export=condition.getString("export_collName");
        DBCollection coll_export = db.getDB().getCollection(collName_export);

        BasicDBObject document = new BasicDBObject();
        Iterator<String> it=condition.keys();
        while(it.hasNext()){
            String key = it.next();
            String value=condition.getString(key);
            if(key.equals("year")||key.equals("month")||key.equals("source")){
                document.put(key,value);
            }
        }

        DBCursor cursor = coll_export.find(document);

        String poi;
        JSONObject obj;
        double lng;
        double lat;
        int code;
        int row;
        int col;

        String house_type;
        String area;
        String floors;
        String direction;
        String flooron;
        String price;
        String unit_price;


        int count=0;
        if(cursor.hasNext()) {
            while (cursor.hasNext()){
                BasicDBObject cs = (BasicDBObject)cursor.next();
                poi=cs.toString();
                obj= JSONObject.fromObject(poi);
                obj.remove("_id");
                lat=obj.getDouble("lat");
                lng=obj.getDouble("lng");

                String[] result=PoiCode.setPoiCode_50(lat,lng).split(",");
                code=Integer.parseInt(result[0]);
                row=Integer.parseInt(result[1]);
                col=Integer.parseInt(result[2]);

                obj.put("code",code);
                obj.put("row",row);
                obj.put("col",col);

                codesSet.add(code);


                if(obj.containsKey("house_type")){
                    house_type=obj.getString("house_type");
                    setAttributeMap(code,house_type,code_houseType_map);
                }

                if(obj.containsKey("direction")){
                    direction=obj.getString("direction");
                    setAttributeMap(code,direction,code_direction_map);
                }

                if(obj.containsKey("floors")){
                    floors=obj.getString("floors");
                    setAttributeMap(code,floors,code_floors_map);
                }

                if(obj.containsKey("flooron")){
                    flooron=obj.getString("flooron");
                    setAttributeMap(code,flooron,code_flooron_map);
                }

                if(obj.containsKey("area")){
                    area=obj.getString("area");
                    boolean num= NumJudge.isNum(area);
                    if(num){
                        setAttributeMap(code,area,code_area_map);
                    }else {
                        System.out.println(code+":"+area);
                    }

                }

                if(obj.containsKey("price")){
                    price=obj.getString("price");
                    boolean num= NumJudge.isNum(price);
                    if(num){
                        setAttributeMap(code,price,code_price_map);
                    }else {
                        System.out.println(code+":"+price);
                    }
                }

                if(obj.containsKey("unit_price")){
                    unit_price=obj.getString("unit_price");
                    boolean num= NumJudge.isNum(unit_price);
                    if(num){
                        setAttributeMap(code,unit_price,code_unitprice_map);
                    }else {
                        System.out.println(code+":"+unit_price);
                    }
                }
                ++count;
            }
        }
        System.out.println("共有" +count+ "条数据");
    }

    //2:遍历整个code_houseType_map，计算每个网格里边的户型的个数，并生成json格式数据
    public static void statisticCode(){
        stasticAttributeNum(code_houseType_map);
        stasticAttributeNum(code_direction_map);
        stasticAttributeNum(code_floors_map);
        stasticAttributeNum(code_flooron_map);
        stasticAttributeNum(code_area_map);
        stasticAttributeNum(code_price_map);
        stasticAttributeNum(code_unitprice_map);
    }

    //3、遍历所有网格，汇总每一个网格的统计信息
    public static void ergodicStatistics(JSONObject condition){

        JSONObject obj;
        double weight_area=0;
        double weight_price=0;
        double weight_unitprice=0;
        int row;
        int col;
        int[] rc=new int[2];
        int code;
        Object[] codeslist=codesSet.toArray();

        String collName_import=condition.getString("import_collName");
        DBCollection coll_import = db.getDB().getCollection(collName_import);
        BasicDBObject document;
        int index=0;
        int documentcount=0;
        for(int i=0;i<codeslist.length;i++){

            code=(int)codeslist[i];
            obj=new JSONObject();
            document = new BasicDBObject();
            if(code_houseType_map.containsKey(code)){
                index++;
                Map<String,Integer> houseType=code_houseType_map.get(code);
                String type=getAttributeJson(houseType);
                //obj.put("houseType",type);
                document.put("houseType",type);
            }

            if(code_direction_map.containsKey(code)){
                index++;
                Map<String,Integer> direction=code_direction_map.get(code);
                String dir=getAttributeJson(direction);
                document.put("direction",dir);
            }

            if(code_floors_map.containsKey(code)){
                index++;
                Map<String,Integer> floors=code_floors_map.get(code);
                String floor=getAttributeJson(floors);
                document.put("floors",floor);
            }

            if(code_flooron_map.containsKey(code)){
                index++;
                Map<String,Integer> flooron=code_flooron_map.get(code);
                String fo=getAttributeJson(flooron);
                document.put("flooron",fo);
            }

            if(code_area_map.containsKey(code)){
                index++;
                Map<String,Integer> area=code_area_map.get(code);
                String ar=getAttributeJson(area);

                weight_area=getInvestmentThreshold(area);
                document.put("area",ar);

            }

            if(code_price_map.containsKey(code)){
                index++;
                Map<String,Integer> price=code_price_map.get(code);
                String pr=getAttributeJson(price);

                weight_price=getInvestmentThreshold(price);
                document.put("price",pr);
            }

            if(code_unitprice_map.containsKey(code)){
                index++;
                Map<String,Integer> unitprice=code_unitprice_map.get(code);
                String up=getAttributeJson(unitprice);

                weight_unitprice=getInvestmentThreshold(unitprice);
                document.put("unitprice",up);
            }

            if(index!=0){
                document.put("code",code);
                rc=RowColCalculation.Code_RowCol(code,1);
                row=rc[0];
                col=rc[1];
                document.put("row",row);
                document.put("col",col);
                document.put("weight_price",weight_price);
                document.put("weight_unitprice",weight_area*weight_unitprice);

                Iterator<String> it=condition.keys();
                while(it.hasNext()){
                    String key = it.next();
                    String value=condition.getString(key);
                    if(key.equals("year")||key.equals("month")||key.equals("source")){
                        document.put(key,value);
                    }
                }

                //System.out.println(document);
                //将数据存入50*50的源数据表GridData_Resold_Investment_50中
                DBCursor rls =coll_import.find(document);
                if(rls == null || rls.size() == 0){
                    documentcount++;
                    coll_import.insert(document);
                }else{
                    System.out.println("该数据已经存在!");
                }
                //FileTool.Dump(document.toString(),"D:\\test\\栅格特征统计.txt","utf-8");
            }
        }
        System.out.println("一共导入"+documentcount+"条数据");
    }

    //建立一个map，其中key为code，value是一个属性值——个数的一个子map
    public static void setAttributeMap(int code,String attribute,Map<Integer,Map<String,Integer>> map){
        if(map.containsKey(code)){

            Map<String,Integer> num_map=map.get(code);
            if(num_map.containsKey(attribute)){
                int num=num_map.get(attribute);
                num_map.put(attribute,++num);
                map.put(code,num_map);

            }else {
                num_map.put(attribute,1);
                map.put(code,num_map);
            }

        }else {
            Map<String,Integer> num_map=new HashMap<>();
            num_map.put(attribute,1);
            map.put(code,num_map);
        }
    }

    //验证所有的统计结果是否与总的数据的相符
    public static void stasticAttributeNum(Map<Integer,Map<String,Integer>> map){
        int code;
        String attribute="";
        int num;
        Map<String,Integer> attribute_num;

        int count=0;
        for(Map.Entry<Integer,Map<String,Integer>> entry:map.entrySet()){
            code=entry.getKey();
            attribute_num=entry.getValue();

            JSONObject obj=new JSONObject();
            obj.put("code",code);
            for(Map.Entry<String,Integer> entry1:attribute_num.entrySet()){
                attribute=entry1.getKey();
                num=entry1.getValue();
                obj.put(attribute,num);
                count+=num;
            }
            // System.out.println(obj);
        }
        System.out.println("共有"+count+"条"+attribute+"信息");
    }

    //遍历code下的子map，并将所有的值以json形式返回,这里统计的是每个属性所含有的个数
    public static String getAttributeJson(Map<String,Integer> attribute){

        String attr;
        int num;
        String obj="";
        JSONObject object=new JSONObject();
        for(Map.Entry<String,Integer> entry:attribute.entrySet()){
            attr=entry.getKey();
            num=entry.getValue();

            //object.put(attr,num);
            obj+=attr+","+num+";";
        }
        return obj;
    }

    public static double getInvestmentThreshold(Map<String,Integer> attribute){

        String attr;
        int num;
        int totalnum=0;
        double ratio=0;
        double weightresult=0;
        for(Map.Entry<String,Integer> entry:attribute.entrySet()){
            num=entry.getValue();
            totalnum+=num;
        }

        for(Map.Entry<String,Integer> entry:attribute.entrySet()){
            attr=entry.getKey();
            num=entry.getValue();

            if(totalnum!=0){
                ratio=(double)num/(double)totalnum;
            }

            weightresult+=ratio*Double.parseDouble(attr);
        }
        return weightresult;
    }
}
