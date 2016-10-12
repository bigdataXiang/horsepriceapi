package com.reprocess.grid_100.util;

/**
 * Created by ZhouXiang on 2016/10/12.
 */
public class Resolution {

    public static int getResolution(int zoom){
        int N=0;
        switch(zoom){
            case 18:N=1;
                break;
            case 17:N=2;
                break;
            case 16:N=4;
                break;
            case 15:N=5;
                break;
            case 14:N=8;
                break;
            case 13:N=10;
                break;
            case 12:N=20;
                break;
            case 11:N=30;
                break;
            case 10:N=40;
                break;
        }
        return N;
    }
}
