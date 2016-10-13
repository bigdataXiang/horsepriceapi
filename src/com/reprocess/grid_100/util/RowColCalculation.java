package com.reprocess.grid_100.util;

/**
 * Created by ZhouXiang on 2016/10/12.
 */
public class RowColCalculation {

    public static double width=0.0011785999999997187;//每100m的经度差
    public static double length=9.003999999997348E-4;//每100m的纬度差

    /**通过最东的坐标获取当前范围内的最小列号*/
    public static int getColMin(double west){
        int colmin=(int) Math.ceil((west-115.417284)/width);
        return colmin;
    }

    /**通过最东的坐标获取当前范围内的最大列号*/
    public static int getColMax(double east){
        int colmax=(int)Math.ceil((east-115.417284)/width);
        return colmax;
    }

     /**通过最南的坐标获取当前范围内的最小行号*/
    public static int getRowMin(double south){
        int rowmin=(int)Math.ceil((south-39.438283)/length);
        return rowmin;
    }

    /**通过最北的坐标获取当前范围内的最大行号*/
    public static int getRowMax(double north){
        int rowmax=(int)Math.ceil((north-39.438283)/length);
        return rowmax;
    }

    /**建立(N*50)*(N*50)的网格编码与50*50的网格编码的映射关系*/
    public static int[] codeMapping50toN50(int row_50,int col_50,int N){
        int cols=4000;
        int row;
        int col;
        int code;
        int[] result =new int[3];
        double rowtemp=(double)row_50/N;
        row= (int) Math.ceil(rowtemp);
        result[0]=row;
        double coltemp=(double)col_50/N;
        col= (int) Math.ceil(coltemp);
        result[1]=col;
        code=(col + (cols/N)* (row - 1));
        result[2]=code;
        return result;
    }



}
